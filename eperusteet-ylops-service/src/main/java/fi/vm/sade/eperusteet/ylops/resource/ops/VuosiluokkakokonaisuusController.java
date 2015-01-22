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

import com.wordnik.swagger.annotations.Api;
import fi.vm.sade.eperusteet.ylops.dto.ops.VuosiluokkakokonaisuusDto;
import fi.vm.sade.eperusteet.ylops.service.ops.VuosiluokkakokonaisuusService;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/opetussuunnitelmat/{opsId}/vuosiluokkakokonaisuudet")
@Api(value = "Opetussuunnitelmat")
public class VuosiluokkakokonaisuusController {

    @Autowired
    private VuosiluokkakokonaisuusService vuosiluokkakokonaisuudet;

    @RequestMapping(method = RequestMethod.POST)
    public VuosiluokkakokonaisuusDto add(@PathVariable("opsId") final Long opsId, @RequestBody VuosiluokkakokonaisuusDto dto) {
        return vuosiluokkakokonaisuudet.add(opsId, dto);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<VuosiluokkakokonaisuusDto> get(@PathVariable("opsId") final Long opsId, @PathVariable("id") final Long id) {
        return response(vuosiluokkakokonaisuudet.get(opsId, id));
    }

    @RequestMapping(method = RequestMethod.GET)
    public Set<VuosiluokkakokonaisuusDto> getAll(@PathVariable("opsId") final Long opsId) {
        throw new UnsupportedOperationException("TODO: toteuta");
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public VuosiluokkakokonaisuusDto update(@PathVariable("opsId") final Long opsId,
        @PathVariable("id") final Long id,
        @RequestBody VuosiluokkakokonaisuusDto dto) {
        dto.setId(id);
        return vuosiluokkakokonaisuudet.update(opsId, dto);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("opsId") final Long opsId, @PathVariable("id") final Long id) {
        vuosiluokkakokonaisuudet.delete(opsId, id);
    }

    private static <T> ResponseEntity<T> response(T data) {
        if (data == null) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

}