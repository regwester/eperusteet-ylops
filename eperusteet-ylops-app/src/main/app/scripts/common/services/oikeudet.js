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
.service('Oikeudet', function($http, $q, $rootScope, $window) {
  var isVirkailija = false;
  var info = {};
  var GROUP = 'APP_EPERUSTEET_YLOPS_CRUD_1.2.246.562.10.00000000001';

  this.setVirkailija = function (value) {
    isVirkailija = !!value;
  };

  this.isVirkailija = function () {
    return isVirkailija;
  };

  this.isLocal = function () {
    return $window.location.host.indexOf('localhost') === 0;
  };

  function setOikeudet() {
    isVirkailija = _.contains(info.groups, GROUP);
  }

  function getCasTiedot() {
    var deferred = $q.defer();
    if (!info.$casFetched) {
      info.$casFetched = true;
      $http.get('/cas/me').success(function(res) {
        if (res.oid) {
          info.oid = res.oid;
          info.lang = res.lang;
          info.groups = res.groups;
          setOikeudet();
        }
        deferred.resolve(res);
        $rootScope.$broadcast('fetched:casTiedot');
      }).error(function() {
        deferred.resolve({});
        $rootScope.$broadcast('fetched:casTiedot');
      });
    } else {
      deferred.resolve(info);
    }
    return deferred.promise;
  }

  this.getCasTiedot = getCasTiedot;

});
