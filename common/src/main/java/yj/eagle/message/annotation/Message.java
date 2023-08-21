package yj.eagle.message.annotation;

import yj.eagle.message.DefaultMessageDecoder;
import yj.eagle.message.DefaultMessageEncoder;
import yj.eagle.message.MessageDecoder;
import yj.eagle.message.MessageEncoder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Message {
    String value() default "";

    Class<? extends MessageDecoder<?>> decoder() default DefaultMessageDecoder.class;

    Class<? extends MessageEncoder<?>> encoder() default DefaultMessageEncoder.class;
}
