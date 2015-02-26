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
package fi.vm.sade.eperusteet.ylops.service.ops;

import fi.vm.sade.eperusteet.ylops.service.locking.OpsCtx;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author jhyoty
 */
@Getter
@Setter
public class OpsOppiaineCtx extends OpsCtx {

    Long oppiaineId;
    Long kokonaisuusId;
    Long vuosiluokkaId;

    public OpsOppiaineCtx() {
    }

    public OpsOppiaineCtx(Long opsId, Long oppiaineId, Long kokonaisuusId, Long vuosiluokkaId) {
        super(opsId);
        this.oppiaineId = oppiaineId;
        this.kokonaisuusId = kokonaisuusId;
        this.vuosiluokkaId = vuosiluokkaId;
    }

    public boolean isValid() {
        return ( isOppiane() || isKokonaisuus() || isVuosiluokka() );
    }

    public boolean isOppiane() {
        return getOpsId()!= null && oppiaineId != null && kokonaisuusId == null && vuosiluokkaId == null;
    }

    public boolean isKokonaisuus() {
        return getOpsId()!= null && oppiaineId != null && kokonaisuusId != null && vuosiluokkaId == null;
    }

    public boolean isVuosiluokka() {
        return getOpsId()!= null && oppiaineId != null && kokonaisuusId != null && vuosiluokkaId != null;
    }

}
