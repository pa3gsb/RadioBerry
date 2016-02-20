package org.radioberry.radio;

import java.io.File;

import org.g0orx.openhpsdr.wdsp.WDSP;
import org.radioberry.service.Configuration;
import org.radioberry.service.Generator;
import org.radioberry.service.RXIQData;
import org.radioberry.utility.Channel;
import org.radioberry.utility.Display;
import org.radioberry.utility.Modes;


public class Radio {

	private WDSP wdsp;

	private int frequency;
	private Configuration configuration;
	
	private Generator generator;
	private RXIQData rxData;

	public RXIQData getRxData() {
		return rxData;
	}

	public Generator getGenerator() {
		return generator;
	}

	public static Radio getInstance() {
		if (instance == null) {
			instance = new Radio();
		}
		return instance;
	}

	private static Radio instance;

	private Radio() {
		configuration = Configuration.getInstance();
		wdsp = WDSP.getInstance();
		//generator = new Generator();
		rxData = new RXIQData();
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
		
		rxData.setRXFrequency(frequency);
		
		//generator.setVFOFrequency(frequency);

		//SDRT9RControlMessage.getInstance().setFrequency(frequency);
	}
	
	public void start() {
		 if(System.getProperty("os.name").startsWith("Windows")) {
	            String libraryPath=System.getProperty("user.dir");
	            wdsp.WDSPwisdom(libraryPath+File.separator);
	        } else if(System.getProperty("os.name").startsWith("Linux")) {
	        	String libraryPath=System.getProperty("user.dir");
	        	//libraryPath = libraryPath + File.separator + "lib" + File.separator + "linux";
	        	wdsp.WDSPwisdom(libraryPath+File.separator);
	        }
		 
		// setup receiver
		wdsp.OpenChannel(Channel.RX, configuration.buffersize,
				configuration.fftsize, (int) configuration.samplerate,
				(int) configuration.dsprate, 48000,
				0/* rx */, 1/* RUNNING */, 0.010, 0.025, 0.0, 0.010, 0);

		wdsp.SetRXAMode(Channel.RX, Modes.AM);
		wdsp.SetRXABandpassFreqs(Channel.RX, Modes.filterLow[Modes.AM],
				Modes.filterHigh[Modes.AM]);
		wdsp.SetRXABandpassRun(Channel.RX, 1);
		setAGC(Channel.RX, 0);
		wdsp.SetRXAAGCTop(Channel.RX, 120);
		wdsp.SetRXAAMDSBMode(Channel.RX, 0);

		wdsp.SetRXAEMNRRun(Channel.RX, configuration.NB2 ? 1 : 0);
		wdsp.SetRXAEMNRgainMethod(Channel.RX, configuration.NB2_GAIN);
		wdsp.SetRXAEMNRnpeMethod(Channel.RX, configuration.NB2_NPE);
		wdsp.SetRXAEMNRaeRun(Channel.RX, configuration.NB2_AE ? 1 : 0);
		wdsp.SetRXAEMNRPosition(Channel.RX, configuration.NB2_POSITION);
		wdsp.SetRXAANRRun(Channel.RX, configuration.NR ? 1 : 0);
		wdsp.SetRXAANFRun(Channel.RX, configuration.ANF ? 1 : 0);

		setSpectrumAnalyzer();

	}
	
	public void setAGCgain(double agcGain) {
		wdsp.SetRXAAGCTop(Channel.RX, agcGain);
	}
	

	public void setMode(int mode) {
		wdsp.SetRXAMode(Channel.RX, mode);
//		wdsp.SetRXABandpassFreqs(Channel.RX, Modes.filterLow[mode],
//				Modes.filterHigh[mode]);
	}
	
	public void setAGCMode(int AGCMode) {
		setAGC(Channel.RX, AGCMode);
	}

