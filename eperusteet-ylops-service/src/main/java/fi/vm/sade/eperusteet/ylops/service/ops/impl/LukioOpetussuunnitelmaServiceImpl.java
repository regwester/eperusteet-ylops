/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.ylops.service.ops.impl;

import fi.vm.sade.eperusteet.ylops.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.ylops.domain.Tyyppi;
import fi.vm.sade.eperusteet.ylops.domain.lukio.*;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Oppiaine;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.OppiaineTyyppi;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.domain.ops.OpsOppiaine;
import fi.vm.sade.eperusteet.ylops.domain.teksti.LokalisoituTeksti;
import fi.vm.sade.eperusteet.ylops.dto.lukio.*;
import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.ylops.dto.peruste.lukio.AihekokonaisuudetDto;
import fi.vm.sade.eperusteet.ylops.dto.peruste.lukio.OpetuksenYleisetTavoitteetDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.ylops.repository.ops.*;
import fi.vm.sade.eperusteet.ylops.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.ylops.service.external.EperusteetService;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.ops.lukio.LukioOpetussuunnitelmaService;
import fi.vm.sade.eperusteet.ylops.service.util.LambdaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.vm.sade.eperusteet.ylops.service.util.LambdaUtil.map;
import static fi.vm.sade.eperusteet.ylops.service.util.LambdaUtil.orEmpty;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;

/**
 * User: tommiratamaa
 * Date: 19.11.2015
 * Time: 15.32
 */
@Service
public class LukioOpetussuunnitelmaServiceImpl implements LukioOpetussuunnitelmaService {
    @Autowired
    private DtoMapper mapper;

    @Autowired
    private OpetussuunnitelmaRepository opetussuunnitelmaRepository;

    @Autowired
    private EperusteetService eperusteetService;

    @Autowired
    private OppiaineLukiokurssiRepository oppiaineLukiokurssiRepository;

    @Autowired
    private LukioOppiaineJarjestysRepository jarjestysRepository;

    @Autowired
    private OpsOppiaineParentViewRepository oppiaineParentViewRepository;

    @Override
    @Transactional(readOnly = true)
    public LukioOpetussuunnitelmaRakenneOpsDto getRakenne(long opsId) {
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        PerusteDto perusteDto = eperusteetService.getPeruste(ops.getPerusteenDiaarinumero());
        LukioOpetussuunnitelmaRakenneOpsDto rakenne = new LukioOpetussuunnitelmaRakenneOpsDto();
        rakenne.setMuokattu(ops.getMuokattu());
        rakenne.setOpsId(ops.getId());
        rakenne.setRoot(ops.getPohja() == null || ops.getPohja().getTyyppi() == Tyyppi.POHJA);
        Map<Long,OpsOppiaineParentView> parentRelateionsByOppiaineId
                = oppiaineParentViewRepository.findByOpetusuunnitelmaId(ops.getId())
                        .stream().collect(toMap(o -> o.getOpsOppiaine().getOppiaineId(), o -> o));
        Map<Long,OppiaineJarjestysDto> jarjestykset =
                jarjestysRepository.findJarjestysDtosByOpetussuunnitelmaId(opsId).stream()
                        .collect(toMap(OppiaineJarjestysDto::getId, o -> o));
        map(ops.getOppiaineet().stream().map(OpsOppiaine::getOppiaine),
            LambdaUtil.map(parentRelateionsByOppiaineId, OpsOppiaineParentView::isOma),
            LambdaUtil.map(parentRelateionsByOppiaineId, o -> o.getPohjanOppiaine() != null),
            LambdaUtil.map(jarjestykset, OppiaineJarjestysDto::getJarjestys),
            rakenne.getOppiaineet(),
            orEmpty(ops.getLukiokurssit().stream()
                .sorted(comparing(OppiaineLukiokurssi::getJarjestys)
                    .thenComparing((OppiaineLukiokurssi oaLk) -> oaLk.getKurssi().getNimi().firstByKieliOrder().orElse("")))
                .collect(groupingBy(k -> k.getOppiaine().getId()))::get));
        rakenne.setPerusteen(perusteDto.getLukiokoulutus().getRakenne());
        return rakenne;
    }

