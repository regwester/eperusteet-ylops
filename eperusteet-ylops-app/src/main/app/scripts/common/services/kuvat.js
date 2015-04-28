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
  .service('EpImageService', function ($q, OpsService, OpsinKuvat, Upload, YlopsResources) {
    this.getAll = function () {
      return OpsinKuvat.query({opsId: OpsService.getId()}).$promise;
    };

    this.save = function (image) {
      var deferred = $q.defer();
      var url = (YlopsResources.OPS + '/kuvat').replace(':opsId', '' + OpsService.getId());

      Upload.upload({
        url: url,
        file: image,
        fields: {
          nimi: image.name
        },
        success: function (data) {
          deferred.resolve(data);
        }
      });
      return deferred.promise;
    };

    this.getUrl = function (image) {
      return (YlopsResources.OPS + '/kuvat').replace(':opsId', '' + OpsService.getId()) + '/' + image.id;
    };
  })

  .controller('EpImagePluginController', function ($scope, EpImageService, Kaanna, Algoritmit, $timeout) {
    $scope.service = EpImageService;
    $scope.filtered = [];
    $scope.images = [];
    $scope.showPreview = false;
    $scope.model = {
      files: [],
      chosen: null
    };

    $scope.$watch('model.files[0]', function () {
      if (_.isArray($scope.model.files) && $scope.model.files.length > 0) {
        $scope.showPreview = true;
      }
    });
    $scope.$watch('model.chosen', function () {
      $scope.showPreview = false;
    });

    var callback = angular.noop;
    var setDeferred = null;

    function setChosenValue (value) {
      var found = _.find($scope.images, function (image) {
        return image.id === value;
      });
      $scope.model.chosen = found || null;
    }

    function doSort(items) {
      return _.sortBy(items, function (item) {
        return Kaanna.kaanna(item.nimi).toLowerCase();
      });
    }

    $scope.urlForImage = function (image) {
      return $scope.service.getUrl(image);
    };

    $scope.init = function () {
      $scope.service.getAll().then(function (res) {
        $scope.images = res;
        $scope.filtered = doSort(res);
        if (setDeferred) {
          setChosenValue(_.cloneDeep(setDeferred));
          setDeferred = null;
        }
      });
    };

    $scope.filterImages = function (value) {
      $scope.filtered = _.filter(doSort($scope.images), function (item) {
        return Algoritmit.match(value, item.nimi);
      });
    };

    // data from angular model to plugin
    $scope.registerListener = function (cb) {
      callback = cb;
    };
    $scope.$watch('model.chosen', function (value) {
      callback(value);
    });

    // data from plugin to angular model
    $scope.setValue = function (value) {
      $scope.$apply(function () {
        if (_.isEmpty($scope.images)) {
          setDeferred = value;
        } else {
          setChosenValue(value);
        }
      });
    };

    $scope.closeMessage = function () {
      $scope.message = null;
    };

    $scope.saveNew = function () {
      var image = $scope.model.files[0];
      $scope.service.save(image).then(function (res) {
        $scope.message = 'epimage-plugin-tallennettu';
        $timeout(function () {
          $scope.closeMessage();
        }, 8000);
        // TODO res should be the image id
        setDeferred = _.clone(res);
        $scope.init();
      });
    };

  })

  .directive('kuvalinkit', function ($timeout, EpImageService) {
    return {
      restrict: 'A',
      link: function (scope, element) {
        function setup() {
          element.find('img[data-uid]').each(function () {
            var el = angular.element(this);
            el.attr('src', EpImageService.getUrl({id: el.attr('data-uid')}));
          });
        }

        function refresh() {
          $timeout(function () {
            setup();
          }, 500);
        }
        refresh();
      }
    };
  });
