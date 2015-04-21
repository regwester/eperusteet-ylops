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

/**
* Statusbadge:
* <statusbadge status="luonnos|..." editable="true|false"></statusbadge>
* Tyylit eri statuksille määritellään "statusbadge" sass-moduulissa.
* Sama avainsana pitää olla käytössä tyyleissä ja lokalisoinnissa.
*/
ylopsApp
.directive('statusbadge', function () {
  var OFFSET = 4;
  return {
    templateUrl: 'views/opetussuunnitelmat/directives/statusbadge.html',
    restrict: 'EA',
    replace: true,
    scope: {
      status: '=',
      editable: '=?',
      projektiId: '=',
      model: '='
    },
    controller: 'StatusbadgeController',
    link: function (scope, element) {
      // To fit long status names into the badge, adjust letter spacing
      var el = element.find('.status-name');

      function adjust() {
        if (scope.status && scope.status.length > 8) {
          var spacing = 1 - ((scope.status.length - OFFSET) * 0.2);
          el.css('letter-spacing', spacing + 'px');
        }
      }
      scope.$watch('status', adjust);
      adjust();
    }
  };
})

.controller('StatusbadgeController', function ($scope, $state, OpsinTilanvaihto, OpsinTila, Notifikaatiot,
                                               OpetussuunnitelmaOikeudetService) {
    $scope.iconMapping = {
      luonnos: 'pencil',
      laadinta: 'pencil',
      kommentointi: 'comment',
      viimeistely: 'certificate',
      kaannos: 'book',
      valmis: 'thumbs-up',
      julkaistu: 'glass',
      poistettu: 'folder-open',
    };

    var OPSTILAT = ['luonnos', 'valmis', 'julkaistu', 'poistettu'];
    var POHJATILAT = ['luonnos', 'valmis', 'poistettu'];

    function getTilat(isPohja, current) {
      var tilat = _.without(isPohja ? POHJATILAT : OPSTILAT, current);
      switch(current) {
        case 'valmis':
          return isPohja ? ['luonnos', 'poistettu'] : tilat;
        case 'julkaistu':
          return _.without(tilat, 'luonnos');
        default:
          return _.without(tilat, 'julkaistu');
      }
    }

    $scope.appliedClasses = function () {
      var classes = {editable: $scope.editable};
      classes[$scope.status] = true;
      return classes;
    };

    $scope.iconClasses = function () {
      return 'glyphicon glyphicon-' + $scope.iconMapping[$scope.status];
    };

    $scope.startEditing = function() {
      var isPohja = $scope.model.tyyppi === 'pohja';
      if (!OpetussuunnitelmaOikeudetService.onkoOikeudet(isPohja ? 'pohja' : 'opetussuunnitelma', 'muokkaus')) {
        return;
      }

      OpsinTilanvaihto.start({
        currentStatus: $scope.status,
        mahdollisetTilat: getTilat(isPohja, $scope.status),
        isPohja: isPohja
      }, function (newStatus) {
        OpsinTila.save($scope.model, newStatus, function(res) {
          Notifikaatiot.onnistui('tallennettu-ok');
          $scope.status = res.tila;
          $state.go($state.current, {}, {reload: true});
        });
      });
    };

  });
