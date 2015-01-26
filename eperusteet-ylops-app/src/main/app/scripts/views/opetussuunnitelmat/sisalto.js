/*
* Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
*
* This program is free software: Licensed under the EUPL, Version 1.1 or - as
* soon as they will be approved by the European Commission - subsequent versions
* of the EUPL (the "Licence");
*
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* European Union Public Licence for more details.
*/

'use strict';

ylopsApp
.controller('OpetussuunnitelmaSisaltoController', function ($scope, OpetussuunnitelmanTekstit,
  Notifikaatiot, opsService, opsModel, $rootScope, $stateParams) {
  $scope.rakenneEdit = {jarjestaminen: false, lahtokohdat: false};

  console.log("sisalto ctrl", $stateParams);
  $scope.model = opsModel;

  /*$scope.$on('$stateChangeSuccess', function () {
    console.log("state change");
    $scope.model = opsService.get();
  });*/

  function mapModel() {
    $scope.model.jarjestaminen = $scope.model.tekstit ? $scope.model.tekstit.lapset[0] : [];
    $scope.model.lahtokohdat = $scope.model.tekstit ? $scope.model.tekstit.lapset[1] : [];
  }

  $scope.$watch('model', function () {
    mapModel();
  }, true);

  function fetch(cb, notify) {
    opsService.refetch(function (res) {
      $scope.model = res;
      (cb || angular.noop)(res);
      if (notify) {
        $rootScope.$broadcast('rakenne:updated');
      }
    });
  }

  function mapSisalto(root) {
    return {
      id: root.id,
      lapset: _.map(root.lapset, mapSisalto)
    };
  }

  function saveRakenne(osio) {
    var postdata = mapSisalto($scope.model.tekstit);
    OpetussuunnitelmanTekstit.save({
      opsId: $scope.model.id,
      viiteId: $scope.model.tekstit.id
    }, postdata, function () {
      Notifikaatiot.onnistui('tallennettu-ok');
      $scope.rakenneEdit[osio] = false;
      fetch(angular.noop, true);
    }, Notifikaatiot.serverCb);
  }

  var original = null;

  $scope.rakenne = {
    edit: function (osio, event) {
      event.preventDefault();
      event.stopPropagation();
      fetch();
      $scope.rakenneEdit[osio] = true;
      original = _.cloneDeep($scope.model[osio].lapset);
    },
    cancel: function (osio) {
      $scope.rakenneEdit[osio] = false;
      $scope.model[osio].lapset = _.cloneDeep(original);
      fetch();
    },
    save: function (osio) {
      saveRakenne(osio);
    }
  };
});
