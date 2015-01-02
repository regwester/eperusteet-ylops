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
  .directive('ylopsHeader', function () {
    return {
      restrict: 'AE',
      templateUrl: 'views/common/directives/header.html',
      controller: 'YlopsHeaderController'
    };
  })

  .controller('YlopsHeaderController', function ($scope, $state) {
    $scope.$on('$stateChangeSuccess', function (event, toState) {
      if (toState.name.indexOf('root.opetussuunnitelmat.yksi') === 0) {
        $scope.crumbs = [
          {url: $state.href('root.opetussuunnitelmat.lista'), label: 'opetussuunnitelmat'}
        ];
      } else {
        $scope.crumbs = [];
      }
    });
  });
