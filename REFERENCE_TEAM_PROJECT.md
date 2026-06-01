# Team Project Final Reference

## 문서 목적

이 문서는 `C:\Users\mikey\Desktop\Team Project`에 있는 기존 영화 예매 프로그램을 앞으로 현재 프로젝트에서 참고하기 위한 작업용 기준서다.

이 파일을 먼저 읽으면 기존 프로젝트의 구조, 흐름, 핵심 설계 원칙, 검증 방법, 주의할 한계를 빠르게 복원할 수 있도록 정리했다. 원본 `Team Project`는 수정하지 않았고, 이 요약 문서는 현재 작업 폴더에 둔다.

원본 위치:

```text
C:\Users\mikey\Desktop\Team Project
```

참고 문서 위치:

```text
C:\Users\mikey\Desktop\영화예매프로그램\REFERENCE_TEAM_PROJECT.md
```

## 한 줄 요약

Java Swing 클라이언트와 Socket 서버가 line-delimited JSON으로 통신하며, 서버가 좌석 중복 예약 방지, 좌석 시야 점수 기반 가격 계산, 그룹 예매 All-or-Nothing 흐름을 처리하는 영화 예매 시스템이다.

## 가장 중요한 설계 원칙

앞으로 이 프로젝트를 참고하거나 확장할 때 아래 원칙은 우선 유지한다.

1. GUI는 화면 입력과 이동만 담당한다.
2. 예매 가능 여부, 가격 계산, 좌석 확정 판단은 서버가 담당한다.
3. 클라이언트가 본 좌석 상태와 가격은 참고용 snapshot일 뿐이다.
4. 최종 예매 시 서버가 좌석 상태와 가격을 다시 검증한다.
5. 좌석 확인과 상태 변경은 `DataRepository`의 `synchronized` 메서드 안에서 한 번에 처리한다.
6. 상영관 타입은 `Theater` 추상 클래스와 하위 클래스로 표현한다.
7. 가격 계산은 `PricingStrategy` 인터페이스 구현체로 분리한다.
8. 좌석 위치 분석은 `ViewScoreCalculator`가 담당한다.
9. 그룹 예매는 전원 결제 전까지 `HELD`이고, 전원 `PAID`일 때만 `CONFIRMED`가 된다.
10. 저장소는 현재 메모리 기반이며, 서버 재시작 시 사용자와 예매 데이터는 초기화된다.

## 전체 아키텍처

```text
Swing GUI
 -> SessionManager
 -> ClientConnection
 -> Socket
 -> line-delimited JSON
 -> ClientHandler Thread
 -> RequestRouter
 -> AuthService / DataRepository / Domain
```

패키지별 책임:

- `moviebooking.client`: Swing 화면, 클라이언트 Socket 연결, 화면별 세션
- `moviebooking.server`: 서버 실행, 클라이언트 연결 처리, 요청 라우팅, 인증
- `moviebooking.domain`: 영화, 상영관, 좌석, 예매, 그룹, 가격 정책
- `moviebooking.repository`: 메모리 저장소, 좌석 상태, 예매 상태, 동시성 제어
- `moviebooking.protocol`: 요청/응답 DTO, 요청 타입, 오류 정보, 자체 JSON codec
- `moviebooking.session`: `Session` 인터페이스와 화면 스택 관리
- `moviebooking.testing`: 발표/개발용 smoke test
- `docs`: 설계, 프로토콜, 도메인, 발표, 시연 문서

## 실행 진입점

서버:

```text
src\moviebooking\server\ServerMain.java
```

서버 실행 흐름:

```text
ServerMain.main()
 -> DataRepository 생성
 -> DemoDataSeeder.seed(repository)
 -> AuthService 생성
 -> RequestRouter 생성
 -> MovieBookingServer.start()
 -> ServerSocket.accept()
 -> 클라이언트마다 ClientHandler Thread 생성
 -> JSON 한 줄 단위 요청 처리
```

