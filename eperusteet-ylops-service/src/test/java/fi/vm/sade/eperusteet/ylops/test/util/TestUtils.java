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
package fi.vm.sade.eperusteet.ylops.test.util;

import fi.vm.sade.eperusteet.ylops.domain.teksti.Kieli;
import fi.vm.sade.eperusteet.ylops.domain.teksti.LokalisoituTeksti;
import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import java.util.Collections;
import java.util.Optional;

/**
 * @author mikkom
 */
public abstract class TestUtils {
    public static LokalisoituTeksti lokalisoituTekstiOf(Kieli kieli, String teksti) {
        return LokalisoituTeksti.of(Collections.singletonMap(kieli, teksti));
    }

    static Long uniikki = (long) 0;

    static public String uniikkiString() {
        return "uniikki" + (++uniikki).toString();
    }

    static public Long uniikkiId() {
        return ++uniikki;
    }

    public static LokalisoituTekstiDto lt(String teksti) {
        return new LokalisoituTekstiDto(null, Collections.singletonMap(Kieli.FI, teksti));
    }

    public static Optional<LokalisoituTekstiDto> olt(String teksti) {
        return Optional.of(lt(teksti));
    }
}
