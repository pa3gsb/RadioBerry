package org.hermeslite.control;

import java.io.IOException;

import org.hermeslite.hardware.IRxHandler;
import org.hermeslite.hardware.RXEmulatorController;
import org.hermeslite.hardware.RxEmulatorHandler;
import org.hermeslite.hardware.RxSPIHandler;
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

	private RingBuffer<Byte> rxIQBuffer = new RingBuffer<Byte>(48000 * 6 * 8);  // create a big buffer...
//	private RingBuffer<Integer> rxIQBuffer = new RingBuffer<Integer>(96000 * 2);  
	private RingBuffer<Byte> txIQBuffer = new RingBuffer<Byte>(12288);
	
	//private RXEmulatorController rxHandler;
	private RxEmulatorHandler rxHandler;
	//private RxSPIHandler rxHandler;
	private Protocol protocol;


	public static void main(String[] args) {
		RadioControl rc = new RadioControl();
		rc.startRxHandler();
		rc.startProtocolHandler();
	}

	private void startRxHandler() {
//		rxHandler = new RXEmulatorController(rxIQBuffer);
		rxHandler = new RxEmulatorHandler(rxIQBuffer);
//		try {
//			rxHandler = new RxSPIHandler(rxIQBuffer);
//		} catch (IOException e) {
//			System.out.println("error");
//		}
		rxHandler.start();
	}
	
	private void startProtocolHandler() {
		protocol = new Protocol(rxHandler);
		protocol.start();
	}
	
}
