package code.message.server;

import java.io.Serializable;

import code.common.ConfigInfo;
import code.data.Record;
import code.message.Message;

@SuppressWarnings("serial")
public class ServerRequestMessage extends Message implements Serializable {

	private Record record;

	public ServerRequestMessage() {
	}

	public ServerRequestMessage(ConfigInfo config, Record record) {
		super(config);
		this.record = record;
	}

	public Record getRecord() {
		return record;
	}
}