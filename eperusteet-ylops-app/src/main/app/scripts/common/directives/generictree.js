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
.directive('genericTreeNode', function($compile, $q, $templateCache, Notifikaatiot) {
    return {
        restrict: 'E',
        replace: true,
        template: '<span></span>',
        scope: {
            node: '=',
            treeProvider: '='
        },
        controller: function($scope) {
            $scope.treeProvider.extension($scope.node, $scope);
        },
        link: function(scope, element, attrs) {
            function setContext(node, children) {
                node.$$depth = node.$$depth || 0;
                _.each(children, function(cnode) {
                    cnode.$$depth = node.$$depth + 1;
                    cnode.$$nparent = node;
                });
            }

            scope.$watch('node', function(node) {
                scope.treeProvider.children(node)
                    .then(function(children) {
                        setContext(node, children);
                        var template = scope.treeProvider.template(node);
                        template = $templateCache.get(template) || template || '<pre>{{ node | json }}</pre>';
                        if (!_.isEmpty(children)) {
                            scope.tree = children;
                            template += '' +
                                '<div ng-repeat="node in tree">' +
                                '   <generic-tree-node node="node" tree-provider="treeProvider"></generic-tree-node>' +
                                '</div>';
                        }
                        element.append(template);
                        $compile(element.contents())(scope);
                    });
            });
        }
    };
})
.directive('genericTree', function($compile, $q, $templateCache, Notifikaatiot) {
    return {
        restrict: 'E',
        template: '<div></div>',
        replace: true,
        scope: {
            treeProvider: '=', // FIXME: Add interface
        },
        controller: function($scope) {
            function run(provider) {
                $scope.tprovider = provider;
                provider.root()
                    .then(provider.children)
                    .then(function(children) {
                        console.log(children);
                        $scope.tree = children;
                    })
                    .catch(Notifikaatiot.serverCb);
            }

            $scope.treeProvider
                .then(run)
                .catch(function(err) {
                    console.log(err);
                });
        },
        link: function(scope, element, attrs) {
            scope.$watch('tree', function(tree) {
                if (tree) {
                    // element.append('<pre>{{ tree | json }}</pre>');
                    element.append('' +
                        '<div ng-repeat="node in tree">' +
                        '   <generic-tree-node node="node" tree-provider="tprovider"></generic-tree-node>' +
                        '</div>');
                    $compile(element.contents())(scope);
                }
            }, true);
        }
    };
});
