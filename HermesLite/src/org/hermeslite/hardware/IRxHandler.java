package org.hermeslite.hardware;

import org.hermeslite.utility.RingBuffer;
/**
 * 
 * Interface which must be implemented to get the radio going...
 * 
 * @author PA3GSB
 *
 */
public interface IRxHandler {

	 RingBuffer<Byte> getReceiveIQStream();
	
	void setRXFrequency(int freq);
	
	void setSamplingRate(int rate);
	
}
