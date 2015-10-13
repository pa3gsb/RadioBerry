package org.radioberry.web;

import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.radioberry.service.RadioClients;

@WebSocket
public class RadioWebSocketHandler {

	Session session;

	@OnWebSocketClose
	public void onClose(int statusCode, String reason) {
		// System.out.println("Close: statusCode=" + statusCode + ", reason=" +
		// reason);

		RadioClients.getInstance().removeRadioClient(this);

		this.session = null;
	}

	@OnWebSocketError
	public void onError(Throwable t) {
		System.out.println("Error: " + t.getMessage());
	}

	@OnWebSocketConnect
	public void onConnect(Session session) {
		this.session = session;
		// System.out.println("Connect: " +
		// session.getRemoteAddress().getAddress());

		// Add to list of Radio Clients
		RadioClients.getInstance().addRadioClient(this);

	}

	@OnWebSocketMessage
	public void onMessage(String message) {
	}

	public void sendMessage(String message) {

		if (null == session) {
			return;
		}

		try {
			if (session.isOpen()) {
				this.session.getRemote().sendString(message);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
