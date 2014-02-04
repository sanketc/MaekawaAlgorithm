package code.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import code.client.resource.Clock;
import code.client.resource.MessageManager;
import code.client.util.RequestComparator;
import code.common.ConfigInfo;
import code.common.Globals;
import code.data.Record;
import code.message.AckMessage;
import code.message.ErrorMessage;
import code.message.Message;
import code.message.MessageEnums;
import code.message.MessageFactory;
import code.message.controller.StartOfComputationMessage;
import code.message.controller.TerminateMessage;
import code.message.maekawa.EnquireMessage;
import code.message.maekawa.FailedMessage;
import code.message.maekawa.MaekawaMessage;
import code.message.maekawa.ReleaseMessage;
import code.message.maekawa.ReplyMessage;
import code.message.maekawa.RequestMessage;
import code.message.maekawa.YieldMessage;
import code.net.Connection;
import code.net.RequestBroadcasterThread;

/**
 * Services all types of messages.
 * 
 * @author Sanket Chandorkar
 */
public class ClientService extends Thread {
	
	private PriorityQueue<RequestMessage> requestQueue;
	
	private ConfigInfo config;
	
	private Clock clock;
	
	private MessageManager msgManager;
	
	private MessageFactory factory;
	
	private HashMap<String, Connection> connectionPoolTable;
	
	private RequestMessage lastRequest = null;
	
	private int sequenceNumber = 1;
	
	private int replyCount = 0;
	
	private final int quorumSize = Globals.getQuorumList().size() + 1;
	
	private boolean hasGotFailedMessage = false;
	
	private boolean hasGotGrant = false;
	
	private boolean yieldSent = false;
	
	private ArrayList<EnquireMessage> unhandledEnquireMsgList = null;
	
	private long startTime = 0;
	
	private long endTime = 0;
	
	private ConfigInfo socConfigServer = null;
	
	private int noOfReplyReceived = 0, noOfRequestReceived = 0,
			noOfYieldReceiced = 0 , noOfInquiryReceived = 0,
			noOfReleaseReceived = 0, noOfFailedReceived = 0;

	private int noOfReplySent = 0, noOfRequestSent = 0,
			noOfYieldSent = 0 , noOfInquirySent = 0,
			noOfReleaseSent = 0, noOfFailedSent = 0;
	
