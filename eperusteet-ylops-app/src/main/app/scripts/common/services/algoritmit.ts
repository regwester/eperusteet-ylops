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

ylopsApp.service("Algoritmit", function(Kaanna) {
    function traverse(objekti, lapsienAvain, cb, depth) {
        if (!objekti) {
            return;
        }
        depth = depth || 0;
        _.forEach(objekti[lapsienAvain], function(solmu, index) {
            if (!cb(solmu, depth, index, objekti[lapsienAvain], objekti)) {
                solmu.$$traverseParent = objekti;
                solmu.$$nodeParent = objekti;
                traverse(solmu, lapsienAvain, cb, depth + 1);
            }
        });
    }

    function match(input, to) {
        var vertailu = Kaanna.kaanna(to) || "";
        return vertailu.toLowerCase().indexOf(input.toLowerCase()) !== -1;
    }

    this.traverse = traverse;
    this.match = match;
});
