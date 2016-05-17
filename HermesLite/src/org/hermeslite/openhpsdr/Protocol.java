package org.hermeslite.openhpsdr;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Date;

import org.hermeslite.hardware.IRxHandler;
import org.hermeslite.service.Configuration;

public class Protocol implements Runnable {
	
	
	class Reading implements Runnable {

		@Override
		public void run() {
			readPackets();
		}
		
	}
	
	
	private volatile Thread thread;
	private volatile Thread readingthread;

	private byte SYNC = 0x7F;
	int last_sequence_number = 0;
	int nrx = 1; // n Receivers

	private DatagramSocket socket = null;
	private InetAddress remoteAddress;
	private int remotePort;

	private byte rxbuffer[] = new byte[1032];

	private boolean running = false;

	private Configuration configuration = Configuration.getInstance();

	private CommandControl ccontrol = new CommandControl();

	private IRxHandler rxHandler;

	public Protocol(IRxHandler rxHandler) {
		this.rxHandler = rxHandler;
	}

	public void start() {
		System.out.println("start");
		
		if (readingthread== null){
			Reading r = new Reading();
			readingthread = new Thread(r);
			readingthread.start();
		}
		
		if (thread == null) {
			thread = new Thread(this);
			thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();
		}
		
		
	}

	public void stop() {

		System.out.println("stop");
		if (socket != null) {
			socket.close();
			socket = null;
		}
		readingthread = null;
		thread = null;
	}

	@Override
	public void run() {
		while (thread == Thread.currentThread()) {
			//readPackets();
			if (running) {
				sendPacket();
			}
		}
	}

	private void readPackets() {

		try {
			if (socket == null) {
				// InetAddress addr =
				// InetAddress.getByName(configuration.IP_HERMES);
				InetSocketAddress socketaddress = new InetSocketAddress(configuration.PORT_HERMES);
				socket = new DatagramSocket(socketaddress);
				socket.setReuseAddress(true);
				socket.setBroadcast(true);
				socket.setSoTimeout(100);
			}

			DatagramPacket datagram = new DatagramPacket(rxbuffer, rxbuffer.length);
			socket.receive(datagram);
			handlePacket(datagram);
		} catch (SocketException se) {
		} catch (IOException ioe) {
		}

	}

	private void handlePacket(DatagramPacket receivedDatagram) throws IOException {
		byte[] received = receivedDatagram.getData();

		if (received[2] == 2) {
			System.out.println(" Discovery packet received ");
			this.remoteAddress = receivedDatagram.getAddress();
			this.remotePort = receivedDatagram.getPort();
			System.out.println(" Discovery Remote Address " + this.remoteAddress + " Remote Port " + this.remotePort);
			byte[] response = getDiscoveryReplyMessage();
			DatagramPacket datagram = new DatagramPacket(response, response.length, this.remoteAddress, this.remotePort);
			socket.send(datagram);
			System.out.println(" Discovery reply packet sent ");

			return;

		} else if (received[2] == 4) {
			if (received[3] == 1 || received[3] == 3) {
				running = true;
				this.remotePort = receivedDatagram.getPort();
				System.out.println(" Data Remote Address " + this.remoteAddress + " Remote Port " + this.remotePort);
				System.out.println(" SDR Program sends Start command ");
				return;
			} else {
				last_sequence_number = 0;
				running = false;
				System.out.println("  SDR Program sends Stop command ");
				return;
			}
		}

		if (isValidFrame(received)) {
			// packet contains 2 hpsdr frames; so 2 c&c's
			byte[] cc = new byte[5];
			System.arraycopy(received, 11, cc, 0, 5); // index 11 .... 15
			ccontrol.CommandAndControl(cc);
			System.arraycopy(received, 523, cc, 0, 5); // index 523 .... 531 (8
														// + 512 + 3 sync)
			ccontrol.CommandAndControl(cc);

			nrx = ccontrol.getNrOfReceivers();

			if (ccontrol.isControlDataChanged()) {
				rxHandler.setRXFrequency(ccontrol.getRXFrequency());
				System.out.println("Number of receivers " + nrx);
			}

			// lees data en vul buffers
			for (int frame = 0; frame < 2; frame++) {
				int coarse_pointer = frame * 512 + 8;

				for (int j = 8; j < 512; j += 8) {
					int k = coarse_pointer + j;

					// M
					// No codec.. skip
					// TX IQ
					// No tx yet

				}
			}
		} else {
			System.out.println(" Invalid frame ");
		}

	}

	private boolean isValidFrame(byte[] data) {

		return (data[8] == SYNC && data[9] == SYNC && data[10] == SYNC && data[520] == SYNC && data[521] == SYNC && data[522] == SYNC);
	}

