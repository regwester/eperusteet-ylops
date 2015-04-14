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

import com.fasterxml.jackson.databind.JsonNode;
import fi.vm.sade.eperusteet.ylops.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.ylops.domain.Tila;
import fi.vm.sade.eperusteet.ylops.domain.Tyyppi;
import fi.vm.sade.eperusteet.ylops.domain.Vuosiluokkakokonaisuusviite;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Oppiaine;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.domain.ops.OpsOppiaine;
import fi.vm.sade.eperusteet.ylops.domain.ops.OpsVuosiluokkakokonaisuus;
import fi.vm.sade.eperusteet.ylops.domain.peruste.PerusopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.ylops.domain.peruste.Peruste;
import fi.vm.sade.eperusteet.ylops.domain.peruste.PerusteLaajaalainenosaaminen;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Kieli;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Omistussuhde;
import fi.vm.sade.eperusteet.ylops.domain.teksti.TekstiKappaleViite;
import fi.vm.sade.eperusteet.ylops.domain.vuosiluokkakokonaisuus.Vuosiluokkakokonaisuus;
import fi.vm.sade.eperusteet.ylops.dto.Reference;
import fi.vm.sade.eperusteet.ylops.dto.koodisto.KoodistoDto;
import fi.vm.sade.eperusteet.ylops.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.ylops.dto.koodisto.KoodistoMetadataDto;
import fi.vm.sade.eperusteet.ylops.dto.koodisto.OrganisaatioDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.KopioOppimaaraDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaBaseDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaInfoDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineLaajaDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpsOppiaineDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.TekstiKappaleDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.TekstiKappaleViiteDto;
import fi.vm.sade.eperusteet.ylops.repository.ops.OpetussuunnitelmaRepository;
import fi.vm.sade.eperusteet.ylops.repository.ops.OppiaineRepository;
import fi.vm.sade.eperusteet.ylops.repository.ops.VuosiluokkakokonaisuusviiteRepository;
import fi.vm.sade.eperusteet.ylops.repository.teksti.TekstiKappaleRepository;
import fi.vm.sade.eperusteet.ylops.repository.teksti.TekstikappaleviiteRepository;
import fi.vm.sade.eperusteet.ylops.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.ylops.service.external.EperusteetService;
import fi.vm.sade.eperusteet.ylops.service.external.KoodistoService;
import fi.vm.sade.eperusteet.ylops.service.external.OrganisaatioService;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.ops.OpetussuunnitelmaService;
import fi.vm.sade.eperusteet.ylops.service.ops.OppiaineService;
import fi.vm.sade.eperusteet.ylops.service.ops.TekstiKappaleViiteService;
import fi.vm.sade.eperusteet.ylops.service.ops.VuosiluokkakokonaisuusService;
import fi.vm.sade.eperusteet.ylops.service.security.PermissionEvaluator.RolePermission;
import fi.vm.sade.eperusteet.ylops.service.teksti.KommenttiService;
import fi.vm.sade.eperusteet.ylops.service.util.CollectionUtil;
import fi.vm.sade.eperusteet.ylops.service.util.SecurityUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static fi.vm.sade.eperusteet.ylops.service.util.Nulls.assertExists;
import java.util.regex.Pattern;

/**
 *
 * @author mikkom
 */
@Service
@Transactional
public class OpetussuunnitelmaServiceImpl implements OpetussuunnitelmaService {

    @Autowired
    private DtoMapper mapper;

    @Autowired
    private OpetussuunnitelmaRepository repository;

    @Autowired
    private TekstikappaleviiteRepository viiteRepository;

    @Autowired
    private TekstiKappaleRepository tekstiKappaleRepository;

    @Autowired
    private TekstiKappaleViiteService tekstiKappaleViiteService;

    @Autowired
    private OppiaineService oppiaineService;

    @Autowired
    private OppiaineRepository oppiaineRepository;

    @Autowired
    private KoodistoService koodistoService;

