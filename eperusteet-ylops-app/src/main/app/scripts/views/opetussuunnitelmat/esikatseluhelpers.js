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
.service('StateHelperService', function ($state, $stateParams) {
  function isActive(item, type) {
    if (type === 'oppiaine') {
      return $state.is('root.opetussuunnitelmat.yksi.esikatselu.oppiaine') &&
        ('' + item.id === '' + $stateParams.oppiaineId);
    } else if (type === 'vlk') {
      return $state.is('root.opetussuunnitelmat.yksi.esikatselu.vuosiluokkakokonaisuus') &&
        ('' + item.id === '' + $stateParams.vlkId) ||
        $state.is('root.opetussuunnitelmat.yksi.esikatselu.oppiaine') &&
        ('' + item.id === '' + $stateParams.oppiaineId) &&
        (!$stateParams.vuosiluokka || $stateParams.vuosiluokka && item.vuosiluokka === $stateParams.vuosiluokka);
    }
    return $state.is('root.opetussuunnitelmat.yksi.esikatselu.tekstikappale') &&
      ('' + item.id === '' + $stateParams.tekstikappaleId);
  }
  this.isActive = isActive;
})

.service('TreeHelper', function (Algoritmit, $state, StateHelperService) {
  function createTextTree(node, nodemap) {
    var arr = [];
    Algoritmit.traverse(node, 'lapset', function (lapsi, depth) {
      arr.push({
        label: lapsi.tekstiKappale ? lapsi.tekstiKappale.nimi : '[tyhjä viite]',
        id: lapsi.id,
        url: $state.href('root.opetussuunnitelmat.yksi.esikatselu.tekstikappale', {tekstikappaleId: lapsi.id}),
        depth: depth,
        valmis: lapsi.valmis
      });
      nodemap[lapsi.id] = lapsi;
    });
    return arr;
  }

  function getParent(tree, originIndex) {
    var index = originIndex - 1;
    var parent = -1;
    for (;index >= 0; --index) {
      if (tree[index].depth === tree[originIndex].depth - 1) {
        parent = index;
        break;
      }
    }
    return parent;
  }

  function openTree(tree, originIndex, force, toggle) {
    var childIndex = originIndex + 1;
    while (childIndex < tree.length && tree[childIndex].depth > tree[originIndex].depth) {
      if (tree[childIndex].depth === 1 || force) {
        tree[childIndex].$hidden = toggle ? !tree[childIndex].$hidden : false;
      }
      childIndex++;
    }
  }

  function updateTreeNavi(tree, type) {
    var active = null;
    _.each(tree, function (item, index) {
      item.$hidden = item.depth > 0;
      item.$header = false;
      item.$active = StateHelperService.isActive(item, type);
      if (item.$active) {
        active = index;
      }
    });
    if (active !== null) {
      openTree(tree, active);
      var origin = tree[active];
      if (origin.depth > 0) {
        origin.$hidden = false;
        var parentIndex = getParent(tree, active);
        while (parentIndex > -1) {
          var parent = tree[parentIndex];
          parent.$hidden = false;
          parent.$header = true;
          parentIndex = getParent(tree, parentIndex);
        }
      }
      _.each(tree, function (item, index) {
        if (item.$header) {
          openTree(tree, index, item.depth > 0);
        }
      });
    }
  }

  this.updateTreeNavi = updateTreeNavi;
  this.createTextTree = createTextTree;
  this.openTree = openTree;
})

.directive('epTekstiotsikko', function () {
  return {
    restrict: 'E',
    scope: {
      model: '=',
      level: '@'
    },
    template: '<span class="otsikko-wrap"><span ng-bind-html="model.tekstiKappale.nimi | kaanna | unsafe"></span>' +
    '  <span ng-if="false" class="teksti-linkki">' +
    '    <a ui-sref="^.tekstikappale({tekstikappaleId: model.id})" icon-role="new-window"></a>' +
    '  </span></span>',
    link: function (scope, element) {
      var headerEl = angular.element('<h' + scope.level + '>');
      element.find('.otsikko-wrap').wrap(headerEl);
    }
  };
})

.directive('esitysTeksti', function () {
  return {
    restrict: 'A',
    scope: {
      model: '=esitysTeksti',
      perusteModel: '=esitysPeruste',
      showPeruste: '=',
      hideOtsikko: '='
    },
    template: '<div ng-if="hasText()"><h2 ng-hide="hideOtsikko" ng-bind-html="perusteModel.otsikko | kaanna | unsafe"></h2>' +
      '<div class="esitys-peruste" ng-if="showPeruste" ng-bind-html="perusteModel.teksti | kaanna | unsafe"></div>' +
      '<div class="esitys-paikallinen" ng-bind-html="model.teksti | kaanna | unsafe"></div></div>',
    controller: function ($scope, Kieli) {
      $scope.hasText = function () {
        var hasPeruste = $scope.perusteModel && !_.isEmpty($scope.perusteModel.teksti) && !_.isEmpty($scope.perusteModel.teksti[Kieli.getSisaltokieli()]);
        var hasPaikallinen = $scope.model && !_.isEmpty($scope.model.teksti) && !_.isEmpty($scope.model.teksti[Kieli.getSisaltokieli()]);
        return (!$scope.showPeruste && hasPaikallinen) || ($scope.showPeruste && (hasPeruste || hasPaikallinen));
      };
    }
  };
});
