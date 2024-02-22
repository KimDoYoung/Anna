package kr.co.kalpa.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface BroadcastServiceAsync {

	void broadcastServer(String message, AsyncCallback<String> callback);

}
