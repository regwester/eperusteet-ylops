/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.ylops.resource.ops.lukio;

import fi.vm.sade.eperusteet.ylops.dto.lukio.*;
import fi.vm.sade.eperusteet.ylops.service.audit.EperusteetYlopsAudit;

import static fi.vm.sade.eperusteet.ylops.service.audit.EperusteetYlopsMessageFields.ABSTRAKTIOPPIAINE;
import static fi.vm.sade.eperusteet.ylops.service.audit.EperusteetYlopsMessageFields.AIHEKOKONAISUUS;
import static fi.vm.sade.eperusteet.ylops.service.audit.EperusteetYlopsMessageFields.KIELITARJONTA;
import static fi.vm.sade.eperusteet.ylops.service.audit.EperusteetYlopsMessageFields.KURSSI;
import static fi.vm.sade.eperusteet.ylops.service.audit.EperusteetYlopsMessageFields.OPPIAINE;
import static fi.vm.sade.eperusteet.ylops.service.audit.EperusteetYlopsMessageFields.RAKENNE;
import static fi.vm.sade.eperusteet.ylops.service.audit.EperusteetYlopsMessageFields.YLEISKUVAUS;
import static fi.vm.sade.eperusteet.ylops.service.audit.EperusteetYlopsMessageFields.YLEISTAVOITE;
import static fi.vm.sade.eperusteet.ylops.service.audit.EperusteetYlopsOperation.JARJESTAMINEN;
import static fi.vm.sade.eperusteet.ylops.service.audit.EperusteetYlopsOperation.LIITOS;
import static fi.vm.sade.eperusteet.ylops.service.audit.EperusteetYlopsOperation.LIITOSPOISTO;
import static fi.vm.sade.eperusteet.ylops.service.audit.EperusteetYlopsOperation.LISAYS;
import static fi.vm.sade.eperusteet.ylops.service.audit.EperusteetYlopsOperation.MUOKKAUS;
import static fi.vm.sade.eperusteet.ylops.service.audit.EperusteetYlopsOperation.POISTO;

import fi.vm.sade.eperusteet.ylops.service.audit.LogMessage;
import fi.vm.sade.eperusteet.ylops.service.ops.lukio.LukioOpetussuunnitelmaService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * User: tommiratamaa
 * Date: 19.11.2015
 * Time: 14.32
 */
@RestController
@RequestMapping("/opetussuunnitelmat/lukio/{opsId}")
@Api(value = "Lukio")
public class LukioOpetussuunnitelmatController {
    @Autowired
    private EperusteetYlopsAudit audit;

    @Autowired
    private LukioOpetussuunnitelmaService lukioOpetussuunnitelmaService;

    @ResponseBody
    @RequestMapping(value = "/rakenne", method = RequestMethod.GET)
    public LukioOpetussuunnitelmaRakenneOpsDto getRakenne(@PathVariable("opsId") Long opsId) {
        return lukioOpetussuunnitelmaService.getRakenne(opsId);
    }

    @ResponseBody
    @RequestMapping(value = "/oppiaine/{oppiaineId}", method = RequestMethod.GET)
    public LukioOppiaineTiedotDto getOppiaine(@PathVariable("opsId") Long opsId,
                                              @PathVariable("oppiaineId") Long oppiaineId) {
        return lukioOpetussuunnitelmaService.getOppiaineTiedot(opsId, oppiaineId);
    }

