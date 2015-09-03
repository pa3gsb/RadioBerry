package org.radioberry.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.radioberry.utility.Log;

public class Response {

	private ResponseInfo responseInfo;

	public Response(ResponseInfo responseInfo) {
		this.responseInfo = responseInfo;
	}

	public void sendResponse()  {

		Log.info("Response", "sendresponse");

		send(getBroadcastReplyMessage());

	}

	private byte[] getBroadcastReplyMessage() {

		byte[] broadcastReply = new byte[63];
		for (int i = 0; i < 63; i++) {
			broadcastReply[i] = 0;
		}

		byte[] mac = this.getMAC(responseInfo.getLocalAddress());
		int i = 0;
		broadcastReply[i++] = (byte) 0xEF;
		broadcastReply[i++] = (byte) 0xFE;
		broadcastReply[i++] = (byte) 0x02;
		broadcastReply[i++] = mac[0];
		broadcastReply[i++] = mac[1];
		broadcastReply[i++] = mac[2];
		broadcastReply[i++] = mac[3];
		broadcastReply[i++] = mac[4];
		broadcastReply[i++] = mac[5];
		broadcastReply[i++] = (byte) 100;
		broadcastReply[i++] = (byte) 1; // Hermes boardtype public static final int DEVICE_HERMES_LITE = 6;

		return broadcastReply;
	}

	private void send(byte buffer[]) {
		 DatagramSocket socket;
		 DatagramPacket datagram;
		
		try {
			InetAddress address = this.responseInfo.getRequestInfo().getRemoteAddress();
			InetAddress socketaddress = this.responseInfo.getLocalAddress();
			InetSocketAddress socketaddr = new InetSocketAddress(socketaddress, this.responseInfo.getLocalPort());
			datagram = new DatagramPacket(buffer, buffer.length, address, this.responseInfo.getRequestInfo().getRemotePort());
			socket = new DatagramSocket(socketaddr);
			socket.setReuseAddress(false);
			socket.send(datagram);
		} catch (SocketException se) {
		} catch (UnknownHostException uhe) {
		} catch (IOException ioe) {
		}

	}

	private byte[] getMAC(InetAddress ip) {

		byte[] mac = {};
		
		try {
			NetworkInterface network = NetworkInterface.getByInetAddress(ip);

			mac = network.getHardwareAddress();

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
			}
			Log.info("Response: ", "MAC Broadcast Response" + sb.toString());
		} catch (SocketException se) {
		}

		return mac;
	}
}
