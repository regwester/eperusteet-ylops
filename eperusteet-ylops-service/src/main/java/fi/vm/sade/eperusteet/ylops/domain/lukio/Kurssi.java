/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.ylops.domain.lukio;

import fi.vm.sade.eperusteet.ylops.domain.AbstractAuditedReferenceableEntity;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.domain.teksti.LokalisoituTeksti;
import fi.vm.sade.eperusteet.ylops.domain.validation.ValidHtml;
import fi.vm.sade.eperusteet.ylops.domain.validation.ValidHtml.WhitelistType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * User: tommiratamaa
 * Date: 17.11.2015
 * Time: 14.02
 */
@Entity
@Table(name = "kurssi", schema = "public")
@Audited
@Inheritance(strategy = InheritanceType.JOINED)
public class Kurssi extends AbstractAuditedReferenceableEntity {

    @Getter
    @Setter
    @Column(nullable = false, unique = true, updatable = false)
    private UUID tunniste;

    @Getter
    @Setter
    @NotNull
    @ValidHtml(whitelist = WhitelistType.MINIMAL)
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "nimi_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    protected LokalisoituTeksti nimi;

    @Getter
    @Setter
    @ValidHtml
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "kuvaus_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    protected LokalisoituTeksti kuvaus;

    @Getter
    @Setter
    @Column(name = "koodi_uri")
    protected String koodiUri;

    @Getter
    @Setter
    @Column(name = "koodi_arvo")
    protected String koodiArvo;

    @Getter
    @Setter
    @JoinColumn(name = "opetussuunnitelma_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Opetussuunnitelma opetussuunnitelma;

    public Kurssi() {
    }

    public Kurssi(Opetussuunnitelma opetussuunnitelma, UUID tunniste) {
        this.opetussuunnitelma = opetussuunnitelma;
        this.tunniste = tunniste;
    }
}
