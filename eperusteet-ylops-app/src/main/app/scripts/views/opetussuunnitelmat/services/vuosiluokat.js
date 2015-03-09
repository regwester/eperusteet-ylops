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
    return OpetussuunnitelmaCRUD.laajaalaiset({opsId: opetussuunnitelmaId});
  }

  function getOppiaine(oppiaineId) {
    return OppiaineCRUD.get({opsId: opsId || OpsService.getId()}, {id: oppiaineId});
  }

  function getPerusteOppiaine(oppiaineId) {
    return OppiaineCRUD.peruste({opsId: opsId || OpsService.getId()}, {id: oppiaineId});
  }

  function getVuosiluokka(vuosiluokkaId) {
    var vuosiluokka = {
      id: vuosiluokkaId,
      nimi: {fi: 'Vuosiluokka 1'}
    };
    return promisify(vuosiluokka);
  }

  function generateOppiaineItem(oppiaine, vlk, depth) {
    return {
      depth: depth || 1,
      label: oppiaine.nimi,
      id: oppiaine.id,
      vlkId: vlk.id,
      url: $state.href('root.opetussuunnitelmat.yksi.oppiaine', {vlkId: vlk.id, oppiaineId: oppiaine.id}),
    };
  }

  function getTunnisteet(vlkt) {
    return _.map(vlkt, function (item) {
      return item._vuosiluokkakokonaisuus;
    });
  }

  function mapForMenu(ops) {
    var arr = [];
    if (ops) {
      _.each(vuosiluokkakokonaisuudet, function (vlk) {
        var obj = vlk.vuosiluokkakokonaisuus;
        var item = {
          label: obj.nimi,
          id: obj.id,
          url: $state.href('root.opetussuunnitelmat.yksi.vuosiluokkakokonaisuus', {vlkId: obj.id}),
        };
        arr.push(item);

        var sorted = _.sortBy(ops.oppiaineet, function (item) {
          return Utils.sort(item.oppiaine);
        });
        _.each(sorted, function (oppiaine) {
          var tunnisteet = getTunnisteet(oppiaine.oppiaine.vuosiluokkakokonaisuudet);
          var parentPushed = false;
          if (_.indexOf(tunnisteet, obj._tunniste) > -1) {
            arr.push(generateOppiaineItem(oppiaine.oppiaine, obj));
            parentPushed = true;
          }
          if (oppiaine.oppiaine.koosteinen) {
            var oppimaarat = _.sortBy(oppiaine.oppiaine.oppimaarat, Utils.sort);
            _.each(oppimaarat, function (oppimaara) {
              tunnisteet = getTunnisteet(oppimaara.vuosiluokkakokonaisuudet);
              if (_.indexOf(tunnisteet, obj._tunniste) > -1) {
                if (!parentPushed) {
                  arr.push(generateOppiaineItem(oppiaine.oppiaine, obj));
                  parentPushed = true;
                }
                arr.push(generateOppiaineItem(oppimaara, obj, 2));
              }
            });
          }
        });
        // TODO: enable
        /*arr.push({
          depth: 1,
          label: 'valinnaiset-oppiaineet',
          id: 'valinnaiset',
          vlkId: vlk.vuosiluokkakokonaisuus.id,
          url: $state.href('root.opetussuunnitelmat.yksi.valinnaiset', {vlkId: vlk.vuosiluokkakokonaisuus.id})
        });*/
      });
    }
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
    return parseInt(_.last(vuosiluokkaEnum.split('_')), 10);
  };
});
