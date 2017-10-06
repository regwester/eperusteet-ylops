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
package fi.vm.sade.eperusteet.ylops.service.ops.impl;

import fi.vm.sade.eperusteet.ylops.domain.ops.Opetussuunnitelma;
import fi.vm.sade.eperusteet.ylops.repository.ops.OpetussuunnitelmaRepository;
import fi.vm.sade.eperusteet.ylops.repository.teksti.TekstikappaleviiteRepository;
import fi.vm.sade.eperusteet.ylops.service.exception.LockingException;
import fi.vm.sade.eperusteet.ylops.service.locking.AbstractLockService;
import fi.vm.sade.eperusteet.ylops.service.locking.OpsCtx;
import fi.vm.sade.eperusteet.ylops.service.ops.OpsSisaltoLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author jhyoty
 */
@Service
public class OpsSisaltoLockServiceImpl extends AbstractLockService<OpsCtx> implements OpsSisaltoLockService {

    @Autowired
    private OpetussuunnitelmaRepository suunnitelmat;

    @Autowired
    private TekstikappaleviiteRepository viitteet;

    @Override
    protected Long getLockId(OpsCtx ctx) {
        Opetussuunnitelma ops = suunnitelmat.findOne(ctx.getOpsId());
        return ops == null ? null : ops.getTekstit().getId();
    }

    @Override
    protected int latestRevision(OpsCtx ctx) {
        return viitteet.getLatestRevisionId(getLockId(ctx));
    }

    @Override
    protected Long validateCtx(OpsCtx ctx, boolean readOnly) {
        Long id = getLockId(ctx);
        if (id != null) {
            return id;
        }
        throw new LockingException("virheellinen lukitus");
    }

}
