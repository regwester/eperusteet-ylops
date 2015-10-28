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
.controller('OppiaineBaseController', function (
    $scope,
    perusteOppiaine,
    MurupolkuData,
    $stateParams,
    $rootScope,
    OppiaineService) {

    $scope.oppiaine = OppiaineService.getOppiaine();
    $scope.oppiaineenVlk = OppiaineService.getOpVlk();

    if (perusteOppiaine) {
      if (perusteOppiaine.eiPerustetta) {
        $scope.eiPerustetta = true;
      }
      if (perusteOppiaine.tunniste === $scope.oppiaine.tunniste) {
        $scope.perusteOppiaine = perusteOppiaine;
      }
      else {
        $scope.perusteOppiaine = _.find(perusteOppiaine.oppimaarat, function (om) {
          return om.tunniste === $scope.oppiaine.tunniste;
        });
      }
    }

  $scope.isVuosiluokkaistettava = _.any($scope.perusteOppiaine.vuosiluokkakokonaisuudet, function(vlk) {
    return vlk._vuosiluokkakokonaisuus === $scope.oppiaineenVlk._vuosiluokkakokonaisuus && !_.isEmpty(vlk.tavoitteet);
  });

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
  VuosiluokatService, Kaanna, OppiaineService, TextUtils, Utils, Kielitarjonta, OppiaineCRUD, OpsService, Notifikaatiot,
  VuosiluokkakokonaisuusMapper, Lukko, Kommentit, KommentitByOppiaine, opsModel) {

  Kommentit.haeKommentit(KommentitByOppiaine, {
    id: $stateParams.oppiaineId,
    opsId: $stateParams.id,
    oppiaineId: $stateParams.oppiaineId,
    vlkId: $stateParams.vlkId
  });

  $scope.lukkotiedot = null;
  $scope.vuosiluokat = [];
  $scope.alueOrder = Utils.sort;

  var commonParams = $scope.oppiaineenVlk ? {
    opsId: $stateParams.id,
    vlkId: $scope.oppiaineenVlk.id,
    oppiaineId: $stateParams.oppiaineId
    } : null;

  if (commonParams) {
    Lukko.isLocked($scope, commonParams);
  }

  function vanhempiOnUskontoTaiKieli(oppiaine) {
    return _.isString(oppiaine.koodiArvo) && _.includes(['AI', 'VK', 'TK', 'KT'], oppiaine.koodiArvo.toUpperCase());
  }

  OppiaineService.getParent()
    .then(function(res) {
      $scope.oppiaine.$parent = res;
      $scope.$onKieliTaiUskonto = vanhempiOnUskontoTaiKieli(res);
      if ($scope.$onKieliTaiUskonto) {
        $scope.oppiaine.tehtava = $scope.oppiaine.tehtava || {};
      }
    })
    .catch(_.noop);

  $scope.perusteOpVlk = $scope.perusteOppiaine ?
    _.find($scope.perusteOppiaine.vuosiluokkakokonaisuudet, function (vlk) {
      return vlk._vuosiluokkakokonaisuus === $scope.oppiaineenVlk._vuosiluokkakokonaisuus;
    }) : {};
  if ($scope.eiPerustetta) {
    VuosiluokkakokonaisuusMapper.createEmptyText($scope.perusteOpVlk, 'tyotavat');
    VuosiluokkakokonaisuusMapper.createEmptyText($scope.perusteOpVlk, 'ohjaus');
    VuosiluokkakokonaisuusMapper.createEmptyText($scope.perusteOpVlk, 'arviointi');
  }

  var perusteTavoitteet = _.indexBy($scope.perusteOpVlk ? $scope.perusteOpVlk.tavoitteet : null, 'tunniste');

  if ($scope.oppiaine.koosteinen && vanhempiOnUskontoTaiKieli($scope.oppiaine)) {
    $scope.valitseOppimaara = function() {
      var opsId = $stateParams.id;
      Kielitarjonta.rakenna(opsId, $scope.oppiaine, $scope.perusteOppiaine, function (res) {
        // var ops = OpsService.get(opsId);
        var ops = opsModel;
        console.log(ops);

        var tunnisteet = _.map(res.vuosiluokkakokonaisuudet, '_vuosiluokkakokonaisuus');
        // TODO järjestys vuosiluokkaenumin mukaan nimen sijasta?
        var lisatytVlkt = _(ops.vuosiluokkakokonaisuudet)
            .map('vuosiluokkakokonaisuus')
            .filter(function (vlk) {
              return _.includes(tunnisteet, vlk._tunniste);
            })
            .sortBy(function (vlk) {
              return Kaanna.kaanna(vlk.nimi);
            })
            .value();

        Notifikaatiot.onnistui(
          Kaanna.kaanna(res.nimi) +
          Kaanna.kaanna('oppiaine-lisattiin-vuosiluokkakokonaisuuksiin') +
          _.map(lisatytVlkt, function (vlk) {
            return Kaanna.kaanna(vlk.nimi);
          }).join(', '));

        var vlkId =
          _(lisatytVlkt).map('id').includes(parseInt($stateParams.vlkId))? $stateParams.vlkId : lisatytVlkt[0].id;

        $state.go('root.opetussuunnitelmat.yksi.opetus.oppiaine.oppiaine', {
          oppiaineId: res.id,
          vlkId: vlkId,
          oppiaineTyyppi: res.tyyppi
        }, { reload: true });
      });
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
      return OpsService.isEditable() && $scope.oppiaine.oma && (!$scope.lukkotiedot || !$scope.lukkotiedot.lukittu);
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
    $state.go('root.opetussuunnitelmat.yksi.opetus.oppiaine.vuosiluokka', {vlId: vuosiluokka.id});
  };

  $scope.startVuosiluokkaistaminen = function () {
    function start() {
      $state.go('root.opetussuunnitelmat.yksi.opetus.oppiaine.vuosiluokkaistaminen', {
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
    Lukko.lock(commonParams, function () {
      $state.go('root.opetussuunnitelmat.yksi.opetus.uusioppiaine', {
        vlkId: $stateParams.vlkId,
        oppiaineId: $scope.oppiaine.id
      });
    });
  };

  $scope.removeOppiaine = function () {
    Lukko.lock(commonParams, function () {
      Varmistusdialogi.dialogi({
        otsikko: 'varmista-poisto',
        primaryBtn: 'poista',
        successCb: function () {
          $scope.oppiaine.$delete({opsId: OpsService.getId()}, function () {
            Lukko.unlock(commonParams);
            Notifikaatiot.onnistui('poisto-onnistui');
            $state.go('root.opetussuunnitelmat.yksi.opetus.vuosiluokkakokonaisuus', {vlkId: $stateParams.vlkId}, {reload: true});
          }, function () {
            Lukko.unlock(commonParams);
            Notifikaatiot.serverCb();
          });
        },
        failureCb: function () {
          Lukko.unlock(commonParams);
        }
      })();
    });
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
          $state.go('root.opetussuunnitelmat.yksi.opetus.oppiaine.oppiaine', {
            vlkId: $stateParams.vlkId,
            oppiaineId: res.id,
            oppiaineTyyppi: res.tyyppi
          }, { reload: true });
        }, Notifikaatiot.serverCb);
      }
    })();
  };
});
