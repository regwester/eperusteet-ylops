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
  .service('ListaSorter', function (Utils, Kaanna) {
    this.init = function ($scope) {
      $scope.nimiSort = Utils.sort;
      return {
        key: 'muokattu',
        desc: true,
        set: function (key) {
          if (key === $scope.sorter.key) {
            $scope.sorter.desc = !$scope.sorter.desc;
          } else {
            $scope.sorter.key = key;
            $scope.sorter.desc = false;
          }
        },
        fn: function (item) {
          switch($scope.sorter.key) {
            case 'nimi':
              return Utils.sort(item);
            case 'muokattu':
              return item.muokattu;
            case 'tila':
              return Utils.nameSort(item, 'tila');
            case 'kunta':
              return _(item.kunnat).map(function (kunta) {
                return Kaanna.kaanna(kunta.nimi).toLowerCase();
              }).sortBy().first();
          }
        }
      };
    };
  })

  .controller('OpetussuunnitelmatListaController', function ($scope, $state,
    OpsListaService, Utils, ListaSorter) {

    $scope.sorter = ListaSorter.init($scope);
    $scope.opsiLista = true;

    $scope.items = OpsListaService.query();
    $scope.opsLimit = $state.is('root.etusivu') ? 7 : 100;

    $scope.addNew = function () {
      $state.go('root.opetussuunnitelmat.yksi.tiedot', {id: 'uusi'});
    };
  })

  .controller('TiedotteetController', function ($scope) {
    $scope.tiedotteet = [
      {nimi: {fi: 'Tiedote 1'}, muokattu: '14.1.2015'},
      {nimi: {fi: 'Jotain tärkeää on tapahtunut jossain'}, muokattu: '12.1.2015'}
    ];
  });
