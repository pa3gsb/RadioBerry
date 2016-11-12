angular
    .module('app')
    .component('spectrum', {
        templateUrl: 'app/widgets/spectrum_view.html',
        bindings: {
            radio: '='
        },
        controller: function ($window, $scope, $interval, NotifyingService) {
            var self = this;

            this.canvasElement = $window.document.getElementById('visualizer');

            NotifyingService.subscribe($scope, function somethingChanged() {
                // Handle notification
                console.log("Iam notified....")

            });

            var createData = function () {
                
                var dataArray = new Float32Array(1280);
                var bandWidth = 0;

                WIDTH = 900;
                HEIGHT = 300;
                self.canvasElement.width = WIDTH;
	            self.canvasElement.height = HEIGHT;

                var bufferLength = 1280;
                var canvasCtx = self.canvasElement.getContext("2d");
                canvasCtx.clearRect(0, 0, WIDTH, HEIGHT);

                function draw() {
                    canvasCtx.fillStyle = "#808080"; 
                    canvasCtx.fillRect(0, 0, WIDTH, HEIGHT);

                    // horizontal raster
                    canvasCtx.strokeStyle = "#959492";
                    canvasCtx.lineWidth = 0.4;
                    canvasCtx.beginPath();
                    p = 0;
                    for (i = 0; i < (HEIGHT + 10); i += 20) {
                        canvasCtx.moveTo(10, i);
                        canvasCtx.lineTo(WIDTH, i);
                        canvasCtx.fillStyle = "#000000";
                        canvasCtx.font = "bold 13px sans-serif";
                        canvasCtx.fillText("-" + p, 0, i);
                        p += 10;
                    }
                    ;
                    canvasCtx.stroke();

                    // vertical raster
                    canvasCtx.beginPath();
                    for (i = 30; i < (WIDTH + 10); i += 20) {
                        canvasCtx.moveTo(i, 0);
                        canvasCtx.lineTo(i, HEIGHT);
                    }
                    ;
                    canvasCtx.stroke();

                    //actual spectrum plot
                    canvasCtx.lineWidth = 1;
                    canvasCtx.strokeStyle = 'rgb(0, 0, 0)';
                    canvasCtx.beginPath();
                    var sliceWidth = (WIDTH - 30) * 1.0 / bufferLength;
                    var x = 30;

                    var left = 600; //$('#bandwith').pixels('left');
                    var width = 600; //$('#bandwith').pixels('width');

                    if (bandWidth != width) {
                        bandWidth = width;
                        changed = true;
                    }

                    var freqMarkers = [];
                    var j = 0;
                    var start = true;
                    var end = false;
                    canvasCtx.font = "bold 14px sans-serif";
                    canvasCtx.fillStyle = "#959492";
                    var k = 1;
                    for (var i = 0; i < bufferLength; i++) {
                        k = k * -1;
                        var y = 240.0 + ( (Math.random() * 4) * k) ; //dataArray[i] * -1 * 2;
                        if (i === 0) {
                            canvasCtx.moveTo(x, y);
                        } else {
                            // if (x >= left && x < (left + width)) {
                            //     if (start) { canvasCtx.stroke(); canvasCtx.beginPath(); start = false; end = true; }
                            //     canvasCtx.strokeStyle = '#ff0000';
                            //     canvasCtx.lineWidth = 1;
                            // }
                            // else {
                                if (end) { canvasCtx.stroke(); canvasCtx.beginPath(); end = false };
                                canvasCtx.strokeStyle = 'rgb(0, 0, 0)';
                                canvasCtx.lineWidth = 1;
                            // }
                            canvasCtx.lineTo(x, y);
                        }

                        //sdrspectrumfrequency
                        x += sliceWidth;

                        if (i % (bufferLength / 8) == 0) {
                            canvasCtx.fillStyle = "#000000";
                            var strFreq = "3.630";//numeral(((radioControl.frequency - 24000) + (6000 * j)) / 1000).format('0,0');
                            strFreq = strFreq.replace(/,/g, ".");
                            canvasCtx.fillText(strFreq, x, 15);
                            freqMarkers[freqMarkers.length] = x;
                            //console.log(strFreq + " pos: " + x);

                            if ((6000 * j) == 24000) {
                                sdrspectrumcenter = x;
                            }

                            j++;
                        }
                    }
                    canvasCtx.fillStyle = "#000000";
                    var strFreq = "3.630.000";//numeral((radioControl.frequency + 24000) / 1000).format('0,0');
                    strFreq = strFreq.replace(/,/g, ".");
                    canvasCtx.fillText(strFreq, WIDTH - 30, 15);
                    freqMarkers[freqMarkers.length] = WIDTH - 30;

                    canvasCtx.stroke();

                    canvasCtx.beginPath();
                    canvasCtx.strokeStyle = "#959492";
                    canvasCtx.lineWidth = 2.0;
                    for (i = 0; i < freqMarkers.length; i++) {

                        canvasCtx.moveTo(freqMarkers[i], 0);
                        canvasCtx.lineTo(freqMarkers[i], HEIGHT);
                    }
                    canvasCtx.stroke();
                };
                draw();
            };
            //using interval
            $interval(createData, 50);
        }
    });
	