    @Autowired
    private OrganisaatioService organisaatioService;

    @Autowired
    private KommenttiService kommenttiService;

    @Autowired
    private VuosiluokkakokonaisuusService vuosiluokkakokonaisuudet;

    @Autowired
    private EperusteetService eperusteetService;

    @Autowired
    private VuosiluokkakokonaisuusviiteRepository vuosiluokkakokonaisuusviiteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OpetussuunnitelmaInfoDto> getAll(Tyyppi tyyppi) {
        Set<String> organisaatiot = SecurityUtil.getOrganizations(EnumSet.allOf(RolePermission.class));
        List<Opetussuunnitelma> opetussuunnitelmat = repository.findAllByTyyppi(tyyppi, organisaatiot);
        List<OpetussuunnitelmaInfoDto> dtot = mapper.mapAsList(opetussuunnitelmat, OpetussuunnitelmaInfoDto.class);
        dtot.stream().forEach(this::fetchKuntaNimet);
        dtot.stream().forEach(this::fetchOrganisaatioNimet);
        return dtot;
    }

    @Override
    public Peruste getPeruste(Long opsId) {
        Opetussuunnitelma ops = repository.findOne(opsId);
        assertExists(ops, "Pyydettyä opetussuunnitelmaa ei ole olemassa");
        return eperusteetService.getPerusopetuksenPeruste(ops.getPerusteenDiaarinumero());
    }

    @Override
    @Transactional(readOnly = true)
    public OpetussuunnitelmaDto getOpetussuunnitelma(Long id) {
        Opetussuunnitelma ops = repository.findOne(id);
        assertExists(ops, "Pyydettyä opetussuunnitelmaa ei ole olemassa");
        OpetussuunnitelmaDto dto = mapper.map(ops, OpetussuunnitelmaDto.class);
        fetchKuntaNimet(dto);
        fetchOrganisaatioNimet(dto);
        return dto;
    }

    @Override
    public Set<PerusteLaajaalainenosaaminen> getLaajaalaisetosaamiset(Long id) {
        Opetussuunnitelma ops = repository.findOne(id);
        assertExists(ops, "Pyydettyä opetussuunnitelmaa ei ole olemassa");
        return eperusteetService.getPerusopetuksenPeruste(ops.getPerusteenDiaarinumero()).getPerusopetus().getLaajaalaisetosaamiset();
    }

    private void fetchKuntaNimet(OpetussuunnitelmaBaseDto opetussuunnitelmaDto) {
        for (KoodistoDto koodistoDto : opetussuunnitelmaDto.getKunnat()) {
            Map<String, String> tekstit = new HashMap<>();
            KoodistoKoodiDto kunta = koodistoService.get("kunta", koodistoDto.getKoodiUri());
            if (kunta != null) {
                for (KoodistoMetadataDto metadata : kunta.getMetadata()) {
                    tekstit.put(metadata.getKieli(), metadata.getNimi());
                }
            }
            koodistoDto.setNimi(new LokalisoituTekstiDto(tekstit));
        }
    }

    private void fetchOrganisaatioNimet(OpetussuunnitelmaBaseDto opetussuunnitelmaDto) {
        for (OrganisaatioDto organisaatioDto : opetussuunnitelmaDto.getOrganisaatiot()) {
            Map<String, String> tekstit = new HashMap<>();
            List<String> tyypit = new ArrayList<>();
            JsonNode organisaatio = organisaatioService.getOrganisaatio(organisaatioDto.getOid());
            if (organisaatio != null) {
                JsonNode nimiNode = organisaatio.get("nimi");
                if (nimiNode != null) {
                    Iterator<Map.Entry<String, JsonNode>> it = nimiNode.fields();
                    while (it.hasNext()) {
                        Map.Entry<String, JsonNode> field = it.next();
                        tekstit.put(field.getKey(), field.getValue().asText());
                    }
                }

                JsonNode tyypitNode = Optional.ofNullable(organisaatio.get("tyypit"))
                        .orElse(organisaatio.get("organisaatiotyypit"));
                if (tyypitNode != null) {
                    tyypit = StreamSupport.stream(tyypitNode.spliterator(), false)
                            .map(JsonNode::asText)
                            .collect(Collectors.toList());
                }
            }
            organisaatioDto.setNimi(new LokalisoituTekstiDto(tekstit));
            organisaatioDto.setTyypit(tyypit);
        }
    }

