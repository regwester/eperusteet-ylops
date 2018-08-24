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
            treeProvider: "=",
            uiSortableConfig: "=?"
        },
        templateUrl: "views/common/directives/lukiotree.html",
        controller: ($scope, $log, Algoritmit) => {
            $scope.editointi = false;
            const initProvider = (provider) => {
                provider
                    .root()
                    .then(root => {
                        $scope.root = root;
                        Algoritmit.traverse($scope.root, "lapset", (lapsi, depth, index, arr) => {});
                    })
                    .catch(err => {
                        $log.error(err);
                    });
            };

            // Haetaan puun juuri ja kiinnitetään se scopeen.
            initProvider($scope.treeProvider);

            $scope.sortableConfig = _.merge({
                    handle: ".tree-handle",
                    helper: "clone",
                    axis: "y",
                    cursor: "move",
                    delay: 100,
                    placeholder: "lukio-tree-placeholder",
                    forcePlaceholderSize: true,
                    opacity: 0.5,
                    tolerance: "pointer",
                    //connectWith: ".recursivetree",
                    //cursorAt: { top: 2, left: 2 },
                    //disabled: $scope.treeProvider.useUiSortable()
                    start: (e, ui) => {
                        ui.placeholder.height(ui.item.height() - 10);
                    }
                }, /*$scope.uiSortableConfig ||*/ {});

            $scope.poista = (node) => {
                let foundIndex, foundList;
                Algoritmit.traverse($scope.root, "lapset", (lapsi, depth, index, arr) => {
                    if (lapsi === node) {
                        foundIndex = index;
                        foundList = arr;
                        return true;
                    }
                });
                if (foundList) {
                    foundList.splice(foundIndex, 1);
                }
            };

            $scope.$on("enableEditing", () => {
                $scope.editointi = true;
            });
            $scope.$on("disableEditing", () => {
                $scope.editointi = false;
            });
        }
    };
});
