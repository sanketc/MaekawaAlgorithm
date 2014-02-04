package code.message.maekawa;

import java.io.Serializable;

import code.client.resource.Clock;
import code.common.ConfigInfo;
import code.data.Record;

@SuppressWarnings("serial")
public class RequestMessage extends MaekawaMessage implements Serializable {

	private Record record;
	
	public RequestMessage(){
	}
	
	public RequestMessage(ConfigInfo config, Clock clock, Record record) {
		super(config, clock);
		this.record = record;
	}

	public Record getRecord() {
		return record;
	}
	
	public int compareValues(RequestMessage o2) {
		RequestMessage o1 = this;
		if (o1.getClock().getTimeStamp() < o2.getClock().getTimeStamp()) {
			return 1; /* o1 >priority> o2 */
		} else if (o1.getClock().getTimeStamp() == o2.getClock().getTimeStamp()) {
			/* Compare process Id: lower less means high priority */
			int id1 = Integer.parseInt(o1.getConfig().getId());
			int id2 = Integer.parseInt(o2.getConfig().getId());
			if (id1 <= id2) 
				return 1;
			return -1;
		} else {
			return -1; /* o1 <priority< o2 */
		}
	}
}