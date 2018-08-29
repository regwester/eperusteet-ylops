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

ylopsApp.directive("lukioTree", () => {
    return {
        restrict: "E",
        replace: true,
        scope: {
            rakenne: "=",
            root: "="
        },
        templateUrl: "views/common/directives/lukiotree.html",
        controller: ($scope, $log, $modal,$stateParams, Algoritmit, LukioTreeUtils) => {
            $scope.stateParams = $stateParams;
            $scope.sortableConfig = {
                    handle: ".tree-handle",
                    helper: "clone",
                    axis: "y",
                    cursor: "move",
                    delay: 100,
                    placeholder: "lukio-tree-placeholder",
                    opacity: 0.5,
                    tolerance: "pointer",
                    disabled: true
                };

            $scope.poista = node => {
                _.remove(node.$$nodeParent.lapset, node);
            };

            $scope.lisaa = node => {
                $modal.open({
                    templateUrl: "views/opetussuunnitelmat/modals/kurssi.html",
                    controller: "LukioTreeModalController",
                    size: "lg",
                    resolve: {
                        rakenne: _.constant($scope.rakenne),
                        root: _.constant($scope.root)
                    },
                    backdrop  : 'static',
                    keyboard  : false
                }).result.then(kurssit => {
                    const kurssitMap = _.indexBy(node.lapset, "id");
                    _.each(kurssit, kurssi => {
                        if (!_.has(kurssitMap, kurssi.id)) {
                            node.lapset.push(kurssi);
                        }
                    });
                });
            };

            $scope.$on("enableEditing", () => {
                LukioTreeUtils.search($scope.root, "");
                $scope.sortableConfig.disabled = false;
            });

            $scope.$on("disableEditing", () => {
                $scope.sortableConfig.disabled = true;
            });
        }
    };
}).controller("LukioTreeModalController", ($scope, $modalInstance, $timeout, LukioTreeUtils,
                                           rakenne, root) => {
    const resolveKurssit = (node) => {
        const kurssit = _(node)
            .flattenTree(n => n.lapset)
            .filter(c => c.dtype == LukioKurssiTreeNodeType.kurssi)
            .value();

        return _.uniq(kurssit, "id");
    };

    const resolveLiittamattomatKurssit = () => {
        const kaikkiKurssit = resolveKurssit(LukioTreeUtils.buildTree(rakenne));
        const liitetytKurssit = resolveKurssit(root);

        const liitetytKurssitMap = _.indexBy(liitetytKurssit, "id");

        return _.filter(kaikkiKurssit, k => !_.has(liitetytKurssitMap, k.id));
    };

    $scope.liittamattomatHaku = "";
    $scope.liitetytHaku = "";
    $scope.lisattavatKurssit = [];
    $scope.liittamattomatKurssit = resolveLiittamattomatKurssit();
    $scope.liitetytKurssit = resolveKurssit(root);

    _.each($scope.liitetytKurssit, kurssi => {
       kurssi.$$lisatty = false;
    });

    $scope.liittamattomatHae = () => {
        $timeout(() => {
            LukioTreeUtils.search($scope.liittamattomatKurssit, $scope.liittamattomatHaku);
        });
    };

    $scope.liitetytHae = () => {
        $timeout(() => {
            LukioTreeUtils.search($scope.liitetytKurssit, $scope.liitetytHaku);
        });
    };

    $scope.lisaaKurssi = (kurssi) => {
        if (!_.includes($scope.lisattavatKurssit, { id: kurssi.id })) {
            $scope.lisattavatKurssit.push(kurssi);
            kurssi.$$lisatty = true;
        }
    };

    $scope.poistaKurssi = (kurssi) => {
        _.remove($scope.lisattavatKurssit, kurssi);
        kurssi.$$lisatty = false;
    };

    $scope.lisaa = () => {
        $modalInstance.close($scope.lisattavatKurssit);
    };
});
