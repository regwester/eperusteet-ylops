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

import fi.vm.sade.eperusteet.ylops.domain.Tyyppi;
import fi.vm.sade.eperusteet.ylops.domain.lukio.Lukiokurssi;
import fi.vm.sade.eperusteet.ylops.domain.lukio.LukiokurssiTyyppi;
import fi.vm.sade.eperusteet.ylops.domain.lukio.OppiaineLukiokurssi;
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
import fi.vm.sade.eperusteet.ylops.repository.ops.OpetussuunnitelmaRepository;
import fi.vm.sade.eperusteet.ylops.repository.ops.OppiaineRepository;
import fi.vm.sade.eperusteet.ylops.service.external.EperusteetService;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.ops.lukio.LukioOpetussuunnitelmaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

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

    @Override
    @Transactional(readOnly = true)
    public LukioOpetussuunnitelmaRakenneOpsDto getRakenne(long opsId) {
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        PerusteDto perusteDto = eperusteetService.getPeruste(ops.getPerusteenDiaarinumero());
        LukioOpetussuunnitelmaRakenneOpsDto rakenne = new LukioOpetussuunnitelmaRakenneOpsDto();
        rakenne.setMuokattu(ops.getMuokattu());
        rakenne.setOpsId(ops.getId());
        rakenne.setRoot(ops.getPohja() == null || ops.getPohja().getTyyppi() == Tyyppi.POHJA);
        map(ops.getOppiaineet().stream().map(OpsOppiaine::getOppiaine),
            ops.getOppiaineet().stream()
                .collect(toMap(ooa -> ooa.getOppiaine().getId(), OpsOppiaine::isOma))::get,
            rakenne.getOppiaineet(),
            orEmpty(ops.getLukiokurssit().stream()
                .sorted(comparing(OppiaineLukiokurssi::getJarjestys)
                    .thenComparing((OppiaineLukiokurssi oaLk) -> oaLk.getKurssi().getNimi().firstByKieliOrder().orElse("")))
                .collect(groupingBy(k -> k.getOppiaine().getId()))::get));
        rakenne.setPerusteen(perusteDto.getLukiokoulutus().getRakenne());
        return rakenne;
    }

    private void map(Stream<Oppiaine> from, Function<Long,Boolean> isOma, Collection<LukioOppiaineListausDto> to,
                     Function<Long, List<OppiaineLukiokurssi>> lukiokurssiByOppiaineId) {
        from.sorted(comparing((Oppiaine oa) -> ofNullable(oa.getJarjestys()).orElse(0))
                .thenComparing(comparing((Oppiaine oa)-> oa.getNimi().firstByKieliOrder().orElse("")))
            ).forEach(oa -> {
                LukioOppiaineListausDto dto = mapper.map(oa, new LukioOppiaineListausDto());
                dto.setOma(isOma.apply(oa.getId()));
                dto.setKurssiTyyppiKuvaukset(LokalisoituTekstiDto.ofOptionalMap(oa.getKurssiTyyppiKuvaukset()));
                dto.setKurssit(lukiokurssiByOppiaineId.apply(oa.getId()).stream()
                        .map(this::mapKurssi).collect(toList()));
                map(oa.getOppimaaratReal().stream(), child -> dto.isOma(),
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
        Oppiaine oppiaine = mapper.map(dto, new Oppiaine(OppiaineTyyppi.LUKIO));
        if (dto.getKurssiTyyppiKuvaukset() != null) {
            for (Map.Entry<LukiokurssiTyyppi, Optional<LokalisoituTekstiDto>> kv : dto.getKurssiTyyppiKuvaukset().entrySet()) {
                kv.getKey().oppiaineKuvausSetter().set(oppiaine, kv.getValue()
                        .map(tekstiDto -> LokalisoituTeksti.of(tekstiDto.getTekstit())).orElse(null));
            }
        }
        ops.addOppiaine(oppiaine);
        opetussuunnitelmaRepository.flush();
        return oppiaine.getId();
    }
}
