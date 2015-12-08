interface LukioKurssiTreeState {
    isEditMode: () => boolean
    defaultCollapse: boolean
}
interface LukioTreeUtilsI {
    templates: (state:LukioKurssiTreeState) => LukioTreeTemplatesI
    treeRootFromRakenne: (rakenne: Lukio.LukioOpetussuunnitelmaRakenneOps) => LukioKurssiTreeNode,
    defaultHidden: (node: LukioKurssiTreeNode) => boolean
    collapseToggler: (treeRootProvider: Provider<LukioKurssiTreeNode>, state: LukioKurssiTreeState) => (() => void),
    extensions: (myExtensions?: ((node:LukioKurssiTreeNode, scope:any) => void)) => ((node:LukioKurssiTreeNode, scope:any) => void)
    acceptMove: (from:LukioKurssiTreeNode, to:LukioKurssiTreeNode, event:any, ui:any) => boolean
    updatePagination: (arr:PaginationNode[], pagination:PaginationDetails) => void
}
interface Provider<T> {
    (): T
}
enum LukioKurssiTreeNodeType {
    root,
    kurssi,
    oppiaine
}
interface PaginationNode extends GenericTreeNode {
    $$hide?: boolean
    $$index?: number
    $$pagingShow?: boolean
}
interface LukioKurssiTreeNode extends PaginationNode {
    dtype: LukioKurssiTreeNodeType
    nimi?: l.Lokalisoitu //käytännössä not null (muilla kuin rootilla)
    id?: number, //käytännössä not null (muilla kuin rootilla)
    lapset?: LukioKurssiTreeNode[]
    $$nodeParent?: LukioKurssiTreeNode
    koosteinen?: boolean
    koodiArvo?: string
    lokalisoituKoodi?: l.Lokalisoitu
    kurssit?: Lukio.LukiokurssiOps[]
    oppimaarat?: Lukio.LukioOppiaine[]
}
interface LukioTreeTemplatesI {
    treeHandle: () => string
    kurssiColorbox: () => string
    timestamp: () => string
    collapse: () => string
    name: (node: LukioKurssiTreeNode) => string
    nodeTemplate: (node: LukioKurssiTreeNode) => string
    nodeTemplateKurssilista: (n:LukioKurssiTreeNode) => string
}
interface PaginationDetails {
    currentPage:number,
    showPerPage:number,
    total?:number,
    multiPage?:boolean,
    changePage:(to:number) => void
}

