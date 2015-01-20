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

.controller('PohjaController', function ($state) {
  if ($state.current.name === 'root.pohjat.yksi') {
    $state.go('root.pohjat.yksi.sisalto');
  }
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

.controller('PohjaSisaltoController', function () {

});
