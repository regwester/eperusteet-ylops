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
package fi.vm.sade.eperusteet.ylops.resource.config;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import fi.vm.sade.eperusteet.ylops.dto.EntityReference;

/**
 * JSON-kenttien nimeämisstrategia.
 *
 * Nimeää EntitiReference -tyyppiä olevat kentät muotoon _kentännimi ja käyttää muissa tapauksissa oletusnimeämistä.
 *
 * @author jhyoty
 */
class CustomNamingStrategy extends PropertyNamingStrategy {

    @Override
    public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return tryToconvertFromMethodName(method, defaultName);
    }

    @Override
    public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return tryToconvertFromMethodName(method, defaultName);
    }

    private String tryToconvertFromMethodName(AnnotatedMethod annotatedMethod, String defaultName) {
        if ((annotatedMethod.getParameterCount() == 1 && EntityReference.class.isAssignableFrom(annotatedMethod.getParameter(0).getRawType())) ||
            EntityReference.class.isAssignableFrom(annotatedMethod.getRawReturnType())) {
            defaultName = '_' + defaultName;
        }
        return defaultName;
    }

}
