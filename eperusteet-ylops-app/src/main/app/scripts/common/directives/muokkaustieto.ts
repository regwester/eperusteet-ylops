/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */

angular.module("ylopsApp").directive("muokkaustieto", function($rootScope, EperusteetKayttajatiedot) {
    return {
        template:
            '<div ng-if="tiedot && tiedot.luotu" class="dir-muokkaustieto">' +
            "  <span>" +
            '    <span class="muokkaustieto-topic" kaanna="muokattu-viimeksi"></span>:' +
            '    <span class="muokkaustieto-data" ng-bind="(tiedot.muokattu || tiedot.luotu) | aikaleima"></span>' +
            "  </span>" +
            '  <span style="margin-left: 5px" ng-if="muokkaajanNimi">' +
            '    <span class="muokkaustieto-topic" kaanna="muokkaaja"></span>:' +
            '    <span class="muokkaustieto-data" ng-bind="muokkaajanNimi"></span>' +
            "  </span>" +
            "</div>",
        scope: {
            tiedot: "="
        },
        controller: $scope => {
            $scope.$watch(
                "tiedot",
                tiedot => {
                    if (tiedot && tiedot.muokkaajaOid) {
                        EperusteetKayttajatiedot.get(
                            {
                                oid: tiedot.muokkaajaOid
                            },
                            res => {
                                if (res.sukunimi && (res.kutsumanimi || res.etunimet)) {
                                    $scope.muokkaajanNimi = (res.kutsumanimi || res.etunimet) + " " + res.sukunimi;
                                }
                            }
                        );
                    }
                },
                true
            );
        }
    };
});
