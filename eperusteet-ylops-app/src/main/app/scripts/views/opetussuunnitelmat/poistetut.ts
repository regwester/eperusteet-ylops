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
.controller('OpetussuunnitelmaPoistetutController', (tekstiKappaleet, oppiaineet, $scope, OpetussuunnitelmanTekstit, $stateParams,
                                                     $state, Algoritmit, $filter, OppiaineService, $modal, OpetussuunnitelmaCRUD,
                                                     Notifikaatiot) => {

  $scope.kaikki = [];
  $scope.currentPage = 1;
  $scope.itemsPerPage = 10;
  $scope.haku = '';
  const isLukio = () => $scope.model.koulutustyyppi === 'koulutustyyppi_2';

  const addItems = (items, type) => {
    _.forEach( items, (item) => {
      item.type= type;
      $scope.kaikki.push(item);
    });
  };

  addItems(tekstiKappaleet, "teksti");
  //TODO lukion oppiaineiden palauttaminen puuttuu
  if( !isLukio() ){
    addItems(oppiaineet, "oppiaine");
  }
  $scope.kaikki = _.sortBy($scope.kaikki, 'luotu');
  $scope.poistetut = $scope.kaikki;

  $scope.$watch('haku', (searchString) => {
    if(_.isEmpty(searchString)){
      $scope.poistetut = $scope.kaikki;
      return;
    }

    $scope.poistetut = _.filter($scope.kaikki, (item) => {
      const matchRemover = Algoritmit.match(searchString, item.luoja);
      const matchRemovedDate = Algoritmit.match(searchString, $filter('aikaleima')(item.luotu, 'date'));
      const matchTextTitle = Algoritmit.match(searchString, item.nimi);
      return matchRemover || matchRemovedDate || matchTextTitle;
    });
  });

  $scope.clearSearch = () => {
    $scope.haku = '';
  };

  const palautaOppiaine = (params) => {
    $modal.open({
      templateUrl: 'views/common/modals/oppiainePalautus.html',
      controller: 'OppiaineModalController',
      size: 'lg',
      resolve: {
        ops: () => {
          return OpetussuunnitelmaCRUD.get({opsId: params.opsId}, {}).$promise;
        }
      }
    }).result.then((palautettava) => {
      params.oppimaara = (palautettava.type==="oppimaara" ? palautettava.oppimaara.oppiaine.id: null);
      OppiaineService.palauta(params, {}).then((palautettu) => {
        $state.go('root.opetussuunnitelmat.yksi.opetus.oppiaine.oppiaine', {
          oppiaineId: palautettu.id,
          vlkId: _.first(palautettu.vuosiluokkakokonaisuudet).id
        }, { reload: true });
      }, () => {
        Notifikaatiot.fataali('palautus-epaonnistui');
      });
    });
  };

  $scope.returnVersion = (id, type) => {
    const params = {
      opsId: parseInt($stateParams.id),
      id: id,
    };
    if(type === "oppiaine"){
      palautaOppiaine(params);
    }else if(type === "teksti"){
      OpetussuunnitelmanTekstit.palauta( params, {}).$promise.then( () => {
        $state.go('root.opetussuunnitelmat.yksi.sisalto', { reload: true });
      });
    }
  }

})

.controller('OppiaineModalController', (ops, $scope, OppiaineService, $modalInstance) => {
  $scope.koosteiset = _.filter(ops.oppiaineet, 'oppiaine.koosteinen');
  $scope.palauta = {
    type: 'oppiaine'
  };

  $scope.isValid = () => { return ( $scope.palauta.type === 'oppimaara' ) ? !_.isEmpty($scope.palauta.oppimaara) : true };
  $scope.ok = () => $modalInstance.close($scope.palauta);
  $scope.peruuta = () => $modalInstance.dismiss();
});

