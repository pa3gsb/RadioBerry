package org.radioberry.firmware;

import java.io.FileInputStream;
import java.io.InputStream;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class FPGALoader {

	private final GpioController gpio;

	private final GpioPinDigitalInput iPinCONF_DONE;
	private final GpioPinDigitalInput iPinNSTATUS;

	private final GpioPinDigitalOutput oPinNCONFIG;
	private final GpioPinDigitalOutput oPinDATA;
	private final GpioPinDigitalOutput oPinDCLK;

	public FPGALoader() {

		// create gpio controller
		gpio = GpioFactory.getInstance();

		// Input pins..
		iPinCONF_DONE = gpio.provisionDigitalInputPin(RaspiPin.GPIO_03, "CONF_DONE");
		iPinNSTATUS = gpio.provisionDigitalInputPin(RaspiPin.GPIO_25, "NSTATUS");

		// Output pins...
		oPinNCONFIG = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "NCONFIG", PinState.LOW);
		oPinDATA = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_23, "DATA[0]", PinState.LOW);
		oPinDCLK = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, "DCLK", PinState.LOW);
	}

	public void load() throws Exception {
		System.out.println("");
		System.out.println("XXXXXXXXXXX-----------------XXXXXXXXXXXXXXXX");
		System.out.println("");
		System.out.println("PS serial loading FPGA");

		startDeviceInitialisation();
		
		if (prepareLoading()) {
			loadFPGAData();
			System.out.println("check if loading succeed");
			if (isLoadingSucceeded()) {
				startDeviceInitialisation();
				System.out.println("Loading firmware FPGA ended.");
				return;
			}
		}

		System.out.println("Loading firmware FPGA failed.");
	}

	private void startDeviceInitialisation()
	{
		System.out.println("Device initialisation started.");
		// After conf_done goes high; 2 dclk falling edges are required.
		triggerData();
		triggerData();
		System.out.println("Device initialisation ended.");
	}
	
	private boolean isLoadingSucceeded() {
		boolean result = false;

		System.out.println("isLoadingSucceeded");
		if (iPinNSTATUS.isState(PinState.LOW)) {
			System.out.println("NSTATUS still low ; loading FPGA failed");
			result = false;
		} else if (iPinCONF_DONE.isState(PinState.HIGH)) {
			System.out.println("CONF_DONE high ; loading FPGA succesfull");
			result = true;
		}

		System.out.println("isLoadingSucceeded ended " + result);
		return result;
	}

	/**
	 * Set the nconfig pin... and wait for the nstatus to go high..
	 * 
	 * 
	 * @return false: FPGA config does not start true: FPGA condig
	 *         started..ready to send config data.
	 * @throws Exception
	 */

	private boolean prepareLoading() throws Exception {

		System.out.println("start prepareLoading");

		System.out.println("nstatus true = high = " + iPinNSTATUS.isState(PinState.HIGH));
		
		oPinNCONFIG.low();
		oPinDATA.low();
		oPinDCLK.low();
		Thread.sleep(1);
		oPinNCONFIG.high();

		int count = 0;
		while (iPinNSTATUS.isState(PinState.LOW)) {
			count++;
			Thread.sleep(10);
			if (count >= 255) {
				System.out.println("prepareLoading failed");
				return false;
			}
		}

		System.out.println("end prepareLoading");
		return true;
	}

	private void loadFPGAData() throws Exception {

		System.out.println("start loadFPGAData");

		try (InputStream inputStream = new FileInputStream("radioberry.rbf");) {
			Integer c;

			System.out.println("Input stream open...");
			int count = 0;
			// continue reading till the end of the file
			while ((c = inputStream.read()) != -1) {
				count++;
				
				if (iPinNSTATUS.isState(PinState.LOW)){
					
					System.out.println("config error");
					return;
				}
				
				// First program LSB for each byte
				loadConfigByte(((byte) c.intValue()));
			}
			
			System.out.println("byte read; and uploaded " + count);
			
			System.out.println("Input stream uploaded to FPGA.");
		}

		System.out.println("end loadFPGAData");
	}

	private void loadConfigByte(byte data) {
		for (int position = 0; position < 8; position++) {
			if (((data >> position) & 1) != 0) {
				setDataHigh();
			} else {
				setDataLow();
			}
		}
	}

	private void setDataHigh() {
		oPinDATA.high();
		triggerData();
	}

	private void setDataLow() {
		oPinDATA.low();
		triggerData();
	}

	private void triggerData() {
		// clock the data
		oPinDCLK.high();
		oPinDCLK.low();
	}
}
