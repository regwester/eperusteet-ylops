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
package fi.vm.sade.eperusteet.ylops.dto.oppiaine;

import fi.vm.sade.eperusteet.ylops.domain.Tila;
import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.TekstiosaDto;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 *
 * @author mikkom
 */
@Getter
@Setter
public class OppiaineDto {
    public Long id;
    public String koodi;
    public LokalisoituTekstiDto nimi;
    public TekstiosaDto tehtava;
    public OppiaineDto oppiaine;
    public boolean koosteinen;
    public Set<OppiaineDto> oppimaarat;
    public Tila tila;
    public Set<OpetuksenKohdealueDto> kohdealueet;
    // TODO: Lisää vuosiluokkakokonaisuudet
}
