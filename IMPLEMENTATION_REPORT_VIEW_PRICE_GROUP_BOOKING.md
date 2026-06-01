# 시야 점수 가격 측정 및 그룹 예매 기능 구현 보고서

## 1. 무엇을 만들었는지

이번 작업에서는 영화 예매 프로그램에 두 가지 기능을 추가했다.

첫 번째 기능은 `시야 점수에 따른 가격 측정`이다. 좌석마다 화면을 보기 좋은 정도를 점수로 계산하고, 그 점수가 높을수록 가격이 더 높아지도록 했다. 예를 들어 중앙에 가까운 좌석은 시야 점수가 높고, 가장자리 좌석은 시야 점수가 낮다.

두 번째 기능은 `그룹 예매`이다. 사용자가 인원 수를 입력하면 프로그램이 그 인원 수에 맞는 좌석을 자동으로 추천한다. 가능하면 같은 줄에 붙어 있는 연속 좌석을 먼저 추천하고, 연속 좌석이 부족하면 떨어져 있더라도 예약 가능한 좌석을 대안으로 알려준다.

기존 프로그램의 구조를 크게 바꾸지 않고, 현재 코드 흐름에 맞게 기능을 추가했다.

```text
GUI 화면
 -> BookingController
 -> ApiClient
 -> ClientHandler
 -> DataRepository
 -> domain 계산 클래스
```

## 2. 왜 이렇게 만들었는지

현재 폴더에는 두 개의 기준 문서가 있다.

- `REFERENCE_TEAM_PROJECT.md`
- `CODING_STYLE_PROFILE.md`

`REFERENCE_TEAM_PROJECT.md`의 핵심 방향은 다음과 같다.

- GUI는 입력과 화면 표시만 담당한다.
- 실제 예매 가능 여부, 가격 계산, 좌석 확정 판단은 서버가 담당한다.
- 좌석 중복 예약을 막기 위해 좌석 확인과 예약 처리는 저장소에서 동기화해서 처리한다.
- 가격 계산은 따로 분리해서 관리한다.

`CODING_STYLE_PROFILE.md`의 핵심 코딩 스타일은 다음과 같다.

- 너무 복잡한 구조를 만들지 않는다.
- `for`문, `if`문, 명확한 변수 이름을 사용한다.
- 알고리즘의 흐름이 눈에 보이도록 작성한다.
- 필요한 helper method는 만들되, 과한 추상화는 피한다.

그래서 이번 구현도 새로운 큰 프레임워크나 복잡한 설계를 추가하지 않았다. 기존에 있던 `DataRepository`, `ClientHandler`, `BookingController`, `SeatSelectionPanel` 흐름에 필요한 메서드만 추가했다.

## 3. 수정하거나 추가한 파일 목록

### 수정한 파일

```text
src/domain/DynamicPriceCalculator.java
src/repository/DataRepository.java
src/server/ClientHandler.java
src/controller/BookingController.java
src/gui/SeatSelectionPanel.java
```

### 작업 전부터 이미 변경되어 있던 관련 파일

```text
src/domain/Reservation.java
src/gui/ReservationPanel.java
```

이 두 파일은 이번 작업에서 직접 새로 만든 핵심 기능은 아니지만, 현재 프로그램에서 예매 가격을 저장하고 화면에 표시하는 흐름과 연결되어 있다.

### 추가된 보고서 파일

```text
IMPLEMENTATION_REPORT_VIEW_PRICE_GROUP_BOOKING.md
```

## 4. 각 파일에서 변경한 내용

## 4.1 `DynamicPriceCalculator.java`

역할:

```text
좌석 가격을 계산하는 클래스
```

추가/보완한 내용:

- 상영관 정보가 없을 때 오류를 내도록 했다.
- 상영 시간 정보가 없을 때 오류를 내도록 했다.
- 예약 좌석 수가 잘못 들어오면 기본값처럼 0으로 처리하도록 했다.
- 좌석 시야 점수를 계산하는 `calculateViewScore()` 메서드를 외부에서도 사용할 수 있게 했다.
- 잘못된 좌석 코드가 들어오면 명확한 오류를 내도록 했다.

가격 계산 방식:

```java
int viewPremium = (viewScore - ViewScoreCalculator.MIN_SCORE) * VIEW_SCORE_UNIT_PRICE;
return basePrice + viewPremium + getDemandSurcharge(theater, reservedSeatCount);
```

