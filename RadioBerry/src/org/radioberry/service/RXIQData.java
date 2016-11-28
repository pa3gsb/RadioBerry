package org.radioberry.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.g0orx.openhpsdr.wdsp.WDSP;
import org.radioberry.utility.Channel;
import org.radioberry.utility.Display;

import com.pi4j.io.spi.SpiMode;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.GpioUtil;
import com.pi4j.wiringpi.Spi;

import de.fxworld.jbcm2835.FunctionSelect;
import de.fxworld.jbcm2835.JBcm2835Library;
import de.fxworld.jbcm2835.RPiGPIOPin;
import de.fxworld.jbcm2835.SPIBitOrder;
import de.fxworld.jbcm2835.SPIChipSelect;
import de.fxworld.jbcm2835.SPIClockDivider;
import de.fxworld.jbcm2835.SPIMode;


public class RXIQData implements Runnable {

//	private GpioController gpio;
//	private GpioPinDigitalInput iPinSPIValid;

	// SPI device
	//public static SpiDevice spi = null;
	int spi;

	// required to get the 6th sample from the subsequent buffers
	int index = 0;
	byte packet[] = new byte[6];
	int freq;

	private int inoffset = 0;
	private float[] inlsamples;
	private float[] inrsamples;

	private int isample;
	private int qsample;

	private Configuration configuration;

	double amplitude;

	private float[] outlsamples;
	private float[] outrsamples;

	private WDSP wdsp;
	private int[] error = new int[1];

	private volatile Thread thread;

	// Local audio output
	private byte[] audiooutput = new byte[1024 * 4]; // 2 channels of shorts
	private int audiooutputindex = 0;

	private long start = 0;
	
	private long count =0;

	public RXIQData() {
		// create gpio controller
		 System.out.print("Test SPI\n");
		 JBcm2835Library.init();
		JBcm2835Library.bcm2835_spi_begin();
		JBcm2835Library.bcm2835_spi_setBitOrder(SPIBitOrder.LSBFIRST);  
		JBcm2835Library.bcm2835_spi_setDataMode(SPIMode.SPI_MODE3); //JBcm2835Library.BCM2835_SPI_MODE3);
		JBcm2835Library.bcm2835_spi_setClockDivider(SPIClockDivider.SPI_CLOCK_DIVIDER_16);//BCM2835_SPI_CLOCK_DIVIDER_16
		JBcm2835Library.bcm2835_spi_chipSelect(SPIChipSelect.SPI_CS0);//JBcm2835Library.BCM2835_SPI_CS0);
		//JBcm2835Library.bcm2835_spi_setChipSelectPolarity(SPIChipSelect.SPI_CS0,  (byte) 0);  //);JBcm2835Library.BCM2835_SPI_CS0,bcm2835.LOW);
		
		System.out.println("SPI initialized...");
		
		//while ( bcm2835_gpio_lev(RPI_BPLUS_GPIO_J8_33 ) == HIGH) {}; 
		
		JBcm2835Library.bcm2835_gpio_fsel(RPiGPIOPin.RPI_BPLUS_GPIO_J8_33,FunctionSelect.GPIO_FSEL_INPT)  ;//(RPI_BPLUS_GPIO_J8_33 , BCM2835_GPIO_FSEL_INPT);	
		//JBcm2835Library.bcm2835_gpio_lev(RPiGPIOPin.RPI_BPLUS_GPIO_J8_33.getValue());
		
//		JBcm2835Library.bcm2835_spi_end();
//		JBcm2835Library.close();
		
//		if (Gpio.wiringPiSetup() == -1) {
//			System.out.println(" ==>> GPIO SETUP FAILED");
//			return;
//		}
//		
//		GpioUtil.export(23, GpioUtil.DIRECTION_IN);
//        Gpio.pinMode (23, Gpio.INPUT) ;
		
		// RaspiGpioProvider prov = new RaspiGpioProvider();
//		gpio = GpioFactory.getInstance();
//		gpio.shutdown();
//		gpio = null;
//		gpio = GpioFactory.getInstance();
//		Pin pin = CommandArgumentParser.getPin(RaspiPin.class, RaspiPin.GPIO_23);
//		PinPullResistance pull = CommandArgumentParser.getPinPullResistance(PinPullResistance.OFF);
//		iPinSPIValid = gpio.provisionDigitalInputPin(pin, "RXFIFOEMPTY", pull);
//		System.out.println("pin info adres = " + pin.getAddress() + "provide" + pin.getProvider() + "" + pin.supportsPinPullResistance());

		configuration = Configuration.getInstance();
		setRXFrequency(4607000);
		inlsamples = new float[configuration.buffersize];
		inrsamples = new float[configuration.buffersize];
		// create SPI object instance for SPI for communication
//		try {
//			spi = SpiFactory.getInstance(SpiChannel.CS0, 16000000, SpiMode.MODE_3);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		//spi = Spi.wiringPiSPISetupMode(Spi.CHANNEL_0, 32000000, Spi.MODE_3);
		
//		int fd = Spi.wiringPiSPISetupMode(Spi.CHANNEL_0, 8000000, Spi.MODE_3);
//        if (fd <= -1) {
//            System.out.println(" ==>> SPI SETUP FAILED");
//            return;
//        }

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

		start = System.nanoTime();
		
		while (thread == Thread.currentThread()) {
			// wait till rxFIFO buffer is filled with at least one element
			//while (Gpio.digitalRead(23) == 1) {};
			while (JBcm2835Library.bcm2835_gpio_lev(RPiGPIOPin.RPI_BPLUS_GPIO_J8_33.getValue()) == 1) {}
			setRXFrequency(this.freq);
//			byte[] rxbuffer = null;
//			try {
//				rxbuffer = spi.write(packet);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
//			byte[] buffer = new byte[length];
//	        System.arraycopy(data, start, buffer, 0, length);
			
//			long startSpi = System.nanoTime();
//			int i = Spi.wiringPiSPIDataRW(Spi.CHANNEL_0, packet);
//			if (i <= 0) {
//				System.out.println("Failed to write data to SPI channel");
//			}
			
			ByteBuffer bb = ByteBuffer.allocate(6);
			bb.put(packet);
			JBcm2835Library.bcm2835_spi_transfern(bb, 6);
			
//			long stopSpi = System.nanoTime();
//			long spitime = stopSpi - startSpi;
//			if (spitime <20000)
//				//System.out.println("spi time "+ spitime);
//			//else
//				System.out.println("snel genoeg");			
			//processReceivedData(rxbuffer);
			processReceivedData(packet);
		}
	}

