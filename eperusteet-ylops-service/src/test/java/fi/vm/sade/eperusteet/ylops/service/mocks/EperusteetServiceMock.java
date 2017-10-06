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
package fi.vm.sade.eperusteet.ylops.service.mocks;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import fi.vm.sade.eperusteet.ylops.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusopetuksenPerusteenSisaltoDto;
import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusteInfoDto;
import fi.vm.sade.eperusteet.ylops.resource.config.ReferenceNamingStrategy;
import fi.vm.sade.eperusteet.ylops.service.external.EperusteetService;
import fi.vm.sade.eperusteet.ylops.service.external.impl.perustedto.EperusteetPerusteDto;
import fi.vm.sade.eperusteet.ylops.service.mapping.DtoMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author nkala
 */
@Service
@SuppressWarnings("TransactionalAnnotations")
public class EperusteetServiceMock implements EperusteetService {

    public static final String DIAARINUMERO = "mock-diaarinumero";
    private EperusteetPerusteDto perusteDto = null;

    @Autowired
    private DtoMapper mapper;

    @Override
    public List<PerusteInfoDto> findPerusteet(Set<KoulutusTyyppi> tyypit) {
        PerusteInfoDto perusteInfo = new PerusteInfoDto();
        perusteInfo.setDiaarinumero(DIAARINUMERO);
        return Collections.singletonList(perusteInfo);
    }

    @Override
    public List<PerusteInfoDto> findPerusteet() {
        return findPerusteet(null);
    }

    @Override
    public List<PerusteInfoDto> findPerusopetuksenPerusteet() {
        PerusteInfoDto perusteInfo = new PerusteInfoDto();
        perusteInfo.setDiaarinumero(DIAARINUMERO);
        return Collections.singletonList(perusteInfo);
    }

    @Override
    public List<PerusteInfoDto> findLukiokoulutusPerusteet() {
        PerusteInfoDto perusteInfo = new PerusteInfoDto();
        perusteInfo.setDiaarinumero(DIAARINUMERO);
        return Collections.singletonList(perusteInfo);
    }

    @Override
    public PerusteDto getEperusteetPeruste(Long id) {
        PerusteDto peruste = new PerusteDto();
        peruste.setId(id);
        peruste.setDiaarinumero(DIAARINUMERO);
        PerusopetuksenPerusteenSisaltoDto sisalto = new PerusopetuksenPerusteenSisaltoDto();
        sisalto.setOppiaineet(Collections.emptySet());
        peruste.setPerusopetus(sisalto);
        return peruste;
    }

    @Override
    public PerusteDto getPeruste(String diaariNumero) {
        return getPerusteByDiaari(diaariNumero);
    }

    @Override
    public PerusteDto getPerusteUpdateCache(String diaarinumero) {
        return getPerusteByDiaari(diaarinumero);
    }

    private PerusteDto getPerusteByDiaari(String diaariNumero) {
        if (perusteDto != null && diaariNumero.equals(perusteDto.getDiaarinumero())) {
            return mapper.map(perusteDto, PerusteDto.class);
        }
        return getEperusteetPeruste(0L);
    }

    @Override
    public JsonNode getTiedotteet(Long jalkeen) {
        return null;
    }

    public void setPeruste(InputStream is) throws IOException {
        if (is == null) {
            perusteDto = null;
        } else {
            final ObjectMapper om = new ObjectMapper();
            om.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
            om.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
            om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            om.registerModule(new Jdk8Module());
            om.setPropertyNamingStrategy(new ReferenceNamingStrategy());
            perusteDto = om.readValue(is, EperusteetPerusteDto.class);
        }
    }

}