쉽게 말하면 가격은 아래 세 가지를 더해서 만든다.

```text
최종 가격 = 상영관 기본 가격 + 시야 점수 추가 금액 + 수요 추가 금액
```

상영관 기본 가격 예시:

- 일반관: 12,000원
- IMAX: 18,000원
- 4DX: 20,000원

시야 점수 추가 금액:

```text
(시야 점수 - 1) * 500원
```

예를 들어 시야 점수가 10점이면:

```text
(10 - 1) * 500 = 4,500원 추가
```

## 4.2 `DataRepository.java`

역할:

```text
JSON 파일 데이터를 읽고, 회원/영화/상영시간/좌석/예매 데이터를 관리하는 저장소
```

추가/보완한 내용:

- 예약할 때 서버에서 총 가격을 다시 계산하도록 했다.
- 선택 좌석의 총 가격을 계산하는 `calculatePrice()`를 추가했다.
- 좌석 하나의 시야 점수와 가격을 조회하는 `calculateSeatPriceInfo()`를 추가했다.
- 그룹 인원 수에 맞는 좌석을 추천하는 `findRecommendedGroupSeats()`를 추가했다.
- 같은 줄에 붙어 있는 연속 좌석을 찾는 `findContinuousSeats()`를 추가했다.
- 연속 좌석이 없을 때 가능한 좌석 대안을 찾는 `findAvailableSeats()`를 추가했다.
- 좌석 코드 정규화 메서드인 `normalizeSeatCode()`와 `normalizeSeatCodes()`를 추가했다.
- 중복 좌석 선택을 검사하도록 했다.

중요한 점:

좌석 추천과 예약 관련 로직은 `synchronized` 메서드 안에서 처리된다. `synchronized`는 여러 사용자가 동시에 요청할 때 같은 데이터를 동시에 바꾸지 못하게 막는 Java 문법이다.

예를 들어 두 명이 동시에 같은 좌석을 예약하려고 할 때, 한 명의 요청이 먼저 처리되는 동안 다른 요청은 기다리게 된다. 그래서 같은 좌석이 두 번 예약되는 문제를 줄일 수 있다.

그룹 좌석 추천 흐름:

```text
1. 상영 시간 ID가 맞는지 확인한다.
2. 인원 수가 1명 이상인지 확인한다.
3. 상영관 정보를 찾는다.
4. 같은 행에서 연속으로 비어 있는 좌석을 먼저 찾는다.
5. 연속 좌석이 있으면 그 좌석들을 반환한다.
6. 연속 좌석이 없으면 떨어져 있는 좌석이라도 가능한 만큼 찾는다.
7. 전체 가능한 좌석도 부족하면 오류를 반환한다.
```

연속 좌석을 찾는 핵심 방식:

```java
for(int row=0; row<theater.getRows(); row++) {
    result.clear();
    for(int col=1; col<=theater.getColumns(); col++) {
        String seatCode = createSeatCode(row, col);
        if(isAvailableSeat(theater, showtime, seatCode)) {
            result.add(seatCode);
            if(result.size() == peopleCount) {
                return new ArrayList<>(result);
            }
        } else {
            result.clear();
        }
    }
}
```

쉽게 말하면 한 줄씩 좌석을 보면서, 빈 좌석이 연속으로 원하는 인원 수만큼 나오면 바로 추천한다.

## 4.3 `ClientHandler.java`

역할:

```text
클라이언트가 보낸 요청을 서버에서 받아 command별로 처리하는 클래스
```

추가한 command:

```text
CALCULATE_PRICE
GET_SEAT_PRICE
FIND_GROUP_SEATS
```

각 command의 의미:

- `CALCULATE_PRICE`: 선택한 여러 좌석의 총 가격 계산
- `GET_SEAT_PRICE`: 좌석 하나의 시야 점수와 가격 조회
- `FIND_GROUP_SEATS`: 인원 수에 맞는 그룹 좌석 추천

추가한 helper method:

```java
private int readInt(Object value)
```

이 메서드는 클라이언트가 보낸 인원 수 값을 숫자로 바꾼다. 값이 없거나 숫자가 아니면 알기 쉬운 오류 메시지를 반환하도록 했다.

## 4.4 `BookingController.java`

역할:

```text
GUI와 서버 API 요청 사이를 연결하는 클래스
```

