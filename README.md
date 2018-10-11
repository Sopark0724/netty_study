# netty_study

## 1장. 네티소개
chapter 1 소스 참고

## 2장 네티의 주요 특징

### 2.1 동기와 비동기
![image](https://user-images.githubusercontent.com/6028071/46670102-383d1080-cc0c-11e8-9a6a-bf8ecda691ec.png | width=100) 

![image](https://user-images.githubusercontent.com/6028071/46670217-8e11b880-cc0c-11e8-8f9d-fff897e193cf.png)

#### 2.1.1 정의
### 2.2 블로킹과 논블로킹
- 블로킹 : 요청한 작업이 성공하거나 에러가 발생하기 전까지는 응답을 돌려주지 않음
- 논블로킹 : 요청한 작업의 성공 여부와 상관없이 바로 결과를 돌려주는 것. 이때 요청의 응답값에 의해서 에러나 성공 여부를 판단한다.

JDK 1.4 부터 NIO라는 논블로킹 I/O API가 추가되었다. 입출력과 관련된 기능을 제공하는데, 소켓도 입출력 채널의 하나로서 NIO API를 사용할 수 있으며 NIO API 를 통해서 블로킹과 논블로킹 모드의 소켓을 사용할 수 있다.

#### 2.2.1 블로킹 소켓
블룅 소켓은 ServerSocket, Socket 클래스, 논블로킹 소켓은 ServerSocketChannel, SOcketChannel 클래스를 사용한다.
    
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
    

![image](https://user-images.githubusercontent.com/6028071/46670269-a84b9680-cc0c-11e8-8283-cf54726e9941.png)

메소드 별로 블로킹 되는 위치를 표시 한다.

블로킹 소켓은 데이터 입출력에서 스레드의 블로킹이 발생하기 떄문에 동시에 여러 클라이언트에 대한 처리가 불가능하게 된다. 
그래서 이를 해결하기 위해서 등장한 모델은 연결된 클라이언트별로 각각 스레드를 할당하는 방법이다. 
서버에 연결된 클라이언트마다 각각 새로운 스레드를 생성하는 구조를 가진 서버를 밑에 그림으로 표현.

![image](https://user-images.githubusercontent.com/6028071/46670299-c1544780-cc0c-11e8-8287-4b846618cfd7.png)

클라이언트가 서버에 접속하면 서버 소켓의 accept 메소드를 통해 연결된 클라이언트 소켓을 얻어온다. 
이떄 블로킹 소켓은 I/O 처리에 블로킹이 발생하기 때문에 **새로운 스레드를 하나 생성(OOM 의 문제 발생 가능성)**하고 그 쓰레드에게 클라이언트 소켓에 대한 I/O 처리를 넘겨주면된다.

[그림 2-4]를 살펴보면 서버 소켓의 accept 메서드가 병목 지점이다. accept 메서드는 단위 시간에 하나의 연결만을 처리하는 블로킹 모드로 동작하기 때문에 여러 클라이언트가 동시에 접속 요청을 하는 상황에 대기시간이 길어진다는 단점이 있다.
또한 접속할 클라이언트 수가 정해져 있지 않은 상황에서도 문제가 발생할 수 있다. **서버에 접속하는 클라이언트 수가 증가하면 애플리케이션 서버의 스레드 수가 증가하게 되는데,
이때 바자의 힙 메모리 부족으로 인한 OOM 오류가 발생할 수 있다.**
 
위와 같은 서비스 불가 상황이 발생하지 않도록 하려면 서버에서 생성되는 스레드 수를 제한하는 방법인 스레드 풀링을 사용하기도 한다. ([그림 2-5] 참고)

![image](https://user-images.githubusercontent.com/6028071/46670329-d8933500-cc0c-11e8-8ca4-4557bca07ad9.png)

[그림 2.-5]는 [그림 2-4]의 단점인 스레드 증가에 따른 OOM 오류를 피하기 위해 스레드 풀을 사용한다.
클라이언트가 서버에 접속하면 서버 소켓으로부터 클라이언트 소켓을 얻은 다음 스레드 풀에서 가용 스레드를 하나 가져오고 해당 스레드에 클라이언트 소켓을 할당.

위와 같은 구조에서는 동시에 접속 가능한 사용자 수가 스레드 풀에 지정된 스레드 수에 의존하는 현상 발생.
동시 접속이란 동일한 시간에 서버에 연결되어 있는 클라이언트 수를 의미한다. 동시 접속 수를 늘리기 위해서 스레드 풀의 크기를 고려해야 한다.

> 블로킹 소켓의 동작 방식으로 인하여 블로킹 소켓을 사용한 서버는 충분한 동시접속 사용자를 수용하지 못한다. 이런 단점을개선한 방식이 논블록킹 소켓 방식인다.

#### 2.2.2 




