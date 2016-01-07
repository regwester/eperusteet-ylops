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

ylopsApp
  .factory('Dokumentti', function($resource, SERVICE_LOC) {
    var baseUrl = SERVICE_LOC + '/dokumentit/:id';

    return {
      states: $resource(baseUrl, {}, {
        query: { method: 'GET', params: {}, isArray: false }
      }),
      countries: $resource(baseUrl, {}, {
        query: { method: 'GET', params: {}, isArray: false }
      })
    };

    return $resource(baseUrl, {
      id: '@id',
      tila: {
        method: 'GET', url: baseUrl+'/tila'
      }
    });
  })
  .service('Pdf', function(Dokumentti, SERVICE_LOC) {

    function generoiPdf(opsId, kieli, success, failure) {
      success = success || angular.noop;
      failure = failure || angular.noop;

      Dokumentti.save({
        'opsId': opsId,
        'kieli': kieli
      }, null, success, failure);
    }

    function haeTila(tokenId, success, failure) {
      success = success || angular.noop;
      failure = failure || angular.noop;

      console.log(Dokumentti);

      Dokumentti.tila({
        id: tokenId
      }, success);
    }

    function haeDokumentti(tokenId, success, failure) {
      success = success || angular.noop;
      failure = failure || angular.noop;

      return Dokumentti.get({
        id: tokenId
      }, success);
    }

    function haeLinkki(tokenId) {
      // dis like, ewwww
      return SERVICE_LOC + '/dokumentit/' + tokenId;
    }

    function haeUusin(opsId, kieli, success, failure) {
      success = success || angular.noop;
      failure = failure || angular.noop;

      console.log("asdasd");

      return Dokumentti.get({
        opsId: opsId,
        kieli: kieli
      }, success);

    }
    return {
      generoiPdf: generoiPdf,
      haeDokumentti: haeDokumentti,
      haeTila: haeTila,
      haeLinkki: haeLinkki,
      haeUusin: haeUusin
    };
  })

  .factory('PdfCreation', function($modal) {
    var service: any = {};
    var opsId = null;

    service.setOpsId = function(id) {
      opsId = id;
    };

    service.openModal = function() {
      $modal.open({
        templateUrl: 'views/opetussuunnitelmat/modals/dokumentti.html',
        controller: 'PdfCreationController',
        resolve: {
          opsId: function() {
            return opsId;
          },
          kielet: function() {
            return {
              lista: _.sortBy(['fi', 'sv']),
              valittu: 'fi'
            };
          }
        }
      });
    };

    return service;
  })
  .controller('PdfCreationController', function($scope, kielet, Pdf, opsId,
    $timeout, Notifikaatiot, Kaanna) {
    $scope.kielet = kielet;
    $scope.docs = {};
    var pdfToken = null;

    $scope.hasPdf = function() {
      return !!$scope.docs[$scope.kielet.valittu];
    };

    function fetchLatest(lang) {
      var kielet;
      if (_.isString(lang)) {
        kielet = [lang];
      } else {
        kielet = kielet.lista;
      }
      _.each(kielet, function(kieli) {

        Pdf.haeUusin(opsId, kieli, function(res) {
          console.log(res);
          if (kieli === $scope.kielet.valittu) {
            $scope.tila = res.tila;
          }
          if (res.id !== null) {
            res.url = Pdf.haeLinkki(res.id);
            $scope.docs[kieli] = res;
          }
        });
      });
    }

    function enableActions(disable = false) {
      $scope.generateInProgress = !disable;
    }

    function getStatus(id) {
      Pdf.haeTila(id, function(res) {
        $scope.tila = res.tila;
        switch (res.tila) {
          case 'luodaan':
          case 'ei_ole':
            startPolling(res.id);
            break;
          case 'valmis':
            Notifikaatiot.onnistui('dokumentti-luotu');
            res.url = Pdf.haeLinkki(res.id);
            $scope.docs[$scope.kielet.valittu] = res;
            enableActions();
            break;
          default: // 'epaonnistui' + others(?)
            Notifikaatiot.fataali(Kaanna.kaanna('dokumentin-luonti-epaonnistui') +
              ': ' + res.virhekoodi || res.tila);
            enableActions();
            break;
        }
      });
    }

    function startPolling(id) {
      $scope.poller = $timeout(function() {
        getStatus(id);
      }, 3000);
    }

    $scope.$on('$destroy', function() {
      $timeout.cancel($scope.poller);
    });

    $scope.generate = function() {
      enableActions(false);
      $scope.docs[$scope.kielet.valittu] = null;
      $scope.tila = 'luodaan';
      Pdf.generoiPdf(opsId, $scope.kielet.valittu,
      function(res) {
        if (res.id !== null) {
          pdfToken = res.id;
          startPolling(res.id);
        }
      }, function() {
        enableActions();
        $scope.tila = 'ei_ole';
      });
    };

    $scope.$watch('kielet.valittu', function(value) {
      fetchLatest(value);
    });
  });
