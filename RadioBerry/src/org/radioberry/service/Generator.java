package org.radioberry.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import org.g0orx.openhpsdr.wdsp.WDSP;
import org.radioberry.utility.Channel;
import org.radioberry.utility.Display;

public class Generator implements Runnable {

	private Configuration configuration;
	
	private final static int SAMPLES = 1024;
	private final static int BANDWIDTH = 48000;
	private final static double TIMESTEP = (1.0 / BANDWIDTH);
	private double timebase = 0.0;

	double amplitude;
	
	private int vfo = 14250000;
	private int frequency = 14260000; 

	

	private float[] outlsamples;
	private float[] outrsamples;
	
	private WDSP wdsp;
	private int[] error = new int[1];
	
	private volatile Thread thread;
	
	// Local audio output
	private byte[] audiooutput = new byte[1024 * 4]; // 2 channels of shorts
	private int audiooutputindex = 0;

	
	public Generator() {
		configuration = Configuration.getInstance();
		
		outlsamples = new float[configuration.buffersize];
		outrsamples = new float[configuration.buffersize];
		
		this.wdsp = WDSP.getInstance();
	}
	
	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();
		}
	}

	public void stop() {
		thread = null;
	}

	@Override
	public void run() {

		while (thread == Thread.currentThread()) {
			generate();
		}
	}
	
	public void setVFOFrequency(int vfo) {
		this.vfo = vfo;
		int f1 = (int) frequency - vfo;

		
		System.out.println("vfo = " + vfo  +"  frequency tone = " + frequency +  "  f1 =" + f1 );
	}
	
	private void generate() {
		float[] inlsamples = new float[SAMPLES];
		float[] inrsamples = new float[SAMPLES];

		// required to get the 6th sample from the subsequent buffers
		int index = 0;
		
		int amplitudeDB = -73;
		amplitude = 1 / Math.pow(Math.sqrt(10), -(double)amplitudeDB / 10);
		System.out.println("Output generator -73dBm (S9)  = " + amplitude );
		long start = new Date().getTime();
		double actual = 0.0;
		double required = 0.0;
		
		while (thread == Thread.currentThread()) {
			long now = new Date().getTime();
			required =  ((now - start) / 1000.0) * 48000;
			if (actual - required < 0.0) {
				generate(inlsamples, inrsamples);
				// DSP demodulation
				wdsp.fexchange2(Channel.RX, inlsamples, inrsamples,
						outlsamples, outrsamples, error);

				// Put only the right sample in the buffer (maybe
				// later the other channel as well)
				List<Double> audioBuffer = new ArrayList<Double>();
				// place each 6th sample in the buffer (starting
				// with the first)
				for (int j = 0; j < configuration.buffersize; j++) {
					if (index % 6 == 0) {
						audioBuffer.add((double) outlsamples[j] / 32767.0F);
					}
					index++;
				}
				index = index % 6;
				// Fill the stream with the samples
				RadioClients.getInstance().fillAudioStream(
						audioBuffer);
				wdsp.Spectrum(Display.RX, 0, 0, inrsamples,
						inlsamples);
				actual = actual + 1024;
				
//				for (int j = 0; j < outlsamples.length; j++) {
//					
//					short lsample;
//                    short rsample;
//                    lsample = (short) (outlsamples[j] * 32767.0F * configuration.afgain);
//                    rsample = (short) (outrsamples[j] * 32767.0F * configuration.afgain);
//					
//					audiooutput[audiooutputindex++] = (byte) ((lsample >> 8) & 0xFF);
//					audiooutput[audiooutputindex++] = (byte) (lsample & 0xFF);
//					audiooutput[audiooutputindex++] = (byte) ((rsample >> 8) & 0xFF);
//					audiooutput[audiooutputindex++] = (byte) (rsample & 0xFF);
//					if (audiooutputindex == audiooutput.length) {
//						LocalAudio.getInstance().writeAudio(audiooutput);
//						audiooutputindex = 0;
//					}
//				}
			}
		}
	}

	private void generate(float[] inlsamples, float[] inrsamples) {
		int f1 = (int) frequency - vfo;
		
		//System.out.println("vfo = " + vfo  +"  frequency tone = " + frequency +  "  f1 =" + f1 );
		int idx = 0;
		
		int amplitudeDB = -100;
		float noiseAmplitude = (float) (1 / Math.pow(Math.sqrt(10), -(double)amplitudeDB / 10));
		
		while (idx < SAMPLES) {
			double angle1 = f1 * 2 * Math.PI * timebase;
			inlsamples[idx] =  noiseAmplitude * (2 * (float) Math.random() - 1);
			inrsamples[idx] =  noiseAmplitude * (2 * (float) Math.random() - 1);
			inlsamples[idx] += (float) (((float) Math.sin(angle1) * amplitude) ); 
			inrsamples[idx] += (float) (((float) Math.cos(angle1) * amplitude) ); 
			idx++;
			timebase += TIMESTEP;
		}
	}
}
