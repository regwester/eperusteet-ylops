interface LukioKurssiTreeState {
    isEditMode: () => boolean
    defaultCollapse: boolean
}
interface LukioTreeUtilsI {
    templates: (state:LukioKurssiTreeState) => LukioTreeTemplatesI
    treeRootFromRakenne: (rakenne: Lukio.LukioOpetussuunnitelmaRakenneOps) => LukioKurssiTreeNode,
    defaultHidden: (node: LukioKurssiTreeNode) => boolean
    collapseToggler: (treeRootProvider: Provider<LukioKurssiTreeNode>, state: LukioKurssiTreeState) => (() => void),
    extensions: (myExtensions?: ((node:any, scope:any) => void)) => ((node:any, scope:any) => void)
}
interface Provider<T> {
    (): T
}
enum LukioKurssiTreeNodeType {
    root,
    kurssi,
    oppiaine
}
interface LukioKurssiTreeNode extends GenericTreeNode {
    id?: number,
    dtype: LukioKurssiTreeNodeType
    lapset?: LukioKurssiTreeNode[]
    $$nodeParent?: LukioKurssiTreeNode
    $$hide?: boolean
}
interface LukioTreeTemplatesI {
    treeHandle: () => string
    kurssiColorbox: () => string
    timestamp: () => string
    collapse: () => string
    name: (node: LukioKurssiTreeNode) => string
    nodeTemplate: (node: LukioKurssiTreeNode) => string
}

