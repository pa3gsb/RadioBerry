package org.radioberry.loader;

import com.pi4j.wiringpi.Spi;

public class FPGALoader {

	
	public static void main(String args[]){
		
	       // setup SPI for communication
        int fd = Spi.wiringPiSPISetup(0, 10000000);
		
		
		
	}
	
}
