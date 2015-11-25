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
package fi.vm.sade.eperusteet.ylops.service.ops;

import fi.vm.sade.eperusteet.ylops.domain.Tila;
import fi.vm.sade.eperusteet.ylops.domain.Tyyppi;
import fi.vm.sade.eperusteet.ylops.domain.Vuosiluokka;
import fi.vm.sade.eperusteet.ylops.domain.Vuosiluokkakokonaisuusviite;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.OppiaineTyyppi;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Kieli;
import fi.vm.sade.eperusteet.ylops.dto.Reference;
import fi.vm.sade.eperusteet.ylops.dto.koodisto.KoodistoDto;
import fi.vm.sade.eperusteet.ylops.dto.koodisto.OrganisaatioDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetuksenTavoiteDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaInfoDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaLuontiDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineSuppeaDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineenVuosiluokkaDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineenVuosiluokkakokonaisuusDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpsOppiaineDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpsVuosiluokkakokonaisuusDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.VuosiluokkakokonaisuusDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.TekstiosaDto;
import fi.vm.sade.eperusteet.ylops.repository.ops.OpetussuunnitelmaRepository;
import fi.vm.sade.eperusteet.ylops.repository.ops.OppiaineRepository;
import fi.vm.sade.eperusteet.ylops.repository.ops.VuosiluokkakokonaisuusviiteRepository;
import fi.vm.sade.eperusteet.ylops.service.mocks.EperusteetServiceMock;
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
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
    OppiaineRepository oppiaineRepo;

    @Autowired
    VuosiluokkakokonaisuusService vuosiluokkakokonaisuusService;

    @Autowired
    private VuosiluokkakokonaisuusviiteRepository vlkViitteet;

    private Long opsId;
    private Reference vlkViiteRef;

    @Before
    public void setUp() {
        OpetussuunnitelmaLuontiDto ops = new OpetussuunnitelmaLuontiDto();
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
        ops.setOrganisaatiot(new HashSet<>(Collections.singleton(kouluDto)));
        opetussuunnitelmaService.addPohja(ops);

        List<OpetussuunnitelmaInfoDto> opsit = opetussuunnitelmaService.getAll(Tyyppi.POHJA);
//        assertEquals(1, opsit.size());

        this.opsId = opsit.get(0).getId();
        assertNotNull(this.opsId);

        Vuosiluokkakokonaisuusviite viite = new Vuosiluokkakokonaisuusviite(UUID.randomUUID(), EnumSet.of(Vuosiluokka.VUOSILUOKKA_1, Vuosiluokka.VUOSILUOKKA_2));
        this.vlkViiteRef = Reference.of(vlkViitteet.save(viite));
    }

    private OppiaineDto createOppiaine(String nimi) {
        OppiaineDto oppiaineDto = new OppiaineDto();
        oppiaineDto.setTyyppi(OppiaineTyyppi.YHTEINEN);
        oppiaineDto.setNimi(lt(nimi));
        oppiaineDto.setKoodiUri("koodikoodi");
        oppiaineDto.setTunniste(UUID.randomUUID());
        oppiaineDto.setKoosteinen(false);
        return oppiaineDto;
    }

    private OpetuksenTavoiteDto createTavoite() {
        OpetuksenTavoiteDto tavoite = new OpetuksenTavoiteDto();
        tavoite.setTunniste(UUID.randomUUID());
        return tavoite;
    }

    private TekstiosaDto createTekstiosa(String nimi, String otsikko) {
        TekstiosaDto result = new TekstiosaDto();
        result.setTeksti(Optional.of(new LokalisoituTekstiDto(Collections.singletonMap("fi", nimi))));
        result.setOtsikko(Optional.of(new LokalisoituTekstiDto(Collections.singletonMap("fi", otsikko))));
        return result;
    }

    @Test
    public void testValinnainenAine() {
        OpetussuunnitelmaDto ops = opetussuunnitelmaService.getOpetussuunnitelmaKaikki(opsId);

        VuosiluokkakokonaisuusDto vlk = new VuosiluokkakokonaisuusDto(vlkViiteRef);
        OpsVuosiluokkakokonaisuusDto opsVlkDto = new OpsVuosiluokkakokonaisuusDto();
        vlk.setNimi(Optional.of(lt("ykköskakkoset")));
        vlk = vuosiluokkakokonaisuusService.add(ops.getId(), vlk);
        opsVlkDto.setVuosiluokkakokonaisuus(vlk);
        ops.setVuosiluokkakokonaisuudet(Collections.singleton(opsVlkDto));

        OppiaineDto valinnainen = createOppiaine("Valinnainen");
        valinnainen.setTyyppi(OppiaineTyyppi.MUU_VALINNAINEN);
        OppiaineenVuosiluokkakokonaisuusDto ovk = new OppiaineenVuosiluokkakokonaisuusDto();
        ovk.setVuosiluokkakokonaisuus(vlkViiteRef);

        OppiaineenVuosiluokkaDto ovlDto = new OppiaineenVuosiluokkaDto();
        ovk.setVuosiluokat(Collections.singleton(ovlDto));

        valinnainen.setVuosiluokkakokonaisuudet(Collections.singleton(ovk));

        valinnainen = oppiaineService.add(opsId, valinnainen);
        assertNotNull(valinnainen);

        OppiaineenVuosiluokkaDto vuosiluokka = valinnainen.getVuosiluokkakokonaisuudet().stream()
                .findAny()
                .get()
                .getVuosiluokat().stream()
                    .findFirst()
                    .get();

        List<TekstiosaDto> tavoitteet = new ArrayList<>();
        tavoitteet.add(createTekstiosa("hello", "world"));
        oppiaineService.updateValinnaisenVuosiluokanSisalto(opsId, valinnainen.getId(), vuosiluokka.getId(), tavoitteet);
        tavoitteet.add(createTekstiosa("foo", "bar"));
        OpsOppiaineDto get = oppiaineService.get(opsId, valinnainen.getId());
        oppiaineService.updateValinnaisenVuosiluokanSisalto(opsId, valinnainen.getId(), vuosiluokka.getId(), tavoitteet);

        { // Valinnaisten oppiaineiden tavoitteet (tavoitteet + sisältöalueet)
            OppiaineenVuosiluokkaDto ovlk = get.getOppiaine().getVuosiluokkakokonaisuudet().iterator().next()
                    .getVuosiluokat().iterator().next();
            assertEquals(ovlk.getTavoitteet().size(), 1);
            assertEquals(ovlk.getSisaltoalueet().size(), 1);
        }
    }

    @Test
    public void testCRUD() {
        OpetussuunnitelmaDto ops = opetussuunnitelmaService.getOpetussuunnitelmaKaikki(opsId);

        VuosiluokkakokonaisuusDto vlk = new VuosiluokkakokonaisuusDto(vlkViiteRef);
        vlk.setNimi(Optional.of(lt("ykköskakkoset")));
        vlk = vuosiluokkakokonaisuusService.add(ops.getId(), vlk);
        assertNotNull(vlk);

        OppiaineDto oppiaineDto = new OppiaineDto();
        oppiaineDto.setTyyppi(OppiaineTyyppi.YHTEINEN);
        oppiaineDto.setNimi(lt("Uskonto"));
        oppiaineDto.setKoodiUri("koodikoodi");
        oppiaineDto.setTunniste(UUID.randomUUID());
        oppiaineDto.setKoosteinen(false);

        oppiaineDto = oppiaineService.add(opsId, oppiaineDto);
        assertNotNull(oppiaineDto);

        oppiaineDto = new OppiaineDto();
        oppiaineDto.setTyyppi(OppiaineTyyppi.YHTEINEN);
        oppiaineDto.setNimi(lt("Äidinkieli"));
        oppiaineDto.setKoodiUri("koodi_123");
        oppiaineDto.setTunniste(UUID.randomUUID());

        OppiaineSuppeaDto oppimaaraDto = new OppiaineSuppeaDto();
        oppimaaraDto.setTyyppi(OppiaineTyyppi.YHTEINEN);
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
        oppiaineDto.setTyyppi(OppiaineTyyppi.YHTEINEN);
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

        oppiaineet = oppiaineService.getAll(opsId, true);
        assertNotNull(oppiaineet);
        assertEquals(0, oppiaineet.size());

        oppiaineet = oppiaineService.getAll(opsId, false);
        assertNotNull(oppiaineet);
        assertEquals(3, oppiaineet.size());

        OpsOppiaineDto opsOppiaineDto = oppiaineService.get(opsId, oppiaineDto.getId());
        assertNotNull(opsOppiaineDto);
        oppiaineDto = opsOppiaineDto.getOppiaine();
        assertNotNull(oppiaineDto);
        assertEquals("Matematiikka", oppiaineDto.getNimi().get(Kieli.FI));

        oppiaineDto.setNimi(lt("Biologia"));
        oppiaineDto = oppiaineService.update(opsId, oppiaineDto).getOppiaine();

        opsOppiaineDto = oppiaineService.get(opsId, oppiaineDto.getId());
        assertNotNull(opsOppiaineDto);
        oppiaineDto = opsOppiaineDto.getOppiaine();
        assertNotNull(oppiaineDto);
        assertEquals("Biologia", oppiaineDto.getNimi().get(Kieli.FI));

        assertEquals(1, oppiaineDto.getVuosiluokkakokonaisuudet().size());
        ovk = oppiaineDto.getVuosiluokkakokonaisuudet().stream().findFirst().get();
        final String TYOTAVAT = "Uudet työtavat";
        ovk.setTyotavat(getTekstiosa(TYOTAVAT));
        ovk = oppiaineService.updateVuosiluokkakokonaisuudenSisalto(opsId, oppiaineDto.getId(), ovk);
        assertNotNull(ovk);
        final String TYOTAVAT_OTSIKKO = "otsikko_" + TYOTAVAT;
        assertEquals(TYOTAVAT_OTSIKKO, ovk.getTyotavat().getOtsikko().get().get(Kieli.FI));

        opsOppiaineDto = oppiaineService.get(opsId, oppiaineDto.getId());
        assertNotNull(opsOppiaineDto);
        oppiaineDto = opsOppiaineDto.getOppiaine();
        ovk = oppiaineDto.getVuosiluokkakokonaisuudet().stream().findFirst().get();
        assertEquals(TYOTAVAT_OTSIKKO, ovk.getTyotavat().getOtsikko().get().get(Kieli.FI));

        assertNotNull(oppiaineRepo.isOma(opsId, oppiaineDto.getId()));
        assertNull(oppiaineRepo.isOma(opsId, -1));
    }

    private static TekstiosaDto getTekstiosa(String suffiksi) {
        TekstiosaDto dto = new TekstiosaDto();
        dto.setOtsikko(Optional.of(lt("otsikko_" + suffiksi)));
        dto.setTeksti(Optional.of(lt("teksti_" + suffiksi)));
        return dto;
    }
}

