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
package fi.vm.sade.eperusteet.ylops.domain.oppiaine;

import fi.vm.sade.eperusteet.ylops.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.ylops.domain.Vuosiluokka;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;

/**
 *
 * @author jhyoty
 */
@Entity
@Audited
@Table(name = "oppiaineenvuosiluokka", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"kokonaisuus_id", "vuosiluokka"})})
public class Oppiaineenvuosiluokka extends AbstractAuditedReferenceableEntity {

    @Getter
    @Setter(AccessLevel.PACKAGE)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @NotNull
    private Oppiaineenvuosiluokkakokonaisuus kokonaisuus;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private Vuosiluokka vuosiluokka;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinTable
    @OrderColumn
    @BatchSize(size = 25)
    private List<Opetuksentavoite> tavoitteet = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinTable
    @OrderColumn
    @BatchSize(size = 25)
    private List<Keskeinensisaltoalue> sisaltoalueet = new ArrayList<>();

    public Oppiaineenvuosiluokka() {
    }

    public Oppiaineenvuosiluokka(Vuosiluokka vuosiluokka) {
        this.vuosiluokka = vuosiluokka;
    }

    public Optional<Keskeinensisaltoalue> getSisaltoalue(UUID tunniste) {
        return this.sisaltoalueet.stream()
            .filter(k -> Objects.equals(k.getTunniste(), tunniste))
            .findAny();
    }

    public Optional<Opetuksentavoite> getTavoite(UUID tunniste) {
        return this.tavoitteet.stream()
            .filter(k -> Objects.equals(k.getTunniste(), tunniste))
            .findAny();
    }

    public List<Opetuksentavoite> getTavoitteet() {
        return new ArrayList<>(tavoitteet);
    }

    public void setTavoitteet(List<Opetuksentavoite> tavoitteet) {
        this.tavoitteet.clear();
        if (tavoitteet != null) {
            this.tavoitteet.addAll(tavoitteet);
        }
    }

    public List<Keskeinensisaltoalue> getSisaltoalueet() {
        return new ArrayList<>(sisaltoalueet);
    }

    public void setSisaltoalueet(Collection<Keskeinensisaltoalue> sisaltoalueet) {
        this.sisaltoalueet.clear();
        if (sisaltoalueet != null) {
            this.sisaltoalueet.addAll(sisaltoalueet);
        }
    }

    static Oppiaineenvuosiluokka copyOf(final Oppiaineenvuosiluokka other, final Map<Long, Opetuksenkohdealue> kohdealueet) {
        Oppiaineenvuosiluokka ovl = new Oppiaineenvuosiluokka();
        ovl.setVuosiluokka(other.getVuosiluokka());

        Map<Long, Keskeinensisaltoalue> sisaltoalueet = other.getSisaltoalueet().stream()
            .collect(Collectors.toMap(s -> s.getId(), s -> Keskeinensisaltoalue.copyOf(s), (u, v) -> u, LinkedHashMap::new));
        ovl.setSisaltoalueet(sisaltoalueet.values());
        ovl.setTavoitteet(
            other.tavoitteet.stream()
            .map(t -> Opetuksentavoite.copyOf(t, kohdealueet, sisaltoalueet))
            .collect(Collectors.toList()));
        return ovl;
    }

}
