package yj.eagle.message;

public interface MessageNameGenerator {
    String generateMessageName(Class<?> msgClass);
}
