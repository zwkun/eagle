package yj.eagle.message;

import yj.eagle.utils.ClassUtil;

public class DefaultMessageNameGenerator implements MessageNameGenerator {
    public String generateMessageName(Class<?> msgClass) {
        return ClassUtil.generateSimpleName(msgClass);
    }
}
