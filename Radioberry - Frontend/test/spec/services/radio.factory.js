'use strict';

describe('Service: radio.factory', function () {

  // load the service's module
  beforeEach(module('fountainInjectApp'));

  // instantiate service
  var radio.factory;
  beforeEach(inject(function (_radio.factory_) {
    radio.factory = _radio.factory_;
  }));

  it('should do something', function () {
    expect(!!radio.factory).toBe(true);
  });

});
