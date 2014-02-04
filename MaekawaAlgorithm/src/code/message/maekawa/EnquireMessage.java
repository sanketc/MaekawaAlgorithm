package code.message.maekawa;

import java.io.Serializable;

import code.client.resource.Clock;
import code.common.ConfigInfo;

@SuppressWarnings("serial")
public class EnquireMessage extends MaekawaMessage implements Serializable {

	public EnquireMessage(){
	}

	public EnquireMessage(ConfigInfo config, Clock clock) {
		super(config, clock);
	}
}