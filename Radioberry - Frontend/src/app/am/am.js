angular
  .module('app')
  .component('am', {
    templateUrl: 'app/am/am.html',
    controller: function () {
      this.hello = 'Hello AM World!';
    }
  });
