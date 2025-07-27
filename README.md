# 제주농디🍊 - Backend

> 제주도 공공데이터 활용 창업 공모전 프로젝트  
> 제주도 농업 정보와 농촌 일자리를 연결하는 플랫폼

## 📱 프로젝트 소개

**제주농디🍊**는 제주도의 농업 공공데이터를 활용하여 농업인과 구직자를 연결하는 종합 농업 플랫폼입니다.

### 🎯 주요 기능
- **🌾 유휴농지 정보**: 제주도 내 활용 가능한 농지 정보 제공
- **💼 농업 일자리**: 농장별 구인 정보와 매칭 서비스
- **🤝 멘토링**: 농업 전문가와 신규 농업인 연결
- **🌤️ 농업 정보**: 날씨, 작물 가격 등 실시간 정보
- **💬 채팅**: 실시간 소통 및 상담 서비스
- **🤖 AI 팁**: 농업 관련 인공지능 조언

## 🏗️ 아키텍처

```
Frontend (Flutter) → Backend (Spring Boot) → Database (PostgreSQL)
     ↓                       ↓                      ↓
 GitHub Pages            Render.com              Supabase
```

## 🛠 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.3
- **Language**: Java 21
- **Database**: PostgreSQL (Supabase)
- **Authentication**: Spring Security + JWT
- **Real-time**: WebSocket
- **Documentation**: Swagger/OpenAPI 3
- **Build Tool**: Gradle

### Infrastructure
- **Deployment**: Render.com
- **Database**: Supabase
- **CI/CD**: GitHub Actions

## 🚀 로컬 개발 환경 설정

### 1. 프로젝트 클론
```bash
git clone https://github.com/your-username/jeju-nongdi-be.git
cd jeju-nongdi-be
```

### 2. 애플리케이션 실행
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 3. API 문서 확인
애플리케이션 실행 후: `http://localhost:8080/swagger-ui.html`

## 🌐 배포 정보

### 🖥️ Production URLs
- **Frontend**: https://sonhyeokjin.github.io (GitHub Pages)
- **Backend**: https://jeju-nongdi-backend.onrender.com (Render.com)
- **API Docs**: https://jeju-nongdi-backend.onrender.com/swagger-ui.html

### 🗂️ 프로젝트 구조
```
src/
├── main/
│   ├── java/com/jeju_nongdi/jeju_nongdi/
│   │   ├── controller/     # REST API 컨트롤러
│   │   ├── service/        # 비즈니스 로직
│   │   ├── entity/         # JPA 엔티티
│   │   ├── repository/     # 데이터 접근 계층
│   │   ├── dto/           # 데이터 전송 객체
│   │   ├── config/        # 설정 클래스
│   │   └── security/      # 보안 설정
│   └── resources/
│       ├── application.properties
│       └── application-local.properties (로컬 전용)
```

## 📊 주요 API 엔드포인트

### 🔐 Authentication
```
POST /api/auth/signup     # 회원가입
POST /api/auth/login      # 로그인
```

### 🌾 농지 정보
```
GET  /api/farmlands           # 유휴농지 목록
POST /api/farmlands           # 농지 등록
GET  /api/farmlands/{id}      # 농지 상세정보
```

### 💼 구인정보
```
GET  /api/job-postings        # 일자리 목록
POST /api/job-postings        # 구인공고 등록
GET  /api/job-postings/{id}   # 구인공고 상세
```

### 🤝 멘토링
```
GET  /api/mentoring           # 멘토링 목록
POST /api/mentoring           # 멘토링 신청
```

### 💬 채팅
```
GET  /api/chat/rooms          # 채팅방 목록
POST /api/chat/rooms          # 채팅방 생성
WebSocket: /ws/chat           # 실시간 채팅
```

## 🔒 보안 설정

- **JWT 토큰 기반 인증**
- **CORS 정책 적용**
- **민감한 정보는 환경변수로 관리**
- **application-local.properties는 Git에서 제외**

## 🏆 공모전 정보

**제주도 공공데이터 활용 창업 공모전**
- 주최: 제주특별자치도
- 주제: 제주도 공공데이터를 활용한 창업 아이템
- 분야: 농업 × 기술 융합

## 🤝 팀 정보

### 역할 분담
- **Backend**: Spring Boot API 개발, 데이터베이스 설계
- **Frontend**: Flutter 앱 개발, UI/UX 디자인
- **Data**: 제주도 공공데이터 분석 및 가공

## 📞 문의

프로젝트 관련 문의사항이 있으시면 이슈를 등록해주세요.

---

**Made with ❤️ for Jeju Island Agriculture**
