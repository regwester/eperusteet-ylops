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

import fi.vm.sade.eperusteet.ylops.domain.AbstractAuditedEntity;
import fi.vm.sade.eperusteet.ylops.domain.Mergeable;
import fi.vm.sade.eperusteet.ylops.domain.OpetussuunnitelmanTila;
import fi.vm.sade.eperusteet.ylops.domain.ReferenceableEntity;
import fi.vm.sade.eperusteet.ylops.domain.WithOpetussuunnitelmanTila;
import fi.vm.sade.eperusteet.ylops.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.ylops.dto.EntityReference;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 *
 * @author mikkom
 */
@Entity
@Table(name = "tekstikappale")
@Audited
public class TekstiKappale extends AbstractAuditedEntity
        implements Serializable, Mergeable<TekstiKappale>, ReferenceableEntity,
        WithOpetussuunnitelmanTila {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Getter
    @Setter
    private Long id;

    @ValidHtml(whitelist = ValidHtml.WhitelistType.MINIMAL)
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    private LokalisoituTeksti nimi;

    @ValidHtml
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Getter
    @Setter
    private LokalisoituTeksti teksti;

    @Enumerated(value = EnumType.STRING)
    @NotNull
    @Getter
    private OpetussuunnitelmanTila tila = OpetussuunnitelmanTila.LUONNOS;

    public TekstiKappale() { }

    public TekstiKappale(TekstiKappale other) {
        copyState(other);
    }

    @Override
    public void mergeState(TekstiKappale updated) {
        copyState(updated);
    }

    @Override
    public EntityReference getReference() {
        return new EntityReference(getId());
    }

    @Override
    public void setTila(OpetussuunnitelmanTila tila) {
        if (this.tila == null || this.tila == OpetussuunnitelmanTila.LUONNOS) {
            this.tila = tila;
        }
    }

    public TekstiKappale copy() { return new TekstiKappale(this); }

    private void copyState(TekstiKappale other) {
        this.setNimi(other.getNimi());
        this.setTeksti(other.getTeksti());
    }
}
