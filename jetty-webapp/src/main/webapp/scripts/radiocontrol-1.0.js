/*!
 * radiocontrol JavaScript Library v1.0
 *
 * Author: Johan Maas PA3GSB
 *
 */
 
var radioControl = { }; //"frequency":parseInt(1008000), "mode":parseInt(6), "agc":parseInt(0), "low":parseInt(-4000), "high":parseInt(4000), "agc_gain":parseFloat(85)};

radioControl.init = function() {
	radioControl.frequency = parseInt(1008000);
	radioControl.showFrequency();
	radioControl.sendSDRControlData();
	radioControl.setWebAudioVolume();
};


radioControl.setRXFrequency = function(freq) {
	radioControl.frequency = parseInt(freq);
	radioControl.showFrequency();
	radioControl.sendSDRControlData();
};

radioControl.setRXFrequencyStepUp = function() {
	radioControl.frequency = radioControl.frequency + parseInt($('#freqStep').val());
	radioControl.sendSDRControlData();
	radioControl.showFrequency();
};

radioControl.setRXFrequencyStepDown = function() {
	radioControl.frequency = radioControl.frequency - parseInt($('#freqStep').val());
	radioControl.sendSDRControlData();
	radioControl.showFrequency();
};

radioControl.setTXFrequency = function() {
 	radioControl.frequency = parseInt(document.getElementById("freq").value);
 	radioControl.showFrequency();
	radioControl.sendSDRControlData();
};
 
radioControl.setMode = function(mode) {
	radioControl.mode = parseInt(mode);
	radioControl.sendSDRControlData();
};

radioControl.setAGC = function(agc) {
	radioControl.agc = parseInt(agc);
	radioControl.sendSDRControlData();
}; 	

radioControl.setAGCGain = function(agcgain) {
	radioControl.agc_gain = parseInt(agcgain);
	radioControl.sendSDRControlData();
}; 	

radioControl.setWebAudioVolume = function() {
	volume = parseInt(document.getElementById("volume").value);
	webaudio.volume(volume);
};		
		
radioControl.showFrequency = function() {
	var strFreq = numeral(radioControl.frequency).format('0,0');
	strFreq = strFreq.replace(/,/g , ".");
		
	if (parseInt(radioControl.frequency) >= 10000000){
		display.setValue(strFreq);
	} else if (parseInt(radioControl.frequency) >= 1000000){
			display.setValue(" " +strFreq);
	} else {
			display.setValue("   " +strFreq);
	}
			
	displayRIT.setValue('   747.000');
};		

radioControl.sendSDRControlData = function() {
	try{
		var xhr = new XMLHttpRequest();
		xhr.open("POST","/websdr/control.do", true);
		xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
		var jsondata = JSON.stringify(radioControl);
		console.log(jsondata);
		xhr.send(jsondata);
	}
	catch(e){console.log('radioControl.sendSDRControlData error');}
};

