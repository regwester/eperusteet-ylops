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
package fi.vm.sade.eperusteet.ylops.service.util;

import fi.vm.sade.eperusteet.ylops.domain.teksti.Kieli;
import fi.vm.sade.eperusteet.ylops.domain.teksti.LokalisoituTeksti;
import fi.vm.sade.eperusteet.ylops.dto.teksti.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.ylops.service.exception.ValidointiException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author nkala
 */
@Getter
public class Validointi {
    @Getter
    static public class Virhe {
        private String syy;
        private Map<Kieli, String> nimi = new HashMap<>();

        Virhe(String syy) {
            this.syy = syy;
            this.nimi = null;
        }

        Virhe(String syy, LokalisoituTeksti t) {
            this.syy = syy;
            if (t != null) {
                this.nimi = t.getTeksti();
            }
        }

        Virhe(String syy, LokalisoituTekstiDto t) {
            this.syy = syy;
            if (t != null) {
                this.nimi = t.getTekstit();
            }
        }
    }

    private List<Virhe> virheet = new ArrayList<>();

    public void lisaaVirhe(String syy) {
        virheet.add(new Virhe(syy));
    }

    public void lisaaVirhe(String syy, LokalisoituTeksti t, LokalisoituTeksti parent) {
        virheet.add(new Virhe(syy, t == null ? parent : t));
    }

    public void lisaaVirhe(String syy, LokalisoituTekstiDto t, LokalisoituTekstiDto parent) {
        virheet.add(new Virhe(syy, t));
    }

    public void tuomitse() {
        if (!virheet.isEmpty()) {
            throw new ValidointiException(this);
        }
    }
}
