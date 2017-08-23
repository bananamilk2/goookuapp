package com.example.howard.myapplication;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Map;

/**
 * Created by Howard on 2017/8/23.
 */
public class WebClient extends WebSocketClient {

    private ISocketListener mISocketListener;
    public WebClient(URI serverUri) {
        super(serverUri);
    }

    public WebClient(URI serverUri, Draft protocolDraft) {
        super(serverUri, protocolDraft);
    }

    public WebClient(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders, int connectTimeout) {
        super(serverUri, protocolDraft, httpHeaders, connectTimeout);
    }

    public WebClient(URI serverUri, ISocketListener aListener) {
        super(serverUri);
        mISocketListener = aListener;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.i("WebClient", "Open Connection");
        if(null != mISocketListener){
            mISocketListener.onSocketOpen(handshakedata);
        }
    }

    @Override
    public void onMessage(String message) {
        Log.i("WebClient", "Receive Message = " + message);
        if(null != mISocketListener){
            mISocketListener.onReceiveMessage(message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.i("WebClient", "Close Connection");
        if(null != mISocketListener){
            mISocketListener.onSocketClose(code, reason, remote);
        }
    }

    @Override
    public void onError(Exception ex) {
        Log.i("WebClient", "Error Connection");
        if(null != mISocketListener){
            mISocketListener.onSocketError(ex);
        }
    }
}