    @Override
    public OpetussuunnitelmaDto addOpetussuunnitelma(OpetussuunnitelmaDto opetussuunnitelmaDto) {
        opetussuunnitelmaDto.setTyyppi(Tyyppi.OPS);
        Opetussuunnitelma ops = mapper.map(opetussuunnitelmaDto, Opetussuunnitelma.class);

        Set<String> userOids = SecurityUtil.getOrganizations(EnumSet.of(RolePermission.CRUD));
        if (CollectionUtil.intersect(userOids, ops.getOrganisaatiot()).isEmpty()) {
            throw new BusinessRuleViolationException("Käyttäjällä ei ole luontioikeutta opetussuunnitelman organisaatioissa");
        }

        Opetussuunnitelma pohja = ops.getPohja();

        if (pohja == null) {
            pohja = repository.findOneByTyyppiAndTilaAndKoulutustyyppi(Tyyppi.POHJA, Tila.VALMIS, opetussuunnitelmaDto.getKoulutustyyppi());
        }

        if (pohja != null) {
            ops.setTekstit(new TekstiKappaleViite(Omistussuhde.OMA));
            ops.getTekstit().setLapset(new ArrayList<>());
            luoOpsPohjasta(pohja, ops);
            ops.setTila(Tila.LUONNOS);
            ops = repository.save(ops);
        } else {
            throw new BusinessRuleViolationException("Valmista opetussuunnitelman pohjaa ei löytynyt");
        }

        return mapper.map(ops, OpetussuunnitelmaDto.class);
    }

    private boolean onPoistettava(OpsOppiaine oa) {
        String koodi = oa.getOppiaine().getKoodiArvo();
        return oa.getOppiaine().getKoodiArvo() == null
                || Pattern.matches("^AI.+", koodi)
                || Pattern.matches("^A1.+", koodi)
                || Pattern.matches("^A2.+", koodi)
                || Pattern.matches("^B1.+", koodi)
                || Pattern.matches("^B2.+", koodi)
                || Pattern.matches("^B3.+", koodi)
                || Pattern.matches("^RU", koodi)
                || Pattern.matches("^SK", koodi)
                //                || Pattern.matches("^TK", koodi)
                //                || Pattern.matches("^VK", koodi)
                || Pattern.matches("^LK", koodi);
    }

    private void luoOpsPohjasta(Opetussuunnitelma pohja, Opetussuunnitelma ops) {
        ops.setPohja(pohja);
        ops.setPerusteenDiaarinumero(pohja.getPerusteenDiaarinumero());
        boolean teeKopio = pohja.getTyyppi() == Tyyppi.POHJA;
        kasitteleTekstit(pohja.getTekstit(), ops.getTekstit(), teeKopio);

        // FIXME poista filtteröi oppiaineet onPoistettavalla
        ops.setOppiaineet(
            pohja.getOppiaineet().stream()
                 .map(ooa -> new OpsOppiaine(Oppiaine.copyOf(ooa.getOppiaine()), teeKopio))
                 .collect(Collectors.toSet()));

        ops.setVuosiluokkakokonaisuudet(
            pohja.getVuosiluokkakokonaisuudet().stream()
                 .filter(ovlk -> ops.getVuosiluokkakokonaisuudet().stream()
                                    .anyMatch(vk -> vk.getVuosiluokkakokonaisuus().getId().equals(ovlk.getVuosiluokkakokonaisuus().getId())))
                 .map(ovlk -> new OpsVuosiluokkakokonaisuus(Vuosiluokkakokonaisuus.copyOf(ovlk.getVuosiluokkakokonaisuus()), teeKopio))
                 .collect(Collectors.toSet()));
    }

