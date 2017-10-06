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
package fi.vm.sade.eperusteet.ylops.service.mapping;

import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.dto.koodisto.KoodistoDto;
import fi.vm.sade.eperusteet.ylops.dto.koodisto.OrganisaatioDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OpetussuunnitelmaDto;

import java.util.Collections;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.junit.Assert;
import org.junit.Test;

import static fi.vm.sade.eperusteet.ylops.test.util.TestUtils.lt;

/**
 * @author mikkom
 */
public class MappingTest {

    @Getter
    @Setter
    public static class Luokka {
        public Set<String> domainString;
    }

    @Getter
    @Setter
    public static class LuokkaDto {
        public Set<OrganisaatioDto> domainString;
    }

    @Test
    public void testOrganisaatioMapping() {
        DefaultMapperFactory factory = new DefaultMapperFactory.Builder().build();
        factory.getConverterFactory().registerConverter(new OrganisaatioConverter());
        factory.getConverterFactory().registerConverter(new LokalisoituTekstiConverter());

        MapperFacade mapper = factory.getMapperFacade();

        String domainString = "Foobar";
        OrganisaatioDto organisaatioDto = mapper.map(domainString, OrganisaatioDto.class);
        Assert.assertNotNull(organisaatioDto);
        Assert.assertEquals(domainString, organisaatioDto.getOid());

        domainString = mapper.map(organisaatioDto, String.class);
        Assert.assertEquals(domainString, organisaatioDto.getOid());

        Luokka luokka = new Luokka();
        luokka.setDomainString(Collections.singleton("Foobar"));

        LuokkaDto dto = mapper.map(luokka, LuokkaDto.class);
        Assert.assertNotNull(dto);

        luokka.setDomainString(Collections.emptySet());

        mapper.map(dto, luokka);

        OpetussuunnitelmaDto opsDto = new OpetussuunnitelmaDto();
        opsDto.setNimi(lt("opsi"));

        KoodistoDto koodiDto = new KoodistoDto();
        koodiDto.setKoodiUri("kunta_007");
        opsDto.setKunnat(Collections.singleton(koodiDto));
        organisaatioDto = new OrganisaatioDto();
        organisaatioDto.setNimi(lt("Etelä-Hervannan koulu"));
        organisaatioDto.setOid("2.14.132352.26");
        opsDto.setOrganisaatiot(Collections.singleton(organisaatioDto));

        Opetussuunnitelma ops = mapper.map(opsDto, Opetussuunnitelma.class);
        Assert.assertNotNull(ops);
    }
}
