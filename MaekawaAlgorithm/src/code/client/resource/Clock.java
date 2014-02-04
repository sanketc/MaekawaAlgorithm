package code.client.resource;

import java.io.Serializable;

/**
 * Implementation of Lamports clock.
 * 
 * @author Sanket Chandorkar
 */
@SuppressWarnings("serial")
public class Clock implements Serializable {

	private int timeStamp;
	
	private final int INCREMENT = 1;
	
	public Clock() {
	}

	public Clock(int timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	synchronized public int getTimeStamp() {
		return timeStamp;
	}
	
	/**
	 * Updates current clock and return new clock.
	 */
	synchronized public Clock updateClock() {
		timeStamp = timeStamp + INCREMENT;
		return new Clock(timeStamp);
	}
	
	synchronized public Clock updateClock(Clock msgClock) {
		timeStamp = Math.max(timeStamp + INCREMENT, msgClock.getTimeStamp() + INCREMENT);
		return new Clock(timeStamp);
	}
}