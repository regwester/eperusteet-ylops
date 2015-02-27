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

/* jshint ignore:start */

var ylopsApp = angular.module('ylopsApp', [
  'ngRoute',
  'ngSanitize',
  'ui.router',
  'ngResource',
  'ngAnimate',
  'pascalprecht.translate',
  'ui.bootstrap',
  'ui.utils',
  'ui.select',
  'ui.tree',
  'ui.sortable'
]);

/* jshint ignore:end */

ylopsApp
  .run(function ($rootScope, VirheService, $window) {
    $rootScope.$on('$stateChangeError', function(event, toState/*, toParams, fromState*/) {
      VirheService.virhe({state: toState.name});
    });

    $rootScope.$on('$stateNotFound', function(event, toState/*, toParams, fromState*/) {
      VirheService.virhe({state: toState.to});
    });

    // SurveyMonkey popup
    $window.addEventListener('load', function() {
      var surveyMonkey = window.open('', 'ePerusteet POPS pilotointi', 'width=700, height=650');
      surveyMonkey.document.write('<div id="surveyMonkeyInfo"><div><script src="https://www.surveymonkey.com/jsEmbed.aspx?sm=4dHAHhCxKodziIrA5BOOpA_3d_3d"> </script></div>Create your free online surveys with <a href="https://www.surveymonkey.com">SurveyMonkey</a> , the world\'s leading questionnaire tool.</div>');
    });
  });
