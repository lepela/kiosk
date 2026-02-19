# Kiosk Management System

## 프로젝트 소개
키오스크 관리 시스템 백엔드 API

## 기술 스택
- Java 21
- Spring Boot 3.5.10
- Spring Data JPA
- Redis (Cache)
- MySQL
- Docker Compose

## 주요 기능
- 상품 관리 (CRUD, 캐싱)
- 주문 처리 (재고 관리, 동시성 제어)
- 터미널 관리

## 실행 방법
```bash
# Redis 실행
docker-compose up -d

# 애플리케이션 실행
./gradlew bootRun
```

## API 문서
- Swagger UI: http://localhost:8080/swagger-ui.html
- Error Codes: [docs/error-codes.md](docs/error-codes.md)

## 주요 기술 특징
- Pessimistic Lock으로 동시성 제어
- 데드락 방지 (정렬 기반)
- Redis 분산 캐시
- DDD 기반 패키지 구조
- Custom Exception 체계

## 추후 예정 
- 결제, 터미널, 관리자 구현

## 기본 로컬 서버
- API : http://localhost:8080
- DB Manage (Adminer) : http://localhost:9090
- Redis Stack : http://localhost:8081

## 트러블슈팅 이력
```
# 2026-02-19
현상: Redis에서 Record 타입 역직렬화 시 데이터 불일치.
원인: Record의 Final 특성과 기본 생성자 부재로 인한 LinkedHashMap 매핑.
해결: TypeResolverBuilder 커스텀 설정을 통한 클래스 타입 보존.
```
       
    