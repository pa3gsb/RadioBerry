package org.hermeslite.control;

import org.hermeslite.hardware.RxEmulatorHandler;
import org.hermeslite.openhpsdr.Protocol;
import org.hermeslite.utility.RingBuffer;
/**
 * 
 * Radio Control
 * 
 * @author PA3GSB
 *
 */
public class RadioControl  {

	private RingBuffer<Byte> rxIQBuffer = new RingBuffer<Byte>(48000 * 6 * 2 * 2);  // create a big buffer...
	private RingBuffer<Byte> txIQBuffer = new RingBuffer<Byte>(12288);
	
	private RxEmulatorHandler rxHandler;
	private Protocol protocol;
	
	public static void main(String[] args) {
		RadioControl rc = new RadioControl();
		rc.startRxHandler();
		rc.startProtocolHandler();
	}

	private void startRxHandler() {
		rxHandler = new RxEmulatorHandler(rxIQBuffer);
		rxHandler.start();
	}
	
	private void startProtocolHandler() {
		protocol = new Protocol(rxHandler);
		protocol.start();
	}
	
}
