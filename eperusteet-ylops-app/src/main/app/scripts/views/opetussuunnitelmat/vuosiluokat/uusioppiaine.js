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
.controller('UusiOppiaineController', function ($scope, $stateParams, $state, Utils, OpsService, vlk, vlkPeruste,
                                                MurupolkuData, Notifikaatiot, OppiaineCRUD, Kieli, Kaanna) {
  MurupolkuData.set({osioNimi: 'vuosiluokat-ja-oppiaineet', alueId: 'vuosiluokat', vlkNimi: vlk.nimi, vlkId: vlk.id});

  $scope.luonnissa = $stateParams.oppiaineId === 'uusi' || !$stateParams.OppiaineId;
  $scope.options = {};
  $scope.tyypit = [
    'taide_taitoaine',
    'muu_valinnainen'
  ];

  $scope.liittyvatAineet = [];

  $scope.ops = OpsService.get();
  $scope.oppiaineet = $scope.ops.oppiaineet;
  $scope.vuosiluokat = vlkPeruste.vuosiluokat.sort();
  $scope.valitutVuosiluokat = {};

  var sisaltokieli = Kieli.getSisaltokieli();
  vlk.tehtava = {
    otsikko: {},
    teksti: {}
  };
  vlk.tehtava.otsikko[sisaltokieli] = Kaanna.kaanna('oppiaineen-tehtava');

  vlk.arviointi = {
    otsikko: {},
    teksti: {}
  };
  vlk.arviointi.otsikko[sisaltokieli] = Kaanna.kaanna('oppiaine-arviointi');

  vlk.ohjaus = {
    otsikko: {},
    teksti: {}
  };
  vlk.ohjaus.otsikko[sisaltokieli] = Kaanna.kaanna('oppiaine-ohjaus');

  vlk.tyotavat = {
    otsikko: {},
    teksti: {}
  };
  vlk.tyotavat.otsikko[sisaltokieli] = Kaanna.kaanna('oppiaine-tyotavat');

  $scope.oppiaine = {
    nimi: {},
    vuosiluokkakokonaisuudet: [vlk],
  };

  $scope.tavoitteet = [];

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
           model.vuosiluokkakokonaisuudet[0].tehtava.teksti &&
           $scope.valitutVuosiluokat && _($scope.valitutVuosiluokat).values().some()
           ;
  };

  var successCb = function () {
    Notifikaatiot.onnistui('tallennettu-ok');
    if ($scope.luonnissa) {
      $state.go('root.opetussuunnitelmat.yksi.sisalto', {id: res.id}, {reload: true});
    } else {
      fetch(true);
    }
  };

  $scope.tyyppiUpdated = function () {
    if ($scope.oppiaine.tyyppi == 'taide_taitoaine') {
      var liittyvat = ['KUVATAIDE', 'MUSIIKKI', 'LIIKUNTA', 'KOTITALOUS', 'KÄSITYÖ'];
      $scope.liittyvatAineet = _.filter($scope.ops.oppiaineet, function (oppiaine) {
        return _.includes(liittyvat, oppiaine.oppiaine.nimi.fi);
      });
    } else if ($scope.oppiaine.tyyppi == 'muu_valinnainen') {
      $scope.liittyvatAineet = $scope.ops.oppiaineet;
    } else {
      $scope.liittyvatAineet = [];
    }
  };

  $scope.uusi = {
    create: function () {
      var vuosiluokat = _($scope.valitutVuosiluokat)
        .keys()
        .filter(function (vuosiluokka) {
          return $scope.valitutVuosiluokat[vuosiluokka];
        }).value();

      var tallennusDto = {
        oppiaine: $scope.oppiaine,
        vuosiluokkakokonaisuus: vlk,
        vuosiluokat: vuosiluokat,
        tavoitteet: $scope.tavoitteet
      };

      if ($scope.luonnissa) {
        OppiaineCRUD.saveValinnainen({
          opsId: $scope.ops.id
        }, tallennusDto, successCb, Notifikaatiot.serverCb);
      } else {
        $scope.tallennusDto.$save({}, successCb, Notifikaatiot.serverCb);
      }
    },
    cancel: function () {
    }
  };

  $scope.tavoite = {
    add: function () {
      $scope.tavoitteet.push({
        otsikko: {},
        teksti: {}
      });
    }
  };

});
