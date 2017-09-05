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
    .service("MurupolkuData", function($rootScope) {
        var data = {};
        this.set = function(key, value) {
            if (_.isObject(key)) {
                _.each(key, function(item, k) {
                    data[k] = item;
                });
            } else {
                data[key] = value;
            }
            $rootScope.$broadcast("murupolku:update");
        };
        this.get = function(key) {
            return data[key];
        };
    })
    .directive("ylopsHeader", function() {
        return {
            restrict: "AE",
            scope: {},
            templateUrl: "views/common/directives/header.html",
            controller: "YlopsHeaderController"
        };
    })
    .controller("YlopsHeaderController", function($scope, $state, Oikeudet, MurupolkuData, Kaanna) {
        var currentState = null;

        var STATES = {
            "root.opetussuunnitelmat.yksi.sisalto": {
                useData: "opsNimi",
                useId: "opsId"
            },
            "root.opetussuunnitelmat.yksi.sisaltoalue": {
                parent: "root.opetussuunnitelmat.yksi.sisalto",
                useData: "osioNimi",
                useId: "alueId"
            },
            "root.opetussuunnitelmat.yksi.tiedot": {
                parent: "root.opetussuunnitelmat.yksi.sisalto",
                label: "opsn-tiedot"
            },
            "root.opetussuunnitelmat.yksi.kasitteet": {
                parent: "root.opetussuunnitelmat.yksi.sisalto",
                label: "kasitteet"
            },
            "root.opetussuunnitelmat.yksi.sisalto.tekstikappale": {
                useData: "tekstiNimi",
                parent: "root.opetussuunnitelmat.yksi.sisalto"
            },
            "root.opetussuunnitelmat.yksi.opetus": {
                parent: "root.opetussuunnitelmat.yksi.sisalto",
                label: "opetus-eri-vuosiluokilla"
            },
            "root.pohjat.yksi.sisalto": {
                useData: "opsNimi"
            },
            "root.pohjat.yksi.tiedot": {
                parent: "root.pohjat.yksi.sisalto",
                label: "pohjan-tiedot"
            },
            "root.pohjat.yksi.tekstikappale": {
                useData: "tekstiNimi",
                parent: "root.pohjat.yksi.sisalto"
            },
            "root.opetussuunnitelmat.yksi.opetus.vuosiluokkakokonaisuus": {
                useData: "vlkNimi",
                useId: "vlkId",
                parent: "root.opetussuunnitelmat.yksi.opetus"
            },
            "root.opetussuunnitelmat.yksi.opetus.valinnaiset": {
                parent: "root.opetussuunnitelmat.yksi.opetus.vuosiluokkakokonaisuus",
                label: "valinnaiset-oppiaineet"
            },
            "root.opetussuunnitelmat.yksi.uusioppiaine": {
                parent: "root.opetussuunnitelmat.yksi.opetus.vuosiluokkakokonaisuus",
                label: "uusi-oppiaine"
            },
            "root.opetussuunnitelmat.yksi.opetus.oppiaine.oppiaine": {
                useData: "oppiaineNimi",
                parent: "root.opetussuunnitelmat.yksi.opetus.vuosiluokkakokonaisuus"
            },
            "root.opetussuunnitelmat.yksi.opetus.oppiaine.vuosiluokka.tavoitteet": {
                useData: "vuosiluokkaNimi",
                parent: "root.opetussuunnitelmat.yksi.opetus.oppiaine.oppiaine"
            },
            "root.opetussuunnitelmat.yksi.opetus.oppiaine.vuosiluokkaistaminen": {
                label: "vuosiluokkaistaminen",
                parent: "root.opetussuunnitelmat.yksi.opetus.oppiaine.oppiaine"
            },
            "root.opetussuunnitelmat.yksi.opetus.oppiaine.vuosiluokka.sisaltoalueet": {
                useData: "vuosiluokkaNimi",
                parent: "root.opetussuunnitelmat.yksi.opetus.oppiaine.oppiaine"
            }
        };

        function getPath(state) {
            var tree = [];
            if (!state) {
                return tree;
            }
            var current = STATES[state];
            if (!current) {
                return tree;
            } else {
                tree.push(_.extend({ state: state }, current));
                var parents = getPath(current.parent);
                if (!_.isEmpty(parents)) {
                    tree = tree.concat(parents);
                }
            }
            return tree;
        }

        function setTitle() {
            var titleEl = angular.element("head > title");
            var leaf = _.last($scope.crumbs);
            var last = leaf ? Kaanna.kaanna(leaf.label) : null;
            titleEl.html(Kaanna.kaanna("ops-tyokalu") + (last ? " – " + last : ""));
        }

        function update() {
            var toState = currentState;
            if (!toState) {
                return;
            }
            $scope.crumbs = [];

            var path = getPath(toState.name);
            _(path)
                .reverse()
                .each(function(item) {
                    var params = {};
                    if (item.useId) {
                        params[item.useId] = MurupolkuData.get(item.useId);
                    }

                    // Jos ollaan luomassa uutta OPS:ia, niin ei laiteta murupolkuun linkkiä sisältöön. Uutta luodessa ei sisältöä ole vielä olemassa.
                    if (
                        !(
                            item.useData &&
                            item.useData === "opsNimi" &&
                            item.state === "root.opetussuunnitelmat.yksi.sisalto" &&
                            MurupolkuData.get(item.useData) === "uusi"
                        )
                    ) {
                        $scope.crumbs.push({
                            url: $state.href(item.state, params),
                            label: item.useData
                                ? MurupolkuData.get(item.useData)
                                : item.label ? item.label : _.last(item.state.split("."))
                        });
                    }
                })
                .value();

            setTitle();
        }

        $scope.isLocal = Oikeudet.isLocal();
        if ($scope.isLocal) {
            Oikeudet.setVirkailija(true);
        }
        $scope.isVirkailija = Oikeudet.isVirkailija();
        $scope.$on("fetched:casTiedot", function() {
            $scope.isLocal = Oikeudet.isLocal();
            if ($scope.isLocal) {
                Oikeudet.setVirkailija(true);
            }
            $scope.isVirkailija = Oikeudet.isVirkailija();
        });

        $scope.$on("murupolku:update", update);

        $scope.$on("$stateChangeSuccess", function(event, toState) {
            currentState = toState;
            update();
        });
        currentState = $state.current;
        update();

        $scope.$watch("isVirkailija", function(value) {
            Oikeudet.setVirkailija(value);
        });
    });
