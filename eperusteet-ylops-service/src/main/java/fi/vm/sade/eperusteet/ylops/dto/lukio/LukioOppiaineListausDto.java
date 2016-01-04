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

import fi.vm.sade.eperusteet.ylops.domain.Tila;
import fi.vm.sade.eperusteet.ylops.domain.lukio.LukiokurssiTyyppi;
import fi.vm.sade.eperusteet.ylops.domain.oppiaine.OppiaineTyyppi;
import fi.vm.sade.eperusteet.ylops.dto.peruste.lukio.LukioPerusteOppiaineDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.ylops.dto.teksti.TekstiosaDto;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

/**
 * User: tommiratamaa
 * Date: 27.11.2015
 * Time: 13.06
 */
@Getter
@Setter
public class LukioOppiaineListausDto implements Serializable,
        PerusteeseenViittaava<LukioPerusteOppiaineDto> {
    private LukioPerusteOppiaineDto perusteen;
    private Long id;
    private Long oppiaineId;
    private Date muokattu;
    private UUID tunniste;
    private Tila tila;
    private BigDecimal lukioLaajuus;
    private boolean oma;
    private boolean maariteltyPohjassa;
    private Integer jarjestys;
    private OppiaineTyyppi tyyppi;
    private String laajuus;
    private boolean koosteinen;
    private LokalisoituTekstiDto nimi;
    private Boolean abstrakti;
    private TekstiosaDto tehtava;
    private TekstiosaDto tavoitteet;
    private TekstiosaDto arviointi;
    private Map<LukiokurssiTyyppi, Optional<LokalisoituTekstiDto>> kurssiTyyppiKuvaukset = new HashMap<>();
    private List<LukioOppiaineListausDto> oppimaarat = new ArrayList<>();
    private List<LukioOppiaineListausDto> pohjanTarjonta = new ArrayList<>();
    private String koodiUri;
    private String koodiArvo;
    private String kieliKoodiUri;
    private String kieliKoodiArvo;
    private LokalisoituTekstiDto kieli;
    private List<LukiokurssiOpsDto> kurssit = new ArrayList<>();

    @Override
    public Stream<? extends PerusteeseenViittaava<?>> viittaukset() {
        return Stream.concat(Stream.concat(oppimaarat.stream(), kurssit.stream()), pohjanTarjonta.stream());
    }
}
