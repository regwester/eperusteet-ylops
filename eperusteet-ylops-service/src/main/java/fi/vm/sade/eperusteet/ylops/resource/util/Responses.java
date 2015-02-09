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
package fi.vm.sade.eperusteet.ylops.resource.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Apuluokka tyypillisten vastausten luontiin.
 *
 * @author jhyoty
 */
public final class Responses {

    private Responses() {
    }

    /**
     * Luo ResponseEntityn
     *
     * @param <T> tyyppi
     * @param data body, voi oll null
     * @return jos data on null, asettaa paluukoodiksi NOT_FOUD, muussa tapauksessa OK
     */
    public static <T> ResponseEntity<T> ofNullable(T data) {
        if (data == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    public static <T> ResponseEntity<T> ofNullable(T data, HttpHeaders headers) {
        if (data == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

}
