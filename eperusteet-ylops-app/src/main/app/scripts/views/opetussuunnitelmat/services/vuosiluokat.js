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
.service('DummyData', function () {
  this.getVuosiluokat = function () {
    return [
      {id: 'dummy1', nimi: {fi: 'Vuosiluokat 1-2'}},
      {id: 'dummy2', nimi: {fi: 'Vuosiluokat 3-6'}},
      {id: 'dummy3', nimi: {fi: 'Vuosiluokat 7-9'}},
    ];
  };

  this.getOppiaineet = function () {
    return [
      {id: 'dummy4', nimi: {fi: 'Matematiikka'}},
      {id: 'dummy5', nimi: {fi: 'Äidinkieli ja kirjallisuus'}},
      {id: 'dummy6', nimi: {fi: 'Musiikki'}},
      {id: 'dummy7', nimi: {fi: 'Liikunta'}},
    ];
  };

  this.getTavoitteet = function () {
    return [
      {tavoite: {fi: 'pitää yllä oppilaan innostusta ja kiinnostusta matematiikkaa kohtaan sekä tukee positiivista minäkuvaa ja itseluottamusta'}},
      {tavoite: {fi: 'ohjaa oppilasta havaitsemaan yhteyksiä oppimiensa asioiden välillä'}},
      {tavoite: {fi: 'kehittää oppilaan taitoa esittää kysymyksiä ja tehdä perusteltuja päätelmiä havaintojensa pohjalta'}},
      {tavoite: {fi: 'kannustaa oppilasta esittämään ratkaisujaan ja päätelmiään muille konkreettisin välinein, piirroksin, suullisesti ja kirjallisesti käyttäen myös tieto- ja viestintäteknologiaa'}},
      {tavoite: {fi: 'ohjaa oppilasta ymmärtämään ja käyttämään matemaattisia käsitteitä ja merkintöjä'}},
      {tavoite: {fi: 'varmistaa, että oppilas ymmärtää kymmenjärjestelmän periaatteen sekä desimaaliluvut sen osana'}},
      {tavoite: {fi: 'laajentaa lukukäsitteen ymmärtämistä positiivisiin rationaalilukuihin ja negatiivisiin kokonaislukuihin'}},
      {tavoite: {fi: 'ohjaa oppilasta arvioimaan mittauskohteen suuruutta ja valitsemaan mittaamiseen sopivan välineen sekä käyttämään sopivaa mittayksikköä ja pohtimaan mittaustuloksen järkevyyttä'}},
      {tavoite: {fi: 'laajentaa lukukäsitteen ymmärtämistä positiivisiin rationaalilukuihin ja negatiivisiin kokonaislukuihin'}},
    ];
  };

  this.getOppiaine = function () {
    return {
      nimi: {fi: 'Matematiikka'},
      tehtava: {otsikko: {fi: 'Oppiaineen tehtävä'}, teksti: {fi: 'Matematiikan opetuksen tehtävänä on kehittää oppilaan loogista, täsmällistä ja luovaa matemaattista ajattelua. Opetus luo pohjan matemaattisten käsitteiden ja rakenteiden ymmärtämiselle sekä kehittää oppilaan kykyä käsitellä tietoa ja ratkaista ongelmia.'}},
      vuosiluokat: [
        {
          vuosiluokka: '1'
        },
        {
          vuosiluokka: '2'
        },
      ],
      vuosiluokkakokonaisuudet: [
        {
          nimi: {fi: 'Vuosiluokat 1-2'},

          tehtava: {otsikko: {fi: 'Matematiikan tehtävä vuosiluokilla 1-2'}, teksti: {fi: 'Vuosiluokkien 1−2 matematiikan opetuksessa oppilaalle tarjotaan monipuolisia kokemuksia matemaattisten käsitteiden ja rakenteiden muodostumisen perustaksi.'}},
          tyotavat: {otsikko: {fi: 'Oppiaineen oppimisympäristöihin ja työtapoihin liittyvät tavoitteet vuosiluokalla 1-2'}, teksti: {fi: 'Opetuksen lähtökohtana käytetään oppilaalle tuttuja ja kiinnostavia aiheita ja ongelmia. Tavoitteena on luoda oppimisympäristö, jossa matematiikkaa ja matematiikan käsitteitä opiskellaan toiminnallisesti ja välineiden avulla.'}},
          ohjaus: {otsikko: {fi: 'Ohjaus ja tuki oppiaineessa vuosiluokilla 1-2'}, teksti: {fi: 'Oppilaiden osaamisessa on huomattavia eroja jo ennen koulun alkamista. Hierarkkisena oppiaineena matematiikan perusasioiden hallinta on välttämätön edellytys uusien sisältöjen oppimiselle.'}},
          arviointi: {otsikko: {fi: 'Oppilaan oppimisen arviointi oppiaineessa vuosiluokilla 1-2'}, teksti: {fi: 'Vuosiluokilla 1-2 matematiikan oppimisen arvioinnissa on kiinnitettävä huomiota kannustavan palautteen antamiseen. Keskeisten sisältöjen oppimisen rinnalla on myös arvioitava laaja-alaisten taitojen kehittymistä monipuolisesti.'}},
        },
      ]
    };
  };
})

.service('VuosiluokatService', function ($q, DummyData, $state) {
  var opsId = null;
  var vuosiluokat = null;

  function promisify(data) {
    var deferred = $q.defer();
    deferred.resolve(data);
    return deferred.promise;
  }

  function setOps(ops) {
    opsId = ops.id;
  }

  function fetch() {
    var vlk = DummyData.getVuosiluokat();
    vlk[0].oppiaineet = DummyData.getOppiaineet();
    var promise = promisify(vlk);
    promise.then(function (res) {
      vuosiluokat = res;
    });
    return promise;
  }

  function getVuosiluokat() {
    if (vuosiluokat === null) {
      return fetch();
    }
    return promisify(vuosiluokat);
  }

  function getTavoitteet(/*oppiaineenVlkId*/) {
    return promisify(DummyData.getTavoitteet());
  }

  function getOppiaine() {
    return promisify(DummyData.getOppiaine());
  }

  function mapForMenu(data) {
    var arr = [];
    _.each(data, function (item) {
      var vlk = {
        label: item.nimi,
        id: item.id,
        url: $state.href('root.opetussuunnitelmat.yksi.vuosiluokkakokonaisuus', {vlkId: item.id}),
      };
      arr.push(vlk);
      _.each(item.oppiaineet, function (oppiaine) {
        arr.push({
          depth: 1,
          label: oppiaine.nimi,
          id: oppiaine.id,
          url: $state.href('root.opetussuunnitelmat.yksi.oppiaine', {vlkId: item.id, oppiaineId: oppiaine.id}),
        });
      });
    });
    return arr;
  }

  this.setOps = setOps;
  this.fetch = fetch;
  this.getVuosiluokat = getVuosiluokat;
  this.getTavoitteet = getTavoitteet;
  this.getOppiaine = getOppiaine;
  this.mapForMenu = mapForMenu;
});
