angular
    .module('app')
    .controller('RadioControl', ['$scope', 'RadiostateService',   function($scope, RadiostateService){

       $scope.radioState = RadiostateService.getRadioState();

       $scope.modes = [
            {"mode": "LSB", "code": 0},
            {"mode": "USB", "code": 1},
            {"mode": "DSB", "code": 2},
            {"mode": "CWL", "code": 3},
            {"mode": "CWU", "code": 4},
            {"mode": "FM", "code": 5},
            {"mode": "AM", "code": 6},
            {"mode": "DIGU", "code": 7},
            {"mode": "SPEC", "code": 8},
            {"mode": "DIGL", "code": 9},
            {"mode": "SAM", "code": 10},
            {"mode": "DRM", "code": 11}
       ];

       $scope.ssbmode = [
            {"mode": "LSB", "code": 0},
            {"mode": "USB", "code": 1}
       ];

       $scope.agcmode = [
           {"mode": "OFF", "code": 0},
           {"mode": "LONG", "code": 1},
           {"mode": "SLOW", "code": 2},
           {"mode": "MED", "code": 3},
           {"mode": "FAST", "code": 4}
           //{"mode": "CUSTOM", "code": 5},
       ];

       $scope.hello = "hello-from-controller-radio-state";

    }]);