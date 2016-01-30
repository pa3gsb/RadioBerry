package org.hermeslite.hardware;

import java.io.IOException;

import org.hermeslite.utility.RingBuffer;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;


/**
 * 
 * RX emulator. 
 * 
 * 
 * Note : for actual hardware a separate RxHandler must be defined 
 * 
 * @author PA3GSB
 *
 */
public class RxSPIHandler  implements Runnable, IRxHandler { 

	// SPI device
	public static SpiDevice spi = null;
	
	byte packet[] = new byte[6];
	
	int rxfreq= 0;
	
	private volatile Thread thread;

	private RingBuffer<Byte> rxIQBuffer;

	public RxSPIHandler(RingBuffer<Byte> rxIQBuffer) throws IOException {
		this.rxIQBuffer = rxIQBuffer;
		
		// create SPI object instance for SPI for communication
		spi = SpiFactory.getInstance(SpiChannel.CS0, 10000000, SpiMode.MODE_3);
	}

	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.start();
		}
	}

	public void stop() {
		while (rxIQBuffer.size() > 0) {
			try {
				rxIQBuffer.remove();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		thread = null;

	}

	@Override
	public void run() {
		//byte packet[] = new byte[6];

		byte value = (byte) 0x00;
		for (int i = 0; i < 6; i++) {
			packet[i] = value;
		}
		
		setRXFrequency(4607000);
		while (thread == Thread.currentThread()) {
			
			

			try {
				byte[] result = spi.write(packet);
				
				for (int i = 0; i < 6; i++) {
					try {
						//msb first
						rxIQBuffer.add(result[i]);
						if (rxfreq==4607000){
								//System.out.print(String.format("%02x ", result[i]));
						}
					} catch (InterruptedException e) {
						System.out.println("adding problem");
					}
				}
				//if (rxfreq==4607000){System.out.println();}
				
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
			
	}


	@Override
	public RingBuffer<Byte> getReceiveIQStream() {
		return rxIQBuffer;
	}

	@Override
	public void setRXFrequency(int freq) {
		
		rxfreq = freq;
		
		packet[2] = (byte) ((freq >> 24) & 0xFF);
		packet[3] = (byte) ((freq >> 16) & 0xFF);
		packet[4] = (byte) ((freq >> 8) & 0xFF);
		packet[5] = (byte) ((freq) & 0xFF);
		System.out.println("freq out = " + freq);
		for (int i = 0; i < 6; i++){
			System.out.print(String.format("%02x ", packet[i]));
		}
		System.out.println();
	}

	@Override
	public void setSamplingRate(int rate) {
		//not required yet with actual hardware...
	}

}
