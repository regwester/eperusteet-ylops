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
.service('OpsListaService', function (OpetussuunnitelmaCRUD) {
  var cached = null;
  this.query = function (useCache) {
    if (useCache && cached) {
      return cached;
    }
    cached = OpetussuunnitelmaCRUD.query();
    return cached;
  };
})

.service('OpsService', function (OpetussuunnitelmaCRUD, Notifikaatiot, MurupolkuData) {
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
        MurupolkuData.set('opsNimi', angular.copy(res.nimi));
        ops = res;
        (cb || angular.noop)(ops);
      }, Notifikaatiot.serverCb);
      return deferred;
    }
  }

  function fetch(id) {
    opsId = id;

    if (opsId === 'uusi') {
      MurupolkuData.set('opsNimi', 'uusi');
      return uusi();
    }
    deferred = OpetussuunnitelmaCRUD.get({opsId: opsId}, function (res) {
      MurupolkuData.set('opsNimi', angular.copy(res.nimi));
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

  this.oppiaineIsKieli = function(oppiaine) {
    return _.isString(oppiaine.koodiArvo) && _.includes(['AI', 'VK', 'TK'], oppiaine.koodiArvo.toUpperCase());
  };

  this.fetch = fetch;
  this.fetchPohja = fetch;
  this.refetch = refetch;
  this.refetchPohja = refetch;
  this.get = get;
  this.getPohja = get;
  this.getId = function () {
    return opsId;
  };
})

  .service('OpetussuunnitelmaOikeudetService', function ($rootScope,
                                                         $stateParams,
                                                         OpetussuunnitelmaOikeudet,
                                                         OpsService,
                                                         Notifikaatiot) {
    var kayttajaOikeudet = null;
    var opsOikeudet;

    function fetch(stateParams) {
      if (stateParams.id === 'uusi') {
        return query();
      }

      var deferred = OpetussuunnitelmaOikeudet.get({opsId: stateParams.id}, function (res) {
        opsOikeudet = res;
      }, Notifikaatiot.serverCb);
      return deferred.$promise;
    }

    function get() {
      return _.clone(opsOikeudet);
    }

    function query() {
      if (!kayttajaOikeudet) {
        kayttajaOikeudet = OpetussuunnitelmaOikeudet.query();
      }
      return kayttajaOikeudet.$promise;
    }

    function onkoOikeudet(target, permission, kayttaja) {
      var oikeudet = kayttaja ? kayttajaOikeudet : opsOikeudet;
      return oikeudet ? _.contains(oikeudet[target], permission) : false;
    }

    return {
      fetch: fetch,
      get: get,
      query: query,
      onkoOikeudet: onkoOikeudet
    };
  });
