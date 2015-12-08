
interface KoodistoArvo {
    koodiArvo: string
    koodiUri: string
    nimi: l.Lokalisoitu
}

ylopsApp
    .controller('LukioOppiaineetController', function($scope, $q:IQService, $stateParams, Kaanna, $log,
                                                      LukioOpetussuunnitelmaService: LukioOpetussuunnitelmaServiceI,
                                                      LukioTreeUtils: LukioTreeUtilsI, Kommentit,
                                                      Editointikontrollit, $state) {
        $scope.editMode = false;
        $scope.sortableConfig = <SortableConfig> {
            placeholder: 'placeholder',
            handle: '.treehandle',
            cursorAt: { top : 5, left: 5 }
        };
        $scope.sortableLiittamattomatConfig = <SortableConfig> _.cloneDeep($scope.sortableConfig);
        $scope.sortableLiitetytConfig = <SortableConfig> _.cloneDeep($scope.sortableConfig);
        $scope.treeRoot =  <LukioKurssiTreeNode>{
            dtype: LukioKurssiTreeNodeType.root,
            lapset: []
        };
        $scope.liittamattomatRoot = <LukioKurssiTreeNode>_.cloneDeep($scope.treeRoot);
        $scope.liitetytRoot = <LukioKurssiTreeNode>_.cloneDeep($scope.treeRoot);
        var updatePagination = () => {
            LukioTreeUtils.updatePagination($scope.liittamattomatRoot.lapset, $scope.liittamattomatPagination);
            LukioTreeUtils.updatePagination($scope.liitetytRoot.lapset, $scope.liitetytPagination);
        };
        var state:LukioKurssiTreeState = {
            isEditMode: () => $scope.editMode,
            defaultCollapse: false,
            root: () => $scope.treeRoot,
            liitetytKurssit: () => $scope.liitetytRoot.lapset,
            liittamattomatKurssit: () => $scope.liittamattomatRoot.lapset,
            updatePagination: () => updatePagination()
        };
        $scope.toggleCollapse = LukioTreeUtils.collapseToggler(() => $scope.treeRoot, state);
        var resolveRakenne = (d: IDeferred<LukioKurssiTreeNode>) => LukioOpetussuunnitelmaService.getRakenne().then(rakenne => {
            $scope.treeRoot.lapset.length = 0; // empty the Array (but maintain object reference)
            _.each(LukioTreeUtils.treeRootLapsetFromRakenne(rakenne), (lapsi:LukioKurssiTreeNode) => {
                $scope.treeRoot.lapset.push(lapsi);
            });
            $scope.liitetytRoot.lapset.length = 0; // empty the Array (but maintain object reference)
            _.each(_($scope.treeRoot).flattenTree(n => n.lapset)
                    .filter((n:LukioKurssiTreeNode) => n.dtype == LukioKurssiTreeNodeType.kurssi)
                    .uniq((n:LukioKurssiTreeNode) => n.id).value(),
                (k: LukioKurssiTreeNode) => {
                    k = _.cloneDeep(k);
                    k.$$nodeParent = null;
                    $scope.liitetytRoot.lapset.push(k);
                }
            );
            $scope.liittamattomatRoot.lapset.length = 0; // empty the Array (but maintain object reference)
            updatePagination();
            d.resolve($scope.treeRoot);
            return rakenne;
        });
        $scope.treeProvider = $q.when(<GenericTreeConfig<LukioKurssiTreeNode>>{
            root: () => {
                var d = $q.defer<LukioKurssiTreeNode>();
                resolveRakenne(d);
                return d.promise;
            },
            children: node => $q.when(node.lapset || []),
            useUiSortable: () => !state.isEditMode(),
            hidden: LukioTreeUtils.defaultHidden,
            template: node => LukioTreeUtils.templates(state).nodeTemplate(node),
            extension: LukioTreeUtils.extensions(state, (n, scope) => {
            }),
            acceptDrop: LukioTreeUtils.acceptDropWrapper(state),
            sortableClass: _.constant('is-draggable-into')
        });

        $scope.liittamattomatPagination = <PaginationDetails>{showPerPage: 5, currentPage: 1};
        $scope.liittamattomatTreeProvider = $q.when(<GenericTreeConfig<LukioKurssiTreeNode>>{
            root: () => $q.when($scope.liittamattomatRoot),
            children: node => $q.when(node.lapset || []),
            useUiSortable: () => !state.isEditMode(),
            hidden: LukioTreeUtils.defaultHidden,
            template: node => LukioTreeUtils.templates(state).nodeTemplateKurssilista(node),
            extension: LukioTreeUtils.extensions(state, (n, scope) => {
            }),
            acceptDrop: LukioTreeUtils.acceptDropWrapper(state),
            sortableClass: _.constant('is-draggable-into')
        });

        $scope.liitetytPagination = <PaginationDetails>{showPerPage: 5, currentPage: 1};
        $scope.liitetytTreeProvider = $q.when(<GenericTreeConfig<LukioKurssiTreeNode>>{
            root: () => $q.when($scope.liitetytRoot),
            children: node => $q.when(node.lapset || []),
            hidden: LukioTreeUtils.defaultHidden,
            useUiSortable: () => !state.isEditMode(),
            template: node => LukioTreeUtils.templates(state).nodeTemplateKurssilista(node),
            extension: LukioTreeUtils.extensions(state, (n, scope) => {
            }),
            acceptDrop: LukioTreeUtils.acceptDropWrapper(state),
            sortableClass: _.constant('is-draggable-into')
        });
        $scope.haku = LukioTreeUtils.luoHaku(state);
        $scope.liitetytHaku = LukioTreeUtils.luoHaku(state, $scope.liitetytRoot);
        $scope.liittamattomatHaku = LukioTreeUtils.luoHaku(state, $scope.liittamattomatRoot);

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
                resolveRakenne($q.defer()).then(() => {
                    $scope.$broadcast('genericTree:refresh'); // templates get updated
                });
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


    .service('LukioControllerHelpers', function($rootScope, Koodisto, MuokkausUtils, Kieli, Kaanna, $log) {
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
        var kielella = (val) => {
            var loc = {};
            loc[Kieli.getSisaltokieli()] = val;
            return loc;
        };
        var osat = (idt:string[], locPrefix:string) => (obj) => {
          var os = _(idt).map((id) => {return {id: id, obj: obj[id]};}).value(),
              byId = _(os).indexBy(o => o.id).value();
          return {
              osat: os,
              isAddAvailable: () => _.any(os, o => !o.obj),
              addOsa: (id) => {
                  var newOsa = {
                      otsikko: kielella(Kaanna.kaanna(locPrefix+id)),
                      teksti: kielella('')
                  };
                  obj[id] = newOsa;
                  byId[id].obj = newOsa;
                  $rootScope.$broadcast('notifyCKEditor');
                  $rootScope.$broadcast('enableEditing');
              },
              removeOsa: (id) => {
                  obj[id] = null;
                  byId[id].obj = null;
                  $rootScope.$broadcast('notifyCKEditor');
              }
          };
        };
        return {
            openOppiaineKoodisto: (obj:Lukio.Oppiaine) => openKoodisto(obj, 'oppiaineetyleissivistava2'),
            openKurssiKoodisto: (obj:Lukio.LukiokurssiOps) => openKoodisto(obj, 'lukionkurssit',
                    koodi => {
                        obj.lokalisoituKoodi = {};
                        obj.lokalisoituKoodi[Kieli.getSisaltokieli()] = koodi.koodiArvo;
                    }),
            muokattavatOppiaineOsat: osat(['tehtava', 'tavoitteet', 'arviointi'], 'oppiaine-osa-'),
            muokattavatKurssiOsat: osat(['tavoitteet', 'keskeinenSisalto', 'tavoitteetJaKeskeinenSisalto'], 'kurssi-osa-')
        };
    })
    .controller('LukioOppiaineController', function($scope, $q:IQService, $stateParams, $state,
                        LukioOpetussuunnitelmaService: LukioOpetussuunnitelmaServiceI, Kaanna, $log,
                        Editointikontrollit, LukioControllerHelpers) {
        // TODO:
        $scope.oppiaine = null;
        $scope.editMode = false;
        LukioOpetussuunnitelmaService.getOppiaine($stateParams.oppiaineId).then(oa => {
            $scope.oppiaine = oa;
            $scope.muokattavatOsat = LukioControllerHelpers.muokattavatOppiaineOsat(oa);
        });

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
        $scope.muokattavatOsat = LukioControllerHelpers.muokattavatOppiaineOsat($scope.oppiaine);
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
                LukioOpetussuunnitelmaService.saveOppiaine($scope.oppiaine).then(function(ref) {
                    $state.go('root.opetussuunnitelmat.lukio.opetus.oppiaine', {
                        oppiaineId: ref.id
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
    })

    .controller('LukioOppiaineSisaltoController', function($scope, $q:IQService, $stateParams,
                                                  LukioOpetussuunnitelmaService: LukioOpetussuunnitelmaServiceI, Kaanna, $log) {
        $scope.textHidden = true;

        $scope.toggleTextVisible = function(){
            $scope.textHidden = !$scope.textHidden;
        }
    });