클라이언트:

```text
src\moviebooking\client\ClientMain.java
```

클라이언트 실행 흐름:

```text
ClientMain.main()
 -> SwingClientApp.start()
 -> ClientConnection 생성
 -> JFrame 생성
 -> SessionManager.start(LoginSession)
```

## 주요 화면 흐름

```text
LoginSession
 -> MovieListSession
 -> ScheduleListSession
 -> SeatLayoutSession
 -> ReservationDetailSession
```

보조 화면:

```text
MovieListSession
 -> MyAccountSession
```

각 화면의 역할:

- `LoginSession`: 회원가입, 로그인, 비밀번호 입력 필터링
- `MovieListSession`: 영화 목록 조회, MyAccount 이동, 그룹 결제 상태 갱신 진입
- `ScheduleListSession`: 선택 영화의 상영 일정과 상영관 조회
- `SeatLayoutSession`: 좌석 배치 조회, 개인/그룹 예매 생성
- `ReservationDetailSession`: 예매 상세 조회, 그룹 내 결제, 그룹 확정
- `MyAccountSession`: 현재 사용자가 볼 수 있는 개인/그룹 예매 목록 조회

화면 전환 구조:

- 모든 화면은 `Session` 인터페이스를 구현한다.
- `SessionManager`는 `Stack<Session>`으로 화면 이동, 뒤로 가기, home 성격의 재시작을 관리한다.
- `Home` 버튼은 중간 화면 스택을 정리하고 영화 목록 화면으로 돌아가는 방식이다.

## 서버 요청 흐름

요청은 `RequestType` enum으로 구분된다.

지원 요청:

- `SIGN_UP`
- `LOGIN`
- `LOGOUT`
- `GET_MOVIE_LIST`
- `GET_THEATER_SCHEDULE`
- `GET_SEAT_LAYOUT`
- `CREATE_PERSONAL_RESERVATION`
- `CREATE_GROUP_RESERVATION`
- `UPDATE_GROUP_PAYMENT_STATUS`
- `CONFIRM_GROUP_RESERVATION`
- `GET_RESERVATION_DETAIL`
- `GET_MY_RESERVATIONS`

일반 처리 순서:

```text
ClientConnection.send(BaseRequest)
 -> ProtocolCodec.encodeRequest()
 -> Socket writeLine()
 -> ClientHandler.readLine()
 -> ProtocolCodec.decodeRequest()
 -> RequestRouter.route()
 -> handler method
 -> BaseResponse
 -> ProtocolCodec.encodeResponse()
 -> Socket writeLine()
 -> ClientConnection.decodeResponse()
```

## JSON 프로토콜

요청 공통 형태:

```json
{
  "requestId": "REQ-0001",
  "type": "LOGIN",
  "sessionToken": null,
  "payload": {}
}
```

성공 응답:

```json
{
  "requestId": "REQ-0001",
  "type": "LOGIN",
  "success": true,
  "payload": {}
}
```

실패 응답:

```json
{
  "requestId": "REQ-0001",
  "type": "LOGIN",
  "success": false,
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "아이디 또는 비밀번호가 올바르지 않습니다."
  }
}
```

구현 메모:

- Socket 메시지는 한 줄에 JSON 하나다.
- `ProtocolCodec`은 외부 JSON 라이브러리 없이 직접 파싱/직렬화한다.
- 범용 JSON 처리기가 아니라 이 프로젝트 envelope에 필요한 수준의 제한 구현이다.
- `UNKNOWN_TYPE`은 `ErrorCode` enum에 있지만, 알 수 없는 요청 타입은 codec 단계에서 `INVALID_REQUEST`로 떨어질 수 있다.
- `SCHEDULE_CONFLICT`는 실제 라우터 응답에서 문자열 code로 사용되지만 `ErrorCode` enum에는 없다. 오류 처리 코드를 정리할 때 확인해야 한다.

## 인증 흐름

관련 클래스:

