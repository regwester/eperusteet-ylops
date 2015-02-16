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
.controller('VuosiluokkaBaseController', function ($scope, $stateParams, MurupolkuData, $state, Kaanna,
  VuosiluokatService, baseLaajaalaiset) {
  $scope.vuosiluokka = _.find($scope.oppiaineenVlk.vuosiluokat, function (vuosiluokka) {
    return '' + vuosiluokka.id === $stateParams.vlId;
  });
  $scope.vuosiluokkaNro = VuosiluokatService.fromEnum($scope.vuosiluokka.vuosiluokka);
  MurupolkuData.set('vuosiluokkaNimi', Kaanna.kaanna('vuosiluokka') + ' ' + $scope.vuosiluokkaNro);
  $scope.perusteOpVlk = _.find($scope.perusteOppiaine.vuosiluokkakokonaisuudet, function (vlk) {
    return vlk._vuosiluokkakokonaisuus === $scope.oppiaineenVlk._vuosiluokkakokonaisuus;
  });
  $scope.laajaalaiset = _.indexBy(baseLaajaalaiset, 'tunniste');
  $scope.perusteSisaltoalueet = _.indexBy($scope.perusteOpVlk.sisaltoalueet, 'tunniste');

  $scope.isState = function (name) {
    return _.endsWith($state.current.name, 'vuosiluokka.' + name);
  };

  if ($state.is('root.opetussuunnitelmat.yksi.oppiaine.vuosiluokka')) {
    $state.go('root.opetussuunnitelmat.yksi.oppiaine.vuosiluokka.tavoitteet');
  }
})

.controller('VuosiluokkaTavoitteetController', function ($scope, VuosiluokatService, Editointikontrollit, Utils) {
  $scope.tunnisteet = [];
  $scope.collapsed = {};
  $scope.muokattavat = {};
  $scope.nimiOrder = Utils.sort;

  function fetch() {
    $scope.tavoitteet = $scope.vuosiluokka.tavoitteet;
    processTavoitteet();
  }
  fetch();

  function processTavoitteet() {
    var perusteKohdealueet = _.indexBy($scope.perusteOppiaine.kohdealueet, 'id');
    _.each($scope.tavoitteet, function (item) {
      var perusteTavoite = _.find($scope.perusteOpVlk.tavoitteet, function (pTavoite) {
        return pTavoite.tunniste === item.tunniste;
      });
      item.$sisaltoalueet = _.map(perusteTavoite.sisaltoalueet, function (tunniste) {
        return $scope.perusteSisaltoalueet[tunniste];
      });
      item.$kohdealue = perusteKohdealueet[_.first(perusteTavoite.kohdealueet)];
      item.$laajaalaiset = _.map(perusteTavoite.laajaalaisetosaamiset, function (tunniste) {
        return $scope.laajaalaiset[tunniste];
      });
      item.$arvioinninkohteet = perusteTavoite.arvioinninkohteet;
    });
    $scope.perusteTavoiteMap = _.indexBy($scope.tavoitteet, 'tunniste');
    $scope.tunnisteet = _.keys($scope.perusteTavoiteMap);
    _.each($scope.tunnisteet, function (tunniste) {
      // TODO map existing
      $scope.muokattavat[tunniste] = {};
    });
  }

  $scope.callbacks = {
    edit: function () {
      fetch();
    },
    save: function () {
    },
    cancel: function () {
      fetch();
    },
    notify: function (mode) {
      $scope.callbacks.notifier(mode);
    },
    notifier: angular.noop
  };
  Editointikontrollit.registerCallback($scope.callbacks);

})

.controller('VuosiluokkaSisaltoalueetController', function ($scope, Editointikontrollit) {
  $scope.tunnisteet = [];
  $scope.muokattavat = {};

  $scope.sisaltoalueet = $scope.vuosiluokka.sisaltoalueet;
  $scope.tunnisteet = _.map($scope.sisaltoalueet, 'tunniste');

  $scope.callbacks = {
    edit: function () {
      //fetch();
    },
    save: function () {
    },
    cancel: function () {
      //fetch();
    },
    notify: function (mode) {
      $scope.callbacks.notifier(mode);
    },
    notifier: angular.noop
  };
  Editointikontrollit.registerCallback($scope.callbacks);

})

.directive('opsTeksti', function () {
  return {
    restrict: 'A',
    scope: {
      muokattava: '=opsTeksti',
      callbacks: '=',
    },
    templateUrl: 'views/opetussuunnitelmat/vuosiluokat/directives/opsteksti.html',
    controller: 'TekstiosaController',
    link: function (scope, element, attrs) {
      scope.editable = !!attrs.opsTeksti;
      scope.options = {
        collapsed: scope.editable
      };
    }
  };
})

.directive('popoverHtml', function ($document) {
  var OPTIONS = {
    html: true,
    placement: 'bottom'
  };
  return {
    restrict: 'A',
    link: function (scope, element) {
      element.popover(OPTIONS);

      // Click anywhere else to close
      $document.on('click', function (event) {
        var clickedParent = angular.element(event.target).hasClass('laajaalainen-popover');
        var clickedContent = angular.element(event.target).closest('.popover').length > 0;
        if (clickedParent || clickedContent || element.find(event.target).length > 0) {
          return;
        }
        element.popover('hide');
      });

      scope.$on('$destroy', function () {
        $document.off('click');
        element.popover('destroy');
      });
    }
  };
});
