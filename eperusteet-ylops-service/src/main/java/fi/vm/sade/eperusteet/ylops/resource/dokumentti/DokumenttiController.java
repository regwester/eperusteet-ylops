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
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import fi.vm.sade.eperusteet.ylops.domain.dokumentti.DokumenttiTila;
import fi.vm.sade.eperusteet.ylops.domain.teksti.Kieli;
import fi.vm.sade.eperusteet.ylops.dto.dokumentti.DokumenttiDto;
import fi.vm.sade.eperusteet.ylops.resource.util.CacheControl;
import fi.vm.sade.eperusteet.ylops.service.dokumentti.DokumenttiService;
import fi.vm.sade.eperusteet.ylops.service.exception.DokumenttiException;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;

/**
 *
 * @author iSaul
 */
@RestController
@RequestMapping("/dokumentit")
public class DokumenttiController {

    private static final Logger LOG = LoggerFactory.getLogger(DokumenttiController.class);

    private static final int MAX_TIME_IN_MINUTES = 2;

    @Autowired
    DokumenttiService service;

    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation("luo dokumentti")
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "dokumentti luodaan"),
            @ApiResponse(code = 401, message = "ei oikeutta luoda dokumenttia"),
            @ApiResponse(code = 404, message = "opetussuunnitelma ei löydy"),
            @ApiResponse(code = 403, message = "dokumentin luonti on jo käynnissä")
    })
    public ResponseEntity<DokumenttiDto> create(
            @RequestParam("opsId") final long opsId,
            @RequestParam(value = "kieli", defaultValue = "fi") final String kieli) throws DokumenttiException {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        DokumenttiDto dtoForDokumentti = service.getDto(opsId, Kieli.of(kieli));

        // Jos dokumentti ei löydy valmiiksi niin koitetaan tehdä uusi
        if (dtoForDokumentti == null)
            dtoForDokumentti = service.createDtoFor(opsId, Kieli.of(kieli));

        // Jos tila epäonnistunut, opsia ei löytynyt
        if (dtoForDokumentti == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        // Aloitetaan luonti jos luonti ei ole jo päällä tai maksimi luontiaika ylitetty
        if (isTimePass(dtoForDokumentti) || dtoForDokumentti.getTila() != DokumenttiTila.LUODAAN) {
            // Vaihdetaan dokumentin tila luonniksi
            service.setStarted(dtoForDokumentti);

            // Generoidaan dokumentin data sisältö
            // Asynkroninen metodi
            service.generateWithDto(dtoForDokumentti);

            status = HttpStatus.ACCEPTED;
        } else {
            status = HttpStatus.FORBIDDEN;
        }

        // Uusi objekti dokumentissa, jossa päivitetyt tiedot
        final DokumenttiDto dtoDokumentti = service.getDto(dtoForDokumentti.getId());

        return new ResponseEntity<>(dtoDokumentti, status);
    }

    private boolean isTimePass(DokumenttiDto dokumenttiDto) {
        Date date = dokumenttiDto.getAloitusaika();
        if (date == null) {
            return true;
        }

        Date newDate = DateUtils.addMinutes(date, MAX_TIME_IN_MINUTES);
        return newDate.before(new Date());
    }

    @RequestMapping(value = "/{dokumenttiId}", method = RequestMethod.GET)
    @ApiOperation("luo dokumentti")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "dokumentti ladattu onnistuneesti"),
            @ApiResponse(code = 401, message = "ei oikeutta luoda dokumenttia"),
            @ApiResponse(code = 404, message = "dokumenttia ei löydy")
    })
    @CacheControl(age = CacheControl.ONE_YEAR, nonpublic = false)
    public ResponseEntity<byte[]> get(@PathVariable("dokumenttiId") final Long dokumenttiId) {
        byte[] pdfdata = service.get(dokumenttiId);

        if (pdfdata == null || pdfdata.length == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "pdf"));
        headers.set("Content-disposition", "inline; filename=\"" + dokumenttiId + ".pdf\"");
        headers.setContentLength(pdfdata.length);

        return new ResponseEntity<>(pdfdata, headers, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, params = "opsId")
    public ResponseEntity<DokumenttiDto> getDokumentti(
            @RequestParam("opsId") final Long opsId,
            @RequestParam(value = "kieli", defaultValue = "fi") final String kieli) {
        Kieli k = Kieli.of(kieli);
        DokumenttiDto dto = service.getDto(opsId, k);
        if (dto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(dto, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/{dokumenttiId}/dokumentti", method = RequestMethod.GET)
    public ResponseEntity<DokumenttiDto> query(@PathVariable("dokumenttiId") final Long dokumenttiId) {
        DokumenttiDto dto = service.query(dokumenttiId);
        if (dto == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        else
            return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/{dokumenttiId}/tila", method = RequestMethod.GET)
    public ResponseEntity<DokumenttiTila> exist(
            @RequestParam("opsId") final Long opsId,
            @RequestParam(value = "kieli", defaultValue = "fi") final String kieli) {
        Kieli k = Kieli.of(kieli);
        DokumenttiTila tila = service.getTila(opsId, k);
        if (tila == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return ResponseEntity.ok(tila);
        }
    }
}
