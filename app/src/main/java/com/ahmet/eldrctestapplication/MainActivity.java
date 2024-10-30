package com.ahmet.eldrctestapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.ahmet.eldrctestapplication.communication.RobotMessageBuilder;
import com.ahmet.eldrctestapplication.databinding.ActivityMainBinding;
import com.ahmet.eldrctestapplication.log.LogAdapter;
import com.ahmet.eldrctestapplication.log.LogEntry;
import com.ahmet.eldrctestapplication.virtual_joystick.JoystickView;
import com.ahmet.eldrctestapplication.websocket.WebSocketManager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    JoystickView virtualLeftJoystick;
    LogAdapter logAdapter;
    WebSocketManager webSocketManager;

    private static final String url = "ws://192.168.3.2:2005";

    int leftMotorsSpeed = 0;
    int rightMotorsSpeed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        logAdapter = new LogAdapter(this, new ArrayList<>(), binding.logListView);
        binding.logListView.setAdapter(logAdapter);



        virtualLeftJoystick = binding.virtualJoystickLeft;
        virtualLeftJoystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {

                int y = virtualLeftJoystick.getNormalizedY();
                int x = virtualLeftJoystick.getNormalizedX();

                leftMotorsSpeed = y + x;
                rightMotorsSpeed = y - x;

                leftMotorsSpeed = Math.max(-100, Math.min(100, leftMotorsSpeed));
                rightMotorsSpeed = Math.max(-100, Math.min(100, rightMotorsSpeed));

                sendMessage(RobotMessageBuilder.createMotorSpeedPacket(RobotMessageBuilder.Transmitter.MASTER , RobotMessageBuilder.Receiver.ALL , RobotMessageBuilder.Component.MOTOR_DRIVE ,
                        leftMotorsSpeed , leftMotorsSpeed , leftMotorsSpeed , rightMotorsSpeed ,rightMotorsSpeed , rightMotorsSpeed));
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        connectWebSocket();
    }

    @Override
    protected void onStop() {
        super.onStop();
        disconnectWebSocket();
    }

    private void connectWebSocket() {
        if (url.isEmpty()) {
            logAdapter.log(LogEntry.LogType.ERROR, "WebSocket URL is empty");
            return;
        }

        // Perform URL validation
        if (!isValidWebSocketUrl(url)) {
            logAdapter.log(LogEntry.LogType.ERROR, "Invalid WebSocket URL");
            return;
        }


        try {
            webSocketManager = new WebSocketManager(url, logAdapter);
            webSocketManager.connect();
        } catch (Exception e) {
            logAdapter.log(LogEntry.LogType.ERROR, "Failed to create WebSocketManager: " + e.getMessage());
        }
    }

    private void disconnectWebSocket() {
        if (webSocketManager != null && webSocketManager.isSocketOpen()) {
            webSocketManager.disconnect();
            logAdapter.log(LogEntry.LogType.INFO, "WebSocket disconnected");
        }
    }

    private boolean isValidWebSocketUrl(String url) {
        // Simple URL validation logic
        return url.startsWith("ws://") || url.startsWith("wss://");
    }

    private void sendMessage(String message) {
        if (message.isEmpty()) {
            logAdapter.log(LogEntry.LogType.ERROR, "Message is empty");
            return;
        }

        if (webSocketManager == null || !webSocketManager.isSocketOpen()) {
            logAdapter.log(LogEntry.LogType.ERROR, "WebSocket is not connected \n" + "Message: " + message);
            return;
        }

        try {
            webSocketManager.sendMessage(message);
        } catch (Exception e) {
            logAdapter.log(LogEntry.LogType.ERROR, "Failed to send message: " + e.getMessage());
        }
    }
}