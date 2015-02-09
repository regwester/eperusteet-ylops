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
.controller('PohjaTiedotController', function ($scope, $stateParams, $state,
  OpetussuunnitelmaCRUD, Notifikaatiot, Utils, OpsService, $rootScope,
  Editointikontrollit, $timeout, Kieli, Varmistusdialogi) {

    $scope.luonnissa = $stateParams.pohjaId === 'uusi';
    $scope.kieliOrderFn = Kieli.orderFn;

    if ($scope.luonnissa) {
      $scope.model.tyyppi = 'pohja';
      $scope.model.julkaisukielet = ['fi'];
    }

    $scope.editMode = false;
    $scope.kielivalinnat = ['fi', 'sv', 'se'];

    $scope.hasRequiredFields = function () {
      var model = $scope.model;
      return Utils.hasLocalizedText(model.nimi) &&
      _.any(_.values($scope.julkaisukielet));
    };

    function fetch(notify) {
      OpsService.refetchPohja(function (res) {
        $scope.model = res;
        if (notify) {
          $rootScope.$broadcast('rakenne:updated');
        }
      });
    }

    var successCb = function (res) {
      Notifikaatiot.onnistui('tallennettu-ok');
      if ($scope.luonnissa) {
        $state.go('root.pohjat.yksi', {pohjaId: res.id}, {reload: true});
      } else {
        fetch(true);
      }
    };

    function mapJulkaisukielet() {
      $scope.julkaisukielet = _.zipObject($scope.kielivalinnat, _.map($scope.kielivalinnat, function (kieli) {
        return _.indexOf($scope.model.julkaisukielet, kieli) > -1;
      }));
    }

    $scope.$watch('model.julkaisukielet', mapJulkaisukielet);

    var callbacks = {
      edit: function () {
        fetch();
      },
      validate: function () {
        return $scope.hasRequiredFields();
      },
      save: function () {
        $scope.model.julkaisukielet = _($scope.julkaisukielet).keys().filter(function (koodi) {
          return $scope.julkaisukielet[koodi];
        }).value();
        OpetussuunnitelmaCRUD.save({}, $scope.model, successCb, Notifikaatiot.serverCb);
      },
      cancel: function () {
        fetch();
      },
      notify: function (mode) {
        $scope.editMode = mode;
      }
    };
    Editointikontrollit.registerCallback(callbacks);

    $scope.uusi = {
      cancel: function () {
        $timeout(function () {
          $state.go('root.etusivu');
        });
      },
      create: function () {
        callbacks.save();
      }
    };

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
              $state.go('root.etusivu');
            });
          }, Notifikaatiot.serverCb);
        }
      })();
    };

  });
