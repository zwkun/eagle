package yj.eagle.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import yj.eagle.message.MessageHandler;
import yj.eagle.message.exception.ExceptionMessage;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author zwk
 * @version 1.0
 * @date 2023/8/9 15:57
 */
@Slf4j
@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<Object> {

    private List<MessageHandler> handlers;
    private Executor executor;

    public ServerHandler(List<MessageHandler> handlers, int executeThreadNum) {
        this.handlers = handlers;
        executor = Executors.newFixedThreadPool(executeThreadNum);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        executor.execute(() -> {
            Class<?> msgClass = msg.getClass();
            boolean foundHandler = false;
            for (MessageHandler handler : handlers) {
                if (handler.support(msgClass)) {
                    foundHandler = true;
                    try {
                        handler.handleMessage(msg, channelHandlerContext::writeAndFlush);
                    } catch (Throwable t) {
                        String message = t.getMessage();
                        if (message == null) {
                            message = t.getClass().getName();
                        }
                        channelHandlerContext.writeAndFlush(new ExceptionMessage(message));
                        log.error("exception caught", t);
                    }
                    break;
                }
            }
            if (!foundHandler) {
                log.error("cannot find message handler: " + msg.getClass().getName());
                channelHandlerContext.writeAndFlush(new ExceptionMessage("server handler not found"));
            }
        });
    }
}
