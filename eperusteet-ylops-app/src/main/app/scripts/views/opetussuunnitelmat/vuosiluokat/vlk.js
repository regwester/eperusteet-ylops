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
.service('VuosiluokkakokonaisuusMapper', function (VuosiluokatService, $stateParams) {
  function success(res, scope, laajaalaisetosaamiset) {
    scope.perusteVlk = res;

    scope.tunnisteet = _.map(scope.perusteVlk.laajaalaisetosaamiset, '_laajaalainenosaaminen');
    var decorated = _.map(scope.perusteVlk.laajaalaisetosaamiset, function (item) {
      var base = laajaalaisetosaamiset[item._laajaalainenosaaminen];
      item.teksti = item.kuvaus;
      item.otsikko = base ? base.nimi : {fi: '[Ei nimeä]'};
      return item;
    });
    scope.laajaalaiset = _.indexBy(decorated, '_laajaalainenosaaminen');
    scope.paikalliset = _.mapValues(scope.laajaalaiset, function (item) {
      var newItem = _.cloneDeep(item);
      var model = _.find(scope.vlk.laajaalaisetosaamiset, function (osaaminen) {
        return '' + osaaminen._laajaalainenosaaminen === '' + item._laajaalainenosaaminen;
      });
      newItem.teksti = model ? model.kuvaus : {};
      return newItem;
    });
  }

  function createEmptyText(obj, key) {
    obj[key] = {
      otsikko: {fi: '-', sv: '-', en: '-', se: '-'},
      teksti: {fi: '', sv: '', en: '', se: ''}
    };
  }

  function error(scope) {
    scope.eiPerustetta = true;
    scope.perusteVlk = {};
    createEmptyText(scope.perusteVlk, 'tehtava');
    createEmptyText(scope.perusteVlk, 'siirtymaEdellisesta');
    createEmptyText(scope.perusteVlk, 'siirtymaSeuraavaan');
    if (scope.vlk && !_.isEmpty(scope.vlk.laajaalaisetosaamiset)) {
      scope.tunnisteet = _.map(scope.vlk.laajaalaisetosaamiset, '_laajaalainenosaaminen');
      scope.laajaalaiset = {};
      scope.paikalliset = {};
      _.each(scope.tunnisteet, function (tunniste) {
        var model = _.find(scope.vlk.laajaalaisetosaamiset, function (osaaminen) {
          return '' + osaaminen._laajaalainenosaaminen === tunniste;
        });
        createEmptyText(scope.laajaalaiset, tunniste);
        scope.paikalliset[tunniste] = {
          teksti: model ? model.kuvaus : {}
        };
      });
    }
  }

  this.init = function (scope, laajaalaisetosaamiset) {
    VuosiluokatService.getVlkPeruste($stateParams.id, $stateParams.vlkId, function (res) {
      success(res, scope, laajaalaisetosaamiset);
    }, function () {
      error(scope);
    });
  };

  this.createEmptyText = createEmptyText;
})

.controller('VuosiluokkakokonaisuusSortController', function($state, $scope, Editointikontrollit, OpsNavigaatio,
      vlk, vlkId, opsModel, OpetussuunnitelmaCRUD, Notifikaatiot) {
  OpsNavigaatio.setActive(false);

  $scope.oppiaineet = _(opsModel.oppiaineet)
    .map('oppiaine')
    .map(function(oa) {
      return [oa, oa.oppimaarat || []];
    })
    .flatten(true)
    .filter(function(oa) {
      oa.$$vklid = _.findIndex(oa.vuosiluokkakokonaisuudet, function(oavlk) {
        return vlk._tunniste === oavlk._vuosiluokkakokonaisuus;
      });
      return -1 !== oa.$$vklid;
    })
    .each(function(oa) {
      oa.$$jnro = oa.vuosiluokkakokonaisuudet[oa.$$vklid].jnro;
    })
    .sortBy('$$jnro')
    .value();

  $scope.sortableOptions = {
    handle: '> .handle',
    placeholder: 'placeholder-vklsort',
    connectWith: '.container-items',
    cursor: 'move',
    cursorAt: {top : 2, left: 2},
    tolerance: 'pointer',
  };

  $scope.sortableOptionsOa = {
    handle: '> .handle',
    placeholder: 'placeholder-vklsort',
    connectWith: '.container-items-oa',
    cursor: 'move',
    cursorAt: {top : 2, left: 2},
    tolerance: 'pointer',
  };

  Editointikontrollit.registerCallback({
    edit: _.noop,
    validate: _.constant(true),
    save: function() {
      var jrnoMap = _($scope.oppiaineet)
        .filter(function(oa) {
          return oa.$$vklid >= 0;
        })
        .map(function(oa) {
          return [oa, oa.oppimaarat || []];
        })
        .flatten(true)
        .map(function(oa, idx) {
          return {
            id: opsModel.id,
            lisaIdt: [oa.vuosiluokkakokonaisuudet[oa.$$vklid].id],
            jnro: idx
          };
        })
        .value();

      OpetussuunnitelmaCRUD.jarjestaOppiaineet({
        opsId: opsModel.id
      }, jrnoMap, function() {
        Notifikaatiot.onnistui('tallennettu-ok');
        $state.go('root.opetussuunnitelmat.yksi.sisalto', {}, { reload: true });
      });
    },
    cancel: function() {
      $state.go('root.opetussuunnitelmat.yksi.sisalto');
    },
    notify: _.noop
  });

  Editointikontrollit.startEditing();
})

