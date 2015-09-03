package org.radioberry.discovery;

import java.net.InetAddress;

public class RequestInfo {
	private InetAddress remoteAddress;
	private int remotePort;

	public InetAddress getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(InetAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}
}
