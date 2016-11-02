angular
    .module('app')
    .component('agcgainselector', {
        templateUrl: 'app/widgets/agc-gain-selector_view.html',
        bindings: {
            radio: '='
        },
        controller: function ($scope, RadiostateService) {
            var self = this;

            $scope.value = 120 - self.radio.agcgain;

            self.selectAGCGain = function (agcgain) {
                invert =  120 - agcgain;
                console.log('agc gain  ' + invert + ' selected');
                self.radio.agcgain = invert;
                RadiostateService.updateRadioState(self.radio);
            };

        }
    });