package org.radioberry.service;


public class AudioStream implements Runnable {

	private volatile Thread thread;

	
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
			RadioClients.getInstance().stream();
		}
	}
}
