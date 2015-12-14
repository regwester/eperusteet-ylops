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

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * User: tommiratamaa
 * Date: 10.12.2015
 * Time: 13.08
 */
@Getter
@Setter
@Embeddable
public class LukioOppiaineId implements Serializable {
    @Column(name = "oppiaine_id")
    private Long oppiaineId;
    @Column(name = "opetussuunnielma_id")
    private Long opetusuunnitelmaId;

    protected LukioOppiaineId() {
    }

    public LukioOppiaineId(Long opetusuunnitelmaId, Long oppiaineId) {
        this.opetusuunnitelmaId = opetusuunnitelmaId;
        this.oppiaineId = oppiaineId;
    }
}
