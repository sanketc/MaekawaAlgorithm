package code.message.maekawa;

import java.io.Serializable;

import code.client.resource.Clock;
import code.common.ConfigInfo;

@SuppressWarnings("serial")
public class ReplyMessage extends MaekawaMessage implements Serializable {

	private boolean grant;
	
	public ReplyMessage(){
	}

	public ReplyMessage(ConfigInfo config, Clock clock, boolean grant) {
		super(config, clock);
		this.grant = grant;
	}
	
	public boolean isGrant() {
		return grant;
	}
}