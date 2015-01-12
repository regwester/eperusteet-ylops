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
  .controller('OpetussuunnitelmaController', function ($scope, Editointikontrollit, $stateParams,
    $timeout, $state, OpetussuunnitelmaCRUD, opsModel, opsService, Notifikaatiot, Varmistusdialogi,
    OpetussuunnitelmanTekstit) {

    $scope.editMode = false;
    $scope.rakenneEdit = false;
    if ($stateParams.id === 'uusi') {
      $timeout(function () {
        $scope.edit();
      }, 200);
    }

    $scope.model = opsModel;

    function fetch() {
      opsService.refetch();
    }

    $scope.edit = function () {
      Editointikontrollit.startEditing();
    };

    $scope.delete = function () {
      Varmistusdialogi.dialogi({
        otsikko: 'varmista-poisto',
        primaryBtn: 'poista',
        successCb: function () {
          $scope.model.$delete({}, function () {
            Notifikaatiot.onnistui('poisto-onnistui');
            $timeout(function () {
              $state.go('root.opetussuunnitelmat.lista');
            });
          }, Notifikaatiot.serverCb);
        }
      })();
    };

    $scope.addTekstikappale = function () {
      $state.go('root.opetussuunnitelmat.yksi.tekstikappale', {tekstikappaleId: 'uusi'});
    };

    function mapSisalto(root) {
      return {
        id: root.id,
        lapset: _.map(root.lapset, mapSisalto)
      };
    }

    $scope.saveRakenne = function () {
      var postdata = mapSisalto($scope.model.tekstit);
      OpetussuunnitelmanTekstit.save({
        opsId: $scope.model.id,
        viiteId: $scope.model.tekstit.id
      }, postdata, function () {
        Notifikaatiot.onnistui('tallennettu-ok');
        $scope.rakenneEdit = false;
        fetch();
      }, Notifikaatiot.serverCb);
    };

    $scope.editRakenne = function () {
      $scope.rakenneEdit = true;
    };

    $scope.cancelRakenne = function () {
      $scope.rakenneEdit = false;
      fetch();
    };

    var successCb = function (res) {
      $scope.model = res;
      Notifikaatiot.onnistui('tallennettu-ok');
      if ($stateParams.id === 'uusi') {
        $state.go($state.current.name, {id: res.id}, {reload: true});
      }
    };

    var callbacks = {
      edit: function () {
        fetch();
      },
      save: function () {
        if ($stateParams.id === 'uusi') {
          OpetussuunnitelmaCRUD.save({}, $scope.model, successCb, Notifikaatiot.serverCb);
        } else {
          $scope.model.$save({}, successCb, Notifikaatiot.serverCb);
        }
      },
      cancel: function () {
        if ($stateParams.id === 'uusi') {
          $timeout(function () {
            $state.go('root.opetussuunnitelmat.lista');
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