- `AuthService`
- `AuthResult`
- `User`
- `RequestRouter`

회원가입:

```text
SIGN_UP
 -> AuthService.signUp()
 -> User 생성
 -> DataRepository.addUser()
 -> 중복 id면 DUPLICATE_USER_ID
```

로그인:

```text
LOGIN
 -> AuthService.login()
 -> User.checkPassword()
 -> sessionToken 생성
 -> AuthService 내부 Map에 sessionToken -> userId 저장
```

주의:

- 비밀번호는 단순 문자열이다.
- 세션 토큰은 프로젝트용 단순 문자열이다.
- 세션 만료, 암호화, 권한 모델 고도화는 구현 범위 밖이다.

## 개인 예매 흐름

클라이언트:

```text
SeatLayoutSession
 -> Personal 모드
 -> 좌석 선택
 -> CREATE_PERSONAL_RESERVATION
```

서버:

```text
RequestRouter.handleCreatePersonalReservation()
 -> 인증 확인
 -> scheduleId, seatIds 검증
 -> payment.status == PAID 확인
 -> Schedule 조회
 -> 좌석 snapshot 조회
 -> 선택 좌석 존재 여부 확인
 -> Theater resolve
 -> PricingStrategy 선택
 -> ViewScoreCalculator로 점수 계산
 -> 가격 계산
 -> Reservation 생성
 -> reservation.markPaid()
 -> reservation.confirm(totalPrice)
 -> DataRepository.reservePersonalIfAvailable()
```

핵심:

- 클라이언트가 보낸 가격은 신뢰하지 않는다.
- 서버가 현재 좌석 상태와 가격을 다시 계산한다.
- `reservePersonalIfAvailable()` 안에서 시간 충돌과 좌석 가능 여부를 검사하고, 좌석을 `RESERVED`로 바꾸며 예매를 저장한다.

## 그룹 예매 흐름

클라이언트:

```text
SeatLayoutSession
 -> Group 모드
 -> Members 입력
 -> 좌석 선택
 -> CREATE_GROUP_RESERVATION
```

서버 생성 단계:

```text
RequestRouter.handleCreateGroupReservation()
 -> 인증 확인
 -> leaderId가 현재 사용자와 같은지 확인
 -> Group 생성
 -> leader 자동 포함
 -> 그룹 구성원 수와 좌석 수 일치 확인
 -> 구성원 userId 존재 확인
 -> 좌석 존재 확인
 -> 총 가격 계산
 -> Reservation 생성
 -> reservation.hold(totalPrice)
 -> DataRepository.holdGroupIfAvailable()
```

결제 상태 갱신:

```text
UPDATE_GROUP_PAYMENT_STATUS
 -> 현재 로그인 사용자와 payload.userId 일치 확인
 -> PAID / FAILED / EXPIRED만 허용
 -> FAILED 또는 EXPIRED면 ReservationStatus.FAILED
 -> held 좌석 release
```

그룹 확정:

```text
CONFIRM_GROUP_RESERVATION
 -> 현재 사용자가 leader인지 확인
 -> ReservationStatus.HELD인지 확인
 -> Group.isAllPaid() 확인
 -> DataRepository.checkAndConfirmHeldSeats()
 -> Reservation.markPaid()
 -> Reservation.confirm(totalPrice)
```

핵심:

- `CREATE_GROUP_RESERVATION` 성공은 최종 확정이 아니다.
- 좌석은 `HELD`가 되고, 다른 예매에서 사용할 수 없다.
- 전원이 `PAID`가 되기 전까지 `CONFIRMED`가 되면 안 된다.
- 실패 또는 만료 상태가 들어오면 그룹 예매는 실패하고 held 좌석은 해제된다.

## 좌석과 가격 계산

좌석 ID 규칙:

```text
{theaterId}:{row}:{column}
```

예:

```text
TH-STD-1:3:4
```

좌표 기준:

