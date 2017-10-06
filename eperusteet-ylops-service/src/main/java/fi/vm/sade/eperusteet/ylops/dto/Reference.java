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
package fi.vm.sade.eperusteet.ylops.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import fi.vm.sade.eperusteet.ylops.domain.ReferenceableEntity;

import java.util.UUID;

import lombok.EqualsAndHashCode;

/**
 * Kuvaa viitettä toiseen entiteettiin.
 *
 * @author jhyoty
 */
@EqualsAndHashCode
public class Reference {

    private final String id;

    @JsonCreator
    public Reference(String id) {
        this.id = id;
    }

    @JsonValue
    public String getId() {
        return id;
    }

    public static Reference of(ReferenceableEntity e) {
        return (e == null || e.getId() == null) ? null : new Reference(e.getId().toString());
    }

    public static Reference of(ReferenceableDto d) {
        return (d == null || d.getId() == null) ? null : new Reference(d.getId().toString());

    }

    public static Reference of(Long id) {
        return id == null ? null : new Reference(id.toString());
    }

    public static Reference of(UUID id) {
        return id == null ? null : new Reference(id.toString());
    }

    @Override
    public String toString() {
        return id;
    }

}
