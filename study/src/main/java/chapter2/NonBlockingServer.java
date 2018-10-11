package chapter2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class NonBlockingServer {
    private Map<SocketChannel, List<byte[]>> keepDataTrack = new HashMap<>();
    private ByteBuffer buffer = ByteBuffer.allocate(2 * 1024);

    private void startEchoServer() {
        // 자바 1.7에서 새로 등장한 기능으로 try 블록이 끝날 때 소괄호 안에서 선언된 자원을 자동으로 해제해준다.
        // 물론 try 블록이 비정상적으로 끝나서 catch 블록으로 이동하더라도 자원은 정상적으로 해제 된다.
        try (
                // 자바 NIO 컴포넌트 중의 하나인 selector는 자신에게 등록된 채널에 변경 사항이 발생했는지 검사하고 변경사항이 발생한 채널에 대한 접근을 가능하게 해준다.
                Selector selector = Selector.open();
                // 블로킹 소켓의 ServerSocket에 대응되는 논블로킹 소켓의 서버 소켓 채널을 생성한다. 블로킹 소켓과 다르게 소켓 채널을 먼저 생성하고 사용할 포트를 바인딩한다.
                ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()
        ) {

            // 생성한 Selector와 ServerSocketChannel 객체가 정상적으로 생성되었는지 확인한다.
            if ((serverSocketChannel.isOpen()) && (selector.isOpen())) {
                // 소켓 채널의 블로킹 모드의 기본값은 true 다. 즉, 별도로 논블로닝 모드로 지정하지 않으면 블로킹 모드로 동작한다.
                // 여기서 ServerSocketChannel 객체를 논블로킹 모드로 설정
                serverSocketChannel.configureBlocking(false);
                // 클라이언트의 연결을 대기할 포트를 지정하고 생성된 ServerSocketChannel 객체에 할당한다.
                // 이 작업이 완료되면 ServerChannel 객체가 지정된 포트로부터 클라이언트의 연결을 생성할 수 있다.
                serverSocketChannel.bind(new InetSocketAddress(8888));

                // ServerSocketChannel 객체를 Selector 객체에 등록한다. Selector 가 감지할 이벤트는 연결 요청에 해당하는 SelectionKey.OP_ACCEPT
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
                System.out.println("접속 대기중");

                while (true) {
                    // Selector에 등록된 채널에 변경 사항이 발생했는지 검사합니다. Selector에 아무런 I/O 이벤트도 발생하지 않으면 스레드는 이 부분에 블로킹된다.
                    // I/O 이벤트가 발생하지 않을 때 블로킹을 피하고 싶다면 selectNow 메소드를 사용하면 된다.
                    selector.select();
                    // Selector 에 등록된 채널 중에 I/O 이벤트가 발생한 채널들의 목록을 조회한다.
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                    while (keys.hasNext()) {
                        SelectionKey key = (SelectionKey) keys.next();
                        // I/O 이벤트가 발생한 채널에서 동일한 이벤트가 감지되는 것을 방지하기 위하여 조회된 목록에서 제거
                        keys.remove();

                        if (!key.isValid()) {
                            continue;
                        }

                        // 조회된 I/O 이벤트의 종류가 연결 요청인지 확인한다. 만약 연결 요청 이벤트라면 연결처리 메소드로 이동한다.
                        if (key.isAcceptable()) {
                            this.acceptOP(key, selector);
                        }
                        // 조회된 I/O 이벤트의 종류가 데이터 수신인지 확인한다. 만약 데이터 수신 이벤트라면 데이터 읽기 처리 메소드로 이동한다.
                        else if (key.isReadable()) {
                            this.readOP(key);
                        }
                        // 조회된 I/O 이벤트의 종류가 데이터 쓰기 가능인지 확인한다. 만약 데이터 쓰기 가능 이벤트라면 데이터 읽기 처리 메서드로 이동한다.
                        else if (key.isWritable()) {
                            this.writeOP(key);
                        }
                    }
                }
            }
            else {
                System.out.println("서버 소캣을 생성하지 못했습니다.");
            }
        }
        catch (IOException ex) {
            System.err.println(ex);
        }
    }

    private void acceptOP(SelectionKey key, Selector selector) throws IOException {
        // 연결 요청 이벤트가 발생한 채널은 항상 ServerSocketChannel 이므로 이벤트가 발생한 채널을 ServerSocketChannel로 캐스팅 한다.
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        // ServerSocketChannel을 사용하여 클라이언트의 연결을 수락하고 연결된 소켓 채널을 가져온다.
        SocketChannel socketChannel = serverChannel.accept();
        // 연결된 클라이언트 소켓 채널을 논블로킹 모드로 설정한다.
        socketChannel.configureBlocking(false);

        System.out.println("클라이언트 연결됨 : " + socketChannel.getRemoteAddress());

        keepDataTrack.put(socketChannel, new ArrayList<byte[]>());
        // 클라이언트 소ㅔㅅ 채널을 Selector 에 등록하여 I/O 이벤트를 감시
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    private void readOP(SelectionKey key) {
        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            buffer.clear();
            int numRead = -1;
            try {
                numRead = socketChannel.read(buffer);
            }
            catch (IOException e) {
                System.err.println("데이터 읽기 에러!");
            }

            if (numRead == -1) {
                this.keepDataTrack.remove(socketChannel);
                System.out.println("클라이언트 연결 종료 : "
                        + socketChannel.getRemoteAddress());
                socketChannel.close();
                key.cancel();
                return;
            }

            byte[] data = new byte[numRead];
            System.arraycopy(buffer.array(), 0, data, 0, numRead);
            System.out.println(new String(data, "UTF-8")
                    + " from " + socketChannel.getRemoteAddress());

            doEchoJob(key, data);
        }
        catch (IOException ex) {
            System.err.println(ex);
        }
    }

    private void writeOP(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        List<byte[]> channelData = keepDataTrack.get(socketChannel);
        Iterator<byte[]> its = channelData.iterator();

        while (its.hasNext()) {
            byte[] it = its.next();
            its.remove();
            socketChannel.write(ByteBuffer.wrap(it));
        }

        key.interestOps(SelectionKey.OP_READ);
    }

    private void doEchoJob(SelectionKey key, byte[] data) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        List<byte[]> channelData = keepDataTrack.get(socketChannel);
        channelData.add(data);

        key.interestOps(SelectionKey.OP_WRITE);
    }

    public static void main(String[] args) {
        NonBlockingServer main = new NonBlockingServer();
        main.startEchoServer();
    }
}

