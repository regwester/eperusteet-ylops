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

  .controller('TekstikappaleController', function ($scope, $q, Editointikontrollit,
    Notifikaatiot, $timeout, $stateParams, $state, OpetussuunnitelmanTekstit, Kieli,
    OhjeCRUD, MurupolkuData, $rootScope, OpsService, TekstikappaleOps, Utils, Kommentit,
    KommentitByTekstikappaleViite, Lukko, Varmistusdialogi, teksti,
    reresolver, $location) {

    $scope.lukkotiedot = null;
    $scope.perusteteksti = {};
    $scope.valmisOptions = [{valmis: false}, {valmis: true}];
    const TYYPIT = ['ohje', 'perusteteksti'];

    $scope.opsId = $stateParams.id;
    $scope.options = {
      isCollapsed: true
    };
    $scope.editMode = false;

    Kommentit.haeKommentit(KommentitByTekstikappaleViite, {
      id: $stateParams.tekstikappaleId.split('/')[0],
      tekstiKappaleViiteId: $stateParams.tekstikappaleId.split('/')[0],
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
        _.each(TYYPIT, (tyyppi) => {
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
      if ($stateParams.id !== 'uusi' && value.tekstikappale) {
        originalOtsikko = _.cloneDeep(value.tekstiKappale.nimi);
        originalTekstiKappale = _.cloneDeep(value.tekstiKappale);
        MurupolkuData.set('tekstiNimi', value.tekstiKappale.nimi);
      }
    }
    setup(teksti);
    fetchOhje(teksti);

    $scope.vaihdaVersio = () => {
      let versionUrl = $state.href($state.current.name).replace(/#/g, '').split('%')[0];
      if(_.last($scope.versiot.list).numero !== $scope.versiot.chosen.numero){
        versionUrl += '/'+$scope.versiot.chosen.numero;
      }
      $location.url(versionUrl);
    };

    $scope.goToLatest = () => {
      const versionUrl = $state.href($state.current.name).replace(/#/g, '').split('%')[0];
      $location.url(versionUrl);
    };

    $scope.revertToCurrentVersion = () => {
      const params = {
        opsId: parseInt($stateParams.id),
        viiteId: $scope.model.id,
        versio: $scope.versiot.chosen.numero
      };

      OpetussuunnitelmanTekstit.revertTo(params, {}, () => {
        $scope.goToLatest();
      });
    };

    const updateMuokkaustieto = () => {
      OpetussuunnitelmanTekstit.versiot({id: $stateParams.id, tekstiId: $scope.model.tekstiKappale.id}, {}, (res) => {
        $scope.versiot = {list: []};
        _.forEach(res, (value, key) => {
          value.index = res.length-key;
          $scope.versiot.list.push(value);
        });
        $scope.versiot.latest = _.isEmpty($stateParams.versio);
        if($scope.versiot.latest) {
          $scope.versiot.chosen = _.first($scope.versiot.list);
        } else {
          $scope.versiot.chosen = _.find($scope.versiot.list, {'numero': parseInt($stateParams.versio.replace('/', ''))});
        }
      });

      if ($scope.model.tekstiKappale) {
        $scope.$$muokkaustiedot = {
          luotu: $scope.model.tekstiKappale.luotu,
          muokattu: $scope.model.tekstiKappale.muokattu,
          muokkaajaOid: $scope.model.tekstiKappale.muokkaaja
        };
      }
    };

    updateMuokkaustieto();

    var commonParams = {
      opsId: $stateParams.id,
      viiteId: $stateParams.tekstikappaleId
    };

    $scope.edit = function() {
      Lukko.lock(commonParams, function () {
        reresolver('teksti').then(function(res) {
          setup(res);
          Editointikontrollit.startEditing().then(() => {
            $scope.editMode = true;
          });
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
      return $q((resolve, reject) => {
        $scope.model = res;
        Notifikaatiot.onnistui('tallennettu-ok');

        if ($stateParams.tekstikappaleId === 'uusi') {
          $state.reload();
        }
        else {
          var navigaatiomuutos = !_.isEqual(originalOtsikko, _.omit(res.tekstiKappale.nimi, '$$validointi')) ||
          res.tekstiKappale.valmis !== originalTekstiKappale.valmis;
          Lukko.unlock(commonParams, function () {
            $scope.lukkotiedot = null;
            if (navigaatiomuutos) {
              $state.transitionTo($state.current, $stateParams, { reload: true, inherit: true });
            }
          });
        }

        originalOtsikko = _.cloneDeep($scope.model.tekstiKappale.nimi);
        resolve();
      });
    };

    var callbacks = {
      edit: () => $q((resolve) => {
        Lukko.lock(commonParams, resolve);
      }),
      save: () => $q((resolve, reject) => {
        const params = {opsId: $stateParams.id};
        (() => {
          return $q((resolve, reject) => {
            if ($stateParams.tekstikappaleId === 'uusi') {
              OpetussuunnitelmanTekstit.save(params, $scope.model, resolve, reject);
            } else {
              // Pelkkää tekstikappaletta muokattaessa lapset-kenttä tulee jättää pois
              _.omit($scope.model, 'lapset').$save(params, resolve, reject);
            }
          });
        })()
        .then((res) => successCb(res)
            .then(() => {
              $scope.editMode = false;
              resolve();
            })
            .catch(Notifikaatiot.serverCb))
        .catch(Notifikaatiot.serverCb);
      }),
      cancel: () => $q((resolve) => {
        resolve();
        if ($stateParams.tekstikappaleId === 'uusi') {
          $state.go('root.opetussuunnitelmat.yksi.sisalto');
        }
        else {
          Lukko.unlock(commonParams, function () {
            $scope.lukkotiedot = null;
            $state.reload();
          });
        }
      }),
      notify: _.noop
    };

    Editointikontrollit.registerCallback(callbacks);

    $scope.setValmis = function (value) {
      $scope.model.tekstiKappale.valmis = value;
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
