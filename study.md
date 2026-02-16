Q1: @Transactional 없이
order.getItems().remove(item) 하면?

Q2: OSIV(Open Session In View)
켜져 있을 때 lazy loading은?

Q3: fetch join vs @EntityGraph
차이와 선택 기준은?

Q4: N+1 해결 방법 5가지는?

Q5: 2차 캐시 vs 쿼리 캐시
차이는?

---

## **Q1: @Transactional 없이 컬렉션 조작하면?**

### **상황:**

```java
// Service에 @Transactional 없음
public void removeItem(Long orderId, Long itemId) {
    Order order = orderRepository.findById(orderId).get();
    OrderItem item = order.getItems().stream()
        .filter(i -> i.getId().equals(itemId))
        .findFirst().get();
    
    order.getItems().remove(item);  // 컬렉션에서 제거
    
    // save() 호출 안 함
}
```

---

### **결과:**

```
@Transactional 없으면:
→ 변경 감지(dirty checking) 작동 ✗
→ DB에 반영 안 됨
→ 메모리에서만 제거됨

@Transactional 있으면:
→ 트랜잭션 커밋 시 변경 감지
→ orphanRemoval=true면 DELETE 쿼리 실행
→ DB에서도 삭제됨
```

---

### **정리:**

```java
// ✗ 작동 안 함
public void removeItem() {
    order.getItems().remove(item);
    // DB 변경 없음
}

// ✓ 작동함
@Transactional
public void removeItem() {
    order.getItems().remove(item);
    // 커밋 시 DELETE 쿼리 실행
}

// ✓ 작동함 (명시적)
public void removeItem() {
    order.getItems().remove(item);
    orderRepository.save(order);  // 명시적 저장
}
```

**핵심:**
- @Transactional = 변경 감지 활성화
- 없으면 명시적으로 save() 필요

---

## **Q2: OSIV와 lazy loading**

### **OSIV = Open Session In View:**

```yaml
# application.yml
spring:
  jpa:
    open-in-view: true  # 기본값
```

---

### **OSIV가 뭔가?:**

```
OSIV = true (기본):
- 영속성 컨텍스트가 View까지 살아있음
- Controller, Thymeleaf에서도 lazy loading 가능

┌─────────┐  ┌─────────┐  ┌──────┐
│Controller│→│ Service │→│ View │
└─────────┘  └─────────┘  └──────┘
    ↑──────── 영속성 유지 ────────┘

OSIV = false:
- 영속성 컨텍스트가 Service까지만
- Controller에서 lazy loading 불가
- LazyInitializationException 발생

┌─────────┐  ┌─────────┐  ┌──────┐
│Controller│→│ Service │→│ View │
└─────────┘  └─────────┘  └──────┘
              ↑─ 영속성 ─┘
```

---

### **예시:**

```java
// Controller
@GetMapping("/orders/{id}")
public String getOrder(@PathVariable Long id, Model model) {
    Order order = orderService.findById(id);
    
    // OSIV = true: 작동함
    order.getItems().size();  // lazy loading
    
    // OSIV = false: 에러!
    // LazyInitializationException
    
    model.addAttribute("order", order);
    return "order";
}
```

---

### **해결책 (OSIV = false일 때):**

```java
// Service에서 fetch join
@Transactional(readOnly = true)
public Order findById(Long id) {
    return orderRepository.findByIdWithItems(id);
}

// Repository
@Query("SELECT o FROM Order o " +
       "JOIN FETCH o.items " +
       "WHERE o.id = :id")
Order findByIdWithItems(@Param("id") Long id);
```

---

### **OSIV 장단점:**

```
OSIV = true (기본):
장점:
- 편함 (어디서든 lazy loading)
- 코드 간단

단점:
- DB 커넥션 오래 점유
- 성능 문제 (트래픽 많으면)
- 의도치 않은 쿼리 발생

OSIV = false:
장점:
- DB 커넥션 효율적
- 명시적 (쿼리 예측 가능)

단점:
- 귀찮음 (fetch join 필요)
- 코드 복잡

실무:
- 트래픽 적으면: true
- 트래픽 많으면: false
```

