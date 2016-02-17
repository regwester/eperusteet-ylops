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
/*global _*/

ylopsApp
.controller('OpetussuunnitelmaPoistetutController', (poistetut, $scope, OpetussuunnitelmanTekstit, $stateParams,
                                                     $state, Algoritmit, $filter) => {

  $scope.kaikki = poistetut;
  $scope.poistetut = poistetut;
  $scope.currentPage = 1;
  $scope.itemsPerPage = 10;
  $scope.haku = '';

  $scope.$watch('haku', (searchString) => {
    if(_.isEmpty(searchString)){
      $scope.poistetut = $scope.kaikki;
      return;
    }

    $scope.poistetut = _.filter($scope.kaikki, (item) => {
      const matchRemover = Algoritmit.match(searchString, item.luoja);
      const matchRemovedDate = Algoritmit.match(searchString, $filter('aikaleima')(item.luotu, 'date'));
      const matchTextTitle = Algoritmit.match(searchString, item.tekstiKappale.nimi);
      return matchRemover || matchRemovedDate || matchTextTitle;
    });
  });

  $scope.clearSearch = () => {
    $scope.haku = '';
  };

  $scope.returnVersion = (id) => {
    const params = {
      opsId: parseInt($stateParams.id),
      id: id,
    };
    OpetussuunnitelmanTekstit.palauta( params, {}).$promise.then( () => {
      $state.go('root.opetussuunnitelmat.yksi.sisalto', { reload: true });
    });
  }

});
