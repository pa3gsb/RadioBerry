package org.hermeslite.openhpsdr;

public class CommandControl {

	int nrx = 1;        //number of receivers
	
    byte[] CC = new byte[5];
    boolean MOX = false;
    boolean holdMox = false;
    byte sampleSpeed = 0x00;
    byte holdSampleSpeed = 0x00;
    byte[] RXFrequency = new byte[4];
    int freq = 0;
    byte[] holdRXFrequency = new byte[4];
    int holdFreq = 0;
    byte TLVGain = 0x00;
    byte holdTLVGain = 0x00;
    boolean boost = false;
    boolean holdboost = false;
    boolean mic = true;
    boolean holdmic = true;
    byte drive_level = 0x00;
    byte hold_drive_level = 0x00;
    boolean ltc2208dither = false;
    boolean holdltc2208dither = false;
    boolean ltc2208random = false;
    boolean holdltc2208random = false;
    boolean ltc2208preamp = false;
    boolean holdltc2208preamp = false;
	
    int[] samplerate={48000,96000,192000,384000};
    protected int getSampleSpeed() {
    	return samplerate[sampleSpeed];
    }
    
    protected int getRXFrequency(){
    	return freq;
    }
    
    protected void CommandAndControl(byte[] data)
    {
        CC[0] = data[0];
        CC[1] = data[1];
        CC[2] = data[2];
        CC[3] = data[3];
        CC[4] = data[4];

        MOX = (((byte)CC[0] & 0x01)==0x01) ? true:false;
        
        // select Command
        if (((byte)CC[0] & 0xFE)  == 0x00) {
            sampleSpeed = (byte) (CC[1] & 0x03);


            nrx = (((byte)(CC[4] & 0x38) >> 3) + 1);

            //System.out.println("Nr rx " + nrx);
                           
            ltc2208preamp = false;
            if (((byte)CC[3] & (byte)0x04) == 0x04)
                ltc2208preamp = true;

            ltc2208dither = false;
            if (((byte)CC[3] & (byte)0x08) == 0x08)
                ltc2208dither = true; 
            
            ltc2208random = false;
            if (((byte)CC[3] & (byte)0x10) == 0x10)
                ltc2208random = true;

            
        }
        // select Command
        if (((byte)CC[0] & 0xFE) == 0x04)
        {
            RXFrequency[0] = CC[4]; //LSB
            RXFrequency[1] = CC[3];
            RXFrequency[2] = CC[2];
            RXFrequency[3] = CC[1]; //MSB
         
            freq = ((RXFrequency[3] & 0xFF) << 24) | ((RXFrequency[2] & 0xFF) << 16)
                    | ((RXFrequency[1] & 0xFF) << 8) | (RXFrequency[0] & 0xFF);
        }

        // select Command
        if (((byte)CC[0] & 0xFE) == 0x12)
        {
            mic = true;
            boost = false;
            //line
            if (((byte)CC[2] & 0x02) == 0x02)
            {
                mic = false;
            }
            else if ((((byte)CC[2] & 0x01) == 0x01))
            {
                boost = true;
            }
                           
            drive_level = CC[1];
           
        }

        // select Command
        if (((byte)CC[0] & 0xFE) == 0x14)
        {
            TLVGain = (byte)(CC[2] & 0x1F);
            
        }
    }
	
    
    protected boolean isControlDataChanged()
    {
        boolean result = false;

        result = ((MOX == holdMox) ? false : true);
        result = result || ((boost == holdboost) ? false : true);
        result = result || ((TLVGain == holdTLVGain) ? false : true);
        result = result || ((freq == holdFreq) ? false : true);
        result = result || ((sampleSpeed == holdSampleSpeed) ? false : true);
        result = result || ((drive_level == hold_drive_level) ? false : true);
        result = result || ((mic == holdmic) ? false : true);
        result = result || ((ltc2208dither == holdltc2208dither) ? false : true);
        result = result || ((ltc2208random == holdltc2208random) ? false : true);
        result = result || ((ltc2208preamp == holdltc2208preamp) ? false : true);

        if (result) saveControlData();

        if (result)
        {
            System.out.println("Mox = " + MOX);
            System.out.println("sampleSpeed = " + sampleSpeed);
            System.out.println("RXFrequency = " + freq);
            System.out.println("boost = " + boost);
            System.out.println("mic = " + mic);
            System.out.println("drive_level = " + drive_level);
            System.out.println("TLVGain = " + TLVGain);
            System.out.println("ltc2208 dither = " + ltc2208dither);
            System.out.println("ltc2208 random = " + ltc2208random);
            System.out.println("ltc2208 preamp = " + ltc2208preamp);
        }

        return result;
    }

    private void saveControlData()
    {
        holdMox = MOX;
        holdboost  = boost;
        holdTLVGain = TLVGain;
        holdFreq = freq;
        holdSampleSpeed = sampleSpeed;
        hold_drive_level = drive_level;
        holdmic = mic;
        holdltc2208dither = ltc2208dither;
        holdltc2208random = ltc2208random;
        holdltc2208preamp = ltc2208preamp;
    }
    
	
}