    private void kasitteleTekstit(TekstiKappaleViite vanha, TekstiKappaleViite parent, boolean teeKopio) {
        List<TekstiKappaleViite> vanhaLapset = vanha.getLapset();
        if (vanhaLapset != null) {
            vanhaLapset.stream()
                       .filter(vanhaTkv -> vanhaTkv.getTekstiKappale() != null)
                       .forEach(vanhaTkv -> {
                           TekstiKappaleViite tkv = viiteRepository.save(new TekstiKappaleViite());
                           tkv.setOmistussuhde(teeKopio ? Omistussuhde.OMA : Omistussuhde.LAINATTU);
                           tkv.setLapset(new ArrayList<>());
                           tkv.setVanhempi(parent);
                           tkv.setTekstiKappale(teeKopio
                                                ? tekstiKappaleRepository.save(vanhaTkv.getTekstiKappale().copy())
                                                : vanhaTkv.getTekstiKappale());
                           parent.getLapset().add(tkv);
                           kasitteleTekstit(vanhaTkv, tkv, teeKopio);
                       });
        }
    }

    private Opetussuunnitelma addPohjaLisaJaEsiopetus(Opetussuunnitelma ops, Peruste peruste) {
        ops.setKoulutustyyppi(peruste.getKoulutustyyppi());
        return ops;
    }

    private Opetussuunnitelma addPohjaPerusopetus(Opetussuunnitelma ops, Peruste peruste) {
        Long opsId = ops.getId();

        PerusopetuksenPerusteenSisalto sisalto = peruste.getPerusopetus();

        if (sisalto.getVuosiluokkakokonaisuudet() != null) {
            sisalto.getVuosiluokkakokonaisuudet()
                    .forEach(vk -> vuosiluokkakokonaisuusviiteRepository.save(
                                    new Vuosiluokkakokonaisuusviite(vk.getTunniste(), vk.getVuosiluokat())));

            if (sisalto.getOppiaineet() != null) {
                sisalto.getOppiaineet().stream()
                        .map(OpsDtoMapper::fromEperusteet)
                        .forEach(oa -> oppiaineService.add(opsId, oa));
            }

            sisalto.getVuosiluokkakokonaisuudet().stream()
                    .map(OpsDtoMapper::fromEperusteet)
                    .forEach(vk -> vuosiluokkakokonaisuudet.add(opsId, vk));
        }
        return ops;
    }

