package yj.eagle.config;

import io.netty.channel.ServerChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import yj.eagle.exception.EagleException;
import yj.eagle.message.DefaultMessageNameGenerator;
import yj.eagle.message.MessageNameGenerator;
import yj.eagle.utils.ClassUtil;

/**
 * @author zwk
 * @version 1.0
 * @date 2023/8/17 9:28
 */

public class ServerConfig {

    public static final ServerConfig DEFAULT = new ServerConfig();
    public static final Class<? extends ServerChannel> DEFAULT_SERVER_CHANNEL_CLASS = NioServerSocketChannel.class;
    public static final String DEFAULT_CONFIG_NAME = "server.properties";
    private static final int DEFAULT_SERVER_PORT = 8080;
    private static final int DEFAULT_MAX_IDEL_TIME = 30 * 1000;

    private int port = DEFAULT_SERVER_PORT;
    private Class<? extends ServerChannel> channelClass;
    private int maxIdleTime = DEFAULT_MAX_IDEL_TIME;
    private String scanPackages;
    private int executeThreadNum = Runtime.getRuntime().availableProcessors() * 2;
    private boolean daemon = false;
    private MessageNameGenerator messageNameGenerator = new DefaultMessageNameGenerator();

    public void setConfigProperties(ConfigResolver resolver) throws Exception {
        ServerConfig config = this;
        String port = resolver.getConfig("eagle.server.port");
        if (port != null && (port = port.trim()).length() > 0) {
            config.setPort(Integer.parseInt(port));
        }
        String channelClassName = resolver.getConfig("eagle.server.channel.class.name");
        if (channelClassName != null && (channelClassName = channelClassName.trim()).length() > 0) {
            config.setChannelClass((Class<? extends ServerChannel>) Class.forName(channelClassName));
        }
        String maxIdleTime = resolver.getConfig("eagle.server.channel.max.idle.time");
        if (maxIdleTime != null && (maxIdleTime = maxIdleTime.trim()).length() > 0) {
            config.setMaxIdleTime(Integer.parseInt(maxIdleTime));
        }
        String scanPackages = resolver.getConfig("eagle.server.scan.packages");
        if (scanPackages != null && (scanPackages = scanPackages.trim()).length() > 0) {
            config.setScanPackages(scanPackages);
        }
        String executeThreadNum = resolver.getConfig("eagle.server.execute.thread.num");
        if (executeThreadNum != null && (executeThreadNum = executeThreadNum.trim()).length() > 0) {
            config.setExecuteThreadNum(Integer.parseInt(executeThreadNum));
        }

        String daemon = resolver.getConfig("eagle.server.daemon.start");
        if (daemon != null && (daemon = daemon.trim()).length() > 0) {
            config.setDaemon(Boolean.parseBoolean(daemon));
        }
        String messageNameGenerator = resolver.getConfig("eagle.server.message.name.generator");

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

    public Class<? extends ServerChannel> getChannelClass() {
        return channelClass;
    }

    public void setChannelClass(Class<? extends ServerChannel> channelClass) {
        this.channelClass = channelClass;
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

    public int getExecuteThreadNum() {
        return executeThreadNum;
    }

    public void setExecuteThreadNum(int executeThreadNum) {
        this.executeThreadNum = executeThreadNum;
    }

    public boolean isDaemon() {
        return daemon;
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    public MessageNameGenerator getMessageNameGenerator() {
        return messageNameGenerator;
    }

    public void setMessageNameGenerator(MessageNameGenerator messageNameGenerator) {
        this.messageNameGenerator = messageNameGenerator;
    }
}
