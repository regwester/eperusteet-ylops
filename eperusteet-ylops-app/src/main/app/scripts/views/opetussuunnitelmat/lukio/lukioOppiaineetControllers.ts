
ylopsApp
    .controller('LukioOppiaineetController', function($scope, $q:IQService, $stateParams, Kaanna, $log,
                                                      LukioOpetussuunnitelmaService: LukioOpetussuunnitelmaServiceI,
                                                      LukioTreeUtils: LukioTreeUtilsI) {
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
        $scope.treeProvider = $q.when(<GenericTreeConfig<LukioKurssiTreeNode>>{
            root: () => {
                var d = $q.defer<LukioKurssiTreeNode>();
                LukioOpetussuunnitelmaService.getRakenne().then(rakenne => {
                    $scope.treeRoot = LukioTreeUtils.treeRootFromRakenne(rakenne);
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