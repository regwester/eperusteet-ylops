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
package fi.vm.sade.eperusteet.ylops.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import fi.vm.sade.eperusteet.ylops.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.ylops.dto.peruste.Peruste;
import fi.vm.sade.eperusteet.ylops.dto.peruste.PerusteInfo;
import java.util.List;
import java.util.Set;

/**
 *
 * @author nkala
 */
public interface EperusteetService {
    Peruste getPeruste(String diaariNumero);
    Peruste getPerusteUpdateCache(String diaarinumero);
    List<PerusteInfo> findPerusteet();
    List<PerusteInfo> findPerusteet(Set<KoulutusTyyppi> tyypit);
    List<PerusteInfo> findPerusopetuksenPerusteet();
    List<PerusteInfo> findLukiokoulutusPerusteet();
    Peruste getPerusopetuksenPeruste(final Long id);
    Peruste getLukiokoulutuksenPeruste(final Long id);
    JsonNode getTiedotteet(Long jalkeen);
}
