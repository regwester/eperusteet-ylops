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
import fi.vm.sade.generic.rest.CachingRestClient;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import static fi.vm.sade.eperusteet.ylops.service.external.impl.KayttajanTietoParser.parsiKayttaja;

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
    @Async
    public Future<KayttajanTietoDto> haeAsync(String oid) {
        return new AsyncResult<>(hae(oid));
    }

    @Override
    public KayttajanTietoDto haeKirjautaunutKayttaja() {
        KayttajanTietoDto kayttaja = hae(SecurityUtil.getAuthenticatedPrincipal().getName());
        if ( kayttaja == null ) {
            //"fallback" jos integraatio on rikki eikä löydä käyttäjän tietoja
            kayttaja =  new KayttajanTietoDto();
            kayttaja.setOidHenkilo(SecurityUtil.getAuthenticatedPrincipal().getName());
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
        @Value("${cas.service.authentication-service:''}")
        private String serviceUrl;

        private static final String KAYTTAJA_API = "/resources/henkilo/";
        private static final String OMAT_TIEDOT_API = "/resources/omattiedot/";
        private final ObjectMapper mapper = new ObjectMapper();

        @Cacheable("kayttajat")
        public KayttajanTietoDto hae(String oid) {
            CachingRestClient crc = restClientFactory.get(serviceUrl);

            try {
                String url = serviceUrl + (oid == null ? OMAT_TIEDOT_API : KAYTTAJA_API + oid);
                JsonNode json = mapper.readTree(crc.getAsString(url));
                return parsiKayttaja(json);
            } catch (IOException e) {
                return null;
            }
        }
    }
}
