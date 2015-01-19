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
    OpetussuunnitelmanTekstit, KoodistoHaku, PeruskouluHaku, Kieli) {

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

    $scope.haeKunnanNimi = function (kunta) {
      var kieliMap = { fi: 'FI', se: 'SE' };
      var kieli = kieliMap[Kieli.getSisaltokieli()] || 'FI';

      var metadata =
        _.find(kunta.metadata, function (md) { return md.kieli === kieli; }) || kunta.metadata[0];
      return metadata.nimi;
    };

    $scope.haeKoulunNimi = function (koulu) {
      return koulu.nimi[Kieli.getSisaltokieli()] || koulu.nimi.fi;
    };

    function aakkosta(lista, nimiFn) {
      var snd = function (arr) { return arr[1]; };
      var nimet = _.map(lista, nimiFn);

      return _.chain(lista)
        .zip(nimet)
        .sortBy(snd)
        .map(_.first)
        .value();
    }

    $scope.haeKunnat = function () {
      KoodistoHaku.get({ koodistoUri: 'kunta' }, function(kunnat) {
        // Kunnat aakkosjärjestykseen
        $scope.kuntalista = aakkosta(kunnat, $scope.haeKunnanNimi);
      }, Notifikaatiot.serverCb);
    };

    $scope.haeKoulut = function () {
      var kunnat = $scope.model.kunnat;
      if (kunnat.length === 1) {
        var kunta = kunnat[0];
        PeruskouluHaku.get({ kuntaUri: kunta.koodiUri }, function(res) {
          var koulut = res.organisaatiot;
          if (koulut.length === 0) {
            console.log('Kunnasta ' + $scope.haeKunnanNimi(kunta) + ' ei löytynyt yhtäkään koulua');
          }

          // Koulut aakkosjärjestykseen
          $scope.koululista = aakkosta(koulut, $scope.haeKoulunNimi);
        }, Notifikaatiot.serverCb);
      } else {
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