ylopsApp
    .service('LukioTreeUtils', function ($log, $state, $stateParams) {
        var templateAround = function(tmpl:string):string {
            return '<div class="tree-list-item" ng-show="!node.$$hide" ' +
                'ng-class="{ \'opetussialtopuu-solmu-paataso\': (node.$$depth === 0), \'bubble\': node.dtype != \'kurssi\',' +
                '           \'bubble-osa\': node.dtype === \'kurssi\',' +
                '           \'empty-item\': !node.lapset.length }">'+tmpl+'</div>';
        };

        var templatesByState = (state:LukioKurssiTreeState) => {
            var templates = <LukioTreeTemplatesI>{
                treeHandle: () => state.isEditMode() ? '<span icon-role="drag" class="treehandle"></span>' : '',
                kurssiColorbox: () => '  <span class="colorbox kurssi-tyyppi {{node.tyyppi.toLowerCase()}}"></span>',
                timestamp: () => !state.isEditMode() ? '<span class="aikaleima" ng-bind="node.muokattu || 0 | ' +
                    'aikaleima: \'ago\'" title="{{\'muokattu\' | kaanna }} {{node.muokattu || 0 | aikaleima}}"></span>' : '',
                name: node => {
                    var base = node.dtype == LukioKurssiTreeNodeType.kurssi
                        ? '<span ng-bind="(node.nimi | kaanna) + ((node.lokalisoituKoodi | kaanna) ? \' (\'+(node.lokalisoituKoodi | kaanna)+\')\' : \'\')" title="{{node.nimi | kaanna}} {{(node.lokalisoituKoodi | kaanna) ? \'(\'+(node.lokalisoituKoodi | kaanna)+\')\' : \'\'}}"></span>'
                        : '{{ node.nimi | kaanna }}';
                    if (!state.isEditMode()) {
                        return '<a ng-href="{{createHref(node)}}" '
                            + (node.dtype != LukioKurssiTreeNodeType.kurssi ? ' title="' + base + '"' : '')
                            +'>' + base + '</a>';
                    }
                    return base;
                },
                collapse: () => !state.isEditMode() ? '<span ng-show="node.lapset.length" ng-click="toggle(node)"' +
                    '           class="colorbox collapse-toggle" ng-class="{\'suljettu\': node.$$collapsed}">' +
                    '    <span ng-hide="node.$$collapsed" class="glyphicon glyphicon-chevron-down"></span>' +
                    '    <span ng-show="node.$$collapsed" class="glyphicon glyphicon-chevron-right"></span>' +
                    '</span>' : '',
                nodeTemplate: (n:LukioKurssiTreeNode) => {
                    if (n.dtype == LukioKurssiTreeNodeType.kurssi) {
                        var remove = state.isEditMode() ? '   <span class="remove" icon-role="remove" ng-click="removeKurssiFromOppiaine(node)"></span>' : '';
                        return templateAround('<div class="puu-node kurssi-node" ng-class="{\'liittamaton\': node.oppiaineet.length === 0}">' +
                            templates.treeHandle() + templates.kurssiColorbox() + templates.timestamp() +
                            '   <div class="node-content left" ng-class="{ \'empty-node\': !node.lapset.length }">' +
                            templates.name(n) + '   </div>' + remove +
                            '</div>');
                    } else {
                        return templateAround('<div class="puu-node oppiaine-node">' + templates.treeHandle()
                            + templates.collapse() + templates.timestamp() + '<div class="node-content left" ng-class="{ \'empty-node\': !node.lapset.length }">' +
                            '<strong>' + templates.name(n) + '</strong></div></div>');
                    }
                }
            };
            return templates;
        };

        var transformKurssitToTreeNodes = (ks: Lukio.LukiokurssiOps[]): LukioKurssiTreeNode[] => {
            var nodes = [];
            _.each(ks, (k: Lukio.LukiokurssiOps) => {
                var node = _.clone(k);
                node.dtype = LukioKurssiTreeNodeType.kurssi;
                node.lapset = [];
                nodes.push(node);
            });
            return nodes;
        };
        var transformOppiaineToTreeNodes = (oas: Lukio.LukioOppiaine[]): LukioKurssiTreeNode[] => {
            var nodes = [];
            _.each(oas, (oa: Lukio.LukioOppiaine) => {
                var node = _.clone(oa);
                node.dtype = LukioKurssiTreeNodeType.oppiaine;
                node.lapset = _.union(
                    transformOppiaineToTreeNodes(oa.oppimaarat || []),
                    transformKurssitToTreeNodes(oa.kurssit || [])
                );
                nodes.push(node);
            });
            return nodes;
        };
        var treeRootFromRakenne = (rakenne: Lukio.LukioOpetussuunnitelmaRakenneOps) =>
            <LukioKurssiTreeNode>{
                dtype: LukioKurssiTreeNodeType.root,
                lapset: transformOppiaineToTreeNodes(rakenne.oppiaineet)
            };
        var defaultHidden = (node: LukioKurssiTreeNode) => !node || node.$$hide || (node.$$nodeParent && node.$$nodeParent.$$collapsed);
        var collapseToggler = (treeRootProvider: Provider<LukioKurssiTreeNode>, state: LukioKurssiTreeState) => () => {
            state.defaultCollapse = !state.defaultCollapse;
            _.each(_(treeRootProvider()).flattenTree(_.property('lapset')).value(), ((n:LukioKurssiTreeNode) =>
                n.$$collapsed = state.defaultCollapse));
        };
        var extensions = (myExtensions?: ((node:any, scope:any) => void)) => {
            return (node:any, scope:any) => {
                scope.toggle = (node: LukioKurssiTreeNode) => node.$$collapsed = !node.$$collapsed;;
                scope.createHref = (node: LukioKurssiTreeNode) => {
                    if (node.dtype === LukioKurssiTreeNodeType.kurssi) {
                        return $state.href('root.opetussuunnitelmat.lukio.opetus.kurssi', {
                            id: $stateParams.id,
                            oppiaineId: node.$$nodeParent.id,
                            kurssiId: node.id
                        });
                    } else if(node.dtype === LukioKurssiTreeNodeType.oppiaine) {
                        return $state.href('root.opetussuunnitelmat.lukio.opetus.oppiaine', {
                            id: $stateParams.id,
                            oppiaineId: node.id
                        });
                    }
                    return null;
                };
                if (myExtensions) {
                    myExtensions(node, scope);
                }
            };
        };

        return <LukioTreeUtilsI>{
            templates: templatesByState,
            treeRootFromRakenne: treeRootFromRakenne,
            defaultHidden: defaultHidden,
            collapseToggler: collapseToggler,
            extensions: extensions
        };
    });
