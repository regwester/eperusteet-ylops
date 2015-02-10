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
.controller('VuosiluokkaBaseController', function ($scope, vuosiluokka, MurupolkuData, $state) {
  $scope.vuosiluokka = vuosiluokka;
  MurupolkuData.set('vuosiluokkaNimi', vuosiluokka.nimi);

  $scope.isState = function (name) {
    return _.endsWith($state.current.name, 'vuosiluokka.' + name);
  };

  if ($state.is('root.opetussuunnitelmat.yksi.oppiaine.vuosiluokka')) {
    $state.go('root.opetussuunnitelmat.yksi.oppiaine.vuosiluokka.tavoitteet');
  }
})

.controller('VuosiluokkaTavoitteetController', function ($scope, VuosiluokatService) {
  $scope.tavoitteet = [];
  $scope.collapsed = {};

  // TODO oikea data
  var kohdealueet = [
    {fi: 'Työskentelyn taidot'},
    {fi: 'Toinen kohdealue'},
  ];

  function processTavoitteet() {
    _.each($scope.tavoitteet, function (item) {
      item.kohdealue = _.sample(kohdealueet);
    });
  }

  VuosiluokatService.getTavoitteet().then(function (res) {
    $scope.tavoitteet = res;
    processTavoitteet();
  });

})

.controller('VuosiluokkaSisaltoalueetController', function () {

});
