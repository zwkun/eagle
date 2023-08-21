package yj.eagle;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import yj.eagle.config.ConfigResolver;
import yj.eagle.config.ServerConfig;
import yj.eagle.handler.MessageResolver;
import yj.eagle.handler.ServerHandler;
import yj.eagle.message.MessageHandler;
import yj.eagle.message.MessageHelper;
import yj.eagle.message.MessageSerializeResolver;
import yj.eagle.message.exception.ExceptionMessage;

import java.util.*;

/**
 * @author zwk
 * @version 1.0
 * @date 2023/8/9 15:34
 */
@Slf4j
public class EagleServer {
    private ServerConfig config;
    private List<MessageHandler> handlers = new ArrayList<>();
    private MessageSerializeResolver messageSerializeResolver;

    private MessageResolver messageResolver;
    private ServerHandler serverHandler;

    private volatile boolean init = false;
    private volatile boolean start = false;


    public static void main(String[] args) throws Exception {
        ConfigResolver resolver = MessageHelper.getConfigResolver(ServerConfig.DEFAULT_CONFIG_NAME);
        ServerConfig config = new ServerConfig();
        config.setConfigProperties(resolver);
        EagleServer server = new EagleServer(config);
        server.start();
    }

    public EagleServer() {
        this(ServerConfig.DEFAULT);
    }

    public EagleServer(ServerConfig config) {
        this.config = config;
    }

    public synchronized void start() throws Exception {
        if (start) {
            return;
        }
        Runnable runnable = () -> {
            NioEventLoopGroup bossGroup = new NioEventLoopGroup();
            NioEventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap server = new ServerBootstrap()
                        .group(bossGroup, workerGroup)
                        .childOption(ChannelOption.SO_BACKLOG, 1024)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .channel(getPlatformChannelClass())
                        .childHandler(new ChannelInitializer<Channel>() {
                            @Override
                            protected void initChannel(Channel channel) throws Exception {
                                channel.pipeline().addLast(new IdleStateHandler(0, 0, config.getMaxIdleTime()))
                                        .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                                        .addLast(new LengthFieldPrepender(4))
                                        .addLast(getMessageResolver())
                                        .addLast(getServerHandler());
                            }
                        });
                ChannelFuture future = server.bind(config.getPort()).sync();
                future.channel().closeFuture().sync();

            } catch (Exception e) {
                log.error("server start error", e);
            } finally {
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        workerGroup.shutdownGracefully().sync();
                    } catch (InterruptedException e) {
                        //do nothing
                    }
                    try {
                        bossGroup.shutdownGracefully().sync();
                    } catch (InterruptedException e) {
                        //do nothing
                    }
                }));
            }
        };
        if (config.isDaemon()) {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            thread.start();
        } else {
            runnable.run();
        }
        start = true;
    }

    private ChannelHandler getServerHandler() throws Exception {
        if (!init) {
            init();
        }
        return serverHandler;
    }

    private ChannelHandler getMessageResolver() throws Exception {
        if (!init) {
            init();
        }
        return messageResolver;

    }


    private synchronized void init() throws Exception {
        if (init) {
            return;
        }
        List<MessageHandler> tmp = new ArrayList<>();
        ServiceLoader<MessageHandler> serviceLoader = ServiceLoader.load(MessageHandler.class);
        for (MessageHandler messageHandler : serviceLoader) {
            tmp.add(messageHandler);
        }
        if (handlers == null) {
            handlers = tmp;
        } else {
            if (!tmp.isEmpty()) {
                handlers.addAll(tmp);
            }
        }
        serverHandler = new ServerHandler(handlers, config.getExecuteThreadNum());

        if (messageSerializeResolver == null) {
            messageSerializeResolver = new MessageSerializeResolver();
        }

        messageResolver = new MessageResolver(messageSerializeResolver, false);

        List<Class<?>> defaultMessageClass = getDefaultMessageClasses();
        MessageHelper.doRegister(messageSerializeResolver, config.getMessageNameGenerator(), defaultMessageClass);

        MessageHelper.doRegister(messageSerializeResolver, config.getScanPackages(), config.getMessageNameGenerator());

        init = true;
    }

    private List<Class<?>> getDefaultMessageClasses() {
        return Collections.singletonList(ExceptionMessage.class);
    }

    private Class<? extends ServerChannel> getPlatformChannelClass() {
        if (config.getChannelClass() != null) {
            return config.getChannelClass();
        }
        String property = System.getProperty("os.name");
        if (property == null || property.trim().length() == 0) {
            return ServerConfig.DEFAULT_SERVER_CHANNEL_CLASS;
        }
        property = property.toLowerCase(Locale.ROOT);
        if (property.contains("windows")) {
            return ServerConfig.DEFAULT_SERVER_CHANNEL_CLASS;
        }
        if (property.contains("linux")) {
            return EpollServerSocketChannel.class;
        }
        if (property.contains("macos") || property.contains("bsd")) {
            return KQueueServerSocketChannel.class;
        }
        return ServerConfig.DEFAULT_SERVER_CHANNEL_CLASS;
    }

    public void addHandlers(List<MessageHandler> handlers) {
        handlers.forEach(this::addHandler);
    }

    public synchronized void addHandler(MessageHandler handler) {
        Objects.requireNonNull(handler, "message handler cannot be null");
        this.handlers.add(handler);
    }

    public void setMessageSerializeResolver(MessageSerializeResolver messageSerializeResolver) {
        this.messageSerializeResolver = messageSerializeResolver;
    }
}