ylopsApp
    .service('LukioTreeUtils', function ($log, $state, $stateParams, Kaanna, Kieli) {
        var templatesByState = (state:LukioKurssiTreeState) => {
            var templateAround = (tmpl:string) => (!state.isEditMode() ? ' <a class="container-link' : '<span class="span-container')+
                    ' tree-list-item" '+(!state.isEditMode() ? ' ng-href="{{createHref()}}"': '')+
                    ' ng-show="!node.$$hide" ng-class="{ \'opetussisaltopuu-solmu-paataso\': (node.$$depth === 0), \'bubble\': dtypeString() != \'kurssi\',' +
                    '           \'bubble-osa\': dtypeString() == \'kurssi\',' +
                    '           \'empty-item\': !node.lapset.length }">'+tmpl +
                    (!state.isEditMode() ? ' </a>' : '</span>');
            var kurssiListaTemplateAround = (tmpl:string) => '<span class="span-container liittamaton-kurssi recursivetree tree-list-item bubble-osa empty-item"'
                    +' ng-show="!node.$$hide && node.$$pagingShow">'+tmpl + '</span>';
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
                        return '<span '
                            + (node.dtype != LukioKurssiTreeNodeType.kurssi ? ' title="' + base + '"' : '')
                            +'>' + base + '</span>';
                    }
                    return base;
                },
                collapse: () => !state.isEditMode() ? '<span ng-show="node.lapset.length" ng-click="toggle($event)"' +
                    '           class="colorbox collapse-toggle" ng-class="{\'suljettu\': node.$$collapsed}">' +
                    '    <span ng-hide="node.$$collapsed" class="glyphicon glyphicon-chevron-down"></span>' +
                    '    <span ng-show="node.$$collapsed" class="glyphicon glyphicon-chevron-right"></span>' +
                    '</span>' : '',
                nodeTemplate: (n:LukioKurssiTreeNode) => {
                    if (n.dtype == LukioKurssiTreeNodeType.kurssi) {
                        var remove = state.isEditMode() ? '   <span class="remove" icon-role="remove" ng-click="removeKurssiFromOppiaine(node)"></span>' : '';
                        return templateAround('<span class="span-container puu-node kurssi-node" ng-class="{\'liittamaton\': node.oppiaineet.length === 0}">' +
                            templates.treeHandle() + templates.kurssiColorbox() + templates.timestamp() +
                                '   <span class="span-container node-content left" ng-class="{ \'empty-node\': !node.lapset.length }">' +
                            templates.name(n) + '   </span>' + remove + '</span>');
                    } else {
                        return templateAround('<span class="span-container puu-node oppiaine-node">' + templates.treeHandle()
                            + templates.collapse() + templates.timestamp() + '<span class="span-container node-content left" ng-class="{ \'empty-node\': !node.lapset.length }">' +
                            '<strong>' + templates.name(n) + '</strong></span></span>');
                    }
                },
                nodeTemplateKurssilista: (n:LukioKurssiTreeNode) =>
                    kurssiListaTemplateAround('<span class="span-container puu-node kurssi-node">' +
                        templates.treeHandle() + templates.kurssiColorbox() + templates.timestamp() +
                        '   <span class="span-container node-content left">' + templates.name(n) + '</span>' +
                        '</span>')
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
                {n.$$collapsed = state.defaultCollapse;}));
        };
        var extensions = (myExtensions?: ((node:LukioKurssiTreeNode, scope:any) => void)) => {
            return (node:LukioKurssiTreeNode, scope:any) => {
                scope.toggle = ($event) => {
                    node.$$collapsed = !node.$$collapsed;
                    $event.preventDefault();
                    return false;
                };
                scope.createHref = () => {
                    if (node.dtype === LukioKurssiTreeNodeType.kurssi
                                && node.$$nodeParent) {
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
                scope.dtypeString = () => {
                    return LukioKurssiTreeNodeType[node.dtype];
                }
                if (myExtensions) {
                    myExtensions(node, scope);
                }
            };
        };
        var acceptMove = function(node: LukioKurssiTreeNode, to: LukioKurssiTreeNode, event, ui) {
            if (!node) {
                return false;
            }
            //Tarkistetaan, että onko kurssi jo kyseisen oppiaineen/oppimäärän kurssi, mikäli ei
            // siirretä oppiaineen/oppimäärän sisällä. Jos on jo, siirtoa ei sallita.
            if (node.dtype === LukioKurssiTreeNodeType.kurssi
                        && to.dtype === LukioKurssiTreeNodeType.oppiaine
                        && !to.koosteinen && ( _.isUndefined(node.$$nodeParent)
                            || node.$$nodeParent.id !== to.id)
                        && _.any(to.kurssit, kurssi => kurssi.id === node.id)) {
                return false;
            }
            return (node.dtype === LukioKurssiTreeNodeType.oppiaine
                            && to.dtype === LukioKurssiTreeNodeType.root
                            && !node.koosteinen) ||
                (node.dtype === LukioKurssiTreeNodeType.oppiaine
                    && to.dtype === LukioKurssiTreeNodeType.oppiaine
                    && to.koosteinen && !node.koosteinen) ||
                (node.dtype === LukioKurssiTreeNodeType.oppiaine
                    && to.dtype === LukioKurssiTreeNodeType.root ) ||
                (node.dtype === LukioKurssiTreeNodeType.kurssi
                    && to.dtype === LukioKurssiTreeNodeType.oppiaine
                    && !to.koosteinen);
        };

        var updatePagination = function (arr:PaginationNode[], pagination:PaginationDetails) {
            var i = 0, startIndex,
                endIndex;
            if (pagination) {
                startIndex = (pagination.currentPage-1) * pagination.showPerPage;
                endIndex = startIndex + pagination.showPerPage-1;
            }
            _.each(arr, (item:PaginationNode) => {
                item.$$index = i;
                item.$$pagingShow = true;
                if (pagination) {
                    item.$$pagingShow = item.$$index >= startIndex && item.$$index <= endIndex;
                }
                if (!item.$$hide) {
                    i++;
                }
            });
            if (!pagination.currentPage || pagination.currentPage < 0) {
                pagination.currentPage = 1;
            }
            if (!pagination.showPerPage || pagination.showPerPage < 0) {
                pagination.showPerPage = 10;
            }
            if (!pagination.changePage) {
                pagination.changePage = (pageNum:number) => changePage(arr, pagination, pageNum);
            }
            pagination.total = countNotHidden(arr);
            pagination.multiPage = pagination.total  > pagination.showPerPage;
            if (pagination.total > 0 && pagination.total <= (pagination.currentPage - 1) * pagination.showPerPage) {
                changePage(arr, pagination, pagination.currentPage - 1);
            }
        };
        var changePage = function(arr:PaginationNode[], pagination:PaginationDetails, to:number) {
            pagination.currentPage = to;
            updatePagination(arr, pagination);
        };
        var countNotHidden = function(arr:PaginationNode[]) {
            var count:number = 0;
            _.each(arr, (item:PaginationNode) => {
                if (!item.$$hide) {
                    count++;
                }
            });
            return count;
        };

        var textMatch = (txt:string[], to:string):boolean => {
            if (!to) {
                return true;
            }
            if (!txt.length) {
                return false;
            }
            var words = to.toLowerCase().split(/\s+/);
            var found = {};
            for (var part in txt) {
                if (!txt[part]) {
                    continue;
                }
                var lower = txt[part].toLowerCase();
                for (var i in words) {
                    if (words[i] && lower.indexOf(words[i]) !== -1) {
                        found[i] = true;
                    }
                }
            }
            for (var j = 0; j < words.length; ++j) {
                if (!found[j]) {
                    return false;
                }
            }
            return true;
        };
        var matchesSearch = (node: LukioKurssiTreeNode, search:string):boolean => {
            if (!search) {
                return true;
            }
            var nimi = node.nimi,
                koodiArvo = node.koodiArvo,
                lokalisoituKoodi = node.lokalisoituKoodi ? Kaanna.kaanna(node.lokalisoituKoodi) : null;
            if (_.isObject(node.nimi)) {
                nimi = nimi[Kieli.getSisaltokieli().toLowerCase()];
            }
            return textMatch([nimi, koodiArvo, lokalisoituKoodi], search);
        };
        return <LukioTreeUtilsI>{
            templates: templatesByState,
            treeRootFromRakenne: treeRootFromRakenne,
            defaultHidden: defaultHidden,
            collapseToggler: collapseToggler,
            extensions: extensions,
            acceptMove: acceptMove,
            matchesSearch: matchesSearch,
            updatePagination: updatePagination
        };
    });
