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
  .service('UlkopuolisetResources', function (SERVICE_LOC) {
    this.ULKOPUOLISET = SERVICE_LOC + '/ulkopuoliset/';
  })
  .factory('EperusteetTiedotteet', function(UlkopuolisetResources, $resource) {
    return $resource(UlkopuolisetResources.ULKOPUOLISET + 'tiedotteet', {
      get: { isArray: true }
    });
  })
  .factory('EperusteetPerusopetus', function(UlkopuolisetResources, $resource) {
    return $resource(UlkopuolisetResources.ULKOPUOLISET + 'perusopetusperusteet/:perusteId', {
      perusteId: '@id'
    });
  })
  .factory('KoodistoHaku', function(UlkopuolisetResources, $resource) {
    return $resource(UlkopuolisetResources.ULKOPUOLISET + 'koodisto/:koodistoUri', {
      koodistoUri: '@koodistoUri'
    }, {
      get: {
        method: 'GET',
        isArray: true,
        cache: true
      }
    });
  })
  .factory('PeruskouluHaku', function(UlkopuolisetResources, $resource) {
    return $resource(UlkopuolisetResources.ULKOPUOLISET + 'organisaatiot/peruskoulut/oid/:oid', {
      oid: '@oid'
    }, {
      get: {
        method: 'GET',
        isArray: true,
        cache: true
      }
    });
  })
  .factory('PeruskoulutoimijaHaku', function(UlkopuolisetResources, $resource) {
    return $resource(UlkopuolisetResources.ULKOPUOLISET + 'organisaatiot/peruskoulutoimijat/:kuntaUri', {
      kuntaUri: '@kuntaUri'
    }, {
      get: {
        method: 'GET',
        isArray: true,
        cache: true
      }
    });
  })
  .factory('Organisaatioryhmat', function(UlkopuolisetResources, $resource) {
    return $resource(UlkopuolisetResources.ULKOPUOLISET + 'organisaatioryhmat/', {
      oid: '@oid'
    }, {
      get: {
        method: 'GET',
        isArray: true
      },
      yksi: {
        method: 'GET',
        isArray: false,
        url: UlkopuolisetResources.ULKOPUOLISET + 'organisaatioryhmat/:oid'
      }
    });
  });
