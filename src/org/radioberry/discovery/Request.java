package org.radioberry.discovery;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.radioberry.service.Configuration;
import org.radioberry.utility.Log;

/**
 * 
 * (Discovery) Request comming from a SDR program.
 * 
 * From the request the IP address and the port is retrieved and placed in the requestInfo.
 * 
 * 
 * @author PA3GSB
 *
 */
public class Request implements Runnable{

	private volatile Thread thread;

	private byte rxbuffer[] = new byte[1024];
	private DatagramPacket rxdatagram;
	private DatagramSocket socket;
	private RequestInfo requestInfo;
	private IRequest requestHandler;

	public Request(IRequest requestHandler) {
		this.requestHandler = requestHandler;
	}

	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.start();
		}
	}

	public void stop() {
		if (socket != null) {
			socket.close();
			socket = null;
		}
		thread = null;
	}
	
	
	@Override
	public void run() {
		while (thread == Thread.currentThread()) {
			if (handleReceivingSearchRequest()) {
				requestHandler.postRequest();
			}
			;
		}
		
	}
	
	private boolean handleReceivingSearchRequest() {

		try {

			if (socket == null) {
				InetSocketAddress socketaddress = new InetSocketAddress(Configuration.PORT);
				socket = new DatagramSocket(socketaddress);
				socket.setReuseAddress(true);
				socket.setBroadcast(true);
				socket.setSoTimeout(2000);
			}

			rxdatagram = new DatagramPacket(rxbuffer, rxbuffer.length);

			socket.receive(rxdatagram);

			if ((rxbuffer[0] & 0xFF) == 0xEF && (rxbuffer[1] & 0xFF) == 0xFE && (rxbuffer[2] & 0xFF) == 0x02) {

				requestInfo = new RequestInfo();
				requestInfo.setRemoteAddress(rxdatagram.getAddress());
				requestInfo.setRemotePort(rxdatagram.getPort());
				requestHandler.requestInfo(requestInfo);
				
				Log.info("Request from: ", rxdatagram.getAddress().getHostAddress() + "  port: " + rxdatagram.getPort());

				return true;
			}
		} catch (SocketTimeoutException ste) {
			Log.info("Request", "time out");
		} catch (SocketException se) {
			Log.info("Request", "socket exception ");
		} catch (Exception e) {
			Log.info("Request", "exception");
		}

		return false;
	}
	

}
