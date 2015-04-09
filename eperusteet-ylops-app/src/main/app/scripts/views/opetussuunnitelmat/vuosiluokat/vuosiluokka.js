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

/* global _ */

'use strict';

ylopsApp
.controller('VuosiluokkaBaseController', function ($scope, $stateParams, MurupolkuData, $state, Kaanna,
  VuosiluokatService, baseLaajaalaiset) {

  $scope.vuosiluokka = _.find($scope.oppiaineenVlk.vuosiluokat, function (vuosiluokka) {
    return '' + vuosiluokka.id === $stateParams.vlId;
  });
  $scope.vuosiluokkaNro = VuosiluokatService.fromEnum($scope.vuosiluokka.vuosiluokka);
  MurupolkuData.set('vuosiluokkaNimi', Kaanna.kaanna('vuosiluokka') + ' ' + $scope.vuosiluokkaNro);
  $scope.perusteOpVlk = $scope.perusteOppiaine ?
    _.find($scope.perusteOppiaine.vuosiluokkakokonaisuudet, function (vlk) {
      return vlk._vuosiluokkakokonaisuus === $scope.oppiaineenVlk._vuosiluokkakokonaisuus;
    }) : null;

  $scope.onValinnaiselle = !$scope.perusteOpVlk;
  $scope.sisaltoAlueetMap = _.indexBy($scope.vuosiluokka.sisaltoalueet, 'id');

  $scope.laajaalaiset = _.indexBy(baseLaajaalaiset, 'tunniste');
  $scope.perusteSisaltoalueet = $scope.perusteOpVlk ?_.indexBy($scope.perusteOpVlk.sisaltoalueet, 'tunniste') : [];

  $scope.isState = function (name) {
    return _.endsWith($state.current.name, 'vuosiluokka.' + name);
  };

  if ($state.is('root.opetussuunnitelmat.yksi.oppiaine.vuosiluokka')) {
    $state.go('.tavoitteet', {}, {location: 'replace'});
  }
})