    @RequestMapping(value = "/rakenne", method = POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateStructure(@PathVariable("opsId") final Long opsId,
                                @RequestBody OppaineKurssiTreeStructureDto structureDto) {
        audit.withAudit(LogMessage.builder(opsId, RAKENNE, MUOKKAUS), (Void) -> {
            lukioOpetussuunnitelmaService.updateTreeStructure(opsId, structureDto);
            return null;
        });
    }

    @ResponseBody
    @RequestMapping(value = "/aihekokonaisuudet", method = RequestMethod.GET)
    public AihekokonaisuudetPerusteOpsDto getAihekokonaisuudet(@PathVariable("opsId") Long opsId) {
        return lukioOpetussuunnitelmaService.getAihekokonaisuudet(opsId);
    }

    @ResponseBody
    @RequestMapping(value = "/aihekokonaisuudet/kokonaisuus", method = RequestMethod.POST)
    public LongIdResultDto saveAihekokonaisuus(@PathVariable("opsId") Long opsId,
                                               @RequestBody AihekokonaisuusSaveDto kokonaisuus) {
        return audit.withAudit(LogMessage.builder(opsId, AIHEKOKONAISUUS, LISAYS), (Void) -> {
            return new LongIdResultDto(lukioOpetussuunnitelmaService.saveAihekokonaisuus(opsId, kokonaisuus));
        });
    }

    @RequestMapping(value = "/aihekokonaisuudet/jarjesta", method = RequestMethod.POST)
    public void reArrangeAihekokonaisuudet(@PathVariable("opsId") Long opsId,
                                           @RequestBody AihekokonaisuudetJarjestaDto jarjestys) {
        audit.withAudit(LogMessage.builder(opsId, AIHEKOKONAISUUS, JARJESTAMINEN), (Void) -> {
            lukioOpetussuunnitelmaService.reArrangeAihekokonaisuudet(opsId, jarjestys);
            return null;
        });
    }

    @RequestMapping(value = "/aihekokonaisuudet/yleiskuvaus", method = RequestMethod.POST)
    public void updateAihekokonaisuudetYleiskuvaus(@PathVariable("opsId") Long opsId,
                                                   @RequestBody AihekokonaisuusSaveDto kokonaisuus) {
        audit.withAudit(LogMessage.builder(opsId, YLEISKUVAUS, MUOKKAUS), (Void) -> {
            lukioOpetussuunnitelmaService.updateAihekokonaisuusYleiskuvaus(opsId, kokonaisuus);
            return null;
        });
    }

    @RequestMapping(value = "/aihekokonaisuudet/kokonaisuus/{aihekokonaisuusId}", method = RequestMethod.POST)
    public void updateAihekokonaisuus(@PathVariable("opsId") Long opsId,
                                      @PathVariable("aihekokonaisuusId") Long id,
                                      @RequestBody AihekokonaisuusSaveDto kokonaisuus) {
        audit.withAudit(LogMessage.builder(opsId, AIHEKOKONAISUUS, MUOKKAUS), (Void) -> {
            lukioOpetussuunnitelmaService.updateAihekokonaisuus(opsId, id, kokonaisuus);
            return null;
        });
    }

    @RequestMapping(value = "/aihekokonaisuudet/kokonaisuus/{aihekokonaisuusId}", method = RequestMethod.DELETE)
    public void deleteAihekokonaisuus(@PathVariable("opsId") Long opsId,
                                      @PathVariable("aihekokonaisuusId") Long id) {
        audit.withAudit(LogMessage.builder(opsId, AIHEKOKONAISUUS, POISTO), (Void) -> {
            lukioOpetussuunnitelmaService.deleteAihekokonaisuus(opsId, id);
            return null;
        });
    }

    @ResponseBody
    @RequestMapping(value = "/opetuksenYleisetTavoitteet", method = RequestMethod.GET)
    public OpetuksenYleisetTavoitteetPerusteOpsDto getOpetuksenYleisetTavoitteet(@PathVariable("opsId") Long opsId) {
        return lukioOpetussuunnitelmaService.getOpetuksenYleisetTavoitteet(opsId);
    }

    @ResponseBody
    @RequestMapping(value = "/opetuksenYleisetTavoitteet", method = RequestMethod.POST)
    public void updateOpetuksenYleisetTavoitteet(@PathVariable("opsId") Long opsId,
                                                 @RequestBody OpetuksenYleisetTavoitteetUpdateDto tavoitteet) {
        audit.withAudit(LogMessage.builder(opsId, YLEISTAVOITE, MUOKKAUS), (Void) -> {
            lukioOpetussuunnitelmaService.updateOpetuksenYleisetTavoitteet(opsId, tavoitteet);
            return null;
        });
    }

    @ResponseBody
    @RequestMapping(value = "/oppiaine", method = RequestMethod.POST)
    public LongIdResultDto saveOppiaine(@PathVariable("opsId") Long opsId,
                                        @RequestBody LukioOppiaineSaveDto oppiaine) {
        return audit.withAudit(LogMessage.builder(opsId, OPPIAINE, LISAYS), (Void) -> {
            return new LongIdResultDto(lukioOpetussuunnitelmaService.saveOppiaine(opsId, oppiaine));
        });
    }

    @ResponseBody
    @RequestMapping(value = "/oppiaine", method = RequestMethod.PUT)
    public void updateOppiaine(@PathVariable("opsId") Long opsId,
                               @RequestBody LukioOppiaineSaveDto oppiaine) {
        audit.withAudit(LogMessage.builder(opsId, OPPIAINE, MUOKKAUS), (Void) -> {
            lukioOpetussuunnitelmaService.updateOppiaine(opsId, oppiaine);
            return null;
        });
    }

    @ResponseBody
    @RequestMapping(value = "/oppiaine/{oppiaineId}/kielitarjonta", method = RequestMethod.POST)
    public LongIdResultDto addOppimaara(@PathVariable("opsId") final Long opsId,
                                        @PathVariable("oppiaineId") final Long oppiaineId,
                                        @RequestBody LukioKopioiOppimaaraDto kt) {
        return audit.withAudit(LogMessage.builder(opsId, KIELITARJONTA, LISAYS), (Void) -> {
            return new LongIdResultDto(lukioOpetussuunnitelmaService.addOppimaara(opsId, oppiaineId, kt));
        });
    }

    @ResponseBody
    @RequestMapping(value = "/oppiaine/abstrakti", method = RequestMethod.POST)
    public LongIdResultDto addAbstraktiOppiaine(@PathVariable("opsId") final Long opsId,
                                                @RequestBody LukioAbstraktiOppiaineTuontiDto tuonti) {
        return audit.withAudit(LogMessage.builder(opsId, ABSTRAKTIOPPIAINE, LISAYS), (Void) -> {
            return new LongIdResultDto(lukioOpetussuunnitelmaService.addAbstraktiOppiaine(opsId, tuonti));
        });
    }

    @ResponseBody
    @RequestMapping(value = "/kurssi", method = RequestMethod.POST)
    public LongIdResultDto saveKurssi(@PathVariable("opsId") final Long opsId,
                                      @RequestBody LukiokurssiSaveDto kurssi) {
        return audit.withAudit(LogMessage.builder(opsId, KURSSI, LISAYS), (Void) -> {
            return new LongIdResultDto(lukioOpetussuunnitelmaService.saveKurssi(opsId, kurssi));
        });
    }

    @RequestMapping(value = "/kurssi/{kurssiId}", method = RequestMethod.POST)
    public void updateKurssi(@PathVariable("opsId") final Long opsId,
                             @PathVariable("kurssiId") final Long kurssiId,
                             @RequestBody LukiokurssiUpdateDto kurssi) {
        audit.withAudit(LogMessage.builder(opsId, KURSSI, MUOKKAUS), (Void) -> {
            lukioOpetussuunnitelmaService.updateKurssi(opsId, kurssiId, kurssi);
            return null;
        });
    }

    @RequestMapping(value = "/oppiaine/{oppiaineId}/kurssi/{kurssiId}/disconnect", method = RequestMethod.POST)
    public LongIdResultDto disconnectKurssi(@PathVariable final Long opsId,
                                            @PathVariable final Long oppiaineId,
                                            @PathVariable final Long kurssiId) {
        return audit.withAudit(LogMessage.builder(opsId, KURSSI, LIITOSPOISTO), (Void) -> {
            return new LongIdResultDto(lukioOpetussuunnitelmaService.disconnectKurssi(kurssiId, oppiaineId, opsId));
        });
    }

    @RequestMapping(value = "/oppiaine/{oppiaineId}/kurssi/{kurssiId}/reconnect", method = RequestMethod.POST)
    public LongIdResultDto reconnectKurssi(@PathVariable final Long opsId,
                                           @PathVariable final Long oppiaineId,
                                           @PathVariable final Long kurssiId) {
        return audit.withAudit(LogMessage.builder(opsId, KURSSI, LIITOS), (Void) -> {
            return new LongIdResultDto(lukioOpetussuunnitelmaService.reconnectKurssi(kurssiId, oppiaineId, opsId));
        });
    }

    @RequestMapping(value = "/kurssi/{kurssiId}/remove", method = RequestMethod.DELETE)
    public void removeKurssi(@PathVariable("opsId") final Long opsId,
                             @PathVariable("kurssiId") final Long kurssiId) {
        audit.withAudit(LogMessage.builder(opsId, KURSSI, POISTO), (Void) -> {
            lukioOpetussuunnitelmaService.removeKurssi(opsId, kurssiId);
            return null;
        });
    }
}
