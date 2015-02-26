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
package fi.vm.sade.eperusteet.ylops.resource.ops;

import fi.vm.sade.eperusteet.ylops.resource.util.AbstractLockController;
import fi.vm.sade.eperusteet.ylops.service.locking.LockService;
import fi.vm.sade.eperusteet.ylops.service.ops.OppiaineService;
import fi.vm.sade.eperusteet.ylops.service.ops.OpsOppiaineCtx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author jhyoty
 */
@RestController
@RequestMapping(value = {
    "/opetussuunnitelmat/{opsId}/oppiaineet/{oppiaineId}/lukko",
    "/opetussuunnitelmat/{opsId}/oppiaineet/{oppiaineId}/vuosiluokkakokonaisuudet/{kokonaisuusId}/lukko",
    "/opetussuunnitelmat/{opsId}/oppiaineet/{oppiaineId}/vuosiluokkakokonaisuudet/{kokonaisuusId}/vuosiluokat/{vuosiluokkaId}/lukko"
})
public class OppiaineLockController extends AbstractLockController<OpsOppiaineCtx>{
    @Autowired
    private OppiaineService service;

    @Override
    protected LockService<OpsOppiaineCtx> service() {
        return service;
    }

}
