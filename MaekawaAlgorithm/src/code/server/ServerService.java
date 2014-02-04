package code.server;

import java.util.HashMap;

import code.client.resource.MessageManager;
import code.common.ConfigInfo;
import code.common.Globals;
import code.data.FileManager;
import code.message.Message;
import code.message.MessageEnums;
import code.message.MessageFactory;
import code.message.client.ClientRequestMessage;
import code.message.controller.EndOfComputationMessage;
import code.message.controller.TerminateMessage;
import code.message.server.ServerRequestMessage;
import code.net.Connection;

/**
 * Server request handler.
 * 
 * @author Sanket Chandorkar
 */
public class ServerService extends Thread {

	private FileManager fm;

	private MessageFactory factory;

	private ConfigInfo config;

	private MessageManager msgManager;
	
	HashMap<String, Connection> connectionPoolTable;
	
	private int eocReceivedCount = 0;
	
	private int totalMsgsCount = 0;
	
	public ServerService(ConfigInfo config, MessageManager msgManager, 
			HashMap<String, Connection> connectionPoolTable) {
		this.fm = new FileManager(config.getId());
		this.factory = new MessageFactory();
		this.config = config;
		this.msgManager = msgManager;
		this.connectionPoolTable = connectionPoolTable;
	}
	
	@Override
	public void run() {
		Message message;
		while(true){
			try {
				
				// check if msg is there
				synchronized (msgManager) {
					while(msgManager.isEmptyQueue()) {
						msgManager.wait();
					}
					message = msgManager.getNextMessage();
				}
				handleMessage(message);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void handleMessage(Message message) throws Exception {

		try {
			if(message instanceof TerminateMessage) {
				if(msgManager.isEmptyQueue()) {
					System.out.println(" -> Received TERMIATE : " + message.getConfig());
					System.out.println("Exiting program normally !!");
					Globals.logMsg("Exiting program normally !!");
					System.exit(Globals.SYS_SUCCESS);
				} else {
					Thread.sleep(20);
					msgManager.addLastMessage((TerminateMessage)message);
				}
			} else if(message instanceof EndOfComputationMessage) {
				System.out.println(" -> Received EOC : " + ((EndOfComputationMessage)message).getConfig());
				eocReceivedCount++;
				totalMsgsCount += ((EndOfComputationMessage)message).getMessageCount();
				if(eocReceivedCount == Globals.getClientList().size()) {
					Globals.logMsg(" >>>>>>>>> Total message count = " + totalMsgsCount);
					Message ter = factory.generateMessage(MessageEnums.C_TERMINATE, config);
					for(ConfigInfo cliConfig: Globals.getClientList()) {
						Connection con1 = connectionPoolTable.get(cliConfig.getId());
						try {
							con1.sendMessage(ter);
						} catch (Exception e) {	}
					}
					for(ConfigInfo serConfig: Globals.getServerList()) {
						Connection con1 = connectionPoolTable.get(serConfig.getId());
						try {
							con1.sendMessage(ter);
						} catch (Exception e) {	}
					}
					msgManager.addLastMessage(ter);
				}
				
			} else if(message instanceof ClientRequestMessage) {
				ClientRequestMessage cliReqMessage = ((ClientRequestMessage)message);
				System.out.println(" -> Write : " + cliReqMessage.getRecord() );
				fm.appendRecord(cliReqMessage.getRecord());
				for(ConfigInfo serverConfig: Globals.getServerList()) {
					Connection con = connectionPoolTable.get(serverConfig.getId());
					con.sendMessage(factory.generateMessage(MessageEnums.SERVER_WRITE, config, cliReqMessage.getRecord()));
				}			
			} else if(message instanceof ServerRequestMessage) {
				ServerRequestMessage serReqMessage = ((ServerRequestMessage)message); 
				System.out.println(" -> Write : " + serReqMessage.getRecord() );
				fm.appendRecord(serReqMessage.getRecord());
			} else {
				System.out.println(" Unknown Message : "  + message.getClass().getSimpleName());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}