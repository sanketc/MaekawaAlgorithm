package code.data;

import java.io.Serializable;

/**
 * This class represents the structure of one record/request.
 * 
 * @author Sanket Chandorkar
 */
@SuppressWarnings("serial")
public class Record implements Serializable {

	private String id;
	
	private int sequenceNumber;
	
	private String hostName;

	private static final String SEPERATOR = "	";

	public Record(String id, int sequenceNumber, String hostName) {
		this.id = id;
		this.sequenceNumber = sequenceNumber;
		this.hostName = hostName;
	}
	
	@Override
	public String toString(){
		return id + SEPERATOR + sequenceNumber + SEPERATOR + hostName;
	}

	public String getId() {
		return id;
	}

	public int getSequenceNumber() {
		return sequenceNumber;
	}

	public String getHostName() {
		return hostName;
	}

}