/*!
 * dspcontrol JavaScript Library v1.0
 *
 * Author: Johan Maas PA3GSB
 *
 */
 
var dspControl = {};

dspControl.init = function() {
	//"mode":parseInt(6), "agc":parseInt(0), "low":parseInt(-4000), "high":parseInt(4000), "agc_gain":parseFloat(85)
	dspControl.mode = parseInt(0);	//LSB
	dspControl.agc = parseInt(2);
	dspControl.agc_gain = parseFloat(85);
	dspControl.low = parseInt(-2700);
	dspControl.high = parseInt(-300);
	dspControl.shift = 0.0;
	dspControl.sendDSPControlData();
};

dspControl.setBandWidth = function(low, high) {
	dspControl.low = parseInt(low);
	dspControl.high = parseInt(high);
	dspControl.sendDSPControlData();
};

dspControl.setMode = function(mode) {
	console.log('mode change' + parseInt(mode));
	dspControl.mode = parseInt(mode);
	dspControl.sendDSPControlData();
};

dspControl.setAGC = function(agc) {
	dspControl.agc = parseInt(agc);
	dspControl.sendDSPControlData();
}; 	

dspControl.setAGCGain = function(agcgain) {
	dspControl.agc_gain = parseInt(agcgain);
	dspControl.sendDSPControlData();
}; 	

dspControl.setShift = function(shift) {
	dspControl.shift = parseFloat(shift);
	dspControl.sendDSPControlData();
}; 	

dspControl.sendDSPControlData = function() {
		try {
			var xhr = new XMLHttpRequest();
			xhr.open("POST","/websdr/dspcontrol.do", true);
			xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
			var jsondata = JSON.stringify(dspControl);
			console.log(jsondata);
			xhr.send(jsondata);
		}
		catch(e){console.log('dspControl.sendDSPControlData error');}
};

