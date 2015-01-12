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
      model: '='
    },
    transclude: true
  };
})

.controller('OpsNavigaatioController', function ($scope, OpsNavigaatio, $state, $stateParams) {
  $scope.isActive = true;
  $scope.chosen = 0;

  function listener(value) {
    $scope.isActive = value;
  }
  OpsNavigaatio.listen(listener);
  $scope.$on('$destroy', function () {
    OpsNavigaatio.stopListening();
  });

  /*var found = null;
  function findChild(node, viiteId) {
    _.each(node.lapset, function (lapsi) {
      if (lapsi.id === viiteId) {
        found = lapsi;
      }
      findChild(lapsi, viiteId);
    });
  }*/

  function updateActive() {
    //var inTekstikappale = $state.is('root.opetussuunnitelmat.yksi.tekstikappale');
    _.each($scope.items, function (item, index) {
      item.active = $stateParams.alueId === '' + item.id;
      if (item.active) {
        $scope.chosen = index;
      }
      // TODO etsi oikea osio teksikappaleelle
      /*if (inTekstikappale) {
        var root = _.find($scope.model.tekstit.lapset, {id: item.id});
        if (root) {
          found = null;
          findChild(root, $stateParams.tekstikappaleId);
          item.active = found !== null;
        }
      }*/
    });
  }

  $scope.$watch('model.tekstit.lapset', function () {
    if ($scope.model && $scope.model.tekstit) {
      $scope.items = _.map($scope.model.tekstit.lapset, function (lapsi) {
        return {
          label: lapsi.tekstiKappale.nimi,
          id: lapsi.id,
          items: _.map(lapsi.lapset, function (alilapsi) {
            return {
              label: alilapsi.tekstiKappale.nimi,
              id: alilapsi.id,
              url: $state.href('root.opetussuunnitelmat.yksi.tekstikappale', {tekstikappaleId: alilapsi.id})
            };
          }),
          url: $state.href('root.opetussuunnitelmat.yksi.sisaltoalue', {alueId: lapsi.id})
        };
      });
      $scope.items.push({
        label: 'vuosiluokat-ja-oppiaineet',
        id: 'vuosiluokat',
        items: [],
        url: $state.href('root.opetussuunnitelmat.yksi.sisaltoalue', {alueId: 'vuosiluokat'})
      });
      updateActive();
    }
  }, true);

  $scope.$on('$stateChangeSuccess', function () {
    updateActive();
  });

  _.each($scope.items, function (item, index) {
    item.items = _.map(_.range(10), function (num) {
      return {label: 'Aliotsikko ' + index + '.' + num};
    });
  });
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
