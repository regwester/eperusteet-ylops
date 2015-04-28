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
/* global CKEDITOR,$ */

ylopsApp
  .run(function() {
    CKEDITOR.disableAutoInline = true;
    // load external plugins
    var basePath = CKEDITOR.basePath;
    basePath = basePath.substr(0, basePath.indexOf('bower_components/'));
    CKEDITOR.plugins.addExternal('epimage', basePath + 'ckeditor-plugins/epimage/', 'plugin.js');
  })
  .constant('editorLayouts', {
    minimal:
      [
        { name: 'clipboard', items : [ 'Cut','Copy','-','Undo','Redo' ] },
        { name: 'tools', items : [ 'About' ] }
      ],
    simplified:
      [
        { name: 'clipboard', items : [ 'Cut','Copy','Paste','-','Undo','Redo' ] },
        { name: 'basicstyles', items : [ 'Bold','Italic','Underline','Strike','-','RemoveFormat' ] },
        { name: 'paragraph', items : [ 'NumberedList','BulletedList','-','Outdent','Indent'] },
        { name: 'tools', items : [ 'About' ] }
      ],
    normal:
      [
        { name: 'clipboard', items : [ 'Cut','Copy','Paste','PasteText','PasteFromWord','-','Undo','Redo' ] },
        { name: 'basicstyles', items : [ 'Bold','Italic','Underline','Strike','-','RemoveFormat' ] },
        { name: 'paragraph', items : [ 'NumberedList','BulletedList','-','Outdent','Indent','-','Blockquote' ] },
        { name: 'insert', items : [ 'Table','HorizontalRule','SpecialChar','Link','epimage'] },
        { name: 'tools', items : [ 'About' ] }
      ]
  })

  .config(function(uiSelectConfig) {
    uiSelectConfig.theme = 'bootstrap';
  })

  .directive('ckeditor', function($q, $filter, $rootScope, editorLayouts, $timeout, Kaanna) {
    return {
      priority: 10,
      restrict: 'A',
      require: 'ngModel',
      scope: {
        editorPlaceholder: '@?',
        editMode: '@?editingEnabled'
      },
      link: function(scope, element, attrs, ctrl) {
        var placeholderText = null;
        var editingEnabled = (scope.editMode || 'true') === 'true';

        if(editingEnabled) {
          element.addClass('edit-mode');
        }
        element.attr('contenteditable', 'true');

        function getPlaceholder() {
          if(scope.editorPlaceholder) {
            return $filter('kaanna')(scope.editorPlaceholder);
          } else {
            return '';
          }
        }

        var editor = CKEDITOR.instances[attrs.id];
        if (editor) {
          return;
        }

        var toolbarLayout;
        if(!_.isEmpty(attrs.layout) && !_.isEmpty(editorLayouts[attrs.layout])) {
          toolbarLayout = editorLayouts[attrs.layout];
        } else {
          if(element.is('div')) {
            toolbarLayout = editorLayouts.normal;
          } else {
            toolbarLayout = editorLayouts.minimal;
          }
        }

        var ready = false;
        var deferredcall = null;
        editor = CKEDITOR.inline(element[0], {
          toolbar: toolbarLayout,
          removePlugins: 'resize,elementspath,scayt,wsc,image',
          extraPlugins: 'divarea,sharedspace,epimage',
          // TODO: doesn't remove src attribute from img
          disallowedContent: 'br; tr td{width,height}; img[src]',
          extraAllowedContent: 'img[data-uid]',
          language: 'fi',
          'entities_latin': false,
          sharedSpaces: {
            top: 'ck-toolbar-top'
          },
          readOnly: !editingEnabled,
          title: false,
          customData: {
            kaanna: Kaanna.kaanna,
          }
        });

        // poistetaan enterin käyttö, jos kyseessä on yhden rivin syöttö
        if(!element.is('div')) {
          editor.on('key', function(event) {
            if(event.data.keyCode === 13) {
              event.cancel();
            }
          });
        }

        scope.$on('$translateChangeSuccess', function() {
          placeholderText = getPlaceholder();
          ctrl.$render();
        });

        function setReadOnly(state) {
          editor.setReadOnly(state);
        }

        scope.$on('enableEditing', function() {
          editingEnabled = true;
          if (ready) {
            setReadOnly(!editingEnabled);
          } else {
            deferredcall = _.partial(setReadOnly, !editingEnabled);
          }
          element.addClass('edit-mode');
        });

        scope.$on('disableEditing', function() {
          editingEnabled = false;
          editor.setReadOnly(!editingEnabled);
          element.removeClass('edit-mode');
        });

        scope.$on('$destroy', function() {
          $timeout(function () {
            if (editor && editor.status !== 'destroyed') {
              editor.destroy(false);
            }
          });

        });

        editor.on('focus', function() {
          if (editingEnabled) {
            element.removeClass('has-placeholder');
            $('#toolbar').show();
            if(_.isEmpty(ctrl.$viewValue)) {
              editor.setData('');
            }
          }
        });

        function trim(obj) {
          // Replace all nbsps with normal spaces, remove extra spaces and trim ends.
          if (_.isString(obj)) {
            obj = obj.replace(/&nbsp;/gi, ' ').replace(/ +/g, ' ').trim();
          }
          return obj;
        }

        var dataSavedOnNotification = false;
        scope.$on('notifyCKEditor', function() {
          if(editor.checkDirty()) {
            dataSavedOnNotification = true;
            var data = element.hasClass('has-placeholder') ? '' : editor.getData();
            ctrl.$setViewValue(trim(data));
          }
          $('#toolbar').hide();
        });

        function updateModel () {
          if (editor.checkDirty()) {
            var data = editor.getData();
            scope.$apply(function() {
              ctrl.$setViewValue(trim(data));
              // scope.$broadcast('edited');
            });
            if(_.isEmpty(data)) {
              element.addClass('has-placeholder');
              editor.setData(placeholderText);
            }
          }

        }

        editor.on('blur', function() {
          if (dataSavedOnNotification) {
            dataSavedOnNotification = false;
            return;
          }
          updateModel();
          $('#toolbar').hide();
        });

        editor.on('instanceReady', function () {
          ready = true;
          if (deferredcall) {
            deferredcall();
            deferredcall = null;
          }
          $rootScope.$broadcast('ckEditorInstanceReady');
        });

        // model -> view

        ctrl.$render = function() {
          if (editor) {
            if(angular.isUndefined(ctrl.$viewValue) || (angular.isString(ctrl.$viewValue) && _.isEmpty(ctrl.$viewValue) && placeholderText)) {
              element.addClass('has-placeholder');
              editor.setData(placeholderText);
              editor.resetDirty();
            } else {
              element.removeClass('has-placeholder');
              editor.setData(ctrl.$viewValue);
            }
          }
        };

        placeholderText = getPlaceholder();
        ctrl.$render();
      }
    };
  })

  .service('EpImageService', function ($q, OpsService, OpsinKuvat, Upload, YlopsResources) {
    this.getAll = function () {
      return OpsinKuvat.query({opsId: OpsService.getId()}).$promise;
      // TODO fetch list from backend
      /*var deferred = $q.defer();
      deferred.resolve([
        {nimi: {fi: 'Kuva 1'}, id: '44443333ffff'},
        {nimi: {fi: 'Kuva 2 hepasta'}, id: 'aaaabbbb6666'},
      ]);
      return deferred.promise;*/
    };

    this.save = function (image) {
      var deferred = $q.defer();
      var url = (YlopsResources.OPS + '/kuvat').replace(':opsId', '' + OpsService.getId());
      console.log(image, url);

      Upload.upload({
        url: url,
        file: image,
        fields: {
          nimi: {
            fi: image.name,
            sv: image.name
          }
        },
        success: function (data) {
          deferred.resolve(data);
        }
      });
      return deferred.promise;

      // TODO save to backend
      /*console.log(image);
      if (!image.id) {
        image.id = '' + _.random(9999);
      }
      var deferred = $q.defer();
      deferred.resolve(image);
      return deferred.promise;*/
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

  });
