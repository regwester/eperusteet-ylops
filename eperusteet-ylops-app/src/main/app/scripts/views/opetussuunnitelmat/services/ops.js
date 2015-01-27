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
      return OpetussuunnitelmaCRUD.get({opsId: opsId}, function (res) {
        ops = res;
        (cb || angular.noop)(res);
      }, Notifikaatiot.serverCb);
    }
  }

  function fetch(id) {
    opsId = id;

    if (('' + opsId).substr(0, 5) === 'dummy') {
      // TODO remove dummy data
      return {
        $resolved: true,
        tila: 'luonnos',
        nimi: {fi: 'Opetussuunnitelmapohja'},
        tekstit: {
          lapset: [
            {tekstiKappale: {nimi: {fi: 'Opetuksen järjestäminen'}}, lapset: []},
            {tekstiKappale: {nimi: {fi: 'Opetuksen toteuttamisen lähtökohdat'}}, lapset: []}
          ]
        }
      };
    }

    return opsId === 'uusi' ? uusi() : OpetussuunnitelmaCRUD.get({opsId: opsId}, function (res) {
      ops = res;
    }, Notifikaatiot.serverCb);
  }

  function get() {
    return ops;
  }

  this.fetch = fetch;
  this.fetchPohja = fetch;
  this.refetch = refetch;
  this.get = get;
});
