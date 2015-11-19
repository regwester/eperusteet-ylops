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
  $timeout, $q, $rootScope, OpetussuunnitelmaCRUD, Notifikaatiot, OpsService, Utils, KoodistoHaku, PeruskouluHaku,
  PeruskoulutoimijaHaku, kunnat, Kieli, OpetussuunnitelmaOikeudetService, Varmistusdialogi) {

  $scope.kielivalinnat = []; // Täytetään pohjan perusteella
  $scope.luonnissa = $stateParams.id === 'uusi';
  $scope.editableModel = _.clone($scope.model);
  if ($scope.luonnissa) {
    $scope.editableModel.julkaisukielet = [_.first($scope.kielivalinnat)];
    $scope.editableModel._pohja = $stateParams.pohjaId === '' ? null : $stateParams.pohjaId;
    $scope.editVuosiluokkakokonaisuudet = true;
  }else{
    $scope.editVuosiluokkakokonaisuudet = OpetussuunnitelmaOikeudetService.onkoOikeudet('pohja', 'luonti', true) ||
        OpetussuunnitelmaOikeudetService.onkoOikeudet('opetussuunnitelma', 'tilanvaihto', true);
  }

  $scope.$$isOps = true;
  $scope.editMode = false;
  $scope.loading = false;
  $scope.kuntalista = [];
  $scope.koulutoimijalista = [];
  $scope.koululista = [];
  $scope.eiKoulujaVaroitus = false;
  $scope.nimiOrder = function(vlk) {
    return Utils.sort(vlk.vuosiluokkakokonaisuus);
  };

  $scope.model.nimi.$$validointi = Kieli.validoi($scope.model.nimi);

  $scope.dateOptions = {
    'year-format': 'yy',
    'starting-day': 1
  };
  $scope.format = 'd.M.yyyy';
  $scope.kalenteriTilat = {};
  $scope.open = function ($event) {
    $event.stopPropagation();
    $event.preventDefault();
    $scope.kalenteriTilat.paatospaivamaaraButton = true;

  };

  $scope.hasRequiredFields = function () {
    var model = $scope.editableModel;
    var nimiOk = Utils.hasLocalizedText(model.nimi);
    var organisaatiotOk = model.kunnat && model.kunnat.length > 0 && model.koulutoimijat && model.koulutoimijat.length > 0;
    var julkaisukieletOk = _.any(_.values($scope.julkaisukielet));
    var vlkOk = (!$scope.luonnissa && !$scope.editVuosiluokkakokonaisuudet) || model.koulutustyyppi !== 'koulutustyyppi_16' ||
      _(model.vuosiluokkakokonaisuudet).filter({valittu: true}).size() > 0;
    return nimiOk && organisaatiotOk && julkaisukieletOk && vlkOk;
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

  function isValidKielivalinta(chosen, options) {
    return _.all(chosen, function (lang) {
      return _.contains(options, lang);
    });
  }

  function asetaKieletJaVlk(ops) {
    $scope.opsvuosiluokkakokonaisuudet = ops.vuosiluokkakokonaisuudet;
    $scope.editableModel.vuosiluokkakokonaisuudet = ops.vuosiluokkakokonaisuudet;
    $scope.kielivalinnat = ops.julkaisukielet;
    if (_.isEmpty($scope.editableModel.julkaisukielet) ||
        !isValidKielivalinta($scope.editableModel.julkaisukielet, $scope.kielivalinnat)) {
      $scope.editableModel.julkaisukielet = [_.first($scope.kielivalinnat)];
    }
    mapJulkaisukielet();
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
      $scope.editableModel.koulutustyyppi = res.koulutustyyppi;
      asetaKieletJaVlk(res);
    }, Notifikaatiot.serverCb);
  }

  function getPohja(koulutustyyppi, existingPohja) {
    if (_.isObject(existingPohja) && existingPohja.id) {
      OpetussuunnitelmaCRUD.get({opsId: existingPohja.id}, asetaKieletJaVlk);
    } else {
      OpetussuunnitelmaCRUD.query({tyyppi: 'pohja'}, function(pohjat) {
        var aktiivinenPohja = _.find(pohjat, {tila: 'valmis', koulutustyyppi: koulutustyyppi});
        if (aktiivinenPohja) {
          $scope.pohjaVaroitus = false;
          OpetussuunnitelmaCRUD.get({opsId: aktiivinenPohja.id}, asetaKieletJaVlk);
          $scope.avataInputs(koulutustyyppi);
        } else {
          $scope.pohjaVaroitus = true;
        }
      });
    }
  }

  $scope.$watch('editableModel.koulutustyyppi', function (value) {
    if (value) {
      getPohja(value, $scope.editableModel.pohja);
    }
  });

  $scope.kieliOrderFn = Kieli.orderFn;

  function fixTimefield(field) {
    if (typeof $scope.editableModel[field] === 'number') {
      $scope.editableModel[field] = new Date($scope.editableModel[field]);
    }
  }

  const fetch = (notify?) => $q((resolve, reject) => {
    OpsService.refetch((res) => {
      $scope.model = res;
      var vuosiluokkakokonaisuudet = _(res.vuosiluokkakokonaisuudet)
        .each((v) => {
          v.valittu = true;
        })
        .concat($scope.opsvuosiluokkakokonaisuudet )
        .value();

      vuosiluokkakokonaisuudet = _.uniq(vuosiluokkakokonaisuudet,function(c){
        return c.vuosiluokkakokonaisuus._tunniste;
      });

      $scope.editableModel = res;
      $scope.editableModel.vuosiluokkakokonaisuudet = vuosiluokkakokonaisuudet;
      $scope.editableModel.koulutoimijat = filterKoulutustoimija(res.organisaatiot);
      $scope.editableModel.koulut = filterOppilaitos(res.organisaatiot);
      fixTimefield('paatospaivamaara');
      if (notify) {
        $rootScope.$broadcast('rakenne:updated');
      }
      $scope.loading = false;
      resolve();
    });
  });

  var successCb = function (res) {
    Notifikaatiot.onnistui('tallennettu-ok');
    if ($scope.luonnissa) {
      $state.go('root.opetussuunnitelmat.yksi.sisalto', {id: res.id}, {reload: true});
    } else {
      fetch(true);
    }
  };

  const callbacks = {
    edit: function () {
      $scope.loading = true;
      return fetch();
    },
    asyncValidate: function(save){
      var muokattuVuosiluokkakokonaisuuksia = _.some(_.pluck($scope.editableModel.vuosiluokkakokonaisuudet, 'muutettu'));
      if (!$scope.luonnissa && muokattuVuosiluokkakokonaisuuksia) {
        Varmistusdialogi.dialogi({
          otsikko: 'vahvista-vuosiluokkakokonaisuudet-muokkaus-otsikko',
          teksti: 'vahvista-vuosiluokkakokonaisuudet-muokkaus-teksti'
        })(save);
      } else {
        save();
      }
      return $q.when();
    },
    validate: function () {
      return $scope.hasRequiredFields();
    },
    save: function () {
      $scope.editableModel.julkaisukielet = _($scope.julkaisukielet).keys().filter(function (koodi) {
        return $scope.julkaisukielet[koodi];
      }).value();
      $scope.editableModel.organisaatiot = $scope.editableModel.koulutoimijat.concat($scope.editableModel.koulut);
      delete $scope.editableModel.tekstit;
      delete $scope.editableModel.oppiaineet;

      $scope.editableModel.vuosiluokkakokonaisuudet = _.remove($scope.editableModel.vuosiluokkakokonaisuudet, {valittu: true});
      if ($scope.luonnissa) {
        OpetussuunnitelmaCRUD.save({}, $scope.editableModel, successCb, Notifikaatiot.serverCb);
      } else {
        $scope.editableModel.$save({}, successCb, Notifikaatiot.serverCb);
      }
      return $q.when();
    },
    cancel: function () {
      fetch();
      return $q.when();
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

  $scope.toggle = function(vkl){
    vkl.muutettu = (vkl.muutettu === undefined) ? true : !vkl.muutettu;
  };

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

    $scope.koulutustyyppiOnValittu = false;

    $scope.avataInputs = function(valittuKoulutustyyppi) {
      $scope.koulutustyyppiOnValittu = false;
      var validTyypit = ['koulutustyyppi_15', 'koulutustyyppi_16', 'koulutustyyppi_6', 'koulutustyyppi_2'];
      return _.includes(validTyypit, valittuKoulutustyyppi) ? ($scope.koulutustyyppiOnValittu = true) : ($scope.koulutustyyppiOnValittu = false);
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
