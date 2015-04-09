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
  .run(function ($rootScope, VirheService, $window, Editointikontrollit, Kaanna, Varmistusdialogi, $state) {
    $rootScope.$on('$stateChangeError', function(event, toState/*, toParams, fromState*/) {
      VirheService.virhe({state: toState.name});
    });

    $rootScope.$on('$stateNotFound', function(event, toState/*, toParams, fromState*/) {
      VirheService.virhe({state: toState.to});
    });

    $rootScope.$on('$stateChangeStart', function(event, toState, toParams, fromState, fromParams) {
      $rootScope.lastState = {
        state: _.clone(fromState),
        params: _.clone(fromParams)
      };

      if (Editointikontrollit.getEditMode()) {
        event.preventDefault();

        var data = {toState: toState, toParams: toParams};
        Varmistusdialogi.dialogi({
          successCb: function(data) {
            Editointikontrollit.cancelEditing(true);
            $state.go(data.toState, data.toParams);
          }, data: data, otsikko: 'vahvista-liikkuminen', teksti: 'tallentamattomia-muutoksia',
          lisaTeksti: 'haluatko-jatkaa',
          primaryBtn: 'poistu-sivulta'
        })();
      }
    });

    $window.addEventListener('beforeunload', function(event) {
      if (Editointikontrollit.getEditMode()) {
        var confirmationMessage = Kaanna.kaanna('tallentamattomia-muutoksia');
        (event || window.event).returnValue = confirmationMessage;
        return confirmationMessage;
      }
    });
  });
