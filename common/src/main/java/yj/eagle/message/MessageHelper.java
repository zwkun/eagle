package yj.eagle.message;

import yj.eagle.config.ConfigResolver;
import yj.eagle.exception.EagleException;
import yj.eagle.message.annotation.Message;
import yj.eagle.scan.Scanner;
import yj.eagle.utils.ClassUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author zwk
 * @version 1.0
 * @date 2023/8/16 16:03
 */

public class MessageHelper {

    public static void doRegister(
            MessageSerializeResolver messageSerializeResolver,
            String scanPackage,
            MessageNameGenerator nameGenerator) throws Exception {
        if (scanPackage == null) {
            return;
        }
        scanPackage = scanPackage.replace(".", "/");
        Scanner scanner = new Scanner(scanPackage);

        List<Class<?>> classes = scanner.scan(Collections.singletonList(Object.class));
        doRegister(messageSerializeResolver, nameGenerator, classes);
    }


    public static void doRegister(
            MessageSerializeResolver messageSerializeResolver,
            MessageNameGenerator nameGenerator,
            List<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            if (isLegal(clazz)) {
                MessageDefinition messageDefinition = parserMessage(clazz, nameGenerator);
                messageSerializeResolver.register(messageDefinition);
            }
        }
    }

    public static MessageDefinition parserMessage(Class<?> messageClass, MessageNameGenerator nameGenerator) {
        Message message = messageClass.getAnnotation(Message.class);
        String messageName;
        Class<? extends MessageDecoder<?>> decoder;
        Class<? extends MessageEncoder<?>> encoder;
        if (message == null) {
            messageName = nameGenerator.generateMessageName(messageClass);
            decoder = DefaultMessageDecoder.class;
            encoder = DefaultMessageEncoder.class;
        } else {
            messageName = message.value();
            if ("".equals(messageName)) {
                messageName = nameGenerator.generateMessageName(messageClass);
            }

            decoder = message.decoder();
            if (ClassUtil.isAbstract(decoder)) {
                throw new EagleException("message decoder class cannot be abstract: " + decoder.getName());
            }
            encoder = message.encoder();
            if (ClassUtil.isAbstract(encoder)) {
                throw new EagleException("message encoder class cannot be abstract: " + encoder.getName());
            }
        }
        MessageDefinition messageDefinition = new MessageDefinition();
        messageDefinition.setMessageName(messageName);
        messageDefinition.setMessageClass(messageClass);
        messageDefinition.setDecoderClass(decoder);
        messageDefinition.setEncoderClass(encoder);
        return messageDefinition;
    }


    private static boolean isLegal(Class<?> clazz) {
        return !ClassUtil.isAbstract(clazz) && clazz.isAnnotationPresent(Message.class);
    }


    public static ConfigResolver getConfigResolver(String configFileName) throws Exception {
        Properties highPriorityProp = new Properties();
        File file = new File(configFileName);
        if (file.exists()) {
            try (FileInputStream fileInputStream = new FileInputStream(file);) {
                highPriorityProp.load(fileInputStream);
            }
        }
        Properties prop = new Properties();
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFileName)) {
            if (inputStream != null) {
                prop.load(inputStream);
            }
        }

        return key -> getConfig(key, highPriorityProp, prop);
    }

    private static String getConfig(String key, Properties highPriorityProp, Properties prop) {
        if (highPriorityProp != null) {
            String property = highPriorityProp.getProperty(key);
            if (property != null) {
                return property;
            }
        }

        return prop.getProperty(key);
    }
}
