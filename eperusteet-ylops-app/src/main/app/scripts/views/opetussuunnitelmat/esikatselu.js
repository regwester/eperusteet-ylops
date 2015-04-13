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
.controller('EtusivuController', function ($scope, Oikeudet, $state) {
  $scope.isVirkailija = Oikeudet.isVirkailija;

  $scope.addNewPohja = function () {
    $state.go('root.pohjat.yksi.tiedot', {pohjaId: 'uusi'});
  };
})

.controller('EsikatseluController', function ($scope, $state, Algoritmit, Utils) {
  function createTextTree(node, nodemap) {
    var arr = [];
    Algoritmit.traverse(node, 'lapset', function (lapsi, depth) {
      arr.push({
        label: lapsi.tekstiKappale ? lapsi.tekstiKappale.nimi : '[tyhjä viite]',
        id: lapsi.id,
        url: $state.href('root.opetussuunnitelmat.yksi.esikatselu.tekstikappale', {tekstikappaleId: lapsi.id}),
        depth: depth,
        valmis: lapsi.valmis
      });
      nodemap[lapsi.id] = lapsi;
    });
    return arr;
  }
  $scope.oppiaineSort = function (item) {
    return Utils.sort(item.oppiaine);
  };
  $scope.tekstikappaleMap = {};
  $scope.texttree = createTextTree($scope.model.tekstit, $scope.tekstikappaleMap);
})

.controller('EsikatseluTekstikappaleController', function ($scope, $stateParams) {
  $scope.tekstikappaleViite = $scope.tekstikappaleMap[$stateParams.tekstikappaleId];
  $scope.tekstikappale = $scope.tekstikappaleViite.tekstiKappale;
})

.controller('EsikatseluVlkController', function ($scope, $stateParams, baseLaajaalaiset, Utils,
  VuosiluokkakokonaisuusMapper) {
  $scope.vuosiluokkakokonaisuus = _.find($scope.model.vuosiluokkakokonaisuudet, function (vlk) {
    return '' + vlk.vuosiluokkakokonaisuus.id === '' + $stateParams.vlkId;
  });
  $scope.vlk = $scope.vuosiluokkakokonaisuus ? $scope.vuosiluokkakokonaisuus.vuosiluokkakokonaisuus : {};
  var laajaalaisetosaamiset = _.indexBy(baseLaajaalaiset, 'tunniste');
  var laajaalaisetOrder = _(baseLaajaalaiset).sortBy(Utils.sort).map('tunniste').value();
  $scope.orderFn = function (tunniste) {
    return laajaalaisetOrder.indexOf(tunniste);
  };
  VuosiluokkakokonaisuusMapper.init($scope, laajaalaisetosaamiset);
})

.controller('EsikatseluOppiaineController', function ($scope, $stateParams, perusteOppiaine) {
  var oppiaine = _.find($scope.model.oppiaineet, function (oa) {
    return '' + oa.oppiaine.id === '' + $stateParams.oppiaineId;
  });
  $scope.perusteOppiaine = perusteOppiaine;
  $scope.oppiaine = oppiaine.oppiaine;
  $scope.vlkMap = _.indexBy($scope.vuosiluokkakokonaisuudet, function (vlk) {
    return vlk.vuosiluokkakokonaisuus._tunniste;
  });
  $scope.perusteOppiaineVlkMap = _.indexBy($scope.perusteOppiaine.vuosiluokkakokonaisuudet, '_vuosiluokkakokonaisuus');
})


.directive('epTekstiotsikko', function () {
  return {
    restrict: 'E',
    scope: {
      model: '=',
      level: '@'
    },
    template: '<span class="otsikko-wrap"><span ng-bind-html="model.tekstiKappale.nimi | kaanna | unsafe"></span>' +
    '  <span class="teksti-linkki">' +
    '    <a ui-sref="^.tekstikappale({tekstikappaleId: model.id})" icon-role="new-window"></a>' +
    '  </span></span>',
    link: function (scope, element) {
      var headerEl = angular.element('<h' + scope.level + '>');
      element.find('.otsikko-wrap').wrap(headerEl);
    }
  };
})

.directive('esitysTeksti', function () {
  return {
    restrict: 'A',
    scope: {
      model: '=esitysTeksti',
      perusteModel: '=esitysPeruste',
      showPeruste: '='
    },
    template: '<div ng-if="hasText()"><h2 ng-bind-html="perusteModel.otsikko | kaanna | unsafe"></h2>' +
      '<div class="esitys-peruste" ng-if="showPeruste" ng-bind-html="perusteModel.teksti | kaanna | unsafe"></div>' +
      '<div class="esitys-paikallinen" ng-bind-html="model.teksti | kaanna | unsafe"></div></div>',
    controller: function ($scope, Kieli) {
      $scope.hasText = function () {
        var hasPeruste = $scope.perusteModel && !_.isEmpty($scope.perusteModel.teksti) && !_.isEmpty($scope.perusteModel.teksti[Kieli.getSisaltokieli()]);
        var hasPaikallinen = $scope.model && !_.isEmpty($scope.model.teksti) && !_.isEmpty($scope.model.teksti[Kieli.getSisaltokieli()]);
        return (!$scope.showPeruste && hasPaikallinen) || ($scope.showPeruste && (hasPeruste || hasPaikallinen));
      };
    }
  };
});
