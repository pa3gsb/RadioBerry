package org.radioberry.service;

import java.util.Timer;
import java.util.TimerTask;

import org.g0orx.openhpsdr.wdsp.WDSP;
import org.radioberry.utility.Channel;
import org.radioberry.utility.Display;

public class SpectrumStream {

	private WDSP wdsp;
	private float[] samples = new float[1280];

	public SpectrumStream() {
		this.wdsp = WDSP.getInstance();
		this.setTimer();
	}

	public void setTimer() {
		TimerTask timerTask = new TimerTask() {

			@Override
			public void run() {
				int[] result = new int[1];
				wdsp.GetPixels(Display.RX, samples, result);
				if (result[0] == 1)
					RadioClients.getInstance().spectrum(samples);

				int meter = (int) wdsp.GetRXAMeter(Channel.RX, WDSP.S_AV);
				// meterView.setMeter(meter);
				//System.out.println("meter s = " + meter);

			}
		};

		Timer timer = new Timer("Spectrum Timer");

		timer.scheduleAtFixedRate(timerTask, 2000,
				1000 / Configuration.getInstance().fps);
	}
}
