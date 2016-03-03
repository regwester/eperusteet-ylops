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

package fi.vm.sade.eperusteet.ylops.service.ops.impl;

import fi.vm.sade.eperusteet.ylops.repository.ops.OpetussuunnitelmaRepository;
import fi.vm.sade.eperusteet.ylops.repository.version.JpaWithVersioningRepository;
import fi.vm.sade.eperusteet.ylops.service.exception.LockingException;
import fi.vm.sade.eperusteet.ylops.service.locking.AbstractLockService;
import fi.vm.sade.eperusteet.ylops.service.ops.lukio.LukioLockCtx;
import fi.vm.sade.eperusteet.ylops.service.ops.lukio.LukioLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * User: tommiratamaa
 * Date: 16.12.2015
 * Time: 14.05
 */
@Service
public class LukioLockServiceImpl extends AbstractLockService<LukioLockCtx>
        implements LukioLockService {
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private OpetussuunnitelmaRepository opetussuunnitelmaRepository;

    @Override
    protected Long getLockId(LukioLockCtx ctx) {
        if (ctx.getLukittavaOsa().isFromOps()) {
            return ctx.getLukittavaOsa().getFromOps().get()
                .apply(opetussuunnitelmaRepository.findOne(ctx.getOpsId())).getId();
        }
        Object val = getRepo(ctx).findOne(ctx.getId());
        return val == null ? null : ctx.getId();
    }

    @Override
    protected Long validateCtx(LukioLockCtx ctx, boolean readOnly) {
        Long id = getLockId(ctx);
        if (id != null) {
            return id;
        }
        throw new LockingException("virheellinen lukitus");
    }

    protected JpaWithVersioningRepository getRepo(LukioLockCtx ctx) {
        return applicationContext.getBean(ctx.getLukittavaOsa().getRepository());
    }

    @Override
    protected int latestRevision(LukioLockCtx ctx) {
        return getRepo(ctx).getLatestRevisionId(getLockId(ctx));
    }
}
