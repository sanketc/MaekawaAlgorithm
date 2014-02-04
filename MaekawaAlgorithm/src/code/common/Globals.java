package code.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;
import java.util.StringTokenizer;

import code.message.AckMessage;
import code.message.Message;
import code.message.maekawa.MaekawaMessage;

/**
 * Global API, Variables and Constants.
 * 
 * @author Sanket Chandorkar
 */
public class Globals {

	public static final int SYS_SUCCESS = 0;
	public static final int SYS_FAILURE = -1;

	public static final String DATA = "data";
	public static final String CONFIG = "config";
	public static final String LOG = "log";
	
	private static final String SERVER_STR = "SERVER";
	private static final String CLIENT_STR = "CLIENT";
	
	private static PrintWriter lowWriter;
	
	public static final String configurationFile = CONFIG + File.separator + "ConfigurationFile.txt";
	public static final String quorumConfigFile = CONFIG + File.separator + "QuorumConfigurationFile.txt";

	/**
	 * List of servers.
	 */
	private static ArrayList<ConfigInfo> serverList = null;
	
	/**
	 * List of clients.
	 */
	private static ArrayList<ConfigInfo> clientList = null;
	
	/**
	 * List of quorum nodes for this client.
	 */
	private static ArrayList<ConfigInfo> quorumList = null;
	
	/**
	 * Max number of write operations.
	 */
	public static int MAX_WRITE_OPERATION_COUNT = 40;
	
	public static ArrayList<ConfigInfo> getServerList() {
		return serverList;
	}

	public static ArrayList<ConfigInfo> getQuorumList() {
		return quorumList;
	}
	
	public static ArrayList<ConfigInfo> getClientList() {
		return clientList;
	}

	public static void initializeServer(String id) throws Exception {
		initializeLog(id);
		readConfigFile(id);
	}

	public static void initializeClient(String id) throws Exception {
		initializeLog(id);
		readConfigFile(id);
		quorumList = readQuorumFile(id, quorumConfigFile);
	}
	
	public static void Finalize(){
		closeLog();
	}
	
	private static void initializeLog(String id){
		String logFileName = LOG + File.separator + id + "_log.txt";
		try {
			lowWriter = new PrintWriter(new FileWriter(new File(logFileName)));
		} catch (IOException e) {
			System.out.println("Error: While opening log file : " + logFileName);
			System.out.println("Exiting application now !!");
			System.exit(SYS_FAILURE);
		}
	}
	
	synchronized private static void closeLog(){
		lowWriter.close();
	}
	
	synchronized public static void logMsg(String msg){
		lowWriter.println("LOG: " + msg);
		lowWriter.flush();
	}

	synchronized public static void logReceivedMsg(Message message){
		if(message instanceof AckMessage) return;		// TODO:
		ConfigInfo config = message.getConfig();
		if(message instanceof MaekawaMessage) {
			MaekawaMessage mm = (MaekawaMessage) message;
			lowWriter.println("MESSAGE_RECEIVED: " + message.getClass().getSimpleName() +
				" from " + config.getId() + " @ " + config.getAddress() + " | TS= " + mm.getClock().getTimeStamp());
		} else {
			lowWriter.println("MESSAGE_RECEIVED: " + message.getClass().getSimpleName() +
				" from " + config.getId() + " @ " + config.getAddress());
		}
		lowWriter.flush();
	}
	
	synchronized public static void logSentMsg(Message message, ConfigInfo config){
		if(message instanceof AckMessage) return;		// TODO:
		if(message instanceof MaekawaMessage) {
			MaekawaMessage mm = (MaekawaMessage) message;
			lowWriter.println("MESSAGE_SENT: " + message.getClass().getSimpleName() + 
				" to "  + config.getId() + " @ " + config.getAddress() + " | TS= " + mm.getClock().getTimeStamp());
		} else {
			lowWriter.println("MESSAGE_SENT: " + message.getClass().getSimpleName() + 
					" to "  + config.getId() + " @ " + config.getAddress());
		}
		lowWriter.flush();
	}
	
