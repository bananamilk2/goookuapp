package com.example.howard.myapplication;

import org.java_websocket.handshake.ServerHandshake;

/**
 * Created by Howard on 2017/8/23.
 */
public interface ISocketListener {
    void onReceiveMessage(String msg);

    void onSocketClose(int code, String reason, boolean remote);

    void onSocketError(Exception ex);

    void onSocketOpen(ServerHandshake handshakedata);
}
