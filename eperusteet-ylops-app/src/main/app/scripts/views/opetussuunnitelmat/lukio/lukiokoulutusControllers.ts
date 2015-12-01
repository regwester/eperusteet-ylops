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

// states in states/lukioStates.ts

ylopsApp
    .service('LukioNavigaatioProvider', function($state, $q:IQService,
                    LukioOpetussuunnitelmaService, Kaanna) {
        var produceNavigation = function():IPromise<sn.NavigaatioItem[]> {
            var d = $q.defer<sn.NavigaatioItem[]>(),
                aihekok : Lukio.AihekokonaisuudetPerusteenOsa = null,
                rakenne : Lukio.LukioOpetussuunnitelmaRakenneOps = null;
            $q.all([
                LukioOpetussuunnitelmaService.getAihekokonaisuudet()
                    .then((ak : Lukio.AihekokonaisuudetPerusteenOsa) => aihekok = ak),
                LukioOpetussuunnitelmaService.getRakenne()
                    .then((r : Lukio.LukioOpetussuunnitelmaRakenneOps) => rakenne = r)
            ]).then(() => {
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
                    _.each(aihekok.paikallinen.aihekokonaisuudet, (ak :Lukio.OpsAihekokonaisuus) =>
                        items.push({
                            url: $state.href('root.opetussuunnitelmat.lukio.opetus.aihekokonaisuudet'),
                            label: Kaanna.kaanna(ak.otsikko || (ak.perusteen ? ak.perusteen.otsikko : {})),
                            depth: 1
                        }));
                }
                items.push({
                    url: $state.href('root.opetussuunnitelmat.lukio.opetus.oppiaineet'),
                    label: 'lukio-oppiaineet-oppimaarat',
                    depth: 0
                });
                var mapOppiaine = (oa: Lukio.LukioOppiaine, dept: number) => {
                    items.push({
                        url: $state.href('root.opetussuunnitelmat.lukio.opetus.oppiaine', {
                            oppiaineId: oa.id
                        }),
                        label: oa.nimi,
                        depth: dept
                    });
                    _.each(oa.oppimaarat, (om: Lukio.LukioOppiaine) => mapOppiaine(om, dept+1));
                };
                _.each(rakenne.oppiaineet,(oa: Lukio.LukioOppiaine) => mapOppiaine(oa, 1));
                d.resolve(items);
            });
            return d.promise;
        };

        return {
            produceNavigation: produceNavigation
        }
    })
    // Base controller for opetus conttrollers
    .controller('LukioOpetusController', function ($scope, LukioNavigaatioProvider, MurupolkuData, $state) {
        $scope.navi = [];
        LukioNavigaatioProvider.produceNavigation().then((items) =>
            _.each(items, (i: sn.NavigaatioItem) => $scope.navi.push(i)));
        if ($state.is('root.opetussuunnitelmat.lukio.opetus')) {
            $state.go('root.opetussuunnitelmat.lukio.opetus.oppiaineet');
        }
    })
    // a opetus controller:
    .controller('AihekokonaisuudetController', function($scope,
                            LukioOpetussuunnitelmaService: LukioOpetussuunnitelmaServiceI) {
        $scope.aihekokonaisuudet = {};
        LukioOpetussuunnitelmaService.getAihekokonaisuudet().then(ak => $scope.aihekokonaisuudet = ak);
    })
    .controller('OpetuksenYleisetTavoitteetController', function($scope, $log,
                     LukioOpetussuunnitelmaService: LukioOpetussuunnitelmaServiceI) {
        $scope.yleisetTavoitteet = {};
        LukioOpetussuunnitelmaService.getOpetuksenYleisetTavoitteet().then(yt => $scope.yleisetTavoitteet = yt);
    });