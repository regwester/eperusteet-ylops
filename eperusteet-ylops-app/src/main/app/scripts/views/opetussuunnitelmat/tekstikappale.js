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
  .controller('TekstikappaleController', function ($scope, Editointikontrollit,
    Notifikaatiot, $timeout, $stateParams, $state, OpetussuunnitelmanTekstit,
    OhjeCRUD, MurupolkuData, $rootScope, OpsService, TekstikappaleOps, Utils) {

    $scope.ohje = {};
    $scope.perusteteksti = {};
    $scope.options = {tekstiCollapsed: true};
    var TYYPIT = ['ohje', 'perusteteksti'];

    $scope.opsId = $stateParams.id;
    $scope.options = {
      isCollapsed: true
    };
    $scope.editMode = false;
    if ($stateParams.tekstikappaleId === 'uusi') {
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

    function fetchOhje(model) {
      OhjeCRUD.forTekstikappale({uuid: model.tekstiKappale.tunniste}, function (ohje) {
        _.each(TYYPIT, function (tyyppi) {
          var found = _.find(ohje, function (item) {
            return item.tyyppi === tyyppi;
          });
          if (found) {
            $scope[tyyppi] = found;
          }
        });
      });
    }

    function fetch() {
      if ($stateParams.tekstikappaleId === 'uusi') {
        $scope.model = {
          tekstiKappale: {
            nimi: {},
            teksti: {}
          }
        };
      } else {
        OpetussuunnitelmanTekstit.get({
          opsId: $stateParams.id,
          viiteId: $stateParams.tekstikappaleId
        }, function (res) {
          $scope.model = res;
          originalOtsikko = _.cloneDeep($scope.model.tekstiKappale.nimi);
          MurupolkuData.set('tekstiNimi', res.tekstiKappale.nimi);
          fetchOhje(res);
        }, Notifikaatiot.serverCb);

      }
    }
    fetch();

    $scope.edit = function () {
      Editointikontrollit.startEditing();
    };

    $scope.remove = function () {
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
      });
    };

    var successCb = function (res) {
      $scope.model = res;
      Notifikaatiot.onnistui('tallennettu-ok');
      if ($stateParams.tekstikappaleId === 'uusi') {
        $state.go($state.current.name, {tekstikappaleId: res.id}, {reload: true});
      }
      if (!Utils.compareLocalizedText(originalOtsikko, res.tekstiKappale.nimi)) {
        OpsService.refetch(function () {
          $rootScope.$broadcast('rakenne:updated');
        });
      }
      originalOtsikko = _.cloneDeep($scope.model.tekstiKappale.nimi);
    };

    var callbacks = {
      edit: function () {
        fetch();
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
          fetch();
        }
      },
      notify: function (mode) {
        $scope.editMode = mode;
      }
    };
    Editointikontrollit.registerCallback(callbacks);

  });
