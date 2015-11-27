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

package fi.vm.sade.eperusteet.ylops.dto.lukio;

import fi.vm.sade.eperusteet.ylops.dto.peruste.lukio.PerusteenOsa;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.toMap;

/**
 * User: tommiratamaa
 * Date: 19.11.2015
 * Time: 14.26
 */
@Getter
public abstract class PerusteOpsDto<T extends PerusteenOsa, OpsT extends PerusteeseenViittaava<T>>
            implements PerusteeseenViittaava<T>, PerusteenOsa {
    private T perusteen;
    private OpsT paikallinen;
    @Setter
    private String kommentti; // tallennusta varten

    public PerusteOpsDto(T perusteen, OpsT paikallinen) {
        this.perusteen = perusteen;
        this.paikallinen = paikallinen;
        map();
    }

    private void map() {
        if (this.perusteen != null && this.paikallinen != null) {
            Map<UUID, PerusteenOsa> perusteenOsat = this.perusteen.osineen()
                .filter(o -> o != null && o.getTunniste() != null)
                    .map(o -> (PerusteenOsa) o) // <-- required for javac /compiles without in Idea)
                .collect(toMap(PerusteenOsa::getTunniste, o -> o));
            this.paikallinen.viittauksineen().filter(p -> p != null && p.getTunniste() != null)
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

    public PerusteOpsDto(OpsT paikallinen) {
        this.paikallinen = paikallinen;
    }

    @Override
    public void setPerusteen(T vastaava) {
        this.perusteen = vastaava;
        map();
    }

    public void setPaikallinen(OpsT paikallinen) {
        this.paikallinen = paikallinen;
        map();
    }

    @Override
    public UUID getTunniste() {
        if (perusteen != null) {
            return perusteen.getTunniste();
        }
        if (paikallinen != null) {
            return paikallinen.getTunniste();
        }
        return null;
    }
}
