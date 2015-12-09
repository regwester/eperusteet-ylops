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

import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Oppiaine;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

/**
 * NOTE: just a view (do not attempt to modify or save)
 *
 * User: tommiratamaa
 * Date: 9.12.2015
 * Time: 16.54
 */
@Entity
@Getter
@Setter
@Table(name = "ops_oppiaine_parent", schema = "public")
public class OpsOppiaineParentView implements Serializable {
    @EmbeddedId
    private OpsOppiaineId opsOppiaine;
    @MapsId("opetussuunnitelmaId")
    @ManyToOne(fetch = FetchType.LAZY)
    private Opetussuunnitelma opetussuunnitelma;
    @MapsId("oppiaineId")
    @ManyToOne(fetch = FetchType.LAZY)
    private Oppiaine oppiaine;
    @Column(name = "oppiaine_oma", nullable = false)
    private boolean oma;
    @Column(name = "oppiaine_tunniste")
    private UUID tunniste;
    // Ensimmäinen pohja, jossa ko. oppiaine on määritetty OpsOppiaineena (ei välttämättä suora parent)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ensimmaisen_pohjan_oppiaine_id")
    private Oppiaine pohjanOppiaine;
}
