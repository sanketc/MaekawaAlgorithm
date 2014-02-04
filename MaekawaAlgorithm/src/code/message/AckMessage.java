package code.message;

import java.io.Serializable;

import code.common.ConfigInfo;
import code.data.Record;

@SuppressWarnings("serial")
public class AckMessage extends Message implements Serializable{

	private Record record;
	
	public AckMessage(){
	}
	
	public AckMessage(ConfigInfo config, Record record) {
		super(config);
		this.record = record;
	}
	
	public Record getRecord() {
		return record;
	}
}