	synchronized public static void logErrorMsg(String msg) {
		lowWriter.println("ERROR: " + msg);
		lowWriter.flush();
	}
	
	private static void readConfigFile(String currId) throws Exception {
		File file = new File(configurationFile);
		if(!file.exists()) {
			System.out.println("Error: Config file does not exits : " + configurationFile);
			System.out.println("Exiting application now !!");
			System.exit(SYS_FAILURE);
		}
		
		clientList = new ArrayList<ConfigInfo>();
		serverList = new ArrayList<ConfigInfo>();
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		/* Skip header */
		br.readLine();
		
		String line;
		StringTokenizer stk;
		while ((line = br.readLine()) != null) {
			stk = new StringTokenizer(line);
			if (stk.countTokens() != 4) {
				System.out.println("Error: Config file not in correct format !!");
				System.out.println("Error: Config file Name: " + configurationFile);
				System.out.println("Exiting application now !!");
				System.exit(SYS_FAILURE);
			}
			
			String type = stk.nextToken();
			String id = stk.nextToken();
			String address = stk.nextToken();
			int port = Integer.parseInt(stk.nextToken());
			ConfigInfo config = new ConfigInfo(id, address, port);
			if(type.equalsIgnoreCase(SERVER_STR)) {
				if (id.equals(currId))
					continue;
				serverList.add(config);
			} else if(type.equalsIgnoreCase(CLIENT_STR)) {
				clientList.add(config);
			} else {
				br.close();
				throw new Exception("Configguration file in incorrect format !!");
			}
		}
		
		br.close();
	}
	
	/**
	 * Current node is not added. 
	 **/
	private static ArrayList<ConfigInfo> readQuorumFile(String currId, String fileName) throws Exception {
		File file = new File(fileName);
		if(!file.exists()) {
			System.out.println("Error: Config file does not exits : " + fileName);
			System.out.println("Exiting application now !!");
			System.exit(SYS_FAILURE);
		}
		
		/* Format client config list */
		Hashtable<String, ConfigInfo> table = new Hashtable<String, ConfigInfo>();
		for(ConfigInfo info : clientList) {
			table.put(info.getId(), info);
		}
		
		/* Read list */
		BufferedReader br = new BufferedReader(new FileReader(file));
		ArrayList<ConfigInfo> list = new ArrayList<ConfigInfo>();
		
		/* Skip header */
		br.readLine();
		
		String line;
		StringTokenizer stk;
		while ((line = br.readLine()) != null) {
			stk = new StringTokenizer(line);
			if (stk.countTokens() <= 2) {
				System.out.println("Error: Quorum Config file not in correct format !!");
				System.out.println("Error: File Name: " + fileName);
				System.out.println("Exiting application now !!");
				System.exit(SYS_FAILURE);
			}
			
			String id = stk.nextToken();
			if (!id.equals(currId))
				continue;

			while(stk.hasMoreElements()) {
				id = stk.nextToken();
				if (id.equals(currId))
					continue;
				ConfigInfo cInfo = table.get(id);
				if(cInfo == null) {
					System.out.println("Error: Quorum Config file not in correct format :: Clinet address not found !!");
					System.out.println("Error: File Name: " + fileName);
					System.out.println("Exiting application now !!");
					System.exit(SYS_FAILURE);
				}
				list.add(cInfo);
			}
		}
		
		br.close();
		return list;
	}
	
	public static ConfigInfo getRandomServer(){
		if(serverList == null)
			return null;
		Random rand = new Random();
		if(serverList == null) {
			int  index = rand.nextInt(100) % serverList.size() ;
			return serverList.get(index);
		} else {
			return serverList.get(0);
		}
	}
	
	public static int getRandomDelay(){
		Random rand = new Random();
		return 10 + rand.nextInt(40);
	}
	
}