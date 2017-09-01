package com.example.myapplication;

import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Howard on 2017/8/31.
 */
class SubscribeMethodFinder{
    private List<SubscriberInfoIndex> subscriberInfoIndexes;
    private final boolean strictMethodVerification;
    private final boolean ignoreGeneratedIndex;

    private static final int POOL_SIZE = 4;
    private static final FindState[] FIND_STATE_POOL = new FindState[POOL_SIZE];

    private static final int BRIDGE = 0x40;
    private static final int SYNTHETIC = 0x1000;

    private static final int MODIFIERS_IGNORE = Modifier.ABSTRACT | Modifier.STATIC | BRIDGE | SYNTHETIC;

    private static final Map<Class<?>, List<SubscriberMethod>> METHOD_CACHE = new ConcurrentHashMap<>();

    SubscribeMethodFinder(List<SubscriberInfoIndex> subscriberInfoIndexes, boolean strictMethodVerification, boolean ignoreGeneratedIndex){
        this.subscriberInfoIndexes = subscriberInfoIndexes;
        this.strictMethodVerification = strictMethodVerification;
        this.ignoreGeneratedIndex = ignoreGeneratedIndex;
    }
    List<SubscriberMethod> findSubscriberMethods(Class<?> subscriberClass){
        List<SubscriberMethod> subscriberMethods = METHOD_CACHE.get(subscriberClass);
        if(subscriberMethods != null){
            return subscriberMethods;
        }
        if(ignoreGeneratedIndex){
            subscriberMethods = findUsingReflection(subscriberClass);
        }else{
            subscriberMethods = findUsingInfo(subscriberClass);
        }
        if(subscriberMethods.isEmpty()){
            throw new EventBusException("subscriber " + subscriberClass + " and its super class have no public methods with the @subscribe anomation");
        }else{
            METHOD_CACHE.put(subscriberClass, subscriberMethods);
            return subscriberMethods;
        }
    }

    private List<SubscriberMethod> findUsingReflection(Class<?> subscriberClass){
        FindState findState = prepareFindState();
        findState.initForSubscriber(subscriberClass);
        while(findState.clazz != null){
            findUsingReflectionInSingleClass(findState);
            findState.moveToSuperclass();
        }
        return getMethodsAndRelease(findState);
    }

    private List<SubscriberMethod> findUsingInfo(Class<?> subscriberClass){
        FindState findState = prepareFindState();
        findState.initForSubscriber(subscriberClass);
        while(findState.clazz != null){
            findState.subscriberInfo = getSubscriberInfo(findState);
            if(findState.subscriberInfo != null){
                SubscriberMethod[] array = findState.subscriberInfo.getSubscriberMethods();
                for(SubscriberMethod subscriberMethod : array){
                    if(findState.checkAdd(subscriberMethod.method, subscriberMethod.eventType)){
                        findState.subscriberMethods.add(subscriberMethod);
                    }
                }
            }else{
                findUsingReflectionInSingleClass(findState);
            }
            findState.moveToSuperclass();
        }
        return getMethodsAndRelease(findState);
    }

    private List<SubscriberMethod> getMethodsAndRelease(FindState findState){
        List<SubscriberMethod> subscriberMethods = new ArrayList<>(findState.subscriberMethods);
        findState.recycle();
        synchronized (FIND_STATE_POOL){
            for(int i=0; i<POOL_SIZE; i++){
                if(FIND_STATE_POOL[i] == null){
                    FIND_STATE_POOL[i] = findState;
                    break;
                }
            }
        }
        return subscriberMethods;
    }

    private SubscriberInfo getSubscriberInfo(FindState findState){
        if(findState.subscriberInfo != null && findState.subscriberInfo.getSuperSubscriberInfo() != null){
            SubscriberInfo superclassInfo = findState.subscriberInfo.getSuperSubscriberInfo();
            if(findState.clazz == superclassInfo.getSubscriberClass()){
                return superclassInfo;
            }
        }
        if(subscriberInfoIndexes != null){
            for(SubscriberInfoIndex index : subscriberInfoIndexes){
                SubscriberInfo info = index.getSubscriberInfo(findState.clazz);
                if(info != null){
                    return info;
                }
            }
        }
        return null;
    }

    private FindState prepareFindState(){
        synchronized (FIND_STATE_POOL){
            for(int i=0; i<POOL_SIZE; i++){
                FindState state = FIND_STATE_POOL[i];
                if(state != null){
                    FIND_STATE_POOL[i] = null;
                    return state;
                }
            }
        }
        return new FindState();
    }

