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

ylopsApp.controller("AdminController", function(
    $scope,
    ListaSorter,
    Algoritmit,
    OpsinTila,
    Notifikaatiot,
    opsStatistiikka,
    opsit
) {
    $scope.sorter = ListaSorter.init($scope);
    $scope.opsiLista = true;
    $scope.tilat = ["luonnos", "valmis", "poistettu"];
    $scope.items = opsit;
    $scope.$$collapsestats = true;

    $scope.paginate = {
        perPage: 10,
        current: 1
    };

    const updatePageinfo = () => {
        $scope.lastVisible = Math.min(
            ($scope.paginate.current - 1) * $scope.paginate.perPage + $scope.paginate.perPage,
            $scope.filtered.length
        );
    };

    $scope.search = {
        term: "",
        tilaRajain: "",
        changed: function(value) {
            $scope.paginate.current = 1;
            $scope.filtered = _.filter($scope.items, function(item) {
                if ($scope.search.tilaRajain && item.tila !== $scope.search.tilaRajain) {
                    return false;
                }
                if (value) {
                    var nameMatch = Algoritmit.match(value, item.nimi);
                    var kuntaMatch = _.any(item.kunnat, function(kunta) {
                        return Algoritmit.match(value, kunta.nimi);
                    });
                    return nameMatch || kuntaMatch;
                }
                return true;
            });
            updatePageinfo();
        }
    };

    const koulutMap = _(opsit)
        .map("organisaatiot")
        .flatten()
        .filter(org => _.includes(org.tyypit, "Oppilaitos"))
        .indexBy("oid")
        .value();

    $scope.statsit = opsStatistiikka;

    $scope.$watch("search.tilaRajain", function() {
        $scope.search.changed($scope.search.term);
    });

    $scope.$watch("paginate.current", updatePageinfo);

    $scope.palauta = ops => {
        OpsinTila.palauta(ops, res => {
            ops.tila = res.tila;
            Notifikaatiot.onnistui("tallennettu-ok");
        });
    };
});
