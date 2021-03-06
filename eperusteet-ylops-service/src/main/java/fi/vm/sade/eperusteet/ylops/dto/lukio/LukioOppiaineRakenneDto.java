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

package fi.vm.sade.eperusteet.ylops.dto.lukio;

import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: tommiratamaa
 * Date: 27.11.2015
 * Time: 13.06
 */
@Getter
@Setter
public class LukioOppiaineRakenneDto<Tyyppi, KurssiTyyppi extends LukiokurssiListausOpsDto>
        extends LukioOppimaaraPerusTiedotDto
        implements Serializable {
    protected List<Tyyppi> oppimaarat = new ArrayList<>();
    private LokalisoituTekstiDto kieli;
    protected List<KurssiTyyppi> kurssit = new ArrayList<>();
}
