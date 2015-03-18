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
.controller('UusiOppiaineController', function ($scope, $stateParams, Utils, OpsService, vlk, vlkPeruste, MurupolkuData,
                                                Notifikaatiot, OppiaineCRUD) {
  MurupolkuData.set({osioNimi: 'vuosiluokat-ja-oppiaineet', alueId: 'vuosiluokat', vlkNimi: vlk.nimi, vlkId: vlk.id});

  $scope.options = {};
  $scope.tyypit = [
    'taide_taitoaine',
    'muu_valinnainen'
  ];
  $scope.ops = OpsService.get();
  $scope.oppiaineet = $scope.ops.oppiaineet;
  $scope.vuosiluokat = vlkPeruste.vuosiluokat.sort();
  $scope.valitutVuosiluokat = {};

  $scope.oppiaine = {
    nimi: {},
    tehtava: {teksti: {}},
    arviointi: {teksti: {}},
    ohjaus: {teksti: {}},
    tyotavat: {teksti: {}},
    tavoitteet: []
  };
  $scope.chosenVlk = {};
  if ($stateParams.vlkId) {
    $scope.chosenVlk[$stateParams.vlkId] = true;
  }

  $scope.hasRequiredFields = function () {
    var model = $scope.oppiaine;
    return Utils.hasLocalizedText(model.nimi) &&
           _.any(_.values($scope.chosenVlk)) &&
           model.tyyppi &&
           model.laajuus &&
           model.tehtava.teksti &&
           $scope.valitutVuosiluokat && _($scope.valitutVuosiluokat).values().some()
           ;
  };

  var successCb = function () {
    Notifikaatiot.onnistui('tallennettu-ok');
  };

  $scope.uusi = {
    create: function () {
        OppiaineCRUD.save({
          opsId: $scope.ops.id
        }, $scope.oppiaine, successCb, Notifikaatiot.serverCb);
    },
    cancel: function () {
    }
  };

  $scope.tavoite = {
    add: function () {
      $scope.oppiaine.tavoitteet.push({
        tavoite: {},
        kuvaus: {}
      });
    }
  };

});
