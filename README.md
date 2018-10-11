# 1장. 네티소개
chapter 1 소스 참고

# 2장 네티의 주요 특징

## 2.1 동기와 비동기
<img src="https://user-images.githubusercontent.com/6028071/46670102-383d1080-cc0c-11e8-9a6a-bf8ecda691ec.png" alt="image" width="50%">

<a target="_blank" rel="noopener noreferrer" href="https://user-images.githubusercontent.com/6028071/46670102-383d1080-cc0c-11e8-9a6a-bf8ecda691ec.png"><img src="https://user-images.githubusercontent.com/6028071/46670102-383d1080-cc0c-11e8-9a6a-bf8ecda691ec.png" alt="image" style="max-width:50%;"></a>

<a target="_blank" rel="noopener noreferrer" href="https://user-images.githubusercontent.com/6028071/46670217-8e11b880-cc0c-11e8-8f9d-fff897e193cf.png"><img src="https://user-images.githubusercontent.com/6028071/46670217-8e11b880-cc0c-11e8-8f9d-fff897e193cf.png" alt="image" style="max-width:50%;"></a>

### 2.1.1 정의
## 2.2 블로킹과 논블로킹
- 블로킹 : 요청한 작업이 성공하거나 에러가 발생하기 전까지는 응답을 돌려주지 않음
- 논블로킹 : 요청한 작업의 성공 여부와 상관없이 바로 결과를 돌려주는 것. 이때 요청의 응답값에 의해서 에러나 성공 여부를 판단한다.

JDK 1.4 부터 NIO라는 논블로킹 I/O API가 추가되었다. 입출력과 관련된 기능을 제공하는데, 소켓도 입출력 채널의 하나로서 NIO API를 사용할 수 있으며 NIO API 를 통해서 블로킹과 논블로킹 모드의 소켓을 사용할 수 있다.

### 2.2.1 블로킹 소켓
블룅 소켓은 ServerSocket, Socket 클래스, 논블로킹 소켓은 ServerSocketChannel, SOcketChannel 클래스를 사용한다.

```java
    // Blocking 소스
    public class BlockingServer {
        public static void main(String[] args) throws Exception {
            BlockingServer server = new BlockingServer();
            server.run();
        }
    
        private void run() throws IOException {
            ServerSocket server = new ServerSocket(8888);
            System.out.println("접속 대기중");
    
            while (true) {
                Socket sock = server.accept();
    
                // client 가 접속하지 않으면 해당 출력문은 출력되지 않음.
                System.out.println("클라이언트 연결됨");
    
                OutputStream out = sock.getOutputStream();
                InputStream in = sock.getInputStream();
    
                while (true) {
                    try {
    
                        int request = in.read();
                        out.write(request);
                    } catch (IOException e) {
                        break;
                    }
                }
            }
        }
    }
```

<p><a target="_blank" rel="noopener noreferrer" href="https://user-images.githubusercontent.com/6028071/46670269-a84b9680-cc0c-11e8-8283-cf54726e9941.png"><img src="https://user-images.githubusercontent.com/6028071/46670269-a84b9680-cc0c-11e8-8283-cf54726e9941.png" alt="image" style="max-width:50%;"></a></p>

메소드 별로 블로킹 되는 위치를 표시 한다.

블로킹 소켓은 데이터 입출력에서 스레드의 블로킹이 발생하기 떄문에 동시에 여러 클라이언트에 대한 처리가 불가능하게 된다. 
그래서 이를 해결하기 위해서 등장한 모델은 연결된 클라이언트별로 각각 스레드를 할당하는 방법이다. 
서버에 연결된 클라이언트마다 각각 새로운 스레드를 생성하는 구조를 가진 서버를 밑에 그림으로 표현.

<p><a target="_blank" rel="noopener noreferrer" href="https://user-images.githubusercontent.com/6028071/46670299-c1544780-cc0c-11e8-8287-4b846618cfd7.png"><img src="https://user-images.githubusercontent.com/6028071/46670299-c1544780-cc0c-11e8-8287-4b846618cfd7.png" alt="image" style="max-width:70%;"></a></p>

