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

import fi.vm.sade.eperusteet.ylops.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.ylops.domain.Tila;
import fi.vm.sade.eperusteet.ylops.domain.Tyyppi;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Kieli;
import fi.vm.sade.eperusteet.ylops.dto.koodisto.KoodistoDto;
import fi.vm.sade.eperusteet.ylops.dto.koodisto.OrganisaatioDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaInfoDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaKevytDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaLuontiDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.TekstiKappaleDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.TekstiKappaleViiteDto;
import fi.vm.sade.eperusteet.ylops.repository.ops.OpetussuunnitelmaRepository;
import fi.vm.sade.eperusteet.ylops.service.exception.ValidointiException;
import fi.vm.sade.eperusteet.ylops.service.mocks.EperusteetServiceMock;
import fi.vm.sade.eperusteet.ylops.test.AbstractIntegrationTest;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import static fi.vm.sade.eperusteet.ylops.test.util.TestUtils.lt;
import static fi.vm.sade.eperusteet.ylops.test.util.TestUtils.uniikkiString;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author mikkom
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class OpetussuunnitelmaServiceIT extends AbstractIntegrationTest {

    @Autowired
    OpetussuunnitelmaService opetussuunnitelmaService;

    @Autowired
    TekstiKappaleViiteService tekstiKappaleViiteService;

    @Autowired
    OpetussuunnitelmaRepository opetussuunnitelmaRepository;

    @Before
    public void setUp() {
        OpetussuunnitelmaLuontiDto ops = new OpetussuunnitelmaLuontiDto();
        ops.setPerusteenDiaarinumero(EperusteetServiceMock.DIAARINUMERO);
        ops.setNimi(lt(uniikkiString()));
        ops.setKuvaus(lt(uniikkiString()));
        ops.setTyyppi(Tyyppi.POHJA);
        ops.setKoulutustyyppi(KoulutusTyyppi.PERUSOPETUS);
        OpetussuunnitelmaDto luotu = opetussuunnitelmaService.addPohja(ops);
        opetussuunnitelmaService.updateTila(luotu.getId(), Tila.VALMIS);

        ops = new OpetussuunnitelmaLuontiDto();
        ops.setNimi(lt(uniikkiString()));
        ops.setKuvaus(lt(uniikkiString()));
        ops.setTila(Tila.LUONNOS);
        ops.setTyyppi(Tyyppi.OPS);
        ops.setKoulutustyyppi(KoulutusTyyppi.PERUSOPETUS);

        KoodistoDto kunta = new KoodistoDto();
        kunta.setKoodiUri("kunta_837");
        ops.setKunnat(new HashSet<>(Collections.singleton(kunta)));
        OrganisaatioDto kouluDto = new OrganisaatioDto();
        kouluDto.setNimi(lt("Etelä-Hervannan koulu"));
        kouluDto.setOid("1.2.246.562.10.00000000001");
        ops.setOrganisaatiot(new HashSet<>(Collections.singleton(kouluDto)));
        opetussuunnitelmaService.addOpetussuunnitelma(ops);
    }

    @Test
    public void testGetAll() {
        List<OpetussuunnitelmaInfoDto> opsit = opetussuunnitelmaService.getAll(Tyyppi.OPS);
        assertEquals(1, opsit.size());
    }

    @Test
    public void testGetById() {
        List<OpetussuunnitelmaInfoDto> opsit = opetussuunnitelmaService.getAll(Tyyppi.OPS);
        assertEquals(1, opsit.size());

        Long id = opsit.get(0).getId();
        assertNotNull(id);
        OpetussuunnitelmaDto ops = opetussuunnitelmaService.getOpetussuunnitelma(id);
        assertNotNull(ops);
        assertEquals(id, ops.getId());
        assertEquals(EperusteetServiceMock.DIAARINUMERO, ops.getPerusteenDiaarinumero());
    }

    @Test
    public void testUpdate() {
        List<OpetussuunnitelmaInfoDto> opsit = opetussuunnitelmaService.getAll(Tyyppi.OPS);
        assertEquals(1, opsit.size());

        Long id = opsit.get(0).getId();
        OpetussuunnitelmaDto ops = opetussuunnitelmaService.getOpetussuunnitelmaKaikki(id);
        Tila vanhaTila = ops.getTila();
        String kuvaus = uniikkiString();
        ops.setKuvaus(lt(kuvaus));
        ops.setTila(Tila.POISTETTU);
        Date pvm = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 1, Calendar.MARCH, 12).getTime();
        ops.setPaatospaivamaara(pvm);
        opetussuunnitelmaService.updateOpetussuunnitelma(ops);

        ops = opetussuunnitelmaService.getOpetussuunnitelmaKaikki(id);
        assertEquals(kuvaus, ops.getKuvaus().get(Kieli.FI));
        assertEquals(vanhaTila, ops.getTila());
        assertEquals(pvm, ops.getPaatospaivamaara());
    }

    @Test
    public void testUpdateTila() {
        List<OpetussuunnitelmaInfoDto> opsit = opetussuunnitelmaService.getAll(Tyyppi.POHJA);
        assertEquals(1, opsit.size());

        Long id = opsit.get(0).getId();
        {
            OpetussuunnitelmaDto ops = opetussuunnitelmaService.getOpetussuunnitelmaKaikki(id);
            assertEquals(Tila.VALMIS, ops.getTila());

            ops = opetussuunnitelmaService.updateTila(id, Tila.LUONNOS);
            assertEquals(Tila.LUONNOS, ops.getTila());

            ops = opetussuunnitelmaService.updateTila(id, Tila.VALMIS);
            assertEquals(Tila.VALMIS, ops.getTila());
        }

        // Vanha valmis pohja merkitään poistetuksi kun uusi pohja merkitään valmiiksi
        OpetussuunnitelmaLuontiDto ops = new OpetussuunnitelmaLuontiDto();
        ops.setPerusteenDiaarinumero(EperusteetServiceMock.DIAARINUMERO);
        ops.setNimi(lt(uniikkiString()));
        ops.setKuvaus(lt(uniikkiString()));
        ops.setTyyppi(Tyyppi.POHJA);
        OpetussuunnitelmaDto luotu = opetussuunnitelmaService.addPohja(ops);
        luotu = opetussuunnitelmaService.updateTila(luotu.getId(), Tila.VALMIS);
        assertEquals(Tila.VALMIS, luotu.getTila());

        opsit = opetussuunnitelmaService.getAll(Tyyppi.POHJA);
        assertEquals(2, opsit.size());

        luotu = opetussuunnitelmaService.getOpetussuunnitelmaKaikki(luotu.getId());
        assertEquals(Tila.VALMIS, luotu.getTila());

        luotu = opetussuunnitelmaService.getOpetussuunnitelmaKaikki(id);
        assertEquals(Tila.POISTETTU, luotu.getTila());
    }

    @Test
    public void testUpdateOpsinTila() {
        List<OpetussuunnitelmaInfoDto> opsit = opetussuunnitelmaService.getAll(Tyyppi.OPS);
        assertEquals(1, opsit.size());

        Long id = opsit.get(0).getId();
        OpetussuunnitelmaDto ops = opetussuunnitelmaService.getOpetussuunnitelmaKaikki(id);
        assertEquals(Tila.LUONNOS, ops.getTila());

        // Opsin voi palauttaa valmiista luonnokseksi, muuten normaali tilan eteneminen
        ops = opetussuunnitelmaService.updateTila(id, Tila.VALMIS);
        assertEquals(Tila.VALMIS, ops.getTila());

        ops = opetussuunnitelmaService.updateTila(id, Tila.LUONNOS);
        assertEquals(Tila.LUONNOS, ops.getTila());

        ops = opetussuunnitelmaService.updateTila(id, Tila.VALMIS);
        assertEquals(Tila.VALMIS, ops.getTila());

        opetussuunnitelmaService.updateTila(id, Tila.JULKAISTU);
        assertEquals(Tila.VALMIS, ops.getTila());
    }

    @Test
    public void testDelete() {
        List<OpetussuunnitelmaInfoDto> opsit = opetussuunnitelmaService.getAll(Tyyppi.OPS);
        assertEquals(1, opsit.size());

        Long id = opsit.get(0).getId();
        opetussuunnitelmaService.removeOpetussuunnitelma(id);

        assertEquals(0, opetussuunnitelmaService.getAll(Tyyppi.OPS).size());
    }

    @Test
    public void testAddTekstiKappale() {
        List<OpetussuunnitelmaInfoDto> opsit = opetussuunnitelmaService.getAll(Tyyppi.OPS);
        assertEquals(1, opsit.size());

        Long opsId = opsit.get(0).getId();

        TekstiKappaleDto tekstiKappale = new TekstiKappaleDto();
        tekstiKappale.setNimi(lt("Otsake"));
        tekstiKappale.setTeksti(lt("Leipää ja tekstiä"));

        TekstiKappaleViiteDto.Matala viiteDto = new TekstiKappaleViiteDto.Matala();
        viiteDto.setPakollinen(true);
        viiteDto.setTekstiKappale(tekstiKappale);

        TekstiKappaleViiteDto.Puu tekstit = opetussuunnitelmaService.getTekstit(opsId);
        final int lastenMaara = tekstit.getLapset() != null ? tekstit.getLapset().size() : 0;

        viiteDto = opetussuunnitelmaService.addTekstiKappale(opsId, viiteDto);

        tekstit = opetussuunnitelmaService.getTekstit(opsId);
        assertNotNull(tekstit);
        assertEquals(lastenMaara + 1, tekstit.getLapset().size());

        TekstiKappaleViiteDto.Matala dto = tekstiKappaleViiteService.getTekstiKappaleViite(opsId, viiteDto.getId());
        assertNotNull(dto);
        assertTrue(dto.isPakollinen());

        dto.setPakollinen(false);
        TekstiKappaleViiteDto updatedDto =
            tekstiKappaleViiteService.updateTekstiKappaleViite(opsId, viiteDto.getId(), dto);
        assertFalse(updatedDto.isPakollinen());
        dto = tekstiKappaleViiteService.getTekstiKappaleViite(opsId, viiteDto.getId());
        assertNotNull(dto);
        assertFalse(dto.isPakollinen());

        tekstiKappale = new TekstiKappaleDto();
        tekstiKappale.setNimi(lt("Aliotsake"));
        tekstiKappale.setTeksti(lt("Sirkushuveja"));
        viiteDto = new TekstiKappaleViiteDto.Matala();
        viiteDto.setTekstiKappale(tekstiKappale);

        opetussuunnitelmaService.addTekstiKappaleLapsi(opsId, dto.getId(), viiteDto);

        tekstit = opetussuunnitelmaService.getTekstit(opsId);
        assertNotNull(tekstit);
        assertEquals(1, tekstit.getLapset().get(lastenMaara).getLapset().size());
    }
}
