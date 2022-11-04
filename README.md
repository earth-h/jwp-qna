# JPA

## 용어사전
```text
프로젝트에서 사용되는 단어들을 정리하였습니다.
하기 단어들은 이후에 변경될 수 있습니다:)
```
* Question: 질문
* Answer: 답변
* Writer: 작성자
* User: 유저
* DeleteHistory: 삭제이력
* isDeleted: 삭제여부
* userId: 유저Id

## 1단계 - 엔티티 매핑
### 요구 사항
1. 주어진 DDL을 보고 entity 클래스와 리포지토리 클래스 작성
2. @DataJpaTest를 활용하여 테스트 진행

### 테이블 정의
* 아래 DDL(Data Definition Language)을 보고 유추하여 작성
```sql
create table answer
(
    id          bigint generated by default as identity,
    contents    clob,
    created_at  timestamp not null,
    deleted     boolean   not null,
    question_id bigint,
    updated_at  timestamp,
    writer_id   bigint,
    primary key (id)
)
```
```sql
create table delete_history
(
    id            bigint generated by default as identity,
    content_id    bigint,
    content_type  varchar(255),
    create_date   timestamp,
    deleted_by_id bigint,
    primary key (id)
)
```
```sql
create table question
(
    id         bigint generated by default as identity,
    contents   clob,
    created_at timestamp    not null,
    deleted    boolean      not null,
    title      varchar(100) not null,
    updated_at timestamp,
    writer_id  bigint,
    primary key (id)
)
```
```sql
create table user
(
    id         bigint generated by default as identity,
    created_at timestamp   not null,
    email      varchar(50),
    name       varchar(20) not null,
    password   varchar(20) not null,
    updated_at timestamp,
    user_id    varchar(20) not null,
    primary key (id)
)

alter table user
    add constraint UK_a3imlf41l37utmxiquukk8ajc unique (user_id)
```
#### answer
| 컬럼명         | PK 여부 | 타입        | 비고                               |
|-------------|-------|-----------|----------------------------------|
| id          | O     | bigint    | generated by default as identity |
| contents    |   | clob      |                                  |
| created_at  |  | timestamp | not null                         |
| deleted     |  | boolean   | not null                         |
| question_id |  | bigint    |                                  |
| updated_at  |  | timestamp |                                  |
| writer_id   |  | bigint    |                                  |

#### deleted_history
| 컬럼명          | PK 여부 | 타입           | 비고                               |
|--------------|-------|--------------|----------------------------------|
| id           | O     | bigint       | generated by default as identity |
| content_id   |   | bigint       |                                  |
| content_type |  | varchar(255) |                                  |
| create_date  |  | timestamp    |                          |
| deleted_by_id |  | bigint       |                                  |

#### question
| 컬럼명        | PK 여부 | 타입           | 비고                               |
|------------|-------|--------------|----------------------------------|
| id         | O     | bigint       | generated by default as identity |
| contents   |   | clob         |                                  |
| created_at |  | timestamp    | not null                         |
| deleted    |  | boolean      | not null                         |
| title         |  | varchar(100) | not null                         |
| updated_at |  | timestamp    |                                  |
| writer_id   |  | bigint    |                                  |

#### user
| 컬럼명        | PK 여부 | 타입          | 비고                                       |
|------------|-------|-------------|------------------------------------------|
| id         | O     | bigint      | generated by default as identity |
| created_at |  | timestamp   | not null                                 |
| email      |  | varchar(50) |                                          |
| name       |  | varchar(20) | not null                                 |
| password   |  | varchar(20) | not null                                 |
| updated_at |  | timestamp   |                                          |
| user_id    |  | varchar(20) | not null                                 |
* user_id는 unique해야 함

### application.yml 추가 사항
```yml
spring:
  datasource:
    url: jdbc:h2:~/test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    driver-class-name: org.h2.Driver

  h2:
    console:
      enabled: true

  jpa:
    properties:
      hibernate:
        format_sql: true
    hibernate:
      ddl-auto: create-drop

logging:
  level:
    org.hibernate.SQL: debug
```
* H2 콘솔에 접근할 수도 있다고 생각해 콘솔 설정 진행
* application 실행 시마다 기존 테이블 삭제되고 생성되도록 ddl-auto를 create-drop으로 설정

### 테스트 목록
1. [x] Answer 등록/조회/삭제 테스트
2. [x] Answer 객체 생성 시 예외처리 테스트
3. [x] Question 등록/조회/삭제 테스트
4. [x] Question 객체 생성 시 예외처리 테스트
5. [x] User 등록/조회/삭제 테스트
7. [x] DeleteHistory 등록/조회/삭제 테스트

