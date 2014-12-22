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
package fi.vm.sade.eperusteet.ylops.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.stream.Stream;

/**
 *
 * @author mikkom
 */
public enum OpetussuunnitelmanTila {

    LUONNOS("luonnos"),
    VALMIS("valmis"),
    POISTETTU("poistettu");

    private final String tila;

    private OpetussuunnitelmanTila(String tila) { this.tila = tila; }

    @Override
    public String toString() { return tila; }

    @JsonCreator
    public static OpetussuunnitelmanTila of(String tila) {
        return Stream.of(values()).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(tila +
                        " ei ole kelvollinen tila"));
   }
}
