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

package fi.vm.sade.eperusteet.ylops.resource.dokumentti;

import com.wordnik.swagger.annotations.ApiOperation;
import fi.vm.sade.eperusteet.ylops.domain.dokumentti.DokumenttiTila;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Kieli;
import fi.vm.sade.eperusteet.ylops.dto.dokumentti.DokumenttiDto;
import fi.vm.sade.eperusteet.ylops.resource.util.CacheControl;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.DokumenttiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author iSaul
 */
@RestController
@RequestMapping("/dokumentit")
//@InternalApi
public class DokumenttiController {

    private static final Logger LOG = LoggerFactory.getLogger(DokumenttiController.class);

    @Autowired
    DokumenttiService service;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation("luo dokumentti")
    public ResponseEntity<DokumenttiDto> create(
            @RequestParam("opsId") final long opsId,
            @RequestParam(value = "kieli", defaultValue = "fi") final String kieli) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        final DokumenttiDto dtoForDokumentti = service.createDtoFor(opsId, Kieli.of(kieli));

        // Jos dto luonti ei epäonnistunut aletaan rakentamaan dokumenttia
        if (dtoForDokumentti.getTila() != DokumenttiTila.EPAONNISTUI) {
            // Vaihdetaan dokumentin tila luonniksi
            service.setStarted(dtoForDokumentti);
            // Generoidaan dokumentin data sisältö
            service.generateWithDto(dtoForDokumentti);
            status = HttpStatus.ACCEPTED;
        }
        return new ResponseEntity<>(dtoForDokumentti, status);
    }

    @RequestMapping(value = "/{dokumenttiId}", method = RequestMethod.GET, produces = "application/pdf")
    @ResponseBody
    @CacheControl(age = CacheControl.ONE_YEAR, nonpublic = false)
    public ResponseEntity<Object> get(@PathVariable("dokumenttiId") final Long dokumenttiId) {
        byte[] pdfdata = service.get(dokumenttiId);

        if (pdfdata == null || pdfdata.length == 0) {
            LOG.error("Got null or empty data from service");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-disposition", "attachment; filename=\"" + dokumenttiId + ".pdf\"");
        return new ResponseEntity<Object>(pdfdata, headers, HttpStatus.OK);
    }
}
