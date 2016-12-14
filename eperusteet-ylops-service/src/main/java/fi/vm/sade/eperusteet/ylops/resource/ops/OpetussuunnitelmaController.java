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

import com.codahale.metrics.annotation.Timed;
import fi.vm.sade.eperusteet.ylops.domain.Tila;
import fi.vm.sade.eperusteet.ylops.domain.Tyyppi;
import fi.vm.sade.eperusteet.ylops.dto.JarjestysDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.*;
import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusteLaajaalainenosaaminenDto;
import fi.vm.sade.eperusteet.ylops.service.ops.OpetussuunnitelmaService;
import fi.vm.sade.eperusteet.ylops.service.security.PermissionManager;
import fi.vm.sade.eperusteet.ylops.service.util.Validointi;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author mikkom
 */
@RestController
@RequestMapping("/opetussuunnitelmat")
@Api(value = "Opetussuunnitelmat")
public class OpetussuunnitelmaController {
    @Autowired
    private OpetussuunnitelmaService opetussuunnitelmaService;

    @Autowired
    private PermissionManager permissionManager;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    @Timed
    public List<OpetussuunnitelmaInfoDto> getAll(
            @RequestParam(value="tyyppi", required=false) Tyyppi tyyppi,
            @RequestParam(value="tila", required=false) Tila tila) {
        return opetussuunnitelmaService.getAll(tyyppi == null ? Tyyppi.OPS : tyyppi, tila);
    }

    @RequestMapping(value = "/tilastot", method = RequestMethod.GET)
    @ResponseBody
    @Timed
    public ResponseEntity<OpetussuunnitelmaStatistiikkaDto> getStatistiikka() {
        return new ResponseEntity<>(opetussuunnitelmaService.getStatistiikka(), HttpStatus.OK);
    }

    @RequestMapping(value = "/adminlist", method = RequestMethod.GET)
    @ResponseBody
    @Timed
    public ResponseEntity<List<OpetussuunnitelmaInfoDto>> getAdminList() {
        return new ResponseEntity<>(opetussuunnitelmaService.getAdminList(), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    @Timed
    public ResponseEntity<OpetussuunnitelmaKevytDto> get(@PathVariable("id") final Long id) {
        return ResponseEntity.ok(opetussuunnitelmaService.getOpetussuunnitelma(id));
    }

    @RequestMapping(value = "/{id}/kaikki", method = RequestMethod.GET)
    @ResponseBody
    @Timed
    public ResponseEntity<OpetussuunnitelmaLaajaDto> getKaikki(@PathVariable("id") final Long id) {
        return new ResponseEntity<>(opetussuunnitelmaService.getOpetussuunnitelmaEnempi(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/opetussuunnitelmat", method = RequestMethod.GET)
    @ResponseBody
    @Timed
    public ResponseEntity<List<OpetussuunnitelmaInfoDto>> getLapsiOpetussuunnitelmat(@PathVariable("id") final Long id) {
        return new ResponseEntity<>(opetussuunnitelmaService.getLapsiOpetussuunnitelmat(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/laajaalaisetosaamiset", method = RequestMethod.GET)
    @ResponseBody
    @Timed
    public Set<PerusteLaajaalainenosaaminenDto> getLaajalaisetosamiset(@PathVariable("id") final Long id) {
        return opetussuunnitelmaService.getLaajaalaisetosaamiset(id);
    }

    @RequestMapping(value = "/{id}/sync", method = RequestMethod.POST)
    @Timed
    public ResponseEntity sync(@PathVariable("id") final Long id) {
        opetussuunnitelmaService.syncPohja(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    @Timed
    public ResponseEntity<OpetussuunnitelmaDto> addOpetussuunnitelma(
            @RequestBody OpetussuunnitelmaLuontiDto opetussuunnitelmaDto) {
        if (opetussuunnitelmaDto.getTyyppi() == null) {
            opetussuunnitelmaDto.setTyyppi(Tyyppi.OPS);
        }

        if (opetussuunnitelmaDto.getTyyppi().equals(Tyyppi.POHJA)) {
            return new ResponseEntity<>(opetussuunnitelmaService.addPohja(opetussuunnitelmaDto),
                HttpStatus.OK);
        } else {
            return new ResponseEntity<>(opetussuunnitelmaService.addOpetussuunnitelma(opetussuunnitelmaDto),
                HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/{id}/oppiainejarjestys", method = RequestMethod.POST)
    @ResponseBody
    @Timed
    public ResponseEntity updateOpetussuunnitelma(
            @PathVariable("id") final Long id,
            @RequestBody List<JarjestysDto> oppiainejarjestys) {
        opetussuunnitelmaService.updateOppiainejarjestys(id, oppiainejarjestys);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    @ResponseBody
    @Timed
    public ResponseEntity<OpetussuunnitelmaDto> updateOpetussuunnitelma(
            @PathVariable("id") final Long id,
            @RequestBody OpetussuunnitelmaDto opetussuunnitelmaDto) {
        opetussuunnitelmaDto.setId(id);
        return new ResponseEntity<>(opetussuunnitelmaService.updateOpetussuunnitelma(opetussuunnitelmaDto),
                HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/opetussuunnitelmat", method = RequestMethod.POST)
    @Timed
    public ResponseEntity updateLapsiOpetussuunnitelmat(
            @PathVariable("id") final Long id) {
        opetussuunnitelmaService.updateLapsiOpetussuunnitelmat(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/tila/{tila}", method = RequestMethod.POST)
    @ResponseBody
    @Timed
    public ResponseEntity<OpetussuunnitelmaDto> updateTila(
        @PathVariable final Long id,
        @PathVariable Tila tila) {
        return new ResponseEntity<>(opetussuunnitelmaService.updateTila(id, tila), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/validoi", method = RequestMethod.GET)
    @ResponseBody
    @Timed
    public ResponseEntity<List<Validointi>> validoiOpetussuunnitelma(
        @PathVariable("id") final Long id) {
        return new ResponseEntity<>(opetussuunnitelmaService.validoiOpetussuunnitelma(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/palauta", method = RequestMethod.POST)
    @ResponseBody
    @Timed
    public ResponseEntity<OpetussuunnitelmaDto> restore(
        @PathVariable("id") final Long id) {
        return new ResponseEntity<>(opetussuunnitelmaService.restore(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @Timed
    public void deleteOpetussuunnitelma(@PathVariable("id") final Long id) {
        opetussuunnitelmaService.removeOpetussuunnitelma(id);
    }

    @RequestMapping(value = "/oikeudet", method = RequestMethod.GET)
    public ResponseEntity<Map<PermissionManager.TargetType, Set<PermissionManager.Permission>>> getOikeudet() {
        return new ResponseEntity<>(permissionManager.getOpsPermissions(), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/oikeudet", method = RequestMethod.GET)
    public ResponseEntity<Map<PermissionManager.TargetType, Set<PermissionManager.Permission>>> getOikeudet(
        @PathVariable("id") final Long id) {
        return new ResponseEntity<>(permissionManager.getOpsPermissions(id), HttpStatus.OK);
    }
}
