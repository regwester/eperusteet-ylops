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

ylopsApp
.directive('opsNavigaatio', function () {
  return {
    restrict: 'AE',
    templateUrl: 'views/opetussuunnitelmat/directives/navigaatio.html',
    controller: 'OpsNavigaatioController',
    scope: {
      model: '=',
      vuosiluokat: '='
    },
    transclude: true
  };
})

.controller('OpsNavigaatioController', function ($scope, OpsNavigaatio, $state, $stateParams, Algoritmit, VuosiluokatService) {
  $scope.isActive = true;
  $scope.chosen = 0;
  $scope.collapsed = true;

  function listener(value) {
    $scope.isActive = value;
  }
  OpsNavigaatio.listen(listener);
  $scope.$on('$destroy', function () {
    OpsNavigaatio.stopListening();
  });

  function findChild(node, viiteId) {
    var found = null;
    _.each(node.lapset, function (lapsi) {
      if ('' + lapsi.id === '' + viiteId) {
        found = lapsi;
      }
    });
    if (!found) {
      var childFound = false;
      _.each(node.lapset, function (lapsi) {
        if (findChild(lapsi, viiteId)) {
          childFound = true;
        }
      });
      found = childFound;
    }
    return found;
  }

  function startsWith(str1, str2) {
    return str1.substr(0, str2.length) === str2;
  }

  function stateMatch(id, paramName) {
    return '' + id === '' + $stateParams[paramName];
  }

  function findActiveTeksti() {
    var inTekstikappale = $state.is('root.opetussuunnitelmat.yksi.tekstikappale');
    _.each($scope.items, function (item, index) {
      item.active = $stateParams.alueId === '' + item.id;
      if (item.active) {
        $scope.chosen = index;
      }
      _.each(item.items, function (alilapsi) {
        alilapsi.active = false;
      });
      if (inTekstikappale) {
        var root = _.find($scope.model.tekstit.lapset, {id: item.id});
        if (root) {
          var found = findChild(root, $stateParams.tekstikappaleId);
          item.active = !!found;
          if (item.active) {
            $scope.chosen = index;
          }
          _.each(item.items, function (alilapsi) {
            alilapsi.active = stateMatch(alilapsi.id, 'tekstikappaleId');
          });
        }
      }
    });
  }

  function findActiveVuosiluokkaOrOppiaine(isOppiaine) {
    _.each($scope.items[$scope.chosen].items, function (item) {
      item.active = (isOppiaine && stateMatch(item.id, 'oppiaineId')) ||
                    (!isOppiaine && stateMatch(item.id, 'vlkId'));
    });
  }

  function updateActive() {
    _.each($scope.items, function (item) {
      item.active = false;
    });
    var inVuosiluokat = startsWith($state.current.name, 'root.opetussuunnitelmat.yksi.vuosiluokkakokonaisuus');
    var inOppiaine = startsWith($state.current.name, 'root.opetussuunnitelmat.yksi.oppiaine');
    if (inVuosiluokat || inOppiaine) {
      $scope.chosen = $scope.items.length - 1;
      $scope.items[$scope.chosen].active = true;
      findActiveVuosiluokkaOrOppiaine(inOppiaine);
    } else {
      findActiveTeksti();
    }
  }

  function createNavimenu(node) {
    var arr = [];
    Algoritmit.traverse(node, 'lapset', function (lapsi, depth) {
      arr.push({
        label: lapsi.tekstiKappale.nimi,
        id: lapsi.id,
        url: $state.href('root.opetussuunnitelmat.yksi.tekstikappale', {tekstikappaleId: lapsi.id}),
        depth: depth
      });
    });
    return arr;
  }

  function mapLapset(node) {
    return _.map(node, function (lapsi) {
      return {
        label: lapsi.tekstiKappale.nimi,
        id: lapsi.id,
        url: $state.href('root.opetussuunnitelmat.yksi.sisaltoalue', {alueId: lapsi.id}),
        items: createNavimenu(lapsi)
      };
    });
  }

  $scope.$watch('model.tekstit.lapset', function () {
    if ($scope.model && $scope.model.tekstit) {
      $scope.items = mapLapset($scope.model.tekstit.lapset);
      var vuosiluokat = {
        label: 'vuosiluokat-ja-oppiaineet',
        id: 'vuosiluokat',
        url: $state.href('root.opetussuunnitelmat.yksi.sisaltoalue', {alueId: 'vuosiluokat'}),
        items: VuosiluokatService.mapForMenu($scope.vuosiluokat)
      };
      $scope.items.push(vuosiluokat);
      updateActive();
    }
  }, true);

  $scope.$on('$stateChangeSuccess', function () {
    updateActive();
    $scope.collapsed = true;
  });

  $scope.toggle = function () {
    $scope.collapsed = !$scope.collapsed;
  };

})

.service('OpsNavigaatio', function () {
  var active = true;
  var callback = angular.noop;

  this.setActive = function (value) {
    active = _.isUndefined(value) || !!value;
    callback(active);
  };

  this.listen = function (cb) {
    var first = callback === angular.noop;
    callback = cb;
    if (first) {
      callback(active);
    }
  };

  this.stopListening = function () {
    callback = angular.noop;
  };
});
