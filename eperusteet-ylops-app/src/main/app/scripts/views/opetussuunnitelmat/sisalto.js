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
.service('TekstikappaleOps', function ($state, $stateParams, OpetussuunnitelmanTekstit, Notifikaatiot, Algoritmit,
  Varmistusdialogi, Kaanna, OpsService, $rootScope) {
  function mapSisalto(root) {
    return {
      id: root.id,
      tekstiKappale: _.isObject(root.tekstiKappale) ? _.pick(root.tekstiKappale, 'id', 'tunniste') : null,
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

  this.rakennaSivunavi = function(tekstit, isPohja) {
    var state = isPohja ? 'root.pohjat.yksi.sisalto.tekstikappale' : 'root.opetussuunnitelmat.yksi.sisalto.tekstikappale';

    return _(_.deepFlatten(tekstit, _.property('lapset'), function(obj, depth) {
      if (obj.tekstiKappale) {
        var result = {
          id: obj.id,
          label: obj.tekstiKappale.nimi,
          valmis: obj.tekstiKappale.valmis,
          depth: depth - 1,
          url: depth > 1 || isPohja ? $state.href(state, { tekstikappaleId: obj.id }) : undefined
        };
        return result;
      }
    }))
    .flatten(true)
    .compact()
    .value();
  };

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
            '<div style="background: {{ taustanVari }}" class="tekstisisalto-solmu" ng-class="{ \'new-halo\': node.$$newHalo }">' +
            '    <span ng-show="node.$$depth !== 0" class="treehandle" icon-role="drag"></span>' +
            '    <span ng-bind="node.tekstiKappale.nimi || \'nimeton\' | kaanna"></span>' +
            '</div>'
            );
    $templateCache.put('sisaltoNodeTemplate', '' +
            '<div style="background: {{ taustanVari }}" class="tekstisisalto-solmu" ng-class="{ \'search-halo\': node.$$showHalo, \'new-halo\': node.$$newHalo }">' +
            '    <span class="tekstisisalto-chevron action-link" ng-show="node.$$hasChildren" href="" ng-click="node.$$hidden = !node.$$hidden">' +
            '       <span ng-show="node.$$hidden" icon-role="chevron-right"></span>' +
            '       <span ng-hide="node.$$hidden" icon-role="chevron-down"></span>' +
            '    </span>' +
            '    <a ng-if="node.$$depth > 0" href="" ui-sref=".tekstikappale({ tekstikappaleId: node.id })"' +
            '          ng-bind="node.tekstiKappale.nimi || \'nimeton\' | kaanna">' +
            '    </a>' +
            '    <span ng-if="node.$$depth === 0" ng-bind="node.tekstiKappale.nimi || \'nimeton\' | kaanna"></span>' +
            '    <a ng-click="addLapsi(node)" style="margin-left: 10px" ng-show="node.$$depth === 0" href="" icon-role="add" tooltip="{{ \'lisaa-aliotsikko\' | kaanna }}"></a>' +
            '    <span class="pull-right">' +
            '        <span valmius-ikoni="node.tekstiKappale"></span>' +
            '        <span class="muokattu-aika">' +
            '             <span kaanna="\'muokattu-viimeksi\'"></span>:' +
            '             <span ng-bind="node.tekstiKappale.muokattu | aikaleima"></span>' +
            '        </span>' +
            '    </span>' +
            '</div>'
            );
})
.controller('OpetussuunnitelmaSisaltoController', function ($scope, $state, OpetussuunnitelmanTekstit, $templateCache, $timeout,
      Notifikaatiot, opsService, opsModel, $rootScope, $stateParams, TekstikappaleOps, Utils, Lukko, $q, Editointikontrollit,
      $modal, OpetussuunnitelmaCRUD, tekstit, Algoritmit, OpsNavigaatio) {
  $scope.model = opsModel;
  $scope.model.tekstit = tekstit;
  $scope.navi = TekstikappaleOps.rakennaSivunavi(tekstit);
  $scope.opened = false;
  $scope.shouldShow = function() {
    return $state.is('root.opetussuunnitelmat.yksi.sisalto.tekstikappale');
  };

  $scope.rajaus = {
    term: '',
    onUpdate: function(term) {
      Algoritmit.traverse($scope.model.tekstit, 'lapset', function(node) {
        node.$$showHalo = false;

        if (_.isEmpty(term)) {
          node.$$searchHidden = false;
        }
        else if (!Algoritmit.match(term, node.tekstiKappale.nimi)) {
          node.$$searchHidden = true;
        }
        else {
          node.$$searchHidden = false;
          node.$$showHalo = true;
          var p = node.$$traverseParent;
          while (p) {
            p.$$searchHidden = false;
            p = p.$$traverseParent;
          }
        }
      });
    }
  };


  $scope.toggleState = function() {
    $scope.opened = !$scope.opened;
    _.deepFlatten(tekstit, _.property('lapset'), function(obj, depth) {
        if (depth > 1) {
          obj.$$hidden = $scope.opened;
        }
      });
  };
  $scope.toggleState();

  var commonParams = {
    opsId: $stateParams.id,
  };

  // FIXME: Ota kunnon editointikontrollit käyttöön
  Editointikontrollit.registerCallback({
    edit: function() {
    },
    asyncValidate: function(cb) {
      TekstikappaleOps.saveRakenne($scope.model, function () {
        Lukko.unlock(commonParams, cb);
      });
    },
    save: function() {
      Lukko.unlock(commonParams);
      $scope.$$isRakenneMuokkaus = false;
      $rootScope.$broadcast('genericTree:refresh');
    },
    cancel: function() {
      Lukko.unlock(commonParams, $state.reload);
    }
  });

  $scope.muokkaaRakennetta = function() {
    Lukko.lock(commonParams, function() {
      Editointikontrollit.startEditing();
      $scope.$$isRakenneMuokkaus = true;
      $rootScope.$broadcast('genericTree:refresh');
    });
  };

  $scope.sortableLiittamattomatConfig = {
    connectWith: '.recursivetree',
    handle: '.treehandle',
    placeholder: 'placeholder'
  };

  $scope.sortableConfig = {
    placeholder: 'placeholder'
  };

  $scope.tekstitProvider = $q.when({
    root: _.constant($q.when($scope.model.tekstit)),
    hidden: function(node) {
        if ($scope.$$isRakenneMuokkaus || !node.$$nodeParent) {
          return false;
        }
        else {
          return (_.isEmpty($scope.rajaus.term) && node.$$nodeParent.$$hidden) || node.$$searchHidden;
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
    acceptDrop: function(node, target, parentScope) {
      return target !== $scope.model.tekstit || parentScope === $scope;
    },
    sortableClass: function(node) {
      var result = '';
      if (node !== $scope.model.tekstit) {
        result += 'is-draggable-into';
        if ($scope.$$isRakenneMuokkaus && _.isEmpty(node.lapset)) {
          result += ' recursivetree-empty';
        }
      }
      return result;
    },
    extension: function(node, scope) {
      scope.taustanVari = node.$$depth === 0 ? '#f2f2f9' : '#ffffff';
      scope.addLapsi = lisaaTekstikappale;
    }
  });

  $scope.muokkaaAlihierarkiaa = function() {
    $modal.open({
      templateUrl: 'views/opetussuunnitelmat/modals/alihierarkia.html',
      controller: 'OpsAlihierarkiaModalController',
      size: 'lg',
      resolve: {
        ops: _.constant($q.when(_.cloneDeep($scope.model))),
        aliOpsit: function() {
          return OpetussuunnitelmaCRUD.opetussuunnitelmat({
            opsId: $scope.model.id
          }).$promise;
        }
      }
    })
    .result.then(_.noop);
  };

  function lisaaTekstikappale(parentTk) {
    OpetussuunnitelmanTekstit.addChild({
      opsId: $stateParams.id,
      viiteId: parentTk.id
    }, {
      tekstiKappale: {
        nimi: { fi: 'Uusi tekstikappale' }
      },
      lapset: []
    }).$promise
    .then(function(res) {
      res.lapset = [];
      res.$$newHalo = true;
      parentTk.lapset.push(res);
    })
    .catch(Notifikaatiot.serverCb);
  }
})
.controller('OpsAlihierarkiaModalController', function($scope, $modalInstance, ops, aliOpsit, OpetussuunnitelmaCRUD) {
  $scope.ops = ops;
  $scope.aliOpsit = aliOpsit;

  $scope.paivitaRakenne = function() {
    OpetussuunnitelmaCRUD.opetussuunnitelmatSync({
      id: ops.id
    }).$promise.then(function(res) {
      console.log(res);
    })
    .catch(function(err) {
      console.log(err);
    });
  };

  $scope.ok = $modalInstance.close;
  $scope.peruuta = $modalInstance.dismiss;
});
