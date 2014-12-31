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
package fi.vm.sade.eperusteet.ylops.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.vm.sade.eperusteet.ylops.domain.OpetussuunnitelmanTila;
import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.TekstiKappaleViiteDto;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author mikkom
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpetussuunnitelmaDto implements Serializable {
    private Long id;

    private Date luotu;
    private String luoja;
    private Date muokattu;
    private String muokkaaja;
    private LokalisoituTekstiDto nimi;
    private LokalisoituTekstiDto kuvaus;
    private OpetussuunnitelmanTila tila;
    private TekstiKappaleViiteDto.Puu tekstit;
}
