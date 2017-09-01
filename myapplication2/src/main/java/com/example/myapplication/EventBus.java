package com.example.myapplication;

import android.os.Looper;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

/**
 * Created by Howard on 2017/8/31.
 */
public class EventBus {

    static volatile EventBus defaultInstance;
    private final Map<Class<?>, CopyOnWriteArrayList<Subscription>> subscriptionsByEventType;
    private final Map<Object, List<Class<?>>> typesBySubscriber;
    private final Map<Class<?>, Object> stickyEvents;

    private static final Map<Class<?>, List<Class<?>>> eventTypesCache = new HashMap<>();

    private final HandlerPoster mainThreadPoster;
    private final BackgroundPoster backgroundPoster;
    private final AsyncPoster asyncPoster;
    private final SubscribeMethodFinder subscriberMethodFinder;
    private final ExecutorService executorService;

    private final boolean logSubscriberExceptions;
    private final boolean logNoSubscriberMessages;
    private final boolean sendSubscriberExceptionEvent;
    private final boolean sendNoSubscriberEvent;
    private final boolean throwSubscriberException;
    boolean eventInheritance;

    private final ThreadLocal<PostingThreadState> currentPostingThreadState = new ThreadLocal<PostingThreadState>(){
        @Override
        protected PostingThreadState initialValue() {
            return new PostingThreadState();
        }
    };


    private final int indexCount;

    private static final EventBusBuilder DEFAULT_BUILDER = new EventBusBuilder();

    public EventBus(){
        this(DEFAULT_BUILDER);
    }

    EventBus(EventBusBuilder builder){
        subscriptionsByEventType = new HashMap<>();
        typesBySubscriber = new HashMap<>();
        stickyEvents = new ConcurrentHashMap<>();
        mainThreadPoster = new HandlerPoster(this, Looper.getMainLooper(), 10);
        backgroundPoster = new BackgroundPoster(this);
        asyncPoster = new AsyncPoster(this);
        indexCount = builder.subscriberInfoIndexes != null ? builder.subscriberInfoIndexes.size() : 0;
        subscriberMethodFinder = new SubscribeMethodFinder(builder.subscriberInfoIndexes, builder.strictMethodVerification, builder.ignoreGeneratedIndex);
        logSubscriberExceptions = builder.logSubscriberExceptions;
        logNoSubscriberMessages = builder.logNoSubscriberMessage;
        sendSubscriberExceptionEvent = builder.sendSubscriberExceptionEvent;
        sendNoSubscriberEvent = builder.sendNoSubscriberEvent;
        throwSubscriberException = builder.throwSubscriberException;
        eventInheritance = builder.eventInheritance;
        executorService = builder.executorService;
    }

    public static EventBus getDefault(){
        if(defaultInstance == null){
            synchronized (EventBus.class){
                if(defaultInstance == null){
                    defaultInstance = new EventBus();
                }
            }
        }
        return defaultInstance;
    }

    public void register(Object subscriber){
        Class<?> subscriberCLass = subscriber.getClass();
        List<SubscriberMethod> subscriberMethods = subscriberMethodFinder.findSubscriberMethods(subscriberCLass);
        synchronized (this){
            for(SubscriberMethod subscriberMethod : subscriberMethods){
                subscribe(subscriber, subscriberMethod);
            }
        }
    }

    public void subscribe(Object subscriber, SubscriberMethod subscriberMethod){
        Class<?> eventType = subscriberMethod.eventType;
        Subscription newSubscription = new Subscription(subscriber, subscriberMethod);
        CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
        if(subscriptions == null){
            subscriptions = new CopyOnWriteArrayList<>();
            subscriptionsByEventType.put(eventType, subscriptions);
        }else{
            if(subscriptions.contains(newSubscription)){
                throw new EventBusException("Subscriber " + subscriber.getClass() + " already registered to event" + eventType);
            }
        }

        int size = subscriptions.size();
        for(int i=0; i<=size; i++){
            if(i == size || subscriberMethod.priority > subscriptions.get(i).subscriberMethod.priority){
                subscriptions.add(i, newSubscription);
                break;
            }
        }

        List<Class<?>> subscribedEvents = typesBySubscriber.get(subscriber);
        if(subscribedEvents == null){
            subscribedEvents = new ArrayList<>();
            typesBySubscriber.put(subscriber, subscribedEvents);
        }

        subscribedEvents.add(eventType);
        if(subscriberMethod.sticky){
            if(eventInheritance){
                Set<Map.Entry<Class<?>, Object>> enties = stickyEvents.entrySet();
                for(Map.Entry<Class<?>, Object> entry : enties){
                    Class<?> candidateEventType = entry.getKey();
                    if(eventType.isAssignableFrom(candidateEventType)){
                        Object stickyEvent = entry.getValue();
                        checkPostStickyEventToSubscription(newSubscription, stickyEvent);
                    }
                }
            }else{
                Object stickyEvent = stickyEvents.get(eventType);
                checkPostStickyEventToSubscription(newSubscription, stickyEvent);
            }
        }
    }

    private void checkPostStickyEventToSubscription(Subscription newSubscripiton, Object stickyEvent){
        if(stickyEvent != null){
            postToSubscription(newSubscripiton, stickyEvent, Looper.getMainLooper() == Looper.myLooper());
        }
    }

    private void postToSubscription(Subscription subscription, Object event, boolean isMainThread){
        switch(subscription.subscriberMethod.threadMode){
            case POSTING:
                invokeSubscriber(subscription, event);
                break;
            case MAIN:
                if(isMainThread){
                    invokeSubscriber(subscription, event);
                }else{
                    mainThreadPoster.enqueue(subscription, event);
                }
                break;
            case BACKGROUND:
                if(isMainThread){
                    backgroundPoster.enqueue(subscription,event);
                }else{
                    invokeSubscriber(subscription, event);
                }
                break;
            case ASYNC:
                asyncPoster.enqueue(subscription, event);
                break;
            default:
                throw new IllegalStateException("Unkonw thread mode: " + subscription.subscriberMethod.threadMode);
        }
    }

