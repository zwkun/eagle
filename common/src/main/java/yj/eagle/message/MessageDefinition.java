package yj.eagle.message;

import yj.eagle.utils.ClassUtil;

/**
 * @author zwk
 * @version 1.0
 * @date 2023/8/18 10:03
 */

public class MessageDefinition {
    private String messageName;
    private Class<?> messageClass;

    private Class<? extends MessageDecoder<?>> decoderClass;
    private Class<? extends MessageEncoder<?>> encoderClass;


    private volatile MessageDecoder<?> decoder;
    private volatile MessageEncoder<?> encoder;

    private volatile MessageMetaData metaData;

    public MessageDefinition() {
    }

    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public Class<?> getMessageClass() {
        return messageClass;
    }

    public void setMessageClass(Class<?> messageClass) {
        this.messageClass = messageClass;
    }

    public Class<? extends MessageDecoder<?>> getDecoderClass() {
        return decoderClass;
    }

    public void setDecoderClass(Class<? extends MessageDecoder<?>> decoderClass) {
        this.decoderClass = decoderClass;
    }

    public Class<? extends MessageEncoder<?>> getEncoderClass() {
        return encoderClass;
    }

    public void setEncoderClass(Class<? extends MessageEncoder<?>> encoderClass) {
        this.encoderClass = encoderClass;
    }

    public MessageDecoder<?> getDecoder() {
        if (decoder == null) {
            synchronized (this) {
                if (decoder == null) {
                    decoder = (MessageDecoder<?>) ClassUtil.newInstance(decoderClass);
                }
            }
        }
        return decoder;
    }

    public MessageEncoder<?> getEncoder() {
        if (encoder == null) {
            synchronized (this) {
                if (encoder == null) {
                    encoder = (MessageEncoder<?>) ClassUtil.newInstance(encoderClass);
                }
            }
        }
        return encoder;
    }

    public MessageMetaData getMetaData() {
        if (metaData == null) {
            synchronized (this) {
                if (metaData == null) {
                    metaData = new MessageMetaData() {
                        @Override
                        public String getMessageName() {
                            return MessageDefinition.this.getMessageName();
                        }

                        @Override
                        public Class<?> getMessageClass() {
                            return MessageDefinition.this.getMessageClass();
                        }
                    };
                }
            }
        }
        return metaData;
    }
}
