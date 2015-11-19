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
  .config(function($stateProvider) {
    $stateProvider

      .state('root.opetussuunnitelmat.yksi.opetus', {
        url: '/opetus',
        templateUrl: 'views/opetussuunnitelmat/opetus.html',
        controller: ['$scope', '$state', 'VuosiluokatService', 'opsModel', function(
            $scope,
            $state,
            VuosiluokatService,
            opsModel) {

          $scope.navi = VuosiluokatService.mapForMenu(opsModel);

          if ($state.is('root.opetussuunnitelmat.yksi.opetus')) {
            $state.go('root.opetussuunnitelmat.yksi.opetus.vuosiluokkakokonaisuus', { vlkId: _.first($scope.navi).id });
          }
        }]
      })

      .state('root.opetussuunnitelmat.yksi.opetus.vuosiluokkakokonaisuus', {
        url: '/vuosiluokat/:vlkId',
        templateUrl: 'views/opetussuunnitelmat/vuosiluokat/vlk.html',
        controller: 'VuosiluokkakokonaisuusController',
        resolve: {
          vuosiluokatService: 'VuosiluokatService',
          vlkId: ['$stateParams', function($stateParams) {
            return $stateParams.vlkId;
          }],
          vlk: ['vuosiluokatService', 'vlkId', 'opsId', function (vuosiluokatService, vlkId, opsId) {
            return vuosiluokatService.getVuosiluokkakokonaisuus(opsId, vlkId).$promise;
          }],
          baseLaajaalaiset: ['vuosiluokatService', 'opsId', function (vuosiluokatService, opsId) {
            return vuosiluokatService.getLaajaalaiset(opsId);
          }],
        }
      })

      .state('root.opetussuunnitelmat.yksi.opetus.vuosiluokkakokonaisuussort', {
        url: '/vuosiluokat/:vlkId/jarjesta',
        templateUrl: 'views/opetussuunnitelmat/vuosiluokat/vlksort.html',
        controller: 'VuosiluokkakokonaisuusSortController',
        resolve: {
          vuosiluokatService: 'VuosiluokatService',
          vlkId: ['$stateParams', function($stateParams){
            return $stateParams.vlkId;
          }],
          vlk: ['vuosiluokatService', 'vlkId', 'opsId', function (vuosiluokatService, vlkId, opsId) {
            return vuosiluokatService.getVuosiluokkakokonaisuus(opsId, vlkId).$promise;
          }]
        }
      })

      .state('root.opetussuunnitelmat.yksi.opetus.valinnaiset', {
        url: '/vuosiluokat/:vlkId/valinnaiset',
        templateUrl: 'views/opetussuunnitelmat/vuosiluokat/valinnaiset.html',
        controller: 'ValinnaisetOppiaineetController',
        resolve: {
          vuosiluokatService: 'VuosiluokatService',
          vlkId: ['$stateParams', function($stateParams){
            return $stateParams.vlkId;
          }],
          vlk: ['vuosiluokatService', 'vlkId', 'opsId', function (vuosiluokatService, vlkId, opsId) {
            return vuosiluokatService.getVuosiluokkakokonaisuus(opsId, vlkId).$promise;
          }],
          opsModelVal: ['opsService', 'opsId', function(opsService, opsId) {
            var fetched = opsService.fetch(opsId);
            return fetched.$promise ? fetched.$promise : fetched;
          }],
        }
      })

      .state('root.opetussuunnitelmat.yksi.opetus.oppiaine', {
        url: '/vuosiluokat/:vlkId/oppiaine/:oppiaineId?oppiaineTyyppi',
        template: '<div ui-view></div>',
        abstract: true,
        controller: 'OppiaineBaseController',
        resolve: {
          vuosiluokatService: 'VuosiluokatService',
          oppiaineId: ['$stateParams', function($stateParams){
            return $stateParams.oppiaineId;
          }],
          vlkId: ['$stateParams', function($stateParams){
            return $stateParams.vlkId;
          }],
          oppiaineTyyppi: ['$stateParams', function($stateParams) {
            return $stateParams.oppiaineTyyppi;
          }],
          oppiaineInit: ['OppiaineService', 'oppiaineId', 'opsModel', 'vlkId', function (OppiaineService, oppiaineId, opsModel, vlkId) {
            return OppiaineService.refresh(opsModel, oppiaineId, vlkId);
          }],
          perusteOppiaine: ['vuosiluokatService', 'oppiaineId', 'oppiaineTyyppi', 'oppiaineInit', function(
                               vuosiluokatService, oppiaineId, oppiaineTyyppi, oppiaineInit) {
            return oppiaineTyyppi === 'yhteinen' ? vuosiluokatService.getPerusteOppiaine(oppiaineId) : null;
          }]
        }
      })

      .state('root.opetussuunnitelmat.yksi.opetus.oppiaine.oppiaine', {
        url: '',
        templateUrl: 'views/opetussuunnitelmat/vuosiluokat/oppiaine.html',
        controller: 'OppiaineController',
        resolve: {
          onEnter: function() {
          }
        }
      })

      .state('root.opetussuunnitelmat.yksi.opetus.oppiaine.vuosiluokka', {
        url: '/vuosiluokka/:vlId',
        templateUrl: 'views/opetussuunnitelmat/vuosiluokat/vuosiluokka.html',
        controller: 'VuosiluokkaBaseController',
        resolve: {
          baseLaajaalaiset: ['VuosiluokatService', 'opsId', function (VuosiluokatService, opsId) {
            return VuosiluokatService.getLaajaalaiset(opsId);
          }]
        }
      })

      .state('root.opetussuunnitelmat.yksi.opetus.oppiaine.vuosiluokka.tavoitteet', {
        url: '/tavoitteet',
        templateUrl: 'views/opetussuunnitelmat/vuosiluokat/vuosiluokantavoitteet.html',
        controller: 'VuosiluokkaTavoitteetController',
      })

      .state('root.opetussuunnitelmat.yksi.opetus.oppiaine.vuosiluokka.sisaltoalueet', {
        url: '/sisaltoalueet',
        templateUrl: 'views/opetussuunnitelmat/vuosiluokat/vuosiluokansisaltoalueet.html',
        controller: 'VuosiluokkaSisaltoalueetController',
      })

      .state('root.opetussuunnitelmat.yksi.opetus.oppiaine.vuosiluokkaistaminen', {
        url: '/vuosiluokkaistaminen',
        templateUrl: 'views/opetussuunnitelmat/vuosiluokat/vuosiluokkaistaminen.html',
        controller: 'VuosiluokkaistaminenController',
        resolve: {
          vuosiluokatService: 'VuosiluokatService',
          /*tavoitteet: ['vuosiluokatService', function (vuosiluokatService) {
            return vuosiluokatService.getTavoitteet(oppiaineenVlkId);
          }],*/
        }
      })

      .state('root.opetussuunnitelmat.yksi.opetus.uusioppiaine', {
        url: '/uusioppiaine/:vlkId?:oppiaineId',
        templateUrl: 'views/opetussuunnitelmat/vuosiluokat/uusioppiaine.html',
        controller: 'UusiOppiaineController',
        resolve: {
          vuosiluokatService: 'VuosiluokatService',
          vlkId: ['$stateParams', function($stateParams) {
            return $stateParams.vlkId;
          }],
          vlk: ['vuosiluokatService', 'vlkId', 'opsId', function (vuosiluokatService, vlkId, opsId) {
            return vuosiluokatService.getVuosiluokkakokonaisuus(opsId, vlkId).$promise;
          }],
          vlkPeruste: ['vuosiluokatService', 'vlkId', 'opsId', 'Notifikaatiot', function (vuosiluokatService, vlkId, opsId, Notifikaatiot) {
            return vuosiluokatService.getVlkPeruste(opsId, vlkId, angular.noop, function () {
              Notifikaatiot.varoitus('uutta-oppiainetta-ei-voi-luoda');
            }).$promise;
          }],
          oppiaineId: ['$stateParams', function ($stateParams) {
            return $stateParams.oppiaineId;
          }],
        }
      });

  });
