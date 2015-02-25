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


  $scope.hasRequiredFields = function () {
    var model = $scope.editableModel;
    return Utils.hasLocalizedText(model.nimi) &&
           model.kuntaUrit && model.kuntaUrit.length > 0 &&
           model.koulutoimijaOidit && model.koulutoimijaOidit.length > 0 &&
           _.any(_.values($scope.julkaisukielet));
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
      $scope.editableModel.kuntaUrit = _.map(res.kunnat, 'koodiUri');

      $scope.editableModel.koulutoimijat = filterKoulutustoimija(res.organisaatiot);
      $scope.editableModel.koulutoimijaOidit = _.map($scope.editableModel.koulutoimijat, 'oid');

      $scope.editableModel.koulut = filterOppilaitos(res.organisaatiot);
      $scope.editableModel.kouluOidit = _.map(res.koulut, 'oid');
      $scope.haeKoulutoimijat($scope.editableModel.kuntaUrit);
      $scope.haeKoulut($scope.editableModel.koulutoimijaOidit);
    }, Notifikaatiot.serverCb);
  }

  $scope.kieliOrderFn = Kieli.orderFn;

  function fetch(notify) {
    OpsService.refetch(function (res) {
      $scope.model = res;
      $scope.editableModel = res;
      $scope.editableModel.kuntaUrit = _.map(res.kunnat, 'koodiUri');

      $scope.editableModel.koulutoimijat = filterKoulutustoimija(res.organisaatiot);
      $scope.editableModel.koulutoimijaOidit = _.map($scope.editableModel.koulutoimijat, 'oid');

      $scope.editableModel.koulut = filterOppilaitos(res.organisaatiot);
      $scope.editableModel.kouluOidit = _.map($scope.editableModel.koulut, 'oid');
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

  function mapKouluKunta() {
    $scope.editableModel.koulut = _.map($scope.editableModel.kouluOidit, function (oid) {
      return _.find($scope.koululista, function (koulu) {
        return koulu.oid === oid;
      });
    });
    $scope.editableModel.koulutoimijat = _.map($scope.editableModel.koulutoimijaOidit, function (oid) {
      return _.find($scope.koulutoimijalista, function (koulutoimija) {
        return koulutoimija.oid === oid;
      });
    });
    $scope.editableModel.kunnat = _.map($scope.editableModel.kuntaUrit, function (uri) {
      return _.find($scope.kuntalista, function (kunta) {
        return kunta.koodiUri === uri;
      });
    });
    delete $scope.editableModel.kouluOidit;
    delete $scope.editableModel.koulutoimijaOidit;
    delete $scope.editableModel.kuntaUrit;
  }

  var callbacks = {
    edit: function () {
      $scope.loading = true;
      fetch();
    },
    validate: function () {
      return $scope.hasRequiredFields();
    },
    save: function () {
      mapKouluKunta();
      $scope.editableModel.julkaisukielet = _($scope.julkaisukielet).keys().filter(function (koodi) {
        return $scope.julkaisukielet[koodi];
      }).value();
      $scope.editableModel.organisaatiot = $scope.editableModel.koulutoimijat.concat($scope.editableModel.koulut);
      if ($scope.luonnissa) {
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
    $scope.eiKoulujaVaroitus = _.isArray($scope.editableModel.kuntaUrit) &&
      $scope.editableModel.kuntaUrit.length === 1 &&
      _.isArray($scope.koululista) && $scope.koululista.length === 0;
  }

  $scope.$watch('editableModel.kuntaUrit', function () {
    $scope.haeKoulutoimijat();
  });

  $scope.$watch('editableModel.koulutoimijaOidit', function () {
    $scope.haeKoulut();
    updateKouluVaroitus();
  });

  $scope.$watch('koululista', function () {
    if ($scope.editableModel.koulut) {
      $scope.editableModel.kouluOidit = _.map($scope.editableModel.koulut, 'oid');
    }
    updateKouluVaroitus();
  });

  $scope.$watch('editableModel.kouluOidit', function (value) {
    // ui-select tries to set selected values too eagerly
    if (value && value.length > 0 && value[0] === undefined) {
      $scope.editableModel.kouluOidit = _.map($scope.editableModel.koulut, 'oid');
    }
  });

  function mapJulkaisukielet() {
    $scope.julkaisukielet = _.zipObject($scope.kielivalinnat, _.map($scope.kielivalinnat, function (kieli) {
      return _.indexOf($scope.editableModel.julkaisukielet, kieli) > -1;
    }));
  }

  $scope.$watch('editableModel.julkaisukielet', mapJulkaisukielet);

  $scope.haeKoulut = function (koulutoimijaOidit) {
    $scope.loadingKoulut = true;
    var koulutoimijat = koulutoimijaOidit || $scope.editableModel.koulutoimijaOidit;
    if (!($scope.editMode || $scope.luonnissa) || !koulutoimijat) {
      $scope.loadingKoulut = false;
      return;
    }
    if (koulutoimijat.length === 0) {
      $scope.loadingKoulut = false;
      $scope.editableModel.kouluOidit = [];
    } else if (koulutoimijat.length === 1) {
      var koulutoimija = koulutoimijat[0];
      PeruskouluHaku.get({ oid: koulutoimija }, function(res) {
        $scope.koululista = _.sortBy(res, Utils.sort);
        $scope.loadingKoulut = false;
      }, Notifikaatiot.serverCb);
    } else {
      $scope.loadingKoulut = false;
      $scope.editableModel.kouluOidit = [];
      $scope.koululista = [];
    }
  };

  $scope.haeKoulutoimijat = function (kuntaUrit) {
    $scope.loadingKoulutoimijat = true;
    var kunnat = kuntaUrit ? kuntaUrit : $scope.editableModel.kuntaUrit;
    if (!($scope.editMode || $scope.luonnissa) || !kunnat) {
      $scope.loadingKoulutoimijat = false;
      return;
    }
    if (kunnat.length === 0) {
      $scope.loadingKoulutoimijat = false;
      $scope.editableModel.koulutoimijaOidit = [];
    } else {
      PeruskoulutoimijaHaku.get({ kuntaUri: kunnat }, function(res) {
        $scope.koulutoimijalista = _.sortBy(res, Utils.sort);
        $scope.loadingKoulutoimijat = false;
      }, Notifikaatiot.serverCb);
    }
  };

});
