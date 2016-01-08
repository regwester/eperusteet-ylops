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

package fi.vm.sade.eperusteet.ylops.domain.oppiaine;

import fi.vm.sade.eperusteet.ylops.domain.teksti.LokalisoituTeksti;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;
import java.util.UUID;

/**
 * User: tommiratamaa
 * Date: 15.12.2015
 * Time: 10.36
 */
@Getter
@ToString
public class OppiaineOpsTunniste {
    private final UUID tunniste;
    private final String kieliKoodiArvo;
    private final LokalisoituTeksti kieli;

    public OppiaineOpsTunniste(UUID tunniste, String kieliKoodiArvo, LokalisoituTeksti kieli) {
        this.tunniste = tunniste;
        this.kieliKoodiArvo = kieliKoodiArvo;
        this.kieli = kieli;
    }

    @Override
    public int hashCode() {
        int result = tunniste.hashCode();
        result = 31 * result + (kieliKoodiArvo != null ? kieliKoodiArvo.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OppiaineOpsTunniste)) return false;

        OppiaineOpsTunniste that = (OppiaineOpsTunniste) o;

        if (!tunniste.equals(that.tunniste)) return false;
        if (kieliKoodiArvo == null) {
            return that.kieliKoodiArvo == null;
        }
        if (!kieliKoodiArvo.equals(that.kieliKoodiArvo)) {
            return false;
        }
        if ("KX".equals(kieliKoodiArvo)) {
            return Objects.equals(kieli, that.kieli);
        }
        return true;
    }
}
