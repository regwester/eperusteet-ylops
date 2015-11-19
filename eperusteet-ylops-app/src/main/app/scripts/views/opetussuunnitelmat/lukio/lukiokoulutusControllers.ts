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

'use strict';

ylopsApp
    .service('LukioNavigaatioProvider', function($state) {
        var produceNavigation = function():NavigaatioItem[] {
            return [
                {
                    url: $state.href('root.opetussuunnitelmat.lukio.opetus.aihekokonaisuudet'),
                    label: 'lukio-aihekokonaisuudet',
                    depth: 0,
                    active: false
                },
                {
                    url: $state.href('root.opetussuunnitelmat.lukio.opetus.yleisettavoitteet'),
                    label: 'lukio-opetuksen-yleiset-tavoitteet',
                    depth: 0,
                    active: false
                }
            ];
            // TODO...
        };

        return {
            produceNavigation: produceNavigation
        }
    })
    // Base controller for opetus conttrollers
    .controller('LukioOpetusController', function (
            $scope, LukioNavigaatioProvider, MurupolkuData, $state, $stateParams,
            $rootScope, OppiaineService) {
        $scope.navi = LukioNavigaatioProvider.produceNavigation();

        if ($state.is('root.opetussuunnitelmat.lukio.opetus')) {
            $state.go('root.opetussuunnitelmat.lukio.opetus.aihekokonaisuudet');
        }
    })
    // a opetus controller:
    .controller('AihekokonaisuudetController', function($scope) {
        $scope.aihekokonaisuudet = {
            otsikko: {fi: "Aihekokonaisuudet"},
            aihekokonaisuudet: [
                {
                    otsikko: {fi: "Testi"}
                }
            ]
        };
    })
    .controller('OpetuksenYleisetTavoitteetController', function($scope) {
        $scope.yleisetTavoitteet = {
            otsikko: {fi: "Opetuksen yleiset tavoitteet"}
        };
    });