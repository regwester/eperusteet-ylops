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

import fi.vm.sade.eperusteet.ylops.dto.koodisto.OrganisaatioDto;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.springframework.stereotype.Component;

/**
 * @author mikkom
 */
@Component
public class OrganisaatioConverter extends BidirectionalConverter<OrganisaatioDto, String> {

    @Override
    public String convertTo(OrganisaatioDto source, Type<String> destinationType) {
        return source.getOid();
    }

    @Override
    public OrganisaatioDto convertFrom(String source, Type<OrganisaatioDto> destinationType) {
        OrganisaatioDto organisaatioDto = new OrganisaatioDto();
        organisaatioDto.setOid(source);


        return organisaatioDto;
    }
}
