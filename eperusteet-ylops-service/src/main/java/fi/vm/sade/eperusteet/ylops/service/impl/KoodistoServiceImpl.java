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

import fi.vm.sade.eperusteet.ylops.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.ylops.service.external.KoodistoService;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 *
 * @author mikkom
 */
@Service
@Profile(value = "default")
public class KoodistoServiceImpl implements KoodistoService {

    @Value("https://virkailija.opintopolku.fi/koodisto-service")
    private String koodistoServiceUrl;

    private static final String KOODISTO_API = "/rest/json/";
    private static final String YLARELAATIO = "relaatio/sisaltyy-ylakoodit/";
    private static final String ALARELAATIO = "relaatio/sisaltyy-alakoodit/";

    @Autowired
    private DtoMapper mapper;

    @Override
    @Cacheable("koodistot")
    public List<KoodistoKoodiDto> getAll(String koodisto) {
        RestTemplate restTemplate = new RestTemplate();
        String url = koodistoServiceUrl + KOODISTO_API + koodisto + "/koodi/";
        KoodistoKoodiDto[] koodistot = restTemplate.getForObject(url, KoodistoKoodiDto[].class);
        return mapper.mapAsList(Arrays.asList(koodistot), KoodistoKoodiDto.class);
    }

    @Override
    @Cacheable("koodistot")
    public KoodistoKoodiDto get(String koodisto, String koodi) {
        RestTemplate restTemplate = new RestTemplate();
        String url = koodistoServiceUrl + KOODISTO_API + koodisto + "/koodi/" + koodi;
        return restTemplate.getForObject(url, KoodistoKoodiDto.class);
    }

    @Override
    @Cacheable("koodistot")
    public List<KoodistoKoodiDto> filterBy(String koodisto, String haku) {
        List<KoodistoKoodiDto> filter = getAll(koodisto);
        List<KoodistoKoodiDto> tulos = new ArrayList<>();

        Predicate<KoodistoKoodiDto> matches =
                x -> x.getKoodiUri().contains(haku) ||
                     Arrays.stream(x.getMetadata())
                           .anyMatch(y -> y.getNimi().toLowerCase().contains(haku.toLowerCase()));

        filter.stream()
              .filter(matches)
              .forEach(tulos::add);
        return tulos;
    }

    @Override
    @Cacheable("koodistot")
    public List<KoodistoKoodiDto> getAlarelaatio(String koodi) {
        RestTemplate restTemplate = new RestTemplate();
        String url = koodistoServiceUrl + KOODISTO_API + ALARELAATIO + koodi;
        KoodistoKoodiDto[] koodistot = restTemplate.getForObject(url, KoodistoKoodiDto[].class);
        return mapper.mapAsList(Arrays.asList(koodistot), KoodistoKoodiDto.class);
    }

    @Override
    @Cacheable("koodistot")
    public List<KoodistoKoodiDto> getYlarelaatio(String koodi) {
        RestTemplate restTemplate = new RestTemplate();
        String url = koodistoServiceUrl + KOODISTO_API + YLARELAATIO + koodi;
        KoodistoKoodiDto[] koodistot = restTemplate.getForObject(url, KoodistoKoodiDto[].class);
        return mapper.mapAsList(Arrays.asList(koodistot), KoodistoKoodiDto.class);
    }
}