.controller('VuosiluokkaTavoitteetController', function ($scope, VuosiluokatService, Editointikontrollit, Utils,
  $state, OppiaineService, Varmistusdialogi, Notifikaatiot, $stateParams, $rootScope) {
  $scope.tunnisteet = [];
  $scope.collapsed = {};
  $scope.nimiOrder = Utils.sort;

  function mapModel() {
    $scope.muokattavat = {};
    $scope.tavoitteet = $scope.vuosiluokka.tavoitteet;
    $scope.sisaltoAlueetMap = _.indexBy($scope.vuosiluokka.sisaltoalueet, 'id');
    processTavoitteet();
  }

  function refetch() {
    OppiaineService.fetchVuosiluokka($scope.vuosiluokka.id, function (res) {
      $scope.vuosiluokka = res;
      mapModel();
    });
  }
  refetch();

  function processTavoitteet() {
    var perusteKohdealueet = $scope.perusteOppiaine ? _.indexBy($scope.perusteOppiaine.kohdealueet, 'id') : [];
    _.each($scope.tavoitteet, function (item) {
      if ($scope.perusteOpVlk) {
        var perusteTavoite = _.find($scope.perusteOpVlk.tavoitteet, function (pTavoite) {
          return pTavoite.tunniste === item.tunniste;
        });
        item.$tavoite = perusteTavoite.tavoite;
        item.$sisaltoalueet = _.map(perusteTavoite.sisaltoalueet, function (tunniste) {
          var sisaltoalue = $scope.perusteSisaltoalueet[tunniste] || {};
          sisaltoalue.$url = $state.href('^.sisaltoalueet') + '#' + tunniste;
          return sisaltoalue;
        });
        item.$kohdealue = perusteKohdealueet[_.first(perusteTavoite.kohdealueet)];
        item.$laajaalaiset = _.map(perusteTavoite.laajaalaisetosaamiset, function (tunniste) {
          var laajaalainen = $scope.laajaalaiset[tunniste];
          laajaalainen.$url = $state.href('root.opetussuunnitelmat.yksi.vuosiluokkakokonaisuus',
            {vlkId: $stateParams.vlkId}) + '#' + tunniste;
          return laajaalainen;
        });
        item.$arvioinninkohteet = perusteTavoite.arvioinninkohteet;
      }
    });
    $scope.tavoiteMap = _.indexBy($scope.tavoitteet, 'tunniste');

    if ($scope.onValinnaiselle) {
      var otsikot = _.map($scope.tavoitteet, 'tavoite');
      var tekstit = _($scope.tavoitteet).map('sisaltoalueet').map(_.first).map(function (id) {
        return $scope.sisaltoAlueetMap[id].kuvaus;
      }).value();

      $scope.valinnaisenTekstiosat = _.map(_.zip(otsikot, tekstit), function(values) {
        return _.zipObject(['otsikko', 'teksti'], values);
      });
    }

    $scope.tunnisteet = _.keys($scope.tavoiteMap);
    _.each($scope.tunnisteet, function (tunniste) {
      var paikallinen = _.find($scope.tavoitteet, function (tavoite) {
        return tavoite.tunniste === tunniste;
      });
      $scope.muokattavat[tunniste] = (paikallinen && _.isObject(paikallinen.tavoite)) ?
      { teksti: paikallinen.tavoite,
        sisaltoalue: $scope.sisaltoAlueetMap[paikallinen.sisaltoalueet[0]] } :
      { teksti: {} };
    });

    $scope.valinnaisenTavoitteet = _($scope.muokattavat).map(function (tavoite) {
      return { otsikko: tavoite.teksti, teksti: tavoite.sisaltoalue.kuvaus };
    }).value();
  }

  $scope.options = {
    editing: false
  };

  $scope.callbacks = {
    edit: function () {
      //refetch();
    },
    save: function () {
      if ($scope.onValinnaiselle) {
        $rootScope.$broadcast('notifyCKEditor');
        var tavoitteet = angular.copy($scope.valinnaisenTavoitteet);

        OppiaineService.saveValinnainenVuosiluokka($scope.vuosiluokka.id, tavoitteet, function (res) {
          Notifikaatiot.onnistui('tallennettu-ok');
          $scope.vuosiluokka = res;
          mapModel();
        });
      } else {
        var postdata = angular.copy($scope.vuosiluokka);
        _.each(postdata.tavoitteet, function (tavoite) {
          tavoite.tavoite = $scope.muokattavat[tavoite.tunniste].teksti;
          delete tavoite.$sisaltoalueet;
          delete tavoite.$kohdealue;
          delete tavoite.$laajaalaiset;
        });
        OppiaineService.saveVuosiluokka(postdata, function (res) {
          $scope.vuosiluokka = res;
          mapModel();
        });
      }
    },
    add: function () {
      $scope.valinnaisenTavoitteet.push({
        otsikko: {},
        teksti: {}
      });
    },
    remove: function (item) {
      Varmistusdialogi.dialogi({
        otsikko: 'varmista-poisto',
        primaryBtn: 'poista',
        successCb: function () {
          var tavoitteet = _.without($scope.valinnaisenTavoitteet, item);

          OppiaineService.saveValinnainenVuosiluokka($scope.vuosiluokka.id, tavoitteet, function (res) {
            Notifikaatiot.onnistui('poisto-onnistui');
            $scope.vuosiluokka = res;
            mapModel();
          });
        }
      })();
    },
    cancel: function () {
      refetch();
    },
    notify: function (mode) {
      $scope.options.editing = mode;
      $scope.callbacks.notifier(mode);
    },
    notifier: angular.noop
  };
  Editointikontrollit.registerCallback($scope.callbacks);

}) // end of VuosiluokkaTavoitteetController

