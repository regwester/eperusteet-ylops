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
.service('DummyVuosiluokat', function () {
  this.get = function () {
    return [
      {id: 'dummy1', nimi: {fi: 'Vuosiluokat 1-2'}},
      {id: 'dummy2', nimi: {fi: 'Vuosiluokat 3-6'}},
      {id: 'dummy3', nimi: {fi: 'Vuosiluokat 7-9'}},
    ];
  };
})

.service('DummyOppiaineet', function () {
  this.get = function () {
    return [
      {id: 'dummy4', nimi: {fi: 'Matematiikka'}},
      {id: 'dummy5', nimi: {fi: 'Äidinkieli ja kirjallisuus'}},
      {id: 'dummy6', nimi: {fi: 'Musiikki'}},
      {id: 'dummy7', nimi: {fi: 'Liikunta'}},
    ];
  };
})

.service('VuosiluokatService', function ($q, DummyVuosiluokat, DummyOppiaineet, $state) {
  var opsId = null;
  var vuosiluokat = null;

  function promisify(data) {
    var deferred = $q.defer();
    deferred.resolve(data);
    return deferred.promise;
  }

  function setOps(ops) {
    opsId = ops.id;
  }

  function fetch() {
    var vlk = DummyVuosiluokat.get();
    vlk[0].oppiaineet = DummyOppiaineet.get();
    var promise = promisify(vlk);
    promise.then(function (res) {
      vuosiluokat = res;
    });
    return promise;
  }

  function getVuosiluokat() {
    if (vuosiluokat === null) {
      return fetch();
    }
    return promisify(vuosiluokat);
  }

  function mapForMenu(data) {
    var arr = [];
    _.each(data, function (item) {
      var vlk = {
        label: item.nimi,
        id: item.id,
        url: $state.href('root.opetussuunnitelmat.yksi.vuosiluokkakokonaisuus', {vlkId: item.id}),
      };
      arr.push(vlk);
      _.each(item.oppiaineet, function (oppiaine) {
        arr.push({
          depth: 1,
          label: oppiaine.nimi,
          id: oppiaine.id,
          url: $state.href('root.opetussuunnitelmat.yksi.oppiaine', {vlkId: item.id, oppiaineId: oppiaine.id}),
        });
      });
    });
    return arr;
  }

  this.setOps = setOps;
  this.fetch = fetch;
  this.getVuosiluokat = getVuosiluokat;
  this.mapForMenu = mapForMenu;
});
