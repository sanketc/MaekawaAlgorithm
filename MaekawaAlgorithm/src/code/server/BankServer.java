package code.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

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
 * Bank server
 * 
 * @author Sanket Chandorkar
 */
public class BankServer {

	private ConfigInfo config;
	
	private MessageManager msgManager;
	
	private MessageFactory factory;
	
	private HashMap<String, Connection> connectionPoolTable;
	
	public BankServer(String id, String address, int port) throws Exception {
		Globals.initializeServer(id);
		this.config = new ConfigInfo(id, address, port);
		this.connectionPoolTable = new HashMap<String, Connection>();
		this.msgManager = new MessageManager();
		this.connectionPoolTable = new HashMap<String, Connection>();
		this.factory = new MessageFactory();
	}
	
	public void start(boolean startServer) throws Exception {
		
		ConnectionAcceptor connectionAcceptor = new ConnectionAcceptor(config, connectionPoolTable, msgManager);
		connectionAcceptor.start();
		
		// start sending connection messages to all clients
		for(ConfigInfo cliConfig: Globals.getClientList()) {
			connect(cliConfig);
		}

		// start sending connection messages to all lower id server
		for(ConfigInfo serConfig: Globals.getServerList()) {
			if(config.getIntId() > serConfig.getIntId()) {
				connect(serConfig);
			}
		}
		
		// start service
		ServerService service = new ServerService(config, msgManager, connectionPoolTable);
		service.start();
		
		// send start of computation
		if(startServer) {
			Message soc = factory.generateMessage(MessageEnums.C_START, config, Globals.MAX_WRITE_OPERATION_COUNT);
			for(ConfigInfo cliConfig: Globals.getClientList()) {
				Connection con = connectionPoolTable.get(cliConfig.getId());
				con.sendMessage(soc);
			}
		}
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

		boolean startServer = false;
		
		if(args.length != 3 && args.length != 4) {
			System.out.println("Missing argument !!");
			System.out.println("Usage:");
			System.out.println("        java core.server.BankServer <ServerId> <address> <port>");
			System.out.println("        java core.server.BankServer <ServerId> <address> <port> <noOfIteration>");
			System.out.println("Exiting program now !!");
			System.exit(Globals.SYS_FAILURE);
		} 

		if(args.length == 4) {
			Globals.MAX_WRITE_OPERATION_COUNT = Integer.parseInt(args[3]);
			startServer = true;
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
			System.out.println("        java core.server.BankServer <Int:ServerId> <String:address> <Integer:port>");
			System.out.println("Exiting program now !!");
			System.exit(Globals.SYS_FAILURE);
		}
		
		BankServer bs = new BankServer(id, address, port);
		System.out.println(" << Server >> :: ID= '" + id + "' started !!");
		bs.start(startServer);
	}
}