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
.controller('EtusivuController', function ($scope, Oikeudet, $state) {
  $scope.isVirkailija = Oikeudet.isVirkailija;

  $scope.addNewPohja = function () {
    $state.go('root.pohjat.yksi.tiedot', {pohjaId: 'uusi'});
  };
})

.controller('EsikatseluController', function ($scope, $state, Algoritmit, Utils, $stateParams, StateHelperService, TreeHelper) {

  function updateNavi() {
    TreeHelper.updateTreeNavi($scope.texttree);
    TreeHelper.updateTreeNavi($scope.oppiaineMenu, 'oppiaine');
    TreeHelper.updateTreeNavi($scope.vlkMenu, 'vlk');
  }

  $scope.$on('$stateChangeSuccess', updateNavi);

  $scope.oppiaineSort = function (item) {
    return Utils.sort(item.oppiaine);
  };

  $scope.yhteiset = function (item) {
    return item.oppiaine.tyyppi === 'yhteinen';
  };
  $scope.valinnaiset = _.negate($scope.yhteiset);

  function buildOppiaineMenu() {
    _($scope.model.oppiaineet).filter($scope.yhteiset).sortBy($scope.oppiaineSort).each(function(oppiaine) {
      $scope.oppiaineMenu.push(oppiaine.oppiaine);
      oppiaine.oppiaine.depth = 0;
      _(oppiaine.oppiaine.oppimaarat).sortBy(Utils.sort).each(function(oppimaara) {
        $scope.oppiaineMenu.push(oppimaara);
        oppimaara.depth = 1;
      }).value();
    }).value();
    $scope.oppiaineMenu.push({label: 'valinnaiset-oppiaineet', depth: 0});
    _($scope.model.oppiaineet).filter($scope.valinnaiset).sortBy($scope.oppiaineSort).each(function(oppiaine) {
      $scope.oppiaineMenu.push(oppiaine.oppiaine);
      oppiaine.oppiaine.depth = 1;
    }).value();
  }

  var vlkOppiaineMap = {};
  function preprocessVuosiluokat() {
    _.each($scope.vuosiluokkakokonaisuudet, function (vlk) {
      vlkOppiaineMap[vlk.vuosiluokkakokonaisuus._tunniste] = {
        nimi: vlk.vuosiluokkakokonaisuus.nimi,
        vuosiluokkaMap: {},
        oppiaineMap: {}
      };
    });
    function storeOppiaine(oa) {
      _.each(oa.vuosiluokkakokonaisuudet, function (opVlk) {
        vlkOppiaineMap[opVlk._vuosiluokkakokonaisuus].oppiaineMap[oa.tunniste] = oa;
        _.each(opVlk.vuosiluokat, function (vl) {
          if (!vlkOppiaineMap[opVlk._vuosiluokkakokonaisuus].vuosiluokkaMap[vl.vuosiluokka]) {
            vlkOppiaineMap[opVlk._vuosiluokkakokonaisuus].vuosiluokkaMap[vl.vuosiluokka] = {
              oppiaineet: {},
              vuosiluokka: vl.vuosiluokka
            };
          }
          vlkOppiaineMap[opVlk._vuosiluokkakokonaisuus].vuosiluokkaMap[vl.vuosiluokka].oppiaineet[oa.tunniste] = oa;
        });
      });
    }
    _.each($scope.model.oppiaineet, function (oppiaine) {
      var oa = oppiaine.oppiaine;
      storeOppiaine(oa);
      _.each(oa.oppimaarat, storeOppiaine);
    });
    _.each(vlkOppiaineMap, function (vlkObj) {
      vlkObj.vuosiluokat = _.sortBy(vlkObj.vuosiluokkaMap, 'vuosiluokka');
    });
  }

  $scope.vlkMenu = [];
  function buildVuosiluokkaMenu() {
    _.each($scope.vuosiluokkakokonaisuudet, function (vlk) {
      var item = _.extend({
        url: $state.href('root.opetussuunnitelmat.yksi.esikatselu.vuosiluokkakokonaisuus', {
          vlkId: vlk.vuosiluokkakokonaisuus.id,
          menu: null
        }),
        depth: 0
      }, vlk.vuosiluokkakokonaisuus);
      $scope.vlkMenu.push(item);
      _.each(vlkOppiaineMap[vlk.vuosiluokkakokonaisuus._tunniste].vuosiluokat, function (vl) {
        item = {
          nimi: 'vuosiluokka',
          numero: vl.vuosiluokka,
          id: vl.vuosiluokka,
          depth: 1
        };
        $scope.vlkMenu.push(item);
        _.each(vl.oppiaineet, function (oa) {
          item = _.clone(oa);
          item.depth = 2;
          item.vuosiluokka = vl.vuosiluokka;
          item.url = $state.href('root.opetussuunnitelmat.yksi.esikatselu.oppiaine', {
            oppiaineId: oa.id,
            oppiaineTyyppi: oa.tyyppi,
            vuosiluokka: vl.vuosiluokka
          });
          $scope.vlkMenu.push(item);
        });
      });
    });
  }

  $scope.tekstikappaleMap = {};
  $scope.texttree = TreeHelper.createTextTree($scope.model.tekstit, $scope.tekstikappaleMap);
  $scope.oppiaineMenu = [];
  buildOppiaineMenu();
  preprocessVuosiluokat();
  buildVuosiluokkaMenu();


  $scope.switchTab = function (tabId) {
    $state.go($state.current, {menu: tabId});
  };

  $scope.isState = function (tabId) {
    return $stateParams.menu ? $stateParams.menu === tabId : tabId === 'vuosiluokittain';
  };
  updateNavi();

  $scope.toggleVlk = function (index) {
    TreeHelper.openTree($scope.vlkMenu, index, true, true);
  };
})

