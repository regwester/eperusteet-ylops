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
    Varmistusdialogi, Notifikaatiot, $timeout, $stateParams, $state, OpetussuunnitelmanTekstit) {

    $scope.editMode = false;
    if ($stateParams.tekstikappaleId === 'uusi') {
      $timeout(function () {
        $scope.edit();
      }, 200);
    }

    $scope.model = {};

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
        }, Notifikaatiot.serverCb);
      }
    }
    fetch();

    $scope.edit = function () {
      Editointikontrollit.startEditing();
    };

    $scope.delete = function () {
      Varmistusdialogi.dialogi({
        otsikko: 'varmista-poisto',
        primaryBtn: 'poista',
        successCb: function () {
          $scope.model.$delete({opsId: $stateParams.id}, function () {
            Notifikaatiot.onnistui('poisto-onnistui');
            $timeout(function () {
              $state.go('root.opetussuunnitelmat.yksi.opetussuunnitelma');
            });
          }, Notifikaatiot.serverCb);
        }
      })();
    };

    var successCb = function (res) {
      $scope.model = res;
      Notifikaatiot.onnistui('tallennettu-ok');
      if ($stateParams.tekstikappaleId === 'uusi') {
        $state.go($state.current.name, {tekstikappaleId: res.id}, {reload: true});
      }
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
          $scope.model.$save(params, successCb, Notifikaatiot.serverCb);
        }
      },
      cancel: function () {
        if ($stateParams.tekstikappaleId === 'uusi') {
          $timeout(function () {
            $state.go('root.opetussuunnitelmat.yksi.opetussuunnitelma');
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
