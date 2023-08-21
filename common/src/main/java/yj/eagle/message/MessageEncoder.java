package yj.eagle.message;

public interface MessageEncoder<T> {

    byte[] encode(T msg);
}
