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
package fi.vm.sade.eperusteet.ylops.domain.vuosiluokkakokonaisuus;

import fi.vm.sade.eperusteet.ylops.domain.AbstractReferenceableEntity;
import fi.vm.sade.eperusteet.ylops.domain.LaajaalainenosaaminenViite;
import fi.vm.sade.eperusteet.ylops.domain.teksti.LokalisoituTeksti;
import fi.vm.sade.eperusteet.ylops.domain.validation.ValidHtml;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 * @author jhyoty
 */
@Entity
@Audited
@Table(name = "vlkok_laaja_osaaminen")
public class Laajaalainenosaaminen extends AbstractReferenceableEntity {

    @Getter
    @Setter
    @Embedded
    private LaajaalainenosaaminenViite laajaalainenosaaminen;

    @ManyToOne
    @Getter
    @NotNull
    @JoinColumn(updatable = false, nullable = false)
    private Vuosiluokkakokonaisuus vuosiluokkakokonaisuus;

    @Getter
    @Setter
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml
    private LokalisoituTeksti kuvaus;

    public Laajaalainenosaaminen() {
    }

    public Laajaalainenosaaminen(Laajaalainenosaaminen other) {
        this.laajaalainenosaaminen = new LaajaalainenosaaminenViite(other.getLaajaalainenosaaminen());
        this.kuvaus = other.getKuvaus();
    }

    public void setVuosiluokkaKokonaisuus(Vuosiluokkakokonaisuus vuosiluokkakokonaisuus) {
        if (this.vuosiluokkakokonaisuus == null || this.vuosiluokkakokonaisuus.equals(vuosiluokkakokonaisuus)) {
            this.vuosiluokkakokonaisuus = vuosiluokkakokonaisuus;
        } else {
            throw new IllegalStateException("Vuosiluokkakokonaisuuteen kuulumista ei voi muuttaa");
        }
    }

}
