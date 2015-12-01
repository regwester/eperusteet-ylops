
ylopsApp
    .controller('LukioOppiaineetController', function($scope, $q:IQService, $stateParams,
                         LukioOpetussuunnitelmaService: LukioOpetussuunnitelmaServiceI, Kaanna, $log) {
        // TODO:
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