.controller('VuosiluokkaSisaltoalueetController', function ($scope, Editointikontrollit,
  $timeout, $location, $anchorScroll, OppiaineService, Utils) {

  $scope.tunnisteet = [];
  $scope.muokattavat = {};
  $scope.sisaltoInfo = {};
  $scope.sisaltoInfoCollapse = false;

  function mapModel() {
    $scope.sisaltoalueet = $scope.vuosiluokka.sisaltoalueet;
    $scope.tunnisteet = _($scope.sisaltoalueet).sortBy(Utils.sort).map('tunniste').value();
    _.each($scope.tunnisteet, function (tunniste) {
      var paikallinen = _.find($scope.sisaltoalueet, function (alue) {
        return alue.tunniste === tunniste;
      });
      $scope.muokattavat[tunniste] = (paikallinen && _.isObject(paikallinen.kuvaus)) ? {teksti: paikallinen.kuvaus} : {teksti: {}};
    });
  }

  function refetch() {
    OppiaineService.fetchVuosiluokka($scope.vuosiluokka.id, function (res) {
      $scope.vuosiluokka = res;
      mapModel();
    });
  }
  refetch();

  $scope.options = {
    editing: false
  };

  $scope.callbacks = {
    edit: function () {
      refetch();
    },
    save: function () {
      _.each($scope.vuosiluokka.sisaltoalueet, function (alue) {
        alue.kuvaus = $scope.muokattavat[alue.tunniste].teksti;
      });
      OppiaineService.saveVuosiluokka($scope.vuosiluokka, function (res) {
        $scope.vuosiluokka = res;
        mapModel();
      });
    },
    cancel: function () {
      refetch();
    },
    notify: function (mode) {
      $scope.options.editing = mode;
      $scope.callbacks.notifier(mode);
    },
    notifier: angular.noop
  };
  Editointikontrollit.registerCallback($scope.callbacks);

  $timeout(function () {
    if ($location.hash()) {
      $anchorScroll();
    }
  }, 1000);

})

.directive('opsTeksti', function ($timeout, $window) {
  return {
    restrict: 'A',
    scope: {
      muokattava: '=opsTeksti',
      callbacks: '=',
      config: '='
    },
    templateUrl: 'views/opetussuunnitelmat/vuosiluokat/directives/opsteksti.html',
    controller: 'TekstiosaController',
    link: function (scope, element, attrs) {
      scope.editable = !!attrs.opsTeksti;
      scope.options = {
        collapsed: scope.editable
      };
      scope.isEmpty = _.isEmpty;
      scope.focusAndScroll = function () {
        $timeout(function () {
          var el = element.find('[ckeditor]');
          if (el && el.length > 0) {
            el[0].focus();
            $window.scrollTo(0, el.eq(0).offset().top - 400);
          }
        }, 300);
      };
    }
  };
})

.directive('valinnaisenOpsTeksti', function () {
  return {
    restrict: 'A',
    scope: {
      muokattava: '=valinnaisenOpsTeksti',
      callbacks: '=',
      config: '='
    },
    templateUrl: 'views/opetussuunnitelmat/vuosiluokat/directives/valinnaisenopsteksti.html',
    controller: 'TekstiosaController',
    link: function (scope, element, attrs) {
      scope.editable = !!attrs.valinnaisenOpsTeksti;
      scope.options = {
        collapsed: scope.editable
      };
      scope.isEmpty = _.isEmpty;

      scope.focusAndScroll = angular.noop;
    }
  };
})

.directive('popoverHtml', function ($document) {
  var OPTIONS = {
    html: true,
    placement: 'bottom'
  };
  return {
    restrict: 'A',
    link: function (scope, element) {
      element.popover(OPTIONS);

      // Click anywhere else to close
      $document.on('click', function (event) {
        var clickedParent = angular.element(event.target).hasClass('laajaalainen-popover');
        var clickedContent = angular.element(event.target).closest('.popover').length > 0;
        if (clickedParent || clickedContent || element.find(event.target).length > 0) {
          return;
        }
        element.popover('hide');
      });

      scope.$on('$destroy', function () {
        $document.off('click');
        element.popover('destroy');
      });
    }
  };
});