	private void processReceivedData(byte[] rxbuffer) {
		qsample = rxbuffer[0] << 16;
		qsample += (rxbuffer[1] & 0xFF) << 8;
		qsample += (rxbuffer[2] & 0xFF << 0);
		isample = rxbuffer[3] << 16;
		isample += (rxbuffer[4] & 0xFF) << 8;
		isample += (rxbuffer[5] & 0xFF << 0);

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
		
		count++;

		if (inoffset == configuration.buffersize) {

			

			// System.out.print("."); //show progress
			// DSP demodulation
			//wdsp.fexchange2(Channel.RX, inlsamples, inrsamples, outlsamples, outrsamples, error);

			// Put only the right sample in the buffer (maybe
			// later the other channel as well)
			//List<Double> audioBuffer = new ArrayList<Double>();
			// place each 6th sample in the buffer (starting
			// with the first)
//			for (int j = 0; j < configuration.buffersize; j++) {
//				if (index % 6 == 0) {
//					audioBuffer.add((double) outlsamples[j] / 32767.0F);
//				}
//				index++;
//			}
//			index = index % 6;

			// Fill the stream with the samples
	//		RadioClients.getInstance().fillAudioStream(audioBuffer);

	//		wdsp.Spectrum(Display.RX, 0, 0, inrsamples, inlsamples);

			inoffset = 0;

			// for (int j = 0; j < outlsamples.length; j++) {
			//
			// short lsample;
			// short rsample;
			// lsample = (short) (outlsamples[j] * 32767.0F *
			// configuration.afgain);
			// rsample = (short) (outrsamples[j] * 32767.0F *
			// configuration.afgain);
			//
			// audiooutput[audiooutputindex++] = (byte) ((lsample >> 8) & 0xFF);
			// audiooutput[audiooutputindex++] = (byte) (lsample & 0xFF);
			// audiooutput[audiooutputindex++] = (byte) ((rsample >> 8) & 0xFF);
			// audiooutput[audiooutputindex++] = (byte) (rsample & 0xFF);
			// if (audiooutputindex == audiooutput.length) {
			// LocalAudio.getInstance().writeAudio(audiooutput);
			// audiooutputindex = 0;
			// }
			// }

		}
		
		if (count == 48000){
			
			double time = (System.nanoTime() - start) / 1000000d;
			System.out.println("samen stellen packet " + time + " msec");
			start = System.nanoTime();
			count =0;
		}
	}

	public void setRXFrequency(int freq) {
		this.freq = freq;
		packet[0] = 0x00; // sample rate 48K
		packet[1] = 0x00;
		packet[2] = (byte) ((freq >> 24) & 0xFF);
		packet[3] = (byte) ((freq >> 16) & 0xFF);
		packet[4] = (byte) ((freq >> 8) & 0xFF);
		packet[5] = (byte) ((freq) & 0xFF);
	}
}
