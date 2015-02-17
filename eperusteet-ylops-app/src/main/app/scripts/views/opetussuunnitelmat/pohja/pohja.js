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
.controller('PohjaController', function ($scope, $state, pohjaModel, opsService) {
  if ($state.current.name === 'root.pohjat.yksi') {
    $state.go('root.pohjat.yksi.sisalto', {}, {location: 'replace'});
  }
  $scope.model = pohjaModel;
  $scope.$on('rakenne:updated', function () {
    $scope.model = opsService.getPohja();
  });
})

.controller('PohjaListaController', function ($scope, $state, OpetussuunnitelmaCRUD) {
  $scope.items = OpetussuunnitelmaCRUD.query({tyyppi: 'pohja'});
  $scope.opsLimit = $state.is('root.etusivu') ? 7 : 100;

  $scope.addNew = function () {
    $state.go('root.pohjat.yksi.tiedot', {pohjaId: 'uusi'});
  };
})

.controller('PohjaSisaltoController', function ($scope, Algoritmit, Utils, $stateParams, OpetussuunnitelmanTekstit,
  Notifikaatiot, $state, TekstikappaleOps) {
  $scope.uusi = {nimi: {}};
  $scope.rakenneEdit = {jarjestaminen: false, lahtokohdat: false};
  $scope.kappaleEdit = null;

  function mapModel() {
    Algoritmit.traverse($scope.model.tekstit, 'lapset', function (teksti) {
      teksti.$url = $state.href('root.pohjat.yksi.tekstikappale', {
        pohjaId: $scope.model.id,
        tekstikappaleId: teksti.id
      });
    });
    $scope.model.jarjestaminen = $scope.model.tekstit ? $scope.model.tekstit.lapset[0] : [];
    $scope.model.lahtokohdat = $scope.model.tekstit ? $scope.model.tekstit.lapset[1] : [];
  }

  $scope.$watch('model.tekstit', function () {
    mapModel();
  }, true);

  $scope.hasText = function () {
    return Utils.hasLocalizedText($scope.uusi.nimi);
  };

  $scope.pohjaOps = {
    addNew: function (osio) {
      TekstikappaleOps.add($scope.model, osio, $stateParams.pohjaId, $scope.uusi, function () {
        $scope.uusi = {nimi: {}};
        mapModel();
      });
    },
    edit: function (kappale) {
      $scope.kappaleEdit = kappale;
      kappale.$original = _.cloneDeep(kappale.tekstiKappale);
    },
    delete: function (osio, kappale) {
      TekstikappaleOps.delete($scope.model, osio, $stateParams.pohjaId, kappale);
    },
    cancel: function (kappale) {
      $scope.kappaleEdit = null;
      kappale.tekstiKappale = _.cloneDeep(kappale.$original);
      delete kappale.$original;
    },
    save: function (kappale) {
      $scope.kappaleEdit = null;
      var params = {opsId: $stateParams.pohjaId};
      OpetussuunnitelmanTekstit.save(params, _.omit(kappale, 'lapset'), function () {
        Notifikaatiot.onnistui('tallennettu-ok');
        delete kappale.$original;
      }, Notifikaatiot.serverCb);
    }
  };

  $scope.rakenne = {
    edit: function (osio) {
      $scope.rakenneEdit[osio] = true;
      $scope.model[osio].$original = _.cloneDeep($scope.model[osio].lapset);
    },
    save: function (osio) {
      TekstikappaleOps.saveRakenne($scope.model, function () {
        $scope.rakenneEdit[osio] = false;
        delete $scope.model[osio].$original;
      });
    },
    cancel: function (osio) {
      $scope.rakenneEdit[osio] = false;
      $scope.model[osio].lapset = _.cloneDeep($scope.model[osio].$original);
      delete $scope.model[osio].$original;
    }
  };
});
