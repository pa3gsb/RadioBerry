package org.radioberry.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.g0orx.openhpsdr.wdsp.WDSP;
import org.radioberry.utility.Channel;
import org.radioberry.utility.Display;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;

public class RXIQData implements Runnable{

	private final GpioController gpio;
	private final GpioPinDigitalInput iPinSPIValid;
	
	// SPI device
	public static SpiDevice spi = null;
	
	// required to get the 6th sample from the subsequent buffers
			int index = 0;
	
	byte packet[] = new byte[6];
	
	byte save[] = new byte[6];
	
	int freq;
	
	private int inoffset = 0;
	private float[] inlsamples;
	private float[] inrsamples;
	
	private int isample;
	private int qsample;
	
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

	
	public RXIQData() {
		// create gpio controller
		gpio = GpioFactory.getInstance();
		iPinSPIValid = gpio.provisionDigitalInputPin(RaspiPin.GPIO_23, "SPIVALID");

		
		configuration = Configuration.getInstance();
		
		setRXFrequency(4607000);
		
		inlsamples = new float[configuration.buffersize];
		inrsamples = new float[configuration.buffersize];
		
		// create SPI object instance for SPI for communication
		try {
			spi = SpiFactory.getInstance(SpiChannel.CS0, 16000000, SpiMode.MODE_3);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		outlsamples = new float[configuration.buffersize];
		outrsamples = new float[configuration.buffersize];
		
		this.wdsp = WDSP.getInstance();
		
		for (int i = 0; i < 6; i++){
			save[i] = 0;
		}
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
			try {
				//processReceivedData(spi.write(packet));
				
				setRXFrequency(this.freq);
				
				byte[] rxbuffer = spi.write(packet);
				while (iPinSPIValid.isState(PinState.LOW)){
					setRXFrequency(this.freq);
					rxbuffer = spi.write(packet);
				}
//				int count=0;
//				for (int i = 0; i < 6; i++){
//					if (rxbuffer[i] != save[i]){
//						count++;
//					}
//					save[i] = rxbuffer[i];
//				}
//				if (count > 3)
					processReceivedData(rxbuffer);
//				else
//					System.out.print("+");
				
//				processReceivedData(rxbuffer);
				//System.out.println("freq out = " + freq);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void processReceivedData(byte[] rxbuffer){
		
		qsample = rxbuffer[0] << 16;
		qsample |= (rxbuffer[1] & 0xFF) << 8;
		qsample |= (rxbuffer[2] & 0xFF << 0);
		isample = rxbuffer[3] << 16;
		isample |= (rxbuffer[4] & 0xFF) << 8;
		isample |= (rxbuffer[5] & 0xFF << 0);

		inlsamples[inoffset] = (float) isample / 8388607.0F; // 24 bit
																// sample
																// convert
																// to
																// -1..+1
		inrsamples[inoffset] = (float) qsample / 8388607.0F; // 24
																// bit
																// sample
																// convert
																// to
																// -1..+1

		inoffset++;

		if (inoffset == configuration.buffersize) {

			//System.out.print("."); //show progress
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
					audioBuffer
							.add((double) outlsamples[j] / 32767.0F);
				}
				index++;
			}
			index = index % 6;

			// Fill the stream with the samples
			RadioClients.getInstance().fillAudioStream(
					audioBuffer);

			wdsp.Spectrum(Display.RX, 0, 0, inrsamples,
					inlsamples);

			inoffset = 0;

			for (int j = 0; j < outlsamples.length; j++) {
				
				short lsample;
                short rsample;
                lsample = (short) (outlsamples[j] * 32767.0F * configuration.afgain);
                rsample = (short) (outrsamples[j] * 32767.0F * configuration.afgain);
				
				audiooutput[audiooutputindex++] = (byte) ((lsample >> 8) & 0xFF);
				audiooutput[audiooutputindex++] = (byte) (lsample & 0xFF);
				audiooutput[audiooutputindex++] = (byte) ((rsample >> 8) & 0xFF);
				audiooutput[audiooutputindex++] = (byte) (rsample & 0xFF);
				if (audiooutputindex == audiooutput.length) {
					LocalAudio.getInstance().writeAudio(audiooutput);
					audiooutputindex = 0;
				}
			}

		}
	}
	
	
	public void setRXFrequency(int freq) {
		
		this.freq = freq;
		
		packet[1] = 0x20;
		packet[2] = (byte) ((freq >> 24) & 0xFF);
		packet[3] = (byte) ((freq >> 16) & 0xFF);
		packet[4] = (byte) ((freq >> 8) & 0xFF);
		packet[5] = (byte) ((freq) & 0xFF);
		//System.out.println("freq out = " + freq);
		//for (int i = 0; i < 6; i++){
		//	System.out.print(String.format("%02x ", packet[i]));
		//}
		//System.out.println();
	}
	
}
