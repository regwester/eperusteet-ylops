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
  // TODO poista dummydata
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

.service('VuosiluokatService', function ($q, DummyData, $state, OppiaineCRUD, Utils, OpsService) {
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

  function vlkSorter (item) {
    // TODO järjestys vuosiluokkaenumin mukaan nimen sijasta?
    return Utils.sort(item.vuosiluokkakokonaisuus);
  }

  function onCompletion(model, cb) {
    if (model.$promise) {
      model.$promise.then(function (res) {
        cb(res);
      });
    } else {
      cb(model);
    }
  }

  function fetch(ops) {
    var promise = $q.defer();
    function processVuosiluokkakokonaisuudet(model) {
      opsId = model.id;
      var vlkt = model.vuosiluokkakokonaisuudet;
      vuosiluokat = _.sortBy(vlkt, vlkSorter);
      promise.resolve(vuosiluokat);
    }
    var opsModel = (ops || OpsService.get());
    onCompletion(opsModel, processVuosiluokkakokonaisuudet);
    return promise.promise;
  }

  function getVuosiluokat(ops) {
    return fetch(ops);
  }

  function setVuosiluokat(vlkt) {
    vuosiluokat = vlkt;
  }

  function getVuosiluokkakokonaisuus(ops, vlkId) {
    // TODO käytä varsinaista vlk APIa jos/kun sellainen tulee
    var found = null;
    var promise = $q.defer();
    onCompletion(ops, function (model) {
      found = _.find(model.vuosiluokkakokonaisuudet, function (item) {
        return '' + item.vuosiluokkakokonaisuus.id === '' + vlkId;
      });
      promise.resolve(found);
    });
    return promise.promise;
  }

  function getTavoitteet(/*oppiaineenVlkId*/) {
    return promisify(DummyData.getTavoitteet());
  }

  function getOppiaine(oppiaineId) {
    return OppiaineCRUD.get({opsId: opsId || OpsService.getId()}, {id: oppiaineId});
  }

  function generateOppiaineItem(oppiaine, vlk, depth) {
    return {
      depth: depth || 1,
      label: oppiaine.nimi,
      id: oppiaine.id,
      url: $state.href('root.opetussuunnitelmat.yksi.oppiaine', {vlkId: vlk.id, oppiaineId: oppiaine.id}),
    };
  }

  function getTunnisteet(vlkt) {
    return _.map(vlkt, function (item) {
      return item._vuosiluokkakokonaisuus;
    });
  }

  function mapForMenu(ops) {
    var arr = [];
    if (ops) {
      _.each(vuosiluokat, function (vlk) {
        var obj = vlk.vuosiluokkakokonaisuus;
        var item = {
          label: obj.nimi,
          id: obj.id,
          url: $state.href('root.opetussuunnitelmat.yksi.vuosiluokkakokonaisuus', {vlkId: obj.id}),
        };
        arr.push(item);

        var sorted = _.sortBy(ops.oppiaineet, function (item) {
          return Utils.sort(item.oppiaine);
        });
        _.each(sorted, function (oppiaine) {
          var tunnisteet = getTunnisteet(oppiaine.oppiaine.vuosiluokkakokonaisuudet);
          var parentPushed = false;
          if (_.indexOf(tunnisteet, obj._tunniste) > -1) {
            arr.push(generateOppiaineItem(oppiaine.oppiaine, obj));
            parentPushed = true;
          }
          if (oppiaine.oppiaine.koosteinen) {
            var oppimaarat = _.sortBy(oppiaine.oppiaine.oppimaarat, Utils.sort);
            _.each(oppimaarat, function (oppimaara) {
              tunnisteet = getTunnisteet(oppimaara.vuosiluokkakokonaisuudet);
              if (_.indexOf(tunnisteet, obj._tunniste) > -1) {
                if (!parentPushed) {
                  arr.push(generateOppiaineItem(oppiaine.oppiaine, obj));
                  parentPushed = true;
                }
                arr.push(generateOppiaineItem(oppimaara, obj, 2));
              }
            });
          }
        });
      });
    }
    return arr;
  }

  this.setOps = setOps;
  this.fetch = fetch;
  this.getVuosiluokat = getVuosiluokat;
  this.setVuosiluokat = setVuosiluokat;
  this.getVuosiluokkakokonaisuus = getVuosiluokkakokonaisuus;
  this.getTavoitteet = getTavoitteet;
  this.getOppiaine = getOppiaine;
  this.mapForMenu = mapForMenu;
});
