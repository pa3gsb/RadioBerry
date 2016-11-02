angular
  .module('app')
  .config(routesConfig);

/** @ngInject */
function routesConfig($stateProvider, $urlRouterProvider, $locationProvider) {
  $locationProvider.html5Mode(true).hashPrefix('!');
  $urlRouterProvider.otherwise('/');

  $stateProvider
    .state('home', {
      url: '/',
      component: 'home'
    })
	.state('ssb', {
      url: '/ssb',
      component: 'ssb'
    })
	.state('am', {
      url: '/am',
      component: 'am'
    })
	.state('cw', {
      url: '/cw',
      component: 'cw'
    })
    .state('demo', {
      url: '/demo',
      templateUrl: 'app/demo/demo.html',
      controller: 'RadioControl'
    });
}
