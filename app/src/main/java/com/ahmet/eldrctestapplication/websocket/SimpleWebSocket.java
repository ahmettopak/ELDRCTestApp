package com.ahmet.eldrctestapplication.websocket;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketListener;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.PingFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;


public class SimpleWebSocket extends WebSocketClient {

    private WebSocketListener webSocketListener;

    // Constants for ping interval, reconnect delay, max reconnect attempts, and connection timeout
    private static final int PING_INTERVAL = 30000; // 30 seconds
    private static final int RECONNECT_DELAY = 5000; // 5 seconds
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private Timer pingTimer;
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);

    // Constructor to initialize WebSocketClient with URL and listener
    public SimpleWebSocket(String url, WebSocketListener listener) throws URISyntaxException {
        super(new URI(url));
        this.webSocketListener = listener;
    }

    // Method to set the listener
    public void setListener(WebSocketListener listener) {
        this.webSocketListener = listener;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        reconnectAttempts.set(0); // Reset reconnect attempts on successful connection
        webSocketListener.onWebsocketOpen(this, handshakedata);
        startPingTimer(); // Start the ping timer
    }

    @Override
    public void onMessage(String message) {
        webSocketListener.onWebsocketMessage(this, message);
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        webSocketListener.onWebsocketMessage(this, bytes);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        webSocketListener.onWebsocketClose(this, code, reason, remote);
        stopPingTimer(); // Stop the ping timer
        attemptReconnect(); // Attempt to reconnect
    }

    @Override
    public void onError(Exception ex) {
        webSocketListener.onWebsocketError(this, ex);
    }

    @Override
    public void onWebsocketPing(WebSocket conn, Framedata f) {
        super.onWebsocketPing(conn, f);
        webSocketListener.onWebsocketPing(conn, f);
    }

    @Override
    public void onWebsocketPong(WebSocket conn, Framedata f) {
        super.onWebsocketPong(conn, f);
        webSocketListener.onWebsocketPong(conn, f);
    }

    @Override
    public PingFrame onPreparePing(WebSocket conn) {
        webSocketListener.onPreparePing(conn);
        return super.onPreparePing(conn);
    }

    @Override
    public void onWebsocketClosing(WebSocket conn, int code, String reason, boolean remote) {
        webSocketListener.onWebsocketClosing(conn, code, reason, remote);
    }

    @Override
    public void onWebsocketCloseInitiated(WebSocket conn, int code, String reason) {
        webSocketListener.onWebsocketCloseInitiated(conn, code, reason);
    }

    @Override
    public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException {
        return webSocketListener.onWebsocketHandshakeReceivedAsServer(conn, draft, request);
    }

    @Override
    public void onWebsocketHandshakeReceivedAsClient(WebSocket conn, ClientHandshake request, ServerHandshake response) throws InvalidDataException {
        webSocketListener.onWebsocketHandshakeReceivedAsClient(conn, request, response);
    }

    @Override
    public void onWebsocketHandshakeSentAsClient(WebSocket conn, ClientHandshake request) throws InvalidDataException {
        webSocketListener.onWebsocketHandshakeSentAsClient(conn, request);
    }

    @Override
    public InetSocketAddress getLocalSocketAddress(WebSocket conn) {
        return webSocketListener.getLocalSocketAddress(conn);
    }

    @Override
    public InetSocketAddress getRemoteSocketAddress(WebSocket conn) {
        return webSocketListener.getRemoteSocketAddress(conn);
    }

    // Start a timer to send ping frames periodically
    private void startPingTimer() {
        pingTimer = new Timer();
        pingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isOpen()) {
                    sendPing(); // Send ping frame
                }
            }
        }, PING_INTERVAL, PING_INTERVAL);
    }

    // Stop the ping timer
    private void stopPingTimer() {
        if (pingTimer != null) {
            pingTimer.cancel();
            pingTimer = null;
        }
    }

    // Attempt to reconnect after a delay
    private void attemptReconnect() {
        if (reconnectAttempts.incrementAndGet() <= MAX_RECONNECT_ATTEMPTS) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    try{
                        reconnect();
                    }
                    catch (Exception e){
                        webSocketListener.onWebsocketError(SimpleWebSocket.this , e);
                    }
                }
            }, RECONNECT_DELAY);
        } else {
            webSocketListener.onWebsocketError(this, new Exception("Max reconnect attempts reached"));
        }
    }

    public boolean isOpen() {
        return super.isOpen();
    }

    public boolean isClosed() {
        return super.isClosed();
    }

    public boolean isClosing() {
        return super.isClosing();
    }

    // Method to reconnect the WebSocket
    public void reconnect() {
        close();
        connect();
    }

    // Blocking reconnect method
    public boolean reconnectBlocking() throws InterruptedException {
        closeBlocking();
        connectBlocking();
        return isOpen();
    }
}