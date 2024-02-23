# Anna

## 개요

1. GWT 기본 설치
2. WebSocket을 사용하여 접속한 사용자들에게 알람들 보낸다.

## 기능

1. 관리자가 알람메세지를 보낸다.
2. 관리자가 현재 WebSocket에 접속한 사용자들의 리스트 정보를 얻는다.

## 구현

- 전반적으로 jsni를 사용하여 구현하였으며 
- endpoint broadcast를 만듬 
    - web.xml
    ```
      <servlet>
        <servlet-name>broadcastServlet</servlet-name>
        <servlet-class>kr.co.kalpa.gwt.server.BroadcastServiceImpl</servlet-class>
      </servlet>
      
      <servlet-mapping>
        <servlet-name>broadcastServlet</servlet-name>
        <url-pattern>/anna/broadcast</url-pattern>
      </servlet-mapping>
   ```
- java files 
    1. client : BroadcastService.java
    2. client : BroadcastServiceAsync.java
    3. server : BroadcastServiceImpl.java
    
- 주고받는 데이터는 JSON형태로 주고 받으며 java쪽에 WsMessage를 구현함
```
	private String command;
	private String sender;
	private String[] contents;
```
  command는 현재 3게 만들어 놓음
   1. connect : 사용자가 접속했음을 알림 -> WsManager가 session정보를 보관함
   2. alarm   : 알람 메세지임-> WsManager 가 가지고 있는 모든 session 들에  알림메세지를 전송함
   3. status  : 현재 session에 들어 있는 사용자들 리스트를 보내줌
   
- JSON데이터를 처리하기 위해서 jackson라이브러리를 사용함.
    
## 소스    
1. Module Load 시
	
    - setupWebSocket 호출
    - 모든 client가 websocket을 open하게 된다.
    - 모든 client가 메세지를 받을 준비가 된다.
    - 메세지의 종류에 따라서 화면에 동작한다. 현재 alert로 구현
    - 일정시간이 지나면 예기치 못한 원인으로 websocket이 close상태가된다. 이를 방지하기 위해서 3초후에 다시 접속한다.
    
```javascript
public native void setupWebSocket() /*-{
	  //debugger;	
	  $wnd.ws = new WebSocket("ws://localhost:8888/anna/broadcast");
	  $wnd.ws.onopen = function() {
	  	 console.log('client:웹소켓 open');
	  	 //서버로 내가 접속했다고 WsMessage를 보낸다.
	  	 //보내는 데이터는 JSON 객체의 문자열
	  	 var wsMessage = {
	  	 		command : "connect",
	  	 		sender  : "kdy987",
	  	 		contents :[]
	  	 };
	     $wnd.ws.send(JSON.stringify(wsMessage));
	    console.log('client:웹소켓 connect 보냄');
	  };
	  $wnd.ws.onmessage = function(message) {
	  	console.log(message);
	  	 var data = message.data;
	     var wsMsg = JSON.parse(data);
	     console.debug('client 받은 메세지'+wsMsg);
	     if(wsMsg.command == 'alarm'){
	     	var alarmText = wsMsg.contents.join("\n");
	     	console.log(alarmText);
	     	alert(alarmText);
	     }else if(wsMsg.command == 'status'){
	     	var statusText = wsMsg.contents.join("\n");
	     	console.log(statusText);
	     	alert(statusText);
	     }
	  };
	  $wnd.ws.onclose = function() {
	    console.debug(new Date() + " 웹소켓 close!");
        setTimeout(function() {
            // Java 객체의 메소드를 호출
            that.@kr.co.kalpa.gwt.client.Anna::setupWebSocket()();
        }, 3000); // 3초 후 재연결 시도    	    
	  };
	  $wnd.ws.onerror = function(error) {
	    console.error("웹소켓 error !!!"+error);
	  };
	}-*/;    
```
### 서버쪽 BroadcastServiceImpl.java

- WsSessionManager를 작성 : 접속한 session을 관리함.
- 각 command에 따른 메세지 전송을 수행함.

```
	private static final WsSessionManager wsManager = new WsSessionManager();
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

```