.controller('EsikatseluTekstikappaleController', function ($scope, $stateParams) {
  $scope.tekstikappaleViite = $scope.tekstikappaleMap[$stateParams.tekstikappaleId];
  $scope.tekstikappale = $scope.tekstikappaleViite.tekstiKappale;
})

.controller('EsikatseluVlkController', function ($scope, $stateParams, baseLaajaalaiset, Utils,
  VuosiluokkakokonaisuusMapper) {
  $scope.vuosiluokkakokonaisuus = _.find($scope.model.vuosiluokkakokonaisuudet, function (vlk) {
    return '' + vlk.vuosiluokkakokonaisuus.id === '' + $stateParams.vlkId;
  });
  $scope.vlk = $scope.vuosiluokkakokonaisuus ? $scope.vuosiluokkakokonaisuus.vuosiluokkakokonaisuus : {};
  var laajaalaisetosaamiset = _.indexBy(baseLaajaalaiset, 'tunniste');
  var laajaalaisetOrder = _(baseLaajaalaiset).sortBy(Utils.sort).map('tunniste').value();
  $scope.orderFn = function (tunniste) {
    return laajaalaisetOrder.indexOf(tunniste);
  };
  VuosiluokkakokonaisuusMapper.init($scope, laajaalaisetosaamiset);
})

