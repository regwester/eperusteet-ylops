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

      .state('root', {
        url: '/:lang',
        template: '<div ui-view></div>',
        abstract: true,
        onEnter: ['Kieli', '$stateParams', function (Kieli, $stateParams) {
          Kieli.setUiKieli($stateParams.lang, false);
        }]
      })

      .state('root.virhe', {
        url: '/virhe',
        templateUrl: 'views/virhe.html',
        controller: 'VirheController'
      })

      .state('root.etusivu', {
        url: '',
        templateUrl: 'views/etusivu.html',
        controller: 'EtusivuController'
      })

      .state('root.opetussuunnitelmat', {
        url: '/opetussuunnitelmat',
        abstract: true,
        template: '<div ui-view></div>',
      })

      .state('root.opetussuunnitelmat.lista', {
        url: '',
        templateUrl: 'views/opetussuunnitelmat/lista.html',
        controller: 'OpetussuunnitelmatListaController'
      })

      .state('root.opetussuunnitelmat.yksi', {
        url: '/:id',
        templateUrl: 'views/opetussuunnitelmat/opetussuunnitelmabase.html',
        abstract: true,
        resolve: {
          opsService: 'OpsService',
          vuosiluokatService: 'VuosiluokatService',
          opsId: ['$stateParams', function($stateParams){
            return $stateParams.id;
          }],
          opsModel: ['opsService', 'opsId', function(opsService, opsId) {
            return opsService.fetch(opsId);
          }],
          vuosiluokat: ['vuosiluokatService', function (vuosiluokatService) {
            return vuosiluokatService.getVuosiluokat();
          }]
        },
        controller: function ($scope, opsModel, vuosiluokat) {
          $scope.model = opsModel;
          $scope.vuosiluokat = vuosiluokat;
        }
      })

      .state('root.opetussuunnitelmat.yksi.sisalto', {
        url: '/osiot',
        templateUrl: 'views/opetussuunnitelmat/sisalto.html',
        controller: 'OpetussuunnitelmaSisaltoController',
        resolve: {
          naviState: ['OpsNavigaatio', function (OpsNavigaatio) {
            OpsNavigaatio.setActive(false);
          }]
        }
      })

      .state('root.opetussuunnitelmat.yksi.tiedot', {
        url: '/tiedot',
        templateUrl: 'views/opetussuunnitelmat/tiedot.html',
        controller: 'OpetussuunnitelmaTiedotController',
        resolve: {
          naviState: ['OpsNavigaatio', function (OpsNavigaatio) {
            OpsNavigaatio.setActive(false);
          }]
        }
      })

      .state('root.opetussuunnitelmat.yksi.sisaltoalue', {
        url: '/osiot/:alueId',
        templateUrl: 'views/opetussuunnitelmat/sisaltoalue.html',
        controller: 'OpetussuunnitelmaSisaltoAlueController',
        resolve: {
          naviState: ['OpsNavigaatio', function (OpsNavigaatio) {
            OpsNavigaatio.setActive();
          }]
        }
      })

      /* väliaikainen (proto) muokkaustila */
      .state('root.opetussuunnitelmat.yksi.opetussuunnitelma', {
        url: '/ops',
        templateUrl: 'views/opetussuunnitelmat/opetussuunnitelma.html',
        controller: 'OpetussuunnitelmaController',
        resolve: {
          naviState: ['OpsNavigaatio', function (OpsNavigaatio) {
            OpsNavigaatio.setActive(false);
          }]
        }
      })

      .state('root.opetussuunnitelmat.yksi.tekstikappale', {
        url: '/tekstikappale/:tekstikappaleId',
        templateUrl: 'views/opetussuunnitelmat/tekstikappale.html',
        controller: 'TekstikappaleController',
        resolve: {
          naviState: ['OpsNavigaatio', function (OpsNavigaatio) {
            OpsNavigaatio.setActive();
          }]
        }
      })

      .state('root.opetussuunnitelmat.yksi.esikatselu', {
        url: '/esikatselu',
        templateUrl: 'views/opetussuunnitelmat/esikatselu.html',
        controller: 'EsikatseluController'
      })

      .state('root.opetussuunnitelmat.yksi.vuosiluokkakokonaisuus', {
        url: '/vuosiluokat/:vlkId',
        templateUrl: 'views/opetussuunnitelmat/vuosiluokat/vlk.html',
        controller: 'VuosiluokkakokonaisuusController',
        resolve: {
          naviState: ['OpsNavigaatio', function (OpsNavigaatio) {
            OpsNavigaatio.setActive();
          }]
        }
      })

      .state('root.opetussuunnitelmat.yksi.oppiaine', {
        url: '/vuosiluokat/:vlkId/oppiaine/:oppiaineId',
        template: '<div ui-view></div>',
        abstract: true,
        controller: 'OppiaineBaseController'
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
        controller: 'VuosiluokkaController',
        resolve: {
          naviState: ['OpsNavigaatio', function (OpsNavigaatio) {
            OpsNavigaatio.setActive();
          }]
        }
      })

      .state('root.opetussuunnitelmat.yksi.oppiaine.vuosiluokkaistaminen', {
        url: '/vuosiluokkaistaminen',
        templateUrl: 'views/opetussuunnitelmat/vuosiluokat/vuosiluokkaistaminen.html',
        controller: 'VuosiluokkaistaminenController',
        resolve: {
          vuosiluokatService: 'VuosiluokatService',
          tavoitteet: ['vuosiluokatService', function (vuosiluokatService) {
            return vuosiluokatService.getTavoitteet(/*oppiaineenVlkId*/);
          }],
          naviState: ['OpsNavigaatio', function (OpsNavigaatio) {
            OpsNavigaatio.setActive(false);
          }]
        }
      });
  });
