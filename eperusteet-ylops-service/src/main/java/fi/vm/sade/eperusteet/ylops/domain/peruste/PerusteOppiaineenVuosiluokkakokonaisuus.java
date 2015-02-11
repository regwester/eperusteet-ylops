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
package fi.vm.sade.eperusteet.ylops.domain.peruste;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.vm.sade.eperusteet.ylops.dto.ReferenceableDto;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author jhyoty
 */
@Getter
@Setter
public class PerusteOppiaineenVuosiluokkakokonaisuus implements ReferenceableDto {
    private Long id;
    @JsonIdentityReference(alwaysAsId = true)
    @JsonProperty("_vuosiluokkaKokonaisuus")
    private PerusteVuosiluokkakokonaisuus vuosiluokkaKokonaisuus;
    private PerusteTekstiOsa tehtava;
    private PerusteTekstiOsa tyotavat;
    private PerusteTekstiOsa ohjaus;
    private PerusteTekstiOsa arviointi;
    private List<PerusteOpetuksentavoite> tavoitteet;
    private List<PerusteKeskeinensisaltoalue> sisaltoalueet;

    public Optional<PerusteOpetuksentavoite> getTavoite(UUID tunniste) {
        return tavoitteet.stream()
            .filter(t -> t.getTunniste().equals(tunniste))
            .findAny();
    }
}
