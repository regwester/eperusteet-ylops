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
.service('OppiaineService', function (VuosiluokatService, $rootScope, MurupolkuData, $q, OppiaineCRUD, OppiaineenVlk,
  Notifikaatiot, VuosiluokkaCRUD) {
  var vlkTunniste = null;
  var oppiaineenVlk = null;
  var oppiaine = null;
  var opetussuunnitelma = null;

  function setup(ops, vlkId, oppiaineModel, promise) {
    opetussuunnitelma = ops;
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

  this.getParent = function(cb) {
    OppiaineCRUD.getParent({
      opsId: opetussuunnitelma.id,
      oppiaineId: oppiaine.id
    }, cb);
  };
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
  this.saveVlk = function (model) {
    OppiaineenVlk.save({
      opsId: opetussuunnitelma.id,
      oppiaineId: oppiaine.id
    }, model, function () {
      Notifikaatiot.onnistui('tallennettu-ok');
      $rootScope.$broadcast('oppiaine:reload');
    }, Notifikaatiot.serverCb);
  };
  this.fetchVlk = function (vlkId, cb) {
    OppiaineenVlk.get({
      opsId: opetussuunnitelma.id,
      oppiaineId: oppiaine.id,
      vlkId: vlkId
    }, cb, Notifikaatiot.serverCb);
  };
  this.saveVuosiluokka = function (model, cb) {
    VuosiluokkaCRUD.save({
      opsId: opetussuunnitelma.id,
      vlkId: oppiaineenVlk.id,
      oppiaineId: oppiaine.id
    }, model, function (res) {
      Notifikaatiot.onnistui('tallennettu-ok');
      cb(res);
    }, Notifikaatiot.serverCb);
  };
  this.saveValinnainenVuosiluokka = function (vlId, model, cb) {
    VuosiluokkaCRUD.saveValinnainen({
      opsId: opetussuunnitelma.id,
      vlkId: oppiaineenVlk.id,
      oppiaineId: oppiaine.id,
      vvlId: vlId
    }, model, function (res) {
      cb(res);
    }, Notifikaatiot.serverCb);
  };
  this.fetchVuosiluokka = function (vlId, cb) {
    VuosiluokkaCRUD.get({
      opsId: opetussuunnitelma.id,
      oppiaineId: oppiaine.id,
      vlkId: oppiaineenVlk.id,
      vlId: vlId
    }, cb, Notifikaatiot.serverCb);
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

.service('TextUtils', function () {
  this.toPlaintext = function (text) {
    return String(text).replace(/<[^>]+>/gm, '');
  };
  this.getCode = function (text) {
    var match = text.match(/(^|[^A-Za-z])([A-Za-z]\d{1,2})($|[^0-9])/);
    return match ? match[2] : null;
  };
})

.controller('OppiaineController', function ($scope, $state, $stateParams, Editointikontrollit, Varmistusdialogi,
  VuosiluokatService, Kaanna, OppiaineService, TextUtils, Utils, Kielitarjonta, OppiaineCRUD, OpsService, Notifikaatiot) {
  $scope.vuosiluokat = [];
  $scope.alueOrder = Utils.sort;

  function vanhempiOnUskontoTaiKieli(oppiaine) {
    return _.isString(oppiaine.koodiArvo) && _.includes(['AI', 'VK', 'TK', 'KT'], oppiaine.koodiArvo.toUpperCase());
  }

  OppiaineService.getParent(function(res) {
    $scope.oppiaine.$parent = res;
    $scope.$onKieliTaiUskonto = vanhempiOnUskontoTaiKieli(res);
    if ($scope.$onKieliTaiUskonto) {
      $scope.oppiaine.tehtava = $scope.oppiaine.tehtava || {};
    }
  });

  $scope.perusteOpVlk = $scope.perusteOppiaine ?
    _.find($scope.perusteOppiaine.vuosiluokkakokonaisuudet, function (vlk) {
      return vlk._vuosiluokkakokonaisuus === $scope.oppiaineenVlk._vuosiluokkakokonaisuus;
    }) : null;

  var perusteTavoitteet = _.indexBy($scope.perusteOpVlk ? $scope.perusteOpVlk.tavoitteet : [], 'tunniste');

  if ($scope.oppiaine.koosteinen && _.isString($scope.oppiaine.koodiArvo) && _.includes(['AI', 'VK', 'TK', 'KT'], $scope.oppiaine.koodiArvo.toUpperCase())) {
    $scope.valitseOppimaara = function() {
      Kielitarjonta.rakenna($stateParams.id, $scope.oppiaine, $scope.perusteOppiaine);
    };
  }

  $scope.poistaOppimaara = function() {
    Varmistusdialogi.dialogi({
      otsikko: 'varmista-poisto',
      primaryBtn: 'poista',
      successCb: function () {
        OppiaineCRUD.remove({
          opsId: $stateParams.id,
          oppiaineId: $stateParams.oppiaineId
        }, function() {
          Notifikaatiot.onnistui('oppimaaran-poisto-onnistui');
          $state.go($state.current.name, _.merge(_.clone($stateParams), {
            oppiaineId: $scope.oppiaine.$parent.id
          }), { reload: true });
        }, Notifikaatiot.serverCb);
      }
    })();
  };

  function updateVuosiluokat() {
    if (!$scope.oppiaineenVlk) {
      return;
    }
    $scope.vuosiluokat = $scope.oppiaineenVlk.vuosiluokat;
    _.each($scope.vuosiluokat, function (vlk) {
      vlk.$numero = VuosiluokatService.fromEnum(vlk.vuosiluokka);
      var allShort = true;
      _.each(vlk.tavoitteet, function (tavoite) {
        var perusteTavoite = perusteTavoitteet[tavoite.tunniste] || {};
        tavoite.$tavoite = perusteTavoite.tavoite;
        var tavoiteTeksti = TextUtils.toPlaintext(Kaanna.kaanna(perusteTavoite.tavoite));
        tavoite.$short = TextUtils.getCode(tavoiteTeksti);
        if (!tavoite.$short) {
          allShort = false;
        }
      });
      vlk.$tavoitteetShort = allShort;
      allShort = true;
      _.each(vlk.sisaltoalueet, function (alue) {
        alue.$short = TextUtils.getCode(Kaanna.kaanna(alue.nimi));
        if (!alue.$short) {
          allShort = false;
        }
      });
      vlk.$sisaltoalueetShort = allShort;
    });
  }
  updateVuosiluokat();

  $scope.$on('oppiainevlk:updated', function (event, value) {
    $scope.oppiaineenVlk = value;
    updateVuosiluokat();
  });

  function refetch() {
    OppiaineService.fetchVlk($scope.oppiaineenVlk.id, function (res) {
      $scope.oppiaineenVlk = res;
      updateVuosiluokat();
    });
  }

  $scope.options = {
    editing: false,
    isEditable: function () {
      return OpsService.isEditable() && $scope.oppiaine.oma;
    }
  };

  $scope.callbacks = {
    edit: function () {
      refetch();
    },
    save: function () {
      $scope.oppiaine.$save({ opsId: $stateParams.id });
      OppiaineService.saveVlk($scope.oppiaineenVlk);
    },
    cancel: function () {
      refetch();
    },
    notify: function (mode) {
      $scope.options.editing = mode;
      $scope.callbacks.notifier(mode);
    },
    notifier: angular.noop
  };
  Editointikontrollit.registerCallback($scope.callbacks);

  function vuosiluokkaistamisVaroitus(cb) {
    Varmistusdialogi.dialogi({
      otsikko: 'vuosiluokkaistaminen-on-jo-tehty',
      teksti: 'vuosiluokkaistaminen-varoitus',
      primaryBtn: 'jatka',
      successCb: cb
    })();
  }

  $scope.goToVuosiluokka = function (vuosiluokka) {
    $state.go('root.opetussuunnitelmat.yksi.oppiaine.vuosiluokka', {vlId: vuosiluokka.id});
  };

  $scope.startVuosiluokkaistaminen = function () {
    function start() {
      $state.go('root.opetussuunnitelmat.yksi.oppiaine.vuosiluokkaistaminen', {
        vlkId: $stateParams.vlkId
      });
    }
    if (_.isArray($scope.vuosiluokat) && $scope.vuosiluokat.length > 0) {
      vuosiluokkaistamisVaroitus(start);
    } else {
      start();
    }
  };

  $scope.editOppiaine = function () {
    $state.go('root.opetussuunnitelmat.yksi.uusioppiaine', {
      vlkId: $stateParams.vlkId,
      oppiaineId: $scope.oppiaine.id
    });
  };

  $scope.removeOppiaine = function () {
    Varmistusdialogi.dialogi({
      otsikko: 'varmista-poisto',
      primaryBtn: 'poista',
      successCb: function () {
        $scope.oppiaine.$delete({opsId: OpsService.getId()}, function () {
          Notifikaatiot.onnistui('poisto-onnistui');
          $state.go('root.opetussuunnitelmat.yksi.vuosiluokkakokonaisuus', {vlkId: $stateParams.vlkId}, { reload: true });
        }, Notifikaatiot.serverCb);
      }
    })();
  };

  $scope.kopioiMuokattavaksi = function () {
    Varmistusdialogi.dialogi({
      otsikko: 'varmista-kopiointi',
      primaryBtn: 'luo-kopio',
      successCb: function () {
        OppiaineCRUD.kloonaaMuokattavaksi({
          opsId: $stateParams.id,
          oppiaineId: $stateParams.oppiaineId
        }, {}, function(res) {
          Notifikaatiot.onnistui('kopion-luonti-onnistui');
          $state.go('root.opetussuunnitelmat.yksi.oppiaine.oppiaine', {
            vlkId: $stateParams.vlkId,
            oppiaineId: res.id,
            oppiaineTyyppi: res.tyyppi
          }, { reload: true });
        }, Notifikaatiot.serverCb);
      }
    })();
  };
});
