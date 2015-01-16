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
package fi.vm.sade.eperusteet.ylops.domain.ops;

import fi.vm.sade.eperusteet.ylops.domain.AbstractAuditedEntity;
import fi.vm.sade.eperusteet.ylops.domain.ReferenceableEntity;
import fi.vm.sade.eperusteet.ylops.domain.Tila;
import fi.vm.sade.eperusteet.ylops.domain.koodisto.KoodistoKoodi;
import fi.vm.sade.eperusteet.ylops.domain.teksti.LokalisoituTeksti;
import fi.vm.sade.eperusteet.ylops.domain.teksti.TekstiKappaleViite;
import fi.vm.sade.eperusteet.ylops.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.ylops.dto.EntityReference;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 *
 * @author mikkom
 */
@Entity
@Audited
@Table(name = "opetussuunnitelma")
public class Opetussuunnitelma extends AbstractAuditedEntity
        implements Serializable, ReferenceableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private LokalisoituTeksti nimi;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private LokalisoituTeksti kuvaus;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    @Getter
    @Setter
    private Tila tila = Tila.LUONNOS;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Getter
    @Setter
    @JoinColumn
    private TekstiKappaleViite tekstit = new TekstiKappaleViite();

    @ManyToMany(cascade = CascadeType.PERSIST)
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Set<KoodistoKoodi> kunnat = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "opetussuunnitelma", fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<OpsOppiaine> oppiaineet = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "opetussuunnitelma", fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<OpsVuosiluokkakokonaisuus> vuosiluokkakokonaisuudet = new HashSet<>();

    @ElementCollection
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Set<String> koulut = new HashSet<>();

    public boolean containsViite(TekstiKappaleViite viite) {
        return viite != null && tekstit.getId().equals(viite.getRoot().getId());
    }

    @Override
    public EntityReference getReference() {
        return new EntityReference(getId());
    }

    public Set<OpsOppiaine> getOppiaineet() {
        return new HashSet<>(oppiaineet);
    }

    public void setOppiaineet(Set<OpsOppiaine> oppiaineet) {
        if ( oppiaineet == null ) {
            this.oppiaineet.clear();
        } else {
            this.oppiaineet.addAll(oppiaineet);
            this.oppiaineet.retainAll(oppiaineet);
            for ( OpsOppiaine o : oppiaineet ) {
                o.setOpetussuunnitelma(this);
            }
        }
    }

    public Set<OpsVuosiluokkakokonaisuus> getVuosiluokkakokonaisuudet() {
        return new HashSet<>(vuosiluokkakokonaisuudet);
    }

    public void setVuosiluokkakokonaisuudet(Set<OpsVuosiluokkakokonaisuus> vuosiluokkakokonaisuudet) {
        if (vuosiluokkakokonaisuudet == null) {
            this.vuosiluokkakokonaisuudet.clear();
        } else {
            this.vuosiluokkakokonaisuudet.addAll(vuosiluokkakokonaisuudet);
            this.vuosiluokkakokonaisuudet.retainAll(vuosiluokkakokonaisuudet);
            for (OpsVuosiluokkakokonaisuus v : vuosiluokkakokonaisuudet) {
                v.setOpetussuunnitelma(this);
            }
        }
    }
}
