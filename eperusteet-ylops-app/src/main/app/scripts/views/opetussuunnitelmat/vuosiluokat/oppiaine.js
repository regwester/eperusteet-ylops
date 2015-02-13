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
.service('OppiaineService', function (VuosiluokatService, $rootScope, MurupolkuData, $q) {
  var vlkTunniste = null;
  var oppiaineenVlk = null;
  var oppiaine = null;

  function setup(ops, vlkId, oppiaineModel, promise) {
    oppiaine = oppiaineModel;
    MurupolkuData.set('oppiaineNimi', oppiaine.nimi);
    var opsVlk = _.find(ops.vuosiluokkakokonaisuudet, function (vlk) {
      return '' + vlk.vuosiluokkakokonaisuus.id === vlkId;
    });
    vlkTunniste = opsVlk ? opsVlk.vuosiluokkakokonaisuus._tunniste : null;
    oppiaineenVlk = _.find(oppiaine.vuosiluokkakokonaisuudet, function (opVlk) {
      return opVlk._vuosiluokkakokonaisuus === vlkTunniste;
    });
    promise.resolve();
  }

  this.refresh = function (ops, oppiaineId, vlkId) {
    var promise = $q.defer();
    VuosiluokatService.getOppiaine(oppiaineId).$promise.then(function (res) {
      setup(ops, vlkId, res, promise);
      $rootScope.$broadcast('oppiainevlk:updated', oppiaineenVlk);
    });
    return promise.promise;
  };
  this.getOpVlk = function () {
    return oppiaineenVlk;
  };
  this.getOppiaine = function () {
    return oppiaine;
  };
})

.controller('OppiaineBaseController', function ($scope, perusteOppiaine, MurupolkuData, $stateParams,
  $rootScope, OppiaineService) {

  $scope.perusteOppiaine = perusteOppiaine;
  $scope.oppiaine = OppiaineService.getOppiaine();
  $scope.oppiaineenVlk = OppiaineService.getOpVlk();

  $scope.$on('oppiainevlk:updated', function (event, value) {
    $scope.oppiaineenVlk = value;
    $scope.oppiaine = OppiaineService.getOppiaine();
  });

  $scope.$on('oppiaine:reload', function () {
    OppiaineService.refresh($scope.model, $stateParams.oppiaineId, $stateParams.vlkId);
  });
})

.controller('OppiaineController', function ($scope, $state, $stateParams, Editointikontrollit, Varmistusdialogi,
  VuosiluokatService, Kaanna, OppiaineService) {

  $scope.vuosiluokat = [];

  function toPlaintext(text) {
    return String(text).replace(/<[^>]+>/gm, '');
  }

  function getCode(text) {
    var match = text.match(/([A-Za-z]\d+)\s+/);
    return match ? match[1] : null;
  }

  function updateVuosiluokat() {
    $scope.vuosiluokat = $scope.oppiaineenVlk.vuosiluokat;
    _.each($scope.vuosiluokat, function (vlk) {
      vlk.$numero = VuosiluokatService.fromEnum(vlk.vuosiluokka);
      _.each(vlk.tavoitteet, function (tavoite) {
        var tavoiteTeksti = toPlaintext(Kaanna.kaanna(tavoite.tavoite));
        tavoite.$short = getCode(tavoiteTeksti);
      });
      _.each(vlk.sisaltoalueet, function (alue) {
        alue.$short = getCode(Kaanna.kaanna(alue.nimi));
      });
    });
  }
  updateVuosiluokat();

  $scope.$on('oppiainevlk:updated', function (event, value) {
    $scope.oppiaineenVlk = value;
    updateVuosiluokat();
  });

  $scope.tekstit = {
    ohjaus: {
      teksti: {fi: 'Oppilaan matematiikan osaamista ja taitojen kehittymistä seurataan ja tarvittaessa annetaan lisätukea heti tuen tarpeen ilmetessä. Tarjottava tuki antaa oppilaalle mahdollisuuden ymmärtää matematiikkaa ikätasonsa mukaisesti ja kehittää taitojaan niin, että oppimisen ja osaamisen ilo säilyvät. Oppilaille tarjotaan sopivia välineitä oppimisen tueksi ja hänelle tarjotaan mahdollisuuksia oivaltaa ja ymmärtää itse.'}
    },
    tyotavat: {

    },
    arviointi: {

    }
  };

  $scope.callbacks = {
    edit: function () {

    },
    save: function () {

    },
    cancel: function () {

    },
    notify: function (mode) {
      $scope.callbacks.notifier(mode);
    },
    notifier: angular.noop
  };
  Editointikontrollit.registerCallback($scope.callbacks);


  $scope.goToDummy = function (id) {
    $state.go('root.opetussuunnitelmat.yksi.oppiaine.vuosiluokka', {
      vlkId: $stateParams.vlkId,
      vlId: id
    });
  };

  function vuosiluokkaistamisVaroitus(cb) {
    Varmistusdialogi.dialogi({
      otsikko: 'vuosiluokkaistaminen-on-jo-tehty',
      teksti: 'vuosiluokkaistaminen-varoitus',
      primaryBtn: 'jatka',
      successCb: cb
    })();
  }

  $scope.startVuosiluokkaistaminen = function () {
    function start() {
      $state.go('root.opetussuunnitelmat.yksi.oppiaine.vuosiluokkaistaminen', {
        vlkId: $stateParams.vlkId,
      });
    }
    if (_.isArray($scope.vuosiluokat) && $scope.vuosiluokat.length > 0) {
      vuosiluokkaistamisVaroitus(function () {
        // TODO reset vuosiluokat
        start();
      });
    } else {
      start();
    }
  };
});
