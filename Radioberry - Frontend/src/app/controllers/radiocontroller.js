angular
    .module('app')
    .controller('RadioControl', ['$scope', 'RadiostateService',   function($scope, RadiostateService){

       $scope.radioState = RadiostateService.getRadioState();

       $scope.hello = "hello-from-controller-radio-state";

    }]);