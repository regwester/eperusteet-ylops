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
          naviState: ['OpsNavigaatio', function (OpsNavigaatio) {
            OpsNavigaatio.setActive();
          }],
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
        }
      })

      .state('root.opetussuunnitelmat.yksi.oppiaine', {
        url: '/vuosiluokat/:vlkId/oppiaine/:oppiaineId',
        template: '<div ui-view></div>',
        abstract: true,
        controller: 'OppiaineBaseController',
        resolve: {
          vuosiluokatService: 'VuosiluokatService',
          oppiaineId: ['$stateParams', function($stateParams){
            return $stateParams.oppiaineId;
          }],
          oppiaine: ['vuosiluokatService', 'oppiaineId', function (vuosiluokatService, oppiaineId) {
            return vuosiluokatService.getOppiaine(oppiaineId).$promise;
          }],
          perusteOppiaine: ['vuosiluokatService', 'oppiaineId', function (vuosiluokatService, oppiaineId) {
            return vuosiluokatService.getPerusteOppiaine(oppiaineId).$promise;
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
          vuosiluokkaId: ['$stateParams', function($stateParams){
            return $stateParams.vuosiluokkaId;
          }],
          naviState: ['OpsNavigaatio', function (OpsNavigaatio) {
            OpsNavigaatio.setActive();
          }],
          vuosiluokka: ['vuosiluokatService', 'vuosiluokkaId', function (vuosiluokatService, vuosiluokkaId) {
            return vuosiluokatService.getVuosiluokka(vuosiluokkaId);
          }]
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
      });

  });