---

## **Q3: fetch join vs @EntityGraph**

### **둘 다 N+1 해결:**

```java
// 문제: N+1
List<Order> orders = orderRepository.findAll();
for (Order order : orders) {
    order.getItems().size();  // N번 쿼리
}

// 해결책 필요
```

---

### **1. fetch join (JPQL):**

```java
@Query("SELECT o FROM Order o " +
       "JOIN FETCH o.items")
List<Order> findAllWithItems();

// SQL:
SELECT o.*, i.*
FROM orders o
INNER JOIN order_item i ON o.id = i.order_id
```

**장점:**
- 명확함
- JPQL 표준
- 조건 추가 가능

**단점:**
- JPQL 작성 필요
- 쿼리 중복 가능

---

### **2. @EntityGraph (JPA):**

```java
@EntityGraph(attributePaths = {"items"})
@Query("SELECT o FROM Order o")
List<Order> findAllWithItems();

// 또는 메서드 이름만으로
@EntityGraph(attributePaths = {"items"})
List<Order> findAll();

// SQL: (fetch join과 동일)
SELECT o.*, i.*
FROM orders o
LEFT JOIN order_item i ON o.id = i.order_id
```

**장점:**
- 간단함
- JPQL 불필요
- 재사용 가능

**단점:**
- LEFT JOIN (fetch join은 INNER)
- 복잡한 조건 어려움

---

### **차이점:**

| 항목 | fetch join | @EntityGraph |
|------|-----------|--------------|
| 작성 | JPQL 필요 | 어노테이션만 |
| JOIN 타입 | INNER | LEFT (기본) |
| 조건 | 자유롭게 | 제한적 |
| 가독성 | 명확 | 간결 |

---

### **선택 기준:**

```
fetch join 사용:
- 복잡한 조건
- INNER JOIN 필요
- 동적 쿼리

@EntityGraph 사용:
- 간단한 eager loading
- 여러 필드 한번에
- 재사용 많은 경우

실무:
- 간단하면: @EntityGraph
- 복잡하면: fetch join
```

---

## **Q4: N+1 해결 방법 5가지**

### **문제:**

```java
List<Order> orders = orderRepository.findAll();  // 1번
for (Order order : orders) {
    order.getItems().size();  // N번
}

// 총 N+1번 쿼리
```

---

### **해결 1: fetch join**

```java
@Query("SELECT o FROM Order o JOIN FETCH o.items")
List<Order> findAllWithItems();

// 1번 쿼리로 해결
```

---

### **해결 2: @EntityGraph**

```java
@EntityGraph(attributePaths = {"items"})
List<Order> findAll();

// 1번 쿼리로 해결
```

---

### **해결 3: @BatchSize**

```java
@Entity
class Order {
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "order")
    List<OrderItem> items;
}

// N번 쿼리 → N/100번 쿼리로 감소
// WHERE order_id IN (?, ?, ?, ... 100개)
```

**장점:**
- 코드 변경 최소
- 글로벌 설정 가능

```yaml
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 100
```

---

### **해결 4: @Fetch(FetchMode.SUBSELECT)**

```java
@Entity
class Order {
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "order")
    List<OrderItem> items;
}

// SQL:
SELECT * FROM order_item
WHERE order_id IN (
    SELECT id FROM orders
)

// 2번 쿼리로 해결
```

---

### **해결 5: QueryDSL/native query로 직접 제어**

```java
// QueryDSL
return queryFactory
    .selectFrom(order)
    .leftJoin(order.items, orderItem).fetchJoin()
    .fetch();

// Native query
@Query(value = "SELECT o.*, i.* " +
               "FROM orders o " +
               "LEFT JOIN order_item i ON o.id = i.order_id",
       nativeQuery = true)
```

---

### **정리:**

