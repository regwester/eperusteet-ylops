describe('Service: Vuosiluokat', function () {
  var mySvc;

  // Use to provide any mocks needed
  function _provide(callback) {
    // Execute callback with $provide
    module(function ($provide) {
      callback($provide);
    });
  }

  // Use to inject the code under test
  function _inject() {
    inject(function (_VuosiluokatService_) {
      mySvc = _VuosiluokatService_;
    });
  }

  // Call this before each test, except where you are testing for errors
  function _setup() {
    // Mock any expected data
    _provide(function (/*provide*/) {
      //provide.value('myVal', {});
    });

    // Inject the code under test
    _inject();
  }

  beforeEach(function () {
    // Load the service's module
    module('ylopsApp');
  });

  describe('the service api', function () {
    var ops;
    var vuosiluokat;

    beforeEach(function () {
      // Inject with expected values
      _setup();

      ops = {
        oppiaineet: [
          {oppiaine: {id: 1, nimi: {fi: 'MA'}, koosteinen: false, vuosiluokkakokonaisuudet: [
            {'_vuosiluokkakokonaisuus': '12'}
          ]}},
          {oppiaine: {id: 2, nimi: {fi: 'LI'}, koosteinen: false, vuosiluokkakokonaisuudet: [
            {'_vuosiluokkakokonaisuus': '36'}
          ]}},
          {oppiaine: {id: 3, nimi: {fi: 'FY'}, koosteinen: false, vuosiluokkakokonaisuudet: [
            {'_vuosiluokkakokonaisuus': '12'}
          ]}},
          {oppiaine: {id: 4, nimi: {fi: 'A1'}, koosteinen: true, vuosiluokkakokonaisuudet: [
            /*{'_vuosiluokkakokonaisuus': '12'}*/
          ],
            oppimaarat: [
            {id: 7, nimi: {fi: 'JA'}, koosteinen: false, vuosiluokkakokonaisuudet: [
              {'_vuosiluokkakokonaisuus': '12'}
            ]},
            {id: 5, nimi: {fi: 'EN'}, koosteinen: false, vuosiluokkakokonaisuudet: [
              {'_vuosiluokkakokonaisuus': '12'}
            ]},
            {id: 6, nimi: {fi: 'RU'}, koosteinen: false, vuosiluokkakokonaisuudet: [
              {'_vuosiluokkakokonaisuus': '36'}
            ]},
          ]}},
        ]
      };

      vuosiluokat = [
        {vuosiluokkakokonaisuus: {nimi: {fi: '1-2'}, '_tunniste': '12'}},
        {vuosiluokkakokonaisuus: {nimi: {fi: '3-6'}, '_tunniste': '36'}},
      ];
    });

    it('should exist', function () {
      expect(!!mySvc).toBe(true);
    });

    // Add specs
    it('should map vuosiluokat and oppiaineet', function () {
      mySvc.setVuosiluokkakokonaisuudet(vuosiluokat);
      var app = mySvc.mapForMenu(ops);

      var labels = _.map(app, function (item) { return item.label.fi; });
      expect(labels).toEqual(['1-2', 'A1', 'EN', 'JA', 'FY', 'MA', '3-6', 'A1', 'RU', 'LI']);
      expect(app[1].depth).toEqual(1);
      expect(app[2].depth).toEqual(2);
    });
  });

});