    @Override
    public OpetussuunnitelmaDto addPohja(OpetussuunnitelmaDto opetussuunnitelmaDto) {
        Opetussuunnitelma ops = mapper.map(opetussuunnitelmaDto, Opetussuunnitelma.class);
        // Jokainen pohja sisältää OPH:n organisaationaan
        ops.getOrganisaatiot().add(SecurityUtil.OPH_OID);

        Set<String> userOids = SecurityUtil.getOrganizations(EnumSet.of(RolePermission.CRUD));
        if (CollectionUtil.intersect(userOids, ops.getOrganisaatiot()).isEmpty()) {
            throw new BusinessRuleViolationException("Käyttäjällä ei ole luontioikeutta opetussuunnitelman pohjan organisaatioissa");
        }

        final String diaarinumero = ops.getPerusteenDiaarinumero();
        if (StringUtils.isBlank(diaarinumero)) {
            throw new BusinessRuleViolationException("Perusteen diaarinumeroa ei ole määritelty");
        } else if (eperusteetService.findPerusteet().stream()
                .noneMatch(p -> diaarinumero.equals(p.getDiaarinumero()))) {
            throw new BusinessRuleViolationException("Diaarinumerolla " + diaarinumero
                    + " ei löydy voimassaolevaa perustetta");
        }

        if (ops.getPohja() != null) {
            throw new BusinessRuleViolationException("Opetussuunnitelman pohjalla ei voi olla pohjaa");
        }

        ops.setTila(Tila.LUONNOS);
        lisaaTekstipuunJuuri(ops);

        ops = repository.save(ops);
        lisaaTekstipuunLapset(ops);

        Peruste peruste = eperusteetService.getPerusopetuksenPeruste(ops.getPerusteenDiaarinumero());
        if (peruste.getKoulutustyyppi() == null || KoulutusTyyppi.PERUSOPETUS == peruste.getKoulutustyyppi()) {
            ops.setKoulutustyyppi(KoulutusTyyppi.PERUSOPETUS);
            ops = addPohjaPerusopetus(ops, peruste);
        }
        else if (KoulutusTyyppi.LISAOPETUS == peruste.getKoulutustyyppi()
                || KoulutusTyyppi.ESIOPETUS == peruste.getKoulutustyyppi()) {
            ops = addPohjaLisaJaEsiopetus(ops, peruste);
        }
        else {
            throw new BusinessRuleViolationException("Ei toimintatapaa perusteen koulutustyypille");
        }

        return mapper.map(ops, OpetussuunnitelmaDto.class);
    }

    private void lisaaTekstipuunJuuri(Opetussuunnitelma ops) {
        TekstiKappaleViite juuri = new TekstiKappaleViite(Omistussuhde.OMA);
        juuri = viiteRepository.saveAndFlush(juuri);
        ops.setTekstit(juuri);
    }

    private void lisaaTekstipuunLapset(Opetussuunnitelma ops) {
        LokalisoituTekstiDto nimi, teksti;
        nimi = new LokalisoituTekstiDto(null, Collections.singletonMap(Kieli.FI, "Opetuksen järjestäminen"));
        teksti = new LokalisoituTekstiDto(null, null);
        TekstiKappaleDto ohjeistusTeksti = new TekstiKappaleDto(nimi, teksti, Tila.LUONNOS);
        TekstiKappaleViiteDto.Matala ohjeistus = new TekstiKappaleViiteDto.Matala(ohjeistusTeksti);
        addTekstiKappale(ops.getId(), ohjeistus);

        nimi = new LokalisoituTekstiDto(null,
                Collections.singletonMap(Kieli.FI, "Opetuksen toteuttamisen lähtökohdat"));
        teksti = new LokalisoituTekstiDto(null, null);
        TekstiKappaleDto opetuksenJarjestaminenTeksti
                = new TekstiKappaleDto(nimi, teksti, Tila.LUONNOS);
        TekstiKappaleViiteDto.Matala opetuksenJarjestaminen
                = new TekstiKappaleViiteDto.Matala(opetuksenJarjestaminenTeksti);
        addTekstiKappale(ops.getId(), opetuksenJarjestaminen);
    }

    @Override
    public OpetussuunnitelmaDto updateOpetussuunnitelma(OpetussuunnitelmaDto opetussuunnitelmaDto) {
        Opetussuunnitelma ops = repository.findOne(opetussuunnitelmaDto.getId());
        assertExists(ops, "Päivitettävää tietoa ei ole olemassa");

        if (opetussuunnitelmaDto.getTyyppi() != ops.getTyyppi()) {
            throw new BusinessRuleViolationException("Opetussuunnitelman tyyppiä ei voi vaihtaa");
        }

        if (!Objects.equals(opetussuunnitelmaDto.getPohja(), Reference.of(ops.getPohja()))) {
            throw new BusinessRuleViolationException("Opetussuunnitelman pohjaa ei voi vaihtaa");
        }

        if (!Objects.equals(opetussuunnitelmaDto.getPerusteenDiaarinumero(), ops.getPerusteenDiaarinumero())) {
            throw new BusinessRuleViolationException("Perusteen diaarinumeroa ei voi vaihtaa");
        }

        if (opetussuunnitelmaDto.getOrganisaatiot().isEmpty()) {
            throw new BusinessRuleViolationException("Organisaatiolista ei voi olla tyhjä");
        }

        // Tilan muuttamiseen on oma erillinen endpointtinsa
        opetussuunnitelmaDto.setTila(ops.getTila());

        mapper.map(opetussuunnitelmaDto, ops);
        ops = repository.save(ops);

        if (opetussuunnitelmaDto.getTekstit() != null) {
            tekstiKappaleViiteService.reorderSubTree(ops.getId(), ops.getTekstit().getId(), opetussuunnitelmaDto.getTekstit().get());
        }

        return mapper.map(ops, OpetussuunnitelmaDto.class);
    }

