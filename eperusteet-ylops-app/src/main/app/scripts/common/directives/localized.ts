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
  .directive('slocalized', function($rootScope, Kieli)  {
    return {
      priority: 5,
      restrict: 'A',
      require: 'ngModel',
      link: function(scope: any, element, attrs, ngModelCtrl) {

        ngModelCtrl.$formatters.push(function(modelValue) {
          if(angular.isUndefined(modelValue)) { return; }
          if(modelValue === null) { return; }
          return modelValue[Kieli.getSisaltokieli()];
        });

        ngModelCtrl.$parsers.push(function(viewValue) {
          var localizedModelValue = ngModelCtrl.$modelValue;

          if(angular.isUndefined(localizedModelValue)) {
            localizedModelValue = {};
          }
          if(localizedModelValue === null) {
            localizedModelValue = {};
          }
          localizedModelValue[Kieli.getSisaltokieli()] = viewValue;
          return localizedModelValue;
        });

        scope.$on('changed:sisaltokieli', function(event, sisaltokieli) {
          if(ngModelCtrl.$modelValue !== null && !angular.isUndefined(ngModelCtrl.$modelValue) && !_.isEmpty(ngModelCtrl.$modelValue[sisaltokieli])) {
            ngModelCtrl.$setViewValue(ngModelCtrl.$modelValue[sisaltokieli]);
          } else {
            ngModelCtrl.$setViewValue('');
          }
          ngModelCtrl.$render();
        });
      }
    };
  });
