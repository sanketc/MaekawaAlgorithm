package code.message.maekawa;

import java.io.Serializable;

import code.client.resource.Clock;
import code.common.ConfigInfo;

@SuppressWarnings("serial")
public class FailedMessage extends MaekawaMessage implements Serializable {

	public FailedMessage(){
	}

	public FailedMessage(ConfigInfo config, Clock clock) {
		super(config, clock);
	}
}