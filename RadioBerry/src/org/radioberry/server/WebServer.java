package org.radioberry.server;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.radioberry.radio.Radio;
import org.radioberry.service.AudioStream;
import org.radioberry.service.Generator;
import org.radioberry.service.SpectrumStream;
import org.radioberry.utility.Log;
import org.radioberry.web.DSPControlServlet;
import org.radioberry.web.RadioControlServlet;
import org.radioberry.web.RadioWebSocketHandler;

public class WebServer {

	public static void main(String[] args) throws Exception {

		// System.getProperties().list(System.out);
		System.setProperty("java.library.path", System.getProperty("user.dir") + File.separator + "lib");

		//InetAddress address = InetAddress.getByName("192.168.2.8");
		InetAddress address = InetAddress.getByName("169.254.214.88");
		InetSocketAddress socketaddress = new InetSocketAddress(address, 8080);

		Server server = new Server(socketaddress);
		// Server server = new Server(8080);
		WebSocketHandler wsHandler = new WebSocketHandler() {
			@Override
			public void configure(WebSocketServletFactory factory) {
				factory.register(RadioWebSocketHandler.class);
			}
		};

		WebAppContext context = new WebAppContext();
		context.setDescriptor(context + File.separator + "WEB-INF" + File.separator + "web.xml");
		context.setResourceBase(".." + File.separator + "radioberry" + File.separator + "jetty-webapp" + File.separator + "src"
				+ File.separator + "main" + File.separator + "webapp");
		context.setContextPath("/*");
		context.setParentLoaderPriority(true);
		context.addServlet(new ServletHolder(new RadioControlServlet()), "/radioberry/control.do");
		context.addServlet(new ServletHolder(new DSPControlServlet()), "/radioberry/dspcontrol.do");

		Log.info("WebServer", "radio starting");

		// Start radio
		Radio.getInstance().start();
		
		//Radio.getInstance().getGenerator().start();
		Radio.getInstance().getRxData().start();
		
		// Start Audio Stream
		AudioStream as = new AudioStream();
		as.start();
		
		SpectrumStream ss = new SpectrumStream();
		
		HandlerList handlers = new HandlerList();
		handlers.addHandler(wsHandler);
		handlers.addHandler(context);
		server.setHandler(handlers);

		server.start();
		server.join();

	}
}
