# Movie Booking Program

Java Swing, Socket, Jackson 기반의 영화 예매 프로그램입니다. 사용자는 GUI에서 로그인 후 영화, 상영 시간, 좌석을 선택해 예매할 수 있고, 예매 내역 조회 및 취소도 할 수 있습니다. 서버는 클라이언트 요청을 JSON 기반 프로토콜로 받아 처리하고, 데이터는 `data/movie-booking.json` 파일에 저장합니다.

## 실행 방법

이 프로젝트는 Jackson 라이브러리 jar 파일을 `lib/` 폴더에 포함합니다. 따라서 Maven을 따로 실행하지 않아도 터미널에서 바로 컴파일하고 실행할 수 있습니다.

처음 실행하는 경우 스크립트 실행 권한을 부여합니다.

```sh
chmod +x compile.sh run-server.sh run-client.sh generate-data.sh
```

컴파일만 수행하려면 다음 명령을 사용합니다.

```sh
./compile.sh
```

서버를 먼저 실행합니다.

```sh
./run-server.sh
```

다른 터미널에서 GUI 클라이언트를 실행합니다.

```sh
./run-client.sh
```

임의의 테스트 데이터를 다시 생성하려면 다음 명령을 실행합니다.

```sh
./generate-data.sh
```

Windows 환경에서 직접 실행하는 경우 classpath 구분자가 `:` 대신 `;`이므로 다음처럼 실행합니다.

```sh
./compile.sh
java -cp "out;lib/*" server.randomDomainGenerator
```

제출 시에는 `src/`, `data/`, `lib/`, `compile.sh`, `run-server.sh`, `run-client.sh`, `generate-data.sh`, `README.md`를 포함해야 합니다. `out/`과 `target/`은 컴파일 결과물이므로 제출하지 않아도 됩니다.

## 전체 구조

프로그램은 클라이언트 GUI, 컨트롤러, API 서비스, 소켓 통신, 서버 핸들러, 저장소 계층으로 나뉩니다.

```text
GUI Panel
  -> Controller
    -> ApiService
      -> SocketClient
        -> Socket
          -> ClientHandler
            -> DataRepository
              -> movie-booking.json
```

응답은 반대 방향으로 전달됩니다.

```text
movie-booking.json
  -> DataRepository
    -> ClientHandler
      -> SocketClient
        -> ApiService
          -> Controller
            -> GUI Panel
```

성공 응답은 주로 `data`만 전달하고, 성공 메시지는 컨트롤러에서 화면 흐름에 맞게 표시합니다. 실패 응답은 서버에서 `message`를 담아 보내고, `ApiService`가 이를 `ApiException`으로 변환해 컨트롤러에 전달합니다.

## 패키지 구조

### `domain`

프로그램의 핵심 도메인 객체입니다.

- `User`: 사용자 계정 정보입니다.
- `Movie`: 영화 정보입니다.
- `Theater`: 상영관 정보입니다.
- `TheaterType`: 상영관 타입입니다.
- `Showtime`: 영화, 상영관, 시작 시간, 예약된 좌석 정보를 가집니다.
- `Reservation`: 예매 정보입니다.
- `ReservationStatus`: 예매 상태입니다. `CONFIRMED`, `CANCELED`를 가집니다.

### `repository`

JSON 파일 기반 데이터 저장소입니다.

- `MovieBookingData`: JSON 파일 전체 구조를 표현하는 루트 데이터 클래스입니다.
- `DataRepository`: 서버 실행 시 JSON 파일을 읽어 메모리에 보관하고, 회원/영화/상영관/상영일정/예약 데이터를 조회하거나 수정합니다. 변경이 생기면 `movie-booking.json`에 다시 저장합니다.

`DataRepository`의 주요 기능은 다음과 같습니다.

- 사용자 등록 및 로그인
- 영화 목록 및 영화 단건 조회
- 상영 일정 조회
- 상영관 조회
- 예약 생성
- 예약 취소
- 사용자별 예약 내역 조회
- 좌석 유효성 및 중복 예약 검증

### `server`

서버 실행과 클라이언트 요청 처리를 담당합니다.

- `MovieBookingServer`: 서버 시작점입니다. `ServerSocket`으로 클라이언트 연결을 받고, 연결마다 `ClientHandler`를 별도 스레드로 실행합니다.
- `ClientHandler`: 클라이언트 요청을 읽고 command에 따라 로그인, 영화 목록, 예매, 취소 등의 작업으로 분기합니다.
- `randomDomainGenerator`: 테스트용 사용자, 영화, 상영관, 상영 일정, 예약 데이터를 생성해 JSON 파일에 저장합니다.

### `protocol`

클라이언트와 서버가 주고받는 메시지 형식입니다.

