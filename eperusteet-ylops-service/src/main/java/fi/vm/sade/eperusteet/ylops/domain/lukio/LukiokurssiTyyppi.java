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

package fi.vm.sade.eperusteet.ylops.domain.lukio;

import fi.vm.sade.eperusteet.ylops.domain.oppiaine.Oppiaine;
import fi.vm.sade.eperusteet.ylops.domain.teksti.LokalisoituTeksti;
import fi.vm.sade.eperusteet.ylops.dto.peruste.lukio.PerusteenLukiokurssiTyyppi;

import java.util.function.Function;

/**
 * User: tommiratamaa
 * Date: 17.11.2015
 * Time: 13.42
 */
public enum LukiokurssiTyyppi {
    VALTAKUNNALLINEN_PAKOLLINEN(Oppiaine::getValtakunnallinenPakollinenKuvaus, Oppiaine::setValtakunnallinenPakollinenKuvaus),
    VALTAKUNNALLINEN_SYVENTAVA(Oppiaine::getValtakunnallinenSyventavaKurssiKuvaus, Oppiaine::setValtakunnallinenSyventavaKurssiKuvaus),
    VALTAKUNNALLINEN_SOVELTAVA(Oppiaine::getValtakunnallinenSoveltavaKurssiKuvaus, Oppiaine::setValtakunnallinenSoveltavaKurssiKuvaus),
    PAIKALLINEN_PAKOLLINEN(Oppiaine::getPaikallinenPakollinenKuvaus, Oppiaine::setPaikallinenPakollinenKuvaus, true),
    PAIKALLINEN_SYVENTAVA(Oppiaine::getPaikallinenSyventavaKurssiKuvaus, Oppiaine::setPaikallinenSyventavaKurssiKuvaus, true),
    PAIKALLINEN_SOVELTAVA(Oppiaine::getPaikallinenSoveltavaKurssiKuvaus, Oppiaine::setPaikallinenSoveltavaKurssiKuvaus, true);

    public static LukiokurssiTyyppi ofPerusteTyyppi(PerusteenLukiokurssiTyyppi tyyppi) {
        switch (tyyppi) {
            case PAKOLLINEN: return VALTAKUNNALLINEN_PAKOLLINEN;
            case VALTAKUNNALLINEN_SOVELTAVA: return VALTAKUNNALLINEN_SOVELTAVA;
            case VALTAKUNNALLINEN_SYVENTAVA: return VALTAKUNNALLINEN_SYVENTAVA;
            default: throw new IllegalStateException("Unimplemented peruste lukiokurssityyppi: " + tyyppi);
        }
    }

    public interface Setter<ClassType,ValueType> {
        void set(ClassType obj, ValueType value);
    }

    // Vähän hassusti ovat nyt versioinnin takia samassa käsitteessä kaikki propertyinä, niin pientä helpotusta
    private final Function<Oppiaine, LokalisoituTeksti> oppiaineKuvausGetter;
    private final Setter<Oppiaine, LokalisoituTeksti> oppiaineKuvausSetter;
    private final boolean paikallinen;

    LukiokurssiTyyppi(Function<Oppiaine, LokalisoituTeksti> oppiaineKuvausGetter,
                      Setter<Oppiaine, LokalisoituTeksti> oppiaineKuvausSetter) {
        this(oppiaineKuvausGetter, oppiaineKuvausSetter, false);
    }
    LukiokurssiTyyppi(Function<Oppiaine, LokalisoituTeksti> oppiaineKuvausGetter,
                      Setter<Oppiaine, LokalisoituTeksti> oppiaineKuvausSetter, boolean paikallinen) {
        this.oppiaineKuvausGetter = oppiaineKuvausGetter;
        this.oppiaineKuvausSetter = oppiaineKuvausSetter;
        this.paikallinen = paikallinen;
    }

    public Function<Oppiaine, LokalisoituTeksti> oppiaineKuvausGetter() {
        return oppiaineKuvausGetter;
    }

    public Setter<Oppiaine, LokalisoituTeksti> oppiaineKuvausSetter() {
        return oppiaineKuvausSetter;
    }

    public boolean isPaikallinen() {
        return paikallinen;
    }
}
