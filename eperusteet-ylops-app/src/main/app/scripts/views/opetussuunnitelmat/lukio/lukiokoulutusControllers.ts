///<reference path="../services/lukioServices.ts"/>
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
    .service('LukioNavigaatioProvider', function($state, $q:IQService,
                    LukioOpetussuunnitelmaService, Kaanna) {
        var produceNavigation = function():IPromise<NavigaatioItem[]> {
            var d = $q.defer<NavigaatioItem[]>(),
                aihekok : AihekokonaisuudetPerusteenOsa = null;
            $q.all([
                LukioOpetussuunnitelmaService.getAihekokonaisuudet()
                    .then(function(ak : AihekokonaisuudetPerusteenOsa) {aihekok = ak;})
            ]).then(function() {
                var items = <NavigaatioItem[]>[
                    {
                        url: $state.href('root.opetussuunnitelmat.lukio.opetus.yleisettavoitteet'),
                        label: 'lukio-opetuksen-yleiset-tavoitteet',
                        depth: 0
                    },
                    {
                        url: $state.href('root.opetussuunnitelmat.lukio.opetus.aihekokonaisuudet'),
                        label: 'lukio-aihekokonaisuudet',
                        depth: 0
                    }
                ];
                if (aihekok.paikallinen) {
                    _.each(aihekok.paikallinen.aihekokonaisuudet, function(ak :OpsAihekokonaisuus) {
                        items.push({
                            url: $state.href('root.opetussuunnitelmat.lukio.opetus.aihekokonaisuudet'),
                            label: Kaanna.kaanna(ak.otsikko || (ak.perusteen ? ak.perusteen.otsikko : {})),
                            depth: 1
                        });
                    });
                }
                items.push({
                    url: $state.href('root.opetussuunnitelmat.lukio.opetus.oppiaineet'),
                    label: 'lukio-oppiaineet-oppimaarat',
                    depth: 0
                });
                d.resolve(items);
            });
            return d.promise;
        };

        return {
            produceNavigation: produceNavigation
        }
    })
    // Base controller for opetus conttrollers
    .controller('LukioOpetusController', function (
            $scope, LukioNavigaatioProvider, MurupolkuData, $state, $stateParams,
            $rootScope, OppiaineService) {
        LukioNavigaatioProvider.produceNavigation().then(function (items:NavigaatioItem[]) {
            $scope.navi = items;
        });

        if ($state.is('root.opetussuunnitelmat.lukio.opetus')) {
            $state.go('root.opetussuunnitelmat.lukio.opetus.yleisettavoitteet');
        }
    })
    // a opetus controller:
    .controller('AihekokonaisuudetController', function($scope, LukioOpetussuunnitelmaService) {
        $scope.aihekokonaisuudet = {};
        LukioOpetussuunnitelmaService.getAihekokonaisuudet().then(function(ak : AihekokonaisuudetPerusteenOsa) {
            $scope.aihekokonaisuudet = ak;
        });
    })
    .controller('OpetuksenYleisetTavoitteetController', function($scope, LukioOpetussuunnitelmaService, $log) {
        $scope.yleisetTavoitteet = {};
        LukioOpetussuunnitelmaService.getOpetuksenYleisetTavoitteet().then(function(yt: OpetuksenYleisetTavoitteetPerusteenOsa) {
            $scope.yleisetTavoitteet = yt;
        });
    });