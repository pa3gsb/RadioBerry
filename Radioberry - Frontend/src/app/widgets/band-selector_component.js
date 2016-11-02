angular
    .module('app')
    .component('bandselector', {
        templateUrl: 'app/widgets/band-selector_view.html',
        bindings: {
            radio: '='
        },
        controller: function (RadiostateService) {
            var self = this;

            self.selectBand = function (band) {
                console.log('band ' + band + ' selected');
                if (band == 80)
                    self.radio.frequency = 3630000;
                RadiostateService.updateRadioState(self.radio);
            };

        }
    });