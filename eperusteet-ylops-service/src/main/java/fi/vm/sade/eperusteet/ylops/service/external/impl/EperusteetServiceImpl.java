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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import fi.vm.sade.eperusteet.ylops.dto.eperusteet.PerusopetusPerusteKaikkiDto;
import fi.vm.sade.eperusteet.ylops.dto.eperusteet.PerusteInfoDto;
import fi.vm.sade.eperusteet.ylops.resource.config.ReferenceNamingStrategy;
import fi.vm.sade.eperusteet.ylops.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.ylops.service.external.EperusteetService;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
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
    @Value("${fi.vm.sade.eperusteet.ylops.koulutustyyppi_perusopetus:koulutustyyppi_16}")
    private String koulutustyyppiPerusopetus;

    private final RestTemplate client;

    public EperusteetServiceImpl() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.getObjectMapper().enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        converter.getObjectMapper().enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        converter.getObjectMapper().registerModule(new Jdk8Module());
        converter.getObjectMapper().setPropertyNamingStrategy(new ReferenceNamingStrategy());
        client = new RestTemplate(Arrays.asList(converter));
    }


    @Override
    public List<PerusteInfoDto> findPerusopetuksenPerusteet() {
        PerusteInfoDto[] kaikki = client.getForObject(koodistoServiceUrl + "/api/perusteet/perusopetus", PerusteInfoDto[].class);
        return Arrays.asList(kaikki);
    }

    @Override
    public PerusopetusPerusteKaikkiDto getPerusopetuksenPeruste(final Long id) {
        PerusopetusPerusteKaikkiDto peruste = client.getForObject(koodistoServiceUrl + "/api/perusteet/{id}/kaikki", PerusopetusPerusteKaikkiDto.class, id);

        if (peruste == null || !koulutustyyppiPerusopetus.equals(peruste.getKoulutustyyppi())) {
            throw new BusinessRuleViolationException("Pyydetty peruste ei ole oikeaa tyyppiä");
        }
        return peruste;
    }

    @Override
    public JsonNode getTiedotteet(Long jalkeen) {
        String params = "";
        if (jalkeen != null) {
            params = "?alkaen=" + String.valueOf(jalkeen);
        }
        JsonNode tiedotteet = client.getForObject(koodistoServiceUrl + "/api/tiedotteet" + params, JsonNode.class);
        return tiedotteet;
    }
}
