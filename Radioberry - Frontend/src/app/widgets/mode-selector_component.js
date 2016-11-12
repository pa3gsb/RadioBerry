angular
    .module('app')
    .component('modeselector', {
        templateUrl: 'app/widgets/mode-selector_view.html',
        bindings: {
            radio: '=',
            mode: '='
        },
        controller: function ($window, $scope, $log, RadiostateService) {
            var self = this;
            
            self.selectedMode = self.mode[self.radio.mode];
        
            self.setMode = function () {
                 self.radio.mode = self.selectedMode.code;
                 RadiostateService.updateRadioState(self.radio);
             };

        }
    });