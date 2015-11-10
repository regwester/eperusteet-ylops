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

/**
 * General tree-like navigation menu component
 * Format of items:
 * Flat array of objects, with properties:
 * - label: Shown label, will be translated
 * - url: Precalculated url
 * - depth: Depth in hierarchy, starts from 0. Following item which has depth
 *          N + 1 is considered child of current item.
 */
ylopsApp
.directive('navimenu', function () {
  return {
    restrict: 'A',
    scope: {
      items: '=navimenu'
    },
    templateUrl: 'views/common/directives/navimenu.html',
    controller: 'NavimenuController'
  };
})

.controller('NavimenuController', function ($scope) {
  $scope.$watch('items', function () {
    $scope.refresh();
  }, true);

  $scope.$on('$stateChangeSuccess', function () {
    // This uncollapses the current active item if it has children
    updateModel($scope.items);
  });

  $scope.getClasses = function (item) {
    var classes = ['level' + (item.depth || 0)];
    if (item.active) {
      classes.push('active');
    }
    return classes;
  };

  var doRefresh = function (items) {
    var levels = {};
    if (items && items.length && !items[0].root) {
      items.unshift({root: true, depth: -1});
    }
    _.each(items, function (item, index) {
      item.depth = item.depth || 0;
      levels[item.depth] = index;
      item.$parent = levels[item.depth - 1] || null;
      item.$hidden = item.depth > 0;
      item.$matched = true;
    });
    updateModel(items);
  };

  $scope.refresh = function () {
    doRefresh($scope.items);
  };

  function getChildren(items, index) {
    var children = [];
    var level = items[index].depth;
    index = index + 1;
    var depth = level + 1;
    for (; index < items.length && depth > level; ++index) {
      depth = items[index].depth;
      if (depth === level + 1) {
        children.push(index);
      }
    }
    return children;
  }

  function traverse(items, index) {
    if (index >= items.length) {
      return;
    }
    var item = items[index];
    var children = getChildren(items, index);
    var hidden = [];
    for (var i = 0; i < children.length; ++i) {
      traverse(items, children[i]);
      hidden.push(items[children[i]].$hidden);
    }
    item.$leaf = hidden.length === 0;
    item.$collapsed = _.all(hidden);
    item.$active = isActive(item);
    if (!item.$collapsed) {
      // Reveal all children of uncollapsed node
      for (i = 0; i < children.length; ++i) {
        items[children[i]].$hidden = false;
      }
    }
    item.$impHidden = false;
  }

  function hideNodeOrphans(items, index) {
    // If the parent is hidden, then the child is implicitly hidden
    var item = items[index];
    for (index++; index < items.length &&
      items[index].depth > item.depth; ++index) {
        if (!items[index].$hidden) {
          items[index].$impHidden = true;
        }
    }
  }

  function hideOrphans(items) {
    for (var i = 0; i < items.length; ++i) {
      if (items[i].$collapsed) {
        hideNodeOrphans(items, i);
      }
    }
  }

  function unCollapse(items, item) {
    item.$hidden = false;
    // Open up
    var parent = items[item.$parent];
    while (parent) {
      parent.$hidden = false;
      parent = items[parent.$parent];
    }
    // Open down one level
    var index = _.indexOf(items, item);
    if (index > 0) {
      var children = getChildren(items, index);
      _.each(children, function (child) {
        items[child].$hidden = false;
      });
    }
  }

  function isActive(item) {
    return !!item.active;
  }

  function updateModel(items, doUncollapse) {
    if (!items) {
      return;
    }
    doUncollapse = _.isUndefined(doUncollapse) ? true : doUncollapse;
    if (doUncollapse) {
      var active = _.find(items, function (item) {
        return isActive(item);
      });
      if (active) {
        unCollapse(items, active);
      }
    }
    traverse(items, 0);
    hideOrphans(items);
  }

  $scope.toggle = function (items, item, $event, state) {
    // if ($event) {
    //   $event.preventDefault();
    // }

    var index = _.indexOf(items, item);
    state = _.isUndefined(state) ? !item.$collapsed : state;
    if (index >= 0 && index < (items.length - 1)) {
      index = index + 1;
      while (index < items.length &&
        items[index].depth > item.depth) {
          if (items[index].depth === item.depth + 1) {
            items[index].$hidden = state;
          }
          index++;
        }
      }
      updateModel(items, false);
    };

    $scope.refresh();
});
