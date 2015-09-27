/*!
 * sdrspectrum JavaScript Library v1.0
 *
 * Author: Johan Maas PA3GSB
 */

var sdrspectrum = {};

var sdrspectrumcenter;
var sdrspectrumfrequency;

$.fn.pixels = function(property) {
    return parseInt(this.css(property).slice(0,-2));
};


var bandWidthChanged = false;
var bandWidth = 0;


sdrspectrum.isBandwithChanged = function() {
	var result = false;
	if (changed == true) {
		result = true;
		changed = false
	}
	return result;
};

sdrspectrum.getBandwidth = function(mode) {

	var bwidth=[];
	
	var width = $('#bandwith').pixels('width');
	var spectrumWidth = document.getElementById("spectrum").clientWidth;
	var freqBin = 48000.0 / (spectrumWidth * 1.0);

	//LSB
	if (mode == 0) {
		//low
		bwidth[bwidth.length] = width * freqBin * -1;
		//high
		bwidth[bwidth.length] = -300;
	}
	//USB
	if (mode == 1) {
		//low
		bwidth[bwidth.length] = 300;
		//high
		bwidth[bwidth.length] = width * freqBin;
	}
	
	return bwidth;

};

sdrspectrum.getFrequencyShift = function(mode) {

	var spectrumWidth = document.getElementById("spectrum").clientWidth;
	var freqBin = 48000.0 / ((spectrumWidth - 0) * 1.0);
	
	var left = $('#bandwith').pixels('left');
	left = left - 20;
	var width = $('#bandwith').pixels('width');
	var panFrequency = 0;
	//LSB
	if (mode == 0) {
		panFrequency = (left + width) * freqBin;
	}
	//USB
	if (mode == 1) {
		panFrequency = (left + width) * freqBin;
	}
	
	var shiftFrequency = (panFrequency + (radioControl.frequency - 24000)) - radioControl.frequency;
	if (shiftFrequency > -100 && shiftFrequency < 100) shiftFrequency = 0.0;
	
	return shiftFrequency * 1.0;
};

sdrspectrum.visualize = function() {
	var canvas = document.querySelector('.visualizer');
	var canvasCtx = canvas.getContext("2d");

	WIDTH = document.getElementById("spectrum").clientWidth;
	canvas.width = WIDTH;
	HEIGHT = canvas.height;
	var bufferLength = 1280;

	canvasCtx.clearRect(0, 0, WIDTH, HEIGHT);

	function draw() {
		canvasCtx.fillStyle = "#808080"; //"#AAAAAA";  //"#FEEEC"; // "#F8F8F8";
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
		
		var left = $('#bandwith').pixels('left');
		var width = $('#bandwith').pixels('width');
		
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
		for (var i = 0; i < bufferLength; i++) {
			var y = dataArray[i] * -1 * 2;
			if (i === 0) {
				canvasCtx.moveTo(x, y);
			} else {
				if (x >= left && x < (left+width)){
					if (start) {canvasCtx.stroke(); canvasCtx.beginPath(); start = false; end = true;}
					canvasCtx.strokeStyle = '#ff0000';
					//canvasCtx.strokeStyle = 'rgb(0, 0, 0)';
					canvasCtx.lineWidth = 1;
					//canvasCtx.moveTo(x, y);
					//canvasCtx.lineTo(x, HEIGHT);
					//canvasCtx.moveTo(x, y);
				} 
				else {
					if (end) {canvasCtx.stroke(); canvasCtx.beginPath(); end = false};
					canvasCtx.strokeStyle = 'rgb(0, 0, 0)';
					canvasCtx.lineWidth = 1;
				}
				canvasCtx.lineTo(x, y);
			}
			
			//sdrspectrumfrequency
			
			x += sliceWidth;
			
			if (i % (bufferLength/8) == 0){
				canvasCtx.fillStyle = "#000000";
				var strFreq = numeral(((radioControl.frequency - 24000) +  (6000 * j)) / 1000).format('0,0');
				strFreq = strFreq.replace(/,/g , ".");
				canvasCtx.fillText(  strFreq , x, 15);
				freqMarkers[freqMarkers.length] = x;
				console.log(strFreq + " pos: " + x);
				
				if ( (6000 * j) == 24000 ) {
					sdrspectrumcenter = x;
				}
				
				j++;
			}
		}
		canvasCtx.fillStyle = "#000000";
		var strFreq = numeral( (radioControl.frequency + 24000) / 1000).format('0,0');
		strFreq = strFreq.replace(/,/g , ".");
		canvasCtx.fillText(strFreq, WIDTH - 30, 15);
		freqMarkers[freqMarkers.length] = WIDTH - 30;
	
		canvasCtx.stroke();
		
		canvasCtx.beginPath();
		canvasCtx.strokeStyle = "#959492";
		canvasCtx.lineWidth = 2.0;
		for (i = 0; i < freqMarkers.length; i ++) {
			
			canvasCtx.moveTo(freqMarkers[i], 0);
			canvasCtx.lineTo(freqMarkers[i], HEIGHT);
		}
		canvasCtx.stroke();
		
		//bandwith
		//canvasCtx.beginPath();		
		//canvasCtx.fillStyle = "#959492";
		//var left = $('#bandwith').pixels('left');
		//var width = $('#bandwith').pixels('width');
		//canvasCtx.fillRect(WIDTH / 2, 0, 40, 300);
		//canvasCtx.stroke();
		
		
	};
	draw();
};