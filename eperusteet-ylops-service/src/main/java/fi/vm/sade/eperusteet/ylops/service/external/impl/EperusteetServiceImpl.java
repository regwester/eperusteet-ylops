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
import fi.vm.sade.eperusteet.ylops.domain.peruste.Peruste;
import fi.vm.sade.eperusteet.ylops.domain.peruste.PerusteInfo;
import fi.vm.sade.eperusteet.ylops.domain.peruste.PerusteInfoWrapperDto;
import fi.vm.sade.eperusteet.ylops.resource.config.ReferenceNamingStrategy;
import fi.vm.sade.eperusteet.ylops.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.ylops.service.external.EperusteetService;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
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

    @Autowired
    private DtoMapper mapper;

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
    public List<PerusteInfo> findPerusopetuksenPerusteet() {
        PerusteInfoWrapperDto wrapperDto
            = client.getForObject(koodistoServiceUrl + "/api/perusteet?tyyppi=koulutustyyppi_16&sivukoko=100",
                                  PerusteInfoWrapperDto.class);

        // Filtteröi pois perusteet jotka eivät enää ole voimassa
        Date now = new Date();
        return wrapperDto.getData().stream()
            .filter(peruste -> peruste.getVoimassaoloLoppuu() == null || peruste.getVoimassaoloLoppuu().after(now))
            .collect(Collectors.toList());
    }

    @Override
    public Peruste getPerusopetuksenPeruste(final Long id) {
        fi.vm.sade.eperusteet.ylops.service.external.impl.perustedto.PerusopetusPerusteKaikkiDto peruste = client.getForObject(koodistoServiceUrl
            + "/api/perusteet/{id}/kaikki", fi.vm.sade.eperusteet.ylops.service.external.impl.perustedto.PerusopetusPerusteKaikkiDto.class, id);

        if (peruste == null || !koulutustyyppiPerusopetus.equals(peruste.getKoulutustyyppi())) {
            throw new BusinessRuleViolationException("Pyydetty peruste ei ole oikeaa tyyppiä");
        }

        return mapper.map(peruste, Peruste.class);
    }

    //TODO, ei ole lopullinen rajapinta. Haku diaarinumeron perusteella?
    @Override
    @Cacheable("perusteet")
    public Peruste getPerusopetuksenPeruste() {

        // TODO: Paree olisi jos eperusteetService palauttaisi suoraan uusimman perusteen
        PerusteInfo perusteInfoDto = findPerusopetuksenPerusteet().stream()
            .max(Comparator.comparingLong(p -> Optional.ofNullable(p.getVoimassaoloLoppuu()).orElse(new Date(Long.MAX_VALUE)).getTime()))
            .orElseThrow(() -> new BusinessRuleViolationException("Perusopetuksen perustetta ei löytynyt"));

        return getPerusopetuksenPeruste(perusteInfoDto.getId());
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