    void invokeSubscriber(PendingPost pendingPost){
        Object event = pendingPost.event;
        Subscription subscription = pendingPost.subscription;
        PendingPost.releasePendingPost(pendingPost);
        if(subscription.active){
            invokeSubscriber(subscription, event);
        }
    }

    void invokeSubscriber(Subscription subscription, Object event){
        try{
            subscription.subscriberMethod.method.invoke(subscription.subscriber, event);
        }catch (InvocationTargetException e){
            handleSubscriberException(subscription, event, e.getCause());
        }catch (IllegalAccessException e){
            throw new IllegalStateException("Unexpected exception", e);
        }
    }

    private void handleSubscriberException(Subscription subscription, Object event, Throwable cause){
        if(event instanceof SubscriberExceptionEvent){
            LogUtils.hLog().e("SubscriberExceptionEvent subscriber " + subscription.subscriber.getClass() + " throw an exception", cause);
            SubscriberExceptionEvent exEvent = (SubscriberExceptionEvent) event;
            LogUtils.hLog().e("Initial event " + exEvent.causingEvent + " caused exception in " + exEvent.causingSubscriber, exEvent.throwable);
        }else{
            if(throwSubscriberException){
                throw new EventBusException("Invoking subscriber failed", cause);
            }
            if(logSubscriberExceptions){
                LogUtils.hLog().e("Could not dispatch event: " + event.getClass() + " to subscribing class " + subscription.subscriber.getClass(), cause);
            }
            if(sendSubscriberExceptionEvent){
                SubscriberExceptionEvent exEvent = new SubscriberExceptionEvent(this, cause, event, subscription.subscriber);
                post(exEvent);
            }
        }
    }

    ExecutorService getExecutorService(){
        return executorService;
    }

    public void post(Object event){
        PostingThreadState postingState = currentPostingThreadState.get();
        List<Object> eventQueue = postingState.eventQueue;
        eventQueue.add(event);
        if(!postingState.isPosting){
            postingState.isMainThread = Looper.getMainLooper() == Looper.myLooper();
            postingState.isPosting = true;
            if(postingState.canceled){
                throw new EventBusException("Internal error, Abort state was not reset");
            }
            try{
                while(!eventQueue.isEmpty()){
                    postSingleEvent(eventQueue.remove(0), postingState);
                }
            }finally {
                postingState.isPosting = false;
                postingState.isMainThread = false;
            }
        }
    }

    private void postSingleEvent(Object event, PostingThreadState postingState) throws Error{
        Class<?> eventClass = event.getClass();
        boolean subscriptionFound = false;
        if(eventInheritance){
            List<Class<?>> eventTypes = lookupAllEventTypes(eventClass);
            int countTypes = eventTypes.size();
            for(int h=0; h<countTypes; h++){
                Class<?> clazz = eventTypes.get(h);
                subscriptionFound |= postSingleEventForEventType(event, postingState, clazz);
            }
        }else{
            subscriptionFound = postSingleEventForEventType(event, postingState, eventClass);
        }
        if(!subscriptionFound){
            if(logNoSubscriberMessages){
                LogUtils.hLog().d("No subscribers registered for event " + eventClass);
            }
            if(sendNoSubscriberEvent && eventClass != NoSubscriberEvent.class && eventClass != SubscriberExceptionEvent.class){
                post(new NoSubscriberEvent(this, event));
            }
        }
    }

    private boolean postSingleEventForEventType(Object event, PostingThreadState postingState, Class<?> eventClass){
        CopyOnWriteArrayList<Subscription> subscriptions;
        synchronized (this){
            subscriptions = subscriptionsByEventType.get(eventClass);
        }
        if(subscriptions != null && !subscriptions.isEmpty()){
            for(Subscription subscription : subscriptions){
                postingState.event = event;
                postingState.subscription = subscription;
                boolean aborted = false;
                try{
                    postToSubscription(subscription, event, postingState.isMainThread);
                    aborted = postingState.canceled;
                }finally {
                    postingState.event = null;
                    postingState.subscription = null;
                    postingState.canceled = false;
                }
                if(aborted){
                    break;
                }
            }
            return true;
        }
        return false;
    }

    private static List<Class<?>> lookupAllEventTypes(Class<?> eventClass){
        synchronized (eventTypesCache){
            List<Class<?>> eventTypes = eventTypesCache.get(eventClass);
            if(eventTypes == null){
                eventTypes = new ArrayList<>();
                Class<?> clazz = eventClass;
                while(clazz != null){
                    eventTypes.add(clazz);
                    addInterfaces(eventTypes, clazz.getInterfaces());
                    clazz = clazz.getSuperclass();
                }
                eventTypesCache.put(eventClass, eventTypes);
            }
            return eventTypes;
        }
    }

    static void addInterfaces(List<Class<?>> eventTypes, Class<?>[] interfaces){
        for(Class<?> interfaceClass : interfaces){
            if(!eventTypes.contains(interfaceClass)){
                eventTypes.add(interfaceClass);
                addInterfaces(eventTypes, interfaceClass.getInterfaces());
            }
        }
    }

    final static class PostingThreadState{
        final List<Object> eventQueue = new ArrayList<>();
        boolean isPosting;
        boolean isMainThread;
        Subscription subscription;
        Object event;
        boolean canceled;
    }

    @Override
    public String toString() {
        return "EventBus[indexCount=" + indexCount + " ,eventInheritance = " + eventInheritance + "]";
    }
}
