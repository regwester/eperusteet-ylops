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

/**
 * General tree-like navigation menu component
 * Format of items:
 * Flat array of objects, with properties:
 * - label: Shown label, will be translated
 * - url: Precalculated url
 * - depth: Depth in hierarchy, starts from 0. Following item which has depth
 *          N + 1 is considered child of current item.
 */
ylopsApp
.directive('navimenu', function () {
  return {
    restrict: 'A',
    scope: {
      items: '=navimenu'
    },
    templateUrl: 'views/common/directives/navimenu.html',
    controller: 'NavimenuController'
  };
})

.controller('NavimenuController', function ($scope) {
  $scope.getClasses = function (item) {
    var classes = ['level' + (item.depth || 0)];
    if (item.active) {
      classes.push('active');
    }
    return classes;
  };
});
