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

import fi.vm.sade.eperusteet.ylops.domain.AbstractAuditedEntity;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Oppiaine;
import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;

/**
 * User: tommiratamaa
 * Date: 10.12.2015
 * Time: 13.07
 */
@Getter
@Entity
@Audited
@Table(name = "lukio_oppiaine_jarjestys", schema = "public")
public class LukioOppiaineJarjestys extends AbstractAuditedEntity {
    @EmbeddedId
    private LukioOppiaineId id;

    @MapsId("opetusuunnitelmaId")
    @ManyToOne(fetch = FetchType.LAZY)
    private Opetussuunnitelma opetussuunnitelma;

    @MapsId("oppiaineId")
    @ManyToOne(fetch = FetchType.LAZY)
    private Oppiaine oppiaine;

    @Setter
    @Column(name = "jarjestys")
    private Integer jarjestys;

    protected LukioOppiaineJarjestys() {
    }

    public LukioOppiaineJarjestys(Opetussuunnitelma opetussuunnitelma, Oppiaine oppiaine) {
        this.id = new LukioOppiaineId(opetussuunnitelma.getId(), oppiaine.getId());
        this.opetussuunnitelma = opetussuunnitelma;
        this.oppiaine = oppiaine;
    }

    public LukioOppiaineJarjestys(Opetussuunnitelma opetussuunnitelma, Oppiaine oppiaine, Integer jarjestys) {
        this(opetussuunnitelma, oppiaine);
        this.jarjestys = jarjestys;
    }
}
