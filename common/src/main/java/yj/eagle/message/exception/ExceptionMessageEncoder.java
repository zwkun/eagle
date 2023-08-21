package yj.eagle.message.exception;

import yj.eagle.message.MessageEncoder;

import java.nio.charset.StandardCharsets;

/**
 * @author zwk
 * @version 1.0
 * @date 2023/8/16 17:23
 */

public class ExceptionMessageEncoder implements MessageEncoder<ExceptionMessage> {
    @Override
    public byte[] encode(ExceptionMessage msg) {
        return msg.getMessage().getBytes(StandardCharsets.UTF_8);
    }
}
