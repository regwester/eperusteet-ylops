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
.controller('PohjaController', function ($scope, $state, pohjaModel, $stateParams) {
  if ($state.current.name === 'root.pohjat.yksi') {
    $state.go('root.pohjat.yksi.sisalto', {}, {location: 'replace'});
  }
  $scope.model = pohjaModel;
  $scope.luonnissa = $stateParams.pohjaId === 'uusi';
  // FIXME: Miksi tämä on olemassa?
  // $scope.$on('rakenne:updated', function () {
  //   $scope.model = opsService.getPohja();
  // });
})
.controller('PohjaListaController', function ($scope, $state, OpetussuunnitelmaCRUD, ListaSorter, Notifikaatiot) {
  $scope.pohjaMaxLimit = 9999;
  $scope.pohjaMinLimit = 7;

  OpetussuunnitelmaCRUD.query({tyyppi: 'pohja'}, function (res){
   $scope.items = _.filter(res, function (item) {
     return item.tila !== 'poistettu';
   });
   $scope.items.$resolved = true;
  }, Notifikaatiot.serverCb);

  $scope.opsLimit = $state.is('root.etusivu') ? $scope.pohjaMinLimit : $scope.pohjaMaxLimit;
  $scope.sorter = ListaSorter.init($scope);

  $scope.showAll = function() {
      $scope.opsLimit = $scope.pohjaMaxLimit;
  };

  $scope.showLess = function() {
    $scope.opsLimit = $scope.pohjaMinLimit;
  };

})
.run(function($templateCache) {
    $templateCache.put('pohjaSisaltoNodeEditingTemplate', '' +
            '<div style="background: {{ taustanVari }}" class="tekstisisalto-solmu" ng-class="{ \'tekstisisalto-solmu-paataso\': (node.$$depth === 0) }">' +
            '    <span class="treehandle" icon-role="drag"></span>' +
            '    <span ng-bind="node.tekstiKappale.nimi || \'nimeton\' | kaanna"></span>' +
            '</div>'
            );
    $templateCache.put('pohjaSisaltoNodeTemplate', '' +
            '<div style="background: {{ taustanVari }}" class="tekstisisalto-solmu" ng-class="{ \'tekstisisalto-solmu-paataso\': (node.$$depth === 0) }">' +
            '    <span class="tekstisisalto-chevron action-link" ng-show="node.$$hasChildren" href="" ng-click="node.$$hidden = !node.$$hidden">' +
            '       <span ng-show="node.$$hidden" icon-role="chevron-right"></span>' +
            '       <span ng-hide="node.$$hidden" icon-role="chevron-down"></span>' +
            '    </span>' +
            '    <a href="" ui-sref="root.pohjat.yksi.tekstikappale({ tekstikappaleId: node.id })">' +
            '       <span ng-bind="node.tekstiKappale.nimi || \'nimeton\' | kaanna"></span>' +
            '    </a>' +
            '    <span class="pull-right">' +
            '        <span class="muokattu-aika">' +
            '             <span kaanna="\'muokattu-viimeksi\'"></span>:' +
            '             <span ng-bind="node.tekstiKappale.muokattu | aikaleima"></span>' +
            '        </span>' +
            '    </span>' +
            '</div>'
            );
})
.controller('PohjaSisaltoController', function($rootScope, $scope, $q, Algoritmit, Utils, $stateParams, OpetussuunnitelmanTekstit,
  Notifikaatiot, $state, TekstikappaleOps, OpetussuunnitelmaCRUD, pohjaOps, Editointikontrollit, Lukko, tekstit) {
  $scope.model = pohjaOps;
  $scope.model.tekstit = tekstit;

  var commonParams = {
    opsId: $stateParams.pohjaId,
  };

  $scope.sync = function() {
    OpetussuunnitelmaCRUD.syncPeruste({ id: $scope.model.id }, _.bind(Notifikaatiot.onnistui, {}, 'paivitys-onnistui'), Notifikaatiot.serverCb);
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
      Lukko.unlock(commonParams);
      $state.reload();
    }
  });

  $scope.muokkaaRakennetta = function() {
    Lukko.lock(commonParams, function() {
      Editointikontrollit.startEditing();
      $scope.$$isRakenneMuokkaus = true;
      $rootScope.$broadcast('genericTree:refresh');
    });
  };

  $scope.sortableConfig = {
    placeholder: 'placeholder'
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
        return $scope.$$isRakenneMuokkaus  ? 'pohjaSisaltoNodeEditingTemplate' : 'pohjaSisaltoNodeTemplate';
      },
      children: function(node) {
        return $q.when(node && node.lapset ? node.lapset : []);
      },
      useUiSortable: function() {
        return !$scope.$$isRakenneMuokkaus;
      },
      acceptDrop: _.constant(true),
      sortableClass: _.constant('is-draggable-into'),
      extension: function(node, scope) {
        scope.taustanVari = node.$$depth === 0 ? '#f2f2f9' : '#ffffff';

        scope.poistaTekstikappale = function(osio, node) {
          TekstikappaleOps.varmistusdialogi(node.tekstiKappale.nimi, function () {
            osio = osio || $scope.model.tekstit;
            TekstikappaleOps.delete($scope.model, osio, $stateParams.pohjaId, node, function() {
              _.remove(osio.lapset, node);
            });
          }, function () {
            unlockTeksti(node.id);
          });
        };
      }
    });
  });

  function unlockTeksti(id, cb) {
    return Lukko.unlockTekstikappale(_.extend({viiteId: id}, commonParams), cb);
  }

  $scope.lisaaTekstikappale = function() {
      OpetussuunnitelmanTekstit.save({
        opsId: $stateParams.pohjaId
      }, { }, function(res) {
        res.lapset = res.lapset || [];
        Notifikaatiot.onnistui('tallennettu-ok');
        $scope.model.tekstit.lapset.push(res);
      }, Notifikaatiot.serverCb);
  };

  // $scope.uiTreeOptions = {
  //   accept: function(source, destination) {
  //     return (source.$modelValue.$$ylataso && destination.$modelValue === $scope.model.tekstit.lapset) ||
  //       (!source.$modelValue.$$ylataso && destination.$modelValue !== $scope.model.tekstit.lapset);
  //   }
  // };
});