	private byte[] getDiscoveryReplyMessage() {

		byte[] broadcastReply = new byte[60];
		for (int i = 0; i < 60; i++) {
			broadcastReply[i] = 0;
		}

		int i = 0;
		broadcastReply[i++] = (byte) 0xEF;
		broadcastReply[i++] = (byte) 0xFE;
		broadcastReply[i++] = (byte) 0x02;

		broadcastReply[i++] = (byte) 0x00; // MAC
		broadcastReply[i++] = (byte) 0x01;
		broadcastReply[i++] = (byte) 0x02;
		broadcastReply[i++] = (byte) 0x03;
		broadcastReply[i++] = (byte) 0x04;
		broadcastReply[i++] = (byte) 0x05;
		broadcastReply[i++] = (byte) 29;
		broadcastReply[i++] = (byte) 1; // Hermes boardtype public static final
										// int DEVICE_HERMES_LITE = 6;

		return broadcastReply;
	}

	private DatagramPacket rxdatagram;

	private void sendPacket() {

		long start = System.nanoTime();
		byte[] hpsdrdata = getPacketToSend();

		try {
			if (rxdatagram == null)
				rxdatagram = new DatagramPacket(hpsdrdata, hpsdrdata.length, this.remoteAddress, this.remotePort);
			else {
				rxdatagram.setData(hpsdrdata);
				rxdatagram.setLength(hpsdrdata.length);
				rxdatagram.setAddress(this.remoteAddress);
				rxdatagram.setPort(this.remotePort);
			}

			socket.setReuseAddress(true);
			socket.send(rxdatagram);

			double time = (System.nanoTime() - start) / 100000d;
			// if (time > 50.0)
			// System.out.println("samen stellen packet " + time + " msec");

		} catch (SocketException se) {
			System.out.println("se exception");
		} catch (IOException ioe) {
			System.out.println("io exception");
		}
	}

	private byte[] getPacketToSend() {
		byte[] hpsdrdata = new byte[1032];
		hpsdrdata[0] = (byte) 0xEF;
		hpsdrdata[1] = (byte) 0xFE;
		hpsdrdata[2] = (byte) 0x01;
		hpsdrdata[3] = (byte) 0x06;
		hpsdrdata[4] = (byte) ((last_sequence_number >> 24) & 0xFF);
		hpsdrdata[5] = (byte) ((last_sequence_number >> 16) & 0xFF);
		hpsdrdata[6] = (byte) ((last_sequence_number >> 8) & 0xFF);
		hpsdrdata[7] = (byte) (last_sequence_number & 0xFF);
		last_sequence_number++;

		int factor = (nrx - 1) * 6;
		int index;
		for (int frame = 0; frame < 2; frame++) {
			int coarse_pointer = frame * 512; // 512 bytes total in each frame
			hpsdrdata[8 + coarse_pointer] = SYNC;
			hpsdrdata[9 + coarse_pointer] = SYNC;
			hpsdrdata[10 + coarse_pointer] = SYNC;
			hpsdrdata[11 + coarse_pointer] = (byte) 0x00; // c0
			hpsdrdata[12 + coarse_pointer] = (byte) 0x00; // c1
			hpsdrdata[13 + coarse_pointer] = (byte) 0x00; // c2
			hpsdrdata[14 + coarse_pointer] = (byte) 0x00; // c3
			hpsdrdata[15 + coarse_pointer] = (byte) 0x1D; // c4 //v2.9

			for (int j = 0; j < (504 / (8 + factor)); j++) {
				index = 16 + coarse_pointer + (j * (8 + factor));

				// Only (for now) supporting receiving mode....

				try {
					hpsdrdata[index + 0] = rxHandler.getReceiveIQStream().remove(); // MSB;
					// comes
					// first!!!!
					hpsdrdata[index + 1] = rxHandler.getReceiveIQStream().remove();
					hpsdrdata[index + 2] = rxHandler.getReceiveIQStream().remove();
					// Q
					hpsdrdata[index + 3] = rxHandler.getReceiveIQStream().remove(); // MSB;
					// comes
					// first!!!!
					hpsdrdata[index + 4] = rxHandler.getReceiveIQStream().remove();
					hpsdrdata[index + 5] = rxHandler.getReceiveIQStream().remove();

					// Byte[] remove = rxHandler.getReceiveIQStream().remove(6);
					//
					// hpsdrdata[index + 0] = remove[0]; // I MSB first;
					// hpsdrdata[index + 1] = remove[1];
					// hpsdrdata[index + 2] = remove[2];
					// hpsdrdata[index + 3] = remove[3];// Q MSB first;
					// hpsdrdata[index + 4] = remove[4];
					// hpsdrdata[index + 5] = remove[5];

					// Integer i, q;
					// i = rxHandler.getReceiveIQStream().remove();
					// q = rxHandler.getReceiveIQStream().remove();
					// hpsdrdata[index + 0] = (byte) ((i >> 16) & 0xff);
					// hpsdrdata[index + 1] = (byte) ((i >> 8) & 0xff);
					// hpsdrdata[index + 2] = (byte) ((i ) & 0xff);
					// hpsdrdata[index + 3] = (byte) ((q >> 16) & 0xff);
					// hpsdrdata[index + 4] = (byte) ((q >> 8) & 0xff);
					// hpsdrdata[index + 5] = (byte) ((q ) & 0xff);

				} catch (InterruptedException e) {
					System.out.println("remove exception");
				}
			}
		}

		return hpsdrdata;
	}
}
