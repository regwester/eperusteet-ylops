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
/* global _ */

ylopsApp
.service('OpsinTila', function ($modal, OpetussuunnitelmaCRUD, Notifikaatiot) {
  this.palauta = function (ops, cb) {
    OpetussuunnitelmaCRUD.palauta({opsId: ops.id}, null, cb, Notifikaatiot.serverCb);
  };

  this.save = function (ops, tila, cb = _.noop) {
    OpetussuunnitelmaCRUD.setTila({ opsId: ops.id, tila: tila }).$promise
      .then(cb)
      .catch(() => {
        OpetussuunnitelmaCRUD.validoi({ opsId: ops.id }).$promise.then(res => {
          $modal.open({
            templateUrl: 'views/opetussuunnitelmat/modals/validointivirheet.html',
            controller: 'ValidointivirheetController',
            size: 'lg',
            resolve: { virheet: _.constant(res) }
          }).result.then(_.noop);
        });
    });
  };
})

.service('OpsinTilanvaihto', function ($modal) {
  var that = this;
  this.start = function(parametrit, setFn, successCb = _.noop) {
    if (_.isFunction(setFn)) {
      that.setFn = setFn;
    }
    $modal.open({
      templateUrl: 'views/opetussuunnitelmat/modals/tilanvaihto.html',
      controller: 'OpsinTilanvaihtoController',
      resolve: {
        data: () => {
          return {
            isPohja: parametrit.isPohja,
            oldStatus: parametrit.currentStatus,
            mahdollisetTilat: parametrit.mahdollisetTilat,
            statuses: _.map(parametrit.mahdollisetTilat, function (item) {
              return {key: item, description: 'tilakuvaus-' + item};
            })
          };
        }
      }
    }).result.then(res => {
      that.setFn(res);
      successCb(res);
    });
  };
  this.set = function(status, successCb) {
    that.setFn(status, successCb);
  };
})

.controller('ValidointivirheetController', function ($scope, $modalInstance, $state, virheet) {
  const create = (l, field) => _(l)
    .map(field)
    .flatten()
    .value();

  $scope.virheet = create(virheet ,"virheet");
  $scope.varoitukset = create(virheet ,"varoitukset");
  $scope.huomiot = create(virheet ,"huomiot");
  $scope.eiVirheita =
    _.isEmpty($scope.virheet) &&
    _.isEmpty($scope.varoitukset) &&
    _.isEmpty($scope.huomiot);

  $scope.ok = $modalInstance.dismiss;
})

.controller('OpsinTilanvaihtoController', function ($scope, $modalInstance, $state, data) {
  $scope.data = data;
  $scope.data.selected = null;
  $scope.data.editable = false;
  $scope.title = data.isPohja ? 'aseta-pohjan-tila' : 'aseta-opsin-tila';

  $scope.valitse = function () {
    $modalInstance.close($scope.data.selected);
  };

  $scope.peruuta = function () {
    $modalInstance.dismiss();
  };
});
