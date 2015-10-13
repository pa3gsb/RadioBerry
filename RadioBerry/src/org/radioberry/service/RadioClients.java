package org.radioberry.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.radioberry.web.RadioWebSocketHandler;

public class RadioClients {

	private Map<RadioClient, RadioWebSocketHandler> radios = new HashMap<RadioClient, RadioWebSocketHandler>();

	public List<RadioClient> getRadios() {

		List<RadioClient> list = new ArrayList<RadioClient>(radios.keySet());
		return list;
	}

	public static RadioClients getInstance() {
		if (instance == null) {
			instance = new RadioClients();
		}
		return instance;
	}

	private RadioClients() {
	}

	private static RadioClients instance;

	public synchronized void addRadioClient(
			RadioWebSocketHandler radioWebSocketHandler) {
		RadioClient radioClient = new RadioClient(radioWebSocketHandler);
		radios.put(radioClient, radioWebSocketHandler);

		System.out.println("#tuned radios = " + radios.size());
	}

	public synchronized void removeRadioClient(RadioWebSocketHandler radioWebSocketHandler) {
		for (RadioClient r : radios.keySet()){
			if (radios.get(r).equals(radioWebSocketHandler)){
				radios.remove(r);
			}
		}
		radios.remove(radioWebSocketHandler);
		
		
		System.out.println("#tuned radios = " + radios.size());
	}

	public synchronized void fillAudioStream(List<Double> audioBuffer) {
		for (Double sample : audioBuffer) {
			for (RadioClient rc : RadioClients.getInstance().getRadios()) {
				rc.addSample(sample);
			}
		}
	}
	
	public synchronized void stream() {
		for (RadioClient radioClient : RadioClients.getInstance().getRadios()) {
			radioClient.stream();
		}
	}

	public synchronized void spectrum(float[] samples) {
		for (RadioClient radioClient : RadioClients.getInstance().getRadios()) {
			radioClient.spectrum(samples);
		}
		
	}

}
