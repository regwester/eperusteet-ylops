/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.ylops.service.ops.impl;

import fi.vm.sade.eperusteet.ylops.domain.LaajaalainenosaaminenViite;
import fi.vm.sade.eperusteet.ylops.domain.Vuosiluokka;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Keskeinensisaltoalue;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Opetuksenkohdealue;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Opetuksentavoite;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Oppiaine;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Oppiaineenvuosiluokka;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Oppiaineenvuosiluokkakokonaisuus;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Tavoitteenarviointi;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.domain.peruste.Peruste;
import fi.vm.sade.eperusteet.ylops.domain.peruste.PerusteOpetuksentavoite;
import fi.vm.sade.eperusteet.ylops.domain.peruste.PerusteOppiaineenVuosiluokkakokonaisuus;
import fi.vm.sade.eperusteet.ylops.domain.teksti.LokalisoituTeksti;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Tekstiosa;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineLaajaDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineenVuosiluokkaDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineenVuosiluokkakokonaisuusDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.ylops.repository.ops.OpetussuunnitelmaRepository;
import fi.vm.sade.eperusteet.ylops.repository.ops.OppiaineRepository;
import fi.vm.sade.eperusteet.ylops.repository.ops.OppiaineenvuosiluokkaRepository;
import fi.vm.sade.eperusteet.ylops.repository.ops.OppiaineenvuosiluokkakokonaisuusRepository;
import fi.vm.sade.eperusteet.ylops.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.ylops.service.exception.LockingException;
import fi.vm.sade.eperusteet.ylops.service.external.EperusteetService;
import fi.vm.sade.eperusteet.ylops.service.locking.AbstractLockService;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.ops.OppiaineService;
import fi.vm.sade.eperusteet.ylops.service.ops.OpsOppiaineCtx;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author mikkom
 */
@Service
@Transactional
public class OppiaineServiceImpl extends AbstractLockService<OpsOppiaineCtx> implements OppiaineService {

    @Autowired
    private DtoMapper mapper;

    @Autowired
    private OpsDtoMapper opsDtoMapper;

    @Autowired
    private OpetussuunnitelmaRepository opetussuunnitelmaRepository;

    @Autowired
    private OppiaineRepository oppiaineet;

    @Autowired
    private EperusteetService perusteet;

    @Autowired
    private OppiaineenvuosiluokkaRepository vuosiluokat;

    @Autowired
    private OppiaineenvuosiluokkakokonaisuusRepository kokonaisuudet;

    public OppiaineServiceImpl() {
    }

    @Override
    public void updateVuosiluokkienTavoitteet(Long opsId, Long oppiaineId, Long vlkId, Map<Vuosiluokka, Set<UUID>> tavoitteet) {
        Oppiaine oppiaine = getOppiaine(opsId, oppiaineId);
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        Peruste peruste = perusteet.getPerusopetuksenPeruste(ops.getPerusteenDiaarinumero());

        Oppiaineenvuosiluokkakokonaisuus ovk = oppiaine.getVuosiluokkakokonaisuudet().stream()
            .filter(vk -> vk.getId().equals(vlkId))
            .findAny()
            .orElseThrow(() -> new BusinessRuleViolationException("Pyydettyä oppiainetta ei ole opetussuunnitelmassa"));

        PerusteOppiaineenVuosiluokkakokonaisuus pov
            = peruste.getPerusopetus().getOppiaine(oppiaine.getTunniste())
            .flatMap(po -> po.getVuosiluokkakokonaisuus(ovk.getVuosiluokkakokonaisuus().getId()))
            .orElseThrow(() -> new BusinessRuleViolationException("Oppiainetta tai vuosiluokkakokonaisuutta ei ole perusteessa"));

        oppiaineet.lock(oppiaine);
        updateVuosiluokkakokonaisuudenTavoitteet(ovk, pov, tavoitteet);

    }

