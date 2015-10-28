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
        onEnter: ['Kieli', '$stateParams', '$rootScope', function (Kieli, $stateParams, $rootScope) {
          Kieli.setUiKieli($stateParams.lang, false);
          $rootScope.$broadcast('fetched:oikeusTiedot');
        }],
        resolve: {
          casTiedot: ['Oikeudet', '$q', function (Oikeudet, $q) {
            return $q.all([Oikeudet.getKayttaja().$promise, Oikeudet.getCasTiedot()]);
          }],
          opsOikeudet: 'OpetussuunnitelmaOikeudetService',
          kayttajaOikeudetNouto: ['casTiedot', 'opsOikeudet', function (casTiedot, opsOikeudet) {
            return opsOikeudet.query();
          }]
        }
      })

      .state('root.admin', {
        url: '/admin',
        templateUrl: 'views/admin.html',
        controller: 'AdminController',
        resolve: {
          opsStatistiikka: function(OpetussuunnitelmaCRUD) {
            return OpetussuunnitelmaCRUD.tilastot().$promise;
          }
        }
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
        template: '<div ui-view></div>'
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
            var fetched = opsService.fetch(opsId);
            return fetched.$promise ? fetched.$promise : fetched;
          }],
          vuosiluokkakokonaisuudet: ['vuosiluokatService', 'opsModel', function (vuosiluokatService, opsModel) {
            return vuosiluokatService.getVuosiluokkakokonaisuudet(opsModel);
          }],
          'opsOikeudet': 'OpetussuunnitelmaOikeudetService',
          'opsOikeudetNouto': ['opsOikeudet', '$stateParams', function (opsOikeudet, $stateParams) {
            return opsOikeudet.fetch($stateParams);
          }]
        },
        controller: function ($scope, opsModel, vuosiluokkakokonaisuudet, opsService, $rootScope) {
          $scope.model = opsModel;
          $scope.isEditable = opsService.isEditable;
          $scope.vuosiluokkakokonaisuudet = vuosiluokkakokonaisuudet;
          $scope.$on('rakenne:updated', function () {
            $scope.model = opsService.get();
            $rootScope.$broadcast('murupolku:update');
          });
        }
      })

      .state('root.opetussuunnitelmat.yksi.sisalto', {
        url: '/osiot',
        templateUrl: 'views/opetussuunnitelmat/sisalto.html',
        controller: 'OpetussuunnitelmaSisaltoController',
        resolve: {
          tekstit: ['OpetussuunnitelmanTekstit', '$stateParams', function(ot, $stateParams) {
            return ot.otsikot({ opsId: $stateParams.id }).$promise;
          }],
          naviState: ['OpsNavigaatio', function (OpsNavigaatio) {
            OpsNavigaatio.setActive(false);
          }]
        }
      })

      .state('root.opetussuunnitelmat.yksi.tiedot', {
        url: '/tiedot?:pohjaId',
        templateUrl: 'views/opetussuunnitelmat/tiedot.html',
        controller: 'OpetussuunnitelmaTiedotController',
        resolve: {
          naviState: ['OpsNavigaatio', function (OpsNavigaatio) {
            OpsNavigaatio.setActive(false);
          }],
          tiedotId: ['$stateParams', function ($stateParams) {
            return $stateParams.id;
          }],
          kunnat: ['KoodistoHaku', 'tiedotId', function (KoodistoHaku, tiedotId) {
            if (tiedotId === 'uusi') {
              return KoodistoHaku.get({ koodistoUri: 'kunta' }).$promise;
            }
            return null;
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
      /*.state('root.opetussuunnitelmat.yksi.opetussuunnitelma', {
        url: '/ops',
        templateUrl: 'views/opetussuunnitelmat/opetussuunnitelma.html',
        controller: 'OpetussuunnitelmaController',
        resolve: {
          naviState: ['OpsNavigaatio', function (OpsNavigaatio) {
            OpsNavigaatio.setActive(false);
          }]
        }
      })*/

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
      .state('root.opetussuunnitelmat.yksi.kasitteet', {
        url: '/kasitteet',
        templateUrl: 'views/opetussuunnitelmat/kasitteet.html',
        controller: 'KasitteetController',
        resolve: {
          naviState: ['OpsNavigaatio', function (OpsNavigaatio) {
            OpsNavigaatio.setActive(false);
          }]
        }
      })
      .state('root.opetussuunnitelmat.yksi.esikatselu', {
        url: '/esikatselu?menu',
        templateUrl: 'views/opetussuunnitelmat/esikatselu.html',
        controller: 'EsikatseluController',
        resolve: {
          naviState: ['OpsNavigaatio', function (OpsNavigaatio) {
            OpsNavigaatio.setActive(false);
          }]
        }
      })
      .state('root.opetussuunnitelmat.yksi.esikatselu.tekstikappale', {
        url: '/tekstikappale/:tekstikappaleId',
        templateUrl: 'views/opetussuunnitelmat/esikatselu/tekstikappale.html',
        controller: 'EsikatseluTekstikappaleController',
        resolve: {
          naviState: ['OpsNavigaatio', function (OpsNavigaatio) {
            OpsNavigaatio.setActive(false);
          }]
        }
      })
      .state('root.opetussuunnitelmat.yksi.esikatselu.vuosiluokkakokonaisuus', {
        url: '/vuosiluokkakokonaisuus/:vlkId',
        templateUrl: 'views/opetussuunnitelmat/esikatselu/vlk.html',
        controller: 'EsikatseluVlkController',
        resolve: {
          naviState: ['OpsNavigaatio', function (OpsNavigaatio) {
            OpsNavigaatio.setActive(false);
          }],
          baseLaajaalaiset: ['vuosiluokatService', 'opsId', function (vuosiluokatService, opsId) {
            return vuosiluokatService.getLaajaalaiset(opsId);
          }],
        }
      })
      .state('root.opetussuunnitelmat.yksi.esikatselu.oppiaine', {
        url: '/oppiaine/:oppiaineId?oppiaineTyyppi?vuosiluokka',
        templateUrl: 'views/opetussuunnitelmat/esikatselu/oppiaine.html',
        controller: 'EsikatseluOppiaineController',
        resolve: {
          naviState: ['OpsNavigaatio', function (OpsNavigaatio) {
            OpsNavigaatio.setActive(false);
          }],
          oppiaineId: ['$stateParams', function($stateParams){
            return $stateParams.oppiaineId;
          }],
          oppiaineTyyppi: ['$stateParams', function($stateParams) {
            return $stateParams.oppiaineTyyppi;
          }],
          perusteOppiaine: ['vuosiluokatService', 'oppiaineId', 'oppiaineTyyppi', function (vuosiluokatService, oppiaineId, oppiaineTyyppi) {
            return oppiaineTyyppi === 'yhteinen' ? vuosiluokatService.getPerusteOppiaine(oppiaineId) : null;
          }],
          oppiaineInit: ['OppiaineService', 'oppiaineId', 'opsModel', function (OppiaineService, oppiaineId, opsModel) {
            return OppiaineService.refresh(opsModel, oppiaineId);
          }],
          baseLaajaalaiset: ['vuosiluokatService', 'opsId', function (vuosiluokatService, opsId) {
            return vuosiluokatService.getLaajaalaiset(opsId);
          }]
        }
      })

      .state('root.pohjat', {
        url: '/pohjat',
        template: '<div ui-view></div>',
        abstract: true
      })

      .state('root.pohjat.lista', {
        url: '',
        templateUrl: 'views/opetussuunnitelmat/pohja/lista.html',
        controller: 'PohjaListaController'
      })

      .state('root.pohjat.yksi', {
        url: '/:pohjaId',
        templateUrl: 'views/opetussuunnitelmat/pohja/base.html',
        controller: 'PohjaController',
        resolve: {
          opsService: 'OpsService',
          pohjaId: ['$stateParams', function($stateParams){
            return $stateParams.pohjaId;
          }],
          pohjaModel: ['opsService', 'pohjaId', function(opsService, pohjaId) {
            return opsService.fetchPohja(pohjaId);
          }],
          'opsOikeudet': 'OpetussuunnitelmaOikeudetService',
          'opsOikeudetNouto': ['opsOikeudet', '$stateParams', function (opsOikeudet, $stateParams) {
            var params = _.clone($stateParams);
            if (params.pohjaId === 'uusi') {
              params = _.omit(params, 'pohjaId');
            }
            return opsOikeudet.fetch(params);
          }]
        }
      })

      .state('root.pohjat.yksi.sisalto', {
        url: '/sisalto',
        templateUrl: 'views/opetussuunnitelmat/pohja/sisalto.html',
        controller: 'PohjaSisaltoController',
        resolve: {
          pohjaOps: ['OpsService', '$stateParams', function(OpsService, $stateParams) {
            return OpsService.haeOikeasti($stateParams.pohjaId);
          }],
          tekstit: ['OpetussuunnitelmanTekstit', '$stateParams', function(ot, $stateParams) {
            return ot.otsikot({ opsId: $stateParams.pohjaId }).$promise;
          }]
        }
      })

      .state('root.pohjat.yksi.tiedot', {
        url: '/tiedot',
        templateUrl: 'views/opetussuunnitelmat/pohja/tiedot.html',
        controller: 'PohjaTiedotController',
        resolve: {
          pohjaId: ['$stateParams', function ($stateParams) {
            return $stateParams.pohjaId;
          }],
          perusteet: ['EperusteetValmiitPerusteet', 'pohjaId', function (EperusteetPerusopetus, pohjaId) {
            if (pohjaId === 'uusi') {
              return EperusteetPerusopetus.query({}).$promise;
            }
            return null;
          }]
        }
      })

      .state('root.pohjat.yksi.tekstikappale', {
        url: '/tekstikappale/:tekstikappaleId',
        templateUrl: 'views/opetussuunnitelmat/pohja/tekstikappale.html',
        controller: 'PohjaTekstikappaleController',
        resolve: {
          tekstikappaleId: ['$stateParams', function ($stateParams) {
            return $stateParams.tekstikappaleId;
          }],
          tekstikappaleModel: ['pohjaId', 'tekstikappaleId', 'OpetussuunnitelmanTekstit', function (pohjaId, tekstikappaleId, OpetussuunnitelmanTekstit) {
            return OpetussuunnitelmanTekstit.get({opsId: pohjaId, viiteId: tekstikappaleId}).$promise;
          }]
          /*naviState: ['OpsNavigaatio', function (OpsNavigaatio) {
            OpsNavigaatio.setActive();
          }]*/
        }
      });
  });