- row와 column은 1-based다.
- 화면 표시도 1-based다.
- 일정별 좌석 상태는 `DataRepository.seatsByScheduleId`가 관리한다.
- `Theater`는 정적 좌석 배치 정보를 만들고, 저장소가 일정별 좌석 snapshot을 가진다.

가격 계산 구조:

```text
Seat + Theater
 -> ViewScoreCalculator.calculateScore()
 -> PricingStrategy.calculatePrice()
 -> final price
```

전략:

- `StandardPricingStrategy`: `basePrice + viewScoreSurcharge`
- `ImaxPricingStrategy`: `basePrice + IMAX_SURCHARGE + viewScoreSurcharge`
- `FourDxPricingStrategy`: `basePrice + FOUR_DX_SURCHARGE + viewScoreSurcharge`

시야 점수:

- 중앙에 가까울수록 높다.
- `ViewScoreCalculator.MIN_SCORE = 1`
- `ViewScoreCalculator.MAX_SCORE = 10`
- 유클리드 거리 기반 정규화 방식이다.

## 동시성 제어

서버는 클라이언트 연결마다 `ClientHandler` Thread를 만든다. 따라서 같은 좌석에 여러 요청이 동시에 들어올 수 있다.

중요한 synchronized 메서드:

- `DataRepository.addUser()`
- `DataRepository.getSeatSnapshot()`
- `DataRepository.reservePersonalIfAvailable()`
- `DataRepository.holdGroupIfAvailable()`
- `DataRepository.checkAndHoldSeats()`
- `DataRepository.checkAndConfirmHeldSeats()`
- `DataRepository.releaseSeats()`
- `DataRepository.updateGroupPaymentStatus()`

좌석 중복 예약 방지의 핵심:

```text
좌석 가능 여부 확인
 + 좌석 상태 변경
 + 예매 저장
= 하나의 synchronized critical section
```

앞으로 기능을 추가할 때 좌석 상태를 직접 바꾸지 말고 저장소의 동기화된 메서드 안에서 처리한다.

## 시간표 충돌 처리

`DataRepository.reservePersonalIfAvailable()`와 `holdGroupIfAvailable()`는 기존 활성 예매와 새 예매의 상영 시간이 겹치는지 확인한다.

활성 예매 상태:

- `CONFIRMED`
- `HELD`
- `PENDING`

겹치면 라우터는 `SCHEDULE_CONFLICT` 코드와 함께 실패 응답을 반환한다.

주의:

- `SCHEDULE_CONFLICT`는 문서와 실제 응답에는 있지만 `ErrorCode` enum에는 없다.
- 새 클라이언트 오류 처리를 만들 때 문자열 code도 처리 가능해야 한다.

## 데모 데이터

`DemoDataSeeder`가 서버 시작 시 메모리에 주입한다.

영화:

- `MOV-001`: Interstellar, 169분, 12세
- `MOV-002`: Inception, 148분, 12세
- `MOV-003`: The Dark Knight, 152분, 15세

상영관:

- `TH-STD-1`: Standard 1, 5 x 8, basePrice 10000
- `TH-IMAX-1`: IMAX 1, 6 x 10, basePrice 15000
- `TH-4DX-1`: 4DX 1, 4 x 6, basePrice 18000

일정:

- `SCH-001`: `MOV-001`, Standard, 2026-06-01 14:00
- `SCH-002`: `MOV-001`, IMAX, 2026-06-01 18:00
- `SCH-003`: `MOV-002`, Standard, 2026-06-02 13:30
- `SCH-004`: `MOV-003`, 4DX, 2026-06-02 20:00

## 컴파일과 실행

원본 프로젝트 루트:

```powershell
cd "C:\Users\mikey\Desktop\Team Project"
```

컴파일:

```powershell
javac -encoding UTF-8 -d build\classes (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName })
```

서버 실행:

```powershell
java -cp build\classes moviebooking.server.ServerMain
```

클라이언트 실행:

