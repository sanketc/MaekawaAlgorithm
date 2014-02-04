package code.message.controller;

import java.io.Serializable;

import code.common.ConfigInfo;
import code.message.Message;

@SuppressWarnings("serial")
public class EndOfComputationMessage extends Message implements Serializable {

	private int messageCount;

	public EndOfComputationMessage() {
	}

	public EndOfComputationMessage(ConfigInfo config, int messageCount) {
		super(config);
		this.messageCount = messageCount;
	}

	public int getMessageCount() {
		return messageCount;
	}
}