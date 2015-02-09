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
.controller('VuosiluokkakokonaisuusController', function ($scope, Editointikontrollit,
  MurupolkuData, vlk, $stateParams, Notifikaatiot, VuosiluokatService, Utils, Kaanna, $rootScope) {

  $scope.siirtymat = ['siirtymaEdellisesta', 'siirtymaSeuraavaan'];
  $scope.vlk = vlk;
  $scope.temp = {};
  $scope.paikalliset = {};
  $scope.orderFn = function (tunniste) {
    return Kaanna.kaanna($scope.laajaalaiset[tunniste].nimi).toLowerCase();
  };

  function fetch() {
    VuosiluokatService.getVuosiluokkakokonaisuus($stateParams.id, $stateParams.vlkId, function (res) {
      $scope.vlk = res;
      initTexts();
      initPeruste();
    }, Notifikaatiot.serverCb);
  }

  function initTexts() {
    _.each($scope.siirtymat, function (key) {
      $scope.temp[key] = $scope.vlk[key] || {};
    });
  }
  initTexts();

  function initPeruste() {
    VuosiluokatService.getVlkPeruste($stateParams.id, $stateParams.vlkId, function (res) {
      $scope.perusteVlk = res;

      // TODO tunnisteet pitää olla uideja
      $scope.tunnisteet = _.map($scope.perusteVlk.laajaalaisetOsaamiset, 'id');
      var decorated = _.map($scope.perusteVlk.laajaalaisetOsaamiset, function (item) {
        item.teksti = item.kuvaus;
        item.otsikko = item.nimi || {fi: '[Ei nimeä]'};
        return item;
      });
      $scope.laajaalaiset = _.indexBy(decorated, 'id');
      $scope.paikalliset = _.mapValues($scope.laajaalaiset, function () {
        // TODO olemassaolevat
        return {};
      });
    });
  }
  initPeruste();

  MurupolkuData.set('vlkNimi', vlk.nimi);

  $scope.hasSiirtymat = function () {
    return $scope.perusteVlk && ($scope.perusteVlk.siirtymaEdellisesta || $scope.perusteVlk.siirtymaSeuraavaan);
  };

  function commitLaajaalaiset() {
    if (!$scope.vlk.laajaalaisetosaamiset) {
      $scope.vlk.laajaalaisetosaamiset = [];
    }
    _.each($scope.paikalliset, function (value, tunniste) {
      if (value.teksti) {
        var model = _.find($scope.vlk.laajaalaisetosaamiset, function (item) {
          return '' + item.tunniste === '' + tunniste;
        });
        if (model) {
          model.teksti = value.teksti;
        } else {
          model = {
            tunniste: tunniste,
            teksti: value.teksti
          };
          // TODO enable
          //$scope.vlk.laajaalaisetosaamiset.push(model);
        }
      }
    });
  }

  $scope.callbacks = {
    edit: function () {
      fetch();
    },
    save: function () {
      $rootScope.$broadcast('notifyCKEditor');
      commitLaajaalaiset();
      _.each($scope.temp, function (value, key) {
        if (value !== null) {
          $scope.vlk[key] = value;
        }
      });
      $scope.vlk.$save({opsId: $stateParams.id}, function (res) {
        $scope.vlk = res;
        initTexts();
      }, Notifikaatiot.serverCb);
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

});
