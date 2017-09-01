package com.example.myapplication;

/**
 * Created by Howard on 2017/9/1.
 */
public final class NoSubscriberEvent {
    public final EventBus eventBus;
    public final Object originalEvent;

    public NoSubscriberEvent(EventBus eventBus, Object originalEvent){
        this.eventBus =eventBus;
        this.originalEvent = originalEvent;
    }
}
