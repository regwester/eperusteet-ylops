'use strict';

ylopsApp
 .controller('EtusivuController', function ($scope, Oikeudet, $state) {
    $scope.isVirkailija = Oikeudet.isVirkailija;

     $scope.addNewPohja = function () {
        $state.go('root.pohjat.yksi.tiedot', {pohjaId: 'uusi'});
    };
 });