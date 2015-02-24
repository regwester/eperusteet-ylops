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
package fi.vm.sade.eperusteet.ylops.service.teksti;

import fi.vm.sade.eperusteet.ylops.dto.teksti.TekstiKappaleViiteDto;
import fi.vm.sade.eperusteet.ylops.service.locking.LockService;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * @author mikkom
 */
public interface TekstiKappaleViiteService extends LockService<OpsTekstikappaleCtx> {
    @PreAuthorize("permitAll()")
    TekstiKappaleViiteDto.Matala getTekstiKappaleViite(@P("opsId") Long opsId, Long viiteId);

    @PreAuthorize("permitAll()")
    TekstiKappaleViiteDto.Matala addTekstiKappaleViite(@P("opsId") Long opsId, Long viiteId,
                                                       TekstiKappaleViiteDto.Matala viiteDto);

    @PreAuthorize("permitAll()")
    TekstiKappaleViiteDto updateTekstiKappaleViite(@P("opsId") Long opsId, Long rootViiteId, TekstiKappaleViiteDto uusi);

    @PreAuthorize("permitAll()")
    void reorderSubTree(@P("opsId") Long opsId, Long rootViiteId, TekstiKappaleViiteDto.Puu uusi);

    @PreAuthorize("permitAll()")
    void removeTekstiKappaleViite(@P("opsId") Long opsId, Long viiteId);

    @PreAuthorize("permitAll()")
    TekstiKappaleViiteDto.Puu kloonaaTekstiKappale(@P("opsId") Long opsId, Long viiteId);
}
