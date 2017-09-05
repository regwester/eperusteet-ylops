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

ylopsApp.directive("collapseToggler", function($compile) {
    return {
        restrict: "A",
        scope: {
            isCollapsed: "=collapseToggler"
        },
        link: function(scope: any, element) {
            var toggler = angular
                .element("<span>")
                .addClass("glyphicon")
                .addClass("collapse-toggler")
                .attr("ng-class", "isCollapsed ? 'glyphicon-chevron-right' : 'glyphicon-chevron-down'");
            element.prepend($compile(toggler)(scope));

            var toggleFn = function() {
                scope.$apply(scope.toggle);
            };
            element.bind("click", toggleFn);

            scope.$on("$destroy", function() {
                element.unbind("click", toggleFn);
            });
        },
        controller: function($scope) {
            $scope.toggle = function() {
                $scope.isCollapsed = !$scope.isCollapsed;
            };
        }
    };
});
