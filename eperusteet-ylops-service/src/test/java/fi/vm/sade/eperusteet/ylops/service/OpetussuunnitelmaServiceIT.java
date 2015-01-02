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

import fi.vm.sade.eperusteet.ylops.domain.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.domain.OpetussuunnitelmanTila;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Kieli;
import fi.vm.sade.eperusteet.ylops.domain.teksti.TekstiKappale;
import fi.vm.sade.eperusteet.ylops.dto.OpetussuunnitelmaDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.TekstiKappaleDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.TekstiKappaleViiteDto;
import fi.vm.sade.eperusteet.ylops.service.teksti.TekstiKappaleViiteService;
import fi.vm.sade.eperusteet.ylops.service.test.AbstractIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.util.List;

import static fi.vm.sade.eperusteet.ylops.service.test.util.TestUtils.lokalisoituTekstiOf;
import static fi.vm.sade.eperusteet.ylops.service.test.util.TestUtils.lt;
import static fi.vm.sade.eperusteet.ylops.service.test.util.TestUtils.uniikkiString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author mikkom
 */
@Transactional
public class OpetussuunnitelmaServiceIT extends AbstractIntegrationTest {

    @Autowired
    OpetussuunnitelmaService opetussuunnitelmaService;

    @Autowired
    TekstiKappaleViiteService tekstiKappaleViiteService;

    @Before
    public void setUp() {
        OpetussuunnitelmaDto ops;
        ops = new OpetussuunnitelmaDto();
        ops.setNimi(lt(uniikkiString()));
        ops.setKuvaus(lt(uniikkiString()));
        ops.setTila(OpetussuunnitelmanTila.LUONNOS);
        opetussuunnitelmaService.addOpetussuunnitelma(ops);
    }

    @Test
    public void testGetAll() {
        List<OpetussuunnitelmaDto> opsit = opetussuunnitelmaService.getAll();
        assertEquals(1, opsit.size());
    }

    @Test
    public void testGetById() {
        List<OpetussuunnitelmaDto> opsit = opetussuunnitelmaService.getAll();
        assertEquals(1, opsit.size());

        Long id = opsit.get(0).getId();
        assertNotNull(id);
        OpetussuunnitelmaDto ops = opetussuunnitelmaService.getOpetussuunnitelma(id);
        assertNotNull(ops);
        assertEquals(id, ops.getId());
    }

    @Test
    public void testUpdate() {
        List<OpetussuunnitelmaDto> opsit = opetussuunnitelmaService.getAll();
        assertEquals(1, opsit.size());

        Long id = opsit.get(0).getId();
        OpetussuunnitelmaDto ops = opetussuunnitelmaService.getOpetussuunnitelma(id);
        String kuvaus = uniikkiString();
        ops.setKuvaus(lt(kuvaus));
        opetussuunnitelmaService.updateOpetussuunnitelma(ops);

        ops = opetussuunnitelmaService.getOpetussuunnitelma(id);
        assertEquals(kuvaus, ops.getKuvaus().get(Kieli.FI));
    }

    @Test
    public void testDelete() {
        List<OpetussuunnitelmaDto> opsit = opetussuunnitelmaService.getAll();
        assertEquals(1, opsit.size());

        Long id = opsit.get(0).getId();
        opetussuunnitelmaService.removeOpetussuunnitelma(id);

        assertEquals(0, opetussuunnitelmaService.getAll().size());
    }

    @Test
    public void testAddTekstiKappale() {
        List<OpetussuunnitelmaDto> opsit = opetussuunnitelmaService.getAll();
        assertEquals(1, opsit.size());

        Long id = opsit.get(0).getId();

        TekstiKappaleDto tekstiKappale = new TekstiKappaleDto();
        tekstiKappale.setNimi(lt("Otsake"));
        tekstiKappale.setTeksti(lt("Leipää ja tekstiä"));
        tekstiKappale.setTila(OpetussuunnitelmanTila.LUONNOS);

        TekstiKappaleViiteDto.Matala viiteDto = new TekstiKappaleViiteDto.Matala();
        viiteDto.setTekstiKappale(tekstiKappale);

        viiteDto = opetussuunnitelmaService.addTekstiKappale(id, viiteDto);

        TekstiKappaleViiteDto.Puu tekstit = opetussuunnitelmaService.getTekstit(id);
        assertNotNull(tekstit);
        assertEquals(1, tekstit.getLapset().size());

        TekstiKappaleViiteDto.Matala dto = tekstiKappaleViiteService.getTekstiKappaleViite(id, viiteDto.getId());
        assertNotNull(dto);
    }
}
