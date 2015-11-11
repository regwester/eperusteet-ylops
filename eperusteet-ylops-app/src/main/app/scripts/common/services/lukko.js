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
.directive('lukko', function() {
  return {
    template: '<div class="lukko" ng-class="{small: size === \'small\'}" ng-attr-title="{{tip}}"><div class="inner1"></div><div class="inner2"></div></div>',
    restrict: 'AE',
    scope: {
      tip: '=',
      size: '@'
    }
  };
})

.controller('LukittuSisaltoMuuttunutModalController', function($scope, $modalInstance) {
  $scope.$on('$stateChangeSuccess', function() {
    $modalInstance.dismiss();
  });
})

// FIXME: Korjaa käyttämään promiseja
.service('Lukko', function(OpetussuunnitelmanTekstitLukko, OpetussuunnitelmanTekstitRakenneLukko,
                           OppiaineenVuosiluokkakokonaisuusLukko,
                           Notifikaatiot, $state, Editointikontrollit, $modal, Kaanna, $rootScope) {
  var etag = null;
  $rootScope.$on('$stateChangeSuccess', function() {
    etag = null;
  });

  function muuttunutModal(resHeaders) {
    $modal.open({
      templateUrl: 'views/common/modals/sisaltomuuttunut.html',
      controller: 'LukittuSisaltoMuuttunutModalController'
    }).result.then(function() {
      etag = resHeaders.etag;
    }, Editointikontrollit.cancelEditing);
  }

  function doLock(resource, params, cb, failCb) {
    cb = cb || angular.noop;
    failCb = failCb || angular.noop;

    resource.save(params, {}, function(res, headers) {
      if (etag && headers().etag !== etag && Editointikontrollit.getEditMode()) {
        muuttunutModal(headers());
      } else {
        etag = headers().etag;
        cb(res);
      }
    }, function(err) {
      Notifikaatiot.serverLukitus(err);
      failCb();
    });
  }

  function resourceFromState() {
    var resource = null;

    if (_.endsWith($state.current.name, '.tekstikappale')) {
      resource = OpetussuunnitelmanTekstitLukko;
    } else if (_.endsWith($state.current.name, 'yksi.sisalto')) {
      resource = OpetussuunnitelmanTekstitRakenneLukko;
    } else if (_.endsWith($state.current.name, 'yksi.opetus.oppiaine.oppiaine') ||
               _.endsWith($state.current.name, 'yksi.uusioppiaine')) {
      resource = OppiaineenVuosiluokkakokonaisuusLukko;
    }

    if (!resource) {
      console.warn('Ei lukkoresurssia!');
    }
    return resource;
  }

  function isLukittu(res) {
    return res.haltijaOid && new Date() <= new Date(res.vanhentuu) && !res.oma;
  }

  function virheIlmo(res) {
    return Kaanna.kaanna('lukitus-kayttajalla', { user: res.haltijaNimi || res.haltijaOid });
  }

  function checkLock(scope, params) {
    var okCb = function(res) {
      scope.lukkotiedot = res;
      if (isLukittu(res)) {
        scope.lukkotiedot.lukittu = true;
        scope.lukkotiedot.tip = virheIlmo(res);
      } else {
        scope.lukkotiedot.lukittu = false;
      }
    };
    isLocked(params, okCb);
  }

  function isLocked(params, cb) {
    var resource = resourceFromState();
    resource.get(params, cb, Notifikaatiot.serverLukitus);
  }

  function lock(params, cb, failCb) {
    var resource = resourceFromState();
    doLock(resource, params, cb, failCb);
  }

  function unlock(params, cb) {
    var resource = resourceFromState();
    resource.delete(params, cb || angular.noop, Notifikaatiot.serverLukitus);
    etag = null;
  }

  function lockRakenne(params, cb) {
    doLock(OpetussuunnitelmanTekstitRakenneLukko, params, cb);
  }

  function unlockRakenne(params, cb) {
    OpetussuunnitelmanTekstitRakenneLukko.delete(params, cb || angular.noop, Notifikaatiot.serverLukitus);
    etag = null;
  }

  function lockTekstikappale(params, cb) {
    doLock(OpetussuunnitelmanTekstitLukko, params, cb);
  }

  function unlockTekstikappale(params, cb) {
    OpetussuunnitelmanTekstitLukko.delete(params, cb || angular.noop, Notifikaatiot.serverLukitus);
    etag = null;
  }

  this.isLocked = checkLock;
  this.lock = lock;
  this.unlock = unlock;
  this.lockRakenne = lockRakenne;
  this.unlockRakenne = unlockRakenne;
  this.lockTekstikappale = lockTekstikappale;
  this.unlockTekstikappale = unlockTekstikappale;
});
