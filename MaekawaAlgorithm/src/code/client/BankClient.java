package code.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import code.client.resource.Clock;
import code.client.resource.MessageManager;
import code.common.ConfigInfo;
import code.common.Globals;
import code.message.Message;
import code.message.MessageEnums;
import code.message.MessageFactory;
import code.net.Connection;
import code.net.ConnectionAcceptor;
import code.net.ListnerThread;

/**
 * Bank client.
 * 
 * @author Sanket Chandorkar
 */
public class BankClient {

	private ConfigInfo config;
	
	private Clock clock;
	
	private MessageManager msgManager;
	
	private MessageFactory factory;
	
	private HashMap<String, Connection> connectionPoolTable;

	public BankClient(String idStr, String address, int port) throws Exception {
		Globals.initializeClient(idStr);
		config = new ConfigInfo(idStr, address, port);
		clock = new Clock(0);
		msgManager = new MessageManager();
		connectionPoolTable = new HashMap<String, Connection>();
		factory = new MessageFactory();
	}

	public void start() throws Exception {
		
		ConnectionAcceptor connectionAcceptor = new ConnectionAcceptor(config, connectionPoolTable, msgManager);
		connectionAcceptor.start();
		
		// start sending connection messages to lower iDS
		for(ConfigInfo cliConfig: Globals.getClientList()) {
			if(config.getIntId() > cliConfig.getIntId()) {
				connect(cliConfig);
			}
		}
		
		// start service
		ClientService service = new ClientService(config, clock, msgManager, connectionPoolTable);
		service.start();
	}
	
	public void connect(ConfigInfo receiverConfig) throws Exception {

		Socket socket = null;
		ObjectOutputStream outStream = null;
		ObjectInputStream inputStream = null;
		
		try {
			socket = new Socket(receiverConfig.getAddress(), receiverConfig.getPort());
			outStream = new ObjectOutputStream(socket.getOutputStream());
			inputStream = new ObjectInputStream(socket.getInputStream());
		} catch (UnknownHostException e) {
			String errMsg = "Don't know about host: " + receiverConfig.getAddress();
			Globals.logErrorMsg(errMsg);
			System.out.println(errMsg);
			System.exit(Globals.SYS_FAILURE);
		} catch (IOException e) {
			String errMsg = "Couldn't get I/O for the connection to: " + receiverConfig.getAddress();
			Globals.logErrorMsg(errMsg);
			System.out.println(errMsg);
			System.exit(Globals.SYS_FAILURE);
		}

		Message connectMessage = factory.generateMessage(MessageEnums.C_CONNECT, config);
		outStream.writeObject(connectMessage);
		outStream.flush();

		Globals.logSentMsg(connectMessage, receiverConfig);
		
		System.out.println("Connected to: " + receiverConfig);
		
		// update connection pool table
		Connection con = new Connection(receiverConfig, socket, inputStream, outStream);
		connectionPoolTable.put(receiverConfig.getId(), con);
		
		// start listner thread
		ListnerThread t = new ListnerThread(con, msgManager);
		t.start();
		System.out.println("Started listneing for : " + receiverConfig);
		
	}
	
	public static void main(String[] args) throws Exception {
		if(args.length != 3) {
			System.out.println("Missing argument !!");
			System.out.println("Usage:");
			System.out.println("        java core.server.BankClient <ClientId> <address> <port>");
			System.out.println("Exiting program now !!");
			System.exit(Globals.SYS_FAILURE);
		}
		
		String id = null, address = null;
		int port = 0;

		try {
			id = args[0];
			address = args[1];
			port = Integer.parseInt(args[2]);
		}
		catch(Exception e) {
			System.out.println("Incorrect argument !!");
			System.out.println("Usage:");
			System.out.println("        java core.server.BankClient <Int:ClientId> <String:address> <Integer:port>");
			System.out.println("Exiting program now !!");
			System.exit(Globals.SYS_FAILURE);
		}

		BankClient bc = new BankClient(id, address, port);
		System.out.println(" << CLIENT >> :: ID= '" + id + "' started !!");
		bc.start();
//		Globals.Finalize();
	}
}