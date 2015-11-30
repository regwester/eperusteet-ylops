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

import NavigaatioItem = Sivunavi.NavigaatioItem;
'use strict';

// states in states/lukioStates.ts

ylopsApp
    .service('LukioNavigaatioProvider', function($state, $q:IQService,
                    LukioOpetussuunnitelmaService, Kaanna, $log) {
        var produceNavigation = function():IPromise<sn.NavigaatioItem[]> {
            $log.info('Produce lukio navigation.');
            var d = $q.defer<sn.NavigaatioItem[]>(),
                aihekok : Lukio.AihekokonaisuudetPerusteenOsa = null,
                rakenne : Lukio.LukioOpetussuunnitelmaRakenneOps = null;
            $q.all([
                LukioOpetussuunnitelmaService.getAihekokonaisuudet()
                    .then(function(ak : Lukio.AihekokonaisuudetPerusteenOsa) {aihekok = ak;}),
                LukioOpetussuunnitelmaService.getRakenne()
                    .then(function(r : Lukio.LukioOpetussuunnitelmaRakenneOps) {rakenne = r;})
            ]).then(function() {
                var items = <sn.NavigaatioItem[]>[
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
                    _.each(aihekok.paikallinen.aihekokonaisuudet, function(ak :Lukio.OpsAihekokonaisuus) {
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
                var mapKurssit = function(oa: Lukio.LukioOppiaine, depth: number) {
                    $log.info('Map oppiaine', oa, 'kurssit', oa.kurssit);
                    _.each(oa.kurssit, function(kurssi: Lukio.LukiokurssiOps) {
                        $log.info('kurssi', kurssi);
                        items.push({
                            url: $state.href('root.opetussuunnitelmat.lukio.opetus.kurssi', {
                                oppiaineId: oa.id,
                                kurssiId: kurssi.id
                            }),
                            label: Lokalisointi.concat(kurssi.nimi, kurssi.koodiArvo ? ' (' + kurssi.koodiArvo + ')' : ''),
                            depth: depth
                        });
                    })
                };
                var mapOppiaine = function(oa: Lukio.LukioOppiaine, dept: number) {
                    items.push({
                        url: $state.href('root.opetussuunnitelmat.lukio.opetus.oppiaine', {
                            oppiaineId: oa.id
                        }),
                        label: oa.nimi,
                        depth: dept
                    });
                    _.each(oa.oppimaarat, function(om: Lukio.LukioOppiaine) {
                        mapOppiaine(om, dept+1);
                    });
                    mapKurssit(oa, dept+1);
                };
                _.each(rakenne.oppiaineet, function(oa: Lukio.LukioOppiaine) {
                    mapOppiaine(oa, 1);
                });
                $log.info('items', items);
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
            $scope, LukioNavigaatioProvider, MurupolkuData, $state, $stateParams) {
        $scope.navi = [];
        LukioNavigaatioProvider.produceNavigation().then(function (items:sn.NavigaatioItem[]) {
            _.each(items, function(i: sn.NavigaatioItem) {
                $scope.navi.push(i);
            });
        });

        if ($state.is('root.opetussuunnitelmat.lukio.opetus')) {
            $state.go('root.opetussuunnitelmat.lukio.opetus.oppiaineet');
        }
    })
    // a opetus controller:
    .controller('AihekokonaisuudetController', function($scope, LukioOpetussuunnitelmaService) {
        $scope.aihekokonaisuudet = {};
        LukioOpetussuunnitelmaService.getAihekokonaisuudet().then(function(ak : Lukio.AihekokonaisuudetPerusteenOsa) {
            $scope.aihekokonaisuudet = ak;
        });
    })
    .controller('OpetuksenYleisetTavoitteetController', function($scope, LukioOpetussuunnitelmaService, $log) {
        $scope.yleisetTavoitteet = {};
        LukioOpetussuunnitelmaService.getOpetuksenYleisetTavoitteet().then(function(yt: Lukio.OpetuksenYleisetTavoitteetPerusteenOsa) {
            $scope.yleisetTavoitteet = yt;
        });
    });