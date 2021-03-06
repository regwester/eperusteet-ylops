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
    .directive("perusteenTekstiosa", function($timeout, $window) {
        return {
            restrict: "A",
            scope: {
                model: "=perusteenTekstiosa",
                muokattava: "=?",
                callbacks: "=",
                startCollapsed: "=?",
                ohjepallo: "=",
                shouldDisable: "="
            },
            templateUrl: "views/opetussuunnitelmat/directives/tekstiosa.html",
            controller: "TekstiosaController",
            link: function(scope: any, element, attrs: any) {
                scope.editable = !!attrs.muokattava;
                scope.focusAndScroll = function() {
                    $timeout(function() {
                        var el = element.find("[ckeditor]");
                        if (el && el.length > 0) {
                            el[0].focus();
                            $window.scrollTo(0, el.eq(0).offset().top - 200);
                        }
                    }, 300);
                };
            }
        };
    })
    .directive("yksinkertainenTekstiosa", function($timeout, $window) {
        return {
            restrict: "A",
            scope: {
                model: "=yksinkertainenTekstiosa",
                callbacks: "=",
                startCollapsed: "=?",
                otsikko: "@?",
                shouldDisable: "="
            },
            templateUrl: "views/opetussuunnitelmat/directives/yksinkertainentekstiosa.html",
            controller: "TekstiosaController",
            link: function(scope: any, element) {
                scope.focusAndScroll = function() {
                    $timeout(function() {
                        var el = element.find("[ckeditor]");
                        if (el && el.length > 0) {
                            el[0].focus();
                            $window.scrollTo(0, el.eq(0).offset().top - 200);
                        }
                    }, 300);
                };
            }
        };
    })
    .controller("TekstiosaController", function($state, $scope, $q, Editointikontrollit, Kieli) {
        $scope.editMode = false;
        $scope.collapsed = _.isUndefined($scope.startCollapsed) ? true : $scope.startCollapsed;

        function validoi() {
            if ($scope.muokattava) {
                $scope.muokattava.otsikko = $scope.muokattava.otsikko || {};
                $scope.muokattava.otsikko.$$validointi = Kieli.validoi($scope.muokattava.otsikko);
                $scope.muokattava.teksti = $scope.muokattava.teksti || {};
                $scope.muokattava.teksti.$$validointi = Kieli.validoi($scope.muokattava.teksti);
            }
        }

        $scope.$watch("muokattava", validoi, true);

        function notifyFn(mode) {
            $scope.editMode = mode;
            if (!mode) {
                $scope.callbacks.notifier = angular.noop;
            }
        }

        $scope.startEditing = function() {
            $scope.editMode = true;
            $scope.callbacks.notifier = notifyFn;
            $scope.focusAndScroll();
            validoi();
            Editointikontrollit.startEditing().then(() => $q.when());
        };

        $scope.remove = function() {
            $scope.callbacks.remove($scope.muokattava);
        };
    });
