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
package fi.vm.sade.eperusteet.ylops.service.external.impl;

import com.fasterxml.jackson.databind.JsonNode;
import fi.vm.sade.eperusteet.ylops.dto.kayttaja.KayttajanTietoDto;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jhyoty
 */
final class KayttajanTietoParser {

    private KayttajanTietoParser() {
        //apuluokka
    }

    private static String getField(JsonNode json, String... fields) {
        for (String field : fields) {
            if (json != null) {
                json = json.get(field);
            } else {
                return null;
            }
        }
        return json != null ? json.asText() : null;
    }

    public static KayttajanTietoDto parsiKayttaja(JsonNode json) {
        KayttajanTietoDto ktd = new KayttajanTietoDto(getField(json, "oidHenkilo"));
        ktd.setKayttajanimi(getField(json, "kayttajatiedot", "username"));
        ktd.setEtunimet(getField(json, "etunimet"));
        ktd.setKieliKoodi(getField(json, "asiointiKieli", "kieliKoodi"));
        ktd.setKutsumanimi(getField(json, "kutsumanimi"));
        ktd.setSukunimi(getField(json, "sukunimi"));
        ktd.setYhteystiedot(json.get("yhteystiedotRyhma"));
        return ktd;
    }

    public static List<KayttajanTietoDto> parsiKayttajat(JsonNode jsonList) {
        return Stream.of(jsonList)
                .map(KayttajanTietoParser::parsiKayttaja)
                .collect(Collectors.toList());
    }

}
