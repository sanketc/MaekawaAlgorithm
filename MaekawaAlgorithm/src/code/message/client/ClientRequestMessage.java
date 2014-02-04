package code.message.client;

import java.io.Serializable;

import code.common.ConfigInfo;
import code.data.Record;
import code.message.Message;

@SuppressWarnings("serial")
public class ClientRequestMessage extends Message implements Serializable {

	private Record record;

	public ClientRequestMessage() {
	}

	public ClientRequestMessage(ConfigInfo config, Record record) {
		super(config);
		this.record = record;
	}

	public Record getRecord() {
		return record;
	}
}