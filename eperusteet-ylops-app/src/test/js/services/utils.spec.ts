describe('Service: Utils', function () {
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
    inject(function (_Utils_) {
      mySvc = _Utils_;
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
    it('should compare localized text objects', function () {
      var cmp = mySvc.compareLocalizedText, t1, t2;

      t1 = {};
      t2 = {};
      expect(cmp(t1, t2)).toBe(true);

      t1 = null;
      t2 = null;
      expect(cmp(t1, t2)).toBe(true);

      t1 = {fi: 'tekstiä', sv:''};
      t2 = {fi: 'tekstiä', sv:''};
      expect(cmp(t1, t2)).toBe(true);

      t1 = {fi: 'tekstiä', sv:'text'};
      t2 = {fi: 'tekstiä', sv:''};
      expect(cmp(t1, t2)).toBe(false);

      t1 = {fi: 'tekstiä', sv:'', '$extra': 'stuff'};
      t2 = {fi: 'tekstiä', sv:'', '_id': 1234};
      expect(cmp(t1, t2)).toBe(true);

    });
  });

});
