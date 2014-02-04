package code.message;

/**
 * Message enums.
 * 
 * @author Sanket Chandorkar
 */
public enum MessageEnums {

	CLIENT_WRITE,
	SERVER_WRITE,
	ERROR,
	ACK,
	HEALTH,
	
	/* Maekawa messages */
	M_REQUEST,
	M_REPLY,
	M_RELEASE,
	M_ENQUIRE,
	M_YIELD,
	M_FAILED,
	
	/* Controlling messages */
	C_START,
	C_END,
	C_TERMINATE,
	C_CONNECT;
	
	@Override
	public String toString(){
		return this.name();
	}
}