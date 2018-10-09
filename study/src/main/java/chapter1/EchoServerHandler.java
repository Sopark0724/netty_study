package chapter1;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;

// ChannelInboundHandlerAdapter : 입력된 데이터를 처리하는 이벤트 핸들러
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    //데이터 수신 이벤트 처리 메소드. 클라이언트로부터 데이터의 수신이 이루어졌을 때 네티가 자동으로 호출하는 이벤트 메소드.
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        // 수신된 데이터를 가지고 있는 네티의 바이트 버퍼 객체로 부터 문자열 데이터를 읽어온다.
        String readMessage =((ByteBuf)msg).toString(Charset.defaultCharset());

        System.out.println("수신된 문자열 [" + readMessage + "]");

        ctx.write(readMessage);
    }

    @Override
    // ctx는 ChannelHandlerContext 인터페이스의 객체로서 채널 파이프라인에 대한 이벤트를 처리한다.
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.flush();
    }
}