	private void setAGC(int channel, int agc) {
		wdsp.SetRXAAGCMode(channel, agc);
		switch (agc) {
		case Modes.AGC_LONG:
			wdsp.SetRXAAGCAttack(channel, 2);
			wdsp.SetRXAAGCHang(channel, 2000);
			wdsp.SetRXAAGCDecay(channel, 2000);
			break;
		case Modes.AGC_SLOW:
			wdsp.SetRXAAGCAttack(channel, 2);
			wdsp.SetRXAAGCHang(channel, 1000);
			wdsp.SetRXAAGCDecay(channel, 500);
			break;
		case Modes.AGC_MEDIUM:
			wdsp.SetRXAAGCAttack(channel, 2);
			wdsp.SetRXAAGCHang(channel, 0);
			wdsp.SetRXAAGCDecay(channel, 250);
			
			break;
		case Modes.AGC_FAST:
			wdsp.SetRXAAGCAttack(channel, 2);
			wdsp.SetRXAAGCHang(channel, 0);
			wdsp.SetRXAAGCDecay(channel, 50);
			break;
		}
	}

	public void setFilterLowAndHigh(int low, int high) {
		wdsp.SetRXABandpassFreqs(Channel.RX, low, high);
	}
	
	public void setShift(double shift) {
		wdsp.SetRXAShiftFreq(Channel.RX, shift);
	}

	public void stop() {
		wdsp.DestroyAnalyzer(Display.RX);
		wdsp.SetChannelState(Channel.RX, 0, 0);
		wdsp.CloseChannel(Channel.RX);
	}

	private void setSpectrumAnalyzer() {
		int[] error = new int[1];
		int[] success = new int[1];

		// rx spectrum
		wdsp.XCreateAnalyzer(Display.RX, success, 262144, 1, 1, "");
		if (success[0] != 0) {
			// Log.i("Metis", "XCreateAnalyzer Display.RX failed:" +
			// success[0]);
		}
		int flp[] = { 0 };
		double KEEP_TIME = 0.1;
		int spur_elimination_ffts = 1;
		int data_type = 1;
		int fft_size = 8192;
		int window_type = 4;
		double kaiser_pi = 14.0;
		int overlap = 2048;
		int clip = 0;
		int span_clip_l = 0;
		int span_clip_h = 0;
		int pixels = 1280;
		int stitches = 1;
		int avm = 0;
		double tau = 0.001 * 120.0;
		int MAX_AV_FRAMES = 60;
		int display_average = Math.max(
				2,
				(int) Math.min((double) MAX_AV_FRAMES,
						(double) Configuration.getInstance().fps * tau));
		double avb = Math.exp(-1.0 / (Configuration.getInstance().fps * tau));
		int calibration_data_set = 0;
		double span_min_freq = 0.0;
		double span_max_freq = 0.0;

		int max_w = fft_size
				+ (int) Math.min(
						KEEP_TIME * (double) Configuration.getInstance().fps,
						KEEP_TIME * (double) fft_size
								* (double) Configuration.getInstance().fps);

		wdsp.SetAnalyzer(Display.RX, spur_elimination_ffts, // number of LO
															// frequencies =
															// number of ffts
															// used in
															// elimination
				data_type, // 0 for real input data (I only); 1 for complex
							// input data (I & Q)
				flp, // vector with one elt for each LO frequency, 1 if
						// high-side LO, 0 otherwise
				fft_size, // size of the fft, i.e., number of input samples
				configuration.buffersize, // number of samples
														// transferred for each
				// OpenBuffer()/CloseBuffer()
				window_type, // integer specifying which window function to use
				kaiser_pi, // PiAlpha parameter for Kaiser window
				overlap, // number of samples each fft (other than the first) is
							// to re-use from the previous
				clip, // number of fft output bins to be clipped from EACH side
						// of each sub-span
				span_clip_l, // number of bins to clip from low end of entire
								// span
				span_clip_h, // number of bins to clip from high end of entire
								// span
				pixels, // number of pixel values to return. may be either <= or
						// > number of bins
				stitches, // number of sub-spans to concatenate to form a
							// complete span
				avm, // averaging mode
				display_average, // number of spans to (moving) average for
									// pixel result
				avb, // back multiplier for weighted averaging
				calibration_data_set, // identifier of which set of calibration
										// data to use
				span_min_freq, // frequency at first pixel value8192
				span_max_freq, // frequency at last pixel value
				max_w // max samples to hold in input ring buffers
		);
	}



}
