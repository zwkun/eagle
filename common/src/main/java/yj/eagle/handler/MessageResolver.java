package yj.eagle.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import yj.eagle.exception.EagleException;
import yj.eagle.message.MessageDecoder;
import yj.eagle.message.MessageEncoder;
import yj.eagle.message.MessageMetaData;
import yj.eagle.message.MessageSerializeResolver;
import yj.eagle.message.exception.ExceptionMessage;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author zwk
 * @version 1.0
 * @date 2023/8/10 17:30
 */
@Slf4j
@ChannelHandler.Sharable
public class MessageResolver extends ChannelDuplexHandler {
    public static final AttributeKey<ChannelPromise> PROMISE_KEY = AttributeKey.valueOf("promise");

    private MessageSerializeResolver messageSerializeResolver;
    private boolean isClient;


    public MessageResolver(MessageSerializeResolver messageSerializeResolver, boolean isClient) {
        Objects.requireNonNull(messageSerializeResolver, "serialize resolver cannot be null!");
        this.messageSerializeResolver = messageSerializeResolver;
        this.isClient = isClient;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        int messageNameLen = byteBuf.readInt();
        byte[] messageNameBs = new byte[messageNameLen];
        byteBuf.readBytes(messageNameBs);
        String messageName = new String(messageNameBs);
        MessageDecoder<?> decoder = messageSerializeResolver.getDecoder(messageName);
        if (decoder == null) {
            String errorMessage = "cannot find message : " + messageName + "'s decoder!";
            if (isClient) {
                ChannelPromise channelPromise = ctx.channel().attr(MessageResolver.PROMISE_KEY).get();
                channelPromise.setFailure(new EagleException(errorMessage));
            } else {
                ctx.fireChannelRead(new ExceptionMessage(errorMessage));
            }
            return;
        }
        byte[] msgBs;
        if (byteBuf.hasArray()) {
            msgBs = byteBuf.array();
        } else {
            msgBs = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(msgBs);
        }
        ctx.fireChannelRead(decoder.decode(msgBs, messageSerializeResolver.getMetaData(messageName)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        Class<?> msgClass = msg.getClass();
        MessageEncoder<Object> encoder = (MessageEncoder<Object>) messageSerializeResolver.getEncoder(msgClass);
        if (encoder == null) {
            String errorMessage = "cannot find message : " + msgClass.getName() + " encoder";
            if (isClient) {
                throw new EagleException(errorMessage);
            } else {
                ExceptionMessage exceptionMessage = new ExceptionMessage(errorMessage);
                writeExceptionMessage(ctx, exceptionMessage, promise);
            }
            return;
        }
        byte[] encode = encoder.encode(msg);
        writeMessage(ctx, encode, promise, msgClass);
    }

    @SuppressWarnings("unchecked")
    private void writeExceptionMessage(ChannelHandlerContext ctx, ExceptionMessage exceptionMessage, ChannelPromise promise) {
        MessageEncoder<Object> encoder = (MessageEncoder<Object>) messageSerializeResolver.getEncoder(exceptionMessage.getClass());
        writeMessage(ctx, encoder.encode(exceptionMessage), promise, exceptionMessage.getClass());
    }

    private void writeMessage(ChannelHandlerContext ctx, byte[] encode, ChannelPromise promise, Class<?> msgClass) {
        MessageMetaData metaData = messageSerializeResolver.getMetaData(msgClass);
        byte[] messageNameBs = metaData.getMessageName().getBytes(StandardCharsets.UTF_8);
        ByteBuf buffer = Unpooled.buffer(4 + messageNameBs.length + encode.length);
        buffer.writeInt(messageNameBs.length);
        buffer.writeBytes(messageNameBs);
        buffer.writeBytes(encode);
        ctx.writeAndFlush(buffer, promise);
    }

}
