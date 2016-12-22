angular
    .module('app')
    .component('agcselector', {
        templateUrl: 'app/widgets/agc-selector_view.html',
        bindings: {
            radio: '=',
            agcmode: '='
        },
        controller: function ($window, $scope, $log, RadiostateService) {
            var self = this;
            
            self.selectedMode = self.agcmode[self.radio.agcmode];
        
            self.setAGCMode = function () {
                 self.radio.agcmode = self.selectedMode.code;
                 RadiostateService.updateRadioState(self.radio);
             };

        }
    });