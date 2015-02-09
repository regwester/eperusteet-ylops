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
.controller('VuosiluokkaistaminenController', function ($scope, tavoitteet, $filter, VariHyrra, ColorCalculator,
  $state) {
  var TAVOITTEET = 'tavoite-list';
  var VUOSILUOKKA = 'vuosiluokka-list';
  $scope.singleVuosiluokat = [3, 4, 5, 6];
  $scope.tavoitteet = tavoitteet;
  $scope.allDragged = false;
  $scope.collapsedMode = false;
  $scope.showKohdealueet = true;

  var kohdealueet = [
    {id: 1, nimi: 'Merkitys, arvot ja asenteet'},
    {id: 2, nimi: 'Työskentelyn taidot'},
    {id: 3, nimi: 'Käsitteelliset ja tiedonalakohtaiset tavoitteet'},
    {id: 4, nimi: 'Tutkimisen ja toimimisen taidot'},
  ];

  VariHyrra.reset();
  _.each(kohdealueet, function (alue) {
    var vari = VariHyrra.next();
    alue.styles = {
      'background-color': '#' + vari,
      color: ColorCalculator.readableTextColorForBg(vari)
    };
  });

  function goBack() {
    $state.go('root.opetussuunnitelmat.yksi.oppiaine.oppiaine');
  }

  $scope.cancel = function () {
    goBack();
  };

  function saveCb() {
    goBack();
  }

  $scope.save = function () {
    // TODO save
    saveCb();
  };

  function processTavoitteet() {
    _.each($scope.tavoitteet, function (tavoite, index) {
      // TODO remove dummy data
      tavoite.koodi = 'T' + (index + 8);
      //var kohdealueId = _.first(tavoite.kohdealueet);
      var kohdealueId = _.sample(_.map(kohdealueet, 'id'));
      tavoite.kohdealue = _.find(kohdealueet, {id: kohdealueId});
    });
  }

  processTavoitteet();

  $scope.containers = {
    tavoitteet: {
      type: 'tavoitteet',
      id: 'container-list-tavoitteet',
      label: 'tavoitteet-ja-sisallot',
      items: []
    }
  };

  function resetTavoitteet() {
    $scope.containers.tavoitteet.items = _.clone($scope.tavoitteet);
    var usedTavoitteet = {};
    var unused = [];
    _.each($scope.containers, function (container, key) {
      if (key !== 'tavoitteet') {
        _.each(container.items, function (tavoite) {
          usedTavoitteet[tavoite.koodi] = true;
        });
      }
    });
    _.each($scope.containers.tavoitteet.items, function (tavoite) {
      tavoite.$kaytossa = usedTavoitteet[tavoite.koodi];
      if (!tavoite.$kaytossa) {
        unused.push(tavoite);
      }
    });
    $scope.allDragged = unused.length === 0;
  }

  resetTavoitteet();

  _.each($scope.singleVuosiluokat, function (item) {
    $scope.containers[item] = {
      items: [],
      vuosiluokka: item,
      id: 'container-list-vuosiluokka-' + item
    };
  });

  function sourceIs(event, className) {
    return angular.element(event.target).hasClass(className);
  }

  function destinationIs(ui, className) {
    return ui.item.sortable.droptarget && ui.item.sortable.droptarget.hasClass(className);
  }

  function adjustIndex(arr, sorters, originalIndex) {
    var sortedList = $filter('orderBy')(arr, sorters);
    var newIndex = _.findIndex(arr, function (item) {
      return item === sortedList[originalIndex];
    });
    return newIndex;
  }

  $scope.tavoiteSorter = function (item) {
    return parseInt(item.koodi.substr(1), 10);
  };

  function modelFromTarget(target) {
    var id = target.getAttribute('id');
    return _.find($scope.containers, function (container) {
      return container.id === id;
    });
  }

  $scope.sortableOptions = {
    placeholder: 'container-item placeholder',
    connectWith: '.container-items',
    cursor: 'move',
    cursorAt: {top : 2, left: 2},
    tolerance: 'pointer',
    start: function (event, ui) {
      // Must use same sorter(s) as in ng-repeat template
      var model = modelFromTarget(event.target);
      if (model) {
        var sortedIndex = adjustIndex(model.items, [$scope.tavoiteSorter], ui.item.sortable.index);
        ui.item.sortable.index = sortedIndex;
      }
    },
    stop: function (event, ui) {
      var fromTavoitteet = sourceIs(event, TAVOITTEET);
      var toVuosiluokka = destinationIs(ui, VUOSILUOKKA);
      var fromVuosiluokka = sourceIs(event, VUOSILUOKKA);
      var toTavoitteet = destinationIs(ui, TAVOITTEET);

      // Tavoitteet -> vuosiluokka: kopio vuosiluokkaan
      if (fromTavoitteet && toVuosiluokka) {
        resetTavoitteet();
      }

      // Vuosiluokka -> tavoitteet: poisto
      if (fromVuosiluokka && toTavoitteet) {
        resetTavoitteet();
      }
    },
    update: function (event, ui) {
      var toVuosiluokka = destinationIs(ui, VUOSILUOKKA);

      // Ei duplikaatteja vuosiluokan sisällä
      if (toVuosiluokka) {
        var dropModel = modelFromTarget(ui.item.sortable.droptarget[0]);
        var koodi = _.last(ui.item[0].getAttribute('id').split('-'));
        var existing = _.find(dropModel.items, function (item) {
          return koodi === item.koodi;
        });
        if (existing && !ui.sender) {
          ui.item.sortable.cancel();
        }
      }
    }
  };
});
