package org.radioberry.clock;

import java.io.IOException;
import java.io.Serializable;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

public class SI570 implements Serializable {

	private final I2CBus bus;
	private I2CDevice si570;

	double fxtal;
	long newFreq;

	public SI570() throws Exception {
		bus = I2CFactory.getInstance(I2CBus.BUS_1);
		si570 = bus.getDevice(0x55);
		
		Xtal xtal = new Xtal().readSerializedObject();
		
		// read the settings and print...
		byte[] si570reg = getSI570Settings();
		
		if (null!=xtal) {
			fxtal = xtal.getXtal();
		} else {
			
			calcFixedXtalFreq(si570reg);
			
			xtal = new Xtal();
			xtal.setXtal(fxtal);
			xtal.writeSerializedObject();
		}
		
	}

	public void setFrequency(int freq) throws Exception {
		
		this.newFreq = freq;
		
		writeFreq();
		
	}

	private byte[] getSI570Settings() throws IOException {
		byte[] si570reg = new byte[6];
		// First reading registers of the SI570
		// Based on the readings further calculations are possible
		si570.read(((byte) 0x07), si570reg, 0, 6); // read 6 registers

		// print the register content register 7 - 12
		for (int counter = 0; counter < 6; counter++) {
			System.out.println(String.format("%02x ", si570reg[counter]));
		}
		return si570reg;
	}

	static int[] HS_MAP = { 4, 5, 6, 7, 0, 9, 0, 11 };
	static double TWO_POWER28 = 268435456.0;
	static double FREQ_NOMINAL = 56.320;
	static double FOSC_MAX = 5670.0;
	static double FOSC_MIN = 4850.0;

	private void calcFixedXtalFreq(byte[] registers) {
		byte[] bytes = new byte[6]; // need to read 6 bytes

		for (int i = 0; i < 6; i++) {
			bytes[i] = registers[i];
		}

		int hs_div = (bytes[0] >> 5) & 7;
		int n1 = (bytes[0] & 0x1f) << 2;
		n1 |= (bytes[1] >> 6) & 3;

		System.out.println("HS_DIV divider: " + HS_MAP[hs_div] + " (" + hs_div + ")");
		System.out.println("N1: " + n1);

		int ref_freq = (bytes[1] & 0x3f) << 4;
		ref_freq |= (bytes[2] >> 4) & 0xf;

		int ref_lo = (bytes[5] & 0xff);
		ref_lo |= (bytes[4] & 0xff) << 8;
		ref_lo |= (bytes[3] & 0xff) << 16;
		ref_lo |= (bytes[2] & 0xf) << 24;

		System.out.println("ref_whole: " + ref_freq + " reff_fractional: " + ref_lo);
		double reff = ref_lo / TWO_POWER28;
		reff += ref_freq;

		System.out.println("Ref Freq: " + reff);

		fxtal = (FREQ_NOMINAL * (1 + n1) * HS_MAP[hs_div]) / reff;

		System.out.println("fxtal: " + fxtal);
	}

	// is there a good HS value for this total_div ratio
	// hs need to be 4, 5, 6, 7, 9, 11
	private static int[] HS_VALUES = { 11, 9, 7, 6, 5, 4 };

	private static int findGoodHS(int total_div) {
		for (int j = 0; j < HS_VALUES.length; j++) {
			if (total_div % HS_VALUES[j] == 0) {
				int res = total_div / HS_VALUES[j];
				if (res == 1)
					return HS_VALUES[j];
				if ((res & 1) == 0)
					return HS_VALUES[j];
			}
		}
		return -1;
	}

	private void writeFreq() throws Exception {

		if (fxtal == 0)
			return;

		double new_freq = newFreq / 1000000.0;

		System.out.println("new_freq: " + new_freq);
		double total_divide_max = FOSC_MAX / new_freq;
		double total_divide_min = FOSC_MIN / new_freq;

		int min_divide = (int) Math.ceil(total_divide_min);
		int max_divide = (int) Math.floor(total_divide_max);
		int new_hs = -1;
		int total_div = 0;
		for (int i = min_divide; i <= max_divide; i++) {
			total_div = i;
			new_hs = findGoodHS(total_div);
			if (new_hs > 0)
				break;
		}
		if (new_hs <= 0) {
			System.out.println("could not find good HS divider!!\n");
		}

		int new_n1 = total_div / new_hs;
		System.out.println("hs: " + new_hs + " n1: " + new_n1);

		double f_osc = new_freq * new_n1 * new_hs;

		double new_reff = f_osc / fxtal;

		System.out.println("f_osc: " + f_osc + " new_reff: " + new_reff);

		// calculate reffreq integer
		System.out.println("new_reff (float): " + new_reff);
		int new_reff_whole = (int) Math.floor(new_reff);
		System.out.println("new_reff_whole: " + new_reff_whole);
		long reff_bytes = ((long) new_reff_whole) << 28;
		System.out.println("reff bytes (whole): " + reff_bytes);
		double new_reff_fraction = new_reff - (double) new_reff_whole;
		System.out.println("new_reff_fraction: " + new_reff_fraction);
		int fractional_bytes = (int) Math.floor((double) new_reff_fraction * (double) TWO_POWER28);
		System.out.println("fractional bytes: " + fractional_bytes);
		reff_bytes |= fractional_bytes;
		System.out.println("reff bytes: " + reff_bytes);

		byte[] new_regs = new byte[6];

		new_regs[5] = (byte) (reff_bytes & 0xff);
		new_regs[4] = (byte) ((reff_bytes >> 8) & 0xff);
		new_regs[3] = (byte) ((reff_bytes >> 16) & 0xff);
		new_regs[2] = (byte) ((reff_bytes >> 24) & 0xff);

		new_regs[1] = (byte) (((long) (reff_bytes >> 32)) & 0x3f);
		--new_n1;
		new_regs[1] |= (byte) ((new_n1 & 3) << 6);

		new_regs[0] = (byte) ((new_n1 >> 2) & 0x1f);
		int new_hs_bits = -1;
		for (int j = 0; j < HS_MAP.length; j++) {
			if (HS_MAP[j] == new_hs) {
				new_hs_bits = j;
				break;
			}
		}
		if (new_hs_bits == -1) {
			System.out.println("Bad HS: " + new_hs);
			// break;
		}
		new_regs[0] |= (byte) (new_hs_bits << 5);

		for (int j = 0; j < new_regs.length; j++) {
			//System.out.println("newRegs[" + j + "]: " + new_regs[j]);
			System.out.println("newRegs[" + j + "]: " + String.format("%02x ", new_regs[j]));
		}

		si570.write(((byte) 137), ((byte) 0x10)); // assert freeze dco
		si570.write(((byte) 0x07), new_regs, 0, 6);
		si570.write(((byte) 137), ((byte) 0x00)); // deassert freeze dco
		si570.write(((byte) 135), ((byte) 0x40)); // set new freq
	}
}
