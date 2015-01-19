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

import fi.vm.sade.eperusteet.ylops.domain.teksti.LokalisoituTeksti;
import ma.glasnost.orika.converter.builtin.PassThroughConverter;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author jhyoty
 */
@Configuration
public class DtoMapperConfig {

    @Bean
    public DtoMapper dtoMapper(
        LokalisoituTekstiConverter lokalisoituTekstiConverter) {
        DefaultMapperFactory factory = new DefaultMapperFactory.Builder()
            .build();

        factory.getConverterFactory().registerConverter(lokalisoituTekstiConverter);
        factory.getConverterFactory().registerConverter(new PassThroughConverter(LokalisoituTeksti.class));
        factory.getConverterFactory().registerConverter(new OrganisaatioConverter());
        OptionalSupport.register(factory);

        return new DtoMapperImpl(factory.getMapperFacade());
    }

}
