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
/*global _*/

ylopsApp
.controller('OpetussuunnitelmaTiedotController', function ($scope, Editointikontrollit, $stateParams, $state,
  $timeout, $rootScope, OpetussuunnitelmaCRUD, Notifikaatiot, OpsService, Utils, KoodistoHaku, PeruskouluHaku,
  PeruskoulutoimijaHaku, kunnat, Kieli) {

  $scope.luonnissa = $stateParams.id === 'uusi';
  $scope.editableModel = $scope.model;
  if ($scope.luonnissa) {
    $scope.editableModel.julkaisukielet = ['fi'];
    $scope.editableModel._pohja = $stateParams.pohjaId === '' ? null : $stateParams.pohjaId;
  }
  $scope.editMode = false;
  $scope.kielivalinnat = ['fi', 'sv', 'se'];
  $scope.loading = false;
  $scope.kuntalista = [];
  $scope.koulutoimijalista = [];
  $scope.koululista = [];
  $scope.eiKoulujaVaroitus = false;
  $scope.nimiOrder = function(vlk) {
    return Utils.sort(vlk.vuosiluokkakokonaisuus);
  };
  //$scope.vuosiluokkakokonaisuudet = [];


  $scope.hasRequiredFields = function () {
    var model = $scope.editableModel;
    return Utils.hasLocalizedText(model.nimi) &&
           model.kunnat && model.kunnat.length > 0 &&
           model.koulutoimijat && model.koulutoimijat.length > 0 &&
           _.any(_.values($scope.julkaisukielet)) &&
           (_(model.vuosiluokkakokonaisuudet).filter({valittu: true}).size() > 0 || model.koulutustyyppi !== 'koulutustyyppi_16');
  };

  function mapKunnat(lista) {
    return _(lista).map(function (kunta) {
      return {
        koodiUri: kunta.koodiUri,
        koodiArvo: kunta.koodiArvo,
        nimi: _(kunta.metadata).indexBy(function (item) {
          return item.kieli.toLowerCase();
        }).mapValues('nimi').value()
      };
    }).sortBy(Utils.sort).value();
  }

  function filterOrganisaatio(tyyppi) {
    return function (organisaatiot) {
      return _.filter(organisaatiot, function (org) {
        return _.includes(org.tyypit, tyyppi);
      });
    };
  }

  var filterKoulutustoimija = filterOrganisaatio('Koulutustoimija');
  var filterOppilaitos = filterOrganisaatio('Oppilaitos');

  if (kunnat) {
    $scope.kuntalista = mapKunnat(kunnat);
  }

  if ($scope.model.organisaatiot) {
    $scope.model.koulutoimijat = filterKoulutustoimija($scope.model.organisaatiot);
    $scope.model.koulut = filterOppilaitos($scope.model.organisaatiot);
  }

  //Jos luodaan uutta ops:ia toisesta opetussuunnitelmasta,
  // niin haetaan pohja opetussuunnitelmasta kunnat ja organisaatiot
  if ($scope.luonnissa && $scope.editableModel._pohja) {
    OpetussuunnitelmaCRUD.get({opsId: $scope.editableModel._pohja}, function (res) {
      $scope.$$pohja = res;
      $scope.pohjanNimi = res.nimi;
      $scope.editableModel.kunnat = res.kunnat;
      $scope.editableModel.koulutoimijat = filterKoulutustoimija(res.organisaatiot);
      $scope.editableModel.koulut = filterOppilaitos(res.organisaatiot);
      $scope.editableModel.vuosiluokkakokonaisuudet = res.vuosiluokkakokonaisuudet;
      $scope.editableModel.koulutustyyppi = res.koulutustyyppi;
    }, Notifikaatiot.serverCb);
    // Jos luonnissa ja ei pohja ops:ia, haetaan vuosiluokkakokonaisuudet virkailijan pohjasta
  } else if ($scope.luonnissa && !$scope.editableModel._pohja) {
    OpetussuunnitelmaCRUD.query({tyyppi: 'pohja'}, function(pohjat) {
      // TODO: pitää varmaankin ottaa huomioon mitä ops:ia ollaan luomassa. (Esi-, lisä vai perusopetus)
      var aktiivinenPohja = _.find(pohjat, {tila: 'valmis', koulutustyyppi: 'koulutustyyppi_16'});
      OpetussuunnitelmaCRUD.get({opsId: aktiivinenPohja.id}, function (ops) {
        $scope.editableModel.vuosiluokkakokonaisuudet = ops.vuosiluokkakokonaisuudet;
      });
    });
  }

  $scope.kieliOrderFn = Kieli.orderFn;

  function fetch(notify) {
    OpsService.refetch(function (res) {
      $scope.model = res;
      $scope.editableModel = res;
      $scope.editableModel.koulutoimijat = filterKoulutustoimija(res.organisaatiot);
      $scope.editableModel.koulut = filterOppilaitos(res.organisaatiot);
      if (notify) {
        $rootScope.$broadcast('rakenne:updated');
      }
      $scope.loading = false;
    });
  }

  var successCb = function (res) {
    Notifikaatiot.onnistui('tallennettu-ok');
    if ($scope.luonnissa) {
      $state.go('root.opetussuunnitelmat.yksi.sisalto', {id: res.id}, {reload: true});
    } else {
      fetch(true);
    }
  };

  var callbacks = {
    edit: function () {
      $scope.loading = true;
      fetch();
    },
    validate: function () {
      return $scope.hasRequiredFields();
    },
    save: function () {
      $scope.editableModel.julkaisukielet = _($scope.julkaisukielet).keys().filter(function (koodi) {
        return $scope.julkaisukielet[koodi];
      }).value();
      $scope.editableModel.organisaatiot = $scope.editableModel.koulutoimijat.concat($scope.editableModel.koulut);
      if ($scope.luonnissa) {
        $scope.editableModel.vuosiluokkakokonaisuudet = _.remove($scope.editableModel.vuosiluokkakokonaisuudet, {valittu: true});
        OpetussuunnitelmaCRUD.save({}, $scope.editableModel, successCb, Notifikaatiot.serverCb);
      } else {
        $scope.editableModel.$save({}, successCb, Notifikaatiot.serverCb);
      }
    },
    cancel: function () {
      fetch();
    },
    notify: function (mode) {
      $scope.editMode = mode;
      if (mode) {
        $scope.haeKunnat();
        $scope.haeKoulutoimijat();
        $scope.haeKoulut();
      }
    }
  };
  Editointikontrollit.registerCallback(callbacks);

  $scope.uusi = {
    cancel: function () {
      $timeout(function () {
        $state.go('root.etusivu');
      });
    },
    create: function () {
      callbacks.save();
    }
  };

  $scope.edit = function () {
    Editointikontrollit.startEditing();
  };

  $scope.haeKunnat = function () {
    if (!($scope.editMode || $scope.luonnissa)) {
      return;
    }
    KoodistoHaku.get({ koodistoUri: 'kunta' }, function(kunnat) {
      $scope.kuntalista = mapKunnat(kunnat);
    }, Notifikaatiot.serverCb);
  };

  function updateKouluVaroitus() {
    $scope.eiKoulujaVaroitus = _.isArray($scope.editableModel.koulutoimijat) &&
      $scope.editableModel.koulutoimijat.length === 1 &&
      _.isArray($scope.koululista) && $scope.koululista.length === 0;
  }

  function updateKoulutoimijaVaroitus() {
    $scope.eiKoulutoimijoitaVaroitus = _.isArray($scope.editableModel.kunnat) &&
      $scope.editableModel.kunnat.length === 1 &&
      _.isArray($scope.koulutoimijat) && $scope.koulutoimijat.length === 0;
  }

  $scope.$watch('editableModel.kunnat', function () {
    $scope.haeKoulutoimijat();
  });

  $scope.$watch('editableModel.koulutoimijat', function () {
    $scope.haeKoulut();
    updateKoulutoimijaVaroitus();
  });

  $scope.$watch('koululista', function () {
    updateKouluVaroitus();
  });

  function mapJulkaisukielet() {
    $scope.julkaisukielet = _.zipObject($scope.kielivalinnat, _.map($scope.kielivalinnat, function (kieli) {
      return _.indexOf($scope.editableModel.julkaisukielet, kieli) > -1;
    }));
  }

  $scope.$watch('editableModel.julkaisukielet', mapJulkaisukielet);

  $scope.haeKoulut = function () {
    $scope.loadingKoulut = true;
    var koulutoimijat = $scope.editableModel.koulutoimijat;
    if (!($scope.editMode || $scope.luonnissa) || !koulutoimijat) {
      $scope.loadingKoulut = false;
      return;
    }
    if (koulutoimijat.length === 0) {
      $scope.loadingKoulut = false;
      $scope.editableModel.koulut = [];
    } else if (koulutoimijat.length === 1) {
      var koulutoimija = koulutoimijat[0];
      PeruskouluHaku.get({oid: koulutoimija.oid}, function (res) {
        $scope.koululista = _(res).map(function (koulu) {
          return _.pick(koulu, ['oid', 'nimi', 'tyypit']);
        }).sortBy(Utils.sort).value();

        $scope.loadingKoulut = false;
      }, Notifikaatiot.serverCb);
    } else {
      $scope.loadingKoulut = false;
      $scope.editableModel.koulut = [];
      $scope.koululista = [];
    }
  };

  $scope.haeKoulutoimijat = function () {

    $scope.loadingKoulutoimijat = true;
    var kunnat = $scope.editableModel.kunnat;
    if (!($scope.editMode || $scope.luonnissa) || !kunnat) {
      $scope.loadingKoulutoimijat = false;
      return;
    }
    if (kunnat.length === 0) {
      $scope.loadingKoulutoimijat = false;
      $scope.editableModel.koulutoimijat = [];
      $scope.editableModel.koulut = [];
    } else {
      var kuntaUrit = _.map(kunnat, 'koodiUri');
      PeruskoulutoimijaHaku.get({ kuntaUri: kuntaUrit }, function(res) {
        $scope.koulutoimijalista =  _(res).map(function (koulutoimija) {
          return _.pick(koulutoimija, ['oid', 'nimi', 'tyypit']);
        }).sortBy(Utils.sort).value();

        $scope.editableModel.koulutoimijat = _.filter($scope.editableModel.koulutoimijat, function (koulutoimija) {
          return _.includes(_.map($scope.koulutoimijalista, 'oid'), koulutoimija.oid);
        });

        $scope.loadingKoulutoimijat = false;
      }, Notifikaatiot.serverCb);
    }
  };

});
