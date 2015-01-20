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
package fi.vm.sade.eperusteet.ylops.service;

import fi.vm.sade.eperusteet.ylops.dto.OpetussuunnitelmaDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.TekstiKappaleViiteDto;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 *
 * @author mikkom
 */
public interface OpetussuunnitelmaService {

    @PreAuthorize("permitAll()")
    List<OpetussuunnitelmaDto> getAll();

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'LUKU')")
    OpetussuunnitelmaDto getOpetussuunnitelma(@P("opsId") Long id);

    @PreAuthorize("hasPermission(null, 'opetussuunnitelma', 'LUONTI')")
    OpetussuunnitelmaDto addOpetussuunnitelma(OpetussuunnitelmaDto opetussuunnitelmaDto);

    @PreAuthorize("hasPermission(#ops.id, 'opetussuunnitelma', 'MUOKKAUS')")
    OpetussuunnitelmaDto updateOpetussuunnitelma(@P("ops") OpetussuunnitelmaDto opetussuunnitelmaDto);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'POISTO')")
    void removeOpetussuunnitelma(@P("opsId") Long id);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'LUKU')")
    TekstiKappaleViiteDto.Puu getTekstit(@P("opsId") final Long opsId);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'MUOKKAUS')")
    TekstiKappaleViiteDto.Matala addTekstiKappale(@P("opsId") final Long opsId, TekstiKappaleViiteDto.Matala viite);

    @PreAuthorize("hasPermission(#opsId, 'opetussuunnitelma', 'MUOKKAUS')")
    TekstiKappaleViiteDto.Matala addTekstiKappaleLapsi(@P("opsId") final Long opsId, final Long parentId,
                                                       TekstiKappaleViiteDto.Matala viite);
}
