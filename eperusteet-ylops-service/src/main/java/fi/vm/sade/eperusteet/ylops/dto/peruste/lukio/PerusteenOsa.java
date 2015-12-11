/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.ylops.dto.peruste.lukio;

import fi.vm.sade.eperusteet.ylops.dto.lukio.PerusteeseenViittaava;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * User: tommiratamaa
 * Date: 19.11.2015
 * Time: 14.57
 */
public interface PerusteenOsa {
    UUID getTunniste();

    default Stream<? extends PerusteenOsa> osat() {
        return Stream.empty();
    }

    default Stream<? extends PerusteenOsa> osineen() {
        return Stream.concat(Stream.of(this), osat()
                .filter(o -> o != null).flatMap(PerusteenOsa::osineen));
    }

    static<T extends PerusteenOsa, OpsT extends PerusteeseenViittaava<T>> void map(T perusteen, OpsT paikallinen) {
        if (perusteen != null && paikallinen != null) {
            Map<UUID, PerusteenOsa> perusteenOsat;
            perusteenOsat = perusteen.osineen()
                    .filter(o -> o != null && o.getTunniste() != null)
                    .map(o -> (PerusteenOsa) o) // <-- required for javac /compiles without in Idea)
                    .collect(toMap(PerusteenOsa::getTunniste, o -> o,  (o1, o2) -> o1));
            paikallinen.viittauksineen().filter(p -> p != null && p.getTunniste() != null)
                    .forEach(p -> {
                        //noinspection unchecked
                        PerusteeseenViittaava viittaava = p;
                        PerusteenOsa osa = perusteenOsat.get(p.getTunniste());
                        if (osa != null) {
                            //noinspection unchecked
                            viittaava.setPerusteen(osa);
                        }
                    });
        }
    }

}
