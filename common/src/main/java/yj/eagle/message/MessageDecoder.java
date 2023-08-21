package yj.eagle.message;

public interface MessageDecoder<T> {

    T decode(byte[] msg,MessageMetaData metaData);
}
