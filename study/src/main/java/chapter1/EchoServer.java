package chapter1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class EchoServer {

    public static void main(String[] args) throws InterruptedException {
        // EventLoopGroup 인터페이스에 NioEventLoopGroup 클래스의 객체를 할당한다.
        // 생성자에 입력된 스레드 수가 1이므로 단일 스레드로 동작하는 NioEventLoopGroup 객체를 생성
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // EventLoopGroup 인터페이스에 NioEventLoopGroup 클래스의 객체를 할당한다.
        // 생성자에 인수가 없으면 CPU 코어수에 따른 스레드 수가 설정됨(core * 2 * 2(하이퍼 쓰레딩 사용한다면))
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();

            // 첫 번째 인수는 부모 쓰레드다. 부모 쓰레드는 클라이언트 연결 요청의 수락을 담담.
            // 두 번째 인수는 연결된 소켓에 대한 I/O 처리를 담당하는 자식 스레드.
            b.group(bossGroup, workerGroup)
                    // 서버 소켓(부노 스레드)이 사용할 네트워크 입출력 모드를 설정. (여기서는 NIO 로 동작)
                    .channel(NioServerSocketChannel.class)
                    // 자식 채널의 초기화 방법을 설정
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        //ChannelInitializer는 클라이언트로부터 연결된 채널이 초기화 될 때이 기본 동작이 지정된 추상 클래스
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            // 채널 파이프라인에 EchoServerHandler 클래스를 등록.
                            // EchoServerHandler 클래스는 이후에 클라이언트의 연결이 생성되었을 때 데이터 처리를 담당.
                            p.addLast(new EchoServerHandler());
                        }
                    });

            ChannelFuture f = b.bind(8888).sync();

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