- `Request`: command와 body를 담는 요청 객체입니다.
- `Response`: 성공 여부, 실패 메시지, 응답 데이터를 담는 응답 객체입니다.

### `client`

서버와의 실제 소켓 통신을 담당합니다.

- `SocketClient`: `Request` 객체를 JSON으로 직렬화해 서버에 보내고, 서버 응답 JSON을 `Response` 객체로 역직렬화합니다.

### `service`

클라이언트에서 서버 API를 메서드 형태로 사용할 수 있게 감쌉니다.

- `ApiService`: 서버 command 문자열과 요청 body 생성을 한 곳에서 관리합니다. `SocketClient`를 이용해 서버에 요청하고, 응답 데이터를 도메인 객체로 변환합니다.
- `ApiException`: 서버 요청 실패, 통신 실패, 응답 변환 실패를 컨트롤러에 전달하기 위한 예외입니다.

### `controller`

GUI 이벤트를 받아 세션 값을 변경하고 화면 흐름을 제어합니다.

- `AuthController`: 로그인, 회원가입, 로그아웃을 처리하고 `UserSession`을 관리합니다.
- `BookingController`: 영화 선택, 상영 시간 선택, 좌석 선택, 예매 생성을 처리하고 `BookingSession`을 관리합니다.
- `ReservationController`: 예매 내역 조회와 예약 취소를 처리합니다.
- `NavigationController`: 화면 이동, 메시지 창, 확인 창을 담당합니다.

### `session`

클라이언트 실행 중 필요한 임시 상태를 저장합니다.

- `UserSession`: 현재 로그인한 사용자 정보를 저장합니다.
- `BookingSession`: 선택한 영화, 상영 시간, 상영관, 좌석 정보를 저장합니다.

세션 클래스는 getter/setter 중심의 상태 저장 객체이고, 실제 상태 변경 흐름은 컨트롤러에서 관리합니다.

### `gui`

Swing 화면을 담당합니다.

- `MovieBookingClient`: GUI 클라이언트 프로그램의 시작점입니다.
- `MainFrame`: 전체 화면을 `CardLayout`으로 관리합니다. 화면 이름과 패널 객체를 `Map<String, JPanel>`로 묶어 저장합니다.
- `BasePanel`: 공통 타이틀 영역과 콘텐츠 영역을 제공하는 패널 부모 클래스입니다.
- `Refreshable`: 화면 진입 시 데이터를 갱신해야 하는 패널이 구현하는 인터페이스입니다.
- `LoginPanel`: 로그인 및 회원가입 화면입니다.
- `HomePanel`: 로그인 후 홈 화면입니다.
- `MovieListPanel`: 영화 목록을 표시하고 영화를 선택합니다.
- `ShowtimePanel`: 선택한 영화의 상영 시간 목록을 표시합니다.
- `SeatSelectionPanel`: 좌석 선택 및 예매 요청 화면입니다.
- `ReservationPanel`: 로그인 사용자의 예매 내역을 표시하고 예약 취소를 요청합니다.

## 데이터 저장 방식

데이터는 `data/movie-booking.json` 파일에 저장됩니다. 서버가 시작될 때 JSON 파일을 읽어 `MovieBookingData` 객체로 메모리에 올리고, 회원가입/예약/취소 등으로 데이터가 바뀌면 다시 파일에 저장합니다.

서버 실행 중 JSON 파일을 외부에서 직접 수정하면 서버 메모리에는 자동 반영되지 않습니다. 데이터 변경은 프로그램을 통해 수행하는 것을 기준으로 합니다.

## 멀티스레딩

서버는 클라이언트 연결마다 별도 스레드를 생성합니다.

```java
Thread thread = new Thread(new ClientHandler(socket, repository));
thread.start();
```

모든 `ClientHandler`는 하나의 `DataRepository` 인스턴스를 공유합니다. `DataRepository`의 public 메서드는 `synchronized`로 선언되어 있어, 여러 클라이언트가 동시에 요청해도 저장소 접근이 한 번에 하나씩 처리됩니다.

## 주요 기능

- 로그인 및 회원가입
- 영화 목록 조회
- 영화별 상영 시간 조회
- 상영관별 좌석 표시
- 좌석 선택 및 예매
- 사용자별 예매 내역 조회
- 예약 취소
- JSON 테스트 데이터 생성

## 기본 데이터

`data/movie-booking.json`에는 테스트용 사용자, 영화, 상영관, 상영 일정, 예약 정보가 들어 있습니다. `randomDomainGenerator`를 실행하면 다음 규모의 임의 데이터가 생성됩니다.

- 사용자 10명
- 영화 10개
- 상영관 8개
- 상영 일정 30개
- 예약 30개

예약 데이터 생성 시 `Reservation.seatCodes`와 `Showtime.reservedSeats`가 일치하도록 생성합니다.
