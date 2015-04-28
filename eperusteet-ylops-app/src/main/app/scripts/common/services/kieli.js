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

ylopsApp
  .service('Kieli', function ($rootScope, $state, $stateParams, $translate) {
    var sisaltokieli = 'fi';
    var uikieli = 'fi';
    var stateInited = false;

    var SISALTOKIELET = [
      'fi',
      'sv',
      'se',
      'ru',
      'en'
    ];

    var SISALTOKIELETMAP = {};

    var UIKIELET = [
      'fi',
      'sv',
      'en'
    ];

    function orderFn(kielikoodi) {
      return _.indexOf(SISALTOKIELET, kielikoodi);
    }

    function isValidKielikoodi(kielikoodi) {
      return _.indexOf(SISALTOKIELET, kielikoodi) > -1;
    }

    this.setSisaltokielet = function(kielikoodit) {
      SISALTOKIELET = kielikoodit;
      SISALTOKIELETMAP = _.zipObject(kielikoodit, _.map(kielikoodit, _.constant(true)));
      $rootScope.$broadcast('update:sisaltokielet');
    };

    this.setSisaltokieli = function (kielikoodi) {
      if (_.indexOf(SISALTOKIELET, kielikoodi) > -1) {
        var old = sisaltokieli;
        sisaltokieli = kielikoodi;
        if (old !== kielikoodi) {
          $rootScope.$broadcast('changed:sisaltokieli', kielikoodi);
        }
      }
    };

    this.getSisaltokieli = function () {
      return sisaltokieli;
    };

    this.setUiKieli = function (kielikoodi, doStateChange) {
      if (isValidKielikoodi(kielikoodi) &&
        (kielikoodi !== uikieli || (stateInited && $stateParams.lang !== kielikoodi))) {
        if (_.isUndefined(doStateChange) || doStateChange === true) {
          $state.go($state.current.name, _.merge($stateParams, {lang: kielikoodi}), {reload: true});
        }
        uikieli = kielikoodi;
        moment.lang(kielikoodi);
        $translate.use(kielikoodi);
        $rootScope.$broadcast('changed:uikieli', kielikoodi);
      }
    };

    this.validoi = function(olio) {
      var errors = [];
      if (!olio) {
        errors.push('tekstikentalla-ei-lainkaan-sisaltoa');
      }
      else {
        _.each(SISALTOKIELET, function(kieli) {
          if (!olio[kieli]) {
            errors.push('tekstikentalla-ei-sisaltoa-kielella-' + kieli);
          }
        });
      }
      return errors;
    };

    this.getUiKieli = function () {
      return uikieli;
    };

    $rootScope.$on('$stateChangeSuccess', function (self, toParams) {
      stateInited = true;
      if (isValidKielikoodi(toParams.lang)) {
        uikieli = toParams.lang;
      }
    });

    this.getSisaltokielet = function() {
      return SISALTOKIELET;
    };

    this.SISALTOKIELET = SISALTOKIELET;
    this.UIKIELET = UIKIELET;
    this.orderFn = orderFn;
  })

  .directive('kielenvaihto', function () {
    return {
      restrict: 'AE',
      scope: {
        modal: '@modal'
      },
      controller: 'KieliController',
      templateUrl: 'views/common/directives/kielenvaihto.html'
    };
  })

  .controller('KieliController', function($scope, $stateParams, $state, Kieli, $q) {
    $scope.isModal = $scope.modal === 'true';
    $scope.sisaltokielet = Kieli.getSisaltokielet();
    $scope.sisaltokieli = Kieli.getSisaltokieli();
    $scope.uikielet = Kieli.UIKIELET;
    $scope.uikieli = Kieli.getUiKieli();
    $scope.uiLangChangeAllowed = true;
    var stateInit = $q.defer();
    var casFetched = $q.defer();

    // TODO Profiili
    /*var info = Profiili.profiili();
    if (info.$casFetched) {
      casFetched.resolve();
    }*/
    casFetched.resolve();

    $scope.$on('$stateChangeSuccess', function () {
      stateInit.resolve();
    });

    $scope.$on('fetched:casTiedot', function () {
      casFetched.resolve();
    });

    $q.all([stateInit.promise, casFetched.promise]).then(function () {
      /*var lang = Profiili.lang();
      // Disable ui language change if language preference found in CAS
      if (Kieli.isValidKielikoodi(lang)) {
        $scope.uiLangChangeAllowed = false;
        Kieli.setUiKieli(lang);
      }
      var profiili = Profiili.profiili();
      if (profiili.preferenssit.sisaltokieli) {
        Kieli.setSisaltokieli(profiili.preferenssit.sisaltokieli);
      }*/
    });

    $scope.$on('update:sisaltokielet', function() {
      $scope.sisaltokielet = Kieli.getSisaltokielet();
    });

    $scope.$on('changed:sisaltokieli', function (event, value) {
      $scope.sisaltokieli = value;
      //Profiili.setPreferenssi('sisaltokieli', value);
    });
    $scope.$on('changed:uikieli', function (event, value) {
      $scope.uikieli = value;
    });

    $scope.setSisaltokieli = function (kieli) {
      Kieli.setSisaltokieli(kieli);
    };

    $scope.setUiKieli = function(kielikoodi) {
      Kieli.setUiKieli(kielikoodi);
    };

  });
