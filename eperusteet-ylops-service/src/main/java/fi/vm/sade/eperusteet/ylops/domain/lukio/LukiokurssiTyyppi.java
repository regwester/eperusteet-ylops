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
import fi.vm.sade.eperusteet.ylops.domain.teksti.TekstiKappale;

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
    PAIKALLINEN_PAKOLLINEN(Oppiaine::getPaikallinenPakollinenKuvaus, Oppiaine::setPaikallinenPakollinenKuvaus),
    PAIKALLINEN_SYVENTAVA(Oppiaine::getPaikallinenSyventavaKurssiKuvaus, Oppiaine::setPaikallinenSyventavaKurssiKuvaus),
    PAIKALLINEN_SOVELTAVA(Oppiaine::getPaikallinenSoveltavaKurssiKuvaus, Oppiaine::setPaikallinenSoveltavaKurssiKuvaus);

    public interface Setter<ClassType,ValueType> {
        void set(ClassType obj, ValueType value);
    }

    private final Function<Oppiaine, TekstiKappale> oppiaineKuvausGetter;
    private final Setter<Oppiaine, TekstiKappale> oppiaineKuvausSetter;

    LukiokurssiTyyppi(Function<Oppiaine, TekstiKappale> oppiaineKuvausGetter, Setter<Oppiaine, TekstiKappale> oppiaineKuvausSetter) {
        this.oppiaineKuvausGetter = oppiaineKuvausGetter;
        this.oppiaineKuvausSetter = oppiaineKuvausSetter;
    }

    public Function<Oppiaine, TekstiKappale> oppiaineKuvausGetter() {
        return oppiaineKuvausGetter;
    }

    public Setter<Oppiaine, TekstiKappale> oppiaineKuvausSetter() {
        return oppiaineKuvausSetter;
    }
}
