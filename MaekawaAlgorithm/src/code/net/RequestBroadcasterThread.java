package code.net;

import java.util.HashMap;

import code.client.resource.Clock;
import code.client.resource.MessageManager;
import code.common.ConfigInfo;
import code.common.Globals;
import code.data.Record;
import code.message.Message;
import code.message.MessageEnums;
import code.message.MessageFactory;

/**
 * Broadcasts request to all nodes including itself. 
 * @author Sanket Chandorkar
 */
public class RequestBroadcasterThread extends Thread {
	
	private Clock clock;

	private ConfigInfo config;

	private MessageFactory msgFactory;
	
	private int sequenceNumber;
	
	private MessageManager msgManager;
	
	private HashMap<String, Connection> connectionPoolTable;

	public RequestBroadcasterThread(ConfigInfo config, 
			Clock clock, int sequenceNumber, MessageManager msgManager,
			HashMap<String, Connection> connectionPoolTable) {
		this.config = config;
		this.clock = clock;
		this.sequenceNumber = sequenceNumber;
		this.msgManager = msgManager;
		this.connectionPoolTable = connectionPoolTable;
		this.msgFactory = new MessageFactory();
	}

	public void run() {
		
		try {
			
			/* Sleep */
			int delay = Globals.getRandomDelay();
			Globals.logMsg("SLEEP: " + delay);
			Thread.sleep(delay);
			
			/* Form request */
			Clock reqClock = clock.updateClock();
			Record record = new Record(config.getId(), sequenceNumber , config.getAddress());
			
			/* Send request to self */
			Message request = msgFactory.generateMessage(
					MessageEnums.M_REQUEST, config, reqClock, record);
			msgManager.addLastMessage(request);
				
			/* Send request to other process */
			for(ConfigInfo quorumClientConfig: Globals.getQuorumList()) {
				Connection con = connectionPoolTable.get(quorumClientConfig.getId());
				con.sendMessage(request);
			}
				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}