.controller('VuosiluokkakokonaisuusController', function ($scope, Editointikontrollit, OpsNavigaatio,
  MurupolkuData, vlk, $state, $stateParams, Notifikaatiot, VuosiluokatService, Utils, Kaanna, $rootScope,
  baseLaajaalaiset, $timeout, $anchorScroll, $location, VuosiluokkakokonaisuusMapper, VuosiluokkakokonaisuusCRUD,
  OpsService, Varmistusdialogi) {

  $timeout(function () {
    if ($location.hash()) {
      $anchorScroll();
    }
  }, 1000);


  // Lokalisaatioavaimet ohjepopover:ien sisällöille
  $scope.vlkErityispiirteet = 'vuosiluokkakokonaisuuden-erityispiirteet-ja-tehtavat-info';
  $scope.siirtymaInfot = {};
  $scope.siirtymaInfot.siirtymaEdellisesta = 'vuosiluokkakokonaisuuden-siirtyma-aikaisempi-nykyinen-info';
  $scope.siirtymaInfot.siirtymaSeuraavaan = 'vuosiluokkakokonaisuuden-siirtymä-nykyinen-seuraava-info';
  $scope.vlkLaajaalaiset = 'vuosiluokkakokonaisuuden-laaja-alaisen-osaamisen-alueet-info';

  var laajaalaisetosaamiset = _.indexBy(baseLaajaalaiset, 'tunniste');
  var laajaalaisetOrder = _(baseLaajaalaiset).sortBy(Utils.sort).map('tunniste').value();
  $scope.siirtymat = ['siirtymaEdellisesta', 'siirtymaSeuraavaan'];
  var editoitavat = ['tehtava'].concat($scope.siirtymat);
  $scope.vlk = vlk;
  $scope.temp = {};
  $scope.paikalliset = {};
  $scope.orderFn = function (tunniste) {
    return laajaalaisetOrder.indexOf(tunniste);
  };

  function fetch() {
    VuosiluokatService.getVuosiluokkakokonaisuus($stateParams.id, $stateParams.vlkId, function (res) {
      $scope.vlk = res;
      initTexts();
      VuosiluokkakokonaisuusMapper.init($scope, laajaalaisetosaamiset);
    }, Notifikaatiot.serverCb);
  }

  function initTexts() {
    _.each(editoitavat, function (key) {
      $scope.temp[key] = $scope.vlk[key] || {};
    });
  }
  initTexts();

  VuosiluokkakokonaisuusMapper.init($scope, laajaalaisetosaamiset);

  MurupolkuData.set('vlkNimi', vlk.nimi);

  $scope.hasSiirtymat = function () {
    return $scope.perusteVlk && ($scope.perusteVlk.siirtymaEdellisesta || $scope.perusteVlk.siirtymaSeuraavaan);
  };

  function commitLaajaalaiset() {
    if (!$scope.vlk.laajaalaisetosaamiset) {
      $scope.vlk.laajaalaisetosaamiset = [];
    }
    _.each($scope.paikalliset, function (value, tunniste) {
      if (value.teksti) {
        var model = _.find($scope.vlk.laajaalaisetosaamiset, function (item) {
          return '' + item._laajaalainenosaaminen === '' + tunniste;
        });
        if (model) {
          model.kuvaus = value.teksti;
        } else {
          model = {
            '_laajaalainenosaaminen': tunniste,
            kuvaus: value.teksti
          };
          $scope.vlk.laajaalaisetosaamiset.push(model);
        }
      }
    });
  }

  $scope.addOppiaine = function() {
  };

  $scope.options = {
    editing: false,
    isEditable: function () {
      return OpsService.isEditable() && $scope.vlk.oma;
    }
  };

  $scope.callbacks = {
    edit: function () {
      fetch();
    },
    save: function () {
      $rootScope.$broadcast('notifyCKEditor');
      commitLaajaalaiset();
      _.each($scope.temp, function (value, key) {
        if (value !== null) {
          $scope.vlk[key] = value;
        }
      });
      $scope.vlk.$save({opsId: $stateParams.id}, function (res) {
        $scope.vlk = res;
        initTexts();
      }, Notifikaatiot.serverCb);
    },
    cancel: function () {
      fetch();
    },
    notify: function (mode) {
      $scope.options.editing = mode;
      $scope.callbacks.notifier(mode);
    },
    notifier: angular.noop
  };
  Editointikontrollit.registerCallback($scope.callbacks);

  $scope.kopioiMuokattavaksi = function () {
    Varmistusdialogi.dialogi({
      otsikko: 'varmista-kopiointi',
      primaryBtn: 'luo-kopio',
      successCb: function () {
        VuosiluokkakokonaisuusCRUD.kloonaaMuokattavaksi({
          opsId: $stateParams.id,
          vlkId: $stateParams.vlkId
        }, {}, function(res) {
          Notifikaatiot.onnistui('kopion-luonti-onnistui');
          $state.go('root.opetussuunnitelmat.yksi.opetus.vuosiluokkakokonaisuus', {
            vlkId: res.id
          }, { reload: true });
        }, Notifikaatiot.serverCb);
      }
    })();
  };
});
