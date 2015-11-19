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

interface NavigaatioItem {
  label: string, // lokalisaatioavain
  url: string, // $state.href(...) or real url
  depth?: number,
  active?: boolean
  valmis?: boolean
}

ylopsApp
.directive('opsNavigaatio', function () {
  return {
    restrict: 'AE',
    templateUrl: 'views/opetussuunnitelmat/directives/navigaatio.html',
    controller: 'OpsNavigaatioController',
    scope: {
      shouldShow: '=',
      items: '='
    },
    transclude: true
  };
})

.controller('OpsNavigaatioController', function ($scope, $state, $stateParams
  /*, MurupolkuData*/) {
  $scope.isActive = false;
  $scope.chosen = 0;
  $scope.collapsed = true;
  $scope.showTakaisin = false;

  var shouldShow = $scope.shouldShow || _.constant(true);

  function updateActive() {
    if (shouldShow()) {
      var currentUrl = $state.href($state.current, $stateParams);
      var deepest = _.reduce($scope.items, function(acc, item) {
        return item.url && _.startsWith(currentUrl, item.url.split('?')[0]) ?
          (acc && (acc.depth || 0) > (item.depth || 0) ? acc : item) :
          acc;
      });
      if (deepest) {
        _.each($scope.items, function(item) { item.active = false; });
        deepest.active = true;
        $scope.isActive = true;
      }
    }
    else {
      $scope.isActive = false;
    }
  }
  updateActive();

  $scope.toggle = function() {
    $scope.collapsed = !$scope.collapsed;
  };

  $scope.$on('$stateChangeStart', function() { $scope.collapsed = true; });
  $scope.$on('$stateChangeSuccess', updateActive);
  $scope.$on('navigaatio:hide', function() { $scope.isActive = false; });
  $scope.$on('navigaatio:show', function() { $scope.isActive = true; });
});
