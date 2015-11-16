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
package fi.vm.sade.eperusteet.ylops.dto.peruste.lukio;

import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusteTekstiOsa;
import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

/**
 * Created by jsikio.
 */
@Getter
@Setter
public class LukioPerusteOppiaineDto {

    private Long id;
    private UUID tunniste;
    private String koodiUri;
    private String koodiArvo;
    private Boolean koosteinen;
    private Integer jarjestys;
    private Boolean abstrakti;
    private LokalisoituTekstiDto nimi;
    private LokalisoituTekstiDto pakollinenKurssiKuvaus;
    private LokalisoituTekstiDto syventavaKurssiKuvaus;
    private LokalisoituTekstiDto soveltavaKurssiKuvaus;
    private PerusteTekstiOsa tehtava;
    private PerusteTekstiOsa tavoitteet;
    private PerusteTekstiOsa arviointi;

    private Set<LukioPerusteOppiaineDto> oppimaarat;
    private Set<Lukiokurssi> kurssit;

}
