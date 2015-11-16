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
  .service('reresolver', function($state, $injector) {
    return function(field) {
      return $injector.invoke($state.current.resolve[field]);
    };
  })

  .controller('TekstikappaleController', function ($scope, Editointikontrollit,
    Notifikaatiot, $timeout, $stateParams, $state, OpetussuunnitelmanTekstit, Kieli,
    OhjeCRUD, MurupolkuData, $rootScope, OpsService, TekstikappaleOps, Utils, Kommentit,
    KommentitByTekstikappaleViite, Lukko, Varmistusdialogi, teksti,
    reresolver) {

    $scope.lukkotiedot = null;
    $scope.perusteteksti = {};
    $scope.options = {tekstiCollapsed: true};
    $scope.valmisOptions = [{valmis: false}, {valmis: true}];
    var savingValmis = false;
    var TYYPIT = ['ohje', 'perusteteksti'];

    $scope.opsId = $stateParams.id;
    $scope.options = {
      isCollapsed: true
    };
    $scope.editMode = false;

    Kommentit.haeKommentit(KommentitByTekstikappaleViite, {
      id: $stateParams.tekstikappaleId,
      tekstiKappaleViiteId: $stateParams.tekstikappaleId,
      opsId: $stateParams.id
    });

    $scope.canRemove = function () {
      return $scope.model && $scope.model.omistussuhde === 'oma';
    };

    $scope.isEmpty = function (model) {
      return _.isEmpty(model);
    };

    var originalOtsikko = null;
    var originalTekstiKappale = null;

    function fetchOhje(model, cb = _.noop) {
      OhjeCRUD.forTekstikappale({
        uuid: model.tekstiKappale.tunniste
      }, function(ohje) {
        _.each(TYYPIT, function (tyyppi) {
          var found = _.find(ohje, function (item) {
            return item.tyyppi === tyyppi;
          });
          if (found) {
            $scope[tyyppi] = found;
          }
        });
        cb();
      });
    }

    function setup(value) {
      $scope.model = value;
      if ($stateParams.id !== 'uusi') {
        originalOtsikko = _.cloneDeep(value.tekstiKappale.nimi);
        originalTekstiKappale = _.cloneDeep(value.tekstiKappale);
        MurupolkuData.set('tekstiNimi', value.tekstiKappale.nimi);
      }
    }
    setup(teksti);
    fetchOhje(teksti);

    function updateMuokkaustieto() {
      if ($scope.model.tekstiKappale) {
        $scope.$$muokkaustiedot = {
          luotu: $scope.model.tekstiKappale.luotu,
          muokattu: $scope.model.tekstiKappale.muokattu,
          muokkaajaOid: $scope.model.tekstiKappale.muokkaaja
        };
      }
    }
    updateMuokkaustieto();

    var commonParams = {
      opsId: $stateParams.id,
      viiteId: $stateParams.tekstikappaleId
    };

    $scope.edit = function() {
      Lukko.lock(commonParams, function () {
        reresolver('teksti').then(function(res) {
          setup(res);
          $scope.editMode = true;
          Editointikontrollit.startEditing();
        });
      });
    };

    $scope.kopioiMuokattavaksi = function () {
      Varmistusdialogi.dialogi({
        otsikko: 'varmista-kopiointi',
        primaryBtn: 'luo-kopio',
        successCb: function () {
          Lukko.lock(commonParams, function () {
            OpetussuunnitelmanTekstit.kloonaaTekstikappale(commonParams, {}, function (res) {
              successCb(res);
              $scope.edit();
            }, Notifikaatiot.serverCb);
          });
        }
      })();
    };

    $scope.addChild = function () {
      var lukkoParams = _.omit(commonParams, 'viiteId');
      Lukko.lockRakenne(lukkoParams, function () {
        TekstikappaleOps.lisaa($scope.model, $stateParams.id, {fi: 'Uusi tekstikappale'}, function (res) {
          Lukko.unlockRakenne(lukkoParams, function () {
            var newParams = _.extend(_.clone($stateParams), {tekstikappaleId: res.id});
            $timeout(function () {
              $state.go('^.tekstikappale', newParams, {reload: true});
            });
          });
        });
      });
    };

    $scope.remove = function () {
      Lukko.lock(commonParams, function () {
        TekstikappaleOps.varmistusdialogi($scope.model.tekstiKappale.nimi, function () {
          $scope.model.$delete({
            opsId: $stateParams.id
          }, function () {
            Notifikaatiot.onnistui('poisto-onnistui');
            $state.go('root.opetussuunnitelmat.yksi.sisalto', { reload: true });
          }, Notifikaatiot.serverCb);
        }, function () {
          Lukko.unlock(commonParams);
        });
      });
    };

    var successCb = function(res) {
      $scope.model = res;
      Notifikaatiot.onnistui('tallennettu-ok');
      if ($stateParams.tekstikappaleId === 'uusi') {
        $state.reload();
      } else {
        var navigaatiomuutos = !_.isEqual(originalOtsikko, _.omit(res.tekstiKappale.nimi, '$$validointi')) ||
          res.tekstiKappale.valmis !== originalTekstiKappale.valmis;
          Lukko.unlock(commonParams, function () {
          $scope.lukkotiedot = null;
          if (navigaatiomuutos) {
            $state.transitionTo($state.current, $stateParams, { reload: true, inherit: true });
          }
        });
      }
      if (savingValmis || !Utils.compareLocalizedText(originalOtsikko, res.tekstiKappale.nimi)) {
        savingValmis = false;
      }
      originalOtsikko = _.cloneDeep($scope.model.tekstiKappale.nimi);
    };

    var callbacks = {
      edit: function () {
      },
      asyncValidate: function (cb) {
        Lukko.lock(commonParams, cb);
      },
      save: function() {
        var params = {opsId: $stateParams.id};
        if ($stateParams.tekstikappaleId === 'uusi') {
          OpetussuunnitelmanTekstit.save(params, $scope.model, successCb, Notifikaatiot.serverCb);
        } else {
          // Pelkkää tekstikappaletta muokattaessa lapset-kenttä tulee jättää pois
          _.omit($scope.model, 'lapset').$save(params, successCb, Notifikaatiot.serverCb);
        }
      },
      cancel: function () {
        if ($stateParams.tekstikappaleId === 'uusi') {
          $state.go('root.opetussuunnitelmat.yksi.sisalto');
        }
        else {
          Lukko.unlock(commonParams, function () {
            $scope.lukkotiedot = null;
            $state.reload();
          });
        }
      },
      notify: _.noop
    };

    Editointikontrollit.registerCallback(callbacks);

    $scope.setValmis = function (value) {
      $scope.model.tekstiKappale.valmis = value;
      savingValmis = true;
      if (!$scope.editMode) {
        Lukko.lock(commonParams, function () {
          callbacks.save();
        });
      }
    };
  })

  .directive('valmiusIkoni', function () {
    return {
      restrict: 'A',
      scope: {
        model: '=valmiusIkoni'
      },
      template: '<span ng-attr-title="{{(model.valmis ? \'valmis\' : \'luonnos\') | kaanna}}" ' +
        'class="valmius glyphicon" ng-class="model.valmis ? \'glyphicon-check\' : \'glyphicon-edit\'"></span>'
    };
  });