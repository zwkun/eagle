package yj.eagle.message.exception;

import yj.eagle.message.annotation.Message;

import java.util.Objects;

/**
 * @author zwk
 * @version 1.0
 * @date 2023/8/16 17:22
 */

@Message(encoder = ExceptionMessageEncoder.class,
        decoder = ExceptionMessageDecoder.class)
public class ExceptionMessage {
    private String message;

    public ExceptionMessage(String message) {
        Objects.requireNonNull(message, "message cannot be null");
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
