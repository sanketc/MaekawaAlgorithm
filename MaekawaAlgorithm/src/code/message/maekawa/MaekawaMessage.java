package code.message.maekawa;

import java.io.Serializable;

import code.client.resource.Clock;
import code.common.ConfigInfo;
import code.message.Message;

@SuppressWarnings("serial")
public abstract class MaekawaMessage extends Message implements Serializable {

	private Clock clock;

	public MaekawaMessage() {
	}

	public MaekawaMessage(ConfigInfo config, Clock clock) {
		super(config);
		this.clock = clock;
	}

	public Clock getClock() {
		return clock;
	}
}