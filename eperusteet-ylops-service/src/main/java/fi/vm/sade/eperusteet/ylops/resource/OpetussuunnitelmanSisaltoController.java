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
package fi.vm.sade.eperusteet.ylops.resource;

import com.wordnik.swagger.annotations.Api;
import fi.vm.sade.eperusteet.ylops.dto.EntityReference;
import fi.vm.sade.eperusteet.ylops.dto.teksti.TekstiKappaleViiteDto;
import fi.vm.sade.eperusteet.ylops.service.OpetussuunnitelmaService;
import fi.vm.sade.eperusteet.ylops.service.teksti.TekstiKappaleViiteService;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author mikkom
 */
@RestController
@RequestMapping("/opetussuunnitelmat/{opsId}")
@Api(value = "Opetussuunnitelman sisältö")
public class OpetussuunnitelmanSisaltoController {
    @Autowired
    private OpetussuunnitelmaService opetussuunnitelmaService;

    @Autowired
    private TekstiKappaleViiteService tekstiKappaleViiteService;

    @RequestMapping(value = "/tekstit", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<TekstiKappaleViiteDto.Matala> addTekstiKappale(
            @PathVariable("opsId") final Long opsId,
            // TODO: Lisätäänkö myös addTekstiKappaleViite PUT-metodi jossa viiteDto on pakollinen kenttä?
            @RequestBody(required = false) TekstiKappaleViiteDto.Matala tekstiKappaleViiteDto) {
        return new ResponseEntity<>(
                opetussuunnitelmaService.addTekstiKappale(opsId, tekstiKappaleViiteDto), HttpStatus.OK);
    }

    @RequestMapping(value = "/tekstit/{viiteId}/lapsi", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<TekstiKappaleViiteDto.Matala> addTekstiKappaleLapsi(
            @PathVariable("opsId") final Long id,
            @PathVariable("viiteId") final Long viiteId) {
        return new ResponseEntity<>(
                opetussuunnitelmaService.addTekstiKappaleLapsi(id, viiteId, null), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/tekstit/{parentId}/lapsi/{childId}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<TekstiKappaleViiteDto.Matala> addTekstiKappaleLapsi(
            @PathVariable("opsId") final Long opsId,
            @PathVariable("parentId") final Long parentId,
            @PathVariable("childId") final Long childId) {
        TekstiKappaleViiteDto.Matala viite = new TekstiKappaleViiteDto.Matala();
        viite.setTekstiKappaleRef(new EntityReference(childId));
        return new ResponseEntity<>(
                opetussuunnitelmaService.addTekstiKappaleLapsi(opsId, parentId, viite), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/tekstit", method = RequestMethod.GET)
    public ResponseEntity<TekstiKappaleViiteDto.Puu> getTekstit(
            @PathVariable("opsId") final Long opsId) {
        TekstiKappaleViiteDto.Puu dto = opetussuunnitelmaService.getTekstit(opsId);
        if (dto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/tekstit/{viiteId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTekstiKappaleViite(
            @PathVariable("opsId") final Long opsId,
            @PathVariable("viiteId") final Long viiteId) {
        tekstiKappaleViiteService.removeTekstiKappaleViite(opsId, viiteId);
    }

    @RequestMapping(value = "/tekstit/{viiteId}", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateTekstiKappaleViite(
            @PathVariable("opsId") final Long opsId,
            @PathVariable("viiteId") final Long viiteId,
            @RequestBody final TekstiKappaleViiteDto tekstiKappaleViiteDto) {
        throw new NotImplementedException();
    }

    @RequestMapping(value = "/tekstit/{viiteId}/muokattavakopio", method = RequestMethod.POST)
    public TekstiKappaleViiteDto kloonaaTekstiKappale(
            @PathVariable("opsId") final Long opsId,
            @PathVariable("viiteId") final Long viiteId) {
        throw new NotImplementedException();
    }
}