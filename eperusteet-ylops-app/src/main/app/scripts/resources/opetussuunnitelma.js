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
  .service('YlopsResources', function (SERVICE_LOC) {
    this.OPS = SERVICE_LOC + '/opetussuunnitelmat/:opsId';
    this.OPPIAINE = this.OPS + '/oppiaineet/:oppiaineId';
  })

  .factory('OpetussuunnitelmaCRUD', function ($resource, YlopsResources) {
    return $resource(YlopsResources.OPS, {
      opsId: '@id'
    });
  })

  .factory('OpetussuunnitelmanTekstit', function ($resource, YlopsResources) {
    return $resource(YlopsResources.OPS + '/tekstit/:viiteId', {
      viiteId: '@id'
    }, {
      setChild: {method: 'POST', url: YlopsResources.OPS + '/tekstit/:parentId/lapsi/:childId'}
    });
  })

  .factory('OppiaineCRUD', function ($resource, YlopsResources) {
    return $resource(YlopsResources.OPPIAINE, {
      oppiaineId: '@id'
    });
  });
