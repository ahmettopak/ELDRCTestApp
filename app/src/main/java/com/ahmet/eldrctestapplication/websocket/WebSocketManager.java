package com.ahmet.eldrctestapplication.websocket;

import com.ahmet.eldrctestapplication.log.LogAdapter;
import com.ahmet.eldrctestapplication.log.LogEntry;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketListener;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.PingFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.Handshakedata;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;

import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Ahmet TOPAK
 * @version 1.0
 * @since 7/23/2024
 */
public class WebSocketManager implements WebSocketListener {

    private SimpleWebSocket webSocket;
    private LogAdapter logAdapter;
    private static final int CONNECTION_TIMEOUT = 5000;
    // Executor for sending messages at fixed intervals
    private ScheduledExecutorService messageSenderExecutor;
    private static final int MESSAGE_SEND_INTERVAL = 10; // 10 milliseconds

    private int messageCount = 0;

    // Start sending messages at regular intervals
    public void startMessageSender(int interval) {
        messageSenderExecutor = Executors.newSingleThreadScheduledExecutor();
        messageSenderExecutor.scheduleWithFixedDelay(() -> {
            if (webSocket.isOpen()) {
                messageCount++;
                sendMessage("Ahmet TOPAK: " + messageCount); // Replace with your message
            }
        }, 0, interval, TimeUnit.MILLISECONDS);
    }
    public void startMessageSender(int interval , String prefix) {
        messageSenderExecutor = Executors.newSingleThreadScheduledExecutor();
        messageSenderExecutor.scheduleWithFixedDelay(() -> {
            if (webSocket.isOpen()) {
                messageCount++;
                sendMessage(prefix + messageCount); // Replace with your message
            }
        }, 0, interval, TimeUnit.MILLISECONDS);
    }

    // Stop sending messages
    public void stopMessageSender() {
        if (messageSenderExecutor != null && !messageSenderExecutor.isShutdown()) {
            messageSenderExecutor.shutdown();
        }
    }

    public WebSocketManager(String url, LogAdapter logAdapter) {
        this.logAdapter = logAdapter;
        try {
            webSocket = new SimpleWebSocket(url, this);
            webSocket.setConnectionLostTimeout(CONNECTION_TIMEOUT);
        } catch (URISyntaxException e) {
            logAdapter.log(LogEntry.LogType.ERROR, "WebSocket URI Syntax Error: " + e.getMessage());
        } catch (Exception e) {
            logAdapter.log(LogEntry.LogType.ERROR, "Unexpected Error during WebSocket initialization: " + e.getMessage());
        }
    }

    public void connect() {
        if (webSocket != null) {
            try {
                webSocket.connect();
            } catch (Exception e) {
                logAdapter.log(LogEntry.LogType.ERROR, "Failed to connect WebSocket: " + e.getMessage());
            }
        } else {
            logAdapter.log(LogEntry.LogType.ERROR, "WebSocket instance is null");
        }
    }
    public void disconnect() {
        if (webSocket != null) {
            try {
                webSocket.close(1000, "User disconnected");
                logAdapter.log(LogEntry.LogType.INFO, "WebSocket disconnected");
            } catch (Exception e) {
                logAdapter.log(LogEntry.LogType.ERROR, "Failed to disconnect WebSocket: " + e.getMessage());
            }
        } else {
            logAdapter.log(LogEntry.LogType.ERROR, "WebSocket instance is null");
        }
    }

    public void sendMessage(String message) {
        if (webSocket != null && webSocket.isOpen()) {
            try {
                webSocket.send(message);
                logAdapter.log(LogEntry.LogType.INFO, "Sent message: " + message);
            } catch (Exception e) {
                logAdapter.log(LogEntry.LogType.ERROR, "Failed to send message: " + e.getMessage());
            }
        } else {
            logAdapter.log(LogEntry.LogType.ERROR, "WebSocket is not connected");
        }
    }

