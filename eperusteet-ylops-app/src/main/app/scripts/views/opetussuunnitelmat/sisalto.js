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
.service('TekstikappaleOps', function (OpetussuunnitelmanTekstit, Notifikaatiot, Algoritmit,
  Varmistusdialogi, Kaanna) {
  function mapSisalto(root) {
    return {
      id: root.id,
      lapset: _.map(root.lapset, mapSisalto)
    };
  }

  function saveRakenne(model, cb) {
    var postdata = mapSisalto(model.tekstit);
    OpetussuunnitelmanTekstit.save({
      opsId: model.id,
      viiteId: model.tekstit.id
    }, postdata, function () {
      Notifikaatiot.onnistui('tallennettu-ok');
      cb();
    }, Notifikaatiot.serverCb);
  }

  function add(model, osio, opsId, uusi, cb) {
    var newNode = {tekstiKappale: angular.copy(uusi), lapset: []};
    model[osio].lapset.push(newNode);
    var params = {opsId: opsId};
    OpetussuunnitelmanTekstit.setChild(_.extend({parentId: model[osio].id}, params), newNode, function (res) {
      var otsikko = _.cloneDeep(newNode.tekstiKappale.nimi);
      newNode.id = res.id;
      newNode.omistussuhde = res.omistussuhde;
      newNode.tekstiKappale = res.tekstiKappale;
      res.tekstiKappale.nimi = otsikko;
      // TODO: lapsi-APIa käyttämällä ei tekstikappale tallennu samalla pyynnöllä
      OpetussuunnitelmanTekstit.save(params, res, function () {
        Notifikaatiot.onnistui('tallennettu-ok');
        cb();
      }, Notifikaatiot.serverCb);
    }, Notifikaatiot.serverCb);
  }

  function deleteKappale(model, osio, opsId, kappale) {
    var params = {opsId: opsId};
    OpetussuunnitelmanTekstit.delete(params, kappale, function () {
      var foundIndex = null, foundList = null;
      Algoritmit.traverse(model[osio], 'lapset', function (lapsi, depth, index, arr) {
        if (lapsi === kappale) {
          foundIndex = index;
          foundList = arr;
          return true;
        }
      });
      if (foundList) {
        foundList.splice(foundIndex, 1);
      }
      Notifikaatiot.onnistui('poisto-onnistui');
    }, Notifikaatiot.serverCb);
  }

  this.saveRakenne = saveRakenne;
  this.add = add;
  this.delete = deleteKappale;
  this.varmistusdialogi = function (nimi, successCb, failureCb) {
    Varmistusdialogi.dialogi({
      otsikko: 'varmista-poisto',
      teksti: Kaanna.kaanna('poista-tekstikappale-teksti', {nimi: Kaanna.kaanna(nimi)}),
      primaryBtn: 'poista',
      successCb: successCb,
      failureCb: failureCb || angular.noop
    })();
  };
})

.controller('OpetussuunnitelmaSisaltoController', function ($scope, OpetussuunnitelmanTekstit,
  Notifikaatiot, opsService, opsModel, $rootScope, $stateParams, TekstikappaleOps, Utils) {
  $scope.rakenneEdit = {jarjestaminen: false, lahtokohdat: false};
  $scope.adding = {jarjestaminen: false, lahtokohdat: false};
  $scope.uusi = {nimi: {}};

  $scope.model = opsService.get($stateParams.id) || opsModel;

  function mapModel() {
    $scope.model.jarjestaminen = $scope.model.tekstit ? $scope.model.tekstit.lapset[0] : [];
    $scope.model.lahtokohdat = $scope.model.tekstit ? $scope.model.tekstit.lapset[1] : [];
  }

  $scope.isAdding = function () {
    return _.any(_.values($scope.adding));
  };

  $scope.hasText = function () {
    return Utils.hasLocalizedText($scope.uusi.nimi);
  };

  $scope.canRemove = function (kappale) {
    return kappale.omistussuhde === 'oma';
  };

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

  var original = null;
  $scope.kappaleEdit = null;

  function stopEvent(event) {
    event.preventDefault();
    event.stopPropagation();
  }

  $scope.rakenne = {
    add: function (osio, event) {
      stopEvent(event);
      $scope.adding[osio] = true;
    },
    doAdd: function (osio) {
      TekstikappaleOps.add($scope.model, osio, $stateParams.id, $scope.uusi, function () {
        $scope.uusi = {nimi: {}};
        mapModel();
      });
    },
    cancelAdd: function (osio) {
      $scope.adding[osio] = false;
      $scope.uusi = {nimi: {}};
    },
    deleteKappale: function (osio, item) {
      TekstikappaleOps.varmistusdialogi(item.tekstiKappale.nimi, function () {
        TekstikappaleOps.delete($scope.model, osio, $stateParams.id, item);
      });
    },
    editTitle: function (osio, kappale) {
      $scope.kappaleEdit = kappale;
      kappale.$original = _.cloneDeep(kappale.tekstiKappale);
    },
    cancelTitle: function (kappale) {
      $scope.kappaleEdit = null;
      kappale.tekstiKappale = _.cloneDeep(kappale.$original);
      delete kappale.$original;
    },
    saveTitle: function (kappale) {
      $scope.kappaleEdit = null;
      var params = {opsId: $stateParams.id};
      OpetussuunnitelmanTekstit.save(params, _.omit(kappale, 'lapset'), function () {
        Notifikaatiot.onnistui('tallennettu-ok');
        delete kappale.$original;
      }, Notifikaatiot.serverCb);
    },
    edit: function (osio, event) {
      stopEvent(event);
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
      TekstikappaleOps.saveRakenne($scope.model, function () {
        $scope.rakenneEdit[osio] = false;
        fetch(angular.noop, true);
      });
    }
  };
});
