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

      .state('root.opetussuunnitelmat.yksi.vuosiluokkakokonaisuus', {
        url: '/vuosiluokat/:vlkId',
        templateUrl: 'views/opetussuunnitelmat/vuosiluokat/vlk.html',
        controller: 'VuosiluokkakokonaisuusController',
        resolve: {
          vuosiluokatService: 'VuosiluokatService',
          vlkId: ['$stateParams', function($stateParams){
            return $stateParams.vlkId;
          }],
          vlk: ['vuosiluokatService', 'vlkId', 'opsId', function (vuosiluokatService, vlkId, opsId) {
            return vuosiluokatService.getVuosiluokkakokonaisuus(opsId, vlkId).$promise;
          }],
          baseLaajaalaiset: ['vuosiluokatService', 'opsId', function (vuosiluokatService, opsId) {
            return vuosiluokatService.getLaajaalaiset(opsId).$promise;
          }],
          naviState: ['baseLaajaalaiset', 'OpsNavigaatio', function (baseLaajaalaiset, OpsNavigaatio) {
            // Odota laaja-alaiset ennen sivunavin aktivointia niin UI-elementit ei pompi
            OpsNavigaatio.setActive();
          }]
        }
      })

      .state('root.opetussuunnitelmat.yksi.valinnaiset', {
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
        }
      })

      .state('root.opetussuunnitelmat.yksi.oppiaine', {
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
          perusteOppiaine: ['vuosiluokatService', 'oppiaineId', 'oppiaineTyyppi', function (vuosiluokatService, oppiaineId, oppiaineTyyppi) {
            return oppiaineTyyppi === 'yhteinen' ? vuosiluokatService.getPerusteOppiaine(oppiaineId).$promise : null;
          }]
        }
      })

      .state('root.opetussuunnitelmat.yksi.oppiaine.oppiaine', {
        url: '',
        templateUrl: 'views/opetussuunnitelmat/vuosiluokat/oppiaine.html',
        controller: 'OppiaineController',
        resolve: {
          naviState: ['OpsNavigaatio', function (OpsNavigaatio) {
            OpsNavigaatio.setActive();
          }]
        }
      })

      .state('root.opetussuunnitelmat.yksi.oppiaine.vuosiluokka', {
        url: '/vuosiluokka/:vlId',
        templateUrl: 'views/opetussuunnitelmat/vuosiluokat/vuosiluokka.html',
        controller: 'VuosiluokkaBaseController',
        resolve: {
          naviState: ['OpsNavigaatio', function (OpsNavigaatio) {
            OpsNavigaatio.setActive();
          }],
          baseLaajaalaiset: ['VuosiluokatService', 'opsId', function (VuosiluokatService, opsId) {
            return VuosiluokatService.getLaajaalaiset(opsId).$promise;
          }],
        }
      })

      .state('root.opetussuunnitelmat.yksi.oppiaine.vuosiluokka.tavoitteet', {
        url: '/tavoitteet',
        templateUrl: 'views/opetussuunnitelmat/vuosiluokat/vuosiluokantavoitteet.html',
        controller: 'VuosiluokkaTavoitteetController',
      })

      .state('root.opetussuunnitelmat.yksi.oppiaine.vuosiluokka.sisaltoalueet', {
        url: '/sisaltoalueet',
        templateUrl: 'views/opetussuunnitelmat/vuosiluokat/vuosiluokansisaltoalueet.html',
        controller: 'VuosiluokkaSisaltoalueetController',
      })

      .state('root.opetussuunnitelmat.yksi.oppiaine.vuosiluokkaistaminen', {
        url: '/vuosiluokkaistaminen',
        templateUrl: 'views/opetussuunnitelmat/vuosiluokat/vuosiluokkaistaminen.html',
        controller: 'VuosiluokkaistaminenController',
        resolve: {
          vuosiluokatService: 'VuosiluokatService',
          /*tavoitteet: ['vuosiluokatService', function (vuosiluokatService) {
            return vuosiluokatService.getTavoitteet(oppiaineenVlkId);
          }],*/
          naviState: ['OpsNavigaatio', function (OpsNavigaatio) {
            OpsNavigaatio.setActive(false);
          }]
        }
      })

      .state('root.opetussuunnitelmat.yksi.uusioppiaine', {
        url: '/uusioppiaine/:vlkId',
        templateUrl: 'views/opetussuunnitelmat/vuosiluokat/uusioppiaine.html',
        controller: 'UusiOppiaineController',
        resolve: {
          naviState: ['OpsNavigaatio', function (OpsNavigaatio) {
            OpsNavigaatio.setActive(false);
          }],
          vuosiluokatService: 'VuosiluokatService',
          vlkId: ['$stateParams', function($stateParams){
            return $stateParams.vlkId;
          }],
          vlk: ['vuosiluokatService', 'vlkId', 'opsId', function (vuosiluokatService, vlkId, opsId) {
            return vuosiluokatService.getVuosiluokkakokonaisuus(opsId, vlkId).$promise;
          }],
          vlkPeruste: ['vuosiluokatService', 'vlkId', 'opsId', function (vuosiluokatService, vlkId, opsId) {
            return vuosiluokatService.getVlkPeruste(opsId, vlkId).$promise;
          }],
        }
      });

  });
