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
      kuvaus: {}
    };
  }

  function refetch() {
    if (opsId !== 'uusi') {
      return OpetussuunnitelmaCRUD.get({opsId: opsId}, function (res) {
        ops = res;
      }, Notifikaatiot.serverCb);
    }
  }

  function fetch(id) {
    opsId = id;
    return opsId === 'uusi' ? uusi() : OpetussuunnitelmaCRUD.get({opsId: opsId}, function (res) {
      ops = res;
    }, Notifikaatiot.serverCb);
  }

  this.fetch = fetch;
  this.refetch = refetch;
});