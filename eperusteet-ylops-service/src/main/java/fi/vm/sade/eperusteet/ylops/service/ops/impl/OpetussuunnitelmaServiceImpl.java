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
import fi.vm.sade.eperusteet.ylops.domain.Tila;
import fi.vm.sade.eperusteet.ylops.domain.Tyyppi;
import fi.vm.sade.eperusteet.ylops.domain.Vuosiluokkakokonaisuusviite;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.domain.ops.OpsOppiaine;
import fi.vm.sade.eperusteet.ylops.domain.ops.OpsVuosiluokkakokonaisuus;
import fi.vm.sade.eperusteet.ylops.domain.peruste.PerusopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.ylops.domain.peruste.Peruste;
import fi.vm.sade.eperusteet.ylops.domain.peruste.PerusteLaajaalainenosaaminen;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Kieli;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Omistussuhde;
import fi.vm.sade.eperusteet.ylops.domain.teksti.TekstiKappaleViite;
import fi.vm.sade.eperusteet.ylops.dto.Reference;
import fi.vm.sade.eperusteet.ylops.dto.koodisto.KoodistoDto;
import fi.vm.sade.eperusteet.ylops.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.ylops.dto.koodisto.KoodistoMetadataDto;
import fi.vm.sade.eperusteet.ylops.dto.koodisto.OrganisaatioDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaInfoDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.TekstiKappaleDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.TekstiKappaleViiteDto;
import fi.vm.sade.eperusteet.ylops.repository.ops.OpetussuunnitelmaRepository;
import fi.vm.sade.eperusteet.ylops.repository.ops.VuosiluokkakokonaisuusviiteRepository;
import fi.vm.sade.eperusteet.ylops.repository.teksti.TekstiKappaleRepository;
import fi.vm.sade.eperusteet.ylops.repository.teksti.TekstiKappaleViiteRepository;
import fi.vm.sade.eperusteet.ylops.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.ylops.service.external.EperusteetService;
import fi.vm.sade.eperusteet.ylops.service.external.KoodistoService;
import fi.vm.sade.eperusteet.ylops.service.external.OrganisaatioService;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.ops.OpetussuunnitelmaService;
import fi.vm.sade.eperusteet.ylops.service.ops.OppiaineService;
import fi.vm.sade.eperusteet.ylops.service.ops.VuosiluokkakokonaisuusService;
import fi.vm.sade.eperusteet.ylops.service.teksti.TekstiKappaleViiteService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private TekstiKappaleViiteRepository viiteRepository;

    @Autowired
    private TekstiKappaleRepository tekstiKappaleRepository;

    @Autowired
    private TekstiKappaleViiteService tekstiKappaleViiteService;

    @Autowired
    private OppiaineService oppiaineService;

    @Autowired
    private KoodistoService koodistoService;

    @Autowired
    private OrganisaatioService organisaatioService;

    @Autowired
    private VuosiluokkakokonaisuusService vuosiluokkakokonaisuudet;

    @Autowired
    private EperusteetService eperusteetService;

    @Autowired
    private VuosiluokkakokonaisuusviiteRepository vuosiluokkakokonaisuusviiteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OpetussuunnitelmaInfoDto> getAll(Tyyppi tyyppi) {
        List<Opetussuunnitelma> opetussuunnitelmat = repository.findAllByTyyppi(tyyppi);
        return mapper.mapAsList(opetussuunnitelmat, OpetussuunnitelmaInfoDto.class);
    }

    @Override
    public Peruste getPeruste(Long opsId) {
        Opetussuunnitelma ops = repository.findOne(opsId);
        assertExists(ops, "Pyydettyä opetussuunnitelmaa ei ole olemassa");
        return eperusteetService.getPerusopetuksenPeruste(ops.getPerusteenDiaarinumero());
    }

    @Override
    @Transactional(readOnly = true)
    public OpetussuunnitelmaDto getOpetussuunnitelma(@P("id") Long id) {
        Opetussuunnitelma ops = repository.findOne(id);
        assertExists(ops, "Pyydettyä opetussuunnitelmaa ei ole olemassa");
        OpetussuunnitelmaDto dto = mapper.map(ops, OpetussuunnitelmaDto.class);

        fetchKuntaNimet(dto);
        fetchKouluNimet(dto);

        return dto;
    }

    @Override
    public Set<PerusteLaajaalainenosaaminen> getLaajaalaisetosaamiset(Long id) {
        Opetussuunnitelma ops = repository.findOne(id);
        assertExists(ops, "Pyydettyä opetussuunnitelmaa ei ole olemassa");
        //TODO hae peruste diaarinumeron perusteella.
        return eperusteetService.getPerusopetuksenPeruste(ops.getPerusteenDiaarinumero()).getPerusopetus().getLaajaalaisetosaamiset();
    }

    private void fetchKuntaNimet(OpetussuunnitelmaDto opetussuunnitelmaDto) {
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

    private void fetchKouluNimet(OpetussuunnitelmaDto opetussuunnitelmaDto) {
        for (OrganisaatioDto organisaatioDto : opetussuunnitelmaDto.getKoulut()) {
            Map<String, String> tekstit = new HashMap<>();
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
            }
            organisaatioDto.setNimi(new LokalisoituTekstiDto(tekstit));
        }
    }

    @Override
    public OpetussuunnitelmaDto addOpetussuunnitelma(OpetussuunnitelmaDto opetussuunnitelmaDto) {
        opetussuunnitelmaDto.setTyyppi(Tyyppi.OPS);
        Opetussuunnitelma ops = mapper.map(opetussuunnitelmaDto, Opetussuunnitelma.class);

        Opetussuunnitelma pohja = ops.getPohja();

        if (pohja == null) {
            //Opetussuunnitelma pohja = repository.findOneByTyyppiAndTila(Tyyppi.POHJA, Tila.VALMIS);
            // TODO: Keksi tapa valita oikea pohja
            pohja = repository.findFirst1ByTyyppi(Tyyppi.POHJA);
        }

        if (pohja != null) {
            ops.setTekstit(new TekstiKappaleViite(Omistussuhde.OMA));
            ops.getTekstit().setLapset(new ArrayList<>());
            luoOpsPohjasta(pohja, ops);
            ops.setTila(Tila.LUONNOS);
            ops = repository.save(ops);
        } else {
            throw new BusinessRuleViolationException("Opetussuunnitelman pohjaa ei ole olemassa");
        }

        return mapper.map(ops, OpetussuunnitelmaDto.class);
    }

    @Transactional
    private void luoOpsPohjasta(Opetussuunnitelma pohja, Opetussuunnitelma ops) {
        ops.setPerusteenDiaarinumero(pohja.getPerusteenDiaarinumero());
        kopioiTekstit(pohja.getTekstit(), ops.getTekstit());

        ops.setOppiaineet(
            pohja.getOppiaineet().stream()
                 .map(ooa -> new OpsOppiaine(ooa.getOppiaine(), false))
                 .collect(Collectors.toSet()));

        ops.setVuosiluokkakokonaisuudet(
            pohja.getVuosiluokkakokonaisuudet().stream()
                 .map(ovlk -> new OpsVuosiluokkakokonaisuus(ovlk.getVuosiluokkakokonaisuus(), false))
                 .collect(Collectors.toSet()));
    }

    @Transactional
    private void kopioiTekstit(TekstiKappaleViite vanha, TekstiKappaleViite parent) {
        List<TekstiKappaleViite> vanhaLapset = vanha.getLapset();
        if (vanhaLapset != null) {
            for (TekstiKappaleViite vanhaTkv : vanhaLapset) {
                if (vanhaTkv.getTekstiKappale() != null) {
                    TekstiKappaleViite tkv = viiteRepository.save(new TekstiKappaleViite());
                    tkv.setOmistussuhde(Omistussuhde.LAINATTU);
                    tkv.setLapset(new ArrayList<>());
                    tkv.setVanhempi(parent);
                    tkv.setTekstiKappale(tekstiKappaleRepository.save(vanhaTkv.getTekstiKappale().copy()));
                    parent.getLapset().add(tkv);
                    kopioiTekstit(vanhaTkv, tkv);
                }
            }
        }
    }

    @Override
    public OpetussuunnitelmaDto addPohja(OpetussuunnitelmaDto opetussuunnitelmaDto) {
        Opetussuunnitelma ops = mapper.map(opetussuunnitelmaDto, Opetussuunnitelma.class);

        final String diaarinumero = ops.getPerusteenDiaarinumero();
        if (StringUtils.isBlank(diaarinumero)) {
            throw new BusinessRuleViolationException("Perusteen diaarinumeroa ei ole määritelty");
        } else if (eperusteetService.findPerusopetuksenPerusteet().stream()
                                    .noneMatch(p -> diaarinumero.equals(p.getDiaarinumero()))) {
            throw new BusinessRuleViolationException("Diaarinumerolla " + diaarinumero +
                                                     " ei löydy voimassaolevaa perustetta");
        }

        if (ops.getPohja() != null) {
            throw new BusinessRuleViolationException("Opetussuunnitelman pohjalla ei voi olla pohjaa");
        }

        ops.setTila(Tila.LUONNOS);
        lisaaTekstipuunJuuri(ops);

        ops = repository.save(ops);
        lisaaTekstipuunLapset(ops);

        Peruste perusteDto = eperusteetService.getPerusopetuksenPeruste(ops.getPerusteenDiaarinumero());

        Long opsId = ops.getId();

        PerusopetuksenPerusteenSisalto sisalto
            = perusteDto.getPerusopetus();

        if (sisalto.getVuosiluokkakokonaisuudet() != null) {
            sisalto.getVuosiluokkakokonaisuudet()
                .forEach(vk -> vuosiluokkakokonaisuusviiteRepository.save(
                        new Vuosiluokkakokonaisuusviite(vk.getTunniste(), vk.getVuosiluokat())));

            final Map<Reference, UUID> vuosiluokkaMap
                = sisalto.getVuosiluokkakokonaisuudet().stream()
                .collect(Collectors.toMap(vlk -> Reference.of(vlk),
                                          vlk -> vlk.getTunniste()));

            if (sisalto.getOppiaineet() != null) {
                sisalto.getOppiaineet().stream()
                    .map(oa -> OpsDtoMapper.fromEperusteet(oa, vuosiluokkaMap))
                    .forEach(oa -> oppiaineService.add(opsId, oa));
            }

            sisalto.getVuosiluokkakokonaisuudet().stream()
                .map(OpsDtoMapper::fromEperusteet)
                .forEach(vk -> vuosiluokkakokonaisuudet.add(opsId, vk));
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
        mapper.map(opetussuunnitelmaDto, ops);
        ops = repository.save(ops);

        if (opetussuunnitelmaDto.getTekstit() != null) {
            tekstiKappaleViiteService.reorderSubTree(ops.getId(), ops.getTekstit().getId(), opetussuunnitelmaDto.getTekstit().get());
        }

        return mapper.map(ops, OpetussuunnitelmaDto.class);
    }

    @Override
    public void removeOpetussuunnitelma(@P("id") Long id) {
        Opetussuunnitelma ops = repository.findOne(id);
        repository.delete(ops);
    }

    @Override
    public TekstiKappaleViiteDto.Puu getTekstit(@P("opsId") Long opsId) {
        Opetussuunnitelma ops = repository.findOne(opsId);
        assertExists(ops, "Opetussuunnitelmaa ei ole olemassa");
        return mapper.map(ops.getTekstit(), TekstiKappaleViiteDto.Puu.class);
    }

    @Override
    public TekstiKappaleViiteDto.Matala addTekstiKappale(@P("opsId") Long opsId, TekstiKappaleViiteDto.Matala viite) {
        Opetussuunnitelma ops = repository.findOne(opsId);
        assertExists(ops, "Opetussuunnitelmaa ei ole olemassa");

        // Lisätään viite juurinoden alle
        return tekstiKappaleViiteService.addTekstiKappaleViite(opsId, ops.getTekstit().getId(), viite);
    }

    @Override
    public TekstiKappaleViiteDto.Matala addTekstiKappaleLapsi(@P("opsId") Long opsId, Long parentId,
        TekstiKappaleViiteDto.Matala viite) {
        // Lisätään viite parent-noden alle
        return tekstiKappaleViiteService.addTekstiKappaleViite(opsId, parentId, viite);
    }

    private static void assertExists(Object o, String msg) {
        if (o == null) {
            throw new BusinessRuleViolationException(msg);
        }
    }
}
