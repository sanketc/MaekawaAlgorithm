package code.message.controller;

import java.io.Serializable;

import code.common.ConfigInfo;
import code.message.Message;

@SuppressWarnings("serial")
public class StartOfComputationMessage extends Message implements Serializable {

	private int maxItteration;
	
	public StartOfComputationMessage() {
	}

	public StartOfComputationMessage(ConfigInfo config, int maxItteration) {
		super(config);
		this.maxItteration = maxItteration;
	}
	
	public int getMaxItteration() {
		return maxItteration;
	}
}