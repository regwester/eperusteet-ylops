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
.controller('ValinnaisetOppiaineetController', function ($scope, vlk, $state, $stateParams, opsModel) {
  $scope.vlk = vlk;
  $scope.valinnaiset = _(opsModel.oppiaineet).map('oppiaine').filter(function (oppiaine) {
    return oppiaine.tyyppi !== 'yhteinen' && _.any(oppiaine.vuosiluokkakokonaisuudet, function (opVlk) {
      return opVlk._vuosiluokkakokonaisuus === vlk._tunniste;
    });
  }).forEach(function (oppiaine) {
    oppiaine.vlk = _(oppiaine.vuosiluokkakokonaisuudet).filter('_vuosiluokkakokonaisuus', vlk.tunniste).first();
    oppiaine.vlk.vuosiluokat = _.sortBy(oppiaine.vlk.vuosiluokat, 'vuosiluokka');
  }).value();

  $scope.addOppiaine = function () {
    $state.go('root.opetussuunnitelmat.yksi.uusioppiaine', {
      vlkId: $stateParams.vlkId
    });
  };
});
