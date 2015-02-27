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

import com.mangofactory.swagger.annotations.ApiIgnore;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineenVuosiluokkaDto;
import fi.vm.sade.eperusteet.ylops.resource.util.Responses;
import fi.vm.sade.eperusteet.ylops.service.ops.OppiaineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author mikkom
 */
@RestController
@RequestMapping("/opetussuunnitelmat/{opsId}/oppiaineet/{oppiaineId}/vuosiluokkakokonaisuudet/{kokonaisuusId}/vuosiluokat")
@ApiIgnore
public class OppiaineenVuosiluokkaController {
    @Autowired
    private OppiaineService oppiaineService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<OppiaineenVuosiluokkaDto> get(
        @PathVariable("opsId") final Long opsId,
        @PathVariable("oppiaineId") final Long oppiaineId,
        @PathVariable("id") final Long id) {
        OppiaineenVuosiluokkaDto oa = oppiaineService.getVuosiluokka(opsId, oppiaineId, id);
        return Responses.ofNullable(oa);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public OppiaineenVuosiluokkaDto updateVuosiluokanSisalto(
        @PathVariable("opsId") final Long opsId,
        @PathVariable("oppiaineId") final Long oppiaineId,
        @PathVariable("id") final Long id,
        @RequestBody OppiaineenVuosiluokkaDto dto) {
        dto.setId(id);
        return oppiaineService.updateVuosiluokanSisalto(opsId, oppiaineId, dto);
    }
}
