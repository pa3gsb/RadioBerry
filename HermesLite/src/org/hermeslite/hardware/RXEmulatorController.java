package org.hermeslite.hardware;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import org.hermeslite.utility.RingBuffer;

public class RXEmulatorController implements Runnable, IRxHandler{

	private final static int RX_EMULATOR_PORT = 5100;

	private volatile Thread thread;
	InetSocketAddress socketaddress;
	private DatagramSocket socket = null;

	private byte request[] = new byte[60];
	
	private RingBuffer<Byte> rxIQBuffer;
	
	public RXEmulatorController(RingBuffer<Byte> rxIQBuffer) {
		this.rxIQBuffer = rxIQBuffer;
	}
	
	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();
		}
	}

	public void stop() {
		sendPacket(false);
		
		if (socket !=null){
			socket.close();
			socket = null;
		}
		thread = null;
	}
	
	@Override
	public void run() {
		initializeSocket();

		sendPacket(true);
		while (thread == Thread.currentThread()) {
			readPackets();
		}
	}

	private void initializeSocket() {
		try {
			if (socket == null) {
				
				socketaddress = new InetSocketAddress("169.254.214.88", 0);
				socket = new DatagramSocket(socketaddress);
				socket.setReuseAddress(true);
				socket.setSoTimeout(1);
			}
		} catch (SocketException se) {System.out.println("socket error");
		}
	}
	
	private void readPackets() {
		try {
			DatagramPacket datagram = new DatagramPacket(request, request.length);
			socket.receive(datagram);
			handlePacket(datagram);
		} catch (SocketException se) {;
		} catch (IOException ioe) {;
		}

	}
	
	private void handlePacket(DatagramPacket receivedDatagram) throws IOException {
		byte[] received = (byte[]) receivedDatagram.getData();
		
//		try {
//			rxIQBuffer.add(received);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//		for (int i = 0 ; i < received.length; i++){
//			try {
//				rxIQBuffer.add(received[i]);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		
	}
	
	private void sendPacket(boolean start) {
		byte[] response = getControlMessage(start);
		try {
			DatagramPacket datagram = new DatagramPacket(response, response.length, socketaddress.getAddress(), RX_EMULATOR_PORT);
			socket.send(datagram);
		} catch (SocketException se) {
			System.out.println("se exception");
		} catch (IOException ioe) {
			System.out.println("io exception");
		}
	}
	
	private byte[] getControlMessage(boolean start) {

		byte[] controlMessage = new byte[60];
		for (int i = 0; i < 60; i++) {
			controlMessage[i] = 0;
		}

		int i = 0;
		controlMessage[i++] = (byte) 0xFE;
		controlMessage[i++] = (byte) 0xEF;
		if (start)
			controlMessage[i++] = (byte) 0xFF;
		else
			controlMessage[i++] = (byte) 0x00;
		
		//set freq and sample rate and so  on!!
		
		return controlMessage;
	}
	
	@Override
	public RingBuffer<Byte> getReceiveIQStream() {
		return rxIQBuffer;
	}

	@Override
	public void setRXFrequency(int freq) {
		//sendPacket(true);
		
	}

	@Override
	public void setSamplingRate(int rate) {
		//sendPacket(true);
		
	}

}
