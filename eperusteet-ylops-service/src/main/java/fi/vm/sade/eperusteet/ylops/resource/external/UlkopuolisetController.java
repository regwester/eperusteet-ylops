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
package fi.vm.sade.eperusteet.ylops.resource.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.mangofactory.swagger.annotations.ApiIgnore;
import fi.vm.sade.eperusteet.ylops.dto.peruste.Peruste;
import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusteInfo;
import fi.vm.sade.eperusteet.ylops.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.ylops.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.ylops.service.external.EperusteetService;
import fi.vm.sade.eperusteet.ylops.service.external.KayttajanTietoService;
import fi.vm.sade.eperusteet.ylops.service.external.KoodistoService;
import fi.vm.sade.eperusteet.ylops.service.external.OrganisaatioService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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

    @Autowired
    private EperusteetService eperusteetService;

    @Autowired
    private KayttajanTietoService kayttajanTietoService;

    @RequestMapping(value = "/kayttajatiedot/{oid:.+}", method = GET)
    @ResponseBody
    public ResponseEntity<KayttajanTietoDto> get(@PathVariable("oid") final String oid) {
        return new ResponseEntity<>(kayttajanTietoService.hae(oid), HttpStatus.OK);
    }

    @RequestMapping(value = "/julkaistutperusteet", method = GET)
    @ResponseBody
    public ResponseEntity<List<PerusteInfo>> getPerusteet() {
        return new ResponseEntity<>(eperusteetService.findPerusteet(), HttpStatus.OK);
    }

    @RequestMapping(value = "/perusopetusperusteet", method = GET)
    @ResponseBody
    public ResponseEntity<List<PerusteInfo>> getPerusopetusperusteet() {
        return new ResponseEntity<>(eperusteetService.findPerusopetuksenPerusteet(), HttpStatus.OK);
    }

    @RequestMapping(value = "/perusopetusperusteet/{id}", method = GET)
    @ResponseBody
    public Peruste getPerusopetusperuste(@PathVariable(value = "id") final Long id) {
        return eperusteetService.getPerusopetuksenPeruste(id);
    }

    @RequestMapping(value = "/lukiokoulutusperusteet", method = GET)
    @ResponseBody
    public ResponseEntity<List<PerusteInfo>> getLukiokoulutusperusteet() {
        return new ResponseEntity<>(eperusteetService.findLukiokoulutusPerusteet(), HttpStatus.OK);
    }

    @RequestMapping(value = "/lukiokoulutusperusteet/{id}", method = GET)
    @ResponseBody
    public Peruste getLukiokoulutusperuste(@PathVariable(value = "id") final Long id) {
        return eperusteetService.getLukiokoulutuksenPeruste(id);
    }

    @RequestMapping(value = "/tiedotteet", method = GET)
    @ResponseBody
    public ResponseEntity<JsonNode> getTiedotteet(@RequestParam(value = "jalkeen", required = false) final Long jalkeen) {
        return new ResponseEntity<>(eperusteetService.getTiedotteet(jalkeen), HttpStatus.OK);
    }

    @RequestMapping(value = "/organisaatiot/peruskoulutoimijat/{kuntaIdt}", method = GET)
    @ResponseBody
    public ResponseEntity<JsonNode> getPeruskoulut(@PathVariable(value = "kuntaIdt") final List<String> kuntaIdt) {
        JsonNode peruskoulut = organisaatioService.getPeruskoulutoimijat(kuntaIdt);
        return new ResponseEntity<>(peruskoulut, HttpStatus.OK);
    }

    @RequestMapping(value = "/organisaatiot/peruskoulut/oid/{oid}", method = GET)
    @ResponseBody
    public ResponseEntity<JsonNode> getPeruskoulutByOid(@PathVariable(value = "oid") final String oid) {
        JsonNode peruskoulut = organisaatioService.getPeruskoulutByOid(oid);
        return new ResponseEntity<>(peruskoulut, HttpStatus.OK);
    }

    @RequestMapping(value = "/organisaatiot/peruskoulut/{kuntaId}", method = GET)
    @ResponseBody
    public ResponseEntity<JsonNode> getPeruskoulutByKuntaId(@PathVariable(value = "kuntaId") final String kuntaId) {
        JsonNode peruskoulut = organisaatioService.getPeruskoulutByKuntaId(kuntaId);
        return new ResponseEntity<>(peruskoulut, HttpStatus.OK);
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
