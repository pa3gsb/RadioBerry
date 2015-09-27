/*!
 * websocket JavaScript Library v1.0
 *
 * Author: Johan Maas PA3GSB
 */

var websockets = {};
var ws = null;

var dataArray = new Float32Array(1280);

websockets.connect = function() {
	var target = 'ws://' + window.location.host + '/';

	if (ws != null) {
		return;
	}

	try {
		if ('WebSocket' in window) {
			ws = new WebSocket(target);
		} else if ('MozWebSocket' in window) {
			ws = new MozWebSocket(target);
		} else {
			alert('WebSocket is not supported by this browser.');
			return;
		}
	}
	catch(e) {console.log('websockets.connect error');};

	try {
		ws.onopen = function() {
			console.log('Info: WebSocket connection opened.');
		};
	}
	catch(e) {console.log('websockets.open error');};

	try {
		ws.onmessage = function(event) {
			var arr = JSON.parse(event.data);

			if (arr.audio) {
				for (var i = 0; i < arr.audio.length; i++) {
					var value = parseFloat(arr.audio[i].d);
					ringBuffer.enq(value);
				}
			}
			if (arr.spectrum) {
				for (var i = 0; i < arr.spectrum.length; i++) {
					dataArray[i] = parseFloat(arr.spectrum[i].d);
				}
				sdrspectrum.visualize();
			}
		};
	}
	catch(e) {console.log('websockets.onmessage error');};

	try{
		ws.onclose = function() {
			console.log('Info: WebSocket connection closed.');
		};
	}
	catch(e) {console.log('websockets.onclose error');};
};

websockets.disconnect = function() {
	if (ws != null) {
		ws.close();
		ws = null;
	}
};

websockets.send = function(tekst) {
	if (ws != null) {
		ws.send(tekst);
	} else {
		console.log('WebSocket connection not established, please connect.');
	}
};