## 2단계 - 연관 관계 매핑
### 요구 사항
1. QnA 서비스를 만들면서 JPA로 실 도메인 모델 구성 및 객체와 테이블간 매핑
  * 객체에서는 참조를 사용하고, 테이블에서는 외래 키를 사용
```text
Answer : Question = N : 1 (연관관계 주인: Answer)
Answer : User = N : 1 (연관관계 주인: Answer)
DeleteHistory : User = N : 1 (연관관계 주인: DeleteHistory)
Question : User = N : 1 (연관관계 주인: Question)
```
* 답변은 하나의 질문에만 속할 수 있다. -> 답변과 질문은 다대일 관계이다.
* 답변은 한명의 작성자만 가질 수 있다. -> 답변과 유저는 다대일 관계이다.
  * 유저는 여러 답변을 할 수 있다.
* 질문은 한명의 작성자만 가질 수 있다. -> 질문과 유저는 다대일 관계이다.
  * 유저는 여러 질문을 할 수 있다.
* 삭제이력은 한명에 의해서만 삭제된다. -> 삭제이력과 유저는 다대일 관계이다.
  * 유저가 질문/답변을 삭제했을 때 삭제이력이 남는다.

* 연관관계 주인 - 외래 키 관리자
  * 양방향 정의 시, 연관관계 편의 메소드를 이용해 순수한 객체까지 고려한 양방향 연관관계 설정
  * 반드시 필요한 경우에만 양방향 관계를 사용해야 함
    * toString() 시 무한루프 위험 있음(양방향 사용 시 잘 막아야함)
  ```java
  // 예시
  public void setTeam(Team team) {
    if(this.team != null) {
        this.team.getMembers().remove(this);
    }
    this.team = team;
    team.getMembers().add(this);
  }
  ```
* [x] equals() 사용 시 overriding 필요한지 확인

* 연속성 전이(cascade) 사용
  * 테스트를 진행할 때, 연관된 엔티티를 save하지 않아도 함께 영속되도록 PERSIST로 설정

* 즉시 로딩/지연 로딩
  * QnA 서비스에서는 Question을 조회할 때, 작성자를 함께 조회하는 것이 유리하다고 판단(EAGER)
    * 해당 질문을 한 사람인지 보통 체크를 진행할 것으로 보임(deleteQuestion)

### 테이블 정의
* 아래 DDL(Data Definition Language)을 보고 유추하여 작성
```sql
alter table answer
    add constraint fk_answer_to_question
        foreign key (question_id)
            references question

alter table answer
    add constraint fk_answer_writer
        foreign key (writer_id)
            references user

alter table delete_history
    add constraint fk_delete_history_to_user
        foreign key (deleted_by_id)
            references user

alter table question
    add constraint fk_question_writer
        foreign key (writer_id)
            references user
```

## 3단계 - 질문 삭제하기 리팩터링
### 기능 요구 사항
* 질문 삭제 시, 레포지토리에서 삭제하는 것이 아닌 deleted 필드를 true로 변경해야 함
* 질문 삭제 가능한 조건
  1. (로그인 사용자 == 질문한 사람) && 답변 없음 
  2. (로그인 사용자 == 질문한 사람) && (로그인 사용자 == 답변한 사람)
* 질문 삭제 시, 답변 역시 삭제되어야 함
* 질문/답변 삭제 시, 삭제 이력 정보를 DeleteHistory에 남겨야 함

### 프로그래밍 요구 사항
* QnaService의 deleteQuestion() 메소드를 단위 테스트 가능한 부분과 어려운 부분을 분리하여 테스트 가능한 부분에 대해 TDD 구현
* 리팩토링 이후에도, QnaServiceTest를 통과해야 함

### 기능 목록
* [x] Question - 질문 삭제 권한 확인 -> 권한 없을 경우 CannotDeleteException 발생("질문을 삭제할 권한이 없습니다.")
* [x] Question - 질문 삭제여부 변경
* [ ] Answer - 답변 삭제 권한 확인 -> 권한 없을 경우 CannotDeleteException 발생("다른 사람이 쓴 답변이 있어 삭제할 수 없습니다.")
* [ ] Answer - 답변 삭제여부 변경
* [x] User - 유저 동일여부 확인
  * equals() 오버라이딩
* [ ] DeleteHistory - 삭제이력 추가

