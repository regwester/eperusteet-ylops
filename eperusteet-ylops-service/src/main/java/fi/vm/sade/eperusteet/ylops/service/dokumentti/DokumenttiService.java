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

package fi.vm.sade.eperusteet.ylops.service.dokumentti;

import fi.vm.sade.eperusteet.ylops.domain.teksti.Kieli;
import fi.vm.sade.eperusteet.ylops.dto.dokumentti.DokumenttiDto;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 *
 * @author iSaul
 */
public interface DokumenttiService {

    @PreAuthorize("isAuthenticated()")
    DokumenttiDto getDto(@P("id") final long opsId, Kieli kieli);

    @PreAuthorize("isAuthenticated()")
    DokumenttiDto createDtoFor(@P("id") final long id, Kieli kieli);

    @PreAuthorize("isAuthenticated()")
    void setStarted(@P("dto") DokumenttiDto dto);

    @PreAuthorize("isAuthenticated()")
    @Async(value = "docTaskExecutor")
    void generateWithDto(@P("dto") DokumenttiDto dto);

    @PreAuthorize("isAuthenticated()")
    DokumenttiDto getDto(@P("id") final long id);

    @PreAuthorize("isAuthenticated()")
    byte[] get(Long id);

    @PreAuthorize("isAuthenticated()")
    public DokumenttiDto query(Long id);
}
