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
		// sets the radioberry clock
		new SI570().setFrequency(73728000);	
		// Load FPGA firmware
		//new FPGALoader().load();  Performance issue...created a C program!!! and called here:
		ProcessBuilder pb = new ProcessBuilder("/home/pi/firmwareloader/loadFPGA");
		pb.start();
	}
	
}