| 방법 | 쿼리 수 | 난이도 | 추천도 |
|------|---------|--------|--------|
| fetch join | 1번 | 쉬움 | ★★★★★ |
| @EntityGraph | 1번 | 쉬움 | ★★★★★ |
| @BatchSize | N/100번 | 매우쉬움 | ★★★★☆ |
| SUBSELECT | 2번 | 보통 | ★★★☆☆ |
| QueryDSL | 1번 | 어려움 | ★★★☆☆ |

**추천:**
- 기본: fetch join / @EntityGraph
- 글로벌: @BatchSize (100~1000)
- 복잡: QueryDSL

---

## **Q5: 2차 캐시 vs 쿼리 캐시**

### **1차 캐시 (기본):**

```java
// 같은 트랜잭션 내
Order order1 = em.find(Order.class, 1L);  // DB 조회
Order order2 = em.find(Order.class, 1L);  // 캐시 (쿼리 ✗)

// 트랜잭션 끝나면 사라짐
```

---

### **2차 캐시 (Application 레벨):**

```java
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
class Product {
    // ...
}

// 설정
spring:
  jpa:
    properties:
      hibernate:
        cache:
          use_second_level_cache: true
          region.factory_class: org.hibernate.cache.jcache.JCacheRegionFactory

// 사용
Product p1 = em.find(Product.class, 1L);  // DB 조회, 캐시 저장
// ... 트랜잭션 종료 ...

// 다른 트랜잭션
Product p2 = em.find(Product.class, 1L);  // 캐시에서 (쿼리 ✗)
```

**특징:**
- 엔티티 단위 캐싱
- find(), getReference()만 적용
- 여러 트랜잭션에서 공유
- 자주 조회, 거의 안 변하는 데이터

---

### **쿼리 캐시:**

```java
// 설정
spring:
  jpa:
    properties:
      hibernate:
        cache:
          use_query_cache: true

// 사용
List<Product> products = em.createQuery(
    "SELECT p FROM Product p WHERE p.category = :cat",
    Product.class)
    .setParameter("cat", "COFFEE")
    .setHint("org.hibernate.cacheable", true)  // 캐싱 활성화
    .getResultList();

// 같은 쿼리 다시 실행
// → 캐시에서 반환 (쿼리 ✗)
```

**특징:**
- JPQL 쿼리 결과 캐싱
- 쿼리 + 파라미터 조합이 키
- 테이블 변경되면 무효화

---

### **차이점:**

| 항목 | 2차 캐시 | 쿼리 캐시 |
|------|----------|-----------|
| 대상 | 엔티티 (ID로 조회) | JPQL 쿼리 결과 |
| 메서드 | find(), getReference() | createQuery() |
| 키 | 엔티티 ID | 쿼리 + 파라미터 |
| 무효화 | 엔티티 변경 시 | 테이블 변경 시 |
| 용도 | 자주 조회되는 엔티티 | 자주 실행되는 쿼리 |

---

### **예시:**

```java
// 2차 캐시 적합
Product product = productRepository.findById(1L);
// ID로 단건 조회
// Product 거의 안 바뀜

// 쿼리 캐시 적합
List<Product> coffees = productRepository
    .findByCategory("COFFEE");
// 같은 쿼리 자주 실행
// 결과가 자주 바뀌지 않음
```

---

### **주의사항:**

```
2차 캐시:
- 엔티티가 변경되면 캐시 무효화
- 분산 환경에서 동기화 문제
- Redis 등 외부 캐시 추천

쿼리 캐시:
- 테이블 변경 시 모든 쿼리 캐시 무효화
- 성능 오히려 악화 가능
- 실무에서 잘 안 씀

실무:
- 2차 캐시: 제품, 카테고리 등
- 쿼리 캐시: 거의 안 씀
- 대신 Redis에 직접 캐싱
```

---

## **정리:**

### **Q1: @Transactional 없이 조작:**
```
→ 변경 감지 작동 ✗
→ DB 반영 안 됨
→ 명시적 save() 필요
```

### **Q2: OSIV:**
```
→ View까지 영속성 유지
→ true = 편함, DB 연결 오래
→ false = 귀찮음, 효율적
```

