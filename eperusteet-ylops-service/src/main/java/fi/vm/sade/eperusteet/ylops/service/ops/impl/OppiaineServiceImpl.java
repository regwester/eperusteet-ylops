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
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Opetuksentavoite;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Oppiaine;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Oppiaineenvuosiluokka;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Oppiaineenvuosiluokkakokonaisuus;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.domain.peruste.Peruste;
import fi.vm.sade.eperusteet.ylops.domain.peruste.PerusteOpetuksentavoite;
import fi.vm.sade.eperusteet.ylops.domain.peruste.PerusteOppiaineenVuosiluokkakokonaisuus;
import fi.vm.sade.eperusteet.ylops.domain.teksti.LokalisoituTeksti;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineLaajaDto;
import fi.vm.sade.eperusteet.ylops.repository.ops.OpetussuunnitelmaRepository;
import fi.vm.sade.eperusteet.ylops.repository.ops.OppiaineRepository;
import fi.vm.sade.eperusteet.ylops.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.ylops.service.external.EperusteetService;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.ops.OppiaineService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author mikkom
 */
@Service
@Transactional
public class OppiaineServiceImpl implements OppiaineService {

    @Autowired
    private DtoMapper mapper;

    @Autowired
    private OpsDtoMapper opsDtoMapper;

    @Autowired
    private OpetussuunnitelmaRepository opetussuunnitelmaRepository;

    @Autowired
    private OppiaineRepository oppiaineRepository;

    @Autowired
    private EperusteetService perusteet;

    public OppiaineServiceImpl() {
    }

    @Override
    public void updateVuosiluokkienTavoitteet(Long opsId, Long oppiaineId, Long id, Map<Vuosiluokka, Set<UUID>> tavoitteet) {
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        Peruste peruste = perusteet.getPerusopetuksenPeruste(ops.getPerusteenDiaarinumero());
        Oppiaine oppiaine = oppiaineRepository.findOne(oppiaineId);

        if (!ops.containsOppiaine(oppiaine)) {
            throw new BusinessRuleViolationException("Pyydettyä oppiainetta ei ole opetussuunnitelmassa");
        }

        Oppiaineenvuosiluokkakokonaisuus ovk = oppiaine.getVuosiluokkakokonaisuudet().stream()
            .filter(vk -> vk.getId().equals(id))
            .findAny()
            .orElseThrow(() -> new BusinessRuleViolationException("Pyydettyä oppiainetta ei ole opetussuunnitelmassa"));

        PerusteOppiaineenVuosiluokkakokonaisuus pov
            = peruste.getPerusopetus().getOppiaine(oppiaine.getTunniste())
            .flatMap(po -> po.getVuosiluokkakokonaisuus(ovk.getVuosiluokkakokonaisuus().getId()))
            .orElseThrow(() -> new BusinessRuleViolationException("Oppiainetta tai vuosiluokkakokonaisuutta ei ole perusteessa"));

        oppiaineRepository.lock(oppiaine);
        updateVuosiluokkakokonaisuudenTavoitteet(ovk, pov, tavoitteet);

    }

