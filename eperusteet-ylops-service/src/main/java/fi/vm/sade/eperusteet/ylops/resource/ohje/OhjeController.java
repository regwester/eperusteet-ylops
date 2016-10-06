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
package fi.vm.sade.eperusteet.ylops.resource.ohje;

import fi.vm.sade.eperusteet.ylops.dto.ohje.OhjeDto;
import fi.vm.sade.eperusteet.ylops.service.ohje.OhjeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author mikkom
 */
@RestController
@RequestMapping("/ohjeet")
@ApiIgnore
public class OhjeController {

    @Autowired
    private OhjeService service;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<OhjeDto> addOhje(@RequestBody OhjeDto ohjeDto) {
        return new ResponseEntity<>(service.addOhje(ohjeDto), HttpStatus.OK);
    }

    @RequestMapping(value = "/tekstikappale/{uuid}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<OhjeDto>> getTekstiKappaleOhje(@PathVariable("uuid") final UUID uuid) {
        List<OhjeDto> ohjeDtos = service.getTekstiKappaleOhjeet(uuid);
        if (ohjeDtos == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(ohjeDtos, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<OhjeDto> getOhje(@PathVariable("id") final Long id) {
        return new ResponseEntity<>(service.getOhje(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<OhjeDto> updateOhje(
            @PathVariable("id") final Long id,
            @RequestBody OhjeDto ohjeDto) {
        ohjeDto.setId(id);
        return new ResponseEntity<>(service.updateOhje(ohjeDto), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteOhje(@PathVariable("id") final Long id) {
        service.removeOhje(id);
    }
}
