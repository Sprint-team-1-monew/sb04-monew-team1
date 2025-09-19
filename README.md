# Monew
- 소개: 여러 뉴스 API를 통합하여 사용자에게 맞춤형 뉴스를 제공하고, 의견을 나눌 수 있는 소셜 기능을 갖춘 서비스
- 개발 기간: 2025.9.2 ~2025.9.23


# 팀원

<br>


<table>
  <tr>
    <td align="center" width="150">
      <a href="https://github.com/cheis11"><b>김찬호</b></a><br/>
      <img src="https://github.com/cheis11.png" width="100" style="border-radius: 50%"/><br/>
      <sub><b>BE</b></sub>
    </td>
    <td align="center" width="150">
      <a href="https://github.com/Eunhye0k"><b>강은혁</b></a><br/>
      <img src="https://github.com/Eunhye0k.png" width="100" style="border-radius: 50%"/><br/>
      <sub><b>BE</b></sub>
    </td>
    <td align="center" width="150">
      <a href="https://github.com/kdfasdf"><b>김대호</b></a><br/>
      <img src="https://github.com/kdfasdf.png" width="100" style="border-radius: 50%"/><br/>
      <sub><b>BE</b></sub>
    </td>
    <td align="center" width="150">
      <a href="https://github.com/chaoskyj1120"><b>권용진</b></a><br/>
      <img src="https://github.com/chaoskyj1120.png" width="100" style="border-radius: 50%"/><br/>
      <sub><b>BE</b></sub>
    </td>
    <td align="center" width="150">
      <a href="https://github.com/B1uffer"><b>신동진</b></a><br/>
      <img src="https://github.com/B1uffer.png" width="100" style="border-radius: 50%"/><br/>
      <sub><b>BE</b></sub>
    </td>
    <td align="center" width="150">
      <a href="https://github.com/Oh-Myeongjae"><b>오명재</b></a><br/>
      <img src="https://github.com/Oh-Myeongjae.png" width="100" style="border-radius: 50%"/><br/>
      <sub><b>BE</b></sub>
    </td>
  </tr>
</table>


## 기술 스택
### Backend
- Java 17
- Spring Boot 3.4.9
- Spring Data JPA 3.5.2
- Spring Batch 3.4.9
- PostgreSQL  42.7.7
- Junit 5
- mockito 5.1.2
- MapStruct 1.5.5
- logback 1.5.18
- querydsl 5.1.0
- spring web flux6.2.1.0(web client만 사용)
- H2 2.3.232

### Infra
- AWS
  - EC2
  - Elastic Container Registry
  - Elastic Container Service
  - S3
  - RDS
- Docker
- GitHub Actions
  
### Collaboration
- Github
- Notion

### etc
- JaCoCo

## 아키텍처
<img width="1340" height="732" alt="image" src="https://github.com/user-attachments/assets/6045b9c6-7900-45ae-b2e2-9bf5902cf4ea" />


## 팀원 별 구현 기능
### 김찬호(팀장)
- 알림 관리
  - 알림 등록
    - 구독 중인 관심사와 관련된 기사가 새로 등록된 경우 알림이 생성
    - 내가 작성한 댓글에 좋아요가 눌리면 알림이 생성
  - 알림 수정
    - 알림을 확인하면 확인 여부를 수정
    - 모든 알림을 한번에 확인할 수 있음
  - 알림 삭제
    - 확인한 알림 중 1주일이 경과된 알림은 자동으로 삭제
    - 삭제는 매일 배치로 수행
  - 알림 목록 조회
    - 확인하지 않은 알림만 조회
    - 시간 순으로 정렬 및 커서 페이지네이션을 구현

### 강은혁
- 댓글 관리
  - 댓글 등록

### 김대호
- 사용자 관리
  - 사용자 등록
    - 비밀번호 bcrypt 암호화   
  - 사용자 수정
  - 사용자 삭제
    - 논리 삭제 1일 후 물리삭제(배치 처리)
  - 로그인
  - 로깅 기능 관리
  - 활동 내역 조회
  - 로그 관리
    - MDC 로깅을 통한 멀티스레드 로그 추적
    - logback 설정을 통한 로그 아카이빙, 커스텀 색상화로 가시성 향상
    - AOP 통한 서비스 계층 로그 call return 로깅     

### 권용진
- 뉴스 기사 관리
  - 뉴스 기사 등록

### 신동진
- CI/CD 파이프라인 구축
- 기사 백업 및 복구

### 오명재
- CI/CD 파이프라인 구축
- 관심사 관리


## ERD
<img width="1150" height="740" alt="image" src="https://github.com/user-attachments/assets/b3e8e36f-6fa7-4fde-b0c2-e91afa60d148" />
 


## 프로젝트 구조

```

  src
    ├─main
    │  ├─java
    │  │  └─com
    │  │      ├─codeit
    │  │      │  └─monew
    │  │      │      ├─article
    │  │      │      │  ├─backUp
    │  │      │      │  │  ├─aws
    │  │      │      │  │  ├─dto
    │  │      │      │  │  ├─repository
    │  │      │      │  │  ├─scheduler
    │  │      │      │  │  └─service
    │  │      │      │  ├─batch
    │  │      │      │  ├─controller
    │  │      │      │  ├─entity
    │  │      │      │  ├─mapper
    │  │      │      │  ├─naver
    │  │      │      │  ├─repository
    │  │      │      │  ├─response_dto
    │  │      │      │  ├─rss
    │  │      │      │  └─service
    │  │      │      ├─notification
    │  │      │      │  ├─batch
    │  │      │      │  ├─controller
    │  │      │      │  ├─entity
    │  │      │      │  ├─event
    │  │      │      │  ├─mapper
    │  │      │      │  ├─repository
    │  │      │      │  ├─response_dto
    │  │      │      │  └─service
    │  │      │      ├─user
    │  │      │      ├─comment
    │  │      │      ├─interest
    │  │      │      ├─activity_management
    │  │      │      ├─subscriptions
    │  │      │      ├─base
    │  │      │      │  └─entity
    │  │      │      ├─exception
    │  │      │      │  ├─article
    │  │      │      │  ├─comment
    │  │      │      │  ├─example
    │  │      │      │  ├─global
    │  │      │      │  ├─interest
    │  │      │      │  ├─notification
    │  │      │      │  └─subscription
    │  │      │      └─global
    │  |      |         ├─aop
    │  │      │         ├─config
    │  │      │         └─util
    │  │      │       
    │  │      └─jdh
    │  │          └─sample
    │  │              └─config
    │  │                  └─log
    │  └─resources
    │      └─static
    │          └─assets
    └─test
        ├─java
        │  └─com
        │      └─codeit
        │          └─monew
        │              ├─activity_management
        │              ├─article
        │              ├─comment
        │              │  └─service
        │              ├─interest
        │              │  └─service
        │              ├─notification
        │              │  ├─batch
        │              │  └─service
        │              ├─subscriptions
        │              │  └─service
        │              └─user
        │                  ├─batch
        │                  ├─controller
        │                  └─service
        └─resources
```

### 배포 주소
http://3.38.246.237/
