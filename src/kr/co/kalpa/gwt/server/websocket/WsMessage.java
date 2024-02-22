package kr.co.kalpa.gwt.server.websocket;

import java.util.Arrays;

public class WsMessage {
	private String command;
	private String sender;
	private String[] contents;
	
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String[] getContents() {
		return contents;
	}
	public void setContents(String[] contents) {
		this.contents = contents;
	}
	@Override
	public String toString() {
		return "WsMessage [command=" + command + ", sender=" + sender + ", contents=" + Arrays.toString(contents) + "]";
	}
	
	
	
}
