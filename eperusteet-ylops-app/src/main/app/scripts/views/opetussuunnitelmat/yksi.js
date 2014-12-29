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
  .controller('OpetussuunnitelmaController', function ($scope, Editointikontrollit, $stateParams,
    $timeout, $state) {
    $scope.editMode = false;
    if ($stateParams.id === 'uusi') {
      $timeout(function () {
        $scope.edit();
      }, 200);
    }

    $scope.edit = function () {
      Editointikontrollit.startEditing();
    };

    var callbacks = {
      edit: function () {

      },
      save: function () {

      },
      cancel: function () {
        if ($stateParams.id === 'uusi') {
          $timeout(function () {
            $state.go('root.opetussuunnitelmat.lista');
          });
        } else {

        }
      },
      notify: function (mode) {
        $scope.editMode = mode;
      }
    };
    Editointikontrollit.registerCallback(callbacks);

  });
