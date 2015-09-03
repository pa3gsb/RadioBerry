package org.radioberry.discovery;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.radioberry.utility.Log;

public class ResponseInfo {
	private InetAddress localAddress;
	private int localPort = 1024;		// default port
	
	boolean isWireless = false;

	private RequestInfo requestInfo;
	
	public ResponseInfo(RequestInfo requestinfo){
		this.requestInfo = requestinfo;
	}
	
	public int getLocalPort() {
		return localPort;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	public InetAddress getLocalAddress() {
		return localAddress;
	}

	public void setLocalAddress(InetAddress localAddress) {
		this.localAddress = localAddress;
	}
	
	//based on the request we determine the response address.
	public void determineResponseAddress() {

		String result = "";

		try {

			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						result = inetAddress.getHostAddress().toString();
						if (result.contains(".")) {
							Log.info("ResponseInfo", "Checking interface...: " + intf.toString() + " " + result);
							NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress.getLocalHost());
							short networkPrefixLength = networkInterface.getInterfaceAddresses().get(0).getNetworkPrefixLength();
							String subnet = inetAddress.getHostAddress() + "/" + Integer.valueOf(networkPrefixLength).toString();
							SubnetUtils utils = new SubnetUtils(subnet);
							SubnetInfo info = utils.getInfo();
							Log.info("ResponseInfo", "interface subnet mask: " + info.getNetmask());
							if (sameNetwork(requestInfo.getRemoteAddress(), inetAddress, info.getNetmask())) {
								Log.info("ResponseInfo", "interface on same subnet " + intf.toString());
								boolean lIsWireless = (intf.getName().contains("wlan"))? true : false;   //;"Wireless".contains(intf.toString()) ? true : false;
								if (localAddress == null) {
									// If no local address found yet; we will use this one.
									localAddress = inetAddress;
									isWireless = lIsWireless;
								} else if (isWireless && !lIsWireless) {
									// If local address already found but a better one is found (preferable using the ethernet cable)
									localAddress = inetAddress;
									isWireless = lIsWireless;
								}

							} else {
								Log.info("ResponseInfo", "Not used; interface on different subnet " + intf.toString());
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			Log.info("ResponseInfo", "Exception in method :" + ex.toString());
		}

	}

	public boolean sameNetwork(InetAddress ip1, InetAddress ip2, String mask) throws Exception {

		byte[] a1 = ip1.getAddress();
		byte[] a2 = ip2.getAddress();
		byte[] m = InetAddress.getByName(mask).getAddress();

		for (int i = 0; i < a1.length; i++)
			if ((a1[i] & m[i]) != (a2[i] & m[i]))
				return false;

		return true;

	}

	public RequestInfo getRequestInfo() {
		return requestInfo;
	}
	
	
}
