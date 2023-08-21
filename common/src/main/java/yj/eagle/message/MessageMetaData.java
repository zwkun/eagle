package yj.eagle.message;

/**
 * @author zwk
 * @version 1.0
 * @date 2023/8/18 9:29
 */

public interface MessageMetaData {

    String getMessageName();

    Class<?> getMessageClass();

}
