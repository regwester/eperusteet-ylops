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
package fi.vm.sade.eperusteet.ylops.service.security;

import fi.vm.sade.eperusteet.ylops.domain.Vuosiluokka;
import fi.vm.sade.eperusteet.ylops.domain.Vuosiluokkakokonaisuusviite;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Omistussuhde;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.VuosiluokkakokonaisuusDto;
import fi.vm.sade.eperusteet.ylops.repository.ops.VuosiluokkakokonaisuusviiteRepository;
import fi.vm.sade.eperusteet.ylops.service.ops.OpetussuunnitelmaService;
import fi.vm.sade.eperusteet.ylops.service.ops.VuosiluokkakokonaisuusService;
import fi.vm.sade.eperusteet.ylops.test.AbstractIntegrationTest;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author jhyoty
 */
@DirtiesContext
public class PermissionRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private PermissionReporitory permissions;

    @Autowired
    private OpetussuunnitelmaService ops;

    @Autowired
    private VuosiluokkakokonaisuusService vks;

    @Autowired
    private VuosiluokkakokonaisuusviiteRepository vkvr;

    @Test
    @Ignore
    public void testPermissionRepository() {

        Vuosiluokkakokonaisuusviite vs = vkvr.save(new Vuosiluokkakokonaisuusviite(UUID.randomUUID(), EnumSet.of(Vuosiluokka.VUOSILUOKKA_1, Vuosiluokka.VUOSILUOKKA_2)));
        vkvr.save(new Vuosiluokkakokonaisuusviite(UUID.randomUUID(), EnumSet.of(Vuosiluokka.VUOSILUOKKA_3, Vuosiluokka.VUOSILUOKKA_4, Vuosiluokka.VUOSILUOKKA_5, Vuosiluokka.VUOSILUOKKA_6)));
        vkvr.save(new Vuosiluokkakokonaisuusviite(UUID.randomUUID(), EnumSet.of(Vuosiluokka.VUOSILUOKKA_7, Vuosiluokka.VUOSILUOKKA_8, Vuosiluokka.VUOSILUOKKA_9)));

        final OpetussuunnitelmaDto dto = ops.addOpetussuunnitelma(new OpetussuunnitelmaDto());
        VuosiluokkakokonaisuusDto vk = vks.add(dto.getId(), new VuosiluokkakokonaisuusDto(vs.getReference()));

        ops.addOpetussuunnitelma(new OpetussuunnitelmaDto());
        final Long id = dto.getTekstit().get().getLapset().get(0).getTekstiKappale().getId();

        Set<Opetussuunnitelma> opsit = permissions.findOpsByTekstikappaleId(id, singleton(Omistussuhde.OMA));
        assertEquals(1, opsit.size());
        opsit.forEach(o -> assertEquals(dto.getId(), o.getId()));

        opsit = permissions.findOpsByVuosiluokkakokonaisuusId(vk.getId());
        assertEquals(1, opsit.size());
        opsit.forEach(o -> assertEquals(dto.getId(), o.getId()));
    }
}
