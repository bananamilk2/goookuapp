package com.example.howard.myapplication;

import android.util.Log;

import java.util.Hashtable;

public class LogUtils {
    /**
     * 是否打印Log
     */
    private final static boolean isEnabled = true;

    public static void i(String tag, String msg) {
        if (isEnabled) {
            Log.i(tag, msg);
        }
    }

    public static void makeLog(Object obj, String msg) {
        if (isEnabled) {
            Log.d(obj.getClass().getSimpleName(), msg);
        }
    }

    public static void makeLog(char des, Object obj, String string) {
        Class<? extends Object> clazz = obj.getClass();
        switch (des) {
            case 'e':
                Log.e(clazz.getName(), string);
                break;
            case 'w':
                Log.w(clazz.getName(), string);
                break;
            case 'd':
                Log.d(clazz.getName(), string);
                break;

            default:
                break;
        }
    }

    public final static String tag = "[GoookuAdvertisingApp]";
    private final static int logLevel = Log.VERBOSE;
    private static Hashtable<String, LogUtils> sLoggerTable = new Hashtable<String, LogUtils>();
    private String mClassName;

    private static LogUtils hLog;
    private static LogUtils blog;

    private static final String HOWARD = "@howard@ ";
    private static final String BING = "@bing@ ";
    private static final String WUDI = "@wudi@ ";


    private LogUtils(String name) {
        mClassName = name;
    }

    /**
     * @param className
     * @return
     */
    @SuppressWarnings("unused")
    private static LogUtils getLogger(String className) {
        LogUtils classLogger = (LogUtils) sLoggerTable
                .get(className);
        if (classLogger == null) {
            classLogger = new LogUtils(className);
            sLoggerTable.put(className, classLogger);
        }
        return classLogger;
    }

    /**
     * 标记用户：bing
     *
     * @return
     */
    public static LogUtils bLog() {
        if (blog == null) {
            blog = new LogUtils(BING);
        }
        return blog;
    }

    /**
     * 标记用户：howard
     *
     * @return
     */
    public static LogUtils hLog() {
        if (hLog == null) {
            hLog = new LogUtils(HOWARD);
        }
        return hLog;
    }

    /**
     * 获取当前方法名称
     *
     * @return
     */
    private String getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return null;
        }
        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }
            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }
            if (st.getClassName().equals(this.getClass().getName())) {
                continue;
            }
            return mClassName + "[ " + Thread.currentThread().getName() + ": "
                    + st.getFileName() + ":" + st.getLineNumber() + " "
                    + st.getMethodName() + " ]";
        }
        return null;
    }

    /**
     * The Log Level:i
     *
     * @param str
     */
    public void i(Object str) {
        if (isEnabled) {
            if (logLevel <= Log.INFO) {
                String name = getFunctionName();
                if (name != null) {
                    Log.i(tag, name + " - " + str);
                } else {
                    Log.i(tag, str.toString());
                }
            }
        }

    }

    /**
     * The Log Level:d
     *
     * @param str
     */
    public void d(Object str) {
        if (isEnabled) {
            if (logLevel <= Log.DEBUG) {
                String name = getFunctionName();
                if (name != null) {
                    Log.d(tag, name + " - " + str);
                } else {
                    Log.d(tag, str.toString());
                }
            }
        }
    }

    /**
     * The Log Level:V
     *
     * @param str
     */
    public void v(Object str) {
        if (isEnabled) {
            if (logLevel <= Log.VERBOSE) {
                String name = getFunctionName();
                if (name != null) {
                    Log.v(tag, name + " - " + str);
                } else {
                    Log.v(tag, str.toString());
                }
            }
        }
    }

    /**
     * The Log Level:w
     *
     * @param str
     */
    public void w(Object str) {
        if (isEnabled) {
            if (logLevel <= Log.WARN) {
                String name = getFunctionName();
                if (name != null) {
                    Log.w(tag, name + " - " + str);
                } else {
                    Log.w(tag, str.toString());
                }
            }
        }
    }

    /**
     * The Log Level:e
     *
     * @param str
     */
    public void e(Object str) {
        if (isEnabled) {
            if (logLevel <= Log.ERROR) {
                String name = getFunctionName();
                if (name != null) {
                    Log.e(tag, name + " - " + str);
                } else {
                    Log.e(tag, str.toString());
                }
            }
        }
    }

    /**
     * The Log Level:e
     *
     * @param ex
     */
    public void e(Exception ex) {
        if (isEnabled) {
            if (logLevel <= Log.ERROR) {
                Log.e(tag, "error", ex);
            }
        }
    }

    /**
     * The Log Level:e
     *
     * @param log
     * @param tr
     */
    public void e(String log, Throwable tr) {
        if (isEnabled) {
            String line = getFunctionName();
            Log.e(tag, "{Thread:" + Thread.currentThread().getName() + "}"
                    + "[" + mClassName + line + ":] " + log + "\n", tr);
        }
    }
}
