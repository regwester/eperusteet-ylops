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

import fi.vm.sade.eperusteet.ylops.service.util.Nulls;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author nkala
 */
@Getter
@Setter
public class PerusopetuksenPerusteenSisalto implements Serializable {

    private PerusteTekstiKappaleViite sisalto;
    private Set<PerusteLaajaalainenosaaminen> laajaalaisetosaamiset;
    private Set<PerusteVuosiluokkakokonaisuus> vuosiluokkakokonaisuudet;
    private Set<PerusteOppiaine> oppiaineet;

    public Optional<PerusteOppiaine> getOppiaine(UUID tunniste) {
        return oppiaineet.stream()
            .flatMap(oa -> Stream.concat(Stream.of(oa), Nulls.nullToEmpty(oa.getOppimaarat()).stream()))
            .filter(oa -> Objects.equals(oa.getTunniste(), tunniste))
            .findAny();

    }
}
