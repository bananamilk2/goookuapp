package com.example.myapplication;

/**
 * Created by Howard on 2017/8/31.
 */
public class EventBusException  extends RuntimeException{
//    private static f

    public EventBusException(String detailMessage){
        super(detailMessage);
    }

    public EventBusException(Throwable throwable){
        super(throwable);
    }

    public EventBusException(String detailMessage, Throwable throwable){
        super(detailMessage, throwable);
    }
}
