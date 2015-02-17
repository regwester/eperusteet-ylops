describe('Service: TextUtils', function () {
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
    inject(function (_TextUtils_) {
      mySvc = _TextUtils_;
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
    beforeEach(function () {
      // Inject with expected values
      _setup();
    });

    it('should exist', function () {
      expect(!!mySvc).toBe(true);
    });

    // Add specs
    it('should get letter-number code from string if available', function () {
      var getCode = mySvc.getCode;

      expect(getCode('Tavoite (T2)')).toBe('T2');
      expect(getCode('L10 Joku laaja-alainen')).toBe('L10');
      expect(getCode('Normaali teksti 1234')).toBe(null);
      expect(getCode('pienikin a3 mätsää')).toBe('a3');
      expect(getCode('u56 a1 eeee8888')).toBe('u56');
      expect(getCode('aaaabbbbccccdddd88887777')).toBe(null);
      expect(getCode('....L7....')).toBe('L7');
    });
  });

});
