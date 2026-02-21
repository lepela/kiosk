## 컴포넌트 구성
```coding
domain/terminal
  /entity
    Terminal
    enums/TerminalStatus
  /repository
    TerminalRepository
  /service
    TerminalCommandService        // 등록/상태변경/하트비트 저장
    TerminalAuthService           // 로그인(키 검증) + 세션 발급
    TerminalSessionService        // Redis 세션 CRUD (저수준)
  /controller
    TerminalAdminController       // 등록/비활성/정비모드
    TerminalPublicController      // 로그인/하트비트
  /dto
    TerminalRegisterRequest/Response
    TerminalLoginRequest/Response
    TerminalHeartbeatRequest/Response (선택)
```
- AuthService: 키 검증 + 세션 발급 (도메인 유스케이스)
- SessionService: Redis 직접 다루는 infra 성격 (token 저장/조회/삭제)
- CommandService: DB 엔티티 변경(heartbeat/status) 담당

## Redis
Key 규칙
- term:sess:<token> → terminalId
- TTL: 12h

TerminalSessionService 책임
- 토큰 발급/저장
- 토큰 검증(terminalId 조회)
- 로그아웃(삭제)

메서드 시그니처
- String createSession(Long terminalId)
- Long getTerminalIdByToken(String token) // 없으면 예외 or Optional
- void deleteSession(String token)
## 단말기 인증 서비스 (로그인 유스케이스)
TerminalAuthService 책임
- terminalId로 Terminal 조회
- status 체크(ACTIVE만 허용)
- terminalKey(원문) vs keyHash(BCrypt) 검증
- 세션 발급 후 토큰 반환
메서드 시그니처
- TerminalLoginResponse login(String terminalNameOrId, String terminalKey)
- terminalId + key
- void logout(String token)
검증/예외
- terminal 없음 → 401 
- key 불일치 → 401
- INACTIVE/MAINTENANCE → 403 
## 단말기 커맨드 서비스 (등록/상태/하트비트)
TerminalCommandService 책임
- 등록(terminalKey 생성 + hash 저장 + 엔티티 저장)
- 상태 전환(invalidate/maintenance)
- 하트비트 업데이트(lastHeartbeat)
메서드 시그니처
- TerminalRegisterResponse register(String name)
- 내부에서 plainKey 생성하고, hash 저장
- 응답엔 terminalId + plainKey (plainKey는 단 1회)
- void invalidate(Long terminalId)
- void maintenance(Long terminalId)
- void heartbeat(Long terminalId, LocalDateTime now)

heartbeat는 “인증된 terminalId”를 받아서 DB만 업데이트.
토큰 파싱/검증은 Filter 또는 PublicController에서 처리.

## 컨트롤러 흐름 (API 3개만 있으면 됨)
(A) 등록 (관리자용)
POST /api/terminals
- 입력: name
- 출력: terminalId, terminalKey(평문 1회)

(B) 로그인(세션 발급)
POST /api/terminals/sessions
- 입력: terminalId, terminalKey
- 출력: accessToken, expiresIn(=12h)

(C) 하트비트
POST /api/terminals/heartbeat
- 헤더: Authorization: Bearer <token>
- 내부: token → terminalId 조회 → heartbeat(terminalId, now)
- 응답: 204 or {serverTime}