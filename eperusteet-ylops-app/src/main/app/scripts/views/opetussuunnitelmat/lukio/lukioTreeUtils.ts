interface LukioKurssiTreeState {
    isEditMode: () => boolean
    defaultCollapse: boolean,
    scope: angular.IScope,
    root(): LukioKurssiTreeNode
    liitetytKurssit(): LukioKurssiTreeNode[]
    liittamattomatKurssit(): LukioKurssiTreeNode[]
    updatePagination(): void
}
interface LukioTreeUtilsI {
    templates: (state:LukioKurssiTreeState) => LukioTreeTemplatesI
    treeRootLapsetFromRakenne : (rakenne: Lukio.LukioOpetussuunnitelmaRakenneOps) => LukioKurssiTreeNode[],
    defaultHidden(usePagination: boolean): (node: LukioKurssiTreeNode) => boolean,
    defaultCollapsed: (node: LukioKurssiTreeNode) => boolean,
    updateCollapse: (state: LukioKurssiTreeState) => void,
    collapseToggler: (treeRootProvider: Provider<LukioKurssiTreeNode>, state: LukioKurssiTreeState) => (() => void),
    extensions: (state: LukioKurssiTreeState,
                 myExtensions?: ((node:LukioKurssiTreeNode, scope:any) => void)) => ((node:LukioKurssiTreeNode, scope:any) => void)
    acceptDropWrapper: (state: LukioKurssiTreeState) =>
        (node: LukioKurssiTreeNode, to: LukioKurssiTreeNode)  => boolean
    updatePagination: (arr:PaginationNode[], pagination:PaginationDetails) => void
    hideBySearch: (state:LukioKurssiTreeState, search:string, root?: LukioKurssiTreeNode) => void
    luoHaku: (state:LukioKurssiTreeState, root?: LukioKurssiTreeNode) => Rajaus
}
interface Rajaus {
    haku: string
    hae(): void
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
    oppiaineet?: Lukio.LukioOppiaineJarjestys[]
    lokalisoituKoodi?: l.Lokalisoitu
    kurssit?: Lukio.LukiokurssiOps[]
    oppimaarat?: Lukio.LukioOppiaine[]
    jarjestys?: number
    oppiaineId?: number
    tyyppi?: string
    muokattu?: number
    paginationUsed?: boolean
}
interface LukioTreeTemplatesI {
    treeHandle: (node: LukioKurssiTreeNode) => JQuery
    kurssiColorbox: (node: LukioKurssiTreeNode) => JQuery
    timestamp: (node: LukioKurssiTreeNode) => JQuery
    collapse: (node: LukioKurssiTreeNode) => JQuery
    name: (node: LukioKurssiTreeNode) => JQuery
    nodeTemplate: (node: LukioKurssiTreeNode) => JQuery
    nodeTemplateKurssilista: (n:LukioKurssiTreeNode) => JQuery
}
interface PaginationDetails {
    currentPage:number,
    showPerPage:number,
    total?:number,
    multiPage?:boolean,
    changePage?: (to:number) => void,
    state: LukioKurssiTreeState
}

ylopsApp
    .service('LukioTreeUtils', function ($log, $state, $stateParams, Kaanna, Kieli, $timeout, $rootScope, $filter) {
        function toggleNode(node, $event, state?: LukioKurssiTreeState) {
            node.$$collapsed = !node.$$collapsed;
            $event.preventDefault();
            if (state) {
                state.scope.$broadcast('genericTree:updateNode', $event.target);
            }
            return false;
        }
        function createHref(node) {
            if (node.dtype === LukioKurssiTreeNodeType.kurssi && node.$$nodeParent) {
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
        }
        function removeKurssiFromOppiaine(state:LukioKurssiTreeState, node:LukioKurssiTreeNode) {
            $rootScope.$broadcast('genericTree:beforeChange');
            _.remove(node.$$nodeParent.lapset, n => n.id === node.id);
            var oppiaine = node.$$nodeParent;
            _.remove(oppiaine.lapset, node);
            var kurssitInTree = _(state.root()).flattenTree(n => n.lapset)
                .filter(c => c.dtype == LukioKurssiTreeNodeType.kurssi && c.id == node.id).value(),
                foundInTree = !_.isEmpty(kurssitInTree),
                inLiittamattomat = _.filter(state.liittamattomatKurssit(), n => n.id == node.id);
            if (!foundInTree && _.isEmpty(inLiittamattomat)) {
                state.liittamattomatKurssit().push(node);
                _.remove(state.liitetytKurssit(), n => n.id == node.id);
            }
            state.updatePagination();
            $timeout(() => {
                $rootScope.$broadcast('genericTree:afterChange');
            });
        }

        var templatesByState = (state:LukioKurssiTreeState) => {
            var templateAround = (tmpl:JQuery, node) => {
                var classes = 'tree-list-item ' + (node.$$depth === 0 ? 'opetussisaltopuu-solmu-paataso ':'')
                        +(node.dtype != LukioKurssiTreeNodeType.kurssi ? 'bubble ':'')
                        +(node.dtype == LukioKurssiTreeNodeType.kurssi ? 'bubble-osa ':'')
                        +(node.lapset.length ? 'empty-item ':''),
                    $el:JQuery;
                if (state.isEditMode()) {
                    classes += 'span-container';
                    $el = $('<span class="'+classes+'"></span>');
                } else {
                    classes += 'container-link';
                    $el = $('<a class="'+classes+'"></a>').attr('href', createHref(node));
                }
                return $el.append(tmpl);
            };
            var kurssiListaTemplateAround = (tmpl:JQuery, node) => $('<span class="span-container liittamaton-kurssi recursivetree tree-list-item bubble-osa empty-item"></span>')
                    .append(tmpl);
            var templates = <LukioTreeTemplatesI>{
                treeHandle: node => {
                    return state.isEditMode() ? $('<span icon-role="drag" class="treehandle"><span class="glyphicon glyphicon-resize-vertical"></span></span>') : null;
                },
                kurssiColorbox: node => $('<span class="colorbox kurssi-tyyppi ' + node.tyyppi.toLowerCase() + '"></span>'),
                timestamp: node => !state.isEditMode() ? $('<span class="aikaleima"></span>')
                    .attr('title', $filter('kaanna')('muokattu') + ' ' + $filter('aikaleima')(node.muokattu || 0))
                    .text($filter('aikaleima')(node.muokattu || 0)) : null,
                name: node => {
                    var base;
                    if (node.dtype == LukioKurssiTreeNodeType.kurssi) {
                        var lokalisoituKoodi = $filter('kaanna')(node.lokalisoituKoodi),
                            name = $filter('kaanna')(node.nimi) + ' ' + (lokalisoituKoodi ? ' ('+lokalisoituKoodi+')' : '');
                        base = $('<span title="'+name+'">'+name+'</span>');
                    } else {
                        base = $('<span></span>').text($filter('kaanna')(node.nimi));
                    }
                    return base;
                },
                collapse: (n:LukioKurssiTreeNode) =>
                        ((!state.isEditMode() || n.dtype == LukioKurssiTreeNodeType.oppiaine) && n.lapset.length)
                        ?  $('<span class="colorbox collapse-toggle"></span>')
                            .append($('<span ng-hide="node.$$collapsed" class="glyphicon collapse-based"></span>')
                                    .addClass(n.$$collapsed ? 'glyphicon-chevron-right' : 'glyphicon-chevron-down'))
                                    .attr('data-uncollapse-class', 'glyphicon-chevron-down')
                                    .attr('data-collapse-class', 'glyphicon-chevron-right suljettu')
                            .addClass(n.$$collapsed ? 'suljettu': '')
                            .click(e => toggleNode(n, e, state)) : null,
                nodeTemplate: (n:LukioKurssiTreeNode) => {
                    if (n.dtype == LukioKurssiTreeNodeType.kurssi) {
                        var remove = state.isEditMode() ? $('<span class="remove" icon-role="remove"><span class="glyphicon glyphicon-remove"></span></span>')
                                .click(e => removeKurssiFromOppiaine(state, n)) : null;
                        return templateAround($('<span class="span-container puu-node kurssi-node"></span>')
                                .addClass(_.isEmpty(n.oppiaineet) ? 'liittamaton' : '')
                                .append(templates.treeHandle(n))
                                .append(templates.kurssiColorbox(n))
                                .append(templates.timestamp(n))
                                .append($('<span class="span-container node-content left">')
                                        .addClass(_.isEmpty(n.lapset) ? 'empty-node' : '')
                                        .append(templates.name(n))
                                ).append(remove),n);
                    } else {
                        return templateAround($('<span class="span-container puu-node oppiaine-node">')
                                .append(templates.treeHandle(n))
                                .append(templates.collapse(n))
                                .append(templates.timestamp(n))
                                .append($('<span class="span-container node-content left"></span>')
                                    .addClass(_.isEmpty(n.lapset) ? 'empty-node' : '')
                                    .append($('<strong></strong>')
                                        .append(templates.name(n)))
                                ),n);
                    }
                },
                nodeTemplateKurssilista: (n:LukioKurssiTreeNode) =>
                    kurssiListaTemplateAround($('<span class="span-container puu-node kurssi-node"></span>')
                            .append(templates.treeHandle(n))
                            .append(templates.kurssiColorbox(n))
                            .append(templates.timestamp(n))
                            .append($('<span class="span-container node-content left"></span>')
                                .append(templates.name(n))
                            ),n)
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
        var treeRootLapsetFromRakenne = (rakenne: Lukio.LukioOpetussuunnitelmaRakenneOps) =>
            transformOppiaineToTreeNodes(rakenne.oppiaineet);
        var defaultHidden = (usePagination) => (node: LukioKurssiTreeNode) => !node || node.$$hide || (node.$$nodeParent && node.$$nodeParent.$$collapsed)
                            || (usePagination && !node.$$pagingShow);
        var defaultCollapsed = (node: LukioKurssiTreeNode) => node && (node.$$collapsed || (node.$$nodeParent && node.$$nodeParent.$$collapsed))
                    && !(node.dtype == LukioKurssiTreeNodeType.oppiaine && _.isEmpty(node.lapset));
        var doSetCollapse = (root: LukioKurssiTreeNode, stateSetter: boolean|((n:LukioKurssiTreeNode)=> boolean)) => {
            _.each(_(root).flattenTree(n => n.lapset).value(), ((n:LukioKurssiTreeNode) =>
                {n.$$collapsed = <boolean>(stateSetter instanceof Function ? stateSetter(n) : stateSetter);}));
        };
        var updateCollapse = (state: LukioKurssiTreeState) => {
            doSetCollapse(state.root(),
                n => state.isEditMode() ?
                    (state.defaultCollapse ?
                        !(n.dtype == LukioKurssiTreeNodeType.oppiaine && n.koosteinen)
                        : false)
                    : state.defaultCollapse);
            state.scope.$broadcast('genericTree:updateNode');
        };
        var collapseToggler = (treeRootProvider: Provider<LukioKurssiTreeNode>, state: LukioKurssiTreeState) => () => {
            state.defaultCollapse = !state.defaultCollapse;
            updateCollapse(state);
        };
        var extensions = (state: LukioKurssiTreeState, myExtensions?: ((node:LukioKurssiTreeNode, scope:any) => void)) => {
            return (node:LukioKurssiTreeNode, scope:any) => {
                scope.toggle = ($event) => {
                    return toggleNode(node, $event);
                };
                scope.createHref = () => {
                    return createHref(node);
                };
                scope.removeKurssiFromOppiaine = (node:LukioKurssiTreeNode) => {
                    return removeKurssiFromOppiaine(state, node);
                };
                scope.dtypeString = () => {
                    return LukioKurssiTreeNodeType[node.dtype];
                };
                if (myExtensions) {
                    myExtensions(node, scope);
                }
            };
        };
        var acceptMove = function(node: LukioKurssiTreeNode, to: LukioKurssiTreeNode) {
            if (!node) {
                return false;
            }
            //Tarkistetaan, että onko kurssi jo kyseisen oppiaineen/oppimäärän kurssi, mikäli ei
            // siirretä oppiaineen/oppimäärän sisällä. Jos on jo, siirtoa ei sallita.
            if (node.dtype === LukioKurssiTreeNodeType.kurssi
                    && to.dtype === LukioKurssiTreeNodeType.oppiaine
                    && !to.koosteinen && (!node.$$nodeParent
                        || node.$$nodeParent.id !== to.id)
                    && _.any(to.lapset, kurssi => kurssi.dtype == LukioKurssiTreeNodeType.kurssi
                    && kurssi.id === node.id)) {
                return false;
            }
            //$log.info('acccept', _.cloneDeep(node), _.cloneDeep(to));
            return (node.dtype === LukioKurssiTreeNodeType.oppiaine
                            && to.dtype === LukioKurssiTreeNodeType.root
                            && !node.oppiaineId) ||
                (node.dtype === LukioKurssiTreeNodeType.oppiaine
                    && to.dtype === LukioKurssiTreeNodeType.oppiaine
                    && to.koosteinen && !node.koosteinen && node.$$nodeParent
                    && to.id == node.$$nodeParent.id) ||
                (node.dtype === LukioKurssiTreeNodeType.kurssi
                    && to.dtype === LukioKurssiTreeNodeType.oppiaine
                    && !to.koosteinen);
        };
        var moved = (state: LukioKurssiTreeState, node: LukioKurssiTreeNode, to: LukioKurssiTreeNode) => {
            if (node.dtype == LukioKurssiTreeNodeType.kurssi  && to.dtype == LukioKurssiTreeNodeType.oppiaine) {
                var from = node.$$nodeParent;
                if (!from) {
                    state.liitetytKurssit().push(_.cloneDeep(node));
                }
            }
            state.updatePagination();
        };
        var moveWrapper = (state: LukioKurssiTreeState) =>
                    (node: LukioKurssiTreeNode, to: LukioKurssiTreeNode) => {
            var accepted = acceptMove(node, to);
            if (accepted) {
                moved(state, node, to);
            }
            return accepted;
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
            } else {
                pagination.state.scope.$broadcast('genericTree:updateVisibilityGiven', function(root:LukioKurssiTreeNode) {
                    return root.paginationUsed;
                });
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
        var hideBySearch = (state:LukioKurssiTreeState, search:string, root?: LukioKurssiTreeNode) => {
            root = root || state.root();
            _.each(_(root).flattenTree(n => n.lapset)
                    .filter(n => n.dtype != LukioKurssiTreeNodeType.root).value(),
                (n:LukioKurssiTreeNode) => {
                    n.$$hide = !matchesSearch(n, search);
                    if (!n.$$hide) {
                        // Ensure also parents shown:
                        _.each(_.flattenTree(n, node => node.$$nodeParent), (node:LukioKurssiTreeNode) => {
                            node.$$hide = false;
                        });
                    }
                });
            state.updatePagination();
            if (!root.paginationUsed) {
                state.scope.$broadcast('genericTree:refreshVisibility');
            }
        };
        var luoHaku = (state:LukioKurssiTreeState, root?: LukioKurssiTreeNode) => {
            var haku = <Rajaus>{
                haku: '',
                hae: () => {}
            };
            haku.hae = () => $timeout(() => {
                hideBySearch(state, haku.haku, root);
            });
            return haku;
        };

        return <LukioTreeUtilsI>{
            templates: templatesByState,
            treeRootLapsetFromRakenne : treeRootLapsetFromRakenne ,
            defaultHidden: defaultHidden,
            defaultCollapsed: defaultCollapsed,
            collapseToggler: collapseToggler,
            updateCollapse: updateCollapse,
            extensions: extensions,
            acceptDropWrapper: moveWrapper,
            matchesSearch: matchesSearch,
            hideBySearch: hideBySearch,
            updatePagination: updatePagination,
            luoHaku: luoHaku
        };
    });