### **Q3: fetch join vs @EntityGraph:**
```
fetch join: 명확, INNER JOIN
@EntityGraph: 간편, LEFT JOIN
→ 간단하면 EntityGraph
→ 복잡하면 fetch join
```

### **Q4: N+1 해결:**
```
1. fetch join (추천)
2. @EntityGraph (추천)
3. @BatchSize (글로벌)
4. SUBSELECT
5. QueryDSL
```

### **Q5: 캐시:**
```
2차 캐시: 엔티티 ID 조회
쿼리 캐시: JPQL 결과
→ 실무는 Redis 직접 사용
```

---

## **고급 질문 5개:**

### **Q1: JPA 영속성 컨텍스트 동시성**

```java
// 상황
@Service
class OrderService {
    
    @Transactional
    public void processOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).get();
        order.setStatus("PROCESSING");
        
        // 여기서 다른 스레드가 같은 Order를 수정했다면?
        // 이 트랜잭션이 커밋될 때 어떻게 되나?
        
        // 추가 질문:
        // 1. Optimistic Lock vs Pessimistic Lock 차이는?
        // 2. @Version은 어떻게 작동하나?
        // 3. LockModeType.PESSIMISTIC_WRITE vs READ 차이는?
        // 4. 데드락은 언제 발생하고 어떻게 해결?
    }
}
```

**질문:**
- 동시성 제어 전략 설명해주세요
- Optimistic vs Pessimistic 언제 쓰나요?
- @Version 내부 동작 원리는?

---

### **Q2: JPA 1차 캐시 vs 2차 캐시 vs 쿼리 캐시 심화**

```java
// 상황 1
@Transactional
public void scenario1() {
    Order order1 = em.find(Order.class, 1L);
    order1.setStatus("PENDING");
    
    em.flush();
    em.clear();  // ← 영속성 컨텍스트 비움
    
    Order order2 = em.find(Order.class, 1L);
    // order2.getStatus()는? "PENDING"? 아니면 DB 값?
    
    // 추가 질문:
    // 1. flush()와 clear()의 정확한 차이는?
    // 2. clear() 후 find()는 DB를 다시 조회하나?
    // 3. 쓰기 지연 SQL 저장소는 언제 비워지나?
}

// 상황 2: 2차 캐시
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
class Product {
    // ...
}

// 질문:
// 1. READ_WRITE vs NONSTRICT_READ_WRITE vs READ_ONLY 차이는?
// 2. 캐시 무효화 전략은?
// 3. 분산 환경에서 캐시 동기화는?
```

**질문:**
- flush()와 clear()의 차이를 메모리 레벨에서 설명
- 2차 캐시 동시성 전략들의 트레이드오프는?
- 분산 환경 캐시 동기화 방법은?

---

### **Q3: JPA N+1의 숨겨진 함정**

```java
// 상황
@Entity
class Order {
    @ManyToOne(fetch = FetchType.EAGER)  // ← EAGER!
    private Customer customer;
    
    @OneToMany
    private List<OrderItem> items;
}

@Query("SELECT o FROM Order o WHERE o.id = :id")
Order findById(@Param("id") Long id);

// 질문:
// 1. EAGER인데 fetch join 안 쓰면?
// 2. 쿼리가 몇 번 나가나?
// 3. JPQL과 EntityManager.find()의 EAGER 처리 차이는?

// 추가 상황
@EntityGraph(attributePaths = {"items", "customer"})
Order findByIdWithGraph(Long id);

// 질문:
// 4. customer는 EAGER인데 EntityGraph에 또 넣으면?
// 5. 카테시안 곱은 언제 발생하나?
// 6. MultipleBagFetchException은 왜 발생?
```

**질문:**
- EAGER + JPQL의 N+1은 어떻게 발생?
- 여러 컬렉션 fetch join 시 문제점은?
- MultipleBagFetchException 해결 방법은?

---

### **Q4: @Transactional 전파 속성 (Propagation)**

