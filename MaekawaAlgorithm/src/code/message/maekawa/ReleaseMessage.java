package code.message.maekawa;

import java.io.Serializable;

import code.client.resource.Clock;
import code.common.ConfigInfo;

@SuppressWarnings("serial")
public class ReleaseMessage extends MaekawaMessage implements Serializable {

	public ReleaseMessage(){
	}

	public ReleaseMessage(ConfigInfo config, Clock clock) {
		super(config, clock);
	}
}