package org.hermeslite.service;


public class Configuration {

	// public final  String IP_HERMES = "192.168.2.1";
    public final  String IP_HERMES = "169.254.214.88";
    public final  int PORT_HERMES = 1024;
	
	public static Configuration getInstance() {
		if(instance==null) {
			instance=new Configuration();
		}
		return instance;
	}

	private Configuration() {
	}

    private static Configuration instance;
	
}
