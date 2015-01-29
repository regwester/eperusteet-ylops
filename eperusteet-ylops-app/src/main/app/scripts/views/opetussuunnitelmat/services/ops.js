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
.service('OpsService', function (OpetussuunnitelmaCRUD, Notifikaatiot) {
  var opsId = null;
  var ops = null;
  var deferred = null;

  function uusi() {
    return {
      nimi: {},
      kuvaus: {},
      kunnat: [],
      koulut: [],
      tekstit: {lapset: []}
    };
  }

  function refetch(cb) {
    if (opsId !== 'uusi') {
      deferred = OpetussuunnitelmaCRUD.get({opsId: opsId}, function (res) {
        ops = res;
        (cb || angular.noop)(res);
      }, Notifikaatiot.serverCb);
      return deferred;
    }
  }

  function fetch(id) {
    opsId = id;

    if (opsId === 'uusi') {
      return uusi();
    }
    deferred = OpetussuunnitelmaCRUD.get({opsId: opsId}, function (res) {
      ops = res;
    }, Notifikaatiot.serverCb);
    return deferred;
  }

  function get(validateId) {
    if (validateId && validateId !== opsId) {
      opsId = null;
      ops = null;
      return null;
    }
    return deferred;
  }

  this.fetch = fetch;
  this.fetchPohja = fetch;
  this.refetch = refetch;
  this.refetchPohja = refetch;
  this.get = get;
  this.getPohja = get;
});
