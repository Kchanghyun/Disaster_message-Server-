
---

## 🌐 `Disaster-Message-Server` — 서버(Spring Boot)

# Disaster Alert Translator Server (Spring Boot)

> 재난문자 수신 → 번역 요청 및 FCM 푸시 → 클라이언트 앱 전달 시스템

## 🧩 개요

본 서버는 90초마다 **재난문자 API**를 호출하여 최신 재난 정보를 수신한 후,  
**Firebase Cloud Messaging**을 통해 해당 내용을 클라이언트 앱에 전송합니다.

클라이언트 앱은 위치 기반 필터링 및 번역(GPT API)을 통해 사용자에게 알림을 제공합니다.

## 🌟 주요 기능

- 재난문자 공공 API 주기적 호출 (90초 간격)
- 재난 정보 필터링 및 파싱
- Firebase FCM을 이용한 앱 푸시
- (추가 예정) 관리자 웹 대시보드
- (추가 예정) 다국어 재난문자 이력 관리

## 🛠 사용 기술

| 기술 | 설명 |
|------|------|
| Java + Spring Boot | 백엔드 프레임워크 |
| MySQL or PostgreSQL | 재난문자 로그 저장 (추가 예정) |
| Firebase FCM | 클라이언트 푸시 알림 |
| REST API | 클라이언트 통신 |
| AWS EC2 (Ubuntu) | 서버 호스팅 |
| Crontab | 90초 주기 실행 관리 |

## 🗃 DB 설계 (추가 예정)

> 아래는 재난문자를 저장할 경우의 테이블 예시입니다.

```sql
CREATE TABLE disaster_alert (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255),
  category VARCHAR(50),
  message TEXT,
  regions TEXT,
  timestamp DATETIME
);
