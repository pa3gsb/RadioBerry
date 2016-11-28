angular
    .module('app')
    .component('volume', {
        templateUrl: 'app/widgets/volume_view.html',
        bindings: {
            radio: '='
        },
        controller: function ($scope, RadiostateService) {
            var self = this;

            $scope.value = self.radio.volume;

            self.setVolume = function (value) {
                console.log('Volume ' + value + ' selected');

                self.radio.volume = value;
                RadiostateService.updateRadioState(self.radio);
            };

        }
    });