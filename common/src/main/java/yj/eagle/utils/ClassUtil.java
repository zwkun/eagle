package yj.eagle.utils;

import yj.eagle.exception.EagleException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.Objects;

/**
 * @author zwk
 * @version 1.0
 * @date 2023/8/17 19:53
 */

public class ClassUtil {
    public static String generateSimpleName(Class<?> msg) {
        Objects.requireNonNull(msg, "msg cannot be null");
        String name = msg.getSimpleName();

        if (name.length() == 1) {
            return name;
        }
        name = name.substring(0, 1).toLowerCase(Locale.ROOT) + name.substring(1);
        return name;
    }

    public static boolean isHashCodeMethod(Method method) {
        return method != null && method.getParameterCount() == 0 && method.getName().equals("hashCode");
    }

    public static boolean isToStringMethod(Method method) {
        return method != null && method.getParameterCount() == 0 && method.getName().equals("toString");
    }

    public static boolean isEqualsMethod(Method method) {
        if (method == null) {
            return false;
        }
        if (method.getParameterCount() != 1) {
            return false;
        }
        if (!method.getName().equals("equals")) {
            return false;
        }
        return method.getParameterTypes()[0] == Object.class;
    }

    public static Object newInstance(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new EagleException(clazz.getName() + " must have non arg constructor", e);
        } catch (Exception e) {
            throw new EagleException(clazz.getName() + " new instance failed", e);
        }
    }

    public static boolean isAbstract(Class<?> clazz) {
        return clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers());
    }
}
