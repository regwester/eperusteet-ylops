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
package fi.vm.sade.eperusteet.ylops.service;

import fi.vm.sade.eperusteet.ylops.domain.Tila;
import fi.vm.sade.eperusteet.ylops.domain.Tyyppi;
import fi.vm.sade.eperusteet.ylops.domain.Vuosiluokka;
import fi.vm.sade.eperusteet.ylops.domain.Vuosiluokkakokonaisuusviite;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Kieli;
import fi.vm.sade.eperusteet.ylops.dto.Reference;
import fi.vm.sade.eperusteet.ylops.dto.koodisto.KoodistoDto;
import fi.vm.sade.eperusteet.ylops.dto.koodisto.OrganisaatioDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaInfoDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineenVuosiluokkakokonaisuusDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.VuosiluokkakokonaisuusDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.TekstiosaDto;
import fi.vm.sade.eperusteet.ylops.repository.ops.OpetussuunnitelmaRepository;
import fi.vm.sade.eperusteet.ylops.repository.ops.VuosiluokkakokonaisuusviiteRepository;
import fi.vm.sade.eperusteet.ylops.service.mocks.EperusteetServiceMock;
import fi.vm.sade.eperusteet.ylops.service.ops.OpetussuunnitelmaService;
import fi.vm.sade.eperusteet.ylops.service.ops.OppiaineService;
import fi.vm.sade.eperusteet.ylops.service.ops.VuosiluokkakokonaisuusService;
import fi.vm.sade.eperusteet.ylops.test.AbstractIntegrationTest;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import static fi.vm.sade.eperusteet.ylops.test.util.TestUtils.lt;
import static fi.vm.sade.eperusteet.ylops.test.util.TestUtils.uniikkiString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author mikkom
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class OppiaineServiceIT extends AbstractIntegrationTest {
    @Autowired
    OpetussuunnitelmaService opetussuunnitelmaService;

    @Autowired
    OppiaineService oppiaineService;

    @Autowired
    OpetussuunnitelmaRepository opetussuunnitelmaRepository;

    @Autowired
    VuosiluokkakokonaisuusService vuosiluokkakokonaisuusService;

    @Autowired
    private VuosiluokkakokonaisuusviiteRepository vlkViitteet;

    private Long opsId;
    private Reference vlkViiteRef;

    @Before
    public void setUp() {
        OpetussuunnitelmaDto ops;
        ops = new OpetussuunnitelmaDto();
        ops.setNimi(lt(uniikkiString()));
        ops.setKuvaus(lt(uniikkiString()));
        ops.setPerusteenDiaarinumero(EperusteetServiceMock.DIAARINUMERO);
        ops.setTila(Tila.LUONNOS);
        ops.setTyyppi(Tyyppi.POHJA);

        KoodistoDto kunta = new KoodistoDto();
        kunta.setKoodiUri("kunta_837");
        ops.setKunnat(new HashSet<>(Collections.singleton(kunta)));
        OrganisaatioDto kouluDto = new OrganisaatioDto();
        kouluDto.setNimi(lt("Etelä-Hervannan koulu"));
        kouluDto.setOid("1.2.15252345624572462");
        ops.setKoulut(new HashSet<>(Collections.singleton(kouluDto)));
        opetussuunnitelmaService.addPohja(ops);

        List<OpetussuunnitelmaInfoDto> opsit = opetussuunnitelmaService.getAll(Tyyppi.POHJA);
        assertEquals(1, opsit.size());

        this.opsId = opsit.get(0).getId();
        assertNotNull(this.opsId);

        Vuosiluokkakokonaisuusviite viite = new Vuosiluokkakokonaisuusviite(UUID.randomUUID(), EnumSet.of(Vuosiluokka.VUOSILUOKKA_1, Vuosiluokka.VUOSILUOKKA_2));
        this.vlkViiteRef = Reference.of(vlkViitteet.save(viite));
    }

    @Test
    public void testCRUD() {
        OpetussuunnitelmaDto ops = opetussuunnitelmaService.getOpetussuunnitelma(opsId);

        VuosiluokkakokonaisuusDto vlk = new VuosiluokkakokonaisuusDto(vlkViiteRef);
        vlk.setNimi(Optional.of(lt("ykköskakkoset")));
        vlk = vuosiluokkakokonaisuusService.add(ops.getId(), vlk);

        OppiaineDto oppiaineDto = new OppiaineDto();
        oppiaineDto.setNimi(lt("Uskonto"));
        oppiaineDto.setKoodiUri("koodikoodi");
        oppiaineDto.setTunniste(UUID.randomUUID());
        oppiaineDto.setKoosteinen(false);

        oppiaineDto = oppiaineService.add(opsId, oppiaineDto);
        assertNotNull(oppiaineDto);

        oppiaineDto = new OppiaineDto();
        oppiaineDto.setNimi(lt("Äidinkieli"));
        oppiaineDto.setKoodiUri("koodi_123");
        oppiaineDto.setTunniste(UUID.randomUUID());

        OppiaineSuppeaDto oppimaaraDto = new OppiaineSuppeaDto();
        oppimaaraDto.setNimi(lt("Suomen kieli ja kirjallisuus"));
        oppimaaraDto.setKoosteinen(false);
        oppimaaraDto.setTunniste(UUID.randomUUID());

        oppiaineDto.setOppimaarat(Collections.singleton(oppimaaraDto));
        oppiaineDto.setKoosteinen(true);

        oppiaineDto = oppiaineService.add(opsId, oppiaineDto);
        assertNotNull(oppiaineDto);
        assertNotNull(oppiaineDto.getOppimaarat());
        assertEquals(1, oppiaineDto.getOppimaarat().size());

        oppiaineDto = new OppiaineDto();
        oppiaineDto.setNimi(lt("Matematiikka"));
        oppiaineDto.setKoodiUri("jaa-a");
        oppiaineDto.setKoosteinen(false);
        oppiaineDto.setTunniste(UUID.randomUUID());

        OppiaineenVuosiluokkakokonaisuusDto ovk = new OppiaineenVuosiluokkakokonaisuusDto();
        ovk.setArviointi(getTekstiosa("Arviointi"));
        ovk.setTehtava(getTekstiosa("Tehtävä"));
        ovk.setTyotavat(getTekstiosa("Työtavat"));
        ovk.setOhjaus(getTekstiosa("Ohjaus"));
        ovk.setVuosiluokkakokonaisuus(vlkViiteRef);
        oppiaineDto.setVuosiluokkakokonaisuudet(Collections.singleton(ovk));

        oppiaineDto = oppiaineService.add(opsId, oppiaineDto);
        assertNotNull(oppiaineDto);

        List<OppiaineDto> oppiaineet = oppiaineService.getAll(opsId);
        assertNotNull(oppiaineet);
        assertEquals(3, oppiaineet.size());

        oppiaineDto = oppiaineService.get(opsId, oppiaineDto.getId());
        assertNotNull(oppiaineDto);
        assertEquals("Matematiikka", oppiaineDto.getNimi().get(Kieli.FI));

        oppiaineDto.setNimi(lt("Biologia"));
        oppiaineDto = oppiaineService.update(opsId, oppiaineDto);

        oppiaineDto = oppiaineService.get(opsId, oppiaineDto.getId());
        assertNotNull(oppiaineDto);
        assertEquals("Biologia", oppiaineDto.getNimi().get(Kieli.FI));
    }

    private static TekstiosaDto getTekstiosa(String suffiksi) {
        TekstiosaDto dto = new TekstiosaDto();
        dto.setOtsikko(Optional.of(lt("otsikko_" + suffiksi)));
        dto.setTeksti(Optional.of(lt("teksti_" + suffiksi)));
        return dto;
    }
}