    private void map(Stream<Oppiaine> from, Function<Long,Boolean> isOma,
                     Function<Long,Boolean> isMaariteltyPohjassa,
                     Function<Long,Integer> jarjestys,
                     Collection<LukioOppiaineListausDto> to,
                     Function<Long, List<OppiaineLukiokurssi>> lukiokurssiByOppiaineId) {
        from.sorted(comparing((Oppiaine oa) -> ofNullable(jarjestys.apply(oa.getId())).orElse(Integer.MAX_VALUE))
                .thenComparing(comparing((Oppiaine oa)-> oa.getNimi().firstByKieliOrder().orElse("")))
            ).forEach(oa -> {
                LukioOppiaineListausDto dto = mapper.map(oa, new LukioOppiaineListausDto());
                dto.setOma(isOma.apply(oa.getId()));
                dto.setMaariteltyPohjassa(isMaariteltyPohjassa.apply(oa.getId()));
                dto.setKurssiTyyppiKuvaukset(LokalisoituTekstiDto.ofOptionalMap(oa.getKurssiTyyppiKuvaukset()));
                dto.setKurssit(lukiokurssiByOppiaineId.apply(oa.getId()).stream()
                        .map(this::mapKurssi).collect(toList()));
                map(oa.getOppimaaratReal().stream(), childId -> dto.isOma(),
                        childId -> dto.isMaariteltyPohjassa(), jarjestys,
                        dto.getOppimaarat(), lukiokurssiByOppiaineId);
                to.add(dto);
            });
    }

    private LukiokurssiOpsDto mapKurssi(OppiaineLukiokurssi oaLk) {
        LukiokurssiOpsDto kurssiDto = mapper.map(oaLk.getKurssi(), new LukiokurssiOpsDto());
        kurssiDto.setOma(oaLk.isOma());
        return kurssiDto;
    }

