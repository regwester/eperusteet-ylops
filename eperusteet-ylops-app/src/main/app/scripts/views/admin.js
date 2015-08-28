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
.controller('AdminController', function ($scope, ListaSorter, OpsListaService, Algoritmit, OpsinTila, Notifikaatiot) {
  $scope.sorter = ListaSorter.init($scope);
  $scope.opsiLista = true;
  $scope.tilat = ['luonnos', 'valmis', 'poistettu'];
  $scope.items = OpsListaService.query();

  $scope.paginate = {
    perPage: 10,
    current: 1
  };

  function updatePageinfo() {
    $scope.lastVisible = Math.min(($scope.paginate.current - 1) * $scope.paginate.perPage + $scope.paginate.perPage,
      $scope.filtered.length);
  }

  function generoiStatsit(items) {
    var statsit = {};
    statsit.maaratKielittain = _.reduce(items, function(acc, item) {
      _.each(item.julkaisukielet, function(jk) {
        acc[jk] = _.isNumber(acc[jk]) ? acc[jk] + 1 : 0;
      });
      return acc;
    }, {});
    _.each(items, function(item) {
      item.$$taso = 1;
      var koulutasoinen = _.any(item.organisaatiot, function(org) {
        return _.findIndex(org.tyypit, 'Oppilaitos') !== -1;
      });

      if (koulutasoinen) {
        item.$$taso = 0;
      }

      if (_.size(item.kunnat) > 1) {
        item.$$taso = 2;
      }
    });

    statsit.maaratTyypeittain = _.groupBy(items, 'koulutustyyppi');
    statsit.maaratTasoittain = _.groupBy(items, '$$taso');
    return statsit;
  }

  $scope.$watch('items', function () {
    $scope.search.changed();
    updatePageinfo();

    $scope.statsit = generoiStatsit($scope.items);
  }, true);

  $scope.$watch('search.tilaRajain', function () {
    $scope.search.changed($scope.search.term);
  });

  $scope.$watch('paginate.current', updatePageinfo);

  $scope.search = {
    term: '',
    tilaRajain: '',
    changed: function (value) {
      $scope.paginate.current = 1;
      $scope.filtered = _.filter($scope.items, function (item) {
        if ($scope.search.tilaRajain && item.tila !== $scope.search.tilaRajain) {
          return false;
        }
        if (value) {
          var nameMatch = Algoritmit.match(value, item.nimi);
          var kuntaMatch = _.any(item.kunnat, function (kunta) {
            return Algoritmit.match(value, kunta.nimi);
          });
          return nameMatch || kuntaMatch;
        }
        return true;
      });
      updatePageinfo();
    }
  };

  $scope.palauta = function (ops) {
    OpsinTila.palauta(ops, function (res) {
      ops.tila = res.tila;
      Notifikaatiot.onnistui('tallennettu-ok');
    });
  };

});