    public void sendByteMessage(byte[] message) {
        if (webSocket != null && webSocket.isOpen()) {
            try {
                webSocket.send(message);
                logAdapter.log(LogEntry.LogType.INFO, "Sent byte message");
            } catch (Exception e) {
                logAdapter.log(LogEntry.LogType.ERROR, "Failed to send byte message: " + e.getMessage());
            }
        } else {
            logAdapter.log(LogEntry.LogType.ERROR, "WebSocket is not connected");
        }
    }

    public boolean isSocketOpen(){
        return webSocket.isOpen();
    }
    public SimpleWebSocket getWebSocket() {
        return webSocket;
    }

    @Override
    public void onWebsocketOpen(WebSocket conn, Handshakedata d) {
        logAdapter.log(LogEntry.LogType.INFO, "WebSocket Opened");
    }

    @Override
    public void onWebsocketClose(WebSocket ws, int code, String reason, boolean remote) {
        logAdapter.log(LogEntry.LogType.INFO, "WebSocket Closed: Code=" + code + ", Reason=" + reason + ", Remote=" + remote);
    }

    @Override
    public void onWebsocketClosing(WebSocket ws, int code, String reason, boolean remote) {
        logAdapter.log(LogEntry.LogType.INFO, "WebSocket Closing: Code=" + code + ", Reason=" + reason + ", Remote=" + remote);
    }

    @Override
    public void onWebsocketMessage(WebSocket conn, String message) {
        logAdapter.log(LogEntry.LogType.INFO, "WebSocket Message: " + message);
    }

    @Override
    public void onWebsocketMessage(WebSocket conn, ByteBuffer blob) {
        try {
            logAdapter.log(LogEntry.LogType.INFO, "WebSocket Binary Message: " + new String(blob.array(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            logAdapter.log(LogEntry.LogType.ERROR, "Failed to process binary message: " + e.getMessage());
        }
    }

    @Override
    public void onWebsocketError(WebSocket conn, Exception ex) {
        logAdapter.log(LogEntry.LogType.ERROR, "WebSocket Error: " + ex.getMessage());
    }

    @Override
    public void onWebsocketPong(WebSocket conn, Framedata f) {
        logAdapter.log(LogEntry.LogType.DEBUG, "WebSocket Pong");
    }

    @Override
    public void onWebsocketPing(WebSocket conn, Framedata f) {
        logAdapter.log(LogEntry.LogType.DEBUG, "WebSocket Ping");
    }

    @Override
    public PingFrame onPreparePing(WebSocket conn) {
        logAdapter.log(LogEntry.LogType.DEBUG, "WebSocket Prepare Ping");
        return null;
    }

    @Override
    public void onWriteDemand(WebSocket conn) {
        logAdapter.log(LogEntry.LogType.DEBUG, "WebSocket Write Demand");
    }

    @Override
    public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException {
        return null;
    }

    @Override
    public void onWebsocketHandshakeReceivedAsClient(WebSocket conn, ClientHandshake request, ServerHandshake response) {
        logAdapter.log(LogEntry.LogType.INFO, "WebSocket Handshake Received As Client");
    }

    @Override
    public void onWebsocketHandshakeSentAsClient(WebSocket conn, ClientHandshake request) {
        logAdapter.log(LogEntry.LogType.INFO, "WebSocket Handshake Sent As Client");
    }

    @Override
    public void onWebsocketCloseInitiated(WebSocket ws, int code, String reason) {
        logAdapter.log(LogEntry.LogType.INFO, "WebSocket Close Initiated: Code=" + code + ", Reason=" + reason);
    }

    @Override
    public InetSocketAddress getLocalSocketAddress(WebSocket conn) {
        logAdapter.log(LogEntry.LogType.DEBUG, "WebSocket Local Socket Address");
        return null;
    }

    @Override
    public InetSocketAddress getRemoteSocketAddress(WebSocket conn) {
        logAdapter.log(LogEntry.LogType.DEBUG, "WebSocket Remote Socket Address");
        return null;
    }
}