클라이언트가 서버에 접속하면 서버 소켓의 accept 메소드를 통해 연결된 클라이언트 소켓을 얻어온다. 
이떄 블로킹 소켓은 I/O 처리에 블로킹이 발생하기 때문에 *새로운 스레드를 하나 생성(OOM 의 문제 발생 가능성)*하고 그 쓰레드에게 클라이언트 소켓에 대한 I/O 처리를 넘겨주면된다.

[그림 2-4]를 살펴보면 서버 소켓의 accept 메서드가 병목 지점이다. accept 메서드는 단위 시간에 하나의 연결만을 처리하는 블로킹 모드로 동작하기 때문에 여러 클라이언트가 동시에 접속 요청을 하는 상황에 대기시간이 길어진다는 단점이 있다.
또한 접속할 클라이언트 수가 정해져 있지 않은 상황에서도 문제가 발생할 수 있다. **서버에 접속하는 클라이언트 수가 증가하면 애플리케이션 서버의 스레드 수가 증가하게 되는데,
이때 바자의 힙 메모리 부족으로 인한 OOM 오류가 발생할 수 있다.**
 
위와 같은 서비스 불가 상황이 발생하지 않도록 하려면 서버에서 생성되는 스레드 수를 제한하는 방법인 스레드 풀링을 사용하기도 한다. ([그림 2-5] 참고)

<p><a target="_blank" rel="noopener noreferrer" href="https://user-images.githubusercontent.com/6028071/46670329-d8933500-cc0c-11e8-8ca4-4557bca07ad9.png"><img src="https://user-images.githubusercontent.com/6028071/46670329-d8933500-cc0c-11e8-8ca4-4557bca07ad9.png" alt="image" style="max-width:70%;"></a></p>

[그림 2.-5]는 [그림 2-4]의 단점인 스레드 증가에 따른 OOM 오류를 피하기 위해 스레드 풀을 사용한다.
클라이언트가 서버에 접속하면 서버 소켓으로부터 클라이언트 소켓을 얻은 다음 스레드 풀에서 가용 스레드를 하나 가져오고 해당 스레드에 클라이언트 소켓을 할당.

위와 같은 구조에서는 동시에 접속 가능한 사용자 수가 스레드 풀에 지정된 스레드 수에 의존하는 현상 발생.
동시 접속이란 동일한 시간에 서버에 연결되어 있는 클라이언트 수를 의미한다. 동시 접속 수를 늘리기 위해서 스레드 풀의 크기를 고려해야 한다.

> 블로킹 소켓의 동작 방식으로 인하여 블로킹 소켓을 사용한 서버는 충분한 동시접속 사용자를 수용하지 못한다. 이런 단점을개선한 방식이 논블록킹 소켓 방식인다.

### 2.2.2 논블로킹 소켓 

앞에서 살펴본 블로킹 모드의 소켓은 read, write, accept 메소드 등과 같은 입출력 메서드가 호출되면 처리가 완료될 때까지 스레드가 멈추게 되어 다른 처리를 할 수 없었다.

이와 같은 단점을 해결하는 방식이 논블로킹 소켓이다. 

블로킹 소켓과 논블로킹 방식의 가장 큰 차이점은 I/O 처리 방법에 있다. 두 동작 방식을 아래의 그림에서 확인할 수 있다.

<a target="_blank" rel="noopener noreferrer" href="https://user-images.githubusercontent.com/6028071/46704272-39e6f280-cc65-11e8-8786-166e4170c3b9.png"><img src="https://user-images.githubusercontent.com/6028071/46704272-39e6f280-cc65-11e8-8786-166e4170c3b9.png" alt="image" style="max-width:50%;"></a>

## 2.3 이벤트 기반 프로그래밍

> 이벤트를 먼저 정의해 두고 발생한 이벤트에 따라 코드가 실행되도록 프로그램을 작성하는 것이 이벤트 기반 프로그래밍이다.

### 2.3.1 이벤트 기반 네트워크 프로그래밍

이벤트 기반 프로그래밍을 제공하려면 먼저 이벤트를 발생하는 객체를 정의해야 한다. 즉 이벤트가 발생할 주체를 정의하고 그 주체에서 발생될 이벤트 종류를 정의해야 한다.
네트워크 프로그램에서 일벤트가 발생하는 주체는 소켓이다. 그리고 발생하는 이벤트는 크게 *소켓 연결, 데이터 송수신*으로 나눌 수 있다.

네트워크 프로그램에서 클라이언트와 서버가 어떻게 데이터를 통신하는지 [그림 2-7] 에서 살펴보자.

<p><a target="_blank" rel="noopener noreferrer" href="https://user-images.githubusercontent.com/6028071/46704279-44a18780-cc65-11e8-9db9-a6e1109acb92.png"><img src="https://user-images.githubusercontent.com/6028071/46704279-44a18780-cc65-11e8-9db9-a6e1109acb92.png" alt="image" style="max-width:50%;"></a></p>

1. 서버는 클라이언트의 연결을 수락하기 위해서 서버 소켓을 생성하고 포트를 서버 소켓에 바인딩 한다.
2. 클라이언트의 연결을 수락하고 클라이언트의 데이터를 송수신할 소켓을 생성
3. 클라이언트가 서버의 지정된 포트로 연결을 시도하면 서버는 서버 소켓으로 부터 클라이언트와 연결된 소켓을 생성
4. 클라이언트, 서버 소켓 통신 시작.

동작 기반의 네트워크 프로그램을 작성한다면 [그림 2-8]과 같은 방법으로 데이터를 송수신해야 한다.

![image](https://user-images.githubusercontent.com/6028071/46704285-4d925900-cc65-11e8-8a36-738536385f47.png)

> 소켓이란 데이터 송수신을 위한 네트워크 추상화 단위로, 일반적으로 네트워크 프로그램에서 소켓은
> IP 주소와 포트를 가지고 있으며 양방향 네트워크 통신이 가능한 객체다.

소켓에 데이터를 기록하고 읽으려면 [그림 2-8]과 같이 소켓에 연결된 소켓 채널 (NIO) 또는 스크림(Old Blocking IO)을 사용해야 한다.

[그림 2-9] 는 네티가 소켓을 통해서 데이터를 송수신하는 방법을 표현하고 있다. 
[그림 2-8] 과 같이 데이터를 소켓으로 전송하기 위해서 채널에 직접 기록하는 것이 아니라 데이터 핸들러를 통해서 기록한다.

<p><a target="_blank" rel="noopener noreferrer" href="https://user-images.githubusercontent.com/6028071/46704285-4d925900-cc65-11e8-8a36-738536385f47.png"><img src="https://user-images.githubusercontent.com/6028071/46704285-4d925900-cc65-11e8-8a36-738536385f47.png" alt="image" style="max-width:50%;"></a></p>

이벤트 기반 코드의 장점
- 서버 애플리케이션의 코드를 클라이언트 애플리케이션에서 재사용
- 각 이벤트에 따라서 로직을 분리
- 네티의 이벤트 핸들러는 에러 이벤트도 같이 정의한다. 이로 인해서 특정 이벤트에 대한 로직을 작성할 때 에러 처리에 대한 부담을 덜어준다.

# 3장. 부트스트랩

부트스트랩 설정
- 이벤트 루프 : 소켓 채널에서 발생한 이벤트를 처리하는 스레드 모델에 대한 구현이 담겨 있다. 
- 채널의 전송 모드 : 블로킹, 논블로킹, epoll. epoll은 입출력 다중화 기법으로써 현재 까지 알려진 입출력 방식 중에 가장 빠르다. 단, 리룩스 커널 2.6 이상에서만 사용가능
- 채널 파이프라인 : 소켓 채널로 수신된 데이터를 처리할 데이터 핸들러들을 지정

## 3.1 부트스트랩의 정의

부트스트랩은 네티로 작성한 네트워크 애플리네이션의 동작 방식과 환경을 설정하는 도우미 클래스.

## 3.2 부트스트랩의 구조
<p><a target="_blank" rel="noopener noreferrer" href="https://user-images.githubusercontent.com/6028071/46806947-d31c2300-cda3-11e8-9099-1495e3c9a612.png"><img src="https://user-images.githubusercontent.com/6028071/46806947-d31c2300-cda3-11e8-9099-1495e3c9a612.png" alt="image" style="max-width:50%;"></a></p>

[그림 3-1]은 부트스트랩이 지원하는 설정 목록을 보여주는데, 실제로 부트스트랩은 네트워크 애플리케이션 설정에 필요한 모든 내용을 담고 있다.

일반적인 네트워크 애플리케이션은 구성요소
- 서비스를 제공할 네트워크 *포트*
- 네트워크 전송에 사용할 *소켓 모드와 소켓 옵션*
- 소켓의 데이터를 처리하는 *스레드*
- 애플리케이션에서 사용하는 *프로토콜*

네티 부트스트랩은 다음과 같이 2가지 클래스로 나뉜다.
- ServerBootstrap 클래스 : 서버 애플리케이션
- Bootstrap 클래스: 클라이언트 애플리케이션

여기서 말하는 서버 애플리케이션과 클라이언트 애플리케이션의 구분은 소켓 연결을 요청하느냐 아니면 대기 하느냐에 따른 구분이다.

![image](https://user-images.githubusercontent.com/6028071/46806964-e0391200-cda3-11e8-83a1-5e890af8911a.png)

## 3.3 ServerBootstrap

EchoServer 소스 참고

### 3.3.1 ServerBootstrap API

#### group - 이벤트 루프 설정

클라이언트는 연결 요청이 완료된 이후의 데이터 송수신 처리를 위해서 하나의 이벤트 루프로 모든 처리가 가능하다.
반대로 서버는 클라이언트의 연결 요청을 수락하기 위한 이벤트 루프와 데이터 송수신 처리를 위한 이벤트 루프 이렇게 두 종류의 이벤트 루프가 필요하다.

```java
    public class ServerBootstrap extends AbstractBootstrap<ServerBootstrap, ServerChannel> {
        ....
        @Override
        public ServerBootstrap group(EventLoopGroup group) {
            return group(group, group);
        }
    
        public ServerBootstrap group(EventLoopGroup parentGroup, EventLoopGroup childGroup) {
            super.group(parentGroup);
            if (childGroup == null) {
                throw new NullPointerException("childGroup");
            }
            if (this.childGroup != null) {
                throw new IllegalStateException("childGroup set already");
            }
            this.childGroup = childGroup;
            return this;
        }
        
```

ServerBootstrap 의 group 을 설정하는 부분은 2가지 지만 파라미터가 1개인 group 설정은 parent와 child group 을 동일하게 설정하게 된다.

#### channel - 소켓 입출력 모드 설정

channel = 입출력 모드를 설정

channel 메소드는 AbstractBootstrap 추상 클래스의 구현체인 SeverBootstrap 과 Bootstrap 클래스에 모두 존재하는 API며 부트스트랩 클래스를 통해서 생성된 채널의 입출력 모드를 설정할 수 있다.

●LocalServerChannel.class
하나의 자바 가상머신에서 가상 통신을 위한 서버 소켓 채널을 생성하는 클래스

※ 통상적으로 하나의 어플리케이션 내에서 클라이언트와 서버를 모두 구현하고 애플리케이션 안에서 소켓 통신을 수행할 때 사용한다.

● OioServerSocketChannel.class
블로킹 모드의 서버 소켓 채널을 생성하는 클래스

● NioServerSocketChannel.class
논블로킹 모드의 서버 소켓 채널을 생성하는 클래스

● EpollServerSocketChannel.class
리눅스 커널의 epoll 입출력 모드를 지원하는 서버 소켓 채널을 생성하는 클래스

● OioSctpServerChannel.class
SCTP 전송 계층을 사용하는 블로킹 모드의 서버 소켓 채널을 생성하는 클래스

● NioSctpServerChannel.class
SCATP 전송 계층을 사용하는 논블로킹 모드의 서버 소켓 채널을 생성하는 클래스

● NioUdtByteAcceptorChannel.class
UDT 프로토콜을 지원하는 논블로킹 모드의 서버 소켓 채널을 생성하는 클래스.
내부적으로 스트림 데이터를 처리하도록 구현되어 있으며 barchart-udt 라이브러리를 사용한다.

※ barchart-udt : https://goo.gl/CGZ6vu

● NioUdtMessageAcceptorChannel.class
UDT 프로토콜을 지원하는 블로킹 모드의 서버 소켓 채널을 생성하는 클래스.
내부적으로 데이터그램 패킷을 처리하도록 구현되어 있다.
위에 나열된 클래스의 설명에 서버 소켓 채널을 생성하는 클래스들은 모두 io.netty.channel 패키지의 ServerChannel 인터페이스를 구현하고 있다.

<p><a target="_blank" rel="noopener noreferrer" href="https://user-images.githubusercontent.com/6028071/46807012-fd6de080-cda3-11e8-9962-0b4742d2a679.png"><img src="https://user-images.githubusercontent.com/6028071/46807012-fd6de080-cda3-11e8-9962-0b4742d2a679.png" alt="image" style="max-width:50%;"></a></p>

<p><a target="_blank" rel="noopener noreferrer" href="https://user-images.githubusercontent.com/6028071/46807027-052d8500-cda4-11e8-9285-606cac781185.png"><img src="https://user-images.githubusercontent.com/6028071/46807027-052d8500-cda4-11e8-9285-606cac781185.png" alt="image" style="max-width:50%;"></a></p>

<p><a target="_blank" rel="noopener noreferrer" href="https://user-images.githubusercontent.com/6028071/46807047-0f4f8380-cda4-11e8-997d-172ee0c78db5.png"><img src="https://user-images.githubusercontent.com/6028071/46807047-0f4f8380-cda4-11e8-997d-172ee0c78db5.png" alt="image" style="max-width:50%;"></a></p>

<p><a target="_blank" rel="noopener noreferrer" href="https://user-images.githubusercontent.com/6028071/46807067-1b3b4580-cda4-11e8-8b57-7201e6cd8604.png"><img src="https://user-images.githubusercontent.com/6028071/46807067-1b3b4580-cda4-11e8-8b57-7201e6cd8604.png" alt="image" style="max-width:50%;"></a></p>

<p><a target="_blank" rel="noopener noreferrer" href="https://user-images.githubusercontent.com/6028071/46807087-22faea00-cda4-11e8-9a95-ee673e6a2f9b.png"><img src="https://user-images.githubusercontent.com/6028071/46807087-22faea00-cda4-11e8-9a95-ee673e6a2f9b.png" alt="image" style="max-width:50%;"></a></p>

<p><a target="_blank" rel="noopener noreferrer" href="https://user-images.githubusercontent.com/6028071/46807124-31490600-cda4-11e8-8f42-b15f18ae22c9.png"><img src="https://user-images.githubusercontent.com/6028071/46807124-31490600-cda4-11e8-8f42-b15f18ae22c9.png" alt="image" style="max-width:50%;"></a></p>

<p><a target="_blank" rel="noopener noreferrer" href="https://user-images.githubusercontent.com/6028071/46807138-3ad26e00-cda4-11e8-9ce3-2f06bda27888.png"><img src="https://user-images.githubusercontent.com/6028071/46807138-3ad26e00-cda4-11e8-9ce3-2f06bda27888.png" alt="image" style="max-width:50%;"></a></p>

<p><a target="_blank" rel="noopener noreferrer" href="https://user-images.githubusercontent.com/6028071/46807209-5b9ac380-cda4-11e8-830c-58209d6ae108.png"><img src="https://user-images.githubusercontent.com/6028071/46807209-5b9ac380-cda4-11e8-830c-58209d6ae108.png" alt="image" style="max-width:50%;"></a></p>

![image](https://user-images.githubusercontent.com/6028071/46807012-fd6de080-cda3-11e8-9962-0b4742d2a679.png)

![image](https://user-images.githubusercontent.com/6028071/46807027-052d8500-cda4-11e8-9285-606cac781185.png)

![image](https://user-images.githubusercontent.com/6028071/46807047-0f4f8380-cda4-11e8-997d-172ee0c78db5.png)

![image](https://user-images.githubusercontent.com/6028071/46807067-1b3b4580-cda4-11e8-8b57-7201e6cd8604.png)

![image](https://user-images.githubusercontent.com/6028071/46807087-22faea00-cda4-11e8-9a95-ee673e6a2f9b.png)

![image](https://user-images.githubusercontent.com/6028071/46807124-31490600-cda4-11e8-8f42-b15f18ae22c9.png)

![image](https://user-images.githubusercontent.com/6028071/46807138-3ad26e00-cda4-11e8-9ce3-2f06bda27888.png)

![image](https://user-images.githubusercontent.com/6028071/46807209-5b9ac380-cda4-11e8-830c-58209d6ae108.png)