추가한 메서드:

```java
public int calculateSelectedSeatPrice()
public Map<String, Object> calculateSeatPriceInfo(String seatCode)
public List<String> recommendGroupSeats(int peopleCount)
```

각 메서드의 역할:

- `calculateSelectedSeatPrice()`: 현재 선택된 좌석들의 총 가격을 서버에 물어본다.
- `calculateSeatPriceInfo(String seatCode)`: 좌석 하나의 시야 점수와 가격을 서버에 물어본다.
- `recommendGroupSeats(int peopleCount)`: 그룹 인원 수에 맞는 좌석 추천을 서버에 요청한다.

왜 컨트롤러에 추가했는가:

GUI가 직접 서버와 복잡하게 통신하지 않도록 하기 위해서다. 화면은 버튼 클릭만 처리하고, 서버 요청은 컨트롤러가 담당한다.

## 4.5 `SeatSelectionPanel.java`

역할:

```text
사용자가 좌석을 보고 선택하는 GUI 화면
```

추가/보완한 내용:

- 좌석 버튼에 시야 점수와 가격을 표시했다.
- 좌석을 선택하면 하단에 선택한 좌석들의 총 가격이 표시되도록 했다.
- 그룹 인원 수 입력칸을 추가했다.
- `Auto Group Seats` 버튼을 추가했다.
- 버튼을 누르면 서버가 추천한 좌석을 자동 선택하도록 했다.
- 연속 좌석이면 연속 좌석 추천 메시지를 보여준다.
- 연속 좌석이 부족해서 대체 좌석을 받은 경우 대체 좌석이라는 메시지를 보여준다.

좌석 버튼 표시 예시:

```text
B5
View 9
16,000 KRW
```

하단 표시 예시:

```text
Selected Price: 32,000 KRW
People [ 2 ] [Auto Group Seats] [Reserve]
```

## 5. 어떻게 동작하는지

## 5.1 시야 점수 가격 측정 흐름

사용자가 좌석 화면에 들어가면 좌석 버튼들이 만들어진다.

각 좌석 버튼은 서버에 다음 정보를 요청한다.

```text
GET_SEAT_PRICE
```

서버는 `DataRepository.calculateSeatPriceInfo()`를 실행한다.

그 안에서:

1. 상영 시간이 존재하는지 확인한다.
2. 상영관 정보를 찾는다.
3. 좌석 코드가 올바른지 확인한다.
4. `ViewScoreCalculator`로 시야 점수를 계산한다.
5. `DynamicPriceCalculator`로 가격을 계산한다.
6. 좌석 코드, 시야 점수, 가격을 GUI에 반환한다.

GUI는 반환받은 값을 좌석 버튼에 표시한다.

## 5.2 좌석 선택 후 총 가격 계산 흐름

사용자가 좌석을 누르면:

```text
SeatSelectionPanel
 -> BookingSession.toggleSeat()
 -> updateSelectedPrice()
 -> BookingController.calculateSelectedSeatPrice()
 -> CALCULATE_PRICE 요청
 -> DataRepository.calculatePrice()
```

서버는 선택한 좌석들이 실제로 유효한지, 이미 예약된 좌석은 아닌지 확인한 뒤 총 가격을 계산한다.

## 5.3 그룹 예매 좌석 추천 흐름

사용자가 인원 수를 입력하고 `Auto Group Seats` 버튼을 누르면:

```text
SeatSelectionPanel
 -> BookingController.recommendGroupSeats()
 -> FIND_GROUP_SEATS 요청
 -> DataRepository.findRecommendedGroupSeats()
```

서버는 먼저 같은 줄에서 붙어 있는 좌석을 찾는다.

예를 들어 3명을 입력했고 C열에 `C3`, `C4`, `C5`가 비어 있으면:

```text
[C3, C4, C5]
```

이렇게 반환한다.

만약 연속 좌석이 없다면 가능한 좌석을 대안으로 반환한다.

예:

```text
[A3, B6, D2]
```

이때 GUI는 “연속 좌석이 부족해서 대체 좌석을 추천했다”는 메시지를 보여준다.

## 5.4 최종 예약 흐름

추천 좌석이 자동 선택된 뒤 사용자가 `Reserve` 버튼을 누르면 기존 예약 흐름을 그대로 사용한다.

