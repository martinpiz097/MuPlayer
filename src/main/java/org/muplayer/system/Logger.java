package org.muplayer.system;

public class Logger {

    public static Logger getLogger(String className, String title, Object msg) {
        return new Logger(className, title, msg);
    }

    public static Logger getLogger(Class clazz, String title, Object msg) {
        return new Logger(clazz, title, msg);
    }

    public static Logger getLogger(String className, Object msg) {
        return new Logger(className, msg);
    }

    public static Logger getLogger(Class clazz, Object msg) {
        return new Logger(clazz, msg);
    }

    public static Logger getLogger(Object classObject, Object msg) {
        return new Logger(classObject.getClass(), msg);
    }

    public static Logger getLogger(Object classObject, String title, Object msg) {
        return new Logger(classObject.getClass(), title, msg);
    }

    public static void showLogger(String className, String title, Object msg) {
        new Logger(className, title, msg).printMsg();
    }

    public static void showLogger(Class clazz, String title, Object msg) {
        new Logger(clazz, title, msg).printMsg();
    }

    public static void showLogger(String className, Object msg) {
        new Logger(className, msg).printMsg();
    }

    public static void showLogger(Class clazz, Object msg) {
        new Logger(clazz, msg).printMsg();
    }

    public static void showLogger(Object classObject, Object msg) {
        new Logger(classObject.getClass().getSimpleName(), msg).printMsg();
    }

    public static void showLogger(Object classObject, String title, Object msg) {
        new Logger(classObject.getClass().getSimpleName(), title, msg).printMsg();
    }

    public static enum TYPE {
        INFO, WARNING, ERROR;
    }

    private String className;
    private String title; // Optional
    private String msg;

    public Logger(String className, String title, Object msg) {
        this.className = className;
        this.title = title;
        this.msg = String.valueOf(msg);
    }

    public Logger(Class clazz, String title, Object msg) {
        this(clazz.getSimpleName(), title, msg);
    }

    public Logger(String className, Object msg) {
        this(className, null, msg);
    }

    public Logger(Class clazz, Object msg) {
        this(clazz.getSimpleName(), msg);
    }

    private StringBuilder getWriter(String color) {
        return new StringBuilder().append(color);
    }

    private String getMsg(StringBuilder sbMsg) {
        //sbMsg.append('[').append(getCurrentTime()).append("] ");
        sbMsg.append(className).append("-> ");
        if (title != null)
            sbMsg.append(title).append(": ");
        sbMsg.append(msg);
        sbMsg.append(ConsoleColor.RESET).append('\n');
        return sbMsg.toString();
    }

    private String getRawMsg(StringBuilder sbMsg) {
        return sbMsg.append(msg).append(ConsoleColor.RESET).append('\n').toString();
    }

    public void error() {
        System.out.println(getMsg(getWriter(ConsoleColor.ANSI_RED)));
    }

    public void info() {
        System.out.println(getMsg(getWriter(ConsoleColor.ANSI_CYAN)));
    }

    public void warning() {
        System.out.println(getMsg(getWriter(ConsoleColor.YELLOW)));
    }

    public void rawError() {
        System.out.println(getRawMsg(getWriter(ConsoleColor.ANSI_RED)));
    }

    public void rawInfo() {
        System.out.print(getRawMsg(getWriter(ConsoleColor.ANSI_CYAN)));
    }

    public void rawWarning() {
        System.out.print(getRawMsg(getWriter(ConsoleColor.YELLOW)));
    }

    public void printMsg() {
        info();
    }

    public void printRawMsg() {
        rawInfo();
    }

    public void printMsg(TYPE type) {
        switch (type) {
            case INFO:
                info();
                break;
            case ERROR:
                error();
                break;
            case WARNING:
                warning();
                break;
        }
    }

    public void printRawMsg(TYPE type) {
        switch (type) {
            case INFO:
                rawInfo();
                break;
            case ERROR:
                rawError();
                break;
            case WARNING:
                rawWarning();
                break;
        }
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
