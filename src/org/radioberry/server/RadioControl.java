package org.radioberry.server;

import org.radioberry.discovery.DiscoveryControl;
import org.radioberry.utility.RingBuffer;

public class RadioControl  {

	private RingBuffer<Byte> rxIQBuffer = new RingBuffer<Byte>(4800);
	
	public static void main(String[] args) {
		RadioControl rc = new RadioControl();
		rc.discovery();
	}

	private void discovery() {
			DiscoveryControl control = new DiscoveryControl();
			control.startDiscovering();
	}
	
}