.controller('EsikatseluOppiaineController', function ($scope, $stateParams, perusteOppiaine, Kaanna,
  VuosiluokatService, VuosiluokkaMapper, baseLaajaalaiset, Utils) {
  $scope.nimiOrder = Utils.sort;
  var oppimaara = null;
  var oppiaine = _.find($scope.model.oppiaineet, function (oa) {
    var found = '' + oa.oppiaine.id === '' + $stateParams.oppiaineId;
    if (!found) {
      found = _.find(oa.oppiaine.oppimaarat, function (om) {
        return '' + om.id === '' + $stateParams.oppiaineId;
      });
      if (found) {
        oppimaara = found;
      }
    }
    return found;
  });
  $scope.laajaalaiset = _.indexBy(baseLaajaalaiset, 'tunniste');
  $scope.perusteOppiaine = perusteOppiaine;
  $scope.oppiaine = oppimaara ? oppimaara : oppiaine.oppiaine;
  $scope.vlkMap = _.indexBy($scope.vuosiluokkakokonaisuudet, function (vlk) {
    return vlk.vuosiluokkakokonaisuus._tunniste;
  });
  $scope.perusteOppiaineVlkMap = $scope.perusteOppiaine ? _.indexBy($scope.perusteOppiaine.vuosiluokkakokonaisuudet, '_vuosiluokkakokonaisuus') : {};

  function getVuosiluokat() {
    var vuosiluokat = {};
    _.each($scope.oppiaine.vuosiluokkakokonaisuudet, function (opVlk) {
      _.each(opVlk.vuosiluokat, function (vl) {
        vuosiluokat[vl.vuosiluokka] = vl;
      });
    });
    return _.values(vuosiluokat);
  }

  function getFirstVuosiluokka() {
    return _.first(_.sortBy($scope.vuosiluokat, 'vuosiluokka'));
  }
  $scope.vuosiluokat = getVuosiluokat();

  function setActiveVuosiluokka() {
    $scope.activeVuosiluokka = _.find($scope.vuosiluokat, function (vl) {
      return $stateParams.vuosiluokka === vl.vuosiluokka;
    });
    if (!$scope.activeVuosiluokka) {
      $scope.activeVuosiluokka = getFirstVuosiluokka();
    }
  }
  setActiveVuosiluokka();

  $scope.$on('$stateChangeSuccess', setActiveVuosiluokka);

  $scope.vuosiluokkaFilter = function (item) {
    return item === $scope.activeVuosiluokka;
  };

  $scope.vlkFilter = function (vlk) {
    return !_.isEmpty(_.filter(vlk.vuosiluokat, function (vl) {
      return $scope.activeVuosiluokka && vl.vuosiluokka === $scope.activeVuosiluokka.vuosiluokka;
    }));
  };

  $scope.isActive = function (vuosiluokka) {
    return $scope.activeVuosiluokka === vuosiluokka;
  };

  $scope.vlkSort = function (oppiaineVlk) {
    return Kaanna.kaanna($scope.vlkMap[oppiaineVlk._vuosiluokkakokonaisuus].vuosiluokkakokonaisuus.nimi);
  };

  $scope.vuosiluokkaSisallot = {};

  _.each($scope.oppiaine.vuosiluokkakokonaisuudet, function (opVlk) {
    $scope.vuosiluokkaSisallot[opVlk._vuosiluokkakokonaisuus] = {};
    _.each(opVlk.vuosiluokat, function (vuosiluokka) {
      vuosiluokka.$numero = VuosiluokatService.fromEnum(vuosiluokka.vuosiluokka);
      var perusteOpVlk = $scope.perusteOppiaineVlkMap[opVlk._vuosiluokkakokonaisuus];
      $scope.vuosiluokkaSisallot[opVlk._vuosiluokkakokonaisuus][vuosiluokka.vuosiluokka] = {
        vuosiluokka: vuosiluokka,
        perusteOpVlk: perusteOpVlk,
        perusteSisaltoalueet: perusteOpVlk ?_.indexBy(perusteOpVlk.sisaltoalueet, 'tunniste') : [],
        laajaalaiset: $scope.laajaalaiset,
        sisaltoalueet: vuosiluokka.sisaltoalueet,
        onValinnaiselle: $scope.oppiaine.tyyppi !== 'yhteinen'
      };
      VuosiluokkaMapper.mapModel($scope.vuosiluokkaSisallot[opVlk._vuosiluokkakokonaisuus][vuosiluokka.vuosiluokka]);
      VuosiluokkaMapper.mapSisaltoalueet($scope.vuosiluokkaSisallot[opVlk._vuosiluokkakokonaisuus][vuosiluokka.vuosiluokka],
        'sisaltoaluetunnisteet', 'sisaltoaluemuokattavat');
    });
  });

});
