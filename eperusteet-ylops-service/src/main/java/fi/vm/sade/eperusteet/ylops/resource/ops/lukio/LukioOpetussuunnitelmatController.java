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
import fi.vm.sade.eperusteet.ylops.dto.lukio.AihekokonaisuudetPerusteOpsDto;
import fi.vm.sade.eperusteet.ylops.dto.lukio.LukioOpetussuunnitelmaRakenneOpsDto;
import fi.vm.sade.eperusteet.ylops.dto.lukio.OpetuksenYleisetTavoitteetPerusteOpsDto;
import fi.vm.sade.eperusteet.ylops.service.ops.lukio.LukioOpetussuunnitelmaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @ResponseBody
    @RequestMapping(value = "/aihekokonaisuudet", method = RequestMethod.GET)
    public AihekokonaisuudetPerusteOpsDto getAihekokonaisuudet(@PathVariable("opsId") Long opsId) {
        return lukioOpetussuunnitelmaService.getAihekokonaisuudet(opsId);
    }

    @ResponseBody
    @RequestMapping(value = "/opetuksenYleisetTavoitteet", method = RequestMethod.GET)
    public OpetuksenYleisetTavoitteetPerusteOpsDto getOpetuksenYleisetTavoitteet(@PathVariable("opsId") Long opsId) {
        return lukioOpetussuunnitelmaService.getOpetuksenYleisetTavoitteet(opsId);
    }
}
