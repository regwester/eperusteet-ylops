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
  .service('YlopsResources', function (SERVICE_LOC) {
    this.OPS = SERVICE_LOC + '/opetussuunnitelmat/:opsId';
    this.OPPIAINE = this.OPS + '/oppiaineet/:oppiaineId';
    this.VLK = this.OPS + '/vuosiluokkakokonaisuudet/:vlkId';
    this.OPVLK = this.OPPIAINE + '/vuosiluokkakokonaisuudet/:vlkId';
    this.VUOSILUOKKA = this.OPPIAINE + '/vuosiluokkakokonaisuudet/:vlkId/vuosiluokat/:vlId';
  })

  .factory('OpetussuunnitelmaCRUD', function ($resource, YlopsResources) {
    return $resource(YlopsResources.OPS, {
      opsId: '@id'
    }, {
      laajaalaiset: {method: 'GET', url: YlopsResources.OPS + '/laajaalaisetosaamiset', isArray: true},
      setTila: {method: 'POST', url: YlopsResources.OPS + '/tila/:tila'},
      lisaaKielitarjonta: {method: 'POST', url: YlopsResources.OPS + '/kielitarjonta', isArray: true},
    });
  })
  .factory('OpetussuunnitelmaOikeudet', function ($resource, YlopsResources) {
    return $resource(YlopsResources.OPS + '/oikeudet', {
      opsId: '@id'
    }, {
      query: {method: 'GET', isArray: false}
    });
  })
  .factory('OpetussuunnitelmanTekstit', function ($resource, YlopsResources) {
    return $resource(YlopsResources.OPS + '/tekstit/:viiteId', {
      viiteId: '@id'
    }, {
      setChild: {method: 'POST', url: YlopsResources.OPS + '/tekstit/:parentId/lapsi/:childId'}
    });
  })

  .factory('OpetussuunnitelmanTekstitLukko', function ($resource, YlopsResources) {
    return $resource(YlopsResources.OPS + '/tekstit/:viiteId/lukko', {
      viiteId: '@id'
    });
  })

  .factory('OpetussuunnitelmanTekstitRakenneLukko', function ($resource, YlopsResources) {
    return $resource(YlopsResources.OPS + '/tekstit/lukko');
  })

  .factory('OppiaineCRUD', function ($resource, YlopsResources) {
    return $resource(YlopsResources.OPPIAINE, {
      oppiaineId: '@id'
    }, {
      peruste: {method: 'GET', url: YlopsResources.OPPIAINE + '/peruste'},
      saveValinnainen: {method: 'POST', url: YlopsResources.OPPIAINE + '/valinnainen'},
      addKielitarjonta: {method: 'POST', url: YlopsResources.OPPIAINE + '/kielitarjonta'},
      getParent: {method: 'GET', url: YlopsResources.OPPIAINE + '/parent'}
    });
  })

  .factory('VuosiluokkakokonaisuusCRUD', function ($resource, YlopsResources) {
    return $resource(YlopsResources.VLK, {
      vlkId: '@id'
    }, {
      peruste: {method: 'GET', url: YlopsResources.VLK + '/peruste'}
    });
  })

  .factory('OppiaineenVlk', function ($resource, YlopsResources) {
    return $resource(YlopsResources.OPVLK, {
      vlkId: '@id'
    }, {
      getTavoitteet: {method: 'GET', isArray: false, url: YlopsResources.OPVLK + '/tavoitteet'},
      vuosiluokkaista: {method: 'POST', url: YlopsResources.OPVLK + '/tavoitteet'},
      peruste: {method: 'GET', url: YlopsResources.OPVLK + '/peruste'}
    });
  })

  .factory('VuosiluokkaCRUD', function ($resource, YlopsResources) {
    return $resource(YlopsResources.VUOSILUOKKA, {
      vlId: '@id'
    });
  });
