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
package fi.vm.sade.eperusteet.ylops.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import fi.vm.sade.eperusteet.ylops.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.ylops.service.external.OrganisaatioService;
import fi.vm.sade.eperusteet.ylops.service.util.RestClientFactory;
import fi.vm.sade.generic.rest.CachingRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.stream.StreamSupport;

/**
 *
 * @author mikkom
 */
@Service
@Transactional
public class OrganisaatioServiceImpl implements OrganisaatioService {

    @Value("${cas.service.organisaatio-service:''}")
    private String serviceUrl;

    private static final String ORGANISAATIOT = "/rest/organisaatio/";
    private static final String ORGANISAATIORYHMAT = ORGANISAATIOT + "1.2.246.562.10.00000000001/ryhmat";
    private static final String HIERARKIA_HAKU = "v2/hierarkia/hae?";
    private static final String KUNTA_KRITEERI_ID = "kunta";
    private static final String PERUSKOULU_HAKU =
            "&aktiiviset=true&suunnitellut=true&lakkautetut=false" +
            "&oppilaitostyyppi=oppilaitostyyppi_11%23*&organisaatiotyyppi=Oppilaitos";

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    RestClientFactory restClientFactory;

    @Override
    public JsonNode getPeruskoulut(String kuntaId) {
        CachingRestClient crc = restClientFactory.get(serviceUrl);
        String url = serviceUrl + ORGANISAATIOT + HIERARKIA_HAKU + KUNTA_KRITEERI_ID + "=" + kuntaId + PERUSKOULU_HAKU;
        try {
            return mapper.readTree(crc.getAsString(url));
        } catch (IOException ex) {
            throw new BusinessRuleViolationException("Peruskoulujen tietojen hakeminen epäonnistui", ex);
        }
    }

    @Override
    public JsonNode getRyhma(String organisaatioOid) {
        CachingRestClient crc = restClientFactory.get(serviceUrl);
        try {
            String url = serviceUrl + ORGANISAATIOT + organisaatioOid;
            return mapper.readTree(crc.getAsString(url));
        } catch (IOException ex) {
            throw new BusinessRuleViolationException("Työryhmän tietojen hakeminen epäonnistui", ex);
        }
    }

    @Override
    public JsonNode getRyhmat() {
        CachingRestClient crc = restClientFactory.get(serviceUrl);
        try {
            String url = serviceUrl + ORGANISAATIORYHMAT;
            JsonNode tree = mapper.readTree(crc.getAsString(url));
            ArrayNode response = JsonNodeFactory.instance.arrayNode();

            StreamSupport.stream(tree.spliterator(), false)
                         .map(ryhma -> ryhma.get("kayttoryhmat"))
                         .filter(kayttoryhma -> "perusteiden_laadinta".equals(kayttoryhma.asText()))
                         .findFirst()
                         .ifPresent(response::add);

            return response;
        } catch (IOException ex) {
            throw new BusinessRuleViolationException("Työryhmätietojen hakeminen epäonnistui", ex);
        }
    }
}
