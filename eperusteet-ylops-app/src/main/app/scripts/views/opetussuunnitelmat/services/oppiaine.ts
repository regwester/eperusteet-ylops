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
.service('OppiaineService', function (VuosiluokatService, $rootScope, MurupolkuData, $q, OppiaineCRUD, OppiaineenVlk,
  Notifikaatiot, VuosiluokkaCRUD) {
  var vlkTunniste = null;
  var oppiaineenVlk = null;
  var oppiaine = null;
  var opetussuunnitelma = null;

  function setup(ops, vlkId, oppiaineModel) {
    return $q((resolve, reject) => {
      opetussuunnitelma = ops;
      oppiaine = oppiaineModel;
      MurupolkuData.set('oppiaineNimi', oppiaine.nimi);
      var opsVlk = _.find(ops.vuosiluokkakokonaisuudet, function (vlk) {
        return '' + vlk.vuosiluokkakokonaisuus.id === vlkId;
      });
      vlkTunniste = opsVlk ? opsVlk.vuosiluokkakokonaisuus._tunniste : null;
      oppiaineenVlk = _.find(oppiaine.vuosiluokkakokonaisuudet, function (opVlk) {
        return opVlk._vuosiluokkakokonaisuus === vlkTunniste;
      });
      resolve();
    });
  }

  this.getParent = function() {
    return OppiaineCRUD.getParent({
      opsId: opetussuunnitelma.id,
      oppiaineId: oppiaine.id
    }).$promise;
  };
  this.revertToVersion = (version) => {
    return OppiaineCRUD.revertToVersion({
      opsId: opetussuunnitelma.id,
      oppiaineId: oppiaine.id,
      versio: version
    }, {}).$promise;
  };
  this.refresh = function (ops, oppiaineId, vlkId, versio) {
    return $q((resolve, reject) => {
      VuosiluokatService.getOppiaine(oppiaineId, versio).$promise
        .then(function (res) {
          setup(ops, vlkId, res).then(resolve);
          $rootScope.$broadcast('oppiainevlk:updated', oppiaineenVlk);
        })
        .catch(reject);
    });
  };
  this.getOpVlk = function () {
    return oppiaineenVlk;
  };
  this.getOppiaine = function () {
    return oppiaine;
  };
  this.saveVlk = (model) => $q((resolve) => {
    OppiaineenVlk.save({
      opsId: opetussuunnitelma.id,
      oppiaineId: oppiaine.id
    }, model, function () {
      Notifikaatiot.onnistui('tallennettu-ok');
      $rootScope.$broadcast('oppiaine:reload');
      resolve();
    }, Notifikaatiot.serverCb);
  });
  this.fetchVlk = function (vlkId, cb) {
    return OppiaineenVlk.get({
      opsId: opetussuunnitelma.id,
      oppiaineId: oppiaine.id,
      vlkId: vlkId
    }, cb, Notifikaatiot.serverCb).$promise;
  };
  this.saveVuosiluokka = function (model, cb) {
    VuosiluokkaCRUD.save({
      opsId: opetussuunnitelma.id,
      vlkId: oppiaineenVlk.id,
      oppiaineId: oppiaine.id
    }, model, function (res) {
      Notifikaatiot.onnistui('tallennettu-ok');
      cb(res);
    }, Notifikaatiot.serverCb);
  };
  this.saveValinnainenVuosiluokka = function (vlId, model, cb) {
    VuosiluokkaCRUD.saveValinnainen({
      opsId: opetussuunnitelma.id,
      vlkId: oppiaineenVlk.id,
      oppiaineId: oppiaine.id,
      vvlId: vlId
    }, model, function (res) {
      cb(res);
    }, Notifikaatiot.serverCb);
  };
  this.fetchVuosiluokka = function (vlId, cb) {
    VuosiluokkaCRUD.get({
      opsId: opetussuunnitelma.id,
      oppiaineId: oppiaine.id,
      vlkId: oppiaineenVlk.id,
      vlId: vlId
    }, cb, Notifikaatiot.serverCb);
  };
});
