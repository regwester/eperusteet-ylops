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

import fi.vm.sade.eperusteet.ylops.domain.vuosiluokkakokonaisuus.Vuosiluokkakokonaisuus;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Opetussuunnitelman oppiaine
 *
 * @author jhyoty
 */
@Entity
@Audited
@Table(name = "ops_vuosiluokkakokonaisuus", indexes = {
    @Index(unique = true, columnList = "opetussuunnitelma_id,vuosiluokkakokonaisuus_id")})
public class OpsVuosiluokkakokonaisuus extends AbstractOpsViite {


    @Getter
    @Setter
    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @NotNull
    private Vuosiluokkakokonaisuus vuosiluokkakokonaisuus;


    protected OpsVuosiluokkakokonaisuus() {
        //JPA
    }

    public OpsVuosiluokkakokonaisuus(Opetussuunnitelma opetussuunnitelma, Vuosiluokkakokonaisuus vuosiluokkakokonaisuus, boolean oma) {
        super(opetussuunnitelma, oma);
        this.vuosiluokkakokonaisuus = vuosiluokkakokonaisuus;
    }


}
