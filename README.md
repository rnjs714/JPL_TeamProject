# Movie Booking Jackson Skeleton

Jackson 기반 영화 예매 프로그램 스켈레톤입니다.

내부 구현은 의도적으로 생략되어 있으며, 각 메서드의 `TODO:` 주석에 구현해야 할 내용을 적어두었습니다.

## 설계 기준

- 그룹 예매 기능 제외
- 가격 계산 기능 제외
- 상영관은 개별 클래스가 아니라 `Theater.type` 필드로 구분
- JSON 파일 처리는 Jackson `ObjectMapper` 사용
- `LocalDateTime` 처리를 위해 `jackson-datatype-jsr310` 사용
