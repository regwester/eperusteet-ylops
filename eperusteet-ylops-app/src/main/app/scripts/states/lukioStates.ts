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
        .state('root.opetussuunnitelmat.lukio', {
            url: '/:id/lukio',
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
                'opsOikeudet': 'OpetussuunnitelmaOikeudetService',
                'opsOikeudetNouto': ['opsOikeudet', '$stateParams', function (opsOikeudet, $stateParams) {
                    return opsOikeudet.fetch($stateParams);
                }]
            },
            controller: function ($scope, $stateParams, opsModel, opsService) {
                $scope.model = opsModel;
                $scope.isEditable = opsService.isEditable;
                $scope.luonnissa = $stateParams.id === 'uusi';
            }
        })
        .state('root.opetussuunnitelmat.lukio.opetus', {
            url: '/opetus',
            templateUrl: 'views/opetussuunnitelmat/lukio/opetus.html',
            controller: 'LukioOpetusController'
        })
        .state('root.opetussuunnitelmat.lukio.opetus.aihekokonaisuudet', {
            url: '/aihekokonaisuudet',
            templateUrl: 'views/opetussuunnitelmat/lukio/aihekokonaisuudet.html',
            controller: 'AihekokonaisuudetController'
        })
        .state('root.opetussuunnitelmat.lukio.opetus.yleisettavoitteet', {
            url: '/yleisetTavoitteet',
            templateUrl: 'views/opetussuunnitelmat/lukio/yleisettavoitteet.html',
            controller: 'OpetuksenYleisetTavoitteetController'
        })
        .state('root.opetussuunnitelmat.lukio.opetus.oppiaineet', {
            url: '/oppiaineet',
            templateUrl: 'views/opetussuunnitelmat/lukio/oppiaineet.html',
            controller: 'LukioOppiaineetController'
        })
        .state('root.opetussuunnitelmat.lukio.opetus.oppiaine', {
            url: '/oppiaineet/:oppiaineId',
            templateUrl: 'views/opetussuunnitelmat/lukio/oppiaine.html',
            controller: 'LukioOppiaineController'
        })
        .state('root.opetussuunnitelmat.lukio.opetus.kurssi', {
            url: '/oppiaineet/:oppiaineId/kurssi/:kurssiId',
            templateUrl: 'views/opetussuunnitelmat/lukio/kurssi.html',
            controller: 'LukioKurssiController'
        })
    });