package yi.eagle.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import yi.eagle.EagleClient;
import yj.eagle.handler.MessageResolver;


/**
 * @author zwk
 * @version 1.0
 * @date 2023/8/10 17:52
 */
@Slf4j
@ChannelHandler.Sharable
public class ClientHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.channel().attr(EagleClient.RESULT_KEY).set(msg);
        ctx.channel().attr(MessageResolver.PROMISE_KEY).get().setSuccess();
    }
}
