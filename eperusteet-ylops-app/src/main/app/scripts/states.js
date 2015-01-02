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
        template: '<div ui-view></div>',
        abstract: true,
      })

      .state('root.opetussuunnitelmat.yksi.opetussuunnitelma', {
        url: '',
        templateUrl: 'views/opetussuunnitelmat/opetussuunnitelma.html',
        controller: 'OpetussuunnitelmaController'
      })

      .state('root.opetussuunnitelmat.yksi.tekstikappale', {
        url: '/tekstikappale/:tekstikappaleId',
        templateUrl: 'views/opetussuunnitelmat/tekstikappale.html',
        controller: 'TekstikappaleController'
      });
  });
