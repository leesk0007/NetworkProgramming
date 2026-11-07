# 🎯 실시간 선착순 초성 퀴즈 게임 (Chosung Quiz)

> **소켓 통신과 멀티스레드로 구현하는 다중 방 실시간 초성 퀴즈 게임**  
> 2026학년도 네트워크 프로그래밍 기말 텀프로젝트 (조 이름: **섭규**)

---

## 👥 팀원 및 역할 분담

| 이름 | 학번 | 역할 분담 | 담당 파트 |
| :--- | :--- | :--- | :--- |
| **임동섭** | 2331311 | **서버 로직 (Server Core)** | 소켓 세션 관리, 다중 방 생성/퇴장, `words.json` 파싱 및 초성 추출, `synchronized` 선착순 판정, 서버 관제 GUI |
| **이성규** | [학번] | **클라이언트 & UI (Client GUI)** | 메인/로비/게임방 화면 구성, `CardLayout` 화면 전환, 비동기 `receiveThread` 메시지 수신, 사용자 이벤트 및 점수판 갱신 |
| **공동** | - | **설계 & 협업** | `Protocol.java` 통신 규격 정의, 통합 동시성 테스트, GitHub 이슈 및 버전 관리 |

---

## 📌 프로젝트 개요 및 동기

* **주제**: 여러 사용자가 같은 방에 모여, 제시된 초성에 맞는 단어를 가장 먼저 입력한 사람이 점수를 획득하는 실시간 네트워크 퀴즈 게임
* **개발 동기**: 평소 즐겨 하던 ‘끄투 온라인’의 초성 퀴즈 게임을 보며, 수업 시간에 배운 **소켓 통신, 입출력 스트림, 멀티스레드 및 동기화** 개념을 직접 적용하여 실시간 대전 시스템을 구현하고자 기획하였습니다.

---

## 💡 교수님 피드백 반영 사항 (High-Traffic Network Design)

> **문제의식**: 퀴즈 정답을 알지 못할 때 발생하는 통신 침묵(Idle) 구간 및 패킷 단절 해소

1. **단계별 실시간 힌트 시스템 (Server Push)**: 10초 이상 미정답 시 한 글자씩 자동 오픈 패킷을 전송하여 정체 구간 해소
2. **실시간 오답 피드 브로드캐스팅**: 유저가 엔터로 시도한 모든 오답 단어를 방 전원에게 실시간 중계하여 초당 수십 개 패킷 교환 유도
3. **1초 단위 서버 타이머 틱 (Tick)**: 서버 타이머 스레드가 매 1초마다 남은 시간 패킷(`TIMER_TICK|초`)을 브로드캐스트하여 완벽한 동기화 보장
4. **실시간 입력 상태 공유 & 견제 아이템**: 플레이어 간 상호작용 및 방해 요소(1초 입력창 잠금 등)로 역동적인 멀티플레이 경험 제공

---

## 🛠️ 주요 기능

### 반드시 구현할 부분 (Must)
1. **다중 서버방**: 방 만들기, 방 목록 새로고침, 입장, 퇴장
2. **서버 GUI**: 서버 시작/중지, 접속자 수, 방 상태, 정답 제출 및 판정 결과 실시간 관제 모니터링
3. **라운드별 초성 출제**: `words.json`에서 단어를 불러와 한글 초성 추출 알고리즘 적용
4. **선착순 정답 판정**: 초성 일치 및 사전 검증, `synchronized` 동기화로 선착순 1등만 정답 처리
5. **점수 동기화**: 정답자 점수 가산 후 참가자 전원의 점수판 실시간 갱신
6. **화면 전환**: `CardLayout`으로 단일 프레임에서 로그인 $\leftrightarrow$ 로비 $\leftrightarrow$ 게임방 매끄러운 전환

### 부가 기능 (Nice to Have)
1. **플레이어 견제 아이템 2종**: 다른 유저 입력창 1초 잠금, 다음 문제 점수 2배
2. **인게임 실시간 채팅**: 방 안 플레이어 간 자유 대화
3. **스킵(Skip) 투표 시스템**: 정체 시 플레이어 과반수 찬성으로 다음 라운드 강제 진행

---

## 🏗️ 시스템 아키텍처 및 수업 핵심 개념 매핑

* **Swing GUI**: `JFrame` 상속, `BorderLayout` / `GridLayout` / `CardLayout` 복합 화면 구성
* **소켓 통신**: `ServerSocket.accept()`, 클라이언트 `Socket.connect(sa, 3000)` 타임아웃 연결
* **입출력 스트림**: `BufferedReader` + `InputStreamReader(..., "UTF-8")`, `PrintWriter(..., true)` autoFlush
* **멀티스레드**: 
  * 서버: `acceptThread` + 접속자별 `ClientHandler extends Thread`
  * 클라이언트: 백그라운드 수신 스레드 `receiveThread` (지연 취소 패턴 적용)
* **동기화 (Concurrency)**: 선착순 정답 판정 시 Race Condition 방지 (`synchronized`), `snapshotUsers()` 복사본 순회

---

## 📂 프로젝트 구조

```text
├── .github/
│   ├── ISSUE_TEMPLATE/
│   │   ├── feature_request.md
│   │   └── bug_report.md
│   └── PULL_REQUEST_TEMPLATE.md
├── src/
│   ├── Protocol.java       # 통신 규격 상수 및 메시지 조립 유틸리티
│   ├── ServerGUI.java      # 서버 메인 및 관제 GUI, ClientHandler, GameRoom
│   ├── ClientGUI.java      # 클라이언트 메인 GUI, CardLayout 화면 전환, receiveThread
│   └── words.json          # 초성 퀴즈용 단어 사전 데이터셋
├── .gitignore
└── README.md
```

---

## 🚀 실행 방법

### 서버 실행
```bash
javac -encoding UTF-8 ServerGUI.java Protocol.java
java ServerGUI
```

### 클라이언트 실행 (다중 실행 가능)
```bash
javac -encoding UTF-8 ClientGUI.java Protocol.java
java ClientGUI
```
