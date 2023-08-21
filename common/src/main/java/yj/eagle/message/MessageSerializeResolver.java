package yj.eagle.message;

import yj.eagle.exception.EagleException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author zwk
 * @version 1.0
 * @date 2023/8/18 10:25
 */

public class MessageSerializeResolver {

    private final Map<String, MessageDefinition> stringMessageDefinitionMap = new HashMap<>();
    private final Map<Class<?>, MessageDefinition> classMessageDefinitionMap = new HashMap<>();

    public MessageSerializeResolver() {
        registerBase();
    }

    private void registerBase() {
        MessageNameGenerator nameGenerator = Class::getName;
        register(MessageHelper.parserMessage(Byte.class, nameGenerator));
        register(MessageHelper.parserMessage(byte.class, nameGenerator));
        register(MessageHelper.parserMessage(Short.class, nameGenerator));
        register(MessageHelper.parserMessage(short.class, nameGenerator));
        register(MessageHelper.parserMessage(Integer.class, nameGenerator));
        register(MessageHelper.parserMessage(int.class, nameGenerator));
        register(MessageHelper.parserMessage(Long.class, nameGenerator));
        register(MessageHelper.parserMessage(long.class, nameGenerator));
        register(MessageHelper.parserMessage(Character.class, nameGenerator));
        register(MessageHelper.parserMessage(char.class, nameGenerator));
        register(MessageHelper.parserMessage(Float.class, nameGenerator));
        register(MessageHelper.parserMessage(float.class, nameGenerator));
        register(MessageHelper.parserMessage(Double.class, nameGenerator));
        register(MessageHelper.parserMessage(double.class, nameGenerator));
        register(MessageHelper.parserMessage(Boolean.class, nameGenerator));
        register(MessageHelper.parserMessage(boolean.class, nameGenerator));
        register(MessageHelper.parserMessage(String.class, nameGenerator));
    }

    public synchronized void register(MessageDefinition messageDefinition) {
        String messageName = messageDefinition.getMessageName();
        Objects.requireNonNull(messageName, "register message name cannot be null");
        Objects.requireNonNull(messageDefinition, "register message definition cannot be null");
        Objects.requireNonNull(messageDefinition.getMessageClass(), "register message class cannot be null");
        if (stringMessageDefinitionMap.put(messageName, messageDefinition) != null) {
            throw new EagleException(messageName + " already registered");
        }
        classMessageDefinitionMap.put(messageDefinition.getMessageClass(), messageDefinition);
    }

    public MessageDecoder<?> getDecoder(String messageName) {
        MessageDefinition messageDefinition = stringMessageDefinitionMap.get(messageName);
        if (messageDefinition != null) {
            return messageDefinition.getDecoder();
        }
        return null;
    }


    public MessageEncoder<?> getEncoder(String messageName) {
        MessageDefinition messageDefinition = stringMessageDefinitionMap.get(messageName);
        if (messageDefinition != null) {
            return messageDefinition.getEncoder();
        }
        return null;
    }

    public MessageEncoder<?> getEncoder(Class<?> msgClass) {
        MessageDefinition messageDefinition = classMessageDefinitionMap.get(msgClass);
        if (messageDefinition != null) {
            return messageDefinition.getEncoder();
        }
        return null;
    }

    public MessageMetaData getMetaData(String messageName) {
        MessageDefinition messageDefinition = stringMessageDefinitionMap.get(messageName);
        if (messageDefinition != null) {
            return messageDefinition.getMetaData();
        }
        return null;
    }

    public MessageMetaData getMetaData(Class<?> msgClass) {
        MessageDefinition messageDefinition = classMessageDefinitionMap.get(msgClass);
        if (messageDefinition != null) {
            return messageDefinition.getMetaData();
        }
        return null;
    }
}
