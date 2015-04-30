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
/* global _, moment */

ylopsApp.service('Profiili', function($http, $q) {
    var info = {};

    var prom = $q.defer();
    info.fetchPromise = prom.promise;

    if (!info.$casFetched) {
      info.$casFetched = true;
      $http
        .get('/cas/me')
        .success(function(res) {
          if (res.oid) {
            info.oid = res.oid;
            info.lang = res.lang;
            info.groups = res.groups;
            prom.resolve();
          }
        })
        .error(function() {
           prom.reject();
        });
    }

    return {
      // Perustiedot
      oid: function() { return info.oid; },
      lang: function() { return info.lang; },
      groups: function() { return info.groups; },
      profiili: function() { return info; }
    };
  })