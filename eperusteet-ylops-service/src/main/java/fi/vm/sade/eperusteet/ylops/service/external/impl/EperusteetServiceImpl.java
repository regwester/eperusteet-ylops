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
import fi.vm.sade.eperusteet.ylops.service.external.EperusteetService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author nkala
 */
@Service
@Profile(value = "default")
public class EperusteetServiceImpl implements EperusteetService {
    @Value("${fi.vm.sade.eperusteet.ylops.eperusteet-service: ''}")
    private String koodistoServiceUrl;

    private final RestTemplate client = new RestTemplate();

    @Override
    public JsonNode perusopetuksenPerusteet() {
        JsonNode perusteet = client.getForObject(koodistoServiceUrl + "/api/perusteet/perusopetus", JsonNode.class);
        return perusteet;
    }

    @Override
    public JsonNode perusopetuksenPeruste(final Long id) {
        JsonNode peruste = client.getForObject(koodistoServiceUrl + "/api/perusteet/perusopetus/" + id.toString() + "/kaikki", JsonNode.class);
        return peruste;
    }

    @Override
    public JsonNode tiedotteet(Long jalkeen) {
        String params = "";
        if (jalkeen != null) {
            params = "?alkaen=" + String.valueOf(jalkeen);
        }
        JsonNode tiedotteet = client.getForObject(koodistoServiceUrl + "/api/tiedotteet" + params, JsonNode.class);
        return tiedotteet;
    }
}