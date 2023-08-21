package yj.eagle.message;

import yj.eagle.utils.JacksonUtil;

/**
 * @author zwk
 * @version 1.0
 * @date 2023/8/18 9:21
 */

public class DefaultMessageDecoder implements MessageDecoder<Object> {
    @Override
    public Object decode(byte[] msg, MessageMetaData metaData) {
        return JacksonUtil.bytesToObj(msg, metaData.getMessageClass());
    }
}
