angular
    .module('app')
    .component('frequencycontrol', {
        templateUrl: 'app/widgets/frequency-control_view.html',
        bindings: {
            radio: '='
        },
        controller: function ($window, $scope, $log, RadiostateService) {
            var self = this;
            
            console.log(self.radio);
        
            self.setFreq = function () {
                RadiostateService.updateRadioState(self.radio);
            };

            var freq = $window.document.getElementById("freq");
            if (freq.addEventListener) {
                // Internet Explorer, Opera, Google Chrome and Safari
                freq.addEventListener("mousewheel", MouseScroll, false);
                // Firefox
                freq.addEventListener("DOMMouseScroll", MouseScroll, false);
            }

            function MouseScroll(event) {
                var rolled = 0;
                if ('wheelDelta' in event) {
                    rolled = event.wheelDelta;
                }
                else {  // Firefox
                    // The measurement units of the detail and wheelDelta properties are different.
                    rolled = -40 * event.detail;
                    rolled = event.detail;
                }
                if (rolled > 0) 
                    { self.radio.frequency = self.radio.frequency + parseInt(self.radio.frequencystep); } 
                else 
                    { self.radio.frequency = self.radio.frequency - parseInt(self.radio.frequencystep); }

                RadiostateService.updateRadioState(self.radio);
                $scope.$apply();
            }


            //disable context menu using secondary mouse event.
            freqstep.addEventListener('contextmenu', function (event) {
                event.preventDefault();
                return false;
            });

            // handling the freq step based on the left and right mouse clicks.
            freqstep.addEventListener("mousedown", function (event) {
                switch (event.which) {
                    case 1:
                        step = parseInt(self.radio.frequencystep);
                        if (step < 10000000) { step = step * 10; }
                        self.radio.frequencystep = String(step);
                        break;
                    case 2:
                        break;
                    case 3:
                        step = parseInt(self.radio.frequencystep);
                        if (step > 1) { step = step / 10; }
                        self.radio.frequencystep = String(step);
                        break;
                    default:
                        console.log('Mouse what are you doing...');
                }
                RadiostateService.updateRadioState(self.radio);
                $scope.$apply();
            });
        }
    });