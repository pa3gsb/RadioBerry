package org.radioberry.loader;

import org.radioberry.clock.SI570;
import org.radioberry.firmware.FPGALoader;

/**
 * 
 * Radioberry loader.
 * 
 * 
 * The loader code is specially created for the radioberry!!!
 * 
 * This class is responsible:
 * 			-) for setting the clock.
 * 			-) loading the fpga firmware.
 * 
 * @author PA3GSB
 *
 */
public class RadioBerryLoader {

	
	public static void main(String args[]) throws Exception{
		
		int freq = 73728000;
		
		if (args.length > 0){
			System.out.println("freq shift = " + args[0]);
			freq = freq + Integer.valueOf(args[0]);
		}
		
		System.out.println("We will program the SI570 with frequency " + freq);
		// sets the radioberry clock
		new SI570().setFrequency(freq);	
		// Load FPGA firmware
		new FPGALoader().load();  
		//Performance issue...created a C program!!! and called here:
		//ProcessBuilder pb = new ProcessBuilder("/home/pi/firmwareloader/loadFPGA");
		//pb.start();
	}
	
}
