package yj.eagle.message.exception;

import yj.eagle.message.MessageDecoder;
import yj.eagle.message.MessageMetaData;

/**
 * @author zwk
 * @version 1.0
 * @date 2023/8/16 17:23
 */

public class ExceptionMessageDecoder implements MessageDecoder<ExceptionMessage> {
    @Override
    public ExceptionMessage decode(byte[] msg, MessageMetaData metaData) {
        return new ExceptionMessage(new String(msg));
    }
}
