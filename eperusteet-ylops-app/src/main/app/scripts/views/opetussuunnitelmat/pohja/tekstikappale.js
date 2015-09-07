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
.controller('PohjaTekstikappaleController', function ($scope, tekstikappaleModel, Editointikontrollit,
  Notifikaatiot, TekstikappaleOps, $timeout, $state, $stateParams, OhjeCRUD, OpetussuunnitelmanTekstit,
  Utils, $rootScope, MurupolkuData, OpsService) {

  $scope.pohjaId = $stateParams.pohjaId;
  $scope.model = tekstikappaleModel;
  MurupolkuData.set('tekstiNimi', $scope.model.tekstiKappale.nimi);
  $scope.ohje = {};
  $scope.perusteteksti = {};
  $scope.options = {tekstiCollapsed: true};
  var TYYPIT = ['ohje', 'perusteteksti'];
  var originals = {
    teksti: _.cloneDeep($scope.model),
    ohje: null,
    perusteteksti: null
  };

  $scope.isEmpty = function (model) {
    return _.isEmpty(model);
  };

  $scope.edit = function () {
    Editointikontrollit.startEditing();
  };

  function saveOriginal(key, teksti) {
    originals[key] = _.cloneDeep(teksti);
  }

  $scope.delete = function () {
    TekstikappaleOps.varmistusdialogi($scope.model.tekstiKappale.nimi, function () {
      $scope.model.$delete({opsId: $stateParams.pohjaId}, function () {
        Notifikaatiot.onnistui('poisto-onnistui');
        OpsService.refetch(function () {
          $rootScope.$broadcast('rakenne:updated');
        });
        $timeout(function () {
          $state.go('root.pohjat.yksi.sisalto');
        });
      }, Notifikaatiot.serverCb);
    });
  };

  function fetchOhje(model) {
    OhjeCRUD.forTekstikappale({uuid: model.tekstiKappale.tunniste}, function (ohje) {
      _.each(TYYPIT, function (tyyppi) {
        var found = _.find(ohje, function (item) {
          return item.tyyppi === tyyppi;
        });
        if (found) {
          $scope[tyyppi] = found;
          saveOriginal(tyyppi, $scope[tyyppi]);
        }
      });
    });
  }

  function fetch(initial) {
    if (initial) {
      fetchOhje($scope.model);
      return;
    }
    OpetussuunnitelmanTekstit.get({
      opsId: $stateParams.pohjaId,
      viiteId: $stateParams.tekstikappaleId
    }, function (res) {
      $scope.model = res;
      MurupolkuData.set('tekstiNimi', res.tekstiKappale.nimi);
      saveOriginal('teksti', res);
      fetchOhje(res);
      $scope.$$muokkaustiedot = {
        luotu: $scope.model.tekstiKappale.luotu,
        muokattu: $scope.model.tekstiKappale.muokattu,
        muokkaajaOid: $scope.model.tekstiKappale.muokkaaja
      };
    }, Notifikaatiot.serverCb);
  }
  fetch(true);

  var successCb = function (res) {
    $scope.model = res;
    saveOriginal('teksti', res);
    Notifikaatiot.onnistui('tallennettu-ok');
    if ($stateParams.tekstikappaleId === 'uusi') {
      $state.go($state.current.name, {tekstikappaleId: res.id}, {reload: true});
    }
  };

  function saveOhje(tyyppi) {
    if (!$scope[tyyppi].tyyppi) {
      $scope[tyyppi].tyyppi = tyyppi;
    }
    if (!$scope[tyyppi].$save) {
      $scope[tyyppi].kohde = $scope.model.tekstiKappale.tunniste;
      OhjeCRUD.save({}, $scope[tyyppi], function (res) {
        $scope[tyyppi] = res;
        saveOriginal(res.tyyppi, res);
      }, Notifikaatiot.serverCb);
    } else {
      $scope[tyyppi].$save({}, function (res) {
        saveOriginal(res.tyyppi, res);
      }, Notifikaatiot.serverCb);
    }
  }

  function checkChanges() {
    var teksti = $scope.model.tekstiKappale;
    var cmp = Utils.compareLocalizedText;
    var tekstiaMuutettu = !cmp(teksti.nimi, originals.teksti.tekstiKappale.nimi) ||
      !cmp(teksti.teksti, originals.teksti.tekstiKappale.teksti);
    var changes = {
      teksti: {
        changed: tekstiaMuutettu || $scope.model.pakollinen !== originals.teksti.pakollinen
      }
    };
    _.each(TYYPIT, function (tyyppi) {
      var lisatty = _.isEmpty(originals[tyyppi]) && Utils.hasLocalizedText($scope[tyyppi].teksti);
      changes[tyyppi] = {
        changed: lisatty ||
                 (!_.isEmpty(originals[tyyppi]) && !cmp($scope[tyyppi].teksti, originals[tyyppi].teksti)),
        deleted: !_.isEmpty(originals[tyyppi]) && !Utils.hasLocalizedText($scope[tyyppi].teksti) &&
                 Utils.hasLocalizedText(originals[tyyppi].teksti)
      };
    });
    return changes;
  }

  var callbacks = {
    edit: function () {
      fetch();
    },
    save: function () {
      // Päivitä modelit ckeditorilta ennen muutosten tarkastelua
      $rootScope.$broadcast('notifyCKEditor');
      var changed = checkChanges();
      if (changed.teksti.changed) {
        var params = {opsId: $stateParams.pohjaId};
        // Pelkkää tekstikappaletta muokattaessa lapset-kenttä tulee jättää pois
        _.omit($scope.model, 'lapset').$save(params, successCb, Notifikaatiot.serverCb);
      }
      _.each(TYYPIT, function (tyyppi) {
        if (changed[tyyppi].deleted) {
          $scope.ohjeOps.delete(null, $scope[tyyppi]);
        } else if (changed[tyyppi].changed) {
          saveOhje(tyyppi);
        }
      });
    },
    cancel: function () {
      fetch();
    },
    notify: function (mode) {
      $scope.editMode = mode;
    }
  };
  Editointikontrollit.registerCallback(callbacks);

  $scope.ohjeOps = {
    delete: function ($event, ohje) {
      if ($event) {
        $event.stopPropagation();
      }
      var tyyppi = ohje.tyyppi;
      ohje.$delete(function () {
        $scope[tyyppi] = {};
        saveOriginal(tyyppi, null);
      });
    }
  };

});
