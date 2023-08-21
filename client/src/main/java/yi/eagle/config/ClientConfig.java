package yi.eagle.config;

import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;
import yj.eagle.config.ConfigResolver;
import yj.eagle.exception.EagleException;
import yj.eagle.message.DefaultMessageNameGenerator;
import yj.eagle.message.MessageNameGenerator;
import yj.eagle.utils.ClassUtil;

/**
 * @author zwk
 * @version 1.0
 * @date 2023/8/17 9:21
 */

public class ClientConfig {

    public static final ClientConfig DEFAULT = new ClientConfig();
    public static final Class<? extends Channel> DEFAULT_SERVER_CHANNEL_CLASS = NioSocketChannel.class;
    public static final String DEFAULT_CONFIG_NAME = "client.properties";

    private static final int DEFAULT_SERVER_PORT = 8080;
    private static final String DEFAULT_SERVER_HOST = "127.0.0.1";
    private static final int DEFAULT_MAX_IDEL_TIME = 30 * 1000;
    private static final int DEFAULT_READ_TIMEOUT = DEFAULT_MAX_IDEL_TIME;
    private static final int DEFAULT_MAX_CONNECTIONS = 10;
    private static final int DEFAULT_ACQUIRE_TIMEOUT = DEFAULT_MAX_IDEL_TIME;


    private int port = DEFAULT_SERVER_PORT;
    private String host = DEFAULT_SERVER_HOST;
    private int readTimeout = DEFAULT_READ_TIMEOUT;
    private int writeTimeout = DEFAULT_READ_TIMEOUT;
    private Class<? extends Channel> channel;
    private int maxIdleTime = DEFAULT_MAX_IDEL_TIME;
    private String scanPackages;
    private int maxConnections = DEFAULT_MAX_CONNECTIONS;
    private int acquireTimeout = DEFAULT_ACQUIRE_TIMEOUT;
    private MessageNameGenerator messageNameGenerator = new DefaultMessageNameGenerator();

    public void setConfigProperties(ConfigResolver resolver) throws Exception {
        ClientConfig config = this;
        String port = resolver.getConfig("eagle.client.server.port");
        if (port != null && (port = port.trim()).length() > 0) {
            config.setPort(Integer.parseInt(port));
        }
        String host = resolver.getConfig("eagle.client.server.host");
        if (host != null && (host = host.trim()).length() > 0) {
            config.setHost(host);
        }
        String readTimeout = resolver.getConfig("eagle.client.read.timeout");
        if (readTimeout != null && (readTimeout = readTimeout.trim()).length() > 0) {
            config.setReadTimeout(Integer.parseInt(readTimeout));
        }
        String writeTimeout = resolver.getConfig("eagle.client.write.timeout");
        if (writeTimeout != null && (writeTimeout = writeTimeout.trim()).length() > 0) {
            config.setWriteTimeout(Integer.parseInt(writeTimeout));
        }
        String channel = resolver.getConfig("eagle.client.channel.class.name");
        if (channel != null && (channel = channel.trim()).length() > 0) {
            config.setChannel((Class<? extends Channel>) Class.forName(channel));

        }
        String maxIdleTime = resolver.getConfig("eagle.client.channel.max.idle.time");
        if (maxIdleTime != null && (maxIdleTime = maxIdleTime.trim()).length() > 0) {
            config.setMaxIdleTime(Integer.parseInt(maxIdleTime));
        }
        String packages = resolver.getConfig("eagle.client.scan.packages");
        if (packages != null && (packages = packages.trim()).length() > 0) {
            config.setScanPackages(packages);
        }
        String maxConnections = resolver.getConfig("eagle.client.max.connections");
        if (maxConnections != null && (maxConnections = maxConnections.trim()).length() > 0) {
            config.setMaxConnections(Integer.parseInt(maxConnections));
        }
        String acquireTimeout = resolver.getConfig("eagle.client.acquire.timeout");
        if (acquireTimeout != null && (acquireTimeout = acquireTimeout.trim()).length() > 0) {
            config.setAcquireTimeout(Integer.parseInt(acquireTimeout));
        }
        String messageNameGenerator = resolver.getConfig("eagle.client.message.name.generator");

        if (messageNameGenerator != null && (messageNameGenerator = messageNameGenerator.trim()).length() > 0) {
            Class<?> clazz = Class.forName(messageNameGenerator);
            if (ClassUtil.isAbstract(clazz)) {
                throw new EagleException(messageNameGenerator + " can not be abstract");
            }
            config.setMessageNameGenerator((MessageNameGenerator) ClassUtil.newInstance(clazz));
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public Class<? extends Channel> getChannel() {
        return channel;
    }

    public void setChannel(Class<? extends Channel> channel) {
        this.channel = channel;
    }

    public int getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(int maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    public String getScanPackages() {
        return scanPackages;
    }

    public void setScanPackages(String scanPackages) {
        this.scanPackages = scanPackages;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getAcquireTimeout() {
        return acquireTimeout;
    }

    public void setAcquireTimeout(int acquireTimeout) {
        this.acquireTimeout = acquireTimeout;
    }

    public MessageNameGenerator getMessageNameGenerator() {
        return messageNameGenerator;
    }

    public void setMessageNameGenerator(MessageNameGenerator messageNameGenerator) {
        this.messageNameGenerator = messageNameGenerator;
    }
}
