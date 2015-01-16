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

import com.fasterxml.jackson.databind.JsonNode;
import com.mangofactory.swagger.annotations.ApiIgnore;
import fi.vm.sade.eperusteet.ylops.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.ylops.service.external.KoodistoService;
import fi.vm.sade.eperusteet.ylops.service.external.OrganisaatioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 *
 * @author mikkom
 */
@RestController
@RequestMapping("/ulkopuoliset")
@ApiIgnore
public class UlkopuolisetController {

    @Autowired
    private OrganisaatioService organisaatioService;

    @Autowired
    private KoodistoService koodistoService;

    @RequestMapping(value = "/organisaatioryhmat", method = GET)
    @ResponseBody
    public ResponseEntity<JsonNode> getOrganisaatioRyhmat() {
        JsonNode ryhmat = organisaatioService.getRyhmat();
        return new ResponseEntity<>(ryhmat, HttpStatus.OK);
    }

    @RequestMapping(value = "/organisaatioryhmat/{oid}", method = GET)
    @ResponseBody
    public ResponseEntity<JsonNode> getOrganisaatioRyhma(@PathVariable(value = "oid") final String oid) {
        JsonNode ryhma = organisaatioService.getRyhma(oid);
        return new ResponseEntity<>(ryhma, HttpStatus.OK);
    }

    @RequestMapping(value = "/organisaatiot/peruskoulut/{kuntaId}", method = GET)
    @ResponseBody
    public ResponseEntity<JsonNode> getPeruskoulut(@PathVariable(value = "kuntaId") final String kuntaId) {
        JsonNode ryhma = organisaatioService.getRyhma(kuntaId);
        return new ResponseEntity<>(ryhma, HttpStatus.OK);
    }

    @RequestMapping(value = "/koodisto/{koodisto}", method = GET)
    @ResponseBody
    public ResponseEntity<List<KoodistoKoodiDto>> kaikki(
            @PathVariable("koodisto") final String koodisto,
            @RequestParam(value = "haku", required = false) final String haku) {
        return new ResponseEntity<>(haku == null || haku.isEmpty()
                                    ? koodistoService.getAll(koodisto)
                                    : koodistoService.filterBy(koodisto, haku), HttpStatus.OK);
    }

    @RequestMapping(value = "/koodisto/{koodisto}/{koodi}", method = GET)
    @ResponseBody
    public ResponseEntity<KoodistoKoodiDto> yksi(
            @PathVariable("koodisto") final String koodisto,
            @PathVariable("koodi") final String koodi) {
        return new ResponseEntity<>(koodistoService.get(koodisto, koodi), HttpStatus.OK);
    }

    @RequestMapping(value = "/koodisto/relaatio/sisaltyy-alakoodit/{koodi}", method = GET)
    @ResponseBody
    public ResponseEntity<List<KoodistoKoodiDto>> alarelaatio(
            @PathVariable("koodi") final String koodi) {
        return new ResponseEntity<>(koodistoService.getAlarelaatio(koodi), HttpStatus.OK);
    }

    @RequestMapping(value = "/koodisto/relaatio/sisaltyy-ylakoodit/{koodi}", method = GET)
    @ResponseBody
    public ResponseEntity<List<KoodistoKoodiDto>> ylarelaatio(
            @PathVariable("koodi") final String koodi) {
        return new ResponseEntity<>(koodistoService.getYlarelaatio(koodi), HttpStatus.OK);
    }
}
