package yj.eagle.message;

public interface MessageHandler {
    boolean support(Class<?> msgClass);

    void handleMessage(Object msg, MessageCallback callback);
}
