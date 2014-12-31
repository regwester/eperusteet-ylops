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

import fi.vm.sade.eperusteet.ylops.domain.teksti.TekstiKappaleViite;
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

    @PreAuthorize("permitAll()")
    OpetussuunnitelmaDto getOpetussuunnitelma(@P("id") Long id);

    @PreAuthorize("permitAll()")
    OpetussuunnitelmaDto addOpetussuunnitelma(OpetussuunnitelmaDto opetussuunnitelmaDto);

    @PreAuthorize("permitAll()")
    OpetussuunnitelmaDto updateOpetussuunnitelma(OpetussuunnitelmaDto opetussuunnitelmaDto);

    @PreAuthorize("permitAll()")
    void removeOpetussuunnitelma(@P("id") Long id);

    @PreAuthorize("permitAll()")
    TekstiKappaleViiteDto.Puu getTekstit(@P("opsId") final Long opsId);

    @PreAuthorize("permitAll()")
    TekstiKappaleViiteDto.Matala addTekstiKappale(@P("opsId") final Long opsId, TekstiKappaleViiteDto.Matala viite);

    @PreAuthorize("permitAll()")
    TekstiKappaleViiteDto.Matala addTekstiKappaleLapsi(@P("opsId") final Long opsId, final Long parentId,
                                                       TekstiKappaleViiteDto.Matala childId);
}