```powershell
java -cp build\classes moviebooking.client.ClientMain
```

클라이언트 smoke 실행:

```powershell
java -cp build\classes moviebooking.client.ClientMain --smoke
```

주의:

- 서버 기본 포트는 `5000`이다.
- GUI를 실행하기 전에 서버가 켜져 있어야 한다.
- 서버 재시작 시 회원가입한 사용자와 예매 내역은 사라진다.

## 검증 명령

아래 smoke test는 현재 확인 기준으로 모두 통과했다.

```powershell
java -cp build\classes moviebooking.testing.DomainCoreSmoke
java -cp build\classes moviebooking.testing.ErrorHandlingSmoke
java -cp build\classes moviebooking.testing.SeatConflictSmoke
java -cp build\classes moviebooking.testing.GroupReservationSmoke
java -cp build\classes moviebooking.testing.GroupConfirmationSmoke
java -cp build\classes moviebooking.testing.ReservationConflictAndAccountSmoke
java -cp build\classes moviebooking.testing.SessionManagerSmoke
```

확인한 결과:

- `DomainCoreSmoke`: 통과
- `ErrorHandlingSmoke`: 통과
- `SeatConflictSmoke`: 통과
- `GroupReservationSmoke`: 통과
- `GroupConfirmationSmoke`: 통과
- `ReservationConflictAndAccountSmoke`: 통과
- `SessionManagerSmoke`: 통과

각 smoke의 의미:

- `DomainCoreSmoke`: 도메인 계산, 시야 점수, 가격, 좌석 상태 전이
- `ErrorHandlingSmoke`: 공통 오류 응답
- `SeatConflictSmoke`: 동일 좌석 동시 요청 시 하나만 성공
- `GroupReservationSmoke`: 그룹 예매 홀딩, 결제 상태 갱신, 실패/만료 시 좌석 해제
- `GroupConfirmationSmoke`: 전원 결제 전 확정 실패, 대표자 권한, 확정 후 좌석 점유
- `ReservationConflictAndAccountSmoke`: 상영 시간 충돌 방지, MyAccount 예매 목록
- `SessionManagerSmoke`: 화면 스택 이동, 뒤로 가기, resume 흐름

## 발표/시연 계정

서버 재시작 후 매번 회원가입해야 한다.

| 역할 | userId | password |
| --- | --- | --- |
| 개인 예매 사용자 | `demo1` | `pass1234` |
| 충돌 테스트 사용자 | `demo2` | `pass1234` |
| 그룹 대표자 | `leader01` | `pass1234` |
| 그룹 구성원 1 | `user02` | `pass1234` |
| 그룹 구성원 2 | `user03` | `pass1234` |

비밀번호 입력 제한:

```text
English letters, numbers, !@#$%^&*()_+-=.?
```

## 앞으로 작업할 때 먼저 볼 파일

요청 종류별로 우선 확인할 파일:

- 서버 요청 추가: `RequestType.java`, `RequestRouter.java`, `PROTOCOL.md`
- 저장소/좌석 상태 변경: `DataRepository.java`
- 예매 상태 변경: `Reservation.java`, `ReservationStatus.java`, `PaymentStatus.java`
- 그룹 기능: `Group.java`, `RequestRouter.java`, `DataRepository.java`
- 가격 계산: `ViewScoreCalculator.java`, `PricingStrategy.java`, 각 전략 구현체
- 상영관 타입 추가: `Theater.java`, `TheaterType.java`, 하위 Theater, 전략 구현체
- GUI 화면 추가: `Session.java`, `SessionManager.java`, 기존 `*Session.java`
- Socket/JSON 문제: `ClientConnection.java`, `ClientHandler.java`, `ProtocolCodec.java`
- 초기 데이터 변경: `DemoDataSeeder.java`
- 테스트 기준: `src\moviebooking\testing`
- 발표 흐름: `docs\FINAL_DEMO_SCENARIO.md`, `docs\OOP_PRESENTATION_NOTES.md`

