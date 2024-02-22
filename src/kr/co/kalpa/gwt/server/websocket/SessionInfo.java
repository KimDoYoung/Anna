package kr.co.kalpa.gwt.server.websocket;

import java.time.LocalDateTime;

import javax.websocket.Session;

/**
 *  메모리에서 관리하는 session정보
 */
public class SessionInfo {
	private Session session;
	private String sender;
	private LocalDateTime connectTime;

	public Session getSession() {
		return session;
	}
	
	public void setSession(Session session) {
		this.session = session;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public LocalDateTime getConnectTime() {
		return connectTime;
	}
	public void setConnectTime(LocalDateTime connectTime) {
		this.connectTime = connectTime;
	}

	@Override
	public String toString() {
		return "SessionInfo [session=" + session + ", sender=" + sender + ", connectTime=" + connectTime + "]";
	}
	
	
}
