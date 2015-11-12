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
  .service('Kielitarjonta', function($modal) {
    function rakenna(opsId, oppiaine, perusteOppiaine, successCb) {
      successCb = successCb || angular.noop;
      $modal.open({
        templateUrl: 'views/opetussuunnitelmat/modals/kielitarjonta.html',
        controller: 'KielitarjontaModalController',
        size: 'lg',
        resolve: {
          opsId: _.constant(opsId),
          oppiaine: _.constant(oppiaine),
          perusteOppiaine: _.constant(perusteOppiaine)
        }
      })
      .result.then(successCb);
    }

    return {
      rakenna: rakenna
    };
  })
  .controller('KielitarjontaModalController', function($scope, $stateParams, $modalInstance, $q, OpsService,
                                                       $state, opsId, oppiaine, perusteOppiaine, OppiaineCRUD, Notifikaatiot) {
    var vuosiluokkakokonaisuudet = _(OpsService.get().vuosiluokkakokonaisuudet).map('vuosiluokkakokonaisuus').map('_tunniste').value();
    vuosiluokkakokonaisuudet = _.zipObject(vuosiluokkakokonaisuudet, _.map(vuosiluokkakokonaisuudet, _.constant(true)));

    function getType() {
      if (!_.isString(oppiaine.koodiArvo)) {
        console.warn('Oppiaineen koodia ei ole määritelty');
        return '';
      }

      if (OpsService.oppiaineIsKieli(oppiaine)) {
        return 'kieli';
      }
      else if (oppiaine.koodiArvo === 'KT') {
        $scope.$valittu = oppiaine;
        return 'uskonto';
      }
      else {
        console.warn('Oppiaineen täytyy olla kieli tai uskonto');
        return '';
      }
    }

    $scope.$type = getType();
    $scope.oppiaine = oppiaine;
    $scope.$kaikki = _.filter(_.clone(perusteOppiaine.oppimaarat), function(om) {
      return _.any(om.vuosiluokkakokonaisuudet, function(vlk) {
        return vuosiluokkakokonaisuudet[vlk._vuosiluokkakokonaisuus];
      });
    });

    if ($scope.$type === 'uskonto') {
      $scope.$kaikki.unshift({
        abstrakti: true
      });
      $scope.$valittu = _.first($scope.$kaikki);
    }

    $scope.$concretet = _.reject(perusteOppiaine.oppimaarat, function(om) {
      return om.abstrakti || _.isEmpty(om.vuosiluokkakokonaisuudet);
    });

    $scope.valitse = function(valinta) {
      $scope.$valittu = valinta || {};
      if (!$scope.$valittu.abstrakti) {
        $scope.$valittu.$concrete = valinta;
      }
      $scope.$onAbstrakti = $scope.$valittu.abstrakti;
      $scope.$omaNimi = _.clone($scope.$valittu.nimi);
    };

    $scope.ok = function() {
      var tunniste = $scope.$type === 'kieli' ? $scope.$valittu.$concrete.tunniste : $scope.$valittu.tunniste;

      OppiaineCRUD.addKielitarjonta({
        opsId: opsId,
        oppiaineId: oppiaine.id
      }, {
        tunniste: tunniste,
        omaNimi: $scope.$omaNimi
      }, function(res) {
        $modalInstance.close(res);
        Notifikaatiot.onnistui('tallennettu-ok');
      }, Notifikaatiot.serverCb);
    };

    $scope.peruuta = $modalInstance.dismiss;
  });