## 기능 추가 시 작업 순서

새 기능은 보통 이 순서로 진행하는 것이 좋다.

1. `docs`와 이 참고 문서에서 기존 원칙을 확인한다.
2. 요청/응답이 필요하면 `RequestType`과 프로토콜 payload를 먼저 정한다.
3. 서버 최종 판단은 `RequestRouter`에 둔다.
4. 상태 저장이나 좌석 변경은 `DataRepository`에 둔다.
5. GUI는 서버 요청을 보내고 응답을 표시하는 정도로 제한한다.
6. 위험한 동작이면 smoke test를 하나 추가한다.
7. 컴파일과 관련 smoke test를 돌린다.
8. 설계가 바뀌면 `DECISIONS.md` 또는 작업 문서에 남긴다.

## 절대 피해야 할 변경

- GUI에서 좌석 가능 여부를 최종 확정하지 않는다.
- GUI에서 가격을 최종 계산하지 않는다.
- `Seat` 상태를 여러 곳에서 직접 바꾸지 않는다.
- 좌석 확인과 상태 변경을 분리하지 않는다.
- 그룹 예매를 일부 결제만으로 `CONFIRMED`로 만들지 않는다.
- `Theater` 안에 가격 계산 정책을 직접 넣지 않는다.
- `PricingStrategy` 안에서 좌석 위치 분석을 직접 하지 않는다.
- 서버 재시작 후에도 데이터가 남는다고 가정하지 않는다.
- 실제 결제 시스템을 구현했다고 표현하지 않는다.

## 현재 구현의 한계

- 저장소는 메모리 기반이다.
- 실제 결제 연동은 없다.
- 친구 목록 고도화는 없다. 그룹 구성원은 userId 직접 입력이다.
- 그룹 예매 취소/만료 전용 API는 없다. `FAILED` 또는 `EXPIRED` 상태 갱신으로 처리한다.
- 세션 토큰은 단순 문자열이다.
- 비밀번호는 평문 문자열이다.
- JSON codec은 제한 구현이다.
- GUI는 발표용 흐름 중심이다.
- `build\classes`에 소스에 없는 예전 class가 남아 있을 수 있으므로, 실제 기준은 `src`와 `docs`다.

## 앞으로 가져올 만한 좋은 구조

- `ClientConnection`으로 Socket 통신 캡슐화
- `BaseRequest` / `BaseResponse` 공통 envelope
- `RequestRouter` 한 곳에서 요청 type 분기
- `Session` / `SessionManager` 기반 Swing 화면 전환
- `DataRepository`의 synchronized critical section
- `Theater` 상속 구조
- `PricingStrategy` 전략 패턴
- `ViewScoreCalculator` 책임 분리
- `Group` + `Reservation` + enum 상태 모델
- smoke test로 발표 핵심 기능을 검증하는 방식

## 작업 보고 템플릿

앞으로 이 프로젝트를 참고해 작업한 뒤에는 아래 형식이 잘 맞는다.

```text
변경 목적:

수정한 파일:

구현 내용:

검증 결과:

남은 리스크 또는 TODO:

문서 갱신 여부:
```

## 빠른 판단 기준

프로젝트를 진행하다 헷갈리면 이 기준을 따른다.

- 화면 문제인가? `client`와 `session`부터 본다.
- 요청/응답 문제인가? `protocol`과 `RequestRouter`부터 본다.
- 예매 규칙 문제인가? `RequestRouter`와 `DataRepository`를 본다.
- 좌석 충돌 문제인가? `DataRepository` synchronized 메서드를 본다.
- 가격 문제인가? `ViewScoreCalculator`와 `PricingStrategy`를 본다.
- 그룹 문제인가? `Group`, `Reservation`, `PaymentStatus`, `ReservationStatus`를 본다.
- 시연 문제인가? `FINAL_DEMO_SCENARIO.md`와 smoke test를 본다.