```java
@Service
class OrderService {
    
    @Transactional
    public void createOrder() {
        Order order = new Order();
        orderRepository.save(order);
        
        try {
            paymentService.processPayment(order);  // ← 새 트랜잭션?
        } catch (Exception e) {
            // 예외 발생 시 order는 롤백되나?
        }
    }
}

@Service
class PaymentService {
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processPayment(Order order) {
        Payment payment = new Payment();
        paymentRepository.save(payment);
        
        throw new RuntimeException("결제 실패!");
    }
}

// 질문:
// 1. order는 저장되나? 롤백되나?
// 2. payment는 저장되나? 롤백되나?
// 3. REQUIRES_NEW vs REQUIRED vs NESTED 차이는?
// 4. 각각 언제 쓰나?
// 5. NESTED는 어떻게 구현되나? (세이브포인트?)
```

**질문:**
- 7가지 전파 속성 설명
- REQUIRES_NEW의 정확한 동작
- NESTED의 세이브포인트 동작 원리
- 실무에서 각각 언제 쓰나?

---

### **Q5: JPA 더티 체킹 vs merge() vs persist()**

```java
// 상황 1
@Transactional
public void scenario1() {
    Order order = new Order();
    order.setId(1L);  // ← 직접 ID 세팅
    order.setStatus("PENDING");
    
    orderRepository.save(order);
    // 이게 persist()? merge()?
    // INSERT? UPDATE?
}

// 상황 2
@Transactional
public void scenario2() {
    Order order = orderRepository.findById(1L).get();
    order.setStatus("COMPLETED");
    
    // save() 호출 안 함!
    
    // 커밋 시 UPDATE 쿼리 나가나?
    // 언제 나가나?
    // 어떻게 변경을 감지하나?
}

// 상황 3
public void scenario3() {
    Order order = new Order();
    order.setStatus("PENDING");
    
    em.persist(order);  // ← @Transactional 없음!
    
    // 어떻게 되나?
}

// 질문:
// 1. persist() vs merge()의 정확한 차이는?
// 2. save()는 내부적으로 뭘 호출?
// 3. 더티 체킹은 어떻게 구현되나?
// 4. @GeneratedValue vs 직접 ID 세팅 차이는?
// 5. 변경 감지 성능 최적화는?
```

**질문:**
- persist()와 merge()의 내부 동작
- 더티 체킹 구현 원리 (스냅샷?)
- save()의 isNew() 판단 로직
- 준영속 엔티티 병합 과정

---

## **보너스: 통합 시나리오**

```java
@Service
class ComplexService {
    
    @Transactional(
        isolation = Isolation.REPEATABLE_READ,
        propagation = Propagation.REQUIRED
    )
    public void complexOperation() {
        // 1. Order 조회 (Lock)
        Order order = orderRepository.findById(1L,
            LockModeType.PESSIMISTIC_WRITE);
        
        // 2. 상태 변경 (더티 체킹)
        order.setStatus("PROCESSING");
        
        // 3. 새 트랜잭션에서 Payment
        paymentService.process(order);
        
        // 4. 캐시에서 Product 조회
        Product product = productRepository.findById(1L);
        
        // 5. BatchSize로 items 조회
        order.getItems().forEach(item -> {
            item.updateQuantity(10);
        });
        
        // 질문:
        // - 쿼리가 총 몇 번?
        // - 각 쿼리의 순서는?
        // - Lock은 언제 획득/해제?
        // - 2차 캐시는 언제 조회?
        // - 더티 체킹은 언제 발생?
        // - Payment 트랜잭션 실패 시?
    }
}
```

---
disableCachingNullValues() 사용이유
>> 상품이 없는경우 까지를 캐싱했을때 상품이 추가되엇을때 ttl만료전까지 "없는상태"가 되어버림. 악의적 공격에 redis에 null stack 증가 우려.

GenericJackson2JsonRedisSerializer 보안 문제
>> @Class를 통한 디시리얼라이즈 어택에 취약. 프로덕션 단계에서 Jackson2JsonRedisSerializer, ObjectMapper로 변경 예정.

캐시 워밍 
>> 적용 예정