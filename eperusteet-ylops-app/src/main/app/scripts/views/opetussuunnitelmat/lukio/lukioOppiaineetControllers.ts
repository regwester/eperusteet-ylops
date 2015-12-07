
interface KoodistoArvo {
    koodiArvo: string
    koodiUri: string
    nimi: l.Lokalisoitu
}

ylopsApp
    .controller('LukioOppiaineetController', function($scope, $q:IQService, $stateParams, Kaanna, $log,
                                                      LukioOpetussuunnitelmaService: LukioOpetussuunnitelmaServiceI,
                                                      LukioTreeUtils: LukioTreeUtilsI, Kommentit,
                                                      Editointikontrollit, $state, $timeout) {
        $scope.editMode = false;
        var state:LukioKurssiTreeState = {
            isEditMode: () => $scope.editMode,
            defaultCollapse: false
        };
        $scope.sortableConfig = <SortableConfig> {
            placeholder: 'placeholder',
            handle: '.treehandle',
            cursorAt: { top : 5, left: 5 }
        };
        $scope.sortableLiittamattomatConfig = <SortableConfig> _.cloneDeep($scope.sortableConfig);
        $scope.sortableLiitetytConfig = <SortableConfig> _.cloneDeep($scope.sortableConfig);
        $scope.toggleCollapse = LukioTreeUtils.collapseToggler(() => $scope.treeRoot, state);
        $scope.liittamattomatRoot = <LukioKurssiTreeNode>{
            dtype: LukioKurssiTreeNodeType.root,
            lapset: [] // should be none by default
        };
        $scope.liitetytRoot = <LukioKurssiTreeNode>{
            dtype: LukioKurssiTreeNodeType.root,
            lapset: []
        };
        var updatePagination = () => {
            LukioTreeUtils.updatePagination($scope.liittamattomatRoot.lapset, $scope.liittamattomatPagination);
            LukioTreeUtils.updatePagination($scope.liitetytRoot.lapset, $scope.liitetytPagination);
        };
        $scope.treeProvider = $q.when(<GenericTreeConfig<LukioKurssiTreeNode>>{
            root: () => {
                var d = $q.defer<LukioKurssiTreeNode>();
                LukioOpetussuunnitelmaService.getRakenne().then(rakenne => {
                    $scope.treeRoot = LukioTreeUtils.treeRootFromRakenne(rakenne);
                    $log.info('resolved root', $scope.treeRoot);
                    $scope.liitetytRoot.lapset.length = 0; // empty the Array (but maintain object reference)
                    _.each(_($scope.treeRoot).flattenTree(_.property('lapset'))
                            .filter((n:LukioKurssiTreeNode) => n.dtype == LukioKurssiTreeNodeType.kurssi)
                            .uniq((n:LukioKurssiTreeNode) => n.id).value(),
                        (k: LukioKurssiTreeNode) => {$scope.liitetytRoot.lapset.push(k);}
                    );
                    updatePagination();
                    d.resolve($scope.treeRoot);
                });
                return d.promise;
            },
            children: node => $q.when(node.lapset || []),
            useUiSortable: () => !state.isEditMode(),
            hidden: LukioTreeUtils.defaultHidden,
            template: node => LukioTreeUtils.templates(state).nodeTemplate(node),
            extension: LukioTreeUtils.extensions((n, scope) => {
            }),
            acceptDrop: LukioTreeUtils.acceptMove,
            sortableClass: _.constant('is-draggable-into')
        });

        $scope.liittamattomatPagination = <PaginationDetails>{showPerPage: 5, currentPage: 1};
        $scope.liittamattomatTreeProvider = $q.when(<GenericTreeConfig<LukioKurssiTreeNode>>{
            root: () => $q.when($scope.liittamattomatRoot),
            children: node => $q.when(node.lapset || []),
            useUiSortable: () => !state.isEditMode(),
            hidden: LukioTreeUtils.defaultHidden,
            template: node => LukioTreeUtils.templates(state).nodeTemplateKurssilista(node),
            extension: LukioTreeUtils.extensions((n, scope) => {
            }),
            acceptDrop: LukioTreeUtils.acceptMove,
            sortableClass: _.constant('is-draggable-into')
        });

        $scope.liitetytPagination = <PaginationDetails>{showPerPage: 5, currentPage: 1};
        $scope.liitetytTreeProvider = $q.when(<GenericTreeConfig<LukioKurssiTreeNode>>{
            root: () => $q.when($scope.liitetytRoot),
            children: node => $q.when(node.lapset || []),
            hidden: LukioTreeUtils.defaultHidden,
            useUiSortable: () => !state.isEditMode(),
            template: node => LukioTreeUtils.templates(state).nodeTemplateKurssilista(node),
            extension: LukioTreeUtils.extensions((n, scope) => {
            }),
            acceptDrop: LukioTreeUtils.acceptMove,
            sortableClass: _.constant('is-draggable-into')
        });

        $scope.addOppiaine = function() {
            $state.go('root.opetussuunnitelmat.lukio.opetus.uusioppiaine');
        };

        Editointikontrollit.registerCallback({
            validate: function() {
                return true;
            },
            edit: function() {
            },
            cancel: function() {
                $scope.editMode = false;
                $scope.$broadcast('genericTree:refresh'); // templates get updated
            },
            save: function(kommentti) {
            }
        });

        $scope.toEditMode = () => {
            $scope.editMode = true;
            $scope.$broadcast('genericTree:refresh'); // templates get updated
            Editointikontrollit.startEditing();
        };

        // TODO:kommentit (ei nyt rakenteelle omaa käsitettä)
    })


    .service('LukioControllerHelpers', function($rootScope, Koodisto, MuokkausUtils, Kieli) {
        var openKoodisto = (obj:any, koodisto:string, and?: (arvoKoodistoArvo) => void) => () => {
            Koodisto.modaali((koodisto: KoodistoArvo) => {
                MuokkausUtils.nestedSet(obj, 'koodiUri', ',', koodisto.koodiUri);
                MuokkausUtils.nestedSet(obj, 'koodiArvo', ',', koodisto.koodiArvo);
                MuokkausUtils.nestedSet(obj, 'nimi', ',', koodisto.nimi);
                if (and) {
                    and(koodisto);
                }
                $rootScope.$broadcast('notifyCKEditor');
            }, {
                tyyppi: function () {
                    return koodisto;
                },
                ylarelaatioTyyppi: function () {
                    return '';
                }
            })();
        };
        return {
            openOppiaineKoodisto: (obj:Lukio.Oppiaine) => openKoodisto(obj, 'oppiaineetyleissivistava2'),
            openKurssiKoodisto: (obj:Lukio.LukiokurssiOps) => openKoodisto(obj, 'lukionkurssit',
                    koodi => {
                        obj.lokalisoituKoodi = {};
                        obj.lokalisoituKoodi[Kieli.getSisaltokieli()] = koodi.koodiArvo;
                    })
        };
    })
    .controller('LukioOppiaineController', function($scope, $q:IQService, $stateParams, $state,
                        LukioOpetussuunnitelmaService: LukioOpetussuunnitelmaServiceI, Kaanna, $log,
                        Editointikontrollit, LukioControllerHelpers) {
        // TODO:
        $scope.oppiaine = null;
        $scope.editMode = false;
        LukioOpetussuunnitelmaService.getOppiaine($stateParams.oppiaineId).then(oa => $scope.oppiaine = oa);

        Editointikontrollit.registerCallback({
            validate: function() {
                return true;
            },
            edit: function() {
            },
            cancel: function() {
                $scope.editMode = false;
            },
            save: function(kommentti) {

            }
        });
        $scope.openKoodisto = LukioControllerHelpers.openOppiaineKoodisto($scope.oppiaine);

        $scope.toEditMode = () => {
            $scope.editMode = true;
            Editointikontrollit.startEditing();
        };
    })
    .controller('LuoLukioOppiaineController', function($scope, $q:IQService, $stateParams, $state,
                       LukioOpetussuunnitelmaService: LukioOpetussuunnitelmaServiceI, Kaanna, $log,
                       Editointikontrollit, LukioControllerHelpers) {
        $scope.oppiaine = <Lukio.LukioOppiaineTallennus>{
            nimi: {},
            kuvaus: {},
            koosteinen: false,
            kurssiTyyppiKuvaukset: {}
        };
        $scope.editMode = true;
        Editointikontrollit.registerCallback({
            validate: function() {
                return true;
            },
            edit: function() {
            },
            cancel: function() {
                $state.go('root.opetussuunnitelmat.lukio.opetus.oppiaineet');
            },
            save: function(kommentti) {
                LukioOpetussuunnitelmaService.saveOppiaine($scope.oppiaine).then(function(id) {
                    $state.go('root.opetussuunnitelmat.lukio.opetus.oppiaine', {
                        oppiaineId: id
                    });
                });
            }
        });
        $scope.openKoodisto = LukioControllerHelpers.openOppiaineKoodisto($scope.oppiaine);
        Editointikontrollit.startEditing();
    })


    .controller('LukioKurssiController', function($scope, $q:IQService, $stateParams,
                        LukioOpetussuunnitelmaService: LukioOpetussuunnitelmaServiceI, Kaanna, $log) {
        // TODO:
        $scope.kurssi = null;
        LukioOpetussuunnitelmaService.getKurssi($stateParams.oppiaineId, $stateParams.kurssiId)
            .then(kurssi => $scope.kurssi = kurssi);
    });