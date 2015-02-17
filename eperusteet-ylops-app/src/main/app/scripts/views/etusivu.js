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
  .controller('UusiOpsController', function ($scope, $state, OpsListaService, Utils) {
    $scope.pohja = {
      active: 0,
      model: null
    };

    $scope.addNew = function (pohja) {
      var pohjaId = null;
      if (pohja) {
        pohjaId = pohja.id;
      }
      $state.go('root.opetussuunnitelmat.yksi.tiedot', {id: 'uusi', pohjaId: pohjaId});
    };

    $scope.pohjat = OpsListaService.query(true);
    $scope.sorter = Utils.sort;
  })
  .controller('EtusivuController', function () {

  });
