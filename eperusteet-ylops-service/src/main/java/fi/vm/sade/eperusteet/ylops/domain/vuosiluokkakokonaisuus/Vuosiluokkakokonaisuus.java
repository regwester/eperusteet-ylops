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

import fi.vm.sade.eperusteet.ylops.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Tekstiosa;
import fi.vm.sade.eperusteet.ylops.domain.Vuosiluokkakokonaisuusviite;
import fi.vm.sade.eperusteet.ylops.domain.teksti.LokalisoituTeksti;
import fi.vm.sade.eperusteet.ylops.domain.validation.ValidHtml;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 *
 * @author jhyoty
 */
@Entity
@Table(name = "vlkokonaisuus")
@Audited
public class Vuosiluokkakokonaisuus extends AbstractAuditedReferenceableEntity {

    @Getter
    @Setter
    @ManyToOne
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Vuosiluokkakokonaisuusviite tunniste;

    @Getter
    @Setter
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    private LokalisoituTeksti nimi;

    @Getter
    @Setter
    @ManyToOne(cascade = CascadeType.ALL, optional = true)
    private Tekstiosa siirtymaEdellisesta;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, optional = true, orphanRemoval = true)
    private Tekstiosa tehtava;

    @Getter
    @Setter
    @ManyToOne(cascade = CascadeType.ALL, optional = true)
    private Tekstiosa siirtymaSeuraavaan;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, optional = true, orphanRemoval = true)
    private Tekstiosa laajaalainenOsaaminen;

    @OneToMany(mappedBy = "vuosiluokkakokonaisuus", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Laajaalainenosaaminen> laajaalaisetosaamiset = new HashSet<>();

    public Set<Laajaalainenosaaminen> getLaajaalaisetOsaamiset() {
        return new HashSet<>(laajaalaisetosaamiset);
    }

    public void setLaajaalaisetOsaamiset(Set<Laajaalainenosaaminen> osaamiset) {

        if (osaamiset == null) {
            this.laajaalaisetosaamiset.clear();
            return;
        }

        this.laajaalaisetosaamiset.retainAll(osaamiset);
        this.laajaalaisetosaamiset.addAll(osaamiset);
        for (Laajaalainenosaaminen v : osaamiset) {
            v.setVuosiluokkaKokonaisuus(this);
        }
    }

    //hiberate javaassist proxy "workaround"
    //ilman equals-metodia objectX.equals(proxy-objectX) on aina false
    @Override
    public boolean equals(Object other) {
        return this == other;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

}
