angular
  .module('app')
  .component('ssb', {
    templateUrl: 'app/ssb/ssb.html',
    controller: function () {
      this.hello = 'Hello SSB World!';
    }
  });