    @Override
    public OpetussuunnitelmaDto updateTila(@P("id") Long id, Tila tila) {
        Opetussuunnitelma ops = repository.findOne(id);
        assertExists(ops, "Opetussuunnitelmaa ei ole olemassa");

        // Sallitaan tilasiirtymät vain yhteen suuntaan (paitsi ops valmis->luonnos)
        if (tila.ordinal() > ops.getTila().ordinal() ||
            (ops.getTyyppi() == Tyyppi.OPS && tila == Tila.LUONNOS && ops.getTila() == Tila.VALMIS)) {
            if (tila == Tila.VALMIS && ops.getTyyppi() == Tyyppi.POHJA) {
                // Arkistoidaan vanhat valmiit pohjat
                List<Opetussuunnitelma> pohjat = repository.findAllByTyyppi(Tyyppi.POHJA);
                pohjat.stream().filter(pohja -> pohja.getTila() == Tila.VALMIS)
                        .forEach(pohja -> updateTila(pohja.getId(), Tila.POISTETTU));
            }

            // TODO opsin validointi kun julkaistaan
            ops.setTila(tila);
            ops = repository.save(ops);
        }
        return mapper.map(ops, OpetussuunnitelmaDto.class);
    }

    @Override
    public OpetussuunnitelmaDto restore(@P("id") Long id) {
        Opetussuunnitelma ops = repository.findOne(id);
        assertExists(ops, "Opetussuunnitelmaa ei ole olemassa");

        ops.setTila(Tila.LUONNOS);
        ops = repository.save(ops);
        return mapper.map(ops, OpetussuunnitelmaDto.class);
    }

    @Override
    public void removeOpetussuunnitelma(Long id) {
        Opetussuunnitelma ops = repository.findOne(id);
        if (ops != null) {
            kommenttiService.getAllByOpetussuunnitelma(id)
                    .forEach(k -> kommenttiService.deleteReally(k.getId()));
        }
        repository.delete(ops);
    }

    @Override
    public TekstiKappaleViiteDto.Puu getTekstit(Long opsId) {
        Opetussuunnitelma ops = repository.findOne(opsId);
        assertExists(ops, "Opetussuunnitelmaa ei ole olemassa");
        return mapper.map(ops.getTekstit(), TekstiKappaleViiteDto.Puu.class);
    }

    @Override
    public TekstiKappaleViiteDto.Matala addTekstiKappale(Long opsId, TekstiKappaleViiteDto.Matala viite) {
        Opetussuunnitelma ops = repository.findOne(opsId);
        assertExists(ops, "Opetussuunnitelmaa ei ole olemassa");

        // Lisätään viite juurinoden alle
        return tekstiKappaleViiteService.addTekstiKappaleViite(opsId, ops.getTekstit().getId(), viite);
    }

    @Override
    public TekstiKappaleViiteDto.Matala addTekstiKappaleLapsi(Long opsId, Long parentId,
            TekstiKappaleViiteDto.Matala viite) {
        // Lisätään viite parent-noden alle
        return tekstiKappaleViiteService.addTekstiKappaleViite(opsId, parentId, viite);
    }

}
