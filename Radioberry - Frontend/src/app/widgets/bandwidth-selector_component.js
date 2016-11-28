angular
    .module('app')
    .component('bandwithselector', {
        templateUrl: 'app/widgets/bandwidth-selector_view.html',
        bindings: {
            radio: '='
        },
        controller: function ($scope, RadiostateService) {
            var self = this;

            $scope.value = Math.abs(self.radio.high);

            self.setBandWidth = function (value) {
                console.log('bandwith ' + value + ' selected');
                //LSB = 0
                if (self.radio.mode == 0) {
                    self.radio.low = -150;
                    self.radio.high = value * -1;
                }   
                if (self.radio.mode == 1) {
                    self.radio.low = 150;
                    self.radio.high = value;
                }
                if (self.radio.mode == 2 || self.radio.mode == 6) {
                    self.radio.low = value * -1
                    self.radio.high = value;
                }

                RadiostateService.updateRadioState(self.radio);
            };
        }
    });