package code.message.maekawa;

import java.io.Serializable;

import code.client.resource.Clock;
import code.common.ConfigInfo;

@SuppressWarnings("serial")
public class YieldMessage extends MaekawaMessage implements Serializable {

	public YieldMessage(){
	}

	public YieldMessage(ConfigInfo config, Clock clock) {
		super(config, clock);
	}
}