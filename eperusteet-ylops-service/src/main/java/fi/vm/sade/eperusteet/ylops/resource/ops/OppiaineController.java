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
import fi.vm.sade.eperusteet.ylops.domain.peruste.Peruste;
import fi.vm.sade.eperusteet.ylops.domain.peruste.PerusteOppiaine;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Kieli;
import fi.vm.sade.eperusteet.ylops.dto.ops.OppiaineDto;
import fi.vm.sade.eperusteet.ylops.resource.util.Responses;
import fi.vm.sade.eperusteet.ylops.service.ops.OpetussuunnitelmaService;
import fi.vm.sade.eperusteet.ylops.service.ops.OppiaineService;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
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

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<OppiaineDto> get(@PathVariable("opsId") final Long opsId, @PathVariable("id") final Long id) {
        return response(oppiaineService.get(opsId, id));
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<OppiaineDto> getAll(@PathVariable("opsId") final Long opsId) {
        return oppiaineService.getAll(opsId);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public OppiaineDto update(@PathVariable("opsId") final Long opsId, @PathVariable("id") final Long id,
        @RequestBody OppiaineDto dto) {
        dto.setId(id);
        return oppiaineService.update(opsId, dto);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("opsId") final Long opsId, @PathVariable("id") final Long id) {
        oppiaineService.delete(opsId, id);
    }

    @RequestMapping(value = "/{id}/peruste", method = RequestMethod.GET)
    public ResponseEntity<PerusteOppiaine> getPerusteSisalto(@PathVariable("opsId") final Long opsId, @PathVariable("id") final Long id) {

        final Peruste peruste = ops.getPeruste(opsId);
        final OppiaineDto aine = oppiaineService.get(opsId, id);

        Optional<PerusteOppiaine> oppiaine;
        if (peruste != null && aine != null) {
            oppiaine = peruste.getPerusopetus().getOppiaineet()
                .stream()
                .flatMap(oa -> Stream.concat(Stream.of(oa), Optional.ofNullable(oa.getOppimaarat()).orElse(Collections.emptySet()).stream()))
                .peek(oa -> LOG.debug(oa.toString()))
                //TODO --koodiuri ei ole riittävän yksilöivä. tarvitaan tunniste oppiaineisiinkin
                //.filter(oa -> Objects.equals(oa.getKoodiUri(), aine.getKoodiUri()))
                //XXX:
                .filter(oa -> Objects.equals(oa.getNimi().get(Kieli.FI), aine.getNimi().get(Kieli.FI)))
                .findAny();
        } else {
            oppiaine = Optional.empty();
        }
        return Responses.of(oppiaine);
    }

    private static final Logger LOG = LoggerFactory.getLogger(OppiaineController.class);

    private static <T> ResponseEntity<T> response(T data) {
        if (data == null) {
            return new ResponseEntity<>((T) null, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
}