    @Override
    @Transactional(readOnly = true)
    public List<OppiaineDto> getAll(@P("opsId") Long opsId) {
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        assertExists(ops, "Pyydettyä opetussuunnitelmaa ei ole olemassa");
        return mapper.mapAsList(oppiaineet.findByOpsId(opsId), OppiaineDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public OppiaineDto get(@P("opsId") Long opsId, Long id) {
        Oppiaine oppiaine = getOppiaine(opsId, id);
        return mapper.map(oppiaine, OppiaineDto.class);
    }

    @Override
    public OppiaineLaajaDto add(@P("opsId") Long opsId, OppiaineLaajaDto oppiaineDto) {
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        assertExists(ops, "Pyydettyä opetussuunnitelmaa ei ole olemassa");
        opetussuunnitelmaRepository.lock(ops);
        Oppiaine oppiaine = opsDtoMapper.fromDto(oppiaineDto);
        oppiaine = oppiaineet.save(oppiaine);
        ops.addOppiaine(oppiaine);
        return mapper.map(oppiaine, OppiaineLaajaDto.class);
    }

    @Override
    public OppiaineDto add(@P("opsId") Long opsId, OppiaineDto oppiaineDto) {
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        assertExists(ops, "Pyydettyä opetussuunnitelmaa ei ole olemassa");
        opetussuunnitelmaRepository.lock(ops);
        Oppiaine oppiaine = opsDtoMapper.fromDto(oppiaineDto);
        oppiaine = oppiaineet.save(oppiaine);
        ops.addOppiaine(oppiaine);
        return mapper.map(oppiaine, OppiaineDto.class);
    }

    @Override
    public OppiaineDto update(@P("opsId") Long opsId, OppiaineDto oppiaineDto) {
        Oppiaine oppiaine = getOppiaine(opsId, oppiaineDto.getId());

        // lockService.assertLock ( opsId ) ... ?
        oppiaineet.lock(oppiaine);

        mapper.map(oppiaineDto, oppiaine);

        oppiaine = oppiaineet.save(oppiaine);
        return mapper.map(oppiaine, OppiaineDto.class);
    }

    @Override
    public void delete(@P("opsId") Long opsId, Long id) {
        Oppiaine oppiaine = getOppiaine(opsId, id);
        oppiaineet.lock(oppiaine);

        if (oppiaine.isKoosteinen()) {
            oppiaine.getOppimaarat().forEach(oppimaara -> delete(opsId, oppimaara.getId()));
        }

        if (oppiaine.getOppiaine() != null) {
            oppiaine.getOppiaine().removeOppimaara(oppiaine);
        } else {
            Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
            ops.removeOppiaine(oppiaine);
        }

        oppiaineet.delete(oppiaine);
    }

    @Override
    public OppiaineenVuosiluokkaDto getVuosiluokka(Long opsId, Long oppiaineId, Long vuosiluokkaId) {
        if ( !oppiaineet.exists(opsId, oppiaineId) ) {
            throw new BusinessRuleViolationException("Opetussuunnitelmaa tai oppiainetta ei ole.");
        }
        Oppiaineenvuosiluokka vl = vuosiluokat.findByOppiaine(oppiaineId, vuosiluokkaId);
        return vl == null ? null : mapper.map(vl, OppiaineenVuosiluokkaDto.class);
    }


    @Override
    public OppiaineenVuosiluokkakokonaisuusDto updateVuosiluokkakokonaisuudenSisalto(@P("opsId") Long opsId, Long id, OppiaineenVuosiluokkakokonaisuusDto dto) {
        Oppiaine oppiaine = getOppiaine(opsId, id);
        Oppiaineenvuosiluokkakokonaisuus oavlk
            = oppiaine.getVuosiluokkakokonaisuudet().stream()
            .filter(ov -> ov.getId().equals(dto.getId()))
            .findAny()
            .orElseThrow(() -> new BusinessRuleViolationException("Pyydettyä oppiaineen vuosiluokkakokonaisuutta ei löydy"));

        oavlk.setTehtava(mapper.map(dto.getTehtava(), Tekstiosa.class));
        oavlk.setTyotavat(mapper.map(dto.getTyotavat(), Tekstiosa.class));
        oavlk.setOhjaus(mapper.map(dto.getOhjaus(), Tekstiosa.class));
        oavlk.setArviointi(mapper.map(dto.getArviointi(), Tekstiosa.class));

        mapper.map(oavlk, dto);
        return dto;
    }

    @Override
    public OppiaineenVuosiluokkaDto updateVuosiluokanSisalto(@P("opsId") Long opsId, Long id, OppiaineenVuosiluokkaDto dto) {
        Oppiaine oppiaine = getOppiaine(opsId, id);
        Oppiaineenvuosiluokka oppiaineenVuosiluokka
            = oppiaine.getVuosiluokkakokonaisuudet().stream()
            .map(oavlk -> oavlk.getVuosiluokka(dto.getVuosiluokka()))
            .flatMap(vl -> vl.map(Stream::of).orElseGet(Stream::empty))
            .findFirst()
            .orElseThrow(() -> new BusinessRuleViolationException("Pyydettyä oppiaineen vuosiluokkaa ei löydy"));

        if (!oppiaineenVuosiluokka.getId().equals(dto.getId())) {
            throw new BusinessRuleViolationException("Annetun vuosiluokan ID ei vastaa olemassaolevan vuosiluokan vastaavaa");
        }

        // Aseta oppiaineen vuosiluokan sisällöstä vain sisaltoalueiden ja tavoitteiden kuvaukset,
        // noin muutoin sisältöön ei pidä kajoaman
        dto.getSisaltoalueet().forEach(
            sisaltoalueDto
            -> oppiaineenVuosiluokka.getSisaltoalue(sisaltoalueDto.getTunniste())
            .ifPresent(sa -> sa.setKuvaus(mapper.map(sisaltoalueDto.getKuvaus(), LokalisoituTeksti.class))));
        dto.getTavoitteet().forEach(
            tavoiteDto
            -> oppiaineenVuosiluokka.getTavoite(tavoiteDto.getTunniste())
            .ifPresent(t -> t.setTavoite(mapper.map(tavoiteDto.getTavoite(), LokalisoituTeksti.class))));

        mapper.map(oppiaineenVuosiluokka, dto);
        return dto;
    }

    private Oppiaine getOppiaine(Long opsId, Long oppiaineId) {
        Oppiaine oppiaine = oppiaineet.findOne(oppiaineId);
        assertExists(oppiaine, "Pyydettyä oppiainetta ei ole olemassa");
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        assertExists(ops, "Pyydettyä opetussuunnitelmaa ei ole olemassa");
        if (!ops.containsOppiaine(oppiaine)) {
            throw new BusinessRuleViolationException("Pyydettyä oppiainetta ei ole opetussuunnitelmassa");
        }
        return oppiaine;
    }

    private static void assertExists(Object o, String msg) {
        if (o == null) {
            throw new BusinessRuleViolationException(msg);
        }
    }

    private void updateVuosiluokkakokonaisuudenTavoitteet(
        Oppiaineenvuosiluokkakokonaisuus v,
        PerusteOppiaineenVuosiluokkakokonaisuus vuosiluokkakokonaisuus,
        Map<Vuosiluokka, Set<UUID>> tavoitteet) {

        if (!vuosiluokkakokonaisuus.getVuosiluokkaKokonaisuus().getVuosiluokat().containsAll(tavoitteet.keySet())) {
            throw new BusinessRuleViolationException("Yksi tai useampi vuosiluokka ei kuulu tähän vuosiluokkakokonaisuuteen");
        }

        tavoitteet.entrySet().stream()
            .filter(e -> v.getVuosiluokkakokonaisuus().getVuosiluokat().contains(e.getKey()))
            .forEach(e -> {
                Oppiaineenvuosiluokka ov = v.getVuosiluokka(e.getKey()).orElseGet(() -> {
                    Oppiaineenvuosiluokka tmp = new Oppiaineenvuosiluokka(e.getKey());
                    v.addVuosiluokka(tmp);
                    return tmp;
                });
                mergePerusteTavoitteet(ov, vuosiluokkakokonaisuus, e.getValue());
                if (ov.getTavoitteet().isEmpty()) {
                    v.removeVuosiluokka(ov);
                }
            });
    }

    private void mergePerusteTavoitteet(Oppiaineenvuosiluokka ov, PerusteOppiaineenVuosiluokkakokonaisuus pvk, Set<UUID> tavoiteIds) {
        List<PerusteOpetuksentavoite> filtered = pvk.getTavoitteet().stream()
            .filter(t -> tavoiteIds.contains(t.getTunniste()))
            .collect(Collectors.toList());

        if (tavoiteIds.size() > filtered.size()) {
            throw new BusinessRuleViolationException("Yksi tai useampi tavoite ei kuulu oppiaineen vuosiluokkakokonaisuuden tavoitteisiin");
        }

        LinkedHashMap<UUID, Keskeinensisaltoalue> alueet = pvk.getSisaltoalueet().stream()
            .filter(s -> filtered.stream().flatMap(t -> t.getSisaltoalueet().stream()).anyMatch(Predicate.isEqual(s)))
            .map(ps -> ov.getSisaltoalue(ps.getTunniste()).orElseGet(() -> {
                Keskeinensisaltoalue k = new Keskeinensisaltoalue();
                k.setTunniste(ps.getTunniste());
                k.setNimi(fromDto(ps.getNimi()));
                // Kuvaus-kenttä on paikaillisesti määritettävää sisältöä joten sitä ei tässä aseteta
                return k;
            }))
            .collect(Collectors.toMap(Keskeinensisaltoalue::getTunniste, k -> k, (u, v) -> u, LinkedHashMap::new));

        ov.setSisaltoalueet(new ArrayList<>(alueet.values()));

        List<Opetuksentavoite> tmp = filtered.stream()
            .map(t -> {
                Opetuksentavoite opst = ov.getTavoite(t.getTunniste()).orElseGet(() -> {
                    Opetuksentavoite uusi = new Opetuksentavoite();
                    // Tavoite-kenttä on paikaillisesti määritettävää sisältöä joten sitä ei tässä aseteta
                    uusi.setTunniste(t.getTunniste());
                    return uusi;
                });
                opst.setLaajattavoitteet(t.getLaajattavoitteet().stream()
                    .map(l -> new LaajaalainenosaaminenViite(l.getTunniste().toString()))
                    .collect(Collectors.toSet()));
                opst.setSisaltoalueet(t.getSisaltoalueet().stream()
                    .map(s -> alueet.get(s.getTunniste()))
                    .collect(Collectors.toSet()));
                opst.setKohdealueet(t.getKohdealueet().stream()
                    .map(k -> ov.getKokonaisuus().getOppiaine().addKohdealue(new Opetuksenkohdealue(fromDto(k.getNimi()))))
                    .collect(Collectors.toSet()));
                opst.setArvioinninkohteet(t.getArvioinninkohteet().stream()
                    .map(a -> new Tavoitteenarviointi(fromDto(a.getArvioinninKohde()),fromDto(a.getHyvanOsaamisenKuvaus())))
                    .collect(Collectors.toSet()));
                return opst;
            })
            .collect(Collectors.toList());
        ov.setTavoitteet(tmp);
    }

    private LokalisoituTeksti fromDto(LokalisoituTekstiDto dto) {
        if ( dto == null ) { return null; }
        return LokalisoituTeksti.of(dto.getTekstit());
    }

    @Override
    protected Long getLockId(OpsOppiaineCtx ctx) {
        if (ctx.getKokonaisuusId() == null) {
            return ctx.getOppiaineId();
        }
        if (ctx.getVuosiluokkaId() == null) {
            return ctx.getKokonaisuusId();
        }
        return ctx.getVuosiluokkaId();
    }

    @Override
    protected int latestRevision(OpsOppiaineCtx ctx) {
        if (ctx.getKokonaisuusId() == null) {
            return oppiaineet.getLatestRevisionId(ctx.getOppiaineId());
        }
        if (ctx.getVuosiluokkaId() == null) {
            return kokonaisuudet.getLatestRevisionId(ctx.getKokonaisuusId());
        }
        return vuosiluokat.getLatestRevisionId(ctx.getVuosiluokkaId());
    }

    @Override
    protected Long validateCtx(OpsOppiaineCtx ctx, boolean readOnly) {
        if (ctx.isValid() && oppiaineet.exists(ctx.getOpsId(), ctx.getOppiaineId())) {
            if (ctx.isOppiane()) {
                return ctx.getOppiaineId();
            }
            if (ctx.isKokonaisuus() && kokonaisuudet.exists(ctx.getOppiaineId(), ctx.getKokonaisuusId())) {
                return ctx.getKokonaisuusId();
            }
            if (ctx.isVuosiluokka() && vuosiluokat.exists(ctx.getOppiaineId(), ctx.getKokonaisuusId(), ctx.getVuosiluokkaId())) {
                return ctx.getVuosiluokkaId();
            }
        }
        throw new LockingException("Virheellinen lukitus");
    }

}
