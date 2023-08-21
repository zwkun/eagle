package yi.eagle;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPromise;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import yi.eagle.config.ClientConfig;
import yi.eagle.handler.ClientHandler;
import yj.eagle.config.ConfigResolver;
import yj.eagle.exception.EagleException;
import yj.eagle.handler.MessageResolver;
import yj.eagle.message.MessageHelper;
import yj.eagle.message.MessageSerializeResolver;
import yj.eagle.message.exception.ExceptionMessage;
import yj.eagle.utils.ClassUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * @author zwk
 * @version 1.0
 * @date 2023/8/10 17:48
 */
@Slf4j
public class EagleClient implements InvocationHandler {

    public static final AttributeKey<Object> RESULT_KEY = AttributeKey.valueOf("result");
    public ClientConfig config;
    private FixedChannelPool pool;
    private MessageSerializeResolver messageSerializeResolver;

    private MessageResolver messageResolver;
    private ClientHandler clientHandler;

    private volatile boolean init = false;
    private volatile boolean start = false;


    public static void main(String[] args) throws Exception {
        ConfigResolver resolver = MessageHelper.getConfigResolver(ClientConfig.DEFAULT_CONFIG_NAME);
        ClientConfig config = new ClientConfig();
        config.setConfigProperties(resolver);
        EagleClient client = new EagleClient(config);
        client.start();
    }


    public EagleClient() {
        this(ClientConfig.DEFAULT);
    }

    public EagleClient(ClientConfig config) {
        this.config = config;
    }

    public synchronized void start() throws Exception {
        if (start) {
            return;
        }
        Object zhis = this;
        Runnable runnable = () -> {
            ClientConfig config = this.config;
            NioEventLoopGroup group = new NioEventLoopGroup();
            Bootstrap client = new Bootstrap()
                    .group(group)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .channel(getPlatformChannelClass())
                    .remoteAddress(config.getHost(), config.getPort());

            ChannelPoolHandler channelPoolHandler = new AbstractChannelPoolHandler() {
                @Override
                public void channelCreated(Channel ch) throws Exception {
                    ch.pipeline().addLast(new IdleStateHandler(0, 0, config.getMaxIdleTime()))
                            .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                            .addLast(new LengthFieldPrepender(4))
                            .addLast(getMessageHandler())
                            .addLast(getClientHandler());
                }
            };
            pool = new FixedChannelPool(
                    client,
                    channelPoolHandler,
                    ChannelHealthChecker.ACTIVE,
                    FixedChannelPool.AcquireTimeoutAction.FAIL,
                    config.getAcquireTimeout(),
                    config.getMaxConnections(),
                    Integer.MAX_VALUE,
                    true,
                    true);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                pool.close();
                try {
                    group.shutdownGracefully().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }));
            synchronized (zhis) {
                zhis.notify();
            }
        };
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.start();
        if (pool == null) {
            zhis.wait();
        }
        start = true;
    }


    private ClientHandler getClientHandler() {
        if (this.clientHandler == null) {
            this.clientHandler = new ClientHandler();
        }
        return clientHandler;
    }

    public Channel getChannel() throws Exception {
        return pool.acquire().get();
    }

    public void release(Channel channel) {
        pool.release(channel);
    }

    private ChannelHandler getMessageHandler() throws Exception {
        if (!init) {
            init();
        }
        return messageResolver;
    }

    private synchronized void init() throws Exception {
        if (init) {
            return;
        }
        if (messageSerializeResolver == null) {
            messageSerializeResolver = new MessageSerializeResolver();
        }
        messageResolver = new MessageResolver(messageSerializeResolver, true);
        clientHandler = getClientHandler();

        List<Class<?>> defaultMessageClasses = getDefaultMessageClasses();
        MessageHelper.doRegister(messageSerializeResolver, config.getMessageNameGenerator(), defaultMessageClasses);

        MessageHelper.doRegister(messageSerializeResolver, config.getScanPackages(), config.getMessageNameGenerator());
        init = true;
    }

    private List<Class<?>> getDefaultMessageClasses() {
        return Collections.singletonList(ExceptionMessage.class);
    }

    private Class<? extends Channel> getPlatformChannelClass() {
        ClientConfig config = this.config;
        if (config.getChannel() != null) {
            return config.getChannel();
        }
        String property = System.getProperty("os.name");
        if (property == null || property.trim().length() == 0) {
            return ClientConfig.DEFAULT_SERVER_CHANNEL_CLASS;
        }
        property = property.toLowerCase(Locale.ROOT);
        if (property.contains("windows")) {
            return ClientConfig.DEFAULT_SERVER_CHANNEL_CLASS;
        }
        if (property.contains("linux")) {
            return EpollSocketChannel.class;
        }
        if (property.contains("macos") || property.contains("bsd")) {
            return KQueueSocketChannel.class;
        }
        return ClientConfig.DEFAULT_SERVER_CHANNEL_CLASS;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (ClassUtil.isEqualsMethod(method)) {
            if (args[0] == null) {
                return false;
            }
            return proxy.getClass().equals(args[0].getClass());
        }
        if (ClassUtil.isHashCodeMethod(method)) {
            return proxy.getClass().hashCode();
        }
        if (ClassUtil.isToStringMethod(method)) {
            return proxy.getClass().toString();
        }
        if (args == null || args.length < 1) {
            throw new EagleException(method.getDeclaringClass().getName() + "#" + method.getName() + " have non parameter");
        }
        Channel channel = getChannel();
        try {
            ChannelPromise writePromise = channel.newPromise();
            ChannelPromise readPromise = channel.newPromise();
            channel.attr(MessageResolver.PROMISE_KEY).set(readPromise);
            channel.writeAndFlush(args[0], writePromise);
            String errorMessage = "write timeout";
            if (!writePromise.await(config.getWriteTimeout(), TimeUnit.MILLISECONDS)) {
                throw new EagleException(errorMessage);
            } else {
                if (!writePromise.isSuccess()) {
                    throw new EagleException(writePromise.cause());
                }
            }

            errorMessage = "read timeout";
            if (readPromise.await(config.getReadTimeout(), TimeUnit.MILLISECONDS)) {
                if (readPromise.isSuccess()) {
                    Object msg = channel.attr(RESULT_KEY).get();
                    if (!(msg instanceof ExceptionMessage)) {
                        if (method.getReturnType().equals(Void.TYPE)) {
                            return null;
                        }
                        return msg;
                    }
                    errorMessage = ((ExceptionMessage) msg).getMessage();
                } else {
                    throw new EagleException(readPromise.cause());
                }
            }
            channel.close().sync();
            throw new EagleException(errorMessage);
        } finally {
            release(channel);
        }
    }


    public MessageSerializeResolver getMessageSerializeResolver() {
        return messageSerializeResolver;
    }

    public void setMessageSerializeResolver(MessageSerializeResolver messageSerializeResolver) {
        this.messageSerializeResolver = messageSerializeResolver;
    }
}
