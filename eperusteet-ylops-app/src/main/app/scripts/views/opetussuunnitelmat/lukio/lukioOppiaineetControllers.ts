
ylopsApp
    .controller('LukioOppiaineetController', function($scope, $q:IQService, $stateParams, Kaanna, $log,
                                                      LukioOpetussuunnitelmaService: LukioOpetussuunnitelmaServiceI,
                                                      LukioTreeUtils: LukioTreeUtilsI, Kommentit) {
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
        $scope.toggleCollapse = LukioTreeUtils.collapseToggler(() => $scope.treeRoot, state);
        $scope.liittamattomatRoot = <LukioKurssiTreeNode>{
            dtype: LukioKurssiTreeNodeType.root,
            lapset: [] // should be none by default
        };
        $scope.liitetytRoot = <LukioKurssiTreeNode>{
            dtype: LukioKurssiTreeNodeType.root,
            lapset: [] // should be none by default
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
                    $scope.liitetytRoot.lapset.length =0;
                    _.each(_($scope.treeRoot).flattenTree(_.property('lapset'))
                            .filter((n:LukioKurssiTreeNode) => n.dtype == LukioKurssiTreeNodeType.kurssi).value(),
                        (k: LukioKurssiTreeNode) => {$scope.liitetytRoot.lapset.push(k);}
                    );
                    updatePagination();
                    d.resolve($scope.treeRoot);
                });
                return d.promise;
            },
            children: node => $q.when(node.lapset || []),
            useUiSortable: state.isEditMode,
            hidden: LukioTreeUtils.defaultHidden,
            template: node => LukioTreeUtils.templates(state).nodeTemplate(node),
            extension: LukioTreeUtils.extensions((n, scope) => {
            }),
            acceptDrop: LukioTreeUtils.acceptMove
        });

        $scope.liittamattomatPagination = <PaginationDetails>{showPerPage: 5, currentPage: 1};
        $scope.liittamattomatTreeProvider = $q.when(<GenericTreeConfig<LukioKurssiTreeNode>>{
            root: () => $q.when($scope.liittamattomatRoot),
            children: node => $q.when(node.lapset || []),
            useUiSortable: state.isEditMode,
            hidden: LukioTreeUtils.defaultHidden,
            template: node => LukioTreeUtils.templates(state).nodeTemplateKurssilista(node),
            extension: LukioTreeUtils.extensions((n, scope) => {
            }),
            acceptDrop: LukioTreeUtils.acceptMove
        });

        $scope.liitetytPagination = <PaginationDetails>{showPerPage: 5, currentPage: 1};
        $scope.liitetytTreeProvider = $q.when(<GenericTreeConfig<LukioKurssiTreeNode>>{
            root: () => $q.when($scope.liitetytRoot),
            children: node => $q.when(node.lapset || []),
            useUiSortable: state.isEditMode,
            hidden: LukioTreeUtils.defaultHidden,
            template: node => LukioTreeUtils.templates(state).nodeTemplateKurssilista(node),
            extension: LukioTreeUtils.extensions((n, scope) => {
            }),
            acceptDrop: LukioTreeUtils.acceptMove
        });

        $scope.toEditMode = () => {
            $scope.editMode = true;
            $scope.$broadcast('genericTree:refresh'); // templates get updated
            //TODO:editointikontrollit
        };

        // TODO:kommentit
    })
    .controller('LukioOppiaineController', function($scope, $q:IQService, $stateParams,
                        LukioOpetussuunnitelmaService: LukioOpetussuunnitelmaServiceI, Kaanna, $log) {
        // TODO:
        $scope.oppiaine = null;
        LukioOpetussuunnitelmaService.getOppiaine($stateParams.oppiaineId).then(oa => $scope.oppiaine = oa);
    })
    .controller('LukioKurssiController', function($scope, $q:IQService, $stateParams,
                        LukioOpetussuunnitelmaService: LukioOpetussuunnitelmaServiceI, Kaanna, $log) {
        // TODO:
        $scope.kurssi = null;
        LukioOpetussuunnitelmaService.getKurssi($stateParams.oppiaineId, $stateParams.kurssiId)
            .then(kurssi => $scope.kurssi = kurssi);
    });