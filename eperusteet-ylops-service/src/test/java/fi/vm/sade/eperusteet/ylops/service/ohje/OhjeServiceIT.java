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
package fi.vm.sade.eperusteet.ylops.service.ohje;

import fi.vm.sade.eperusteet.ylops.domain.Tila;
import fi.vm.sade.eperusteet.ylops.domain.ohje.Ohje;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Kieli;
import fi.vm.sade.eperusteet.ylops.domain.teksti.TekstiKappale;
import fi.vm.sade.eperusteet.ylops.dto.ohje.OhjeDto;
import fi.vm.sade.eperusteet.ylops.repository.ohje.OhjeRepository;
import fi.vm.sade.eperusteet.ylops.repository.teksti.TekstiKappaleRepository;
import fi.vm.sade.eperusteet.ylops.service.ohje.OhjeService;
import fi.vm.sade.eperusteet.ylops.test.AbstractIntegrationTest;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static fi.vm.sade.eperusteet.ylops.test.util.TestUtils.lokalisoituTekstiOf;
import static fi.vm.sade.eperusteet.ylops.test.util.TestUtils.lt;
import java.util.List;

/**
 * @author mikkom
 */
@Transactional
public class OhjeServiceIT extends AbstractIntegrationTest {

    @Autowired
    private OhjeService ohjeService;

    @Autowired
    private OhjeRepository ohjeRepository;

    @Autowired
    private TekstiKappaleRepository tekstiKappaleRepository;

    @Test
    public void testAddAndGetTekstiKappaleOhje() {
        TekstiKappale tekstiKappale = new TekstiKappale();
        final String NIMI = "Namnet";
        final String TEKSTI = "Teksten";
        tekstiKappale.setNimi(lokalisoituTekstiOf(Kieli.SV, NIMI));
        tekstiKappale.setTeksti(lokalisoituTekstiOf(Kieli.SV, TEKSTI));
        tekstiKappale.setTila(Tila.LUONNOS);

        tekstiKappale = tekstiKappaleRepository.save(tekstiKappale);

        OhjeDto ohjeDto = new OhjeDto();
        final String OHJETEKSTI = "Ohjeteksti";
        ohjeDto.setTeksti(lt(OHJETEKSTI));
        ohjeDto.setKohde(tekstiKappale.getTunniste());

        ohjeDto = ohjeService.addOhje(ohjeDto);
        Assert.assertNotNull(ohjeDto);
        Assert.assertEquals(OHJETEKSTI, ohjeDto.getTeksti().get(Kieli.FI));
        Assert.assertEquals(tekstiKappale.getTunniste(), ohjeDto.getKohde());

        ohjeDto = ohjeService.getOhje(ohjeDto.getId());
        Assert.assertNotNull(ohjeDto);
        Assert.assertEquals(OHJETEKSTI, ohjeDto.getTeksti().get(Kieli.FI));
        Assert.assertEquals(tekstiKappale.getTunniste(), ohjeDto.getKohde());

        List<OhjeDto> ohjeDtos = ohjeService.getTekstiKappaleOhjeet(tekstiKappale.getTunniste());
        Assert.assertNotNull(ohjeDtos);
        for (OhjeDto dto : ohjeDtos) {
            Assert.assertEquals(OHJETEKSTI, dto.getTeksti().get(Kieli.FI));
            Assert.assertEquals(tekstiKappale.getTunniste(), dto.getKohde());
        }
    }

    @Test
    public void testGetAndDeleteOhje() {
        Ohje ohje = new Ohje();
        final String OHJETEKSTI = "Så här";
        final UUID uuid = UUID.randomUUID();
        ohje.setTeksti(lokalisoituTekstiOf(Kieli.SV, OHJETEKSTI));
        ohje.setKohde(uuid);
        ohje = ohjeRepository.save(ohje);

        OhjeDto dto = ohjeService.getOhje(ohje.getId());
        Assert.assertNotNull(dto);

        Assert.assertEquals(OHJETEKSTI, dto.getTeksti().get(Kieli.SV));
        Assert.assertEquals(uuid, dto.getKohde());

        ohjeService.removeOhje(ohje.getId());
    }
}
