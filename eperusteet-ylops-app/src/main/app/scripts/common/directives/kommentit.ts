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

ylopsApp.directive("kommentit", function(Kommentit, $timeout, $location, Varmistusdialogi) {
    return {
        restrict: "AE",
        templateUrl: "views/common/directives/kommentit.html",
        scope: {},
        controller: function($scope) {
            $scope.kommentitLadattu = false;
            $scope.editointi = false;
            $scope.editoitava = "";
            $scope.editoi = false;
            $scope.sisalto = false;
            $scope.urlit = {};
            $scope.nimikirjaimet = function(nimi) {
                return _.reduce(
                    nimi.split(" "),
                    function(memo, osa) {
                        return memo + (osa ? osa[0] : "");
                    },
                    ""
                ).toUpperCase();
            };

            $scope.$kommenttiMaxLength = {
                maara: 1024
            };

            function lataaKommentit(url) {
                var lataaja = $scope.urlit[url];
                if (lataaja) {
                    lataaja(function(kommenttipuu) {
                        $scope.sisalto = kommenttipuu;
                        $scope.kommentitLadattu = true;
                    });
                }
            }

            $scope.$on("$stateChangeStart", function() {
                $scope.kommentitLadattu = false;
            });

            function lataajaCb(url, lataaja) {
                if (!$scope.urlit[url]) {
                    $scope.urlit[url] = lataaja;
                    lataaKommentit(url);
                }
            }

            var stored = Kommentit.stored();
            if (!_.isEmpty(stored)) {
                lataajaCb(stored.url, stored.lataaja);
            }

            $scope.$on("update:kommentit", function(event, url, lataaja) {
                if (_.isEmpty(url) || _.isEmpty(lataaja)) {
                    // Pakotetaan kommenttien näyttö lataamatta niitä uudelleen.
                    $scope.kommentitLadattu = true;
                } else {
                    lataajaCb(url, lataaja);
                }
            });

            $scope.muokkaaKommenttia = function(kommentti, uusikommentti, cb) {
                Kommentit.muokkaaKommenttia(kommentti, uusikommentti, cb);
            };
            $scope.poistaKommentti = function(kommentti) {
                Varmistusdialogi.dialogi({
                    otsikko: "vahvista-poisto",
                    teksti: "poistetaanko-kommentti",
                    primaryBtn: "poista",
                    successCb: function() {
                        Kommentit.poistaKommentti(kommentti);
                    }
                })();
            };
            $scope.lisaaKommentti = function(parent, kommentti, cb) {
                Kommentit.lisaaKommentti(parent, kommentti, function() {
                    var first = _.first($scope.sisalto.viestit);
                    if (first) {
                        first.$nimikirjaimet = $scope.nimikirjaimet(first.nimi || first.muokkaaja);
                    }
                    $scope.sisalto.$yhteensa += 1;
                    (cb || angular.noop)();
                });
            };

            $scope.$on("enableEditing", function() {
                $scope.editointi = true;
            });
            $scope.$on("disableEditing", function() {
                $scope.editointi = false;
            });
        }
    };
});
