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
  .service('TekstikappaleEditMode', function () {
    this.mode = false;
    this.setMode = function (mode) {
      this.mode = mode;
    };
    this.getMode = function () {
      var ret = this.mode;
      this.mode = false;
      return ret;
    };
  })

  .controller('TekstikappaleController', function ($scope, Editointikontrollit,
    Notifikaatiot, $timeout, $stateParams, $state, OpetussuunnitelmanTekstit,
    OhjeCRUD, MurupolkuData, $rootScope, OpsService, TekstikappaleOps, Utils, Kommentit,
    KommentitByTekstikappaleViite, Lukko, TekstikappaleEditMode, Varmistusdialogi) {

    Kommentit.haeKommentit(KommentitByTekstikappaleViite, {id: $stateParams.tekstikappaleId, opsId: $stateParams.id});
    $scope.ohje = {};
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

    if ($stateParams.tekstikappaleId === 'uusi' || TekstikappaleEditMode.getMode()) {
      $timeout(function () {
        $scope.edit();
      }, 200);
    }

    $scope.canRemove = function () {
      return $scope.model && $scope.model.omistussuhde === 'oma';
    };

    $scope.isEmpty = function (model) {
      return _.isEmpty(model);
    };

    $scope.model = {};
    var originalOtsikko = null;

    function fetchOhje(model, cb) {
      OhjeCRUD.forTekstikappale({uuid: model.tekstiKappale.tunniste}, function (ohje) {
        _.each(TYYPIT, function (tyyppi) {
          var found = _.find(ohje, function (item) {
            return item.tyyppi === tyyppi;
          });
          if (found) {
            $scope[tyyppi] = found;
          }
        });
        (cb || angular.noop)();
      });
    }

    var commonParams = {
      opsId: $stateParams.id,
      viiteId: $stateParams.tekstikappaleId
    };

    function fetch(noLockCheck, cb) {
      if ($stateParams.tekstikappaleId === 'uusi') {
        $scope.model = {
          tekstiKappale: {
            nimi: {},
            teksti: {}
          }
        };
      } else {
        OpetussuunnitelmanTekstit.get(commonParams, function (res) {
          $scope.model = res;
          originalOtsikko = _.cloneDeep($scope.model.tekstiKappale.nimi);
          MurupolkuData.set('tekstiNimi', res.tekstiKappale.nimi);
          fetchOhje(res, cb);
        }, Notifikaatiot.serverCb);
        if (!noLockCheck) {
          Lukko.isLocked($scope, commonParams);
        }
      }
    }
    fetch();

    $scope.edit = function () {
      Lukko.lock(commonParams, function () {
        Editointikontrollit.startEditing();
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
        TekstikappaleOps.add($scope.model, null, $stateParams.id, {nimi: {fi: 'Uusi tekstikappale'}}, function (res) {
          Lukko.unlockRakenne(lukkoParams, function () {
            var newParams = _.extend(_.clone($stateParams), {tekstikappaleId: res.id});
            TekstikappaleEditMode.setMode(true);
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
          $scope.model.$delete({opsId: $stateParams.id}, function () {
            Notifikaatiot.onnistui('poisto-onnistui');
            OpsService.refetch(function () {
              $rootScope.$broadcast('rakenne:updated');
            });
            $timeout(function () {
              $state.go('root.opetussuunnitelmat.yksi.sisalto');
            });
          }, Notifikaatiot.serverCb);
        }, function () {
          Lukko.unlock(commonParams);
        });
      });
    };

    var successCb = function (res) {
      $scope.model = res;
      Notifikaatiot.onnistui('tallennettu-ok');
      if ($stateParams.tekstikappaleId === 'uusi') {
        $state.go($state.current.name, {tekstikappaleId: res.id}, {reload: true});
      } else {
        Lukko.unlock(commonParams, function () {
          $scope.lukkotiedot = null;
        });
      }
      if (savingValmis || !Utils.compareLocalizedText(originalOtsikko, res.tekstiKappale.nimi)) {
        savingValmis = false;
        OpsService.refetch(function () {
          $rootScope.$broadcast('rakenne:updated');
        });
      }
      originalOtsikko = _.cloneDeep($scope.model.tekstiKappale.nimi);
    };

    var callbacks = {
      edit: function () {
        fetch(true, function () {
          $timeout(function () {
            var el = angular.element('#ops-ckeditor');
            if (el && el.length > 0) {
              el[0].focus();
              el[0].scrollIntoView();
            }
          }, 300);
        });
      },
      asyncValidate: function (cb) {
        Lukko.lock(commonParams, cb);
      },
      save: function () {
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
          $timeout(function () {
            $state.go('root.opetussuunnitelmat.yksi.sisalto');
          });
        } else {
          Lukko.unlock(commonParams, function () {
            $scope.lukkotiedot = null;
            fetch();
          });
        }
      },
      notify: function (mode) {
        $scope.editMode = mode;
      }
    };
    Editointikontrollit.registerCallback(callbacks);

    $scope.setValmis = function (value) {
      $scope.model.valmis = value;
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
