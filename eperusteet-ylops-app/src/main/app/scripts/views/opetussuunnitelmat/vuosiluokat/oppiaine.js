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
.controller('OppiaineBaseController', function ($scope, oppiaine, MurupolkuData) {
  $scope.oppiaine = oppiaine;
  MurupolkuData.set('oppiaineNimi', $scope.oppiaine.nimi);
})

.controller('OppiaineController', function ($scope, $state, $stateParams, Editointikontrollit) {
  $scope.vuosiluokkakokonaisuus = $scope.oppiaine.vuosiluokkakokonaisuudet[0];

  $scope.tekstit = {
    ohjaus: {
      teksti: {fi: 'Oppilaan matematiikan osaamista ja taitojen kehittymistä seurataan ja tarvittaessa annetaan lisätukea heti tuen tarpeen ilmetessä. Tarjottava tuki antaa oppilaalle mahdollisuuden ymmärtää matematiikkaa ikätasonsa mukaisesti ja kehittää taitojaan niin, että oppimisen ja osaamisen ilo säilyvät. Oppilaille tarjotaan sopivia välineitä oppimisen tueksi ja hänelle tarjotaan mahdollisuuksia oivaltaa ja ymmärtää itse.'}
    },
    tyotavat: {

    },
    arviointi: {

    }
  };

  $scope.callbacks = {
    edit: function () {

    },
    save: function () {

    },
    cancel: function () {

    },
    notify: function (mode) {
      $scope.callbacks.notifier(mode);
    },
    notifier: angular.noop
  };
  Editointikontrollit.registerCallback($scope.callbacks);


  $scope.goToDummy = function (id) {
    $state.go('root.opetussuunnitelmat.yksi.oppiaine.vuosiluokka', {
      vlkId: $stateParams.vlkId,
      vlId: id
    });
  };

  $scope.vuosiluokkaistaminenDemo = function () {
    $state.go('root.opetussuunnitelmat.yksi.oppiaine.vuosiluokkaistaminen', {
      vlkId: $stateParams.vlkId,
    });
  };
});