    /**
     ** 利用反射对订阅者进行扫描，找出订阅方法，并用FindState中的Map保存
     ** getMethod获取该类以及父类的所有public方法，
     ** getDeclaredMethod获取当前类的所有方法，包括public/private/protected/default修饰的方法
     **/
    private void findUsingReflectionInSingleClass(FindState findState){
        Method[] methods;
        try{
            methods = findState.clazz.getDeclaredMethods();
        }catch(Throwable th){
            methods = findState.clazz.getMethods();
            findState.skipSuperClasses = true;
        }
        for(Method method : methods){
            int modifier = method.getModifiers();
            if((modifier & Modifier.PUBLIC) != 0 && (modifier & MODIFIERS_IGNORE) == 0){
                Class<?>[] parameterTypes = method.getParameterTypes();
                if(parameterTypes.length == 1){
                    Subscribe subscribeAnnotation = method.getAnnotation(Subscribe.class);
                    if(subscribeAnnotation != null){
                        Class<?> eventType = parameterTypes[0];
                        if(findState.checkAdd(method, eventType)){
                            ThreadMode threadMode = subscribeAnnotation.threadMode();
                            findState.subscriberMethods.add(new SubscriberMethod(method, eventType, threadMode,
                                    subscribeAnnotation.priority(), subscribeAnnotation.sticky()));
                        }
                    }
                }else if(strictMethodVerification && method.isAnnotationPresent(Subscribe.class)){
                    String methodName = method.getDeclaringClass().getName() + "." + method.getName();
                    throw new EventBusException("@Subscribe method " + methodName + " must have exactly 1 parameter but has " + parameterTypes.length);
                }
            }else if(strictMethodVerification && method.isAnnotationPresent(Subscribe.class)){
                String methodName = method.getDeclaringClass().getName() + "." + method.getName();
                throw new EventBusException(methodName + " is a illegal @Subscribe method: must be public, non-static, and non-abstract");
            }
        }
    }

    static class FindState{
        final List<SubscriberMethod> subscriberMethods = new ArrayList<>();
        final Map<Class, Object> anyMethodByEventType = new HashMap<>();
        final Map<String, Class> subscriberClassByMethodKey = new HashMap<>();
        final StringBuilder methodKeyBuilder = new StringBuilder(128);
        Class<?> subscriberClass;
        Class<?> clazz;
        boolean skipSuperClasses;
        SubscriberInfo subscriberInfo;

        void initForSubscriber(Class<?> subscriberClass){
            this.subscriberClass = clazz = subscriberClass;
            skipSuperClasses = false;
            subscriberInfo = null;
        }

        void recycle(){
            subscriberMethods.clear();
            anyMethodByEventType.clear();
            subscriberClassByMethodKey.clear();
            methodKeyBuilder.setLength(0);
            subscriberClass = null;
            clazz = null;
            skipSuperClasses = false;
            subscriberClass = null;
        }

        /**
         * 两步校验订阅事件1
         * 判断eventType类型，将eventType放入Map中，返回值决定是否该事件被其他方法订阅了，
         * 如果没有被订阅则返回，如果已经被订阅则进行第二步校验
         *
         * @param method
         * @param eventType
         * @return
         */
        boolean checkAdd(Method method, Class<?> eventType){
            Object existing = anyMethodByEventType.put(eventType, method);
            if(existing == null){
                return true;
            }else{
                if(existing instanceof Method){
                    if(!checkAddWithMethodSignature((Method)existing, eventType)){
                        throw new IllegalStateException();
                    }
                    anyMethodByEventType.put(eventType, this);
                }
                return checkAddWithMethodSignature(method, eventType);
            }
        }

        /**
         * 两步校验订阅事件2
         * 如果事件已经被其他方法订阅了，则继续进行校验
         *
         *
         * @param method
         * @param eventType
         * @return
         */
        private boolean checkAddWithMethodSignature(Method method, Class<?> eventType){
            methodKeyBuilder.setLength(0);
            methodKeyBuilder.append(method.getName());
            methodKeyBuilder.append('>').append(eventType.getName());
            String methodKey = methodKeyBuilder.toString();
            Class<?> methodClass = method.getDeclaringClass();
            Class<?> methodClassOld = subscriberClassByMethodKey.put(methodKey, methodClass);
            if(methodClassOld == null || methodClassOld.isAssignableFrom(methodClass)){
                return true;
            }else{
                subscriberClassByMethodKey.put(methodKey, methodClassOld);
                return false;
            }
        }

        void moveToSuperclass(){
            if(skipSuperClasses){
                clazz = null;
            }else{
                clazz = clazz.getSuperclass();
                String clazzName = clazz.getName();
                if(clazzName.startsWith("java.") || clazzName.startsWith("javax.") || clazzName.startsWith("android.")){
                    clazz = null;
                }
            }
        }




     }
}