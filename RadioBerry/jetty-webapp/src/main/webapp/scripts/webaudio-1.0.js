/*!
 * webaudio JavaScript Library v1.0
 *
 * Author: Johan Maas PA3GSB
 */
	var webaudio = {};
	var ringBuffer = new RingBuffer(8000);	
	 
	var source = null;
	var gainNode = null;
	var running = false;		
		

	webaudio.play = function() {
	
		var audioCtx;
		
		var audioBuffer;
		var channelBuffer;
		
		try {
				var AudioContext = window.AudioContext || window.webkitAudioContext;
				
				audioCtx = new AudioContext();
				gainNode = audioCtx.createGain();
			}
			catch(e) {
				alert('Web Audio API is not supported in this browser');
			}
			
			var channels = 1;
			var channel_0 = 0;
			var frameCount = 8000; 
			var sampleRate = 8000;
		
			audioBuffer = audioCtx.createBuffer(channels, frameCount, sampleRate); 
		
			channelBuffer = audioBuffer.getChannelData(channel_0); 
	
			running = true;
			
			function play() {
				var length = ringBuffer.size();
							
				for(i = 0; i < length; i++) {
					channelBuffer[i] = ringBuffer.deq();
				}	
			
				playBuffer(audioBuffer);
			}
  
			 function playBuffer(buffer) {
				source = audioCtx.createBufferSource();
				source.buffer = buffer;
				source.connect(gainNode);
				source.onended = onEnded;
				gainNode.connect(audioCtx.destination);
				source.start();
			}
  
			function onEnded() {
				if (running) play();
			}
			
			play();
	};
	
	webaudio.volume = function(level) {
		if (gainNode != null) {
			gainNode.gain.value = level;
		}
		
	};
	
	webaudio.stop = function() {
	
			if (running) {
				source.stop();
				source = null;
				running = false;
			}
	};
	
	

		
 