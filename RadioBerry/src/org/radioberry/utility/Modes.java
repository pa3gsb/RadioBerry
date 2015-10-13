package org.radioberry.utility;

public class Modes {
	public static final int LSB=0;
	public static final int USB=1;
	public static final int DSB=2;
	public static final int CWL=3;
	public static final int CWU=4;
	public static final int FMN=5;
	public static final int AM=6;
	public static final int DIGU=7;
	public static final int SPEC=8;
	public static final int DIGL=9;
	public static final int SAM=10;
	public static final int DRM=11;
	
	public static String[] modes={"LSB","USB","DSB","CWL","CWU","FMN","AM","DIGU","SPEC","DIGL","SAM","DRM"};
	public static int[] filterLow={-2700, 300,-2700,-900,300,-3300,-4000, 300,-4000,-2700,-4000,-6000};
	public static int[] filterHigh={-300,2700, 2700,-300,900, 3300, 4000,2700, 4000, -300, 4000, 6000};
	
	public static String mode(int mode) {
		return modes[mode];
	}
	
	
	public static final int AGC_OFF = 0;
	public static final int AGC_LONG = 1;
	public static final int AGC_SLOW = 2;
	public static final int AGC_MEDIUM = 3;
	public static final int AGC_FAST = 4;
	
	
	
	
	
}
