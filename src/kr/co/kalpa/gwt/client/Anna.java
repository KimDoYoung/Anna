package kr.co.kalpa.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import kr.co.kalpa.gwt.shared.FieldVerifier;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Anna implements EntryPoint {
	/**
	 * 서버에 연락할 수 없거나 오류가 발생한 경우 사용자에게 표시되는 메시지입니다.
	 */
	private static final String SERVER_ERROR = "서버에 연락하는 중 오류가 발생했습니다. 네트워크 연결을 확인하고 다시 시도해주세요.";

	/**
	 * 서버 측 인사 서비스와 통신하기 위한 원격 서비스 프록시를 생성합니다.
	 */
	private final GreetingServiceAsync greetingService = GWT.create(GreetingService.class);
	private final BroadcastServiceAsync broadcastService = GWT.create(BroadcastService.class);
	/**
	 * 이것이 진입점 메서드입니다.
	 */
	public void onModuleLoad() {
		
		final Button sendButton = new Button("전송");
		final TextBox nameField = new TextBox();
		nameField.setText("GWT 사용자");
		final Label errorLabel = new Label();

		// 위젯에 스타일 이름을 추가할 수 있습니다.
		sendButton.addStyleName("sendButton");

		// nameField와 sendButton을 RootPanel에 추가합니다.
		// 전체 body 요소를 가져 오려면 RootPanel.get()을 사용합니다.
		RootPanel.get("nameFieldContainer").add(nameField);
		RootPanel.get("sendButtonContainer").add(sendButton);
		RootPanel.get("errorLabelContainer").add(errorLabel);
		
		//-------------------------------------------------------
		final Button wsAlarmButton = new Button("BroadCast전송(알람)");
		final Button wsStatusButton = new Button("접속정보");
		final TextBox wsMessageField = new TextBox();
		wsMessageField.setText("관리자로 부터의 알람");
		RootPanel.get("wsContainer").add(wsMessageField);
		RootPanel.get("wsContainer").add(wsAlarmButton);
		RootPanel.get("wsContainer").add(wsStatusButton);
		wsAlarmButton.addStyleName("primary");
		// 앱이 로드 될 때 커서를 이름 필드에 포커스합니다.
		nameField.setFocus(true);
		nameField.selectAll();

		// 팝업 대화 상자를 만듭니다.
		final DialogBox dialogBox = new DialogBox();
		dialogBox.setText("원격 프로시저 호출");
		dialogBox.setAnimationEnabled(true);
		final Button closeButton = new Button("닫기");
		// 위젯의 ID를 설정할 수 있습니다.
		closeButton.getElement().setId("closeButton");
		final Label textToServerLabel = new Label();
		final HTML serverResponseLabel = new HTML();
		VerticalPanel dialogVPanel = new VerticalPanel();
		dialogVPanel.addStyleName("dialogVPanel");
		dialogVPanel.add(new HTML("<b>서버에 이름 보내는 중:</b>"));
		dialogVPanel.add(textToServerLabel);
		dialogVPanel.add(new HTML("<br><b>서버 응답:</b>"));
		dialogVPanel.add(serverResponseLabel);
		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		dialogVPanel.add(closeButton);
		dialogBox.setWidget(dialogVPanel);

		// DialogBox를 닫는 핸들러를 추가합니다.
		closeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
				sendButton.setEnabled(true);
				sendButton.setFocus(true);
			}
		});

		// sendButton 및 nameField에 대한 핸들러를 만듭니다.
		class MyHandler implements ClickHandler, KeyUpHandler {
			/**
			 * 사용자가 sendButton을 클릭했을 때 호출됩니다.
			 */
			public void onClick(ClickEvent event) {
				sendNameToServer();
			}

			/**
			 * 사용자가 nameField에 타이핑 할 때 호출됩니다.
			 */
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					sendNameToServer();
				}
			}

			/**
			 * nameField에서 서버로 이름을 보내고 응답을 기다립니다.
			 */
			private void sendNameToServer() {
				// 먼저 입력을 유효성 검사합니다.
				errorLabel.setText("");
				String textToServer = nameField.getText();
				if (!FieldVerifier.isValidName(textToServer)) {
					errorLabel.setText("적어도 네 글자를 입력하세요");
					return;
				}

				// 그런 다음 입력을 서버로 보냅니다.
				sendButton.setEnabled(false);
				textToServerLabel.setText(textToServer);
				serverResponseLabel.setText("");
				greetingService.greetServer(textToServer, new AsyncCallback<String>() {
					public void onFailure(Throwable caught) {
						// 사용자에게 RPC 오류 메시지를 표시합니다.
						dialogBox.setText("원격 프로시저 호출 - 실패");
						serverResponseLabel.addStyleName("serverResponseLabelError");
						serverResponseLabel.setHTML(SERVER_ERROR);
						dialogBox.center();
						closeButton.setFocus(true);
					}

					public void onSuccess(String result) {
						dialogBox.setText("원격 프로시저 호출");
						serverResponseLabel.removeStyleName("serverResponseLabelError");
						serverResponseLabel.setHTML(result);
						dialogBox.center();
						closeButton.setFocus(true);
					}
				});
			}
		}
		//웹소켓 버튼 알람
		wsAlarmButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				String message = wsMessageField.getText();
				sendWebSocketAlarmMessage(message);				
			}
		});
		
		wsStatusButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				String message = wsMessageField.getText();
				sendWebSocketStatusMessage(message);				
			}
		});
		
		// 이름을 서버로 보내는 핸들러를 추가합니다.
		MyHandler handler = new MyHandler();
		sendButton.addClickHandler(handler);
		nameField.addKeyUpHandler(handler);
		
		//웹소켓을 설정한다
		setupWebSocket();
	}
	
	
	public native void setupWebSocket() /*-{
	  var that = this; // Java 객체 참조를 저장
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
	     console.debug('client 받은 메세지 :' + wsMsg);
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
        console.log('WebSocket disconnected. Attempting to reconnect...');
        setTimeout(function() {
            // Java 객체의 메소드를 호출
            that.@kr.co.kalpa.gwt.client.Anna::setupWebSocket()();
        }, 3000); // 3초 후 재연결 시도        
      };	  
	  $wnd.ws.onerror = function(error) {
	    console.error("웹소켓 error !!!"+error);
	  };
	}-*/;

	public native void showAlarm(String text) /*-{
		console.debug("showAlarm:" + text);
		alert(text);
	}-*/;
	
    // 메시지를 보내는 JSNI 메소드
    private native void sendWebSocketAlarmMessage(String text) /*-{
	  	 var wsMessage = {
	  	 		command : "alarm",
	  	 		sender  : "kdy987",
	  	 		contents :[ text ]
	  	 };

	  	 var websocket = $wnd.ws;

	  	 if(websocket && (websocket.readyState != WebSocket.CLOSED)){
	     	websocket.send(JSON.stringify(wsMessage));
	  	 }else{
	  	 	console.error('웹소켓이 설정되지 않았습니다');
	  	 	
	  	 }
    }-*/;
    // 메시지를 보내는 JSNI 메소드
    private native void sendWebSocketStatusMessage(String text) /*-{
	  	 var wsMessage = {
	  	 		command : "status",
	  	 		sender  : "kdy987",
	  	 		contents :[]
	  	 };
	  	 var websocket = $wnd.ws;
	  	 if(websocket){
	     	websocket.send(JSON.stringify(wsMessage));
	  	 }else{
	  	 	console.error('웹소켓이 설정되지 않았습니다');
	  	 }
    }-*/;


}
