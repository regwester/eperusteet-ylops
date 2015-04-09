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
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.OppiaineTyyppi;
import fi.vm.sade.eperusteet.ylops.domain.peruste.Peruste;
import fi.vm.sade.eperusteet.ylops.domain.peruste.PerusteOppiaine;
import fi.vm.sade.eperusteet.ylops.dto.ops.KielitarjontaDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineenTallennusDto;
import fi.vm.sade.eperusteet.ylops.resource.util.CacheControl;
import fi.vm.sade.eperusteet.ylops.resource.util.Responses;
import fi.vm.sade.eperusteet.ylops.service.ops.OpetussuunnitelmaService;
import fi.vm.sade.eperusteet.ylops.service.ops.OppiaineService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author mikkom
 */
@RestController
@RequestMapping("/opetussuunnitelmat/{opsId}/oppiaineet")
@ApiIgnore
public class OppiaineController {

    @Autowired
    private OppiaineService oppiaineService;

    @Autowired
    private OpetussuunnitelmaService ops;

    @RequestMapping(method = RequestMethod.POST)
    public OppiaineDto add(@PathVariable("opsId") final Long opsId, @RequestBody OppiaineDto dto) {
        return oppiaineService.add(opsId, dto);
    }

    @RequestMapping(value = "/valinnainen", method = RequestMethod.POST)
    public OppiaineDto addValinnainen(@PathVariable("opsId") final Long opsId, @RequestBody OppiaineenTallennusDto dto) {
        return oppiaineService.addValinnainen(opsId, dto.getOppiaine(), dto.getVuosiluokkakokonaisuusId(),
                                              dto.getVuosiluokat(), dto.getTavoitteet());
    }

    @RequestMapping(value = "/{id}/kielitarjonta", method = RequestMethod.POST)
    public OppiaineDto addOppimaara(
            @PathVariable("opsId") final Long opsId,
            @PathVariable("id") final Long oppiaineId,
            @RequestBody KielitarjontaDto kt) {
        return oppiaineService.addKielitarjonta(opsId, oppiaineId, kt);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<OppiaineDto> get(@PathVariable("opsId") final Long opsId, @PathVariable("id") final Long id) {
        return Responses.ofNullable(oppiaineService.get(opsId, id));
    }

    @RequestMapping(value = "/{id}/parent", method = RequestMethod.GET)
    public ResponseEntity<OppiaineDto> getParent(@PathVariable("opsId") final Long opsId, @PathVariable("id") final Long id) {
        return Responses.ofNullable(oppiaineService.getParent(opsId, id));
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<OppiaineDto> getAll(@PathVariable("opsId") final Long opsId,
                                    @RequestParam(value = "tyyppi", required = false) OppiaineTyyppi tyyppi) {
        if (tyyppi == null) {
            return oppiaineService.getAll(opsId, OppiaineTyyppi.YHTEINEN);
        } else {
            return oppiaineService.getAll(opsId, tyyppi);
        }
    }

    @RequestMapping(value = "/yhteiset",method = RequestMethod.GET)
    public List<OppiaineDto> getYhteiset(@PathVariable("opsId") final Long opsId) {
        return oppiaineService.getAll(opsId, false);
    }

    @RequestMapping(value = "/valinnaiset",method = RequestMethod.GET)
    public List<OppiaineDto> getValinnaiset(@PathVariable("opsId") final Long opsId) {
        return oppiaineService.getAll(opsId, true);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public OppiaineDto update(@PathVariable("opsId") final Long opsId, @PathVariable("id") final Long id,
        @RequestBody OppiaineDto dto) {
        dto.setId(id);
        return oppiaineService.update(opsId, dto);
    }

    @RequestMapping(value = "/{id}/valinnainen", method = RequestMethod.POST)
    public OppiaineDto updateValinnainen(@PathVariable("opsId") final Long opsId, @PathVariable("id") final Long id,
                                         @RequestBody OppiaineenTallennusDto dto) {
        dto.getOppiaine().setId(id);
        return oppiaineService.updateValinnainen(opsId, dto.getOppiaine(), dto.getVuosiluokkakokonaisuusId(),
                                                 dto.getVuosiluokat(), dto.getTavoitteet());
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("opsId") final Long opsId, @PathVariable("id") final Long id) {
        oppiaineService.delete(opsId, id);
    }

    @RequestMapping(value = "/{id}/peruste", method = RequestMethod.GET)
    @CacheControl(nonpublic = false, age = 3600)
    public ResponseEntity<PerusteOppiaine> getPerusteSisalto(@PathVariable("opsId") final Long opsId, @PathVariable("id") final Long id) {

        Peruste p = ops.getPeruste(opsId);
        return Responses.of(Optional.ofNullable(oppiaineService.get(opsId, id))
            .flatMap(a -> p.getPerusopetus().getOppiaine(a.getTunniste())));
    }

}
