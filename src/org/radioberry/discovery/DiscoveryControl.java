package org.radioberry.discovery;

/**
 * Discovery control handling.
 * 
 * 
 * @author PA3GSB
 *
 */
public class DiscoveryControl implements IRequest {

	private Request request;
	private RequestInfo requestInfo;

	private ResponseInfo responseInfo;

	public void startDiscovering() {
		request = new Request(this);
		request.start();
	}

	@Override
	public void requestInfo(RequestInfo reqInfo) {
		this.requestInfo = reqInfo;
	}

	@Override
	public void postRequest() {
		// Request is received; now we have to determine from which local IP
		// address we need to send the response.
		responseInfo = new ResponseInfo(requestInfo);
		responseInfo.determineResponseAddress();

		// If the local IP address could not be found the broadcast could come
		// from different subnet; so we need to see if the call comes from the
		// same subnet.
		if (null != responseInfo.getLocalAddress()) {
			request.stop();
			request = null;
			Response response = new Response(responseInfo);
			response.sendResponse();
		}
	}
}
