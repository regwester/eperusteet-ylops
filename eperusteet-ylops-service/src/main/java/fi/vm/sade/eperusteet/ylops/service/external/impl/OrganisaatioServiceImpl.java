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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 *
 * @author mikkom
 */
@Service
public class OrganisaatioServiceImpl implements OrganisaatioService {

    private static final String ORGANISAATIOT = "/rest/organisaatio/";
    private static final String HIERARKIA_HAKU = "v2/hierarkia/hae?";
    private static final String KUNTA_KRITEERI = "kunta=";
    private static final String STATUS_KRITEERI = "&aktiiviset=true&suunnitellut=true&lakkautetut=false";
    private static final String ORGANISAATIO_KRITEERI = "oidRestrictionList=";

    @Autowired
    private Client client;

    @Component
    public static class Client {
        @Autowired
        RestClientFactory restClientFactory;

        @Value("${cas.service.organisaatio-service:''}")
        private String serviceUrl;

        @Value("#{'${fi.vm.sade.eperusteet.ylops.organisaatio-service.peruskoulu-oppilaitostyypit}'.split(',')}")
        private List<String> oppilaitostyypit;

        @Value("#{'${fi.vm.sade.eperusteet.ylops.organisaatio-service.lukio-oppilaitostyypit}'.split(',')}")
        private List<String> lukioOppilaitostyypit;

        private static final Logger LOG = LoggerFactory.getLogger(Client.class);

        private final ObjectMapper mapper = new ObjectMapper();

        private String peruskouluHakuehto;
        private String lukioHakuehto;

        @PostConstruct
        public void init() {
            peruskouluHakuehto = "&organisaatiotyyppi=Oppilaitos";
            lukioHakuehto = "&organisaatiotyyppi=Oppilaitos";
        }

        @Cacheable("organisaatiot")
        public JsonNode getOrganisaatio(String organisaatioOid) {
            CachingRestClient crc = restClientFactory.getWithoutCas(serviceUrl);
            String url = serviceUrl + ORGANISAATIOT + organisaatioOid;
            try {
                return mapper.readTree(crc.getAsString(url));
            } catch (IOException ex) {
                throw new BusinessRuleViolationException("Organisaation tietojen hakeminen epäonnistui", ex);
            }
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

        private JsonNode getPeruskoulut(String hakuehto) {
            return getLaitoksetByEhtoAndTyypit(hakuehto + peruskouluHakuehto, oppilaitostyypit);
        }

        private JsonNode getLukiot(String hakuehto) {
            return getLaitoksetByEhtoAndTyypit(hakuehto + lukioHakuehto, lukioOppilaitostyypit);
        }

        private JsonNode getLaitoksetByEhtoAndTyypit(String hakuehto, Collection<String> tyypit) {
            CachingRestClient crc = restClientFactory.getWithoutCas(serviceUrl);
            try {
                final String url =
                        serviceUrl + ORGANISAATIOT + HIERARKIA_HAKU + hakuehto + STATUS_KRITEERI;
                JsonNode tree = mapper.readTree(crc.getAsString(url));
                JsonNode organisaatioTree = tree.get("organisaatiot");
                return flattenTree(organisaatioTree, "children",
                        node -> node.get("oppilaitostyyppi") != null &&
                                tyypit.stream()
                                        .map(t -> "oppilaitostyyppi_" + t + "#1")
                                        .anyMatch(s -> s.equals(node.get("oppilaitostyyppi").asText())));

            } catch (IOException ex) {
                throw new BusinessRuleViolationException("Lukioiden tietojen hakeminen epäonnistui", ex);
            }
        }

        public JsonNode getPeruskoulutByKuntaId(String kuntaId) {
            return getPeruskoulut(KUNTA_KRITEERI + kuntaId);
        }

        public JsonNode getLukiotByKuntaId(String kuntaId) {
            return getLukiot(KUNTA_KRITEERI + kuntaId);
        }

        @Cacheable("organisaation-peruskoulut")
        public JsonNode getPeruskoulutByOid(String oid) {
            return getPeruskoulut(ORGANISAATIO_KRITEERI + oid);
        }

        @Cacheable("organisaation-lukiot")
        public JsonNode getLukiotByOid(String oid) {
            return getLukiot(ORGANISAATIO_KRITEERI + oid);
        }
    }

    @Override
    public JsonNode getOrganisaatio(String organisaatioOid) {
        return client.getOrganisaatio(organisaatioOid);
    }

    @Override
    public JsonNode getPeruskoulutByKuntaId(String kuntaId) {
        return client.getPeruskoulutByKuntaId(kuntaId);
    }

    @Override
    public JsonNode getLukiotByKuntaId(String kuntaId) {
        return client.getLukiotByKuntaId(kuntaId);
    }

    @Override
    public JsonNode getPeruskoulutByOid(String oid) {
        return client.getPeruskoulutByOid(oid);
    }

    @Override
    public JsonNode getPeruskoulutoimijat(List<String> kuntaIdt) {
        return getToimijatByKuntas(kuntaIdt, this::getPeruskoulutByKuntaId);
    }

    private JsonNode getToimijatByKuntas(List<String> kuntaIdt, Function<String,JsonNode> getByKuntaId) {
        Set<String> toimijaOidit =
            kuntaIdt.stream()
                    .flatMap(kuntaId ->
                                 StreamSupport.stream(getByKuntaId.apply(kuntaId).spliterator(), false)
                                              .map(koulu -> koulu.get("parentOid").asText()))
                    .collect(Collectors.toCollection(LinkedHashSet::new));

        ArrayNode toimijat = JsonNodeFactory.instance.arrayNode();
        toimijaOidit.stream().map(this::getOrganisaatio).forEach(toimijat::add);
        return toimijat;
    }

    @Override
    public JsonNode getLukioByOid(String oid) {
        return client.getLukiotByOid(oid);
    }

    @Override
    public JsonNode getLukiotoimijat(List<String> kuntaIdt) {
        return getToimijatByKuntas(kuntaIdt, this::getLukiotByKuntaId);
    }
}
