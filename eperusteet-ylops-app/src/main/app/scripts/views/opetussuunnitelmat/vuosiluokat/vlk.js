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
.controller('VuosiluokkakokonaisuusController', function ($scope, Editointikontrollit,
  MurupolkuData) {
  $scope.perusteVlk = {
    nimi: {fi: 'Vuosiluokat 1-2'},
    tehtava: {otsikko: {fi: 'Vuosiluokkakokonaisuuden tehtävä'}, teksti: {fi: 'Tehtävän teksti.'}},
    siirtymaEdellisesta: {otsikko: {fi: 'Siirtymä esiopetuksesta'}, teksti: {fi: 'Siirtymä edellisestä -teksti.'}},
    siirtymaSeuraavaan: {otsikko: {fi: 'Siirtymä vuosiluokille 3-6'}, teksti: {fi: 'Siirtymä seuraavaan -teksti.'}},
    laajaalainenOsaaminen: {otsikko: {fi: 'Laaja-alainen osaaminen vuosiluokilla 1-2'}, teksti: {fi: 'Laaja-alainen osaaminen -teksti.'}},
    laajaalaisetOsaamiset: [
      {tunniste: '1234', nimi: {fi: 'L1 Joku osaaminen'}, kuvaus: {fi: 'L1 kuvaus.'}},
      {tunniste: '5678', nimi: {fi: 'L2 Joku toinen osaaminen'}, kuvaus: {fi: 'L2 kuvaus.'}},
      {tunniste: 'abcd', nimi: {fi: 'L3 Joku kolmas osaaminen'}, kuvaus: {fi: 'L2 kuvaus.'}},
    ]
  };

  MurupolkuData.set('vlkNimi', $scope.perusteVlk.nimi);

  $scope.vlk = {
    siirtymaEdellisesta: {},
    siirtymaSeuraavaan: {},
  };
  $scope.laajaalaiset = _.map($scope.perusteVlk.laajaalaisetOsaamiset, function (item) {
    item.teksti = item.kuvaus;
    item.otsikko = item.nimi;
    item.paikallinen = {};
    return item;
  });

  $scope.siirtymat = ['siirtymaEdellisesta', 'siirtymaSeuraavaan'];

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

});
