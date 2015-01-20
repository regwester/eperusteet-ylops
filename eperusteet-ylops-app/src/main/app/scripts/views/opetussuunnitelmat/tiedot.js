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
.controller('OpetussuunnitelmaTiedotController', function ($scope, Editointikontrollit, $stateParams, $state,
  OpetussuunnitelmaCRUD, Notifikaatiot, $timeout, OpsService, Utils, KoodistoHaku, PeruskouluHaku) {

  $scope.editableModel = $scope.model;
  $scope.editMode = false;
  $scope.kielivalinnat = ['fi', 'sv', 'se'];
  $scope.loading = false;

  function fetch() {
    OpsService.refetch(function (res) {
      $scope.editableModel = res;
      $scope.editableModel.kuntaUrit = _.map(res.kunnat, 'koodiUri');
      $scope.editableModel.kouluOidit = _.map(res.koulut, 'oid');
      $scope.loading = false;
    });
  }

  var successCb = function (res) {
    $scope.model = res;
    Notifikaatiot.onnistui('tallennettu-ok');
    if ($stateParams.id === 'uusi') {
      $state.go('root.opetussuunnitelmat.yksi.sisalto', {id: res.id}, {reload: true});
    }
  };

  function mapKouluKunta() {
    $scope.editableModel.koulut = _.map($scope.editableModel.kouluOidit, function (oid) {
      return _.find($scope.koululista, function (koulu) {
        return koulu.oid === oid;
      });
    });
    $scope.editableModel.kunnat = _.map($scope.editableModel.kuntaUrit, function (uri) {
      return _.find($scope.kuntalista, function (kunta) {
        return kunta.koodiUri === uri;
      });
    });
    delete $scope.editableModel.kouluOidit;
    delete $scope.editableModel.kuntaUrit;
  }

  var callbacks = {
    edit: function () {
      $scope.loading = true;
      fetch();
    },
    save: function () {
      mapKouluKunta();
      $scope.editableModel.julkaisukielet = _($scope.julkaisukielet).keys().filter(function (koodi) {
        return $scope.julkaisukielet[koodi];
      }).value();
      delete $scope.editableModel.tekstit.lapset;
      if ($stateParams.id === 'uusi') {
        OpetussuunnitelmaCRUD.save({}, $scope.editableModel, successCb, Notifikaatiot.serverCb);
      } else {
        $scope.editableModel.$save({}, successCb, Notifikaatiot.serverCb);
      }
    },
    cancel: function () {
      if ($stateParams.id === 'uusi') {
        $timeout(function () {
          $state.go('root.etusivu');
        });
      } else {
        fetch();
      }
    },
    notify: function (mode) {
      $scope.editMode = mode;
      if (mode) {
        $scope.haeKunnat();
        $scope.haeKoulut();
      }
    }
  };
  Editointikontrollit.registerCallback(callbacks);

  $scope.edit = function () {
    Editointikontrollit.startEditing();
  };

  function mapKunnat(lista) {
    return _(lista).map(function (kunta) {
      return {
        koodiUri: kunta.koodiUri,
        koodiArvo: kunta.koodiArvo,
        nimi: _(kunta.metadata).indexBy(function (item) {
          return item.kieli.toLowerCase();
        }).mapValues('nimi').value()
      };
    }).sortBy(Utils.sort).value();
  }

  $scope.haeKunnat = function () {
    if (!$scope.editMode) {
      return;
    }
    KoodistoHaku.get({ koodistoUri: 'kunta' }, function(kunnat) {
      $scope.kuntalista = mapKunnat(kunnat);
    }, Notifikaatiot.serverCb);
  };

  $scope.$watch('editableModel.kuntaUrit', function () {
    $scope.haeKoulut();
  });

  $scope.$watch('editableModel.kouluOidit', function (value) {
    // ui-select tries to set selected values too eagerly
    if (value && value.length > 0 && value[0] === undefined) {
      $scope.editableModel.kouluOidit = _.map($scope.editableModel.koulut, 'oid');
    }
  });

  function mapJulkaisukielet() {
    $scope.julkaisukielet = _.zipObject($scope.kielivalinnat, _.map($scope.kielivalinnat, function (kieli) {
      return _.indexOf($scope.editableModel.julkaisukielet, kieli) > -1;
    }));
  }

  $scope.$watch('editableModel.julkaisukielet', mapJulkaisukielet);

  $scope.haeKoulut = function () {
    var kunnat = $scope.editableModel.kuntaUrit;
    if (!$scope.editMode || !kunnat) {
      return;
    }
    if (kunnat.length === 0) {
      $scope.editableModel.kouluOidit = [];
    } else if (kunnat.length === 1) {
      var kunta = kunnat[0];
      PeruskouluHaku.get({ kuntaUri: kunta }, function(res) {
        $scope.koululista = _.sortBy(res.organisaatiot, Utils.sort);
      }, Notifikaatiot.serverCb);
    } else {
      $scope.editableModel.kouluOidit = [];
      $scope.koululista = [];
    }
  };

});
