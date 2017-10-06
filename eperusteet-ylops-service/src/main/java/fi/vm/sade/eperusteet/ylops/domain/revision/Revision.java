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
package fi.vm.sade.eperusteet.ylops.domain.revision;

import java.io.Serializable;
import java.util.Date;

import lombok.EqualsAndHashCode;

import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Revision implements Serializable {
    private final Long id;
    private final Integer numero;
    private final Date pvm;
    private final String muokkaajaOid;
    private final String kommentti;

    public Revision(Long id, Integer number, Long timestamp, String muokkaajaOid, String kommentti) {
        this.id = id;
        this.numero = number;
        this.pvm = new Date(timestamp);
        this.muokkaajaOid = muokkaajaOid;
        this.kommentti = kommentti;
    }
}
