# 🌟 뭐해영? 모아영!

대학생들의 시간표 공유와 저축 목표 달성을 함께할 수 있는 **캠퍼스 기반 소셜 일정 공유 & 금융 연동 서비스**입니다.

---

## 👨‍💻 팀원 & 역할

> 아래 표는 예시입니다. 실제 역할/기여도를 맞춰 수정해 주세요.

| 이름  | 주요 역할       | 담당 범위                         | 핵심 기여                               |
| --- | ----------- | ----------------------------- | ----------------------------------- |
| 조현우 | PM / 모아영 백엔드    | 일정/댓글 API, 배포                 | 일정 CRUD, 댓글 CRUD, 배포 파이프라인 구성       |
| 장가은 | 뭐해영 백엔드 / 프론트엔드 | UX 리서치, 피그마, 화면 설계            | 디자인 시스템, 온보딩/마이페이지 UI               |
| 김소연 | 뭐해영 백엔드 / 프론트엔드       | React Native(Expo), 일정 공유 SNS | 시간표/친구/피드 UI, 상태관리, 성능 개선           |
| 송성현 | 모아영 백엔드 / 인프라   | 인증(OAuth2), AWS, 모니터링         | Spring Security(OAuth2), EC2/RDS 구성 |
| 정원석 | 프론트엔드 총괄 및 API 연결      | 사진 업로드, 게시/댓글 인터랙션            | 미디어 업로드, 반응/댓글 UX                   |

---

## 🚀 프로젝트 소개

### **서비스명** : 뭐해영? 모아영!

#### 1️⃣ 뭐해영? – 시간표 기반 SNS

* 각자의 시간표와 일정을 공유할 수 있는 **일정기반 SNS 기능** 제공
* 사용자는 일정에 대해 **후기, 사진, 생각** 등을 자유롭게 기록
* **친구 맺기**를 통해 친구들과의 활발한 교류
* 친구의 시간표 일정을 확인하고 **댓글/반응**을 통해 소통 가능

#### 2️⃣ 모아영! – 일정과 저축의 연동

* 저축일정 완료 시 **자동 저축**으로 저축 습관 형성
* **저축 현황 조회**를 활용해 저축 금액 시각화
* **단체 돈관리 서비스**로 단체 활동 돈관리 간편화

---

## 🛠 기술 스택

* **Backend** : Spring Boot 3.x, Java 17, Spring Security (OAuth2)
* **Frontend** : React Native (Expo, TypeScript, expo-router)
* **Database** : MySQL 8.x (JPA/Hibernate)
* **Infra** : AWS EC2, RDS, (예: S3, CloudFront)  ← 필요 시 수정
* **협업툴** : GitHub, Notion, Figma, Postman

---

## 🗂 DB ERD



## 📦 폴더 구조 (예시)

```
client/
  ├─ app/ (expo-router)
  ├─ components/
  ├─ hooks/
  ├─ contexts/
  ├─ assets/
  ├─ types/
  ├─ constants/
  ├─ babel.config.js
  ├─ tsconfig.json
  └─ package.json
server/
  ├─ src/
  ├─ build.gradle / pom.xml
  └─ ...
```

---

## ⚙️ 환경 변수 (.env 예시)

> Expo는 `EXPO_PUBLIC_` 접두사를 사용하면 앱 번들에 주입됩니다.

```
# client/.env
EXPO_PUBLIC_SERVER_URL=https://api.example.com
```

프론트에서 서버 주소는 예: `import { SERVER_URL } from '@/constants/server'` 내부에서 `process.env.EXPO_PUBLIC_SERVER_URL` 을 참조하도록 구성합니다.

---

## 🏃‍♀️ Expo 실행 방법 (Frontend)

### 0) 사전 준비

* **Node.js LTS 18 또는 20** 권장
* iOS 시뮬레이터(Xcode) 또는 Android 에뮬레이터(Android Studio) 설치
* **Expo Go** 앱(실기기 테스트) 설치 가능

### 1) 설치

```bash
# 루트 또는 client 디렉터리에서
npm install
```

### 2) 개발 서버 실행

```bash
# 로컬 LAN
npx expo start

# 네트워크 제약 시, 터널 모드 (ngrok 자동 사용)
npx expo start --tunnel
```

* iOS 시뮬레이터: 터미널에서 `i`
* Android 에뮬레이터: 터미널에서 `a`
* 웹: 터미널에서 `w`
