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
package fi.vm.sade.eperusteet.ylops.service.dokumentti.impl.util;

import java.util.ArrayList;

/**
 * @author isaul
 */
public class DokumenttiTaulukko {

    private String otsikko;
    private ArrayList<String> otsikkoSarakkeet = new ArrayList<>();
    private ArrayList<DokumenttiRivi> rivit = new ArrayList<>();

    public void addOtsikko(String otsikko) {
        this.otsikko = otsikko;
    }

    public void addOtsikkoSarake(String sarake) {
        otsikkoSarakkeet.add(sarake);
    }

    public void addRivi(DokumenttiRivi rivi) {
        rivit.add(rivi);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        // Tyhjää taulukkoa on turha antaa
        if (rivit.size() > 0) {
            builder.append("<table border=\"1\">");

            if (otsikko != null) {
                builder.append("<caption>");
                builder.append(otsikko);
                builder.append("</caption>");
            }

            // Otsikko rivi
            if (otsikkoSarakkeet.size() > 0) {
                builder.append("<tr bgcolor=\"#d4e3f4\">");
                otsikkoSarakkeet.stream()
                        .forEach((sarake) -> {
                            builder.append("<th>");
                            builder.append(sarake);
                            builder.append("</th>");
                        });
                builder.append("</tr>");
            }

            rivit.stream()
                    .forEach((rivi) -> {
                        builder.append("<tr>");
                        builder.append(rivi.toString());
                        builder.append("</tr>");
                    });

            builder.append("</table>");
        }
        return builder.toString();
    }
}
