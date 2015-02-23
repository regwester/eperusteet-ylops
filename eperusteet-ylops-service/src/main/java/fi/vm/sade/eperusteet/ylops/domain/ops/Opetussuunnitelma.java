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
import fi.vm.sade.eperusteet.ylops.domain.Tyyppi;
import fi.vm.sade.eperusteet.ylops.domain.koodisto.KoodistoKoodi;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Oppiaine;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Kieli;
import fi.vm.sade.eperusteet.ylops.domain.teksti.LokalisoituTeksti;
import fi.vm.sade.eperusteet.ylops.domain.teksti.TekstiKappaleViite;
import fi.vm.sade.eperusteet.ylops.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.ylops.domain.vuosiluokkakokonaisuus.Vuosiluokkakokonaisuus;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
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

    @Getter
    @Setter
    private String perusteenDiaarinumero;

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

    @ValidHtml(whitelist = ValidHtml.WhitelistType.SIMPLIFIED)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private LokalisoituTeksti yhteystiedot;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    @Getter
    @Setter
    private Tila tila = Tila.LUONNOS;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Opetussuunnitelma pohja;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    @Getter
    @Setter
    private Tyyppi tyyppi = Tyyppi.OPS;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Getter
    @Setter
    @JoinColumn
    private TekstiKappaleViite tekstit = new TekstiKappaleViite();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @Getter
    @Setter
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Set<KoodistoKoodi> kunnat = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(joinColumns = {
        @JoinColumn(name = "opetussuunnitelma_id")}, name = "ops_oppiaine")
    private Set<OpsOppiaine> oppiaineet = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(joinColumns = {
        @JoinColumn(name = "opetussuunnitelma_id")}, name = "ops_vuosiluokkakokonaisuus")
    private Set<OpsVuosiluokkakokonaisuus> vuosiluokkakokonaisuudet = new HashSet<>();

    @ElementCollection
    @Getter
    @Setter
    private Set<String> koulut = new HashSet<>();

    @ElementCollection
    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @NotNull
    private Set<Kieli> julkaisukielet = new HashSet<>();

    public void addVuosiluokkaKokonaisuus(Vuosiluokkakokonaisuus vk) {
        vuosiluokkakokonaisuudet.add(new OpsVuosiluokkakokonaisuus(vk, true));
    }

    public void attachVuosiluokkaKokonaisuus(Vuosiluokkakokonaisuus vk) {
        vuosiluokkakokonaisuudet.add(new OpsVuosiluokkakokonaisuus(vk, false));
    }

    public boolean containsViite(TekstiKappaleViite viite) {
        return viite != null && tekstit.getId().equals(viite.getRoot().getId());
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
        }
    }

    public void addOppiaine(Oppiaine oppiaine) {
        if (oppiaine.getOppiaine() != null) {
            // Oppimäärä
            if (containsOppiaine(oppiaine.getOppiaine())) {
                oppiaine.getOppiaine().addOppimaara(oppiaine);
            } else {
                throw new IllegalArgumentException("Ei voida lisätä oppimäärää jonka oppiaine ei kuulu sisältöön");
            }
        } else {
            // Simppeli oppiaine
            oppiaineet.add(new OpsOppiaine(oppiaine, true));
        }
    }

    public void removeOppiaine(Oppiaine oppiaine) {
        List<OpsOppiaine> poistettavat = oppiaineet.stream()
                  .filter(opsOppiaine -> opsOppiaine.getOppiaine().equals(oppiaine))
                  .collect(Collectors.toList());
        for (OpsOppiaine opsOppiaine : poistettavat) {
            // TODO: Tarkista onko opsOppiaineen alla oleva oppiaine enää käytössä missään
            oppiaineet.remove(opsOppiaine);
        }
    }

    public boolean containsOppiaine(Oppiaine oppiaine) {
        if (oppiaine == null) {
            return false;
        }

        if (oppiaine.getOppiaine() != null) {
            return containsOppiaine(oppiaine.getOppiaine());
        }

        return oppiaineet.stream()
                         .anyMatch(opsOppiaine -> opsOppiaine.getOppiaine().equals(oppiaine));
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
        }
    }

    public boolean removeVuosiluokkakokonaisuus(Vuosiluokkakokonaisuus vk) {
        return vuosiluokkakokonaisuudet.remove(new OpsVuosiluokkakokonaisuus(vk, false));
    }

}
