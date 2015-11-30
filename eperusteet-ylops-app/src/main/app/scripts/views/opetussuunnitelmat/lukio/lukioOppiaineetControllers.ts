
ylopsApp
    .controller('LukioOppiaineetController', function($scope, $q:IQService, $stateParams,
                                                 LukioOpetussuunnitelmaService, Kaanna, $log) {
        // TODO:
    })
    .controller('LukioOppiaineController', function($scope, $q:IQService, $stateParams,
                                                    LukioOpetussuunnitelmaService, Kaanna, $log) {
        // TODO:
        $scope.oppiaine = null;
        LukioOpetussuunnitelmaService.getOppiaine($stateParams.oppiaineId).then(function(oa: Lukio.LukioOppiaine) {
            $scope.oppiaine = oa;
        });
    })
    .controller('LukioKurssiController', function($scope, $q:IQService, $stateParams,
                                                    LukioOpetussuunnitelmaService, Kaanna, $log) {
        // TODO:
        $scope.kurssi = null;
        LukioOpetussuunnitelmaService.getKurssi($stateParams.oppiaineId,
                        $stateParams.kurssiId).then(function(kurssi: Lukio.LukiokurssiOps) {
            $scope.kurssi = kurssi;
        });
    });