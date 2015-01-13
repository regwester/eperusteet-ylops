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
package fi.vm.sade.eperusteet.ylops.dto.teksti;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.ylops.domain.OpetussuunnitelmanTila;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

/**
 *
 * @author mikkom
 */
@Getter
@Setter
public class TekstiKappaleDto {
    private Long id;
    private Date luotu;
    private Date muokattu;
    private String muokkaaja;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String muokkaajanNimi;

    private LokalisoituTekstiDto nimi;
    private LokalisoituTekstiDto teksti;
    private OpetussuunnitelmanTila tila;
    private UUID tunniste;

    public TekstiKappaleDto() {}

    public TekstiKappaleDto(LokalisoituTekstiDto nimi, LokalisoituTekstiDto teksti, OpetussuunnitelmanTila tila) {
        this.nimi = nimi;
        this.teksti = teksti;
        this.tila = tila;
    }
}
