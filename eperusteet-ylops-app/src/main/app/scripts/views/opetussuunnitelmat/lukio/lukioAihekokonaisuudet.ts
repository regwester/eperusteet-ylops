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
    .controller("AihekokonaisuudetController", function(
        $scope,
        $state,
        Editointikontrollit,
        $q,
        LukioOpetussuunnitelmaService: LukioOpetussuunnitelmaServiceI,
        $timeout,
        $stateParams,
        Notifikaatiot,
        Kaanna
    ) {
        $scope.aihekokonaisuudet = {};
        $scope.sortableModel = [];
        $scope.editMode = false;
        $scope.yleiskuvaEditMode = false;
        LukioOpetussuunnitelmaService.getAihekokonaisuudet().then(ak => {
            $scope.aihekokonaisuudet = ak;
            $scope.sortableModel = ak.paikallinen.aihekokonaisuudet;
        });

        $scope.sortableOptions = {
            disabled: true
        };

        $scope.toEditMode = () => {
            Editointikontrollit.registerCallback({
                validate: function() {
                    return true;
                },
                edit: () =>
                    $q(resolve => {
                        LukioOpetussuunnitelmaService.lukitseAihekokonaisuudet(
                            $scope.aihekokonaisuudet.paikallinen.id
                        ).then(() => resolve());
                    }),
                cancel: () =>
                    $q(resolve => {
                        LukioOpetussuunnitelmaService.vapautaAihekokonaisuudet(
                            $scope.aihekokonaisuudet.paikallinen.id
                        ).then(() => {
                            resolve();
                            $timeout(() => $state.reload());
                        });
                    }),
                save: () =>
                    $q(resolve => {
                        $scope.editmode = false;
                        $scope.sortableOptions.disabled = true;
                        LukioOpetussuunnitelmaService.rearrangeAihekokonaisuudet({
                            aihekokonaisuudet: _.each($scope.sortableModel, i => _.pick(i, "id"))
                        }).then(() => {
                            Notifikaatiot.onnistui("aihekokonaisuuksien-jarjestaminen-onnistui");
                            LukioOpetussuunnitelmaService.vapautaAihekokonaisuudet(
                                $scope.aihekokonaisuudet.paikallinen.id
                            ).then(() => {
                                resolve();
                                $timeout(() => $state.reload());
                            });
                        });
                    })
            });
            $scope.sortableOptions.disabled = false;
            $scope.editMode = true;
            Editointikontrollit.startEditing();
        };

        $scope.toYleiskuvausEditMode = () => {
            Editointikontrollit.registerCallback({
                validate: function() {
                    return true;
                    //return Kaanna.kaanna($scope.aihekokonaisuudet.paikallinen.otsikko);
                },
                edit: () =>
                    $q(resolve => {
                        LukioOpetussuunnitelmaService.lukitseAihekokonaisuudet(
                            $scope.aihekokonaisuudet.paikallinen.id
                        ).then(() => resolve());
                    }),
                cancel: () =>
                    $q(resolve => {
                        LukioOpetussuunnitelmaService.vapautaAihekokonaisuudet(
                            $scope.aihekokonaisuudet.paikallinen.id
                        ).then(() => {
                            resolve();
                            $timeout(() => $state.reload());
                        });
                    }),
                save: () =>
                    $q(resolve => {
                        $scope.yleiskuvaEditMode = false;
                        LukioOpetussuunnitelmaService.updateAihekokonaisuudetYleiskuvaus({
                            otsikko: $scope.aihekokonaisuudet.paikallinen.otsikko,
                            yleiskuvaus: $scope.aihekokonaisuudet.paikallinen.yleiskuvaus
                        }).then(() => {
                            Notifikaatiot.onnistui("aihekokonaisuuksien-yleiskuvauksen-paivitys-onnistui");
                            LukioOpetussuunnitelmaService.vapautaAihekokonaisuudet(
                                $scope.aihekokonaisuudet.paikallinen.id
                            ).then(() => {
                                resolve();
                                $timeout(() => $state.reload());
                            });
                        });
                    })
            });
            $scope.yleiskuvaEditMode = true;
            Editointikontrollit.startEditing();
        };

        $scope.addAihekokonaisuus = () => {
            $timeout(() =>
                $state.go("root.opetussuunnitelmat.lukio.opetus.uusiaihekokonaisuus", {
                    id: $stateParams.id
                })
            );
        };
    })
    .controller("AihekokonaisuusController", function(
        $scope,
        $state,
        $stateParams,
        Varmistusdialogi,
        Editointikontrollit,
        $q,
        $timeout,
        $log,
        Notifikaatiot,
        Kaanna,
        LukioOpetussuunnitelmaService: LukioOpetussuunnitelmaServiceI
    ) {
        $scope.aihekokonaisuus = null;
        $scope.editMode = false;
        LukioOpetussuunnitelmaService.getAihekokonaisuus($stateParams.aihekokonaisuusId).then(ak => {
            $scope.aihekokonaisuus = ak;
        });
        $scope.isDeletable = () =>
            $scope.aihekokonaisuus && !$scope.aihekokonaisuus.perusteen && !$scope.aihekokonaisuus.parent;

        Editointikontrollit.registerCallback({
            validate: function() {
                return Kaanna.kaanna($scope.aihekokonaisuus.otsikko);
            },
            edit: () =>
                $q(resolve => {
                    LukioOpetussuunnitelmaService.lukitseAihekokonaisuus($scope.aihekokonaisuus.id).then(() =>
                        resolve()
                    );
                }),
            cancel: () =>
                $q(resolve => {
                    LukioOpetussuunnitelmaService.vapautaAihekokonaisuus($scope.aihekokonaisuus.id).then(() => {
                        $timeout(() => $state.reload());
                        resolve();
                    });
                }),
            save: () =>
                $q(resolve => {
                    LukioOpetussuunnitelmaService.updateAihekokonaisuus(
                        $stateParams.aihekokonaisuusId,
                        $scope.aihekokonaisuus
                    ).then(() => {
                        Notifikaatiot.onnistui("aihekokonaisuuden-tallentaminen-onnistui");
                        LukioOpetussuunnitelmaService.vapautaAihekokonaisuus($scope.aihekokonaisuus.id).then(() => {
                            $timeout(() => $state.reload());
                            resolve();
                        });
                    });
                    resolve();
                })
        });

        $scope.toEditMode = () => {
            $scope.editMode = true;
            Editointikontrollit.startEditing();
        };

        $scope.deleteKokonaisuus = () =>
            Varmistusdialogi.dialogi({
                otsikko: "varmista-poista-aihekokonaisuus",
                primaryBtn: "poista",
                successCb: () =>
                    LukioOpetussuunnitelmaService.deleteAihekokonaisuus($stateParams.aihekokonaisuusId).then(() => {
                        Notifikaatiot.onnistui("aihekokonaisuuden-poisto-onnistui");
                        $timeout(() =>
                            $state.go("root.opetussuunnitelmat.lukio.opetus.aihekokonaisuudet", {
                                id: $stateParams.id
                            })
                        );
                    })
            })();
    })
    .controller("LuoAihekokonaisuusController", function(
        $scope,
        $state,
        $stateParams,
        Kaanna,
        Editointikontrollit,
        $q,
        $timeout,
        LukioControllerHelpers,
        Notifikaatiot,
        LukioOpetussuunnitelmaService: LukioOpetussuunnitelmaServiceI
    ) {
        $scope.aihekokonaisuus = {
            otsikko: LukioControllerHelpers.kielella(""),
            yleiskuvaus: LukioControllerHelpers.kielella("")
        };
        $scope.editMode = true;

        Editointikontrollit.registerCallback({
            validate: function() {
                return Kaanna.kaanna($scope.aihekokonaisuus.otsikko);
            },
            edit: () =>
                $q(resolve => {
                    resolve();
                }),
            cancel: () =>
                $q(resolve => {
                    $timeout(() =>
                        $state.go("root.opetussuunnitelmat.lukio.opetus.aihekokonaisuudet", {
                            id: $stateParams.id
                        })
                    );
                    resolve();
                }),
            save: () =>
                $q(resolve => {
                    LukioOpetussuunnitelmaService.saveAihekokonaisuus($scope.aihekokonaisuus).then(res => {
                        Notifikaatiot.onnistui("aihekokonaisuuden-tallentaminen-onnistui");
                        $timeout(() =>
                            $state.go("root.opetussuunnitelmat.lukio.opetus.aihekokonaisuus", {
                                id: $stateParams.id,
                                aihekokonaisuusId: res.id
                            })
                        );
                        resolve();
                    });
                })
        });

        Editointikontrollit.startEditing();
    });