```text
RESERVE
 -> DataRepository.reserve()
 -> 좌석 유효성 검사
 -> 중복 예약 검사
 -> 가격 재계산
 -> reservedSeats에 좌석 추가
 -> Reservation 생성
 -> JSON 파일 저장
```

중요한 점은 최종 예약 시에도 서버가 다시 좌석을 검사한다는 것이다. 그래서 사용자가 화면에서 오래 머무르는 사이 다른 사람이 좌석을 먼저 예약해도 중복 예약이 되지 않는다.

## 6. 예외 상황과 처리 방식

## 6.1 잘못된 좌석 코드

예:

```text
Z99
AA10
빈 문자열
null
```

처리:

- `normalizeSeatCode()`와 `validateSeat()`에서 검사한다.
- 잘못된 값이면 오류 메시지를 반환한다.

## 6.2 이미 예약된 좌석

처리:

- `validateReservationInput()`에서 `showtime.getReservedSeats()`를 확인한다.
- 이미 예약된 좌석이면 예약을 막는다.

오류 예:

```text
이미 예약된 좌석: B5
```

## 6.3 같은 좌석을 두 번 선택한 경우

처리:

- `LinkedHashSet`으로 중복 선택 여부를 검사한다.

오류 예:

```text
Duplicated seat: C4
```

## 6.4 인원 수가 잘못된 경우

예:

```text
0
-1
abc
빈 값
```

처리:

- GUI에서 숫자 형식이 아니면 메시지를 보여준다.
- 서버에서도 숫자 변환에 실패하면 오류를 반환한다.

오류 예:

```text
인원 수는 숫자로 입력해주세요.
```

## 6.5 인원 수만큼 좌석이 부족한 경우

처리:

- 연속 좌석을 먼저 찾는다.
- 연속 좌석이 없으면 전체 가능한 좌석을 찾는다.
- 전체 가능한 좌석도 부족하면 오류를 반환한다.

오류 예:

```text
선택한 인원 수만큼 예약 가능한 좌석이 없습니다.
```

## 6.6 상영 시간이나 상영관 정보가 없는 경우

처리:

- 상영 시간이 없으면 `Wrong Showtime ID.`
- 상영관이 없으면 `Wrong Theater ID.`

## 7. 실행 방법

현재 프로젝트는 Jackson 라이브러리를 사용한다. 일반적으로는 Maven으로 빌드한다.

```powershell
mvn test
```

또는 실행:

```powershell
java -cp target\classes server.MovieBookingServer
java -cp target\classes gui.MovieBookingGui
```

단, 현재 작업 환경에서는 `mvn` 명령과 Jackson jar가 없어 전체 Maven 빌드는 실행하지 못했다. 대신 가격 계산과 관련된 도메인 파일은 `javac -encoding UTF-8`로 부분 컴파일 확인을 했다.

확인한 컴파일 범위:

```text
TheaterType.java
Theater.java
Showtime.java
ViewScoreCalculator.java
DynamicPriceCalculator.java
```

## 8. 기능 확인 방법

## 8.1 시야 점수 가격 확인

1. 서버를 실행한다.
2. GUI를 실행한다.
3. 로그인한다.
4. 영화를 선택한다.
5. 상영 시간을 선택한다.
6. 좌석 화면에 들어간다.
7. 좌석 버튼에 `View 점수`와 `가격`이 표시되는지 확인한다.
8. 중앙 좌석이 가장자리 좌석보다 더 높은 가격인지 확인한다.

확인 포인트:

```text
중앙 좌석: View 점수 높음, 가격 높음
가장자리 좌석: View 점수 낮음, 가격 낮음
```

## 8.2 선택 좌석 총 가격 확인

1. 좌석을 하나 선택한다.
2. 하단 `Selected Price`가 바뀌는지 확인한다.
3. 좌석을 추가로 선택한다.
4. 가격이 합산되는지 확인한다.
5. 선택을 해제하면 가격이 다시 줄어드는지 확인한다.

## 8.3 그룹 예매 확인

1. 좌석 화면으로 이동한다.
2. `People` 입력칸에 인원 수를 입력한다.
3. `Auto Group Seats` 버튼을 누른다.
4. 추천 좌석들이 자동으로 선택되는지 확인한다.
5. 메시지에 연속 좌석인지 대체 좌석인지 표시되는지 확인한다.
6. `Reserve` 버튼을 눌러 예약한다.
7. 예매 내역 화면에서 여러 좌석이 하나의 예약으로 들어갔는지 확인한다.

