angular
    .module('app')
    .component('attselector', {
        templateUrl: 'app/widgets/att-selector_view.html',
        bindings: {
            radio: '='
        },
        controller: function ($scope, RadiostateService) {
            var self = this;

            $scope.value = self.radio.att;

            self.setAtt = function (value) {
                console.log('att ' + value + ' selected');

                self.radio.att = value;
                RadiostateService.updateRadioState(self.radio);
            };

        }
    });