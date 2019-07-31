package com.devcodes.workshopkit.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log
public class MattermostWebsocketDispatcher {
	
	private ObjectMapper mapper;
	
	private WebSocketClient client;
	
	private long seq;
	
	private MattermostEventListener listener;

	private boolean reconnect;

	private ExecutorService executorService;

	public MattermostWebsocketDispatcher(String host, boolean secure, String accessToken, MattermostEventListener listener) {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", "Bearer " + accessToken);

		String protocol = secure ? "wss" : "ws";
		
		try {
			this.client = new WebSocketClientImpl(new URI(protocol+"://" + host + "/api/v4/websocket"), headers);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Failed to create websocket client", e); 
		}
		
		this.mapper = new ObjectMapper();
		
		this.seq = 1;
		
		this.listener = listener;
		
		this.reconnect = true;
	}
	
	public void init() {
		executorService = Executors.newFixedThreadPool(10);
		
		this.client.connect();
	}

	public void destroy() {
		this.reconnect = false;

		this.client.close();

		this.executorService.shutdown();
	}

	private void onOpen(ServerHandshake handshakedata) {
		log.info("Connection opened");
	}

	private void onClose(int code, String reason, boolean remote) {
		this.seq = 1;
		log.warning("closed with exit code " + code + " additional info: " + reason);
		
		if(this.reconnect) {
			log.info("Reconnecting...");
			
			new Thread(() -> this.client.reconnect()).start();
		}
	}

	private void onMessage(String messageString) {
		log.info("received message: " + messageString);
		
		try {
			MattermostEvent message = mapper.readValue(messageString, MattermostEvent.class);

			executorService.submit(new Runnable() {
				@Override
				public void run() {
					MattermostWebsocketDispatcher.this.listener.onEvent(message);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void onError(Exception ex) {
		log.severe(ex.toString());
	}
	
	class WebSocketClientImpl extends WebSocketClient {

		public WebSocketClientImpl(URI serverUri, Map<String, String> httpHeaders) {
			super(serverUri, httpHeaders);
		}

		@Override
		public void onOpen(ServerHandshake handshakedata) {
			MattermostWebsocketDispatcher.this.onOpen(handshakedata);
		}

		@Override
		public void onMessage(String message) {
			MattermostWebsocketDispatcher.this.onMessage(message);
		}

		@Override
		public void onClose(int code, String reason, boolean remote) {
			MattermostWebsocketDispatcher.this.onClose(code, reason, remote);
		}

		@Override
		public void onError(Exception ex) {
			MattermostWebsocketDispatcher.this.onError(ex);
		}
	}
}
