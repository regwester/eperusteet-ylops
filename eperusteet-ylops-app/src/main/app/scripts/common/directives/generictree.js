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
/* global _ */

ylopsApp
.directive('genericTreeNode', function($compile, $templateCache) {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            node: '=',
            treeProvider: '=',
            uiSortableConfig: '='
        },
        controller: function($scope) {
            $scope.treeProvider.initNode($scope.node);
            $scope.treeProvider.extension($scope.node, $scope);
            $scope.isHidden = function(node) {
                return $scope.treeProvider.hidden(node);
            };
        },
        link: function(scope, element) {
            function setContext(node, children) {
                node.$$depth = node.$$depth || 0;
                node.$$hasChildren = !_.isEmpty(children);
                _.each(children, function(cnode) {
                    cnode.$$depth = node.$$depth + 1;
                    cnode.$$nodeParent = node;
                });
            }

            function getTemplate(node) {
                var template = scope.treeProvider.template ? scope.treeProvider.template(node) : undefined;
                return $templateCache.get(template) || template || '<pre>{{ node | json }}</pre>';
            }

            var node = scope.node;
            var children = scope.treeProvider.children(node);
            element.empty();
            setContext(node, node[children]);
            var template = '';
            template += getTemplate(node);
            if (node[children]) {
                template += '<div ui-sortable="uiSortableConfig" class="recursivetree" ng-model="node[children]">';
                scope.children = children;
                scope.parentNode = node;
                if (!_.isEmpty(node[children])) {
                    template += '' +
                        '<div ng-repeat="node in parentNode[children]">' +
                        '    <generic-tree-node node="node" ng-show="!isHidden(node)" ui-sortable-config="uiSortableConfig"' +
                        '                       tree-provider="treeProvider"></generic-tree-node>' +
                        '</div>';
                }
                template += '</div>';
            }
            element.append(template);
            $compile(element.contents())(scope);
        }
    };
})
.directive('genericTree', function($compile, $q, $templateCache, Notifikaatiot) {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            treeProvider: '=', // FIXME: Add interface
            uiSortableConfig: '=?'
        },
        controller: function($scope) {
            function run(provider) {
                $scope.tprovider = provider;
                $scope.tprovider.initNode = provider.initNode || _.noop;
                provider.root()
                    .then(function(root) {
                        $scope.children = root[provider.children()];
                    })
                    .catch(Notifikaatiot.serverCb);
            }

            $scope.treeProvider
                .then(run)
                .catch(function(err) {
                    console.log(err);
                });
        },
        link: function(scope, element) {
            function refresh(tree) {
                if (tree) {
                    scope.sortableConfig = _.merge({
                        connectWith: '.recursivetree',
                        handle: '.treehandle',
                        cursorAt: { top : 2, left: 2 },
                        cursor: 'move',
                        delay: 100,
                        disabled: scope.tprovider.useUiSortable(),
                        tolerance: 'pointer',
                        // placeholder: 'placeholder',
                        // // start: function(e, ui) {
                        // //     ui.placeholder.html('<div class="group-placeholder"></div>');
                        // // },
                        // // cancel: '.ui-state-disabled',
                        // // update: TreeDragAndDrop.update
                    }, scope.uiSortableConfig || {});

                    console.log('forcing redraw');
                    element.empty();
                    element.append('' +
                            '<div ui-sortable="sortableConfig" id="recursivetree" ng-model="children">' +
                            '    <div ng-repeat="node in children">' +
                            '       <generic-tree-node node="node" ui-sortable-config="sortableConfig" tree-provider="tprovider"></generic-tree-node>' +
                            '    </div>' +
                            '</div>');
                    $compile(element.contents())(scope);
                }
            }
            scope.$on('genericTree:refresh', refresh);
            scope.$watch('children', refresh, true);
        }
    };
});
