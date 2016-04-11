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


.controller('OppiaineSortController', ($q, $rootScope, $scope, $state, $stateParams, $timeout, Editointikontrollit,
                                       Notifikaatiot, OpetussuunnitelmaCRUD, opsModel) => {

  $rootScope.$broadcast('navigaatio:hide');
  const vuosiluokat = _.map(opsModel.vuosiluokkakokonaisuudet, 'vuosiluokkakokonaisuus._tunniste');

  $scope.oppiaineet = _(opsModel.oppiaineet)
      .map('oppiaine')
      .map((oa) => [oa || []])
      .filter((oa) => {
        let kokonaisuudet = [_.map(oa[0].vuosiluokkakokonaisuudet, '_vuosiluokkakokonaisuus')];
        if(!_.isEmpty(oa[0].oppimaarat)){
          const vlk = _.map(oa[0].oppimaarat, 'vuosiluokkakokonaisuudet');
          kokonaisuudet.push(_.unique(_.map(_.flatten(vlk), '_vuosiluokkakokonaisuus')));
        }
        return _.intersection(_.flatten(kokonaisuudet), vuosiluokat).length > 0;
      })
      .flatten(true)
      .forEach((oa) => {
        if(!_.isEmpty(oa.oppimaarat)){
          const maarienVlk = _.flatten(_.map(oa.oppimaarat, 'vuosiluokkakokonaisuudet'));
          oa.$$jnro = _.min(_.map(maarienVlk, 'jnro'));
        }
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
    edit: () => $q.when(),
    save: () => $q((resolve) => {
      resolve();
      const jrnoMap = _($scope.oppiaineet)
          .map((oa) => [oa, oa.oppimaarat || []])
          .flatten(true)
          .map((oa, idx) => ({
            oppiaineId: oa.id,
            jnro: idx
          }))
          .value();

      OpetussuunnitelmaCRUD.jarjestaOppiaineet({
        opsId: opsModel.id
      }, jrnoMap, () => {
        resolve();
        Notifikaatiot.onnistui('tallennettu-ok');
        $timeout(() => {
          $state.go('^.vuosiluokkakokonaisuus', $stateParams, { reload: true });
        });
      }, (res) => Notifikaatiot.fataali( _.has(res, 'data.syy') ? res.data.syy : 'tallennus-epaonnistui'));
    }),
    cancel: () => $q((resolve) => {
      resolve();
      $timeout(() => {
        $state.go('^.vuosiluokkakokonaisuus', $stateParams);
      });
    }),
    notify: _.noop
  });

  $timeout(Editointikontrollit.startEditing);
})

.controller('VuosiluokkakokonaisuusController', function(
      $anchorScroll, $location, $q, $rootScope, $scope, $state, $stateParams, $timeout,
      Editointikontrollit, Kaanna, MurupolkuData, Notifikaatiot, OpsService, Utils, Varmistusdialogi,
      VuosiluokatService, VuosiluokkakokonaisuusCRUD, VuosiluokkakokonaisuusMapper, baseLaajaalaiset, vlk) {

  $scope.sortOppiaineet = () => $state.go('^.oppiainesort', $stateParams);

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

  const fetch = () => $q((resolve) => {
    VuosiluokatService.getVuosiluokkakokonaisuus($stateParams.id, $stateParams.vlkId, (res) => {
      $scope.vlk = res;
      initTexts();
      VuosiluokkakokonaisuusMapper.init($scope, laajaalaisetosaamiset);
      resolve();
    }, Notifikaatiot.serverCb);
  });

  function initTexts() {
    _.each(editoitavat, function (key) {
      $scope.temp[key] = $scope.vlk[key] || {};
    });
  }
  initTexts();

  VuosiluokkakokonaisuusMapper.init($scope, laajaalaisetosaamiset);

  MurupolkuData.set('vlkNimi', vlk.nimi);

  $scope.hasSiirtymat = () => {
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
    edit: fetch,
    cancel: fetch,
    save: () => $q((resolve) => {
      commitLaajaalaiset();
      _.each($scope.temp, (value, key) => {
        if (value !== null) {
          $scope.vlk[key] = value;
        }
      });
      $scope.vlk.$save({opsId: $stateParams.id}, (res) => {
        $scope.vlk = res;
        initTexts();
        Notifikaatiot.onnistui('tallennettu-ok');
        resolve();
      }, Notifikaatiot.serverCb);
    }),
    notify: (mode) => {
      $scope.options.editing = mode;
      $scope.callbacks.notifier(mode);
    },
    notifier: _.noop
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
