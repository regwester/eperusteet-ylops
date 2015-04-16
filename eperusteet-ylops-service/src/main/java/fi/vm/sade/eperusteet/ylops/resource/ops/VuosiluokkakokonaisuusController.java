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
import fi.vm.sade.eperusteet.ylops.domain.peruste.Peruste;
import fi.vm.sade.eperusteet.ylops.dto.Reference;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.UnwrappedOpsVuosiluokkakokonaisuusDto;
import fi.vm.sade.eperusteet.ylops.dto.ops.VuosiluokkakokonaisuusDto;
import fi.vm.sade.eperusteet.ylops.resource.util.Responses;
import fi.vm.sade.eperusteet.ylops.service.ops.OpetussuunnitelmaService;
import fi.vm.sade.eperusteet.ylops.service.ops.VuosiluokkakokonaisuusService;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Autowired
    private OpetussuunnitelmaService opetussuunnitelmat;

    @RequestMapping(method = RequestMethod.POST)
    public VuosiluokkakokonaisuusDto add(@PathVariable("opsId") final Long opsId, @RequestBody VuosiluokkakokonaisuusDto dto) {
        return vuosiluokkakokonaisuudet.add(opsId, dto);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<UnwrappedOpsVuosiluokkakokonaisuusDto> get(@PathVariable("opsId") final Long opsId, @PathVariable("id") final Long id) {
        return Responses.ofNullable(new UnwrappedOpsVuosiluokkakokonaisuusDto(vuosiluokkakokonaisuudet.get(opsId, id)));
    }

    @RequestMapping(value = "/{id}/peruste", method = RequestMethod.GET)
    public fi.vm.sade.eperusteet.ylops.domain.peruste.PerusteVuosiluokkakokonaisuus getPerusteSisalto(@PathVariable("opsId") final Long opsId, @PathVariable("id") final Long id) {

        final Peruste peruste = opetussuunnitelmat.getPeruste(opsId);
        final VuosiluokkakokonaisuusDto v = vuosiluokkakokonaisuudet.get(opsId, id).getVuosiluokkakokonaisuus();

        Optional<fi.vm.sade.eperusteet.ylops.domain.peruste.PerusteVuosiluokkakokonaisuus> vkDto = peruste.getPerusopetus().getVuosiluokkakokonaisuudet()
            .stream()
            .filter(vk -> Reference.of(vk.getTunniste()).equals(v.getTunniste().get()))
            .findAny();

        return vkDto.get();
    }

    @RequestMapping(value = "/{id}/oppiaineet", method = RequestMethod.GET)
    public Set<OppiaineDto> findOppiaineet(@PathVariable("opsId") final Long opsId, @PathVariable("id") final Long id) {
        throw new UnsupportedOperationException("Ei ole toteutettu");
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

    @RequestMapping(value = "/{id}/muokattavakopio", method = RequestMethod.POST)
    public VuosiluokkakokonaisuusDto kopioiMuokattavaksi(@PathVariable("opsId") final Long opsId,
                                                         @PathVariable("id") final Long id) {
        return vuosiluokkakokonaisuudet.kopioiMuokattavaksi(opsId, id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("opsId") final Long opsId, @PathVariable("id") final Long id) {
        vuosiluokkakokonaisuudet.delete(opsId, id);
    }

    private static final Logger LOG = LoggerFactory.getLogger(VuosiluokkakokonaisuusController.class);
}
