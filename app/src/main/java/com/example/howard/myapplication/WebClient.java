package com.example.howard.myapplication;

import android.util.Log;

import com.google.gson.Gson;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

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
        Log.i("howard", "Open Connection");
        if(null != mISocketListener){
            mISocketListener.onSocketOpen(handshakedata);
        }
    }

    @Override
    public void onMessage(String message) {
        Log.i("howard", "Receive Message = " + message);
        Gson json = new Gson();
        WechatUserBean bean = json.fromJson(message, WechatUserBean.class);
        if(null != mISocketListener){
            mISocketListener.onReceiveMessage(bean);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.i("howard", "Close Connection");
        if(null != mISocketListener){
            mISocketListener.onSocketClose(code, reason, remote);
        }
    }

    @Override
    public void onError(Exception ex) {
        Log.i("howard", "Error Connection");
        if(null != mISocketListener){
            mISocketListener.onSocketError(ex);
        }
    }
}
