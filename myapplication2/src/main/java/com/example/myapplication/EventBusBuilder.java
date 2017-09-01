package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Howard on 2017/8/31.
 */
public class EventBusBuilder {

    private final static ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    boolean ignoreGeneratedIndex;
    boolean strictMethodVerification;
    boolean eventInheritance;
    boolean logSubscriberExceptions;
    public boolean logNoSubscriberMessage;
    public boolean sendSubscriberExceptionEvent;
    public boolean sendNoSubscriberEvent;
    public boolean throwSubscriberException;

    List<SubscriberInfoIndex> subscriberInfoIndexes;
    ExecutorService executorService = DEFAULT_EXECUTOR_SERVICE;
    List<Class<?>> skipMethodVerificationForClasses;

    EventBusBuilder(){}

    public EventBusBuilder logSubscriberExceptions(boolean logSubscriberExceptions){
        this.logSubscriberExceptions = logSubscriberExceptions;
        return this;
    }

    public EventBusBuilder logNoSubscriberMessage(boolean logNoSubscriberMessage){
        this.logNoSubscriberMessage = logNoSubscriberMessage;
        return this;
    }

    public EventBusBuilder sendSubscriberExceptionEvent(boolean sendSubscriberExceptionEvent){
        this.sendSubscriberExceptionEvent = sendSubscriberExceptionEvent;
        return this;
    }

    public EventBusBuilder sendNoSubscriberEvent(boolean sendNoSubscriberEvent){
        this.sendNoSubscriberEvent = sendNoSubscriberEvent;
        return this;
    }

    public EventBusBuilder throwSubscriberException(boolean throwSubscriberException){
        this.throwSubscriberException = throwSubscriberException;
        return this;
    }

    public EventBusBuilder eventInheritance(boolean eventInheritance){
        this.eventInheritance = eventInheritance;
        return this;
    }

    public EventBusBuilder executorService(ExecutorService executorService){
        this.executorService = executorService;
        return this;
    }

    public EventBusBuilder skipMethodVerificatiionFor(Class<?> clazz){
        if(skipMethodVerificationForClasses == null){
            skipMethodVerificationForClasses = new ArrayList<>();
        }
        skipMethodVerificationForClasses.add(clazz);
        return this;
    }

    public EventBusBuilder strictMethodVerification(boolean strictMethodVerification){
        this.strictMethodVerification = strictMethodVerification;
        return this;
    }

    public EventBusBuilder addIndex(SubscriberInfoIndex index){
        if(subscriberInfoIndexes == null){
            subscriberInfoIndexes = new ArrayList<>();
        }
        subscriberInfoIndexes.add(index);
        return this;
    }

    public EventBus installDefaultEventBus(){
        synchronized (EventBus.class){
            if(EventBus.defaultInstance != null){
                throw new EventBusException("Default instance already exist." +
                        " It may be only set once before it's used the first time to ensure consistent behavior");

            }
            EventBus.defaultInstance = build();
            return EventBus.defaultInstance;
        }
    }

    public EventBus build(){
        return new EventBus(this);
    }


}
