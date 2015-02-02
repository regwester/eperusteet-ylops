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
import fi.vm.sade.eperusteet.ylops.dto.eperusteet.PerusopetuksenPerusteenSisaltoDto;
import fi.vm.sade.eperusteet.ylops.dto.eperusteet.PerusopetusPerusteKaikkiDto;
import fi.vm.sade.eperusteet.ylops.dto.eperusteet.PerusteInfoDto;
import fi.vm.sade.eperusteet.ylops.service.external.EperusteetService;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 *
 * @author nkala
 */
@Service
public class EperusteetServiceMock implements EperusteetService {

    @Override
    public List<PerusteInfoDto> perusopetuksenPerusteet() {
        return Collections.singletonList(new PerusteInfoDto());
    }

    @Override
    public PerusopetusPerusteKaikkiDto perusopetuksenPeruste(Long id) {
        PerusopetusPerusteKaikkiDto peruste = new PerusopetusPerusteKaikkiDto();
        PerusopetuksenPerusteenSisaltoDto sisalto = new PerusopetuksenPerusteenSisaltoDto();
        sisalto.setOppiaineet(Collections.emptySet());
        peruste.setPerusopetuksenPerusteenSisalto(sisalto);
        return peruste;
    }

    @Override
    public JsonNode tiedotteet(Long jalkeen) {
        return null;
    }

}
