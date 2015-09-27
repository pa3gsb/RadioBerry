package org.radioberry.service;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

import org.radioberry.utility.Log;


public class LocalAudio {

	private AudioFormat audioformat;
	private SourceDataLine audioline;

	public void initializeLocalAudioOutput() {

		try {
			audioformat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
					48000F, 16, 2, 4, 48000F, true);
			audioline = AudioSystem.getSourceDataLine(audioformat);
			audioline.open(audioformat, 48000);
			audioline.start();
		} catch (Exception e) {
			Log.info("Metis", "initializeLocalAudioOutput: " + e.toString());
		}

	}

	public void stopLocalAudioOutput() {
		if (audioline != null) {
			audioline.flush();
			audioline.close();
			audioline = null;
		}
	}
	
	
	public static LocalAudio getInstance() {
		if(instance==null) {
			instance=new LocalAudio();
		}
		return instance;
	}

	private LocalAudio() {
		initializeLocalAudioOutput();
	}

    private static LocalAudio instance;

	public void writeAudio(byte[] audiooutput) {
		if (audioline != null) {
			int sent = audioline.write(audiooutput,
					0, audiooutput.length);
			if (sent != audiooutput.length) {
				Log.info("Metis",
						"write audio returned "
								+ sent
								+ " when sending "
								+ audiooutput.length);
			}
		}		
	}

}
