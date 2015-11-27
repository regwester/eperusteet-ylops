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

import com.codepoetics.protonpack.StreamUtils;
import fi.vm.sade.eperusteet.ylops.domain.LaajaalainenosaaminenViite;
import fi.vm.sade.eperusteet.ylops.domain.Vuosiluokka;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.*;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.domain.ops.OpsOppiaine;
import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusteOpetuksentavoiteDto;
import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusteOppiaineenVuosiluokkakokonaisuusDto;
import fi.vm.sade.eperusteet.ylops.domain.teksti.LokalisoituTeksti;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Tekstiosa;
import fi.vm.sade.eperusteet.ylops.domain.vuosiluokkakokonaisuus.Vuosiluokkakokonaisuus;
import fi.vm.sade.eperusteet.ylops.dto.ops.*;
import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.TekstiosaDto;
import fi.vm.sade.eperusteet.ylops.repository.ops.*;
import fi.vm.sade.eperusteet.ylops.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.ylops.service.exception.LockingException;
import fi.vm.sade.eperusteet.ylops.service.external.EperusteetService;
import fi.vm.sade.eperusteet.ylops.service.locking.AbstractLockService;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.ops.OppiaineService;
import fi.vm.sade.eperusteet.ylops.service.ops.OpsOppiaineCtx;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import fi.vm.sade.eperusteet.ylops.service.ops.VuosiluokkakokonaisuusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static fi.vm.sade.eperusteet.ylops.service.util.Nulls.assertExists;

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
    private VuosiluokkakokonaisuusRepository kokonaisuudet;

    @Autowired
    private OppiaineenvuosiluokkakokonaisuusRepository oppiaineenKokonaisuudet;

    @Autowired
    private VuosiluokkakokonaisuusService vuosiluokkakokonaisuusService;

    @Autowired
    private OpetuksenkeskeinenSisaltoalueRepository opetuksenkeskeinenSisaltoalueRepository;

    public OppiaineServiceImpl() {
    }

    @Override
    public void updateVuosiluokkienTavoitteet(Long opsId, Long oppiaineId, Long vlkId, Map<Vuosiluokka, Set<UUID>> tavoitteet) {
        Oppiaine oppiaine = getOppiaine(opsId, oppiaineId);
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        //TODO:should we use same version of Peruste for with the Opetuusuunnitelma was based on if available?
        PerusteDto peruste = perusteet.getPeruste(ops.getPerusteenDiaarinumero());

        Oppiaineenvuosiluokkakokonaisuus ovk = oppiaine.getVuosiluokkakokonaisuudet().stream()
            .filter(vk -> vk.getId().equals(vlkId))
            .findAny()
            .orElseThrow(() -> new BusinessRuleViolationException("Pyydettyä oppiainetta ei ole opetussuunnitelmassa"));

        PerusteOppiaineenVuosiluokkakokonaisuusDto pov
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
    public List<OppiaineDto> getAll(@P("opsId") Long opsId, boolean valinnaiset) {
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        assertExists(ops, "Pyydettyä opetussuunnitelmaa ei ole olemassa");
        Set<Oppiaine> aineet = valinnaiset ?
                               oppiaineet.findValinnaisetByOpsId(opsId) :
                               oppiaineet.findYhteisetByOpsId(opsId);
        return mapper.mapAsList(aineet, OppiaineDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OppiaineDto> getAll(@P("opsId") Long opsId, OppiaineTyyppi tyyppi) {
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        assertExists(ops, "Pyydettyä opetussuunnitelmaa ei ole olemassa");
        return mapper.mapAsList(oppiaineet.findByOpsIdAndTyyppi(opsId, tyyppi), OppiaineDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public OpsOppiaineDto get(@P("opsId") Long opsId, Long id) {
        Boolean isOma = oppiaineet.isOma(opsId, id);
        if (isOma == null) {
            throw new BusinessRuleViolationException("Opetussuunnitelmaa tai oppiainetta ei ole.");
        }
        Oppiaine oppiaine = oppiaineet.findOne(id);
        assertExists(oppiaine, "Pyydettyä oppiainetta ei ole olemassa");
        return mapper.map(new OpsOppiaine(oppiaine, isOma), OpsOppiaineDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public OppiaineDto getParent(@P("opsId") Long opsId, Long id) {
        Oppiaine oppiaine = getOppiaine(opsId, id);
        return mapper.map(oppiaine.getOppiaine(), OppiaineDto.class);
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
    public OppiaineDto addCopyOppimaara(@P("opsId") Long opsId, Long oppiaineId, KopioOppimaaraDto kt) {
        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        assertExists(ops, "Pyydettyä opetussuunnitelmaa ei ole olemassa");

        Opetussuunnitelma opspohja = ops.getAlinPohja();

        Oppiaine parent = oppiaineet.findOne(oppiaineId);
        Oppiaine pohjaparent = oppiaineet.findOneByOpsIdAndTunniste(opspohja.getId(), parent.getTunniste());
        Oppiaine uusi = null;

        if (parent.getKoodiArvo().equalsIgnoreCase("KT") && kt.getTunniste() == null) {
            uusi = Oppiaine.copyOf(pohjaparent, false);
            uusi.setNimi(LokalisoituTeksti.of(kt.getOmaNimi().getTekstit()));
            uusi.setKoosteinen(false);
            uusi.setAbstrakti(false);
            parent.addOppimaara(oppiaineet.save(uusi));
        }
        else {
            for (Oppiaine om : pohjaparent.getOppimaarat()) {
                if (om.getTunniste().equals(kt.getTunniste())) {
                    uusi = Oppiaine.copyOf(om);
                    uusi.setNimi(LokalisoituTeksti.of(kt.getOmaNimi().getTekstit()));
                    parent.addOppimaara(oppiaineet.save(uusi));
                    break;
                }
            }
        }

        assertExists(uusi, "Pyydettyä kielitarjonnan oppiainetta ei ole");
        return mapper.map(uusi, OppiaineDto.class);
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
    public OppiaineDto addValinnainen(@P("opsId") Long opsId, OppiaineDto oppiaineDto, Long vlkId,
                                      Set<Vuosiluokka> vuosiluokat, List<TekstiosaDto> tavoitteetDto) {

        OppiaineenVuosiluokkakokonaisuusDto oavlktDto =
            oppiaineDto.getVuosiluokkakokonaisuudet().stream().findFirst().get();
        oppiaineDto.setVuosiluokkakokonaisuudet(Collections.emptySet());

        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        assertExists(ops, "Pyydettyä opetussuunnitelmaa ei ole olemassa");
        opetussuunnitelmaRepository.lock(ops);
        Oppiaine oppiaine = opsDtoMapper.fromDto(oppiaineDto, true);
        oppiaine = oppiaineet.save(oppiaine);
        ops.addOppiaine(oppiaine);

        Vuosiluokkakokonaisuus vlk = kokonaisuudet.findBy(opsId, vlkId);
        assertExists(vlk, "Pyydettyä vuosiluokkakokonaisuutta ei ole olemassa");

        Oppiaineenvuosiluokkakokonaisuus oavlk = new Oppiaineenvuosiluokkakokonaisuus();
        oavlk.setVuosiluokkakokonaisuus(vlk.getTunniste());
        oavlk.setTehtava(mapper.map(oavlktDto.getTehtava(), Tekstiosa.class));
        oavlk.setYleistavoitteet(mapper.map(oavlktDto.getYleistavoitteet(), Tekstiosa.class));
        oavlk.setTyotavat(mapper.map(oavlktDto.getTyotavat(), Tekstiosa.class));
        oavlk.setOhjaus(mapper.map(oavlktDto.getOhjaus(), Tekstiosa.class));
        oavlk.setArviointi(mapper.map(oavlktDto.getArviointi(), Tekstiosa.class));

        oavlk.setVuosiluokat(luoOppiaineenVuosiluokat(vuosiluokat, tavoitteetDto));
        oppiaine.addVuosiluokkaKokonaisuus(oavlk);

        oppiaine = oppiaineet.save(oppiaine);

        return mapper.map(oppiaine, OppiaineDto.class);
    }

    @Override
    public OppiaineDto updateValinnainen(@P("opsId") Long opsId, OppiaineDto oppiaineDto, Long vlkId,
                                         Set<Vuosiluokka> vuosiluokat, List<TekstiosaDto> tavoitteetDto) {
        Oppiaine oppiaine = getOppiaine(opsId, oppiaineDto.getId());
        assertExists(oppiaine, "Päivitettävää oppiainetta ei ole olemassa");

        delete(opsId, oppiaineDto.getId());

        return addValinnainen(opsId, oppiaineDto, vlkId, vuosiluokat, tavoitteetDto);
    }

    @Override
    public OpsOppiaineDto kopioiMuokattavaksi(@P("opsId") Long opsId, Long id) {
        Boolean isOma = oppiaineet.isOma(opsId, id);
        if (isOma == null) {
            throw new BusinessRuleViolationException("Kopioitavaa oppiainetta ei ole olemassa");
        } else if (isOma) {
            throw new BusinessRuleViolationException("Oppiaine on jo muokattavissa");
        }

        Opetussuunnitelma ops = opetussuunnitelmaRepository.findOne(opsId);
        assertExists(ops, "Pyydettyä opetussuunnitelmaa ei ole olemassa");

        Oppiaine oppiaine = getOppiaine(opsId, id);

        if (oppiaine.getOppiaine() != null) {
            throw new BusinessRuleViolationException("Oppimäärää ei voi kopioida");
        }

        Set<OpsOppiaine> opsOppiaineet = ops.getOppiaineet().stream()
                                            .filter(oa -> !oa.getOppiaine().getId().equals(id))
                                            .collect(Collectors.toSet());

        oppiaine = oppiaineet.save(Oppiaine.copyOf(oppiaine));

        OpsOppiaine kopio = new OpsOppiaine(oppiaine, true);
        opsOppiaineet.add(kopio);
        ops.setOppiaineet(opsOppiaineet);

        return mapper.map(kopio, OpsOppiaineDto.class);
    }

    private Set<Oppiaineenvuosiluokka> luoOppiaineenVuosiluokat(Set<Vuosiluokka> vuosiluokat,
                                                                List<TekstiosaDto> tavoitteetDto) {
        return vuosiluokat.stream().map(Oppiaineenvuosiluokka::new)
                          .map(oavl -> asetaOppiaineenVuosiluokanSisalto(oavl, tavoitteetDto))
                          .collect(Collectors.toSet());
    }

    private Oppiaineenvuosiluokka asetaOppiaineenVuosiluokanSisalto(Oppiaineenvuosiluokka oavl,
                                                                    List<TekstiosaDto> tavoitteetDto) {
        List<Tekstiosa> tavoitteet = mapper.mapAsList(tavoitteetDto, Tekstiosa.class);

        List<Keskeinensisaltoalue> sisaltoalueet = tavoitteet.stream()
            .map(tekstiosa -> {
                Keskeinensisaltoalue k = new Keskeinensisaltoalue();
                k.setTunniste(UUID.randomUUID());
                k.setKuvaus(tekstiosa.getTeksti());
                return k;
            })
            .collect(Collectors.toList());

        List<Keskeinensisaltoalue> oavlSisaltoalueet = sisaltoalueet.stream()
            .map(k -> Keskeinensisaltoalue.copyOf(k))
            .collect(Collectors.toList());

        List<Opetuksentavoite> oavlTavoitteet =
            StreamUtils.zip(tavoitteet.stream(), oavlSisaltoalueet.stream(),
                        (tekstiosa, sisaltoalue) -> {
                            Opetuksentavoite t = new Opetuksentavoite();
                            t.setTunniste(UUID.randomUUID());
                            t.setTavoite(tekstiosa.getOtsikko());
                            t.connectSisaltoalueet(Collections.singleton(sisaltoalue));
                            return t;
                        })
                   .collect(Collectors.toList());

        oavl.getTavoitteet().forEach( opetuksentavoite -> {
            opetuksentavoite.getSisaltoalueet().forEach( opetuksenKeskeinensisaltoalue -> {
                opetuksenkeskeinenSisaltoalueRepository.delete( opetuksenKeskeinensisaltoalue );
            } );
        });

        oavl.setSisaltoalueet(oavlSisaltoalueet);
        oavl.setTavoitteet(oavlTavoitteet);
        return oavl;
    }

    @Override
    public OpsOppiaineDto update(@P("opsId") Long opsId, OppiaineDto oppiaineDto) {
        Boolean isOma = oppiaineet.isOma(opsId, oppiaineDto.getId());
        if (isOma == null) {
            throw new BusinessRuleViolationException("Päivitettävää oppiainetta ei ole olemassa");
        } else if (!isOma) {
            throw new BusinessRuleViolationException("Lainattua oppiainetta ei voi muokata");
        }

        Oppiaine oppiaine = getOppiaine(opsId, oppiaineDto.getId());

        // lockService.assertLock ( opsId ) ... ?
        oppiaineet.lock(oppiaine);

        mapper.map(oppiaineDto, oppiaine);

        oppiaine = oppiaineet.save(oppiaine);
        return mapper.map(new OpsOppiaine(oppiaine, isOma), OpsOppiaineDto.class);
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
            oppiaine.getVuosiluokkakokonaisuudet().forEach(vuosiluokkakokonaisuus -> {
                vuosiluokkakokonaisuusService.removeSisaltoalueetInKeskeinensisaltoalueet(vuosiluokkakokonaisuus);
            });
            ops.removeOppiaine(oppiaine);
        }

        mapper.map(oppiaine, OppiaineDto.class);
        oppiaineet.delete(oppiaine);
    }

    @Override
    public OppiaineenVuosiluokkaDto getVuosiluokka(Long opsId, Long oppiaineId, Long vuosiluokkaId) {
        Oppiaineenvuosiluokka vl = findVuosiluokka(opsId, oppiaineId, vuosiluokkaId);
        return vl == null ? null : mapper.map(vl, OppiaineenVuosiluokkaDto.class);
    }

    private Oppiaineenvuosiluokka findVuosiluokka(Long opsId, Long oppiaineId, Long vuosiluokkaId) throws BusinessRuleViolationException {
        if (!oppiaineExists(opsId, oppiaineId)) {
            throw new BusinessRuleViolationException("Opetussuunnitelmaa tai oppiainetta ei ole.");
        }
        return vuosiluokat.findByOppiaine(oppiaineId, vuosiluokkaId);
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
        oavlk.setYleistavoitteet(mapper.map(dto.getYleistavoitteet(), Tekstiosa.class));
        oavlk.setTyotavat(mapper.map(dto.getTyotavat(), Tekstiosa.class));
        oavlk.setOhjaus(mapper.map(dto.getOhjaus(), Tekstiosa.class));
        oavlk.setArviointi(mapper.map(dto.getArviointi(), Tekstiosa.class));

        mapper.map(oavlk, dto);
        return dto;
    }

    @Override
    public OppiaineenVuosiluokkaDto updateVuosiluokanSisalto(@P("opsId") Long opsId, Long oppiaineId, OppiaineenVuosiluokkaDto dto) {
        if (!oppiaineet.isOma(opsId, oppiaineId)) {
            throw new BusinessRuleViolationException("vain-omaa-oppiainetta-saa-muokata");
        }

        Oppiaineenvuosiluokka oppiaineenVuosiluokka = assertExists(findVuosiluokka(opsId, oppiaineId, dto.getId()), "Vuosiluokkaa ei löydy");

        // Aseta oppiaineen vuosiluokan sisällöstä vain sisaltoalueiden ja tavoitteiden kuvaukset,
        // noin muutoin sisältöön ei pidä kajoaman
        dto.getSisaltoalueet().forEach(
            sisaltoalueDto ->
                oppiaineenVuosiluokka.getSisaltoalue(sisaltoalueDto.getTunniste())
                                     .ifPresent(sa -> sa.setKuvaus(mapper.map(sisaltoalueDto.getKuvaus(), LokalisoituTeksti.class))));
        dto.getTavoitteet().forEach(
            tavoiteDto ->
                oppiaineenVuosiluokka.getTavoite(tavoiteDto.getTunniste())
                                     .ifPresent(t -> t.setTavoite(mapper.map(tavoiteDto.getTavoite(), LokalisoituTeksti.class))));

        dto.getTavoitteet().stream()
                .forEach(opetuksenTavoiteDto -> { opetuksenTavoiteDto.getSisaltoalueet()
                        .forEach(opetuksenKeskeinensisaltoalueDto -> {
                            OpetuksenKeskeinensisaltoalue opetuksenKeskeinensisaltoalue
                                = oppiaineenVuosiluokka.getTavoite(opetuksenTavoiteDto.getTunniste())
                                    .get().getOpetuksenkeskeinenSisaltoalueById(opetuksenKeskeinensisaltoalueDto.getId()).get();

                if (opetuksenKeskeinensisaltoalueDto.getOmaKuvaus() != null) {
                    opetuksenKeskeinensisaltoalue.setOmaKuvaus(mapper.map(opetuksenKeskeinensisaltoalueDto.getOmaKuvaus(), LokalisoituTeksti.class));
                }
                else {
                    opetuksenKeskeinensisaltoalue.setOmaKuvaus(null);
                }
            });
        });

        return mapper.map(oppiaineenVuosiluokka, OppiaineenVuosiluokkaDto.class);
    }

    public OppiaineenVuosiluokkaDto updateValinnaisenVuosiluokanSisalto(@P("opsId") Long opsId, Long id,
                                                                        Long oppiaineenVuosiluokkaId,
                                                                        List<TekstiosaDto> tavoitteetDto) {
        Oppiaineenvuosiluokka oavl = assertExists(findVuosiluokka(opsId, id, oppiaineenVuosiluokkaId), "Vuosiluokkaa ei löydy");

        Oppiaine oppiaine = oppiaineet.findOne(id);
        if (oppiaine.getTyyppi() == OppiaineTyyppi.YHTEINEN) {
            throw new BusinessRuleViolationException("Oppiaine ei ole valinnainen");
        }

        oavl = asetaOppiaineenVuosiluokanSisalto(oavl, tavoitteetDto);
        oavl = vuosiluokat.save(oavl);
        return mapper.map(oavl, OppiaineenVuosiluokkaDto.class);
    }

    private Oppiaine getOppiaine(Long opsId, Long oppiaineId) {
        if (!oppiaineExists(opsId, oppiaineId)) {
            throw new BusinessRuleViolationException("Opetussuunnitelmaa tai oppiainetta ei ole.");
        }

        Oppiaine oppiaine = oppiaineet.findOne(oppiaineId);
        assertExists(oppiaine, "Pyydettyä oppiainetta ei ole olemassa");
        return oppiaine;
    }

    private void updateVuosiluokkakokonaisuudenTavoitteet(
        Oppiaineenvuosiluokkakokonaisuus v,
        PerusteOppiaineenVuosiluokkakokonaisuusDto vuosiluokkakokonaisuus,
        Map<Vuosiluokka, Set<UUID>> tavoitteet) {

        if (!vuosiluokkakokonaisuus.getVuosiluokkaKokonaisuus().getVuosiluokat().containsAll(tavoitteet.keySet())) {
            throw new BusinessRuleViolationException("Yksi tai useampi vuosiluokka ei kuulu tähän vuosiluokkakokonaisuuteen");
        }

        vuosiluokkakokonaisuusService.removeSisaltoalueetInKeskeinensisaltoalueet(v);

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

    private void mergePerusteTavoitteet(Oppiaineenvuosiluokka ov, PerusteOppiaineenVuosiluokkakokonaisuusDto pvk, Set<UUID> tavoiteIds) {
        List<PerusteOpetuksentavoiteDto> filtered = pvk.getTavoitteet().stream()
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

                opst.connectSisaltoalueet(t.getSisaltoalueet().stream()
                        .map(s -> alueet.get(s.getTunniste()))
                        .collect(Collectors.toSet()));

                opst.setKohdealueet(t.getKohdealueet().stream()
                        .map(k -> ov.getKokonaisuus().getOppiaine().addKohdealue(new Opetuksenkohdealue(fromDto(k.getNimi()))))
                        .collect(Collectors.toSet()));
                opst.setArvioinninkohteet(t.getArvioinninkohteet().stream()
                    .map(a -> new Tavoitteenarviointi(fromDto(a.getArvioinninKohde()), fromDto(a.getHyvanOsaamisenKuvaus())))
                    .collect(Collectors.toSet()));
                return opst;
            })
            .collect(Collectors.toList());
        ov.setTavoitteet(tmp);
    }

    private LokalisoituTeksti fromDto(LokalisoituTekstiDto dto) {
        if (dto == null) {
            return null;
        }
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
            return oppiaineenKokonaisuudet.getLatestRevisionId(ctx.getKokonaisuusId());
        }
        return vuosiluokat.getLatestRevisionId(ctx.getVuosiluokkaId());
    }

    @Override
    protected Long validateCtx(OpsOppiaineCtx ctx, boolean readOnly) {
        if (ctx.isValid() && oppiaineExists(ctx.getOpsId(), ctx.getOppiaineId())) {
            if (ctx.isOppiane()) {
                return ctx.getOppiaineId();
            }
            if (ctx.isKokonaisuus() && oppiaineenKokonaisuudet.exists(ctx.getOppiaineId(), ctx.getKokonaisuusId())) {
                return ctx.getKokonaisuusId();
            }
            if (ctx.isVuosiluokka() && vuosiluokat.exists(ctx.getOppiaineId(), ctx.getKokonaisuusId(), ctx.getVuosiluokkaId())) {
                return ctx.getVuosiluokkaId();
            }
        }
        throw new LockingException("Virheellinen lukitus");
    }

    private boolean oppiaineExists(Long opsId, Long oppiaineId) {
        return oppiaineet.isOma(opsId, oppiaineId) != null;
    }
}
