package yj.eagle.message;

import yj.eagle.utils.JacksonUtil;

/**
 * @author zwk
 * @version 1.0
 * @date 2023/8/18 9:20
 */

public class DefaultMessageEncoder implements MessageEncoder<Object> {
    @Override
    public byte[] encode(Object msg) {
        return JacksonUtil.objToBytes(msg);
    }
}
