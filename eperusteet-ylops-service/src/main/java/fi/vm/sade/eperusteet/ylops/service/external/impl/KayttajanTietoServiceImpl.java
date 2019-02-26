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
import fi.vm.sade.eperusteet.ylops.dto.kayttaja.KayttajanProjektitiedotDto;
import fi.vm.sade.eperusteet.ylops.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.ylops.service.external.KayttajanTietoService;
import fi.vm.sade.eperusteet.ylops.service.util.RestClientFactory;
import fi.vm.sade.eperusteet.ylops.service.util.SecurityUtil;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpRequest;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.Future;

import static fi.vm.sade.eperusteet.ylops.service.external.impl.KayttajanTietoParser.parsiKayttaja;
import static fi.vm.sade.eperusteet.ylops.service.security.PermissionEvaluator.RolePermission.ADMIN;
import static fi.vm.sade.eperusteet.ylops.service.security.PermissionEvaluator.RolePermission.CRUD;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

/**
 * @author mikkom
 */
@Service
public class KayttajanTietoServiceImpl implements KayttajanTietoService {

    @Autowired
    private KayttajaClient client;

    @Override
    public KayttajanTietoDto hae(String oid) {
        return client.hae(oid);
    }

    @Override
    public String haeKayttajanimi(String oid) {
        if (oid != null) {
            KayttajanTietoDto tiedot = client.hae(oid);
            if (tiedot != null) {
                String nimi = (tiedot.getEtunimet() != null) ? tiedot.getEtunimet() : "";
                nimi += " " + ((tiedot.getSukunimi() != null) ? tiedot.getSukunimi() : "");
                return nimi;
            }
        }
        return null;
    }

    @Override
    @Async
    public Future<KayttajanTietoDto> haeAsync(String oid) {
        return new AsyncResult<>(hae(oid));
    }

    @Override
    public KayttajanTietoDto haeKirjautaunutKayttaja() {
        Principal ap = SecurityUtil.getAuthenticatedPrincipal();
        KayttajanTietoDto kayttaja = hae(ap.getName());
        if (kayttaja == null) { //"fallback" jos integraatio on rikki eikä löydä käyttäjän tietoja
            kayttaja = new KayttajanTietoDto(ap.getName());
        }
        return kayttaja;
    }

    @Override
    public List<KayttajanProjektitiedotDto> haeOpetussuunnitelmat(String oid) {
        // TODO
        throw new NotImplementedException();
    }

    @Override
    public KayttajanProjektitiedotDto haeOpetussuunnitelma(String oid, Long opsId) {
        // TODO
        throw new NotImplementedException();
    }

    @Component
    public static class KayttajaClient {

        @Autowired
        private RestClientFactory restClientFactory;

        @Value("${cas.service.oppijanumerorekisteri-service:''}")
        private String onrServiceUrl;

        private static final String HENKILO_API = "/henkilo/";
        private final ObjectMapper mapper = new ObjectMapper();

        @Cacheable("kayttajat")
        public KayttajanTietoDto hae(String oid) {
            OphHttpClient client = restClientFactory.get(onrServiceUrl, true);
            String url = onrServiceUrl + HENKILO_API + oid;

            OphHttpRequest request = OphHttpRequest.Builder
                    .get(url)
                    .build();

            try {
                return client.<KayttajanTietoDto>execute(request)
                        .handleErrorStatus(SC_UNAUTHORIZED, SC_FORBIDDEN)
                        .with((res) -> Optional.of(new KayttajanTietoDto(oid)))
                        .expectedStatus(SC_OK)
                        .mapWith(text -> {
                            try {
                                JsonNode json = mapper.readTree(text);
                                return parsiKayttaja(json);
                            } catch (IOException e) {
                                return new KayttajanTietoDto(oid);
                            }
                        })
                        .orElse(new KayttajanTietoDto(oid));
            }
            catch (RuntimeException ex) {
                return new KayttajanTietoDto(oid);
            }
        }
    }

    @Override
    public Set<String> haeOrganisaatioOikeudet() {
        return SecurityUtil.getOrganizations(new HashSet<>(Arrays.asList(ADMIN, CRUD)));
    }

}