    @Override
    @Transactional(readOnly = true)
    public AihekokonaisuudetPerusteOpsDto getAihekokonaisuudet(long opsId) {
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        PerusteDto perusteDto = eperusteetService.getPeruste(ops.getPerusteenDiaarinumero());
        return new AihekokonaisuudetPerusteOpsDto(
            mapper.map(perusteDto.getLukiokoulutus().getAihekokonaisuudet(), AihekokonaisuudetDto.class),
            mapper.map(ops.getAihekokonaisuudet(), AihekokonaisuudetOpsDto.class)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OpetuksenYleisetTavoitteetPerusteOpsDto getOpetuksenYleisetTavoitteet(long opsId) {
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        PerusteDto perusteDto = eperusteetService.getPeruste(ops.getPerusteenDiaarinumero());
        return new OpetuksenYleisetTavoitteetPerusteOpsDto(
            mapper.map(perusteDto.getLukiokoulutus().getOpetuksenYleisetTavoitteet(),
                    OpetuksenYleisetTavoitteetDto.class),
            mapper.map(ops.getAihekokonaisuudet(),
                    OpetuksenYleisetTavoitteetOpsDto.class)
        );
    }

    @Override
    @Transactional
    public long saveOppiaine(long opsId, LukioOppiaineSaveDto dto) {
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        opetussuunnitelmaRepository.lock(ops);

        Oppiaine oppiaine = mapper.map(dto, new Oppiaine(OppiaineTyyppi.LUKIO));
        if (dto.getKurssiTyyppiKuvaukset() != null) {
            for (Map.Entry<LukiokurssiTyyppi, Optional<LokalisoituTekstiDto>> kv : dto.getKurssiTyyppiKuvaukset().entrySet()) {
                kv.getKey().oppiaineKuvausSetter().set(oppiaine, kv.getValue()
                        .map(tekstiDto -> LokalisoituTeksti.of(tekstiDto.getTekstit())).orElse(null));
            }
        }
        if (dto.getOppiaineId() != null) {
            OpsOppiaine parent = ops.getOppiaineet().stream().filter(o -> o.getOppiaine().getId().equals(dto.getOppiaineId()))
                    .findAny().orElseThrow(() -> new BusinessRuleViolationException("Oppiainetta oppimäärälle " +
                            "ei löydy Opetussuunnitelmasta: " + dto.getOppiaineId()));
            if (!parent.getOppiaine().isKoosteinen()) {
                throw new BusinessRuleViolationException("Yritetään lisätä ei koosteiseen oppiaineeeseen.");
            }
            oppiaine.setKoosteinen(false); // ei sallita lapsia oppimäärälle
            parent.getOppiaine().addOppimaara(oppiaine);
        } else {
            ops.addOppiaine(oppiaine);
        }
        opetussuunnitelmaRepository.flush();
        ops.getOppiaineJarjestykset().add(new LukioOppiaineJarjestys(ops, oppiaine, null));
        return oppiaine.getId();
    }

    @Override
    @Transactional
    public void updateTreeStructure(Long opsId, OppaineKurssiTreeStructureDto structureDto) {
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        opetussuunnitelmaRepository.lock(ops);

        Map<Long, LukioOppiaineJarjestys> byOppiaineId = jarjestysRepository.findByOpetussuunnitelmaId(ops.getId()).stream()
                .collect(toMap(j -> j.getId().getOppiaineId(), j -> j));
        for (OppiaineJarjestysDto oppiaineJarjestysDto : structureDto.getOppiaineet()) {
            // Täytyy löytyä, muutoin laiton pyyntö:
            if (byOppiaineId.get(oppiaineJarjestysDto.getId()) == null) {
                throw new BusinessRuleViolationException("Oppiaineelta " + oppiaineJarjestysDto.getId()
                    + " puuttuu järjestys.");
            }
            byOppiaineId.get(oppiaineJarjestysDto.getId()).setJarjestys(oppiaineJarjestysDto.getJarjestys());
        }

        Map<Long, Map<Long,OppiaineLukiokurssi>> oppiaineetByKurssiId = ops.getLukiokurssit().stream()
                .collect(groupingBy(oaLk -> oaLk.getKurssi().getId(),
                    mapping(oaLk -> oaLk, toMap(oaLk -> oaLk.getOppiaine().getId(), oaLk -> oaLk))));
        Set<OppiaineLukiokurssi> toBeRemoved = new HashSet<>();
        for (LukiokurssiOppaineMuokkausDto kurssiDto : structureDto.getKurssit()) {
            // Jos kurssitByOppiaineId olisi null, niin käyttöliittymältä on tullut laittomia kurssi id:itä,
            // sillä kurssin on aiemmin ollut pakko kuulua johonkin oppiaineeseen (liittämättömiä ei sallita):
            Map<Long,OppiaineLukiokurssi> kurssitByOppiaineId = oppiaineetByKurssiId.get(kurssiDto.getId());
            Set<Long> oppiaineIds = new HashSet<>(kurssitByOppiaineId.keySet());
            for (KurssinOppiaineDto oaDto : kurssiDto.getOppiaineet()) {
                OppiaineLukiokurssi existing = kurssitByOppiaineId.get(oaDto.getOppiaineId());
                if (existing != null) {
                    existing.setJarjestys(oaDto.getJarjestys());
                } else {
                    // oli jo oltava jossain (koska liittämättömiä ei voi olla):
                    OppiaineLukiokurssi onePrevious = kurssitByOppiaineId.values().iterator().next();
                    ops.getLukiokurssit().add(new OppiaineLukiokurssi(ops,
                        byOppiaineId.get(oaDto.getOppiaineId()).getOppiaine(), // oppiaineen löydyttävä entuudestaan
                        onePrevious.getKurssi(),
                        oaDto.getJarjestys(),
                        onePrevious.isOma() // sama oma-tila kaikkialla OPS:ssa missä samaa kurssia käytetty
                    ));
                }
                oppiaineIds.remove(oaDto.getOppiaineId()); // käsitelty
            }
            // Poistetaan käsittelemättömät oppiaine-liitokset:
            ops.getLukiokurssit().removeAll(oppiaineIds.stream().map(kurssitByOppiaineId::get).collect(toSet()));
        }
    }

}
