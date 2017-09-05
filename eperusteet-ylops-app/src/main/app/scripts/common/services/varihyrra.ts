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

ylopsApp
    .service("VariHyrra", function() {
        this.current = 0;
        this.colors = [
            "fbb03b",
            "ffd400",
            "3a8fbe",
            "89bcd8",
            "800080",
            "b366b3",
            "0c566e",
            "6d9aa8",
            "008000",
            "8cc63f",
            "2e3192",
            "8283be",
            "663300",
            "a38566",
            "666600",
            "a3a366"
        ];
        /**
     * Cycles through predefined set of colors.
     */
        this.next = function() {
            var ret = this.colors[this.current];
            if (++this.current >= this.colors.length) {
                this.current = 0;
            }
            return ret;
        };
        this.reset = function() {
            this.current = 0;
        };
    })
    .service("ColorCalculator", function() {
        var PATTERN = /^(\w{2})(\w{2})(\w{2})$/;

        /**
     * Calculates a readable text color for given background color
     * @param {string} bgColor Background color in hex format 'rrggbb'
     */
        this.readableTextColorForBg = function(bgColor) {
            var match = PATTERN.exec(bgColor);
            if (match) {
                var r = parseInt(match[1], 16),
                    g = parseInt(match[2], 16),
                    b = parseInt(match[3], 16);
                // W3C proposal formula for color brightness
                // ((Red value X 299) + (Green value X 587) + (Blue value X 114)) / 1000
                var brightness = (r * 299 + g * 587 + b * 114) / 1000;
                if (brightness < 135) {
                    return "white";
                }
            }
            return "black";
        };
    });
