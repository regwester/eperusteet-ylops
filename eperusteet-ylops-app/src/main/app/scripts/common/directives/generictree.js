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
        template: '',
        scope: {
            node: '=',
            treeProvider: '=',
            uiSortableConfig: '='
        },
        controller: function($scope) {
            $scope.treeProvider.extension($scope.node, $scope);
            $scope.isHidden = function(node) {
                return $scope.treeProvider.hidden(node);
            };
        },
        link: function(scope, element) {
            function setContext(node, children) {
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
            scope.treeProvider.children(node)
                .then(function(children) {
                    element.empty();
                    setContext(node, children);
                    var template = '';
                    template += getTemplate(node);
                    if (children) {
                        template += '<div ui-sortable="uiSortableConfig" class="recursivetree" ng-model="children">';
                        scope.children = children;
                        scope.parentNode = node;
                        if (!_.isEmpty(children)) {
                            template += '' +
                                '<div ng-repeat="node in children">' +
                                '    <generic-tree-node node="node" ng-show="!isHidden(node)" ui-sortable-config="uiSortableConfig"' +
                                '                       tree-provider="treeProvider"></generic-tree-node>' +
                                '</div>';
                        }
                        template += '</div>';
                    }
                    element.append(template);
                    $compile(element.contents())(scope);
                });
        }
    };
})
.directive('genericTree', function($compile, $q, $log, $templateCache, Notifikaatiot) {
    return {
        restrict: 'E',
        replace: true,
        template: '',
        scope: {
            treeProvider: '=', // FIXME: Add interface
            uiSortableConfig: '=?'
        },
        controller: function($scope) {
            function run(provider) {
                $scope.tprovider = provider;
                provider.root()
                    .then(provider.children)
                    .then(function(children) {
                        $scope.children = children;
                    })
                    .catch(Notifikaatiot.serverCb);
            }

            $scope.treeProvider
                .then(run)
                .catch(function(err) {
                    $log.error(err);
                });
        },
        link: function(scope, element) {
            function refresh(tree) {
                if (tree) {
                    _.each(scope.children, function(child) {
                        child.$$nodeParent = undefined;
                        child.$$depth = 0;
                    });

                    scope.sortableConfig = _.merge({
                        connectWith: '.recursivetree',
                        handle: '.treehandle',
                        cursorAt: { top : 2, left: 2 },
                        helper: 'clone',
                        option: 'x',
                        cursor: 'move',
                        delay: 100,
                        disabled: scope.tprovider.useUiSortable(),
                        tolerance: 'pointer',
                    }, scope.uiSortableConfig || {});

                    element.empty();
                    element.append('' +
                            '<div ui-sortable="sortableConfig" class="recursivetree" ng-model="children">' +
                            '    <div ng-repeat="node in children">' +
                            '       <generic-tree-node node="node" ui-sortable-config="sortableConfig" tree-provider="tprovider"></generic-tree-node>' +
                            '    </div>' +
                            '</div>');
                    $compile(element.contents())(scope);
                }
            }

            scope.$on('genericTree:refresh', function() {
                refresh(scope.children, scope.children);
            });
            scope.$watch('children', refresh, true);
        }
    };
});