package org.radioberry.service;


import java.util.NoSuchElementException;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.radioberry.web.RadioWebSocketHandler;

public class RadioClient {

	private static final int SAMPLES = 400;
	
	private RadioWebSocketHandler radioWebSocketHandler;
	
	private CircularFifoQueue<Double> buffer = new CircularFifoQueue<Double>(800);
	
	
	public RadioClient(RadioWebSocketHandler radioWebSocketHandler){
		this.radioWebSocketHandler = radioWebSocketHandler;
	}
	
	public void spectrum(float[] samples){
		radioWebSocketHandler.sendMessage(getSpectrumJSONStream(samples));
	}
	
	
	public void addSample(double sample){
		buffer.add(sample);
	}
	
	public void stream() {
		if (buffer.size() >= SAMPLES) {
			radioWebSocketHandler.sendMessage(getAudioJSONStream());
		}
	}
	
	
	//{"spectrum":[{"d":234},{"d":432}]}
	private String getSpectrumJSONStream(float[] samples) {

		String data = null;

		JSONWriter jw = new JSONStringer();
		try {
			jw.object();
			jw.key("spectrum");
			jw.array();
			for (int i = 0; i < samples.length; i++) {
				jw.object();
				jw.key("d");
				jw.value(samples[i]);
				jw.endObject();
			}
			jw.endArray();
			jw.endObject();

			data = jw.toString();
			
			//System.out.println(data);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return data;
	}	
	
	
	//{"audio":[{"d":234},{"d":432}]}
	private String getAudioJSONStream() {

		String data = null;

		JSONWriter jw = new JSONStringer();
		try {
			jw.object();
			jw.key("audio");
			jw.array();
			for (int i = 0; i < SAMPLES; i++) {
				jw.object();
				jw.key("d");
				double val = 0.0;

				try {
					val = buffer.remove();
				} catch (NoSuchElementException ex) {
					System.out.println("No element");
				} catch (Exception ex) {System.out.println("exception ");}

				jw.value(val);
				jw.endObject();
			}
			
			jw.endArray();
			jw.endObject();

			data = jw.toString();

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return data;
	}
	
}
