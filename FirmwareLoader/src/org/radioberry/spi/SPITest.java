package org.radioberry.spi;

import java.io.IOException;
import java.util.Arrays;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;

public class SPITest {

	// SPI device
	public static SpiDevice spi = null;

	public static void main(String args[]) throws IOException {

		System.out.println("<--Pi4J--> SPI Master RPI - FPGA test");

		// create SPI object instance for SPI for communication
		//SpiDevice.DEFAULT_SPI_SPEED
		spi = SpiFactory.getInstance(SpiChannel.CS0, 10000000, // default
																					// spi
																					// speed
																					// 1
																					// MHz
				SpiMode.MODE_3);

		// byte transmit = 0x57;
		//
		//
		// byte result[] = spi.write(transmit);

		// System.out.println("received from fpga" + Arrays.toString(result));

		while (true) {
			
			loop();

//			byte transmit = (byte) 0xff;
//
//			byte result[] = spi.write(transmit);

		}

	}

	public static void loop() throws IOException {
		byte packet[] = new byte[6];

		byte value = (byte) 0x00;
		for (int i = 0; i < 6; i++) {
			packet[i] = value;
			value++;
		}

		byte[] result = spi.write(packet);

		// System.out.println("Ontvangen " + Arrays.toString(result));

		for (int i = 0; i < result.length; i++)
			System.out.print(String.format("%02x ", result[i]));

		System.out.println("");
	}
	
//	public static void loop() throws IOException {
//		byte packet[] = new byte[2048];
//
//		byte value = (byte) 0xFF;
//		for (int i = 0; i < 2048; i++) {
//			packet[i] = value;
//			value++;
//		}
//
//		byte[] result = spi.write(packet);
//
//		// System.out.println("Ontvangen " + Arrays.toString(result));
//
//		for (int i = 0; i < result.length; i++)
//			System.out.print(String.format("%02x ", result[i]));
//
//		System.out.println("");
//	}

}
