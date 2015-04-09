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
package fi.vm.sade.eperusteet.ylops.domain.oppiaine;

/**
 *
 * @author mikkom
 */
public enum OppiaineTyyppi {
    /**
     *
     * Kaikille yhteinen aine
     */
    YHTEINEN("yhteinen"),
    /**
     *
     * Taide- ja/tai taitoaineen valinnainen tunti
     */
    TAIDE_TAITOAINE("taide_taitoaine"),
    /**
     *
     * Muu valinnainen aine
     */
    MUU_VALINNAINEN("muu_valinnainen");

    private final String tyyppi;

    private OppiaineTyyppi(String tyyppi) { this.tyyppi = tyyppi; }

    @Override
    public String toString() { return tyyppi; }
}
