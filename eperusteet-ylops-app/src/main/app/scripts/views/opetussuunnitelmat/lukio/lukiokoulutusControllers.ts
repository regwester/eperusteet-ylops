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

ylopsApp
    .service("LukioNavigaatioProvider", function(
        $state,
        $q: IQService,
        LukioOpetussuunnitelmaService: LukioOpetussuunnitelmaServiceI,
        Kaanna
    ) {
        var buildNavigation = function(): IPromise<sn.NavigaatioItem[]> {
            var d = $q.defer<sn.NavigaatioItem[]>(),
                aihekok: Lukio.AihekokonaisuudetPerusteenOsa = null,
                rakenne: Lukio.LukioOpetussuunnitelmaRakenneOps = null;
            $q
                .all([
                    LukioOpetussuunnitelmaService.getAihekokonaisuudet().then(
                        (ak: Lukio.AihekokonaisuudetPerusteenOsa) => (aihekok = ak)
                    ),
                    LukioOpetussuunnitelmaService.getRakenne().then(
                        (r: Lukio.LukioOpetussuunnitelmaRakenneOps) => (rakenne = r)
                    )
                ])
                .then(() => {
                    var items = <sn.NavigaatioItem[]>[
                        {
                            url: $state.href("root.opetussuunnitelmat.lukio.opetus.yleisettavoitteet"),
                            label: "lukio-opetuksen-yleiset-tavoitteet",
                            depth: 0
                        },
                        {
                            url: $state.href("root.opetussuunnitelmat.lukio.opetus.aihekokonaisuudet"),
                            label: "lukio-aihekokonaisuudet",
                            depth: 0
                        }
                    ];
                    if (aihekok.paikallinen) {
                        _.each(aihekok.paikallinen.aihekokonaisuudet, (ak: Lukio.OpsAihekokonaisuus) =>
                            items.push({
                                url: $state.href("root.opetussuunnitelmat.lukio.opetus.aihekokonaisuus", {
                                    aihekokonaisuusId: ak.id
                                }),
                                label: Kaanna.kaanna(ak.otsikko || (ak.perusteen ? ak.perusteen.otsikko : {})),
                                depth: 1
                            })
                        );
                    }
                    items.push({
                        url: $state.href("root.opetussuunnitelmat.lukio.opetus.oppiaineet"),
                        label: "lukio-oppiaineet-oppimaarat",
                        depth: 0
                    });
                    var mapOppiaine = (oa: Lukio.LukioOppiaine, dept: number) => {
                        items.push({
                            url: $state.href("root.opetussuunnitelmat.lukio.opetus.oppiaine", {
                                oppiaineId: oa.id
                            }),
                            label: oa.nimi,
                            depth: dept
                        });
                        _.each(oa.oppimaarat, (om: Lukio.LukioOppiaine) => mapOppiaine(om, dept + 1));
                    };
                    _.each(rakenne.oppiaineet, (oa: Lukio.LukioOppiaine) => mapOppiaine(oa, 1));
                    d.resolve(items);
                });
            return d.promise;
        };

        var produceNavigation = function(
            doUpateItems: (items: NavigaatioItem[]) => IPromise<NavigaatioItem[]>
        ): IPromise<sn.NavigaatioItem[]> {
            var doBuild = () => buildNavigation().then(doUpateItems);
            LukioOpetussuunnitelmaService.onAihekokonaisuudetUpdate(doBuild);
            LukioOpetussuunnitelmaService.onRakenneUpdate(doBuild);
            return doBuild();
        };

        return {
            produceNavigation: produceNavigation
        };
    })
    // Base controller for opetus conttrollers
    .controller("LukioOpetusController", function($scope, LukioNavigaatioProvider, MurupolkuData, $state, $log) {
        $scope.navi = [];
        LukioNavigaatioProvider.produceNavigation((newItems: NavigaatioItem[]) => {
            $scope.navi.length = 0; // empty
            // Can not be replaced (otherwise the navigation won't show)
            _.each(newItems, (i: sn.NavigaatioItem) => $scope.navi.push(i));
            $scope.$broadcast("navigaatio:setNavi", $scope.navi);
            return newItems;
        });
        if ($state.is("root.opetussuunnitelmat.lukio.opetus")) {
            $state.go("root.opetussuunnitelmat.lukio.opetus.oppiaineet");
        }
    })
    .controller("OpetuksenYleisetTavoitteetController", function(
        $scope,
        $state,
        $log,
        $q,
        Notifikaatiot,
        $timeout,
        LukioOpetussuunnitelmaService: LukioOpetussuunnitelmaServiceI,
        Editointikontrollit
    ) {
        $scope.yleisetTavoitteet = {};
        LukioOpetussuunnitelmaService.getOpetuksenYleisetTavoitteet().then(yt => ($scope.yleisetTavoitteet = yt));
        $scope.editMode = false;

        Editointikontrollit.registerCallback({
            validate: function() {
                return true;
            },
            edit: () =>
                $q(resolve => {
                    LukioOpetussuunnitelmaService.lukitseYleisetTavoitteet().then(() => {
                        $scope.editMode = true;
                        resolve();
                    });
                }),
            cancel: () =>
                $q(resolve => {
                    LukioOpetussuunnitelmaService.vapautaYleisetTavoitteet().then(() => {
                        resolve();
                        $scope.editMode = false;
                        $timeout(() => $state.reload());
                    });
                }),
            save: () =>
                $q(resolve => {
                    $scope.yleiskuvaEditMode = false;
                    LukioOpetussuunnitelmaService.updateYleisetTavoitteet(
                        $scope.yleisetTavoitteet.paikallinen
                    ).then(() => {
                        Notifikaatiot.onnistui("opetuksen-yleisten-tavoitteiden-yleiskuvauksen-paivitys-onnistui");
                        LukioOpetussuunnitelmaService.vapautaYleisetTavoitteet().then(() => {
                            resolve();
                            $scope.editMode = false;
                            $timeout(() => $state.reload());
                        });
                    });
                })
        });

        $scope.toEditMode = () => {
            Editointikontrollit.startEditing();
        };
    });
