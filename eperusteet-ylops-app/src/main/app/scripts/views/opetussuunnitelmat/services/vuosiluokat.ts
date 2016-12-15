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
.service('VuosiluokatService', function ($q, $state, OppiaineCRUD, Utils, OpsService,
  VuosiluokkakokonaisuusCRUD, OpetussuunnitelmaCRUD) {
  var opsId = null;
  var vuosiluokkakokonaisuudet = null;

  function promisify(data) {
    var deferred = $q.defer();
    deferred.resolve(data);
    return deferred.promise;
  }

  function setOps(ops) {
    opsId = ops.id;
  }

  function vlkSorter (item) {
    // TODO järjestys vuosiluokkaenumin mukaan nimen sijasta?
    return Utils.sort(item.vuosiluokkakokonaisuus);
  }

  function onCompletion(model, cb) {
    if (model.$promise) {
      model.$promise.then(function (res) {
        cb(res);
      });
    } else {
      cb(model);
    }
  }

  function fetch(ops) {
    var promise = $q.defer();
    function processVuosiluokkakokonaisuudet(model) {
      opsId = model.id;
      var vlkt = model.vuosiluokkakokonaisuudet;
      vuosiluokkakokonaisuudet = _.sortBy(vlkt, vlkSorter);
      promise.resolve(vuosiluokkakokonaisuudet);
    }
    var opsModel = (ops || OpsService.get());
    onCompletion(opsModel, processVuosiluokkakokonaisuudet);
    return promise.promise;
  }

  function getVuosiluokkakokonaisuudet(ops) {
    return fetch(ops);
  }

  function setVuosiluokkakokonaisuudet(vlkt) {
    vuosiluokkakokonaisuudet = vlkt;
  }

  function getVuosiluokkakokonaisuus(opetussuunnitelmaId, vlkId, successCb, errorCb) {
    return VuosiluokkakokonaisuusCRUD.get({opsId: opetussuunnitelmaId, vlkId: vlkId},
      successCb || angular.noop, errorCb || angular.noop);
  }

  function getVlkPeruste(opetussuunnitelmaId, vlkId, successCb, errorCb) {
    return VuosiluokkakokonaisuusCRUD.peruste({opsId: opetussuunnitelmaId, vlkId: vlkId},
      successCb || angular.noop, errorCb || angular.noop);
  }

  function getLaajaalaiset(opetussuunnitelmaId) {
    var deferred = $q.defer();
    OpetussuunnitelmaCRUD.laajaalaiset({opsId: opetussuunnitelmaId}, function (res) {
      deferred.resolve(res);
    }, function () {
      // Jos perustedataa ei löydy, voidaan silti jatkaa
      deferred.resolve([]);
    });
    return deferred.promise;
  }

  function getOppiaine(oppiaineId, versio) {
    if(versio){
      return OppiaineCRUD.getVersion({opsId: opsId || OpsService.getId(), versio: versio}, {id: oppiaineId});
    }
    return OppiaineCRUD.get({opsId: opsId || OpsService.getId()}, {id: oppiaineId});
  }

  function getPerusteOppiaine(oppiaineId) {
    var deferred = $q.defer();
    OppiaineCRUD.peruste({opsId: opsId || OpsService.getId()}, {id: oppiaineId}, function (res) {
      deferred.resolve(res);
    }, function () {
      // Jos perustedataa ei löydy, voidaan silti jatkaa
      deferred.resolve({eiPerustetta: true});
    });
    return deferred.promise;
  }

  function getVuosiluokka(vuosiluokkaId) {
    var vuosiluokka = {
      id: vuosiluokkaId,
      nimi: {fi: 'Vuosiluokka 1'}
    };
    return promisify(vuosiluokka);
  }

  const populateMenuItems = (arr, obj, oppiaineet, depth = 1) => {
    const alustaVlk = (oa) => {
      const oavlk = _.find(oa.vuosiluokkakokonaisuudet, _.equals(obj._tunniste, '_vuosiluokkakokonaisuus'));
      oa.$$oavlk = oavlk;
      if (oavlk) {
        oa.$$jnro = oavlk.jnro;
      }
    };

    const generateOppiaineItem = (oppiaine, vlk, depth) => {

      let piilotettu = false;
      const oaVlk = _.find(oppiaine.vuosiluokkakokonaisuudet, { _vuosiluokkakokonaisuus: vlk._tunniste });
      if (oaVlk && oaVlk.piilotettu) {
        piilotettu = true;
      }

      return {
        depth: depth || 1,
        label: oppiaine.nimi,
        id: oppiaine.id,
        vlkId: vlk.id,
        piilotettu: piilotettu,
        tyyppi: oppiaine.tyyppi,
        url: $state.href('root.opetussuunnitelmat.yksi.opetus.oppiaine',
            {vlkId: vlk.id, oppiaineId: oppiaine.id, oppiaineTyyppi: oppiaine.tyyppi, versio: null})
      };
    };

    _(oppiaineet)
      .each(alustaVlk)
      .filter((oa) => {
        return oa.$$oavlk || oa.koodiArvo === 'VK' || _.any(oa.oppimaarat, (om) => {
          return _.find(om.vuosiluokkakokonaisuudet, _.equals(obj._tunniste, '_vuosiluokkakokonaisuus'));
        });
      })
      .forEach((oa) => {
        oa.$$jnro = (!oa.$$jnro && !_.isEmpty(oa.oppimaarat)) ? _.first(oa.oppimaarat).$$jnro : oa.$$jnro;
        if(!oa.$$jnro && !_.isEmpty(oa.oppimaarat) && _.first(oa.oppimaarat).vuosiluokkakokonaisuudet){
          oa.$$jnro = _.first(_.first(oa.oppimaarat).vuosiluokkakokonaisuudet).jnro;
        }
      })
      .sortBy(Utils.sort)
      .sortBy('$$jnro')
      .map((oa) => {
        return [
          generateOppiaineItem(oa, obj, depth),
          _(oa.oppimaarat)
            .each(alustaVlk)
            .filter('$$oavlk')
            .sortBy(Utils.sort)
            .sortBy('$$jnro')
            .map(_.partial(generateOppiaineItem, _, obj, depth + 1))
            .value() || []];
      })
      .flatten(true)
      .each((oa) => arr.push(oa))
      .value();
  };

  function mapForMenu(ops) {
    if (!ops) {
      return;
    }

    var arr = [];

    _.each(vuosiluokkakokonaisuudet, function (vlk) {

      var obj = vlk.vuosiluokkakokonaisuus;

      // Vuosiluokka
      arr.push({
        label: obj.nimi,
        id: obj.id,
        url: $state.href('root.opetussuunnitelmat.yksi.opetus.vuosiluokkakokonaisuus', {vlkId: obj.id}),
      });

      // Vuosiluokan oppiaineet
      var oppiaineet = _.map(ops.oppiaineet, 'oppiaine');
      populateMenuItems(arr, obj, oppiaineet);

      // Vuosiluokan valinnaiset
      arr.push({
        depth: 1,
        label: 'valinnaiset-oppiaineet',
        id: 'valinnaiset',
        vlkId: vlk.vuosiluokkakokonaisuus.id,
        url: $state.href('root.opetussuunnitelmat.yksi.opetus.valinnaiset', {vlkId: vlk.vuosiluokkakokonaisuus.id})
      });
    });
    return arr;
  }

  this.setOps = setOps;
  this.fetch = fetch;
  this.getLaajaalaiset = getLaajaalaiset;
  this.getVuosiluokkakokonaisuudet = getVuosiluokkakokonaisuudet;
  this.setVuosiluokkakokonaisuudet = setVuosiluokkakokonaisuudet;
  this.getVuosiluokkakokonaisuus = getVuosiluokkakokonaisuus;
  this.getVlkPeruste = getVlkPeruste;
  this.getVuosiluokka = getVuosiluokka;
  this.getOppiaine = getOppiaine;
  this.getPerusteOppiaine = getPerusteOppiaine;
  this.mapForMenu = mapForMenu;
  this.fromEnum = function (vuosiluokkaEnum) {
    if (!vuosiluokkaEnum) {
      return undefined;
    }
    return parseInt(_.last(vuosiluokkaEnum.split('_')), 10);
  };
});