	public ClientService(ConfigInfo config, Clock clock, MessageManager msgManager,
			HashMap<String, Connection> connectionPoolTable) {
		this.requestQueue = new PriorityQueue<RequestMessage>(30, new RequestComparator());
		this.config = config;
		this.clock = clock;
		this.msgManager = msgManager;
		this.factory = new MessageFactory();
		this.connectionPoolTable = connectionPoolTable;
		this.unhandledEnquireMsgList = new ArrayList<EnquireMessage>();
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
				
				// process msg
				Globals.logMsg("PROCESSING MESSAGE: " + message.getClass().getSimpleName()
						 + " | " + message.getConfig());
				logQueue();
				handleMessage(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void handleMessage(Message message) throws Exception {
		
		if(message instanceof MaekawaMessage) {
			MaekawaMessage maekawaMessage = (MaekawaMessage) message;
			/* update clock */
			clock.updateClock(maekawaMessage.getClock());
		}
		
		if(message instanceof StartOfComputationMessage) {
			handleSOCMessage((StartOfComputationMessage) message);
		} else if(message instanceof TerminateMessage) {
			System.out.println("Program Exited Normally !!");
			Globals.logMsg("Program Exited Normally !!");
			System.exit(Globals.SYS_SUCCESS);
		} else if(message instanceof RequestMessage) {
			noOfRequestReceived++;
			handleRequestMessage((RequestMessage) message);
		} else if(message instanceof ReplyMessage) {
			noOfReplyReceived++;
			handleReplyMessage((ReplyMessage) message);
		} else if(message instanceof ReleaseMessage) {
			noOfReleaseReceived++;
			handleReleaseMessage((ReleaseMessage) message);
			noOfReleaseReceived++;
		} else if(message instanceof YieldMessage) {
			noOfYieldReceiced++;
			handleYieldMessage((YieldMessage) message);
		} else if(message instanceof EnquireMessage) {
			noOfInquiryReceived++;
			handleEnquireMessage((EnquireMessage) message);
		} else if(message instanceof FailedMessage) {
			noOfFailedReceived++;
			handleFailedMessage((FailedMessage) message);
		} else if (message instanceof ErrorMessage){
			System.out.println(" -> Error while writing: " + ((ErrorMessage)message).getMsg());
			Globals.logMsg(" -> Error while writing: " + ((ErrorMessage)message).getMsg());
		} else if(message instanceof AckMessage) {
			System.out.println(" -> Success while writing: " + ((AckMessage)message).getRecord());
			Globals.logMsg(" -> Success while writing: " + ((AckMessage)message).getRecord());
		} else {
			System.out.println("Unknown message received !!" + message.getClass().getSimpleName());
		}
	}
	
	private int printSummary() {
		
		Globals.logMsg("\n\n **************** Summary ******************* \n");
		
		Globals.logMsg("noOfRequestSent = " + noOfRequestSent);
		Globals.logMsg("noOfRequestReceived = " + noOfRequestReceived);
		
		Globals.logMsg("noOfReplySent = " + noOfReplySent);
		Globals.logMsg("noOfReplyReceived = " + noOfReplyReceived);
		
		Globals.logMsg("noOfReleaseSent = " + noOfReleaseSent);
		Globals.logMsg("noOfReleaseReceived = " + noOfReleaseReceived);
		
		Globals.logMsg("noOfInquirySent = " + noOfInquirySent);
		Globals.logMsg("noOfInquiryReceived = " + noOfInquiryReceived);
		
		Globals.logMsg("noOfYieldSent = " + noOfYieldSent);
		Globals.logMsg("noOfYieldReceiced = " + noOfYieldReceiced);

		Globals.logMsg("noOfFailedSent = " + noOfFailedSent);
		Globals.logMsg("noOfFailedReceived = " + noOfFailedReceived);
		
		int total = noOfFailedReceived + noOfInquiryReceived + 
				noOfReleaseReceived + noOfReplyReceived + 
				noOfRequestReceived + noOfYieldReceiced +
				
				noOfFailedSent + noOfInquirySent +
				noOfReleaseSent + noOfReplySent + 
				noOfRequestSent + noOfYieldSent;
		Globals.logMsg("TOTAL MESSAGE = " + total);
		Globals.logMsg("\n\n ******************************************* \n");
		return total;
	}

	private void handleSOCMessage(StartOfComputationMessage message) {
		System.out.println("Got StartOfComputationMessagefrom !!" + message.getConfig());
		Globals.MAX_WRITE_OPERATION_COUNT = message.getMaxItteration();
		socConfigServer = message.getConfig();
		broadCastRequestMessage();
	}

	private void handleYieldMessage(YieldMessage message) throws Exception {
		System.out.println("Got YieldMessage from: " + message.getConfig());
		
		/* Send reply to next process in request queue */
//		RequestMessage lastPrevRequest = lastRequest; // TODO: debug
		lastRequest = null;
		
		/* Process queue for change of state */
		if(requestQueue.isEmpty()) {
			debug("FATAL: Illegal state !! ");
			return;
		}

		/* Dequeue or read queue */
		lastRequest = requestQueue.peek();
		
//		if( lastPrevRequest == lastRequest ) {	// TODO: debug
////			debug(" NOTE: Chain condition !! ");
//		}
		
		/* Send reply(GRANT) message to self */
		Message replyMsg = factory.generateMessage(MessageEnums.M_REPLY, config, clock.updateClock(), true /* grant */ );
		sendLastMessageWrapper(replyMsg, lastRequest.getConfig().getId());
		noOfReplySent++; // log
	}

	private void handleFailedMessage(FailedMessage message) throws Exception {
		System.out.println("Got FailedMessage from: " + message.getConfig());
		
		/* Update failed status */
		hasGotFailedMessage = true;
		
		//TODO: test this unhandled yield messages
		processUnhandledEnquireMessage();
	}
	
	private void processUnhandledEnquireMessage() throws Exception {
		for(EnquireMessage enqMsg: unhandledEnquireMsgList){
			handleEnquireMessage(enqMsg);
		}
		unhandledEnquireMsgList = new ArrayList<EnquireMessage>();
	}
	
	private void handleEnquireMessage(EnquireMessage message) throws Exception {
		System.out.println("Got EnquireMessage from: " + message.getConfig());
		if(hasGotFailedMessage || (yieldSent && !hasGotGrant) ) {
			processEnquiryMessage(message);
		} else {
			// save for future failed message
			unhandledEnquireMsgList.add(message);
		}
	}
	
	private void processEnquiryMessage(EnquireMessage enqMsg) throws Exception {
		/* send yield message */
		Message yieldMessage = factory.generateMessage(MessageEnums.M_YIELD, config, clock.updateClock());
		
		if(enqMsg.getConfig().getId().equals(config.getId())){
			// TODO
			debug("Self enquiry");
		} else {
			// decrement reply count
			replyCount--;  // TODO: BOOKMARK: OK
		}
		
		// send yield
		sendLastMessageWrapper(yieldMessage, enqMsg.getConfig().getId());
		noOfYieldSent++;  // log
		yieldSent = true;
	}
	
	private void handleRequestMessage(RequestMessage message) throws Exception {
		System.out.println("Got RequestMessage from: " + message.getRecord());
		
		/* Enqueue request */
		requestQueue.add(message);

		/* Process queue for change of state */
		processRequestQueue(message);
	}

	private void handleReplyMessage(ReplyMessage message) throws Exception {
		System.out.println("Got ReplyMessage from: " + message.getConfig());
		
		if(message.isGrant()) {
			/* Update grant received from one of the processes status to true */
			hasGotGrant = true;	// TODO: BOOKMARK OK
		}
		
		/* Increment reply message count */
		replyCount++;
		
		/* Check CS condition */
		allReplyAchieved();
	}

	// --------------------------------------

	private void handleReleaseMessage(ReleaseMessage message) throws Exception {
		System.out.println("Got ReleaseMessage from: " + message.getConfig());
		
		/* Update queue and last request variable */
		requestQueue.remove(lastRequest);
		lastRequest = null;

		/* Process queue for change of state */
		if(requestQueue.isEmpty())
			return;

		/* Update failed status */
		hasGotFailedMessage = false;
		hasGotGrant = false;
		yieldSent = false;
		unhandledEnquireMsgList = new ArrayList<EnquireMessage>();

		/* Dequeue or read queue */
		lastRequest = requestQueue.peek();
		
		/* Send reply message to next */
		Message replyMsg = factory.generateMessage(MessageEnums.M_REPLY, config, clock.updateClock(), false);
		sendFirstMessageWrapper(replyMsg, lastRequest.getConfig().getId());
		noOfReplySent++; // log
	}

	
	public void processRequestQueue(RequestMessage requestMsg) throws Exception {
		/* Process queue for change of state */
		if(requestQueue.isEmpty())
			return;
		
		if(lastRequest == null) {
			/* Dequeue or read queue */
			lastRequest = requestQueue.peek();
			/* Send reply message to self */
			Message replyMsg = factory.generateMessage(MessageEnums.M_REPLY, config, clock.updateClock(), false);
			sendFirstMessageWrapper(replyMsg, lastRequest.getConfig().getId());
			noOfReplySent++; // log
		} else {
			
			// safety check
			if(requestMsg.getConfig().getId().equals(config.getId())) {
				// TODO: BOOKMARK check OK
				return;
			}
			
			/* This means reply has been sent: now check for deadlock messages */
			int compareValue = requestMsg.compareValues(lastRequest);
			Message replyMsg = null;
			Globals.logMsg("Value requestMsg= " + requestMsg.getConfig());
			logQueue(compareValue); // TODO
			
			if( compareValue == 1 ) { 
				/* Send Enquire */
				replyMsg = factory.generateMessage(MessageEnums.M_ENQUIRE, config, clock.updateClock());
				sendLastMessageWrapper(replyMsg, lastRequest.getConfig().getId());
				noOfInquirySent++; // log
			} else {
				/* Send Failed */
				replyMsg = factory.generateMessage(MessageEnums.M_FAILED, config, clock.updateClock());
				sendLastMessageWrapper(replyMsg, requestMsg.getConfig().getId());
				noOfFailedSent++; // log
			}
		}
	}
	

	private void sendFirstMessageWrapper(Message message, String receiverId) throws Exception {
		if(config.getId().equals(receiverId)) {
			msgManager.addFirstMessage(message);
		} else {
			Connection con = connectionPoolTable.get(receiverId);
			con.sendMessage(message);
		}
	}

	private void sendLastMessageWrapper(Message message, String receiverId) throws Exception {
		if(config.getId().equals(receiverId)) {
			msgManager.addLastMessage(message);
		} else {
			Connection con = connectionPoolTable.get(receiverId);
			con.sendMessage(message);
		}
	}
	
	public void allReplyAchieved() throws Exception {
		
		if(replyCount > quorumSize)
			Globals.logMsg("FATAL : replyCount excedded = " + replyCount );
		
		if((quorumSize - replyCount) != 0) 
			return;
			
		/* BLOCKING Call: write data and send response */
		executeWrite(lastRequest.getRecord());
		
//		Thread.sleep(40);  // TODO: BOOKMARK thread safety delay
		
		/* update reply count for next iteration */
		replyCount = 0;

		/* release all quorum nodes */
		releaseAllQuorumNodes();
		
		/* Check for end of computation */
		if(sequenceNumber > Globals.MAX_WRITE_OPERATION_COUNT) {
			/* Send End of computation message */  
			int total = printSummary();
			Message eoc = factory.generateMessage(MessageEnums.C_END, config, total);
			Connection con = connectionPoolTable.get(socConfigServer.getId());
			con.sendMessage(eoc);
			System.out.println(" ---------------- End of computation !! --------------- ");
			Globals.logMsg(" ---------------- End of computation!! ---------------  ");
		} else {
			if(sequenceNumber != 1)  // END correction for first broadcast
				broadCastRequestMessage();
		}
	}
	
	public void releaseAllQuorumNodes() throws Exception {
		Message releaseRequest = factory.generateMessage(
					MessageEnums.M_RELEASE, config, clock.updateClock());
		
			// send release to self
			msgManager.addFirstMessage(releaseRequest);
		
			/* Send release message to all clients in quorum */
			for(ConfigInfo quorumClientConfig: Globals.getQuorumList()) {
				Connection con = connectionPoolTable.get(quorumClientConfig.getId());
				con.sendMessage(releaseRequest);
			}
			
			noOfReleaseSent += quorumSize;  // log
	}
	
	private void broadCastRequestMessage() {
		if(sequenceNumber <= Globals.MAX_WRITE_OPERATION_COUNT) {
			startTime = System.currentTimeMillis();
			RequestBroadcasterThread t = new RequestBroadcasterThread(config, clock, sequenceNumber, msgManager, connectionPoolTable);
			noOfRequestSent += quorumSize;  // log
			sequenceNumber++;
			t.start();
		} 
	}		
	
	public void executeWrite(Record record) throws Exception {
		/* log time */
		endTime = System.currentTimeMillis();
		Globals.logMsg(" RESULT | TIME ELAPSED = " + (endTime - startTime));
		
		ConfigInfo serverConfig = Globals.getRandomServer();
		Connection con = connectionPoolTable.get(serverConfig.getId());
		code.message.Message writeRequest = factory.generateMessage(MessageEnums.CLIENT_WRITE, config, record);
		con.sendMessage(writeRequest);
		System.out.println(" -> Write request sent :: " + record);
	}
	
	private void debug(String msg) {
		String logMsg = "DEBUG: " + msg + " | " + Thread.currentThread().getStackTrace()[2];
		System.out.println(logMsg);
		Globals.logMsg(logMsg);
		logQueue();
	}
	
	private void logQueue() {
		PriorityQueue<RequestMessage> tempQ = new PriorityQueue<RequestMessage>(30, new RequestComparator());
		tempQ.addAll(requestQueue);
		String qStr = "QUEUE = ";
		RequestMessage r;
		while( (r = tempQ.poll()) != null) {
			qStr = qStr + " [ID=" + r.getConfig().getId() + " , SEQ=" + 
					r.getRecord().getSequenceNumber() + 
					" , TS=" + r.getClock().getTimeStamp() + "]";
		}
		System.out.println(qStr);
		Globals.logMsg(qStr);
	}
	
	private void logQueue(int i) {
		PriorityQueue<RequestMessage> tempQ = new PriorityQueue<RequestMessage>(30, new RequestComparator());
		tempQ.addAll(requestQueue);
		String qStr = "QUEUE = ";
		RequestMessage r;
		while( (r = tempQ.poll()) != null) {
			qStr = qStr + " [ID=" + r.getConfig().getId() + " , SEQ=" + 
					r.getRecord().getSequenceNumber() + 
					" , TS=" + r.getClock().getTimeStamp() + "]";
		}
		System.out.println(qStr + " compare = " + i + "| last= " + lastRequest.getRecord());
		Globals.logMsg(qStr + " compare = " + i + "| last= " + lastRequest.getRecord());
	}
}