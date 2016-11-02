angular
  .module('app')
  .component('cw', {
    templateUrl: 'app/cw/cw.html',
    controller: function () {
      this.hello = 'Hello CW World!';
    }
  });
