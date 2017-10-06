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
package fi.vm.sade.eperusteet.ylops.domain.teksti;

import fi.vm.sade.eperusteet.ylops.domain.teksti.Kieli;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

/**
 * @author jhyoty
 */
@Embeddable
class Teksti implements Serializable {

    public Teksti() {
        //NOP
    }

    public Teksti(Kieli kieli, String teksti) {
        this.kieli = kieli;
        this.teksti = teksti;
    }

    @Column(insertable = true, updatable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private Kieli kieli;

    @NotNull
    @Column(columnDefinition = "TEXT", insertable = true, updatable = false)
    private String teksti;

    public Kieli getKieli() {
        return kieli;
    }

    public String getTeksti() {
        return teksti;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.kieli);
        hash = 47 * hash + Objects.hashCode(this.teksti);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Teksti) {
            final Teksti other = (Teksti) obj;
            return this.kieli == other.kieli && Objects.equals(this.teksti, other.teksti);
        }
        return false;
    }

}
