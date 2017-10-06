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

import fi.vm.sade.eperusteet.ylops.domain.AbstractAuditedReferenceableEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

/**
 * @author mikkom
 */
@Entity
@Table(name = "kommentti")
public class Kommentti extends AbstractAuditedReferenceableEntity {
    @Getter
    @Setter
    private Boolean poistettu;

    @Getter
    @Setter
    private String nimi;

    @Getter
    @Setter
    @Column(length = 1024)
    @Size(max = 1024, message = "Kommentin maksimipituus on {max} merkkiä")
    private String sisalto;

    @Getter
    @Setter
    private Long ylinId;

    @Getter
    @Setter
    private Long parentId;

    @Getter
    @Setter
    private Long opetussuunnitelmaId;

    @Getter
    @Setter
    private Long tekstiKappaleViiteId;

    @Getter
    @Setter
    private Long oppiaineId;

    @Getter
    @Setter
    private Long vlkId;

    @Getter
    @Setter
    private Long vlId;
}