    @Override
    @Transactional(readOnly = true)
    public List<OppiaineDto> getAll(@P("opsId") Long opsId) {
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        assertExists(ops, "Pyydettyä opetussuunnitelmaa ei ole olemassa");
        return mapper.mapAsList(oppiaineRepository.findByOpsId(opsId), OppiaineDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public OppiaineDto get(@P("opsId") Long opsId, Long id) {
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        assertExists(ops, "Pyydettyä opetussuunnitelmaa ei ole olemassa");
        Oppiaine oppiaine = oppiaineRepository.findOne(id);
        assertExists(ops, "Pyydettyä oppiainetta ei ole olemassa");
        if (!ops.containsOppiaine(oppiaine)) {
            throw new BusinessRuleViolationException("Pyydettyä oppiainetta ei ole opetussuunnitelmassa");
        }

        return mapper.map(oppiaine, OppiaineDto.class);
    }

    @Override
    public OppiaineLaajaDto add(@P("opsId") Long opsId, OppiaineLaajaDto oppiaineDto) {
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        assertExists(ops, "Pyydettyä opetussuunnitelmaa ei ole olemassa");
        opetussuunnitelmaRepository.lock(ops);
        Oppiaine oppiaine = opsDtoMapper.fromDto(oppiaineDto);
        oppiaine = oppiaineRepository.save(oppiaine);
        ops.addOppiaine(oppiaine);
        return mapper.map(oppiaine, OppiaineLaajaDto.class);
    }

    @Override
    public OppiaineDto add(@P("opsId") Long opsId, OppiaineDto oppiaineDto) {
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        assertExists(ops, "Pyydettyä opetussuunnitelmaa ei ole olemassa");
        opetussuunnitelmaRepository.lock(ops);
        Oppiaine oppiaine = opsDtoMapper.fromDto(oppiaineDto);
        oppiaine = oppiaineRepository.save(oppiaine);
        ops.addOppiaine(oppiaine);
        return mapper.map(oppiaine, OppiaineDto.class);
    }

    @Override
    public OppiaineDto update(@P("opsId") Long opsId, OppiaineDto oppiaineDto) {
        Oppiaine oppiaine = oppiaineRepository.findOne(oppiaineDto.getId());
        assertExists(oppiaine, "Pyydettyä oppiainetta ei ole olemassa");

        // lockService.assertLock ( opsId ) ... ?
        oppiaineRepository.lock(oppiaine);

        mapper.map(oppiaineDto, oppiaine);

        oppiaine = oppiaineRepository.save(oppiaine);
        return mapper.map(oppiaine, OppiaineDto.class);
    }

    @Override
    public void delete(@P("opsId") Long opsId, Long id) {
        Oppiaine oppiaine = oppiaineRepository.findOne(id);
        assertExists(oppiaine, "Pyydettyä oppiainetta ei ole olemassa");
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        assertExists(ops, "Pyydettyä opetussuunnitelmaa ei ole olemassa");
        if (!ops.containsOppiaine(oppiaine)) {
            throw new BusinessRuleViolationException("Pyydettyä oppiainetta ei ole opetussuunnitelmassa");
        }

        oppiaineRepository.lock(oppiaine);

        if (oppiaine.isKoosteinen()) {
            oppiaine.getOppimaarat().forEach(oppimaara -> delete(opsId, oppimaara.getId()));
        }

        if (oppiaine.getOppiaine() != null) {
            oppiaine.getOppiaine().removeOppimaara(oppiaine);
        } else {
            ops.removeOppiaine(oppiaine);
        }

        oppiaineRepository.delete(oppiaine);
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

        if ( !vuosiluokkakokonaisuus.getVuosiluokkaKokonaisuus().getVuosiluokat().containsAll(tavoitteet.keySet()) ) {
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
            });

    }

    private void mergePerusteTavoitteet(Oppiaineenvuosiluokka ov, PerusteOppiaineenVuosiluokkakokonaisuus pvk, Set<UUID> tavoiteIds) {
        List<PerusteOpetuksentavoite> filtered = pvk.getTavoitteet().stream()
            .filter(t -> tavoiteIds.contains(t.getTunniste()))
            .collect(Collectors.toList());

        if ( tavoiteIds.size() > filtered.size() ) {
            throw new BusinessRuleViolationException("Yksi tai useampi tavoite ei kuulu oppiaineen vuosiluokkakokonaisuuden tavoitteisiin");
        }

        LinkedHashMap<UUID, Keskeinensisaltoalue> alueet = pvk.getSisaltoalueet().stream()
            .filter(s -> filtered.stream().flatMap(t -> t.getSisaltoalueet().stream()).anyMatch(Predicate.isEqual(s)))
            .map(ps -> {
                return ov.getSisaltoalue(ps.getTunniste()).orElseGet(() -> {
                    Keskeinensisaltoalue k = new Keskeinensisaltoalue();
                    k.setTunniste(ps.getTunniste());
                    k.setKuvaus(LokalisoituTeksti.of(ps.getKuvaus().getTekstit()));
                    k.setNimi(LokalisoituTeksti.of(ps.getNimi().getTekstit()));
                    return k;
                });
            })
            .collect(Collectors.toMap(k -> k.getTunniste(), k -> k, (u, v) -> u, LinkedHashMap::new));

        ov.setSisaltoalueet(new ArrayList<>(alueet.values()));

        List<Opetuksentavoite> tmp = filtered.stream()
            .map(t -> {
                Opetuksentavoite opst = ov.getTavoite(t.getTunniste()).orElseGet(() -> {
                    Opetuksentavoite uusi = new Opetuksentavoite();
                    uusi.setTavoite(LokalisoituTeksti.of(t.getTavoite().getTekstit()));
                    uusi.setTunniste(t.getTunniste());
                    return uusi;
                });
                opst.setLaajattavoitteet(t.getLaajattavoitteet().stream()
                    .map(l -> new LaajaalainenosaaminenViite(l.getTunniste().toString()))
                    .collect(Collectors.toSet()));
                opst.setSisaltoalueet(t.getSisaltoalueet().stream()
                    .map(s -> alueet.get(s.getTunniste()))
                    .collect(Collectors.toSet()));
                return opst;
            })
            .collect(Collectors.toList());
        ov.setTavoitteet(tmp);
    }

}
