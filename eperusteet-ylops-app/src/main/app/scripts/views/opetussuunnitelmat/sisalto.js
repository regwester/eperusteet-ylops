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
.service('TekstikappaleOps', function (OpetussuunnitelmanTekstit, Notifikaatiot, Algoritmit,
  Varmistusdialogi, Kaanna, OpsService, $rootScope) {
  function mapSisalto(root) {
    return {
      id: root.id,
      lapset: _.map(root.lapset, mapSisalto)
    };
  }

  function saveRakenne(model, cb) {
    var postdata = mapSisalto(model.tekstit);
    OpetussuunnitelmanTekstit.save({
      opsId: model.id,
      viiteId: model.tekstit.id
    }, postdata, function () {
      Notifikaatiot.onnistui('tallennettu-ok');
      cb();
    }, Notifikaatiot.serverCb);
  }

  function lisaa(parent, opsId, uusi, cb) {
    var newNode = {tekstiKappale: { nimi: angular.copy(uusi) }, lapset: []};

    OpetussuunnitelmanTekstit.setChild({
      parentId: parent.id,
      opsId: opsId
    }, newNode, function (res) {
      var otsikko = _.cloneDeep(newNode.tekstiKappale.nimi);
      newNode.id = res.id;
      newNode.omistussuhde = res.omistussuhde;
      newNode.tekstiKappale = res.tekstiKappale;
      res.tekstiKappale.nimi = otsikko;
      // TODO: lapsi-APIa käyttämällä ei tekstikappale tallennu samalla pyynnöllä
      OpetussuunnitelmanTekstit.save({ opsId: opsId }, res, function () {
        Notifikaatiot.onnistui('tallennettu-ok');
        cb(res);
        OpsService.refetch(function () {
          $rootScope.$broadcast('rakenne:updated');
        });
      }, Notifikaatiot.serverCb);
    }, Notifikaatiot.serverCb);
  }

  function deleteKappale(model, osio, opsId, kappale, cb) {
    var params = {opsId: opsId};
    OpetussuunnitelmanTekstit.delete(params, kappale, function () {
      var foundIndex = null, foundList = null;
      Algoritmit.traverse(model[osio], 'lapset', function (lapsi, depth, index, arr) {
        if (lapsi === kappale) {
          foundIndex = index;
          foundList = arr;
          return true;
        }
      });
      if (foundList) {
        foundList.splice(foundIndex, 1);
      }
      OpsService.refetch(function () {
        $rootScope.$broadcast('rakenne:updated');
        (cb || angular.noop)();
      });
      Notifikaatiot.onnistui('poisto-onnistui');
    }, Notifikaatiot.serverCb);
  }

  this.saveRakenne = saveRakenne;
  this.lisaa = lisaa;
  this.delete = deleteKappale;
  this.varmistusdialogi = function (nimi, successCb, failureCb) {
    Varmistusdialogi.dialogi({
      otsikko: 'varmista-poisto',
      teksti: Kaanna.kaanna('poista-tekstikappale-teksti', {nimi: Kaanna.kaanna(nimi)}),
      primaryBtn: 'poista',
      successCb: successCb,
      failureCb: failureCb || angular.noop
    })();
  };
})
.run(function($templateCache) {
    $templateCache.put('sisaltoNodeEditingTemplate', '' +
            '<div ng-class="{ \'tekstisisalto-otsikko-solmu\': node.$$hasChildren }"' +
            '     class="tekstisisalto-solmu">' +
            '    <span class="treehandle" icon-role="drag"></span>' +
            '    <span ng-bind="node.tekstiKappale.nimi || \'nimeton\' | kaanna"></span>' +
            '    <span class="pull-right">' +
            '        <a icon-role="remove" ng-click="poistaTekstikappale(node.$$nodeParent, node)"></a>' +
            '    </span>' +
            '</div>'
            );
    $templateCache.put('sisaltoNodeTemplate', '' +
            '<div ng-class="{ \'tekstisisalto-otsikko-solmu\': node.$$hasChildren }" class="tekstisisalto-solmu">' +
            '    <span class="tekstisisalto-chevron action-link" ng-show="node.$$hasChildren" href="" ng-click="node.$$hidden = !node.$$hidden">' +
            '       <span ng-show="node.$$hidden" icon-role="chevron-right"></span>' +
            '       <span ng-hide="node.$$hidden" icon-role="chevron-down"></span>' +
            '    </span>' +
            '    <a href="" ui-sref="root.opetussuunnitelmat.yksi.tekstikappale({ tekstikappaleId: node.id })">' +
            '       <span ng-bind="node.tekstiKappale.nimi || \'nimeton\' | kaanna"></span>' +
            '    </a>' +
            '    <span class="pull-right">' +
            '        <span valmius-ikoni="node"></span>' +
            '        <span ng-bind="node.tekstiKappale.muokattu | aikaleima"></span>' +
            '    </span>' +
            '</div>'
            );
})
.controller('OpetussuunnitelmaSisaltoController', function ($scope, $state, OpetussuunnitelmanTekstit, $templateCache,
      Notifikaatiot, opsService, opsModel, $rootScope, $stateParams, TekstikappaleOps, Utils, Lukko, $q) {
  $scope.uusi = {nimi: {}};
  $scope.lukkotiedot = null;
  $scope.model = opsService.get($stateParams.id) || opsModel;

  $scope.muokkaaRakennetta = function() {
    $scope.$$isRakenneMuokkaus = !$scope.$$isRakenneMuokkaus ;
    $rootScope.$broadcast('genericTree:refresh');
  };

  $scope.sortableConfig = {
    placeholder: 'placeholder',
    // start: function(e, ui) {
    //   console.log(e, ui);
    //   ui.placeholder.html('<div class="placeholder">morjens</div>');
    // }
  };

  $scope.tekstitProvider = $q(function(resolve) {
    resolve({
      root: _.constant($q.when($scope.model.tekstit)),
      hidden: function(node) {
          if ($scope.$$isRakenneMuokkaus || !node.$$nodeParent) {
            return false;
          }
          else {
            return node.$$nodeParent.$$hidden;
          }
      },
      template: function() {
        return $scope.$$isRakenneMuokkaus  ? 'sisaltoNodeEditingTemplate' : 'sisaltoNodeTemplate';
      },
      children: function(node) {
        return $q.when(node && node.lapset ? node.lapset : []);
      },
      useUiSortable: function() {
        return !$scope.$$isRakenneMuokkaus;
      },
      initNode: function(node) {
        // console.log('initing node:', node);
      },
      extension: function(node, scope) {
        scope.poistaTekstikappale = function(osio, node) {
          lockTeksti(node.id, function () {
            TekstikappaleOps.varmistusdialogi(node.tekstiKappale.nimi, function () {
              osio = osio || $scope.model.tekstit;
              TekstikappaleOps.delete($scope.model, osio, $stateParams.id, node, function() {
                _.remove(osio.lapset, node);
              });
            }, function () {
              unlockTeksti(node.id);
            });
          });
        };
      }
    });
  });

  $scope.isAdding = function () {
    return _.any($scope.model.tekstit.lapset, '$$adding');
  };

  $scope.hasText = function(str) {
    return Utils.hasLocalizedText(str);
  };

  $scope.canRemove = function (kappale) {
    return kappale.omistussuhde === 'oma';
  };

  var commonParams = {
    opsId: $stateParams.id,
  };
  Lukko.isLocked($scope, commonParams);

  function fetch(cb, notify) {
    opsService.refetch(function (res) {
      $scope.model = res;
      (cb || angular.noop)(res);
      if (notify) {
        $rootScope.$broadcast('rakenne:updated');
      }
    });
  }

  $scope.kappaleEdit = null;

  function stopEvent(event) {
    event.preventDefault();
    event.stopPropagation();
  }

  function lockTeksti(id, cb) {
    return Lukko.lockTekstikappale(_.extend({viiteId: id}, commonParams), cb);
  }

  function unlockTeksti(id, cb) {
    return Lukko.unlockTekstikappale(_.extend({viiteId: id}, commonParams), cb);
  }

  $scope.lisaaTekstikappale = function() {
      OpetussuunnitelmanTekstit.save({
        opsId: $stateParams.id
      }, {}, function(res) {
        Notifikaatiot.onnistui('tallennettu-ok');
        $scope.model.tekstit.lapset.push(res);
      }, Notifikaatiot.serverCb);
  };

  $scope.rakenne = {
    add: function (osio, event) {
      stopEvent(event);
      osio.$$adding = true;
    },
    cancelAdd: function (osio) {
      osio.$$adding = false;
      $scope.uusi = {nimi: {}};
    },
    cancelTitle: function (kappale) {
      unlockTeksti(kappale.id, function () {
        $scope.kappaleEdit = null;
        kappale.tekstiKappale = _.cloneDeep(kappale.$original);
        delete kappale.$original;
      });
    },
    saveTitle: function (kappale) {
      var params = {opsId: $stateParams.id};
      OpetussuunnitelmanTekstit.save(params, _.omit(kappale, 'lapset'), function () {
        unlockTeksti(kappale.id, function () {
          $scope.kappaleEdit = null;
          Notifikaatiot.onnistui('tallennettu-ok');
          opsService.refetch(function () {
            $rootScope.$broadcast('rakenne:updated');
          });
          delete kappale.$original;
        });
      }, Notifikaatiot.serverCb);
    },
    edit: function (osio, event) {
      stopEvent(event);
      $scope.rakenne.cancelAdd(osio);
      Lukko.lock(commonParams, function () {
        osio.$$edit = true;
      });
    },
    cancel: function (osio) {
      Lukko.unlock(commonParams, function () {
        $scope.lukkotiedot = null;
        fetch(function() {
          osio.$$edit = false;
        });
      });
    },
    save: function (osio) {
      TekstikappaleOps.saveRakenne($scope.model, function () {
        Lukko.unlock(commonParams, function () {
          $scope.lukkotiedot = null;
          osio.$$edit = false;
          fetch(angular.noop, true);
        });
      });
    }
  };
});
