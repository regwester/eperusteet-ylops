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

import fi.vm.sade.eperusteet.ylops.domain.Vuosiluokka;
import fi.vm.sade.eperusteet.ylops.domain.Vuosiluokkakokonaisuusviite;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.dto.EntityReference;
import fi.vm.sade.eperusteet.ylops.dto.ops.VuosiluokkakokonaisuusDto;
import fi.vm.sade.eperusteet.ylops.repository.OpetussuunnitelmaRepository;
import fi.vm.sade.eperusteet.ylops.repository.VuosiluokkakokonaisuusviiteRepository;
import fi.vm.sade.eperusteet.ylops.service.ops.VuosiluokkakokonaisuusService;
import fi.vm.sade.eperusteet.ylops.test.AbstractIntegrationTest;
import java.util.EnumSet;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

/**
 * @author hyoty
 */
@DirtiesContext
public class VuosiluokkakokonaisuusServiceIT extends AbstractIntegrationTest {

    @Autowired
    private OpetussuunnitelmaRepository suunnitelmat;

    @Autowired
    private VuosiluokkakokonaisuusviiteRepository viitteet;

    @Autowired
    private VuosiluokkakokonaisuusService service;

    private Long opsId;
    private EntityReference viite1Ref;
    private EntityReference viite2Ref;

    @Before
    public void setup() {
        Vuosiluokkakokonaisuusviite viite = new Vuosiluokkakokonaisuusviite("VUOSILUOKAT_1_2", EnumSet.of(Vuosiluokka.VUOSILUOKKA_1, Vuosiluokka.VUOSILUOKKA_2));
        this.viite1Ref = viitteet.save(viite).getReference();
        viite = new Vuosiluokkakokonaisuusviite("VUOSILUOKAT_3_6", EnumSet.of(Vuosiluokka.VUOSILUOKKA_3, Vuosiluokka.VUOSILUOKKA_4, Vuosiluokka.VUOSILUOKKA_5, Vuosiluokka.VUOSILUOKKA_6));
        this.viite2Ref = viitteet.save(viite).getReference();
        Opetussuunnitelma ops = new Opetussuunnitelma();
        ops = suunnitelmat.save(ops);
        opsId = ops.getId();
    }

    @Test
    public void crudTest() {

        VuosiluokkakokonaisuusDto dto = new VuosiluokkakokonaisuusDto();
        dto.setTunniste(Optional.of(viite1Ref));

        dto = service.add(opsId, dto);
        dto = service.get(opsId, dto.getId());
        dto.setTunniste(Optional.of(viite2Ref));
        dto = service.update(opsId, dto);
        service.delete(opsId, dto.getId());
    }

}
