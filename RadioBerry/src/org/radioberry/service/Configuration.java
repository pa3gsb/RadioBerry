package org.radioberry.service;


public class Configuration {

	public final static int PORT = 1024;
	
	
	public int fps=10;
	
	public double samplerate=48000.0;
    public double dsprate = 48000.0;
    public int buffersize = 1024;
    public int fftsize = 4096;
    //public static final int fftsize = 1024;
    
    public boolean NB2 = false;
    public boolean NB2_AE = true;
    public int NB2_GAIN = 1; // 0:Linear 1:Log
    public int NB2_NPE = 0; // 0:OSMS 1:MMSE
    public int NB2_POSITION = 1; // 0:PRE-AGC 1:POST-AGC
    
    public boolean NR = false;
    public boolean NB = false;
    public boolean ANF = false;
	
	
	public final static String IP_BROADCAST = "255.255.255.255";
	public final static String IP_SDR_PROGRAM = "169.254.190.181";
	public final static String IP_SDR_T9R = "169.254.190.182";

	public final static int RX_IQ_PORT = 20010;
	public final static int CONTROL_PORT = 20050;
	public final static int BROADCAST_TX_PORT = 20060;
	public final static int BROADCAST_RX_PORT = 20070;
	
	public static String address = "169.254.190.180";
	public static int port = 20021;
	
	
	public float afgain = 1.0F;
	
	public static Configuration getInstance() {
		if(instance==null) {
			instance=new Configuration();
		}
		return instance;
	}

	private Configuration() {
	}

    private static Configuration instance;
	
}
