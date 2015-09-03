package org.radioberry.service;

import org.radioberry.utility.RingBuffer;

public class RxIQData implements Runnable {

	
	private volatile Thread thread;
	
	
	private RingBuffer<Byte> rxIQBuffer;
	
	public RxIQData(RingBuffer<Byte> rxIQBuffer){
		this.rxIQBuffer = rxIQBuffer;
	}
	
	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.start();
		}
	}

	public void stop() {
		thread = null;
	}

	@Override
	public void run() {
		while (thread == Thread.currentThread()) {
			
			fifoBuffer.
			
		}
		
	}
	
}
