package code.message;

import code.client.resource.Clock;
import code.common.ConfigInfo;
import code.data.Record;
import code.message.client.ClientRequestMessage;
import code.message.controller.ConnectMessage;
import code.message.controller.EndOfComputationMessage;
import code.message.controller.StartOfComputationMessage;
import code.message.controller.TerminateMessage;
import code.message.maekawa.EnquireMessage;
import code.message.maekawa.FailedMessage;
import code.message.maekawa.ReleaseMessage;
import code.message.maekawa.ReplyMessage;
import code.message.maekawa.RequestMessage;
import code.message.maekawa.YieldMessage;
import code.message.server.HealthMessage;
import code.message.server.ServerRequestMessage;

/**
 * Factory for message generation.
 *  
 * @author Sanket Chandorkar
 */
public class MessageFactory {

	/**
	 * Generates message
	 * @param type Message.enum
	 * @param config Message Sender config
	 * @param object Can be Record, String ,int or (clock, Record)
	 * @return
	 */
	public Message generateMessage(MessageEnums type, ConfigInfo config, Object ... object) {
		Record record = null;
		String msg = null;
		Clock clock = null;
		int count = 0;
		Boolean grant = false;
		
		if(object.length != 0) {
		
			if( object[0] instanceof Record ) {
				record = (Record) object[0];
			}

			if( object[0] instanceof String ) {
				msg = (String) object[0];
			}
	
			if( object[0] instanceof Integer ) {
				count = (Integer) object[0];
			}
			
			if( object[0] instanceof Clock ) {
				clock = (Clock) object[0];
				if( object.length == 2 && object[1] instanceof Record ) {
					record = (Record) object[1];
				} else if(object.length == 2 && object[1] instanceof Boolean ) {
					grant = (Boolean) object[1];
				}
			}
		}
		
		switch(type) {
			case CLIENT_WRITE: return new ClientRequestMessage(config, record);
			case SERVER_WRITE: return new ServerRequestMessage(config, record);
			case ERROR: return new ErrorMessage(config, msg);
			case ACK: return new AckMessage(config, record);
			case HEALTH:  return new HealthMessage(config);
			case M_REQUEST: return new RequestMessage(config, clock, record);
			case M_REPLY: return new ReplyMessage(config, clock, grant);
			case M_RELEASE: return new ReleaseMessage(config, clock);
			case M_ENQUIRE: return new EnquireMessage(config, clock);
			case M_YIELD: return new YieldMessage(config, clock);
			case M_FAILED: return new FailedMessage(config, clock);
			case C_START: return new StartOfComputationMessage(config, count/* itteration */);
			case C_END: return new EndOfComputationMessage(config, count);
			case C_TERMINATE: return new TerminateMessage(config);
			case C_CONNECT: return new ConnectMessage(config);
			default: return null;
		}
	}
	
}