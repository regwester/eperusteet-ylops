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

package fi.vm.sade.eperusteet.ylops.service.dokumentti.impl;

import fi.vm.sade.eperusteet.ylops.domain.teksti.Kieli;
import fi.vm.sade.eperusteet.ylops.dto.DokumenttiDto;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.DokumenttiService;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;

/**
 *
 * @author isaul
 */
@Service
public class DokumenttiServiceImpl implements DokumenttiService {

    @Override
    public void setStarted(@P("dto") DokumenttiDto dto) {

    }

    @Override
    public void generateWithDto(@P("dto") DokumenttiDto dto) {

    }

    @Override
    public DokumenttiDto createDtoFor(@P("id") long id, Kieli kieli) {
        return null;
    }

    @Override
    public byte[] get(Long id) {
        return new byte[0];
    }
}
