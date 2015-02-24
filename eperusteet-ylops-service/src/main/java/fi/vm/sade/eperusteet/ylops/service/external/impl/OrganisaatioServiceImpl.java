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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import fi.vm.sade.eperusteet.ylops.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.ylops.service.external.OrganisaatioService;
import fi.vm.sade.eperusteet.ylops.service.util.RestClientFactory;
import fi.vm.sade.generic.rest.CachingRestClient;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 *
 * @author mikkom
 */
@Service
public class OrganisaatioServiceImpl implements OrganisaatioService {

    @Value("${cas.service.organisaatio-service:''}")
    private String serviceUrl;

    @Value("#{'${fi.vm.sade.eperusteet.ylops.organisaatio-service.peruskoulu-oppilaitostyypit}'.split(',')}")
    private List<String> oppilaitostyypit;

    private static final String ORGANISAATIOT = "/rest/organisaatio/";
    private static final String ORGANISAATIORYHMAT = ORGANISAATIOT + "1.2.246.562.10.00000000001/ryhmat";
    private static final String HIERARKIA_HAKU = "v2/hierarkia/hae?";
    private static final String KUNTA_KRITEERI = "kunta=";
    private static final String STATUS_KRITEERI = "&aktiiviset=true&suunnitellut=true&lakkautetut=false";
    private static final String ORGANISAATIO_KRITEERI = "oidRestrictionList=";

    private String peruskouluHakuehto;

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    RestClientFactory restClientFactory;

    @PostConstruct
    public void init() {
        peruskouluHakuehto =
            "&organisaatiotyyppi=Oppilaitos" +
            oppilaitostyypit.stream()
                            .reduce("", (acc, t) -> acc + "&oppilaitostyyppi=oppilaitostyyppi_" + t + "%23*");
    }

    @Override
    @Cacheable("organisaatiot")
    public JsonNode getOrganisaatio(String organisaatioOid) {
        CachingRestClient crc = restClientFactory.get(serviceUrl);
        String url = serviceUrl + ORGANISAATIOT + organisaatioOid;
        try {
            return mapper.readTree(crc.getAsString(url));
        } catch (IOException ex) {
            throw new BusinessRuleViolationException("Organisaation tietojen hakeminen epäonnistui", ex);
        }
    }

    private JsonNode getPeruskoulut(String hakuehto) {
        CachingRestClient crc = restClientFactory.get(serviceUrl);

        try {
            final String url =
                serviceUrl + ORGANISAATIOT + HIERARKIA_HAKU + hakuehto + STATUS_KRITEERI + peruskouluHakuehto;
            JsonNode tree = mapper.readTree(crc.getAsString(url));
            JsonNode organisaatioTree = tree.get("organisaatiot");
            return flattenTree(organisaatioTree, "children",
                               node -> node.get("oppilaitostyyppi") != null &&
                                       oppilaitostyypit.stream()
                                                       .map(t -> "oppilaitostyyppi_" + t + "#1")
                                                       .anyMatch(s -> s.equals(node.get("oppilaitostyyppi").asText())));
        } catch (IOException ex) {
            throw new BusinessRuleViolationException("Peruskoulujen tietojen hakeminen epäonnistui", ex);
        }
    }

    @Override
    @Cacheable("organisaatiot")
    public JsonNode getPeruskoulutByKuntaId(String kuntaId) {
        return getPeruskoulut(KUNTA_KRITEERI + kuntaId);
    }

    @Override
    @Cacheable("organisaatiot")
    public JsonNode getPeruskoulutByOid(String oid) {
        return getPeruskoulut(ORGANISAATIO_KRITEERI + oid);
    }

    @Override
    @Cacheable("organisaatiot")
    public JsonNode getPeruskoulutoimijat(List<String> kuntaIdt) {
        Set<String> toimijaOidit =
            kuntaIdt.stream()
                    .flatMap(kuntaId ->
                                 StreamSupport.stream(getPeruskoulutByKuntaId(kuntaId).spliterator(), false)
                                              .map(koulu -> koulu.get("parentOid").asText()))
                    .collect(Collectors.toCollection(LinkedHashSet::new));

        ArrayNode toimijat = JsonNodeFactory.instance.arrayNode();
        toimijaOidit.stream().map(this::getOrganisaatio).forEach(toimijat::add);
        return toimijat;
    }

    private ArrayNode flattenTree(JsonNode tree, String childTreeName, Predicate<JsonNode> filter) {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        if (tree != null) {
            tree.forEach(node -> {
                if (filter.test(node)) {
                    array.add(node);
                }
                JsonNode childTree = node.get(childTreeName);
                array.addAll(flattenTree(childTree, childTreeName, filter));
            });
        }
        return array;
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
