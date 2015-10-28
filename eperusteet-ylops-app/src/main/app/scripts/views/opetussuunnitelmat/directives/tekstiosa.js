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
.directive('perusteenTekstiosa', function ($timeout, $window) {
  return {
    restrict: 'A',
    scope: {
      model: '=perusteenTekstiosa',
      muokattava: '=?',
      callbacks: '=',
      config: '=',
      ohjepallo: '='
    },
    templateUrl: 'views/opetussuunnitelmat/directives/tekstiosa.html',
    controller: 'TekstiosaController',
    link: function (scope, element, attrs) {
      scope.editable = !!attrs.muokattava;
      scope.options = {
        collapsed: true //scope.editable --> this was causing only a few non-editable, random text fields to be uncollapsed?
      };
      scope.focusAndScroll = function () {
        $timeout(function () {
          var el = element.find('[ckeditor]');
          if (el && el.length > 0) {
            el[0].focus();
            $window.scrollTo(0, el.eq(0).offset().top - 200);
          }
        }, 300);
      };
    }
  };
})

.controller('TekstiosaController', function ($state, $scope, Editointikontrollit, Kieli) {
  $scope.editMode = false;

  function validoi() {
    if ($scope.muokattava) {
      if (!$scope.model && $state.current.name === 'root.opetussuunnitelmat.yksi.opetus.oppiaine.oppiaine') {
        return;
      }
      $scope.muokattava.otsikko = $scope.muokattava.otsikko || {};
      $scope.muokattava.otsikko.$$validointi = Kieli.validoi($scope.muokattava.otsikko);
      $scope.muokattava.teksti = $scope.muokattava.teksti || {};
      $scope.muokattava.teksti.$$validointi = Kieli.validoi($scope.muokattava.teksti);
    }
  }

  $scope.$watch('muokattava', validoi, true);

  function notifyFn(mode) {
    $scope.editMode = mode;
    if (!mode) {
      $scope.callbacks.notifier = angular.noop;
    }
  }

  $scope.startEditing = function () {
    $scope.editMode = true;
    $scope.callbacks.notifier = notifyFn;
    Editointikontrollit.startEditing();
    $scope.focusAndScroll();
    validoi();
  };

  $scope.remove = function () {
    $scope.callbacks.remove($scope.muokattava);
  };
});