## 9. 사용자가 보는 변화

기존에는 좌석 버튼이 단순히 좌석 번호만 보여줬다.

```text
A1
```

이제는 좌석 정보가 더 자세히 보인다.

```text
A1
View 1
12,000 KRW
```

또 기존에는 여러 명이 예매하려면 사용자가 좌석을 하나씩 직접 골라야 했다. 이제는 인원 수를 입력하고 자동 추천 버튼을 누르면 프로그램이 가능한 좌석을 골라준다.

## 10. 개발자가 알아야 할 핵심 로직

## 10.1 가격 계산은 `DynamicPriceCalculator`

가격 계산 수식을 바꾸려면 이 파일을 보면 된다.

```text
src/domain/DynamicPriceCalculator.java
```

바꿀 수 있는 값:

```java
private static final int STANDARD_BASE_PRICE = 12000;
private static final int IMAX_BASE_PRICE = 18000;
private static final int FOUR_DX_BASE_PRICE = 20000;
private static final int VIEW_SCORE_UNIT_PRICE = 500;
```

## 10.2 시야 점수 계산은 `ViewScoreCalculator`

좌석 위치에 따라 점수를 계산한다.

```text
src/domain/ViewScoreCalculator.java
```

점수 범위:

```java
public static final int MIN_SCORE = 1;
public static final int MAX_SCORE = 10;
```

## 10.3 그룹 좌석 추천은 `DataRepository`

그룹 좌석 추천 로직은 여기에 있다.

```text
src/repository/DataRepository.java
```

중요 메서드:

```java
findRecommendedGroupSeats()
findContinuousSeats()
findAvailableSeats()
```

## 10.4 GUI 표시와 버튼은 `SeatSelectionPanel`

좌석 화면의 표시 방식은 여기에 있다.

```text
src/gui/SeatSelectionPanel.java
```

중요 메서드:

```java
setSeatPriceText()
updateSelectedPrice()
autoSelectGroupSeats()
```

## 11. 이번 구현의 한계

이번 기능은 현재 프로젝트 구조 안에서 단순하고 명확하게 구현했다. 그래서 다음과 같은 한계가 있다.

- 그룹 예매는 하나의 예약에 여러 좌석을 넣는 방식이다.
- 그룹 구성원별 이름이나 계정은 따로 저장하지 않는다.
- 결제 상태를 사람별로 관리하지 않는다.
- 연속 좌석 추천은 왼쪽 위 좌석부터 순서대로 찾는다.
- 더 좋은 중앙 좌석 우선 추천까지는 구현하지 않았다.
- Maven/Jackson 의존성 문제 때문에 전체 빌드를 이 환경에서 끝까지 확인하지 못했다.

## 12. 앞으로 개선하면 좋은 점

나중에 시간이 있다면 아래 기능을 추가할 수 있다.

- 그룹 구성원 ID를 입력받아 저장하기
- 그룹 구성원별 결제 상태 관리하기
- 중앙에 가까운 연속 좌석을 우선 추천하기
- 연속 좌석이 없을 때 여러 대안을 목록으로 보여주기
- 가격 계산 결과를 더 보기 좋게 표 형태로 표시하기
- 전체 Maven 빌드 환경을 정리해서 테스트 자동화하기

## 13. 전체 요약

이번 작업은 영화 예매 프로그램에 `좌석별 가격 차등`과 `그룹 예매 자동 좌석 추천`을 추가한 것이다.

가격 차등은 좌석의 시야 점수에 따라 결정된다. 중앙에 가까운 좌석은 높은 점수를 받고 가격이 올라간다. 가장자리 좌석은 낮은 점수를 받고 가격이 낮아진다.

그룹 예매는 사용자가 인원 수를 입력하면 서버가 가능한 좌석을 추천한다. 먼저 같은 줄에 붙어 있는 좌석을 찾고, 없으면 예약 가능한 대체 좌석을 알려준다.

가장 중요한 점은 GUI가 직접 판단하지 않는다는 것이다. GUI는 사용자의 입력을 받고 결과를 보여준다. 실제 가격 계산, 좌석 검증, 중복 예약 방지는 서버의 `DataRepository`와 도메인 계산 클래스에서 처리한다.

