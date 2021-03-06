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
package fi.vm.sade.eperusteet.ylops.dto.ops;

import fi.vm.sade.eperusteet.ylops.domain.Tila;
import fi.vm.sade.eperusteet.ylops.dto.Reference;
import fi.vm.sade.eperusteet.ylops.dto.ReferenceableDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.TekstiosaDto;

import java.util.Optional;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

/**
 * @author jhyoty
 */
@Getter
@Setter
public class VuosiluokkakokonaisuusDto implements ReferenceableDto {

    private Long id;
    private Optional<Reference> tunniste;
    private Optional<LokalisoituTekstiDto> nimi;
    private Optional<TekstiosaDto> siirtymaEdellisesta;
    private Optional<TekstiosaDto> tehtava;
    private Optional<TekstiosaDto> siirtymaSeuraavaan;
    private Optional<TekstiosaDto> laajaalainenosaaminen;
    private Optional<Tila> tila;
    private Set<LaajaalainenosaaminenDto> laajaalaisetosaamiset;

    public VuosiluokkakokonaisuusDto() {
    }

    public VuosiluokkakokonaisuusDto(Reference tunniste) {
        this.tunniste = Optional.ofNullable(tunniste);
    }

}
