package org.radioberry.spi;

import java.io.IOException;
import java.util.Arrays;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;

public class LoopBackTest {

	// SPI device
    public static SpiDevice spi = null;
	
	public static void main(String args[]) throws IOException{
		
		
	System.out.println("<--Pi4J--> SPI test loopback test");

	// create SPI object instance for SPI for communication
    spi = SpiFactory.getInstance(SpiChannel.CS0,
                                 SpiDevice.DEFAULT_SPI_SPEED, // default spi speed 1 MHz
                                 SpiDevice.DEFAULT_SPI_MODE); // default spi mode 0
		

    while  (true){
    
    	loop();
    
    }
		
	}
	
	
	public static void loop() throws IOException {
		byte packet[] = new byte[2048];
		
		byte value = (byte) 0xFF;
		for (int i=0; i < 2048; i++){
			packet[i] = value;
			value ++;
		}
		
		byte[] result = spi.write(packet);
		
		System.out.println("Ontvangen " + Arrays.toString(result));
		
		
	}
	
	
}
