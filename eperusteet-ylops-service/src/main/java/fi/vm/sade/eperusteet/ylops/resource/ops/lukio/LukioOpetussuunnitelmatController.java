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

import com.mangofactory.swagger.annotations.ApiIgnore;
import fi.vm.sade.eperusteet.ylops.dto.lukio.*;
import fi.vm.sade.eperusteet.ylops.service.ops.lukio.LukioOpetussuunnitelmaService;
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
@ApiIgnore
public class LukioOpetussuunnitelmatController {
    @Autowired
    private LukioOpetussuunnitelmaService lukioOpetussuunnitelmaService;

    @ResponseBody
    @RequestMapping(value = "/rakenne", method = RequestMethod.GET)
    public LukioOpetussuunnitelmaRakenneOpsDto getRakenne(@PathVariable("opsId") Long opsId) {
        return lukioOpetussuunnitelmaService.getRakenne(opsId);
    }

    @RequestMapping(value = "/rakenne", method = POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateStructure(@PathVariable("opsId") final Long opsId,
                                @RequestBody OppaineKurssiTreeStructureDto structureDto) {
        lukioOpetussuunnitelmaService.updateTreeStructure(opsId, structureDto);
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
        return new LongIdResultDto(lukioOpetussuunnitelmaService.saveAihekokonaisuus(opsId, kokonaisuus));
    }

    @RequestMapping(value = "/aihekokonaisuudet/jarjesta", method = RequestMethod.POST)
    public void reArrangeAihekokonaisuudet(@PathVariable("opsId") Long opsId,
                                               @RequestBody AihekokonaisuudetJarjestaDto jarjestys) {
        lukioOpetussuunnitelmaService.reArrangeAihekokonaisuudet(opsId, jarjestys);
    }

    @RequestMapping(value = "/aihekokonaisuudet/yleiskuvaus", method = RequestMethod.POST)
    public void updateAihekokonaisuudetYleiskuvaus(@PathVariable("opsId") Long opsId,
                                                   @RequestBody AihekokonaisuusSaveDto kokonaisuus) {
        lukioOpetussuunnitelmaService.updateAihekokonaisuusYleiskuvaus(opsId, kokonaisuus);
    }

    @RequestMapping(value = "/aihekokonaisuudet/kokonaisuus/{aihekokonaisuusId}", method = RequestMethod.POST)
    public void updateAihekokonaisuus(@PathVariable("opsId") Long opsId,
                                      @PathVariable("aihekokonaisuusId") Long id,
                                      @RequestBody AihekokonaisuusSaveDto kokonaisuus) {
        lukioOpetussuunnitelmaService.updateAihekokonaisuus(opsId, id, kokonaisuus);
    }

    @RequestMapping(value = "/aihekokonaisuudet/kokonaisuus/{aihekokonaisuusId}", method = RequestMethod.DELETE)
    public void deleteAihekokonaisuus(@PathVariable("opsId") Long opsId,
                                      @PathVariable("aihekokonaisuusId") Long id) {
        lukioOpetussuunnitelmaService.deleteAihekokonaisuus(opsId, id);
    }

    @ResponseBody
    @RequestMapping(value = "/opetuksenYleisetTavoitteet", method = RequestMethod.GET)
    public OpetuksenYleisetTavoitteetPerusteOpsDto getOpetuksenYleisetTavoitteet(@PathVariable("opsId") Long opsId) {
        return lukioOpetussuunnitelmaService.getOpetuksenYleisetTavoitteet(opsId);
    }

    @ResponseBody
    @RequestMapping(value = "/oppiaine", method = RequestMethod.POST)
    public LongIdResultDto saveOppiaine(@PathVariable("opsId") Long opsId,
                                        @RequestBody LukioOppiaineSaveDto oppiaine) {
        return new LongIdResultDto(lukioOpetussuunnitelmaService.saveOppiaine(opsId, oppiaine));
    }

    @ResponseBody
    @RequestMapping(value = "/oppiaine", method = RequestMethod.PUT)
    public void updateOppiaine(@PathVariable("opsId") Long opsId,
                               @RequestBody LukioOppiaineSaveDto oppiaine) {
        lukioOpetussuunnitelmaService.updateOppiaine( opsId, oppiaine );
    }

    @ResponseBody
    @RequestMapping(value = "/oppiaine/{oppiaineId}/kielitarjonta", method = RequestMethod.POST)
    public LongIdResultDto addOppimaara(@PathVariable("opsId") final Long opsId,
            @PathVariable("oppiaineId") final Long oppiaineId,
            @RequestBody LukioKopioiOppimaaraDto kt) {
        return new LongIdResultDto(lukioOpetussuunnitelmaService.addOppimaara(opsId, oppiaineId, kt));
    }

    @ResponseBody
    @RequestMapping(value = "/kurssi", method = RequestMethod.POST)
    public LongIdResultDto saveKurssi(@PathVariable("opsId") final Long opsId,
                                        @RequestBody LukiokurssiSaveDto kurssi) {
        return new LongIdResultDto(lukioOpetussuunnitelmaService.saveKurssi(opsId, kurssi));
    }

    @RequestMapping(value = "/kurssi/{kurssiId}", method = RequestMethod.POST)
    public void updateKurssi(@PathVariable("opsId") final Long opsId,
                                      @PathVariable("kurssiId") final Long kurssiId,
                                      @RequestBody LukiokurssiUpdateDto kurssi) {
        lukioOpetussuunnitelmaService.updateKurssi(opsId, kurssiId, kurssi);
    }

    @RequestMapping(value = "/kurssi/{kurssiId}/disconnect", method = RequestMethod.POST)
    public LongIdResultDto disconnectKurssi(@PathVariable("opsId") final Long opsId,
                                            @PathVariable("kurssiId") final Long kurssiId) {
        return new LongIdResultDto(lukioOpetussuunnitelmaService.disconnectKurssi( kurssiId, opsId ));
    }

    @RequestMapping(value = "/kurssi/{kurssiId}/reconnect", method = RequestMethod.POST)
    public LongIdResultDto reconnectKurssi(@PathVariable("opsId") final Long opsId,
                                            @PathVariable("kurssiId") final Long kurssiId) {
        return new LongIdResultDto(lukioOpetussuunnitelmaService.reconnectKurssi( kurssiId, opsId ));
    }
}
