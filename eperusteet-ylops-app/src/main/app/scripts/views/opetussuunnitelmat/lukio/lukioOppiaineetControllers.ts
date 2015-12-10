
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
        var prefixFunc = (func:string | ((id:string)=>string)) => {
            if (!(func instanceof Function)) {
                var orig = func;
                return (id) => orig + id;
            }
            return <(id:string)=>string>func;
        };
        var tekstit = (idt:string[], label:string | ((id:string)=>string), container?: (any) => any) => (obj) => {
            var labelFunc = prefixFunc(label);
            if (container) {
                obj = container(obj);
            }
            var os = _(idt).map((id) => {return {id: id, label: labelFunc(id), obj: obj?obj[id]:null};}).value(),
                byId = _(os).indexBy(o => o.id).value();
            return {
                osat: os,
                isAddAvailable: () => _.any(os, o => !o.obj),
                isEmpty: () => !_.any(os, o => o.obj),
                addOsa: (id) => {
                    var newOsa = kielella('');
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
        var osat = (idt:string[], label:string | ((id:string)=>string), container?: (any) => any) => (obj) => {
            var labelFunc = prefixFunc(label);
            if (container) {
                obj = container(obj);
            }
            var os = _(idt).map((id) => {
                    return {id: id, obj: obj ? obj[id] : null, label: labelFunc(id)};
                }).value(),
                byId = _(os).indexBy(o => o.id).value();
            return {
                osat: os,
                isAddAvailable: () => _.any(os, o => !o.obj),
                isEmpty: () => !_.any(os, o => o.obj),
                addOsa: (id) => {
                    var newOsa = {
                        otsikko: kielella(Kaanna.kaanna(labelFunc(id))),
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
        var valtakunnallisetKurssiTyypit = ['VALTAKUNNALLINEN_PAKOLLINEN',
            'VALTAKUNNALLINEN_SYVENTAVA', 'VALTAKUNNALLINEN_SOVELTAVA'];
        var paikallisetKurssiTyypit = ['PAIKALLINEN_PAKOLLINEN',
            'PAIKALLINEN_SYVENTAVA', 'PAIKALLINEN_SOVELTAVA'];
        var kaikkiKurssiTyypit = _.union(valtakunnallisetKurssiTyypit, paikallisetKurssiTyypit);
        return {
            openOppiaineKoodisto: (obj:Lukio.Oppiaine) => openKoodisto(obj, 'oppiaineetyleissivistava2'),
            openKurssiKoodisto: (obj:Lukio.LukiokurssiOps) => openKoodisto(obj, 'lukionkurssit',
                    koodi => {
                        obj.lokalisoituKoodi = {};
                        obj.lokalisoituKoodi[Kieli.getSisaltokieli()] = koodi.koodiArvo;
                    }),
            kielella: kielella,
            valtakunnallisetKurssiTyypit: () => _.clone(valtakunnallisetKurssiTyypit),
            paikallisetKurssiTyypit: () => _.clone(paikallisetKurssiTyypit),
            kaikkiKurssiTyypit: () => _.clone(kaikkiKurssiTyypit),
            kurssiTyyppikUvausTekstit: tekstit(kaikkiKurssiTyypit, id => 'lukio-kurssi-tyyppi-otsikko-' + id.toLowerCase(),
                obj => obj.kurssiTyyppiKuvaukset),
            muokattavatOppiaineOsat: osat(['tehtava', 'tavoitteet', 'arviointi'], 'oppiaine-osa-'),
            muokattavatKurssiOsat: osat(['tavoitteet', 'keskeinenSisalto', 'tavoitteetJaKeskeinenSisalto'], 'kurssi-osa-')
        };
    })
    .controller('LukioOppiaineController', function($scope, $q:IQService, $stateParams, $state, $timeout,
                        LukioOpetussuunnitelmaService: LukioOpetussuunnitelmaServiceI, Kaanna, $log,
                        Editointikontrollit, LukioControllerHelpers, Varmistusdialogi, Notifikaatiot, Lukko) {
        // TODO:
        $scope.oppiaine = null;
        $scope.editMode = false;
        $scope.kurssiKuvauksetVisible = false;
        $scope.rootOps = true;
        $scope.connected = () => $scope.oppiaine && !$scope.oppiaine.oma && !$scope.rootOps && $scope.oppiaine.maariteltyPohjassa;
        $scope.isReconnectable = () => $scope.oppiaine && $scope.oppiaine.oma && !$scope.rootOps && $scope.oppiaine.maariteltyPohjassa;
        $scope.isEditable = () => $scope.oppiaine && $scope.oppiaine.oma;
        $scope.isDeletable = () => $scope.oppiaine && $scope.oppiaine.oma && !$scope.oppiaine.maariteltyPohjassa;

        LukioOpetussuunnitelmaService.getOppiaine($stateParams.oppiaineId).then(oa => {
            $scope.oppiaine = oa;
            $scope.muokattavatOsat = LukioControllerHelpers.muokattavatOppiaineOsat(oa);
        });
        LukioOpetussuunnitelmaService.getRakenne().then(r => $scope.rootOps = r.root);

        $scope.openKurssi = (kurssi) => {
            $state.go('root.opetussuunnitelmat.lukio.opetus.kurssi', {
                id: $stateParams.id,
                oppiaineId: $stateParams.oppiaineId,
                kurssiId: kurssi.id
            });
        };
        $scope.valtakunnallisetKurssiTyypit = _.map(LukioControllerHelpers.valtakunnallisetKurssiTyypit(),
            tyyppi => ({
                key: tyyppi,
                tyyppi: tyyppi.split('_')[1].toLowerCase()
            }));
        $scope.paikallisetKurssiTyypit = _.map(LukioControllerHelpers.paikallisetKurssiTyypit(),
            tyyppi => ({
                key: tyyppi,
                tyyppi: tyyppi.split('_')[1].toLowerCase()
            }));
        $scope.kuvauksetIsEmpty = (tyypit:any[]) => {
            return !_.any(tyypit, t => {
                return t && $scope.oppiaine && ($scope.oppiaine.kurssiTyyppiKuvaukset[t.key]
                    || ($scope.oppiaine.perusteen && $scope.oppiaine.perusteen.kurssiTyyppiKuvaukset[t.key]));
                });
        };

        $scope.deleteOppiaine = () => {
            //TODO
        };

        var palautaYlempi = () => {
            LukioOpetussuunnitelmaService.palautaYlempaan($stateParams.oppiaineId)
                .then(res => {
                    Notifikaatiot.onnistui('yhteyden-palautus-onnistui');
                    $timeout(() =>  $state.go('root.opetussuunnitelmat.lukio.opetus.oppiaine', {
                        id: $stateParams.id,
                        oppiaineId: res.id
                    }, { reload: true, notify: true }));
                });
        };

        $scope.connectOppiaine = () => {
            Varmistusdialogi.dialogi({
                otsikko: 'varmista-palauta-yhteys',
                primaryBtn: 'palauta-yhteys',
                // TODO: lukitus!
                successCb: () => palautaYlempi()
            })();
        };

        var cloneOppiaine = () => {
            LukioOpetussuunnitelmaService.kloonaaOppiaineMuokattavaksi($stateParams.oppiaineId)
                .then(res => {
                    Notifikaatiot.onnistui('yhteyden-katkaisu-onnistui');
                    $timeout(() =>  $state.go('root.opetussuunnitelmat.lukio.opetus.oppiaine', {
                        id: $stateParams.id,
                        oppiaineId: res.id
                    }, { reload: true, notify: true }));
                });
        };

        $scope.disconnectOppiaine = () => {
            Varmistusdialogi.dialogi({
                otsikko: 'varmista-katkaise-yhteys',
                primaryBtn: 'katkaise-yhteys',
                // TODO: lukitus!
                successCb: () => cloneOppiaine()
                //successCb: () => Lukko.lock({
                //    opsId: $stateParams.id,
                //    oppiaineId: $stateParams.id
                //}, cloneOppiaine )
            })();
        };

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
        $scope.kurssiTyyppiKuvaukset = LukioControllerHelpers.kurssiTyyppikUvausTekstit($scope.oppiaine);
        $scope.removeKurssiTyyppiKuvaus = (tyyppi)=> { delete $scope.oppiaine.kurssiTyyppiKuvaukset[tyyppi];};
        $scope.addKurssiTyyppiKuvaus = (tyyppi)=> { $scope.oppiaine.kurssiTyyppiKuvaukset[tyyppi] = LukioControllerHelpers.kielella(''); };
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


    .controller('LukioKurssiController', function($scope, $q:IQService, $stateParams, $state, LukioControllerHelpers,
                                                  LukioOpetussuunnitelmaService: LukioOpetussuunnitelmaServiceI, Kaanna, $log) {
        // TODO:
        $scope.kurssi = null;
        $scope.oppiaine = null;
        $scope.editing = false;

        LukioOpetussuunnitelmaService.getOppiaine($stateParams.oppiaineId).then(oa => {
            $scope.oppiaine = oa;
        });

        LukioOpetussuunnitelmaService.getKurssi($stateParams.oppiaineId, $stateParams.kurssiId)
            .then(kurssi => $scope.kurssi = kurssi);

        $scope.getTyyppiSelite = () => {
            if($scope.kurssi){
                var tyyppi = ($scope.kurssi.perusteen)?$scope.kurssi.perusteen.tyyppi:$scope.kurssi.tyyppi;
                tyyppi = tyyppi.contains('_')?tyyppi.split('_')[1].toLowerCase():tyyppi.toLowerCase();
                return 'kurssi-tyyppi-selite-'+tyyppi;
            }
            return '';
        };

        $scope.edit = () => {
            $scope.editing = true;
            //TODO
            //Editointikontrollit.startEditing();
        };

        $scope.openKoodisto = LukioControllerHelpers.openKurssiKoodisto( $scope.kurssi );

        $scope.goBack = () => {
            $state.go('root.opetussuunnitelmat.lukio.opetus.oppiaine', {
                oppiaineId: $stateParams.oppiaineId
            });
        };
    })

    .controller('LukioOppiaineSisaltoController', function($scope, $q:IQService, $stateParams,
                                                  LukioOpetussuunnitelmaService: LukioOpetussuunnitelmaServiceI, Kaanna, $log) {
        $scope.textHidden = true;
        $scope.editing = false;

        $scope.toggleTextVisible = () => {
            $scope.textHidden = !$scope.textHidden;
        };

        $scope.edit = () => {
            console.log("edit");
            $scope.editing = true;
        };
    })


.directive('lukioOppiaineOsa', function () {
    return {
        scope: {
            model: '=lukioOppiaineOsa',
            oppiaine: '=oppiaine',
            perusteenTeksti: '=perusteenTeksti',
            oppiaineenTeksti: '=oppiaineenTeksti',
            title: '=title',
            colorbox: '=colorbox',
            ikoni: '=ikoni',
            editable: '=editable',
            editing: '=editing'
        },
        templateUrl: 'views/opetussuunnitelmat/directives/oppiaineSisalto.html',
        controller: 'LukioOppiaineSisaltoController'
    };
});


