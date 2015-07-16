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
.factory('KasitteetResource', function($resource, SERVICE_LOC) {
  return $resource(SERVICE_LOC + '/opetussuunnitelmat/:opsId/termisto/:id', {
    opsId: '@opsId',
    id: '@id'
  }, { });
})
.service('KasitteetService', function(KasitteetResource) {
  function makeKey(item) {
    var termi = _.first(_.compact(_.values(item.termi))) || '';
    return termi.replace(/[^a-zA-Z0-9]/g, '') + (new Date()).getTime();
  }

  var save = function(opsId, item) {
    if (!item.avain) { item.avain = makeKey(item); }
    return KasitteetResource.save((item.id ?
         { opsId: opsId, id: item.id } :
         { opsId: opsId }),
        item).$promise;
  };

  var get = function(opsId, id) {
    return id ? KasitteetResource.get({
          opsId: opsId,
          id: id
        }).$promise : KasitteetResource.query({ opsId: opsId }).$promise;
  };

  var remove = function(opsId, id) {
    return KasitteetResource.remove({
      opsId: opsId,
      id: id
    }).$promise;
  };

  return {
    save: save,
    get: get,
    remove: remove
  };
})
.controller('KasitteetController', function($scope, $stateParams, $modal, KasitteetService, Algoritmit, Kaanna, Varmistusdialogi) {
  $scope.paginate = {
    perPage: 7,
    current: 1
  };

  $scope.termisto = [];
  $scope.filtered = [];

  function sorter(item) {
    return Kaanna.kaanna(item.termi).toLowerCase();
  }

  function refresh() {
    KasitteetService.get($stateParams.id).then(function(res) {
      $scope.termisto = res;
      $scope.search.changed($scope.search.phrase);
    });
  }

  $scope.search = {
    phrase: '',
    changed: function (value) {
      $scope.filtered = _($scope.termisto)
        .filter(function(termi) {
          return !value || Algoritmit.match(value, termi.termi) || Algoritmit.match(value, termi.selitys);
        })
        .sortBy(sorter)
        .value();
      $scope.paginate.current = 1;
    }
  };

  $scope.filterer = function(item) { return !item.$hidden; };

  $scope.edit = function(item, index) {
    $modal.open({
      templateUrl: 'views/opetussuunnitelmat/modals/termisto.html',
      controller: 'KasitteetMuokkausController',
      size: 'lg',
      resolve: { termimodel: _.constant(_.cloneDeep(item)) }
    })
    .result.then(function(data) {
      KasitteetService.save($stateParams.id, data).then(function(res) {
        if (index) {
          _.assign($scope.termisto[index], res);
        }
        else {
          $scope.termisto.push(res);
          refresh();
        }
      });
    });
  };

  $scope.remove = function(item) {
    Varmistusdialogi.dialogi({
      otsikko: 'vahvista-poisto',
      teksti: 'poistetaanko-termi',
    })(function() {
      KasitteetService.remove($stateParams.id, item.id)
        .then(function() {
          _.remove($scope.termisto, item);
          $scope.search.changed($scope.search.phrase);
          refresh();
        });
    });
  };

  refresh();
})
.controller('KasitteetMuokkausController', function ($scope, termimodel, Varmistusdialogi,
    $modalInstance, $rootScope, KasitteetService) {
  $scope.termimodel = termimodel;
  // $scope.creating = !termimodel;
  // if ($scope.creating) {
  //   $scope.termimodel = TermistoService.newTermi();
  // }

  $scope.ok = function () {
    $rootScope.$broadcast('notifyCKEditor');
    $modalInstance.close($scope.termimodel);
  };

  // $scope.delete = function () {
  //   Varmistusdialogi.dialogi({
  //     otsikko: 'vahvista-poisto',
  //     teksti: 'poistetaanko-termi',
  //   })(function() {
  //     $modalInstance.close(_.extend($scope.termimodel, {$delete: true}));
  //   });
  // };
});
