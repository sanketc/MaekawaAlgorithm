package code.message.server;

import java.io.Serializable;

import code.common.ConfigInfo;
import code.message.Message;

@SuppressWarnings("serial")
public class HealthMessage extends Message implements Serializable {

	public HealthMessage() {
	}

	public HealthMessage(ConfigInfo config) {
		super(config);
	}
}