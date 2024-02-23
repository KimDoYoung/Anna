package kr.co.kalpa.gwt.server.websocket;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.websocket.Session;

public class WsSessionManager {

	private Map<String, SessionInfo> sessionMap;
	
	public WsSessionManager() {
		sessionMap = new HashMap<>();
	}
	/**
	 * 접속한 session을 추가
	 * @param session
	 */
	public void addSession(Session session, String sender) {
		
		String key = session.getId();
		
		SessionInfo sessionInfo = new SessionInfo();
		sessionInfo.setSession(session);
		sessionInfo.setSender(sender);;
		sessionInfo.setConnectTime(LocalDateTime.now());
		
		//중복저장하지 않음
		if(sessionMap.containsKey(key)) {
			sessionMap.remove(key);
		}
		sessionMap.put(key, sessionInfo);
	}
	/**
	 * 현재 접속된 session list 리턴
	 * 
	 * @return
	 */
	public List<Session> sessionList(){
		List<Session> list = new ArrayList<>();
		for (Map.Entry<String, SessionInfo> entry : sessionMap.entrySet()) {
			list.add(entry.getValue().getSession());
		}
		return list;
	}
	/**
	 * 현재 접속된 sessionInfo 리스트 
	 * @return
	 */
	public List<SessionInfo> sessionInfoList(){
		List<SessionInfo> list = new ArrayList<>();
		for (Map.Entry<String, SessionInfo> entry : sessionMap.entrySet()) {
			list.add(entry.getValue());
		}
		return list;
	}
	/**
	 * session을 관리대상에서 삭제한다
	 * @param session
	 */
	public void remove(Session session) {
		if(session == null) {
			return;
		}
		String sessionId = session.getId();
		if(sessionMap.containsKey(sessionId)) {
			sessionMap.remove(sessionId);
		}
	}
}
