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

import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 *
 * @author jhyoty
 */
@Embeddable
@EqualsAndHashCode
public class LaajaalainenosaaminenViite {

    @Getter
    @Column(name = "laajaalainenosaaminen_viite")
    private String viite;

    public LaajaalainenosaaminenViite(String viite) {
        this.viite = viite;
    }

    public LaajaalainenosaaminenViite(LaajaalainenosaaminenViite other) {
        this.viite = other.getViite();
    }
    
    protected LaajaalainenosaaminenViite() {
        //JPA
    }
}
