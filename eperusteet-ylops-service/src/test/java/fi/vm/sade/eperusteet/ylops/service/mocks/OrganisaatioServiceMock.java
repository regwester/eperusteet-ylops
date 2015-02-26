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

import com.fasterxml.jackson.databind.JsonNode;
import fi.vm.sade.eperusteet.ylops.service.external.OrganisaatioService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * @author mikkom
 */
@Service
public class OrganisaatioServiceMock implements OrganisaatioService {
    @Override
    public JsonNode getOrganisaatio(String organisaatioOid) {
        return null;
    }

    @Override
    public JsonNode getPeruskoulutByKuntaId(String kuntaId) {
        return null;
    }

    @Override
    public JsonNode getPeruskoulutByOid(String oid) {
        return null;
    }

    @Override
    public JsonNode getPeruskoulutoimijat(List<String> kuntaIdt) {
        return null;
    }
}
