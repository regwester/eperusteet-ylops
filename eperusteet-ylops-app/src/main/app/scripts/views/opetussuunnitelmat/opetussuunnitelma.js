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
  .controller('OpetussuunnitelmaController', function ($scope, Editointikontrollit, $stateParams,
    $timeout, $state, OpetussuunnitelmaCRUD, opsModel, opsService, Notifikaatiot, Varmistusdialogi,
    OpetussuunnitelmanTekstit, KoodistoHaku, PeruskouluHaku, Kaanna) {

    $scope.editMode = false;
    $scope.rakenneEdit = false;
    if ($stateParams.id === 'uusi') {
      $timeout(function () {
        $scope.edit();
      }, 200);
    }

    $scope.model = opsModel;

    function fetch() {
      opsService.refetch();
    }

    $scope.edit = function () {
      Editointikontrollit.startEditing();
    };

    $scope.delete = function () {
      Varmistusdialogi.dialogi({
        otsikko: 'varmista-poisto',
        primaryBtn: 'poista',
        successCb: function () {
          $scope.model.$delete({}, function () {
            Notifikaatiot.onnistui('poisto-onnistui');
            $timeout(function () {
              $state.go('root.etusivu');
            });
          }, Notifikaatiot.serverCb);
        }
      })();
    };

    $scope.addTekstikappale = function () {
      $state.go('root.opetussuunnitelmat.yksi.tekstikappale', {tekstikappaleId: 'uusi'});
    };

    function mapSisalto(root) {
      return {
        id: root.id,
        lapset: _.map(root.lapset, mapSisalto)
      };
    }

    $scope.saveRakenne = function () {
      var postdata = mapSisalto($scope.model.tekstit);
      OpetussuunnitelmanTekstit.save({
        opsId: $scope.model.id,
        viiteId: $scope.model.tekstit.id
      }, postdata, function () {
        Notifikaatiot.onnistui('tallennettu-ok');
        $scope.rakenneEdit = false;
        fetch();
      }, Notifikaatiot.serverCb);
    };

    $scope.editRakenne = function () {
      $scope.rakenneEdit = true;
    };

    $scope.cancelRakenne = function () {
      $scope.rakenneEdit = false;
      fetch();
    };

    $scope.nimiOrderFn = function (item) {
      return Kaanna.kaanna(item.nimi).toLowerCase();
    };

    function mapKunnat(lista) {
      return _(lista).map(function (kunta) {
        return {
          koodiUri: kunta.koodiUri,
          koodiArvo: kunta.koodiArvo,
          nimi: _(kunta.metadata).indexBy(function (item) {
            return item.kieli.toLowerCase();
          }).mapValues('nimi').value()
        };
      }).sortBy($scope.nimiOrderFn).value();
    }

    $scope.haeKunnat = function () {
      KoodistoHaku.get({ koodistoUri: 'kunta' }, function(kunnat) {
        // Kunnat aakkosjärjestykseen
        $scope.kuntalista = mapKunnat(kunnat);
      }, Notifikaatiot.serverCb);
    };

    $scope.$watch('model.kunnat', function () {
      $scope.haeKoulut();
    });

    $scope.haeKoulut = function () {
      var kunnat = $scope.model.kunnat;
      if (kunnat.length === 0) {
        $scope.model.koulut = [];
      } else if (kunnat.length === 1) {
        var kunta = kunnat[0];
        PeruskouluHaku.get({ kuntaUri: kunta.koodiUri }, function(res) {
          $scope.koululista = _.sortBy(res.organisaatiot, $scope.nimiOrderFn);
        }, Notifikaatiot.serverCb);
      } else {
        $scope.model.koulut = [];
        $scope.koululista = [];
      }
    };

    $scope.haeKunnat();
    $scope.haeKoulut();

    var successCb = function (res) {
      $scope.model = res;
      Notifikaatiot.onnistui('tallennettu-ok');
      if ($stateParams.id === 'uusi') {
        $state.go('root.opetussuunnitelmat.yksi.sisalto', {id: res.id}, {reload: true});
      }
    };

    var callbacks = {
      edit: function () {
        fetch();
      },
      save: function () {
        if ($stateParams.id === 'uusi') {
          OpetussuunnitelmaCRUD.save({}, $scope.model, successCb, Notifikaatiot.serverCb);
        } else {
          $scope.model.$save({}, successCb, Notifikaatiot.serverCb);
        }
      },
      cancel: function () {
        if ($stateParams.id === 'uusi') {
          $timeout(function () {
            $state.go('root.etusivu');
          });
        } else {
          fetch();
        }
      },
      notify: function (mode) {
        $scope.editMode = mode;
      }
    };
    Editointikontrollit.registerCallback(callbacks);

  });
