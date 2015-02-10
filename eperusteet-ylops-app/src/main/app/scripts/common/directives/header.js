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
  .service('MurupolkuData', function ($rootScope) {
    var data = {};
    this.set = function (key, value) {
      if (_.isObject(key)) {
        _.each(key, function (item, k) {
          data[k] = item;
        });
      } else {
        data[key] = value;
      }
      $rootScope.$broadcast('murupolku:update');
    };
    this.get = function (key) {
      return data[key];
    };
  })

  .directive('ylopsHeader', function () {
    return {
      restrict: 'AE',
      scope: {},
      templateUrl: 'views/common/directives/header.html',
      controller: 'YlopsHeaderController'
    };
  })

  .controller('YlopsHeaderController', function ($scope, $state, Oikeudet, MurupolkuData, Kaanna) {
    var currentState = null;
    var STATE_ROOTS = {
      'root.opetussuunnitelmat.yksi': {
        state: 'root.opetussuunnitelmat.lista',
        label: 'opetussuunnitelmat'
      },
      'root.pohjat.yksi': {
        state: 'root.pohjat.lista',
        label: 'pohjat'
      },
    };

    var STATES = {
      'root.opetussuunnitelmat.yksi.sisalto': {
        useData: 'opsNimi'
      },
      'root.opetussuunnitelmat.yksi.sisaltoalue': {
        parent: 'root.opetussuunnitelmat.yksi.sisalto',
        useData: 'osioNimi',
        useId: 'alueId'
      },
      'root.opetussuunnitelmat.yksi.tiedot': {
        parent: 'root.opetussuunnitelmat.yksi.sisalto',
        label: 'opsn-tiedot'
      },
      'root.opetussuunnitelmat.yksi.esikatselu': {
        parent: 'root.opetussuunnitelmat.yksi.sisalto'
      },
      'root.opetussuunnitelmat.yksi.tekstikappale': {
        useData: 'tekstiNimi',
        parent: 'root.opetussuunnitelmat.yksi.sisaltoalue'
      },
      'root.pohjat.yksi.sisalto': {
        useData: 'opsNimi'
      },
      'root.pohjat.yksi.tiedot': {
        parent: 'root.pohjat.yksi.sisalto',
        label: 'pohjan-tiedot'
      },
      'root.pohjat.yksi.tekstikappale': {
        useData: 'tekstiNimi',
        parent: 'root.pohjat.yksi.sisalto'
      },
      'root.opetussuunnitelmat.yksi.vuosiluokkakokonaisuus': {
        useData: 'vlkNimi',
        useId: 'vlkId',
        parent: 'root.opetussuunnitelmat.yksi.sisaltoalue'
      },
      'root.opetussuunnitelmat.yksi.oppiaine.oppiaine': {
        useData: 'oppiaineNimi',
        parent: 'root.opetussuunnitelmat.yksi.vuosiluokkakokonaisuus'
      },
      'root.opetussuunnitelmat.yksi.oppiaine.vuosiluokka.tavoitteet': {
        useData: 'vuosiluokkaNimi',
        parent: 'root.opetussuunnitelmat.yksi.oppiaine.oppiaine'
      },
      'root.opetussuunnitelmat.yksi.oppiaine.vuosiluokka.sisaltoalueet': {
        useData: 'vuosiluokkaNimi',
        parent: 'root.opetussuunnitelmat.yksi.oppiaine.oppiaine'
      }
    };

    function getPath(state) {
      var tree = [];
      if (!state) {
        return tree;
      }
      var current = STATES[state];
      if (!current) {
        return tree;
      } else {
        tree.push(_.extend({state: state}, current));
        var parents = getPath(current.parent);
        if (!_.isEmpty(parents)) {
          tree = tree.concat(parents);
        }
      }
      return tree;
    }

    function setTitle() {
      var titleEl = angular.element('head > title');
      var leaf = _.last($scope.crumbs);
      var last = leaf ? Kaanna.kaanna(leaf.label) : null;
      titleEl.html(Kaanna.kaanna('ops-tyokalu') + (last ? ' – ' + last : ''));
    }

    function update() {
      var toState = currentState;
      if (!toState) {
        return;
      }
      $scope.crumbs = [];

      _.each(STATE_ROOTS, function (root, key) {
        if (toState.name.indexOf(key) === 0 && toState.name !== key) {
          $scope.crumbs.push({
            url: $state.href(root.state),
            label: root.label
          });
        }
      });

      var path = getPath(toState.name);
      _(path).reverse().each(function (item) {
        var params = {};
        if (item.useId) {
          params[item.useId] = MurupolkuData.get(item.useId);
        }
        $scope.crumbs.push({
          url: $state.href(item.state, params),
          label: item.useData ? MurupolkuData.get(item.useData) :
                 (item.label ? item.label : _.last(item.state.split('.')))
        });
      }).value();

      setTitle();
    }

    $scope.isVirkailija = Oikeudet.isVirkailija();

    $scope.$on('murupolku:update', update);

    $scope.$on('$stateChangeSuccess', function (event, toState) {
      currentState = toState;
      update();
    });

    $scope.$watch('isVirkailija', function (value) {
      Oikeudet.setVirkailija(value);
    });
  });
