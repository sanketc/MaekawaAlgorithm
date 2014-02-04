package code.common;

import java.io.Serializable;

/**
 * Host Config information.
 * 
 * @author Sanket Chandorkar
 */
@SuppressWarnings("serial")
public class ConfigInfo implements Serializable {

	private String id;

	private String address;

	private int port;

	public ConfigInfo(String id, String address, int port) {
		this.id = id;
		this.address = address;
		this.port = port;
	}

	public String getId() {
		return id;
	}

	public String getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}
	
	public int getIntId() {
		return Integer.parseInt(id);
	}
	
	@Override
	public String toString() {
		return "[ ID= " + id + " | ADDRESS=" + address + " | PORT=" + port + " ]"; 
	}
}