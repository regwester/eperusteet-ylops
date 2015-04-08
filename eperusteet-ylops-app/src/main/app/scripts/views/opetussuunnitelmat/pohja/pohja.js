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

.controller('PohjaListaController', function ($scope, $state, OpetussuunnitelmaCRUD, ListaSorter) {
  $scope.items = OpetussuunnitelmaCRUD.query({tyyppi: 'pohja'});
  $scope.opsLimit = $state.is('root.etusivu') ? 7 : 100;
  $scope.sorter = ListaSorter.init($scope);

  $scope.addNew = function () {
    $state.go('root.pohjat.yksi.tiedot', {pohjaId: 'uusi'});
  };
})

.controller('PohjaSisaltoController', function ($scope, Algoritmit, Utils, $stateParams, OpetussuunnitelmanTekstit,
  Notifikaatiot, $state, TekstikappaleOps) {
  $scope.kappaleEdit = null;

  function mapModel() {
    Algoritmit.traverse($scope.model.tekstit, 'lapset', function (teksti) {
      teksti.$url = $state.href('root.pohjat.yksi.tekstikappale', {
        pohjaId: $scope.model.id,
        tekstikappaleId: teksti.id
      });
    });
    if ($scope.model.tekstit) {
      $scope.model.lapset = _.map($scope.model.tekstit.lapset, function(lapsi) {
        return _.extend(lapsi, { $$nimi: {} });
      });
    }
  }

  $scope.$watch('model.tekstit', function () {
    mapModel();
  }, true);

  $scope.hasText = function(str) {
    return Utils.hasLocalizedText(str);
  };

  $scope.opsOtsikot = {
    edit: function(kappale) {
      kappale.tekstiKappale.$$original = _.cloneDeep(kappale.tekstiKappale);
      kappale.tekstiKappale.$$edit = true;
    },
    save: function(kappale) {
      $scope.pohjaOps.save(kappale, function() {
        kappale.tekstiKappale.$$edit = false;
      });
    },
    cancel: function(kappale) {
      kappale.tekstiKappale = kappale.tekstiKappale.$$original;
      kappale.tekstiKappale.$$edit = false;
    },
    addNew: function() {
    }
  };

  $scope.pohjaOps = {
    addNew: function (osio) {
      TekstikappaleOps.lisaa(osio, $stateParams.pohjaId, osio.$$nimi, function () {
        osio.$$nimi = {};
        mapModel();
      });
    },
    edit: function (kappale) {
      $scope.kappaleEdit = kappale;
      kappale.$$original = _.cloneDeep(kappale.tekstiKappale);
    },
    delete: function (osio, kappale) {
      TekstikappaleOps.varmistusdialogi(kappale.tekstiKappale.nimi, function () {
        TekstikappaleOps.delete($scope.model, osio, $stateParams.pohjaId, kappale, function () {
          mapModel();
        });
      });
    },
    cancel: function (kappale) {
      $scope.kappaleEdit = null;
      kappale.tekstiKappale = _.cloneDeep(kappale.$$original);
      kappale = _.omit(kappale, '$$original');
    },
    save: function (kappale, cb) {
      cb = cb || angular.noop;
      $scope.kappaleEdit = null;
      var params = {opsId: $stateParams.pohjaId};
      OpetussuunnitelmanTekstit.save(params, _.omit(kappale, 'lapset'), function () {
        Notifikaatiot.onnistui('tallennettu-ok');
        kappale = _.omit(kappale, '$$original');
        cb();
      }, Notifikaatiot.serverCb);
    }
  };

  $scope.rakenne = {
    edit: function (osio) {
      osio.$$edit = true;
      osio.$$original = _.cloneDeep(osio.lapset);
    },
    save: function (osio) {
      TekstikappaleOps.saveRakenne($scope.model, function () {
        osio.$$edit = false;
        osio = _.omit(osio, '$$original');
      });
    },
    cancel: function (osio) {
      osio.$$edit = false;
      osio.lapset = _.cloneDeep(osio.$$original);
      osio = _.omit(osio, '$$original');
    }
  };
});
