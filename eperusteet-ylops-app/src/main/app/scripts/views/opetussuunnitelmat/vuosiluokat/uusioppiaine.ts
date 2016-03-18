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
.controller('UusiOppiaineController', function ($scope, $stateParams, $state, $rootScope, Utils, OpsService, vlk,
                                                vlkPeruste, MurupolkuData, Notifikaatiot, OppiaineCRUD, Kieli, Kaanna, Lukko) {
  MurupolkuData.set({osioNimi: 'vuosiluokat-ja-oppiaineet', alueId: 'vuosiluokat', vlkNimi: vlk.nimi, vlkId: vlk.id});
  $rootScope.$broadcast('navigaatio:hide');

  $scope.luonnissa = $stateParams.oppiaineId === 'uusi' || !$stateParams.oppiaineId;
  $scope.options = {};
  $scope.tyypit = [
    'taide_taitoaine',
    'muu_valinnainen'
  ];

  $scope.ops = OpsService.get();
  $scope.oppiaineet = $scope.ops.oppiaineet;
  $scope.vuosiluokat = vlkPeruste.vuosiluokat.sort();
  $scope.valitutVuosiluokat = {};

  if ($scope.luonnissa) {
    const sisaltokieli = Kieli.getSisaltokieli();
    vlk.tehtava = {
      otsikko: {},
      teksti: {}
    };
    vlk.tehtava.otsikko[sisaltokieli] = Kaanna.kaanna('valinnaisen-oppiaineen-tehtava');

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
  } else {
    const opsId = OpsService.getId();
    OppiaineCRUD.get({opsId: opsId}, {id: $stateParams.oppiaineId}, (oppiaine) => {
      $scope.oppiaine = oppiaine;

      $scope.commonParams = {
        opsId: opsId,
        vlkId: oppiaine.vuosiluokkakokonaisuudet[0].id,
        oppiaineId: oppiaine.id
      };
      Lukko.isLocked($scope, $scope.commonParams);

      const vuosiluokat = oppiaine.vuosiluokkakokonaisuudet[0].vuosiluokat;
      _.forEach(vuosiluokat, (vl) => {
        $scope.valitutVuosiluokat[vl.vuosiluokka] = true;
      });
      const vuosiluokka = vuosiluokat[0];
      const tavoitteet = _.map(vuosiluokka.tavoitteet, 'tavoite');
      const sisallot = _.map(vuosiluokka.sisaltoalueet, 'kuvaus');
      $scope.tavoitteet = _.map(_.zip(tavoitteet, sisallot), (pair) => {
        return {
          otsikko: pair[0],
          teksti: pair[1]
        };
      });
    });
  }

  $scope.chosenVlk = {};
  if ($stateParams.vlkId) {
    $scope.chosenVlk[$stateParams.vlkId] = true;
  }

  const hasRequiredTavoitteet = () => _.every($scope.tavoitteet,
      (tavoite) => Utils.hasLocalizedText(tavoite.otsikko) && Utils.hasLocalizedText(tavoite.teksti));

  $scope.hasRequiredFields = () => {
    const model = $scope.oppiaine;

    return model &&
           Utils.hasLocalizedText(model.nimi) &&
           _.any(_.values($scope.chosenVlk)) &&
           model.tyyppi &&
           model.laajuus &&
           model.vuosiluokkakokonaisuudet[0].tehtava.teksti &&
           $scope.valitutVuosiluokat && _($scope.valitutVuosiluokat).values().some() &&
        (model.id ? true : hasRequiredTavoitteet());
  };

  const successCb = (res) => {
    Notifikaatiot.onnistui('tallennettu-ok');
    $state.go('root.opetussuunnitelmat.yksi.opetus.oppiaine.oppiaine', {
      oppiaineId: res.id,
      vlkId: $stateParams.vlkId,
      oppiaineTyyppi: res.tyyppi
    }, { reload: true });
  };

  $scope.uusi = {
    create: () => {
      $rootScope.$broadcast('notifyCKEditor');

      const vuosiluokat = _($scope.valitutVuosiluokat)
        .keys()
        .filter((vuosiluokka) => {
          return $scope.valitutVuosiluokat[vuosiluokka];
        }).value();

      const tallennusDto = {
        oppiaine: $scope.oppiaine,
        vuosiluokkakokonaisuusId: vlk.id,
        vuosiluokat: vuosiluokat,
        tavoitteet: $scope.tavoitteet
      };

      if ($scope.luonnissa) {
        OppiaineCRUD.saveValinnainen({
          opsId: $scope.ops.id
        }, tallennusDto, successCb, Notifikaatiot.serverCb);
      } else {
        Lukko.unlock($scope.commonParams, () => {
          OppiaineCRUD.saveValinnainen({
            opsId: $scope.ops.id,
            oppiaineId: $scope.oppiaine.id
          }, tallennusDto, successCb, Notifikaatiot.serverCb);
        });
      }
    },
    cancel: () => {
      if ($scope.luonnissa) {
        $state.go('root.opetussuunnitelmat.yksi.opetus.valinnaiset', { vlkId: $stateParams.vlkId });
      }
      else {
        Lukko.unlock($scope.commonParams);
        $state.go('root.opetussuunnitelmat.yksi.opetus.oppiaine.oppiaine', $stateParams);
      }
    }
  };

  $scope.tavoiteFns = {
    add: () => {
      if (!$scope.tavoitteet) {
        $scope.tavoitteet = [];
      }

      $scope.tavoitteet.push({
        otsikko: {},
        teksti: {}
      });
    },

    remove: (item) => {
      if ($scope.tavoitteet) {
        $scope.tavoitteet.splice(item, 1);
      }
    }
  };

});
