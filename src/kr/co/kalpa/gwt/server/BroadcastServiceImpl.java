package kr.co.kalpa.gwt.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import kr.co.kalpa.gwt.client.BroadcastService;
import kr.co.kalpa.gwt.server.websocket.SessionInfo;
import kr.co.kalpa.gwt.server.websocket.WsMessage;
import kr.co.kalpa.gwt.server.websocket.WsSessionManager;

/**
 * The server-side implementation of the RPC service.
 */
@ServerEndpoint("/anna/broadcast")
@SuppressWarnings("serial")
public class BroadcastServiceImpl extends RemoteServiceServlet implements BroadcastService {
	
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final WsSessionManager wsManager = new WsSessionManager();
//	public BroadcastServiceImpl() {
//		wsManager = new WsSessionManager();
//	}
	
    @OnOpen
    public void onOpen(Session session) {
        System.out.println("웹소켓 연결 열림: " + session.getId());
        // 세션매니저에 저장
        //wsManager.addSession(session, wsMessage.getSender());        
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            // 클라이언트로부터 받은 JSON 메시지를 파싱합니다.
            WsMessage wsMessage= objectMapper.readValue(message, WsMessage.class);
            System.out.println("웹소켓 메세지받음: "+wsMessage.toString());
//            // 세션매니저에 저장
            wsManager.addSession(session, wsMessage.getSender());
            
            //명령에 따른 처리 
            String command = wsMessage.getCommand();
            if("status".equals(command)) { // 현재 접속된 모든 사용자들 리스트
            	WsMessage sendMessage = new WsMessage();
            	sendMessage.setCommand("status");
            	sendMessage.setSender(wsMessage.getSender());
            	List<String> infoList = new ArrayList<>();
            	
            	for(SessionInfo info : wsManager.sessionInfoList()) {
            		infoList.add(info.getSender() + ":" + info.getConnectTime().toString());
            	}
            	String[] contentArray = infoList.toArray(new String[0]);
            	sendMessage.setContents(contentArray);
            	
            	//sendMessage을 json String으로
            	String msg = objectMapper.writeValueAsString(sendMessage);
            	
            	//요청한 관리자에게 보낸다.
            	broadcast(new Session[] {session}, msg);
            	
            }else if("alarm".equals(command)) { //접속된 모든 사용자들에게 메세지를 보낸다.
            	
            	String alarmMsg  = objectMapper.writeValueAsString(wsMessage);
            	Session[] sessions = wsManager.sessionList().toArray(new Session[0]);
            	broadcast(sessions, alarmMsg);
            	
            }else if("connect".equals(command)) { //각 사용자들이 자신의 접속정보를 알린다.
            	; //아무 데이터 전송도 하지 않는다.
            }
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }
    // 모든 클라이언트에 메시지 브로드캐스트하는 메서드
    private void broadcast(Session[] sessions, String message) {
    	
    	for(Session session : sessions){
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                    System.out.println("서버에세 sessionId : "+session.getId() + " 로 [" + message + "] 보냄");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }    		
    	}
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("브라우저가 닫히면 웹소켓이 끊어짐");
        System.out.println("웹소켓 연결 닫힘: " + session.getId());
    }
	
	@Override
	public String broadcastServer(String name) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return "아~~ 왜~ 이런 짓을 했을까?";
	}

//	public String greetServer(String input) throws IllegalArgumentException {
//		// Verify that the input is valid. 
//		if (!FieldVerifier.isValidName(input)) {
//			// If the input is not valid, throw an IllegalArgumentException back to
//			// the client.
//			throw new IllegalArgumentException("Name must be at least 4 characters long");
//		}
//
//		String serverInfo = getServletContext().getServerInfo();
//		String userAgent = getThreadLocalRequest().getHeader("User-Agent");
//
//		// Escape data from the client to avoid cross-site script vulnerabilities.
//		input = escapeHtml(input);
//		userAgent = escapeHtml(userAgent);
//
//		return "Hello, " + input + "!<br><br>I am running " + serverInfo + ".<br><br>It looks like you are using:<br>"
//				+ userAgent;
//	}

	/**
	 * 클라이언트로부터 받은 데이터를 이스케이프 처리하여 XSS(교차 사이트 스크립팅) 취약점을 방지합니다.
	 * 
	 * @param html 이스케이프 처리할 HTML 문자열
	 * @return 이스케이프 처리된 문자열
	 */
	private String escapeHtml(String html) {
		if (html == null) {
			return null;
		}
		return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
	}

}
