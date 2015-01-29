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
.controller('PohjaTekstikappaleController', function ($scope, tekstikappaleModel, Editointikontrollit,
  Notifikaatiot, Varmistusdialogi, $timeout, $state, $stateParams, OhjeCRUD, OpetussuunnitelmanTekstit) {

  $scope.pohjaId = $stateParams.pohjaId;
  $scope.model = tekstikappaleModel;
  $scope.ohje = {};

  $scope.isEmpty = function (model) {
    return _.isEmpty(model);
  };

  $scope.edit = function () {
    Editointikontrollit.startEditing();
  };

  $scope.delete = function () {
    Varmistusdialogi.dialogi({
      otsikko: 'varmista-poisto',
      primaryBtn: 'poista',
      successCb: function () {
        $scope.model.$delete({opsId: $stateParams.pohjaId}, function () {
          Notifikaatiot.onnistui('poisto-onnistui');
          $timeout(function () {
            $state.go('root.pohjat.yksi.sisalto');
          });
        }, Notifikaatiot.serverCb);
      }
    })();
  };

  function fetchOhje(model) {
    OhjeCRUD.forTekstikappale({uuid: model.tekstiKappale.tunniste}, function (ohje) {
      $scope.ohje = ohje;
    });
  }

  function fetch(initial) {
    if (initial) {
      fetchOhje($scope.model);
      return;
    }
    OpetussuunnitelmanTekstit.get({
      opsId: $stateParams.pohjaId,
      viiteId: $stateParams.tekstikappaleId
    }, function (res) {
      $scope.model = res;
      fetchOhje(res);
    }, Notifikaatiot.serverCb);
  }
  fetch(true);

  var successCb = function (res) {
    $scope.model = res;
    Notifikaatiot.onnistui('tallennettu-ok');
    if ($stateParams.tekstikappaleId === 'uusi') {
      $state.go($state.current.name, {tekstikappaleId: res.id}, {reload: true});
    }
  };

  function saveOhje() {
    if (!$scope.ohje.$save) {
      $scope.ohje.kohde = $scope.model.tekstiKappale.tunniste;
      OhjeCRUD.save({}, $scope.ohje, function (res) {
        $scope.ohje = res;
      }, Notifikaatiot.serverCb);
    } else {
      $scope.ohje.$save();
    }
  }

  var callbacks = {
    edit: function () {
      fetch();
    },
    save: function () {
      var params = {opsId: $stateParams.pohjaId};
      // Pelkkää tekstikappaletta muokattaessa lapset-kenttä tulee jättää pois
      _.omit($scope.model, 'lapset').$save(params, successCb, Notifikaatiot.serverCb);
      saveOhje();
    },
    cancel: function () {
      fetch();
    },
    notify: function (mode) {
      $scope.editMode = mode;
    }
  };
  Editointikontrollit.registerCallback(callbacks);

  $scope.ohjeOps = {
    delete: function () {
      $scope.ohje.$delete(function () {
        $scope.ohje = {};
      });
    }
  };

});