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
.controller('VuosiluokkakokonaisuusController', function ($scope, Editointikontrollit,
  MurupolkuData, vlk, $stateParams, Notifikaatiot, VuosiluokatService, Utils, Kaanna, $rootScope) {

  $scope.siirtymat = ['siirtymaEdellisesta', 'siirtymaSeuraavaan'];
  $scope.vlk = vlk;
  $scope.temp = {};
  $scope.paikalliset = {};
  $scope.orderFn = function (tunniste) {
    return Kaanna.kaanna($scope.laajaalaiset[tunniste].nimi).toLowerCase();
  };

  function fetch() {
    VuosiluokatService.getVuosiluokkakokonaisuus($stateParams.id, $stateParams.vlkId, function (res) {
      $scope.vlk = res;
      initTexts();
      initPeruste();
    }, Notifikaatiot.serverCb);
  }

  function initTexts() {
    _.each($scope.siirtymat, function (key) {
      $scope.temp[key] = $scope.vlk[key] || {};
    });
  }
  initTexts();

  function initPeruste() {
    // TODO oikea perusteen data
    $scope.perusteVlk = {
      siirtymaEdellisesta: {
        otsikko: {fi: 'Siirtymä edellisestä vaiheesta'},
        teksti: {fi: 'Placeholder-teksti edellisestä'}
      },
      siirtymaSeuraavaan: {
        otsikko: {fi: 'Siirtymä seuraavaan vaiheeseen'},
        teksti: {fi: 'Placeholder-teksti seuraavaan'}
      },
      laajaalaisetOsaamiset: [
        {
          tunniste: '1234',
          nimi: {fi: 'Joku osaamiskokonaisuus (LX)'},
          kuvaus: {fi: 'Kuvausteksti'}
        },
        {
          tunniste: '5678',
          nimi: {fi: 'Ajattelu ja oppimaan oppiminen (L1)'},
          kuvaus: {fi: '<p>Ajattelun ja oppimisen taidot muodostavat perustan muun osaamisen kehittymiselle ja elinikäiselle oppimiselle. Ajatteluun ja oppimiseen vaikuttaa se, miten oppilas hahmottaa itsensä oppijana ja on vuorovaikutuksessa ympäristönsä kanssa. Olennaista on myös, miten hän oppii tekemään havaintoja ja hakemaan, arvioimaan, muokkaamaan, tuottamaan sekä jakamaan tietoa ja ideoita. Tutkiva ja luova työskentelyote, yhdessä tekeminen sekä mahdollisuus syventymiseen ja keskittymiseen edistävät ajattelun ja oppimaan oppimisen kehittymistä.</p>\n\n<p>Opettajien on tärkeä rohkaista oppilaita luottamaan itseensä ja näkemyksiinsä ja olemaan samalla avoimia uusille ratkaisuille. Oppilaita ohjataan pohtimaan asioita eri näkökulmista, hakemaan uutta tietoa ja siltä pohjalta tarkastelemaan ajattelutapojaan. Heidän kysymyksilleen annetaan tilaa, heitä innostetaan etsimään vastauksia ja kuuntelemaan toisten näkemyksiä ja rakentamaan uutta tietoa. Koulun muodostaman oppivan yhteisön jäseninä oppilaat saavat tukea ja kannustusta ideoilleen ja aloitteilleen, jolloin heidän toimijuutensa voi vahvistua.</p>\n\n<p>Oppilaita ohjataan käyttämään hankkimaansa tietoa itsenäisesti ja vuorovaikutuksessa toisten kanssa ongelmanratkaisuun, argumentointiin, päättelyyn ja johtopäätösten tekemiseen. Oppilailla tulee olla mahdollisuus analysoida käsillä olevaa asiaa kriittisesti eri näkökulmista. Innovatiivisten ratkaisujen löytäminen edellyttää, että oppilaat voivat käyttää kuvittelukykyään ja oppivat näkemään vaihtoehtoja ja yhdistelemään näkökulmia ennakkoluulottomasti. Leikit, pelillisyys, fyysinen aktiivisuus, kokeellisuus ja muut toiminnalliset työtavat sekä taiteen eri muodot edistävät oppimisen iloa ja vahvistavat edellytyksiä luovaan ajatteluun. Valmiudet systeemiseen ja eettiseen ajatteluun kehittyvät vähitellen, kun oppilaat oppivat näkemään asioiden välisiä vuorovaikutus suhteita ja keskinäisiä yhteyksiä sekä hahmottamaan kokonaisuuksia.</p>\n\n<p>Jokaista oppilasta autetaan tunnistamaan oma tapansa oppia ja kehittämään oppimisstrategioitaan. Oppimaan oppimisen taidot karttuvat, kun oppilaita ohjataan ikäkaudelleen sopivalla tavalla asettamaan tavoitteita, suunnittelemaan työtään, arvioimaan edistymistään sekä hyödyntämään teknologisia ja muita apuvälineitä opiskelussaan. Oppilaita tuetaan rakentamaan perusopetuksen aikana hyvä tiedollinen perusta ja kestävä motivaatio jatkoopinnoille ja elinikäiselle oppimiselle.</p>'}
        }
      ]
    };

    $scope.tunnisteet = _.map($scope.perusteVlk.laajaalaisetOsaamiset, 'tunniste');
    var decorated = _.map($scope.perusteVlk.laajaalaisetOsaamiset, function (item) {
      item.teksti = item.kuvaus;
      item.otsikko = item.nimi;
      return item;
    });
    $scope.laajaalaiset = _.indexBy(decorated, 'tunniste');
    $scope.paikalliset = _.mapValues($scope.laajaalaiset, function () {
      // TODO olemassaolevat
      return {};
    });
  }
  initPeruste();

  MurupolkuData.set('vlkNimi', vlk.nimi);

  $scope.hasSiirtymat = function () {
    return $scope.perusteVlk.siirtymaEdellisesta || $scope.perusteVlk.siirtymaSeuraavaan;
  };

  function commitLaajaalaiset() {
    if (!$scope.vlk.laajaalaisetosaamiset) {
      $scope.vlk.laajaalaisetosaamiset = [];
    }
    _.each($scope.paikalliset, function (value, tunniste) {
      if (value.teksti) {
        var model = _.find($scope.vlk.laajaalaisetosaamiset, function (item) {
          return '' + item.tunniste === '' + tunniste;
        });
        if (model) {
          model.teksti = value.teksti;
        } else {
          model = {
            tunniste: tunniste,
            teksti: value.teksti
          };
          // TODO enable
          //$scope.vlk.laajaalaisetosaamiset.push(model);
        }
      }
    });
  }

  $scope.callbacks = {
    edit: function () {
      fetch();
    },
    save: function () {
      $rootScope.$broadcast('notifyCKEditor');
      commitLaajaalaiset();
      _.each($scope.temp, function (value, key) {
        if (value !== null) {
          $scope.vlk[key] = value;
        }
      });
      $scope.vlk.$save({opsId: $stateParams.id}, function (res) {
        $scope.vlk = res;
        initTexts();
      }, Notifikaatiot.serverCb);
    },
    cancel: function () {
      fetch();
    },
    notify: function (mode) {
      $scope.callbacks.notifier(mode);
    },
    notifier: angular.noop
  };
  Editointikontrollit.registerCallback($scope.callbacks);

});
