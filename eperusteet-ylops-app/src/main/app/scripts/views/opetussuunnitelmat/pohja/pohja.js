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

.controller('PohjaListaController', function ($scope) {
  // TODO
  $scope.items = {$resolved: true};
})

.controller('PohjaController', function ($scope, $state, pohjaModel) {
  if ($state.current.name === 'root.pohjat.yksi') {
    $state.go('root.pohjat.yksi.sisalto');
  }
  $scope.model = pohjaModel;

  // TODO remove dummy data
  $scope.model.$resolved = true;
  $scope.model.tila = 'luonnos';
  $scope.model.nimi = {fi: 'Opetussuunnitelmapohja'};
  $scope.model.jarjestaminen = {tekstiKappale: {nimi: {fi: 'Opetuksen järjestäminen'}}, lapset: []};
  $scope.model.lahtokohdat = {tekstiKappale: {nimi: {fi: 'Opetuksen toteuttamisen lähtökohdat'}}, lapset: []};
})

.controller('PohjaTiedotController', function ($scope, $stateParams, $state) {
  $scope.isLuonti = $stateParams.pohjaId === 'uusi';

  $scope.cancel = function () {
    $state.go('root.etusivu');
  };

  $scope.save = function () {
    // TODO
    $state.go('root.pohjat.yksi', {pohjaId: 'dummy'}, {reload: true});
  };
})

.controller('PohjaSisaltoController', function ($scope, Algoritmit, Utils) {
  $scope.uusi = {nimi: {}};
  $scope.rakenneEdit = {jarjestaminen: false, lahtokohdat: false};
  $scope.kappaleEdit = null;

  $scope.hasText = function () {
    return Utils.hasLocalizedText($scope.uusi.nimi);
  };

  $scope.pohjaOps = {
    addNew: function (osio) {
      var newNode = {tekstiKappale: angular.copy($scope.uusi), lapset: []};
      $scope.model[osio].lapset.push(newNode);
      // TODO save to backend
      $scope.uusi = {nimi: {}};
    },
    edit: function (kappale) {
      $scope.kappaleEdit = kappale;
      kappale.$original = _.cloneDeep(kappale.tekstiKappale);
    },
    delete: function (osio, kappale) {
      var foundIndex = null, foundList = null;
      Algoritmit.traverse($scope.model[osio], 'lapset', function (lapsi, depth, index, arr) {
        if (lapsi === kappale) {
          foundIndex = index;
          foundList = arr;
          return true;
        }
      });
      if (foundList) {
        foundList.splice(foundIndex, 1);
      }
    },
    cancel: function (kappale) {
      $scope.kappaleEdit = null;
      kappale.tekstiKappale = _.cloneDeep(kappale.$original);
      delete kappale.$original;
    },
    save: function (kappale) {
      $scope.kappaleEdit = null;
      delete kappale.$original;
    }
  };

  $scope.rakenne = {
    edit: function (osio) {
      $scope.rakenneEdit[osio] = true;
      $scope.model[osio].$original = _.cloneDeep($scope.model[osio].lapset);
    },
    save: function (osio) {
      $scope.rakenneEdit[osio] = false;
      delete $scope.model[osio].$original;
    },
    cancel: function (osio) {
      $scope.rakenneEdit[osio] = false;
      $scope.model[osio].lapset = _.cloneDeep($scope.model[osio].$original);
      delete $scope.model[osio].$original;
    }
  };
});
