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
import fi.vm.sade.eperusteet.ylops.domain.peruste.Peruste;
import fi.vm.sade.eperusteet.ylops.domain.peruste.PerusteOpetuksentavoite;
import fi.vm.sade.eperusteet.ylops.domain.peruste.PerusteOppiaine;
import fi.vm.sade.eperusteet.ylops.domain.peruste.PerusteOppiaineenVuosiluokkakokonaisuus;
import fi.vm.sade.eperusteet.ylops.dto.koodisto.KoodistoDto;
import fi.vm.sade.eperusteet.ylops.dto.koodisto.OrganisaatioDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaDto;
import fi.vm.sade.eperusteet.ylops.repository.ops.OpetussuunnitelmaRepository;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.ylops.service.mocks.EperusteetServiceMock;
import fi.vm.sade.eperusteet.ylops.test.AbstractIntegrationTest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import static fi.vm.sade.eperusteet.ylops.test.util.TestUtils.lt;
import static fi.vm.sade.eperusteet.ylops.test.util.TestUtils.uniikkiString;

/**
 * @author mikkom
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class VuosiluokkaistusIT extends AbstractIntegrationTest {

    @Autowired
    OpetussuunnitelmaService opsit;

    @Autowired
    TekstiKappaleViiteService tekstiKappaleViiteService;

    @Autowired
    OpetussuunnitelmaRepository opetussuunnitelmaRepository;

    @Autowired
    OppiaineService oppiaineet;

    @Autowired
    private DtoMapper mapper;

    @Autowired
    private EperusteetServiceMock mock;

    private static boolean setupDone = false;
    private static Long opsId = null;

    @Before
    public void setUp() throws IOException {

        if (!setupDone) {
            try (InputStream json = getClass().getResourceAsStream("/data/peruste.json")) {
                mock.setPeruste(json);
            }

            OpetussuunnitelmaDto ops;
            ops = new OpetussuunnitelmaDto();
            ops.setPerusteenDiaarinumero(EperusteetServiceMock.DIAARINUMERO);
            ops.setNimi(lt(uniikkiString()));
            ops.setKuvaus(lt(uniikkiString()));
            ops.setTyyppi(Tyyppi.POHJA);
            ops = opsit.addPohja(ops);
            ops = opsit.updateTila(ops.getId(), Tila.VALMIS);

            ops = new OpetussuunnitelmaDto();
            ops.setNimi(lt(uniikkiString()));
            ops.setKuvaus(lt(uniikkiString()));
            ops.setTila(Tila.LUONNOS);
            ops.setTyyppi(Tyyppi.OPS);

            KoodistoDto kunta = new KoodistoDto();
            kunta.setKoodiUri("kunta_837");
            ops.setKunnat(new HashSet<>(Collections.singleton(kunta)));
            OrganisaatioDto kouluDto = new OrganisaatioDto();
            kouluDto.setNimi(lt("Etelä-Hervannan koulu"));
            kouluDto.setOid("1.2.15252345624572462");
            ops.setOrganisaatiot(new HashSet<>(Collections.singleton(kouluDto)));
            ops = opsit.addOpetussuunnitelma(ops);
            opsId = ops.getId();
            setupDone = true;
        }
    }

    @Test
    public void testVuosiluokkaistus() {
        OpetussuunnitelmaDto opsDto = opsit.getOpetussuunnitelma(opsId);
        Peruste peruste = opsit.getPeruste(opsId);

        opsDto.getOppiaineet().stream()
            .flatMap(o -> Stream.of(o.getOppiaine()))
            .forEach(oa -> {
                PerusteOppiaine po = peruste.getPerusopetus().getOppiaine(oa.getTunniste()).get();
                oa.getVuosiluokkakokonaisuudet().forEach(vk -> {
                    PerusteOppiaineenVuosiluokkakokonaisuus pvk =
                        po.getVuosiluokkakokonaisuus(UUID.fromString(vk.getVuosiluokkakokonaisuus().toString())).get();
                    Map<Vuosiluokka, Set<UUID>> tavoitteet = new HashMap<>();
                    pvk.getVuosiluokkaKokonaisuus().getVuosiluokat().forEach(
                        l -> tavoitteet.put(l, pvk.getTavoitteet().stream()
                                                  .map(PerusteOpetuksentavoite::getTunniste).collect(Collectors.toSet())));
                    oppiaineet.updateVuosiluokkienTavoitteet(opsId, oa.getId(), vk.getId(), tavoitteet);
                });
            });

        opsDto = opsit.getOpetussuunnitelma(opsId);
        opsDto.getOppiaineet().stream()
            .flatMap(o -> Stream.of(o.getOppiaine()))
            .forEach(oa -> {
                PerusteOppiaine po = peruste.getPerusopetus().getOppiaine(oa.getTunniste()).get();
                oa.getVuosiluokkakokonaisuudet().forEach(vk -> {
                    PerusteOppiaineenVuosiluokkakokonaisuus pvk =
                        po.getVuosiluokkakokonaisuus(UUID.fromString(vk.getVuosiluokkakokonaisuus().toString())).get();
                    Map<Vuosiluokka, Set<UUID>> tavoitteet = new HashMap<>();
                    pvk.getVuosiluokkaKokonaisuus().getVuosiluokat().forEach(
                        l -> tavoitteet.put(l, pvk.getTavoitteet().stream()
                                                  .map(PerusteOpetuksentavoite::getTunniste).collect(Collectors.toSet())));
                    Assert.assertEquals(tavoitteet.keySet().size(), vk.getVuosiluokat().size());
                    vk.getVuosiluokat().forEach(l -> {
                        Assert.assertEquals(pvk.getTavoitteet().size(), l.getTavoitteet().size());
                        Assert.assertEquals(pvk.getSisaltoalueet().size(), l.getSisaltoalueet().size());
                    });
                });
            });

    }

}
