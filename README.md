<!-- ABOUT THE PROJECT -->
##  E-commerce MSA Project 🛍️🛒 

### 1. Summary

* 이 프로젝트는 **특정 시간대에 오픈된 선착순 상품 구매가 원활하게 이루어지도록 설계된 E-commerce 서비스**입니다. 동시성 문제와 대규모 트래픽 등을 고려하여,  많은 사용자가 동시에 접근하더라도 지연 없이 안정적인 거래가 가능하도록  아키텍처 설계를 고민했습니다.

### 2. Period

* 2024년 8월 ~ 2024년 9월 ( 1人 )

### 3. Built With

* Backend
 [![Java 21][Java]][Java-url] [![Spring Boot][SpringBoot]][SpringBoot-url] [![Spring Cloud][SpringCloud]][SpringCloud-url] [![Spring Data JPA][SpringDataJPA]][SpringDataJPA-url] [![JWT][JWT]][JWT-url]

* Database
 [![MySQL][MySQL]][MySQL-url] [![Redis][Redis]][Redis-url]

* Service Discovery & Communication
 [![Eureka Server][EurekaServer]][EurekaServer-url] [![Feign Client][FeignClient]][FeignClient-url]

* Testing
 [![JUnit 5][JUnit5]][JUnit5-url] [![JMeter][JMeter]][JMeter-url]

## Getting Started (예정)

##  System Design 

### 1. ER Diagram
![[image][image]][erd-url]

### 2. Project Architecture
![[image][image]][architecture-url]

### 3. API documentation (예정)

### 4. Main Feature

1.  **회원 관리 및 인증/인가 프로세스**

	- 이메일 인증을 통한 회원가입 
		- Redis의 해시 구조를 사용한 이메일 인증 코드 저장 및 관리
		- 만료 시간을 설정하여 3분 후 자동으로 만료

	- Spring Security Crypto Module을 이용한 회원 정보 암호화 
		- PasswordEncoder를 사용한 비밀번호 암호화 
		- AesBytesEncryptor를 사용한 개인정보 암호화 

	- 인증/인가 프로세스
		- 로그인 시 JWT Access Token과 Refresh Token 발급
		- 만료된 Access Token은 Redis에 저장된 Refresh Token을 검증하여 재발급
		- API Gateway에서 토큰을 검증하고, Claim에 포함된 유저 정보를 헤더에 추가하여 서비스 이용
	
2.  **주문 및 재고 관리 프로세스**

	- 주문 처리 
		- 주문 진행 시 Redis에서 재고 감소, 결제 처리 후 DB 재고 반영
		- 주문 실패 시(고객 이탈 및 결제 실패) 시 Redis의 재고 복구
		
	- Redis를 활용한 재고 관리
		- Redis의 해시(Hash) 구조를 사용하여 상품 재고 및 이벤트 시간 캐싱
		- Redisson Lock을 활용하여 동시성 문제 해결 및 데이터 일관성 유지

	-  스케줄러를 이용한 주문 및 배송 상태 변경 시나리오
		- 매일 자정 주문 상태 변경 
		- 배송준비중 → 배송중 / 배송중 → 배송 완료 /반품진행중 → 반품 완료

	-  주문 취소 및 반품 시나리오
		- 배송 준비 중일 경우 주문 취소가 가능하며, 취소 후 즉시 재고 증가 
		- 배송 완료 후 1일 이내 반품이 가능하며, 반품 신청 후 1일 뒤에 재고 증가
		
	- 그 외 구현 기능들
		- 장바구니 추가/삭제 및 수량 증감 기능
		- 카테고리 별 상품 목록 조회 및 상세 조회
		- 주문 상세 조회
		- 주문 취소 및 반품

## Trouble Shooting (예정)
`
<!-- MARKDOWN LINKS & IMAGES -->
[Java]: https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white
[Java-url]: https://www.oracle.com/java/
[SpringBoot]: https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white
[SpringBoot-url]: https://spring.io/projects/spring-boot
[SpringCloud]: https://img.shields.io/badge/Spring_Cloud-6DB33F?style=for-the-badge&logo=spring&logoColor=white
[SpringCloud-url]: https://spring.io/projects/spring-cloud
[SpringDataJPA]: https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white
[SpringDataJPA-url]: https://spring.io/projects/spring-data-jpa
[MySQL]: https://img.shields.io/badge/MySQL-00758F?style=for-the-badge&logo=mysql&logoColor=white
[MySQL-url]: https://www.mysql.com/
[Redis]: https://img.shields.io/badge/Redis-D82C20?style=for-the-badge&logo=redis&logoColor=white
[Redis-url]: https://redis.io/
[JWT]: https://img.shields.io/badge/JSON_Web_Tokens-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white
[JWT-url]: https://jwt.io/
[JUnit5]: https://img.shields.io/badge/JUnit_5-25A162?style=for-the-badge&logo=junit5&logoColor=white
[JUnit5-url]: https://junit.org/junit5/
[JMeter]: https://img.shields.io/badge/JMeter-D73D4A?style=for-the-badge&logo=apache&logoColor=white
[JMeter-url]: https://jmeter.apache.org/
[EurekaServer]: https://img.shields.io/badge/Eureka_Server-6DB33F?style=for-the-badge&logo=spring&logoColor=white
[EurekaServer-url]: https://spring.io/projects/spring-cloud-netflix
[FeignClient]: https://img.shields.io/badge/Feign_Client-6DB33F?style=for-the-badge&logo=spring&logoColor=white
[FeignClient-url]: https://spring.io/projects/spring-cloud-openfeign
[erd-url]: https://private-user-images.githubusercontent.com/174220273/364687102-d330acb5-e4cc-4dd1-b736-97f91e6b5a2a.png?jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3MjU1Nzg0NTAsIm5iZiI6MTcyNTU3ODE1MCwicGF0aCI6Ii8xNzQyMjAyNzMvMzY0Njg3MTAyLWQzMzBhY2I1LWU0Y2MtNGRkMS1iNzM2LTk3ZjkxZTZiNWEyYS5wbmc_WC1BbXotQWxnb3JpdGhtPUFXUzQtSE1BQy1TSEEyNTYmWC1BbXotQ3JlZGVudGlhbD1BS0lBVkNPRFlMU0E1M1BRSzRaQSUyRjIwMjQwOTA1JTJGdXMtZWFzdC0xJTJGczMlMkZhd3M0X3JlcXVlc3QmWC1BbXotRGF0ZT0yMDI0MDkwNVQyMzE1NTBaJlgtQW16LUV4cGlyZXM9MzAwJlgtQW16LVNpZ25hdHVyZT1mNTZmYzEyYTJiMzkwMDY4Y2ZjOGJkNTExMzExYjhjM2VmZjk4NTZhOTYwNzZlNTZmYjRhMDdlYzE4Y2FhYzRmJlgtQW16LVNpZ25lZEhlYWRlcnM9aG9zdCZhY3Rvcl9pZD0wJmtleV9pZD0wJnJlcG9faWQ9MCJ9.1KxtUlE6dqS_KBNsezlm9-6g2hHyUrfZRB4Mu78gSSQ
[architecture-url]: https://private-user-images.githubusercontent.com/174220273/364977919-faa37c3c-7cd5-4aca-938c-fcb1dbdf1bf4.png?jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3MjU1NzgwOTYsIm5iZiI6MTcyNTU3Nzc5NiwicGF0aCI6Ii8xNzQyMjAyNzMvMzY0OTc3OTE5LWZhYTM3YzNjLTdjZDUtNGFjYS05MzhjLWZjYjFkYmRmMWJmNC5wbmc_WC1BbXotQWxnb3JpdGhtPUFXUzQtSE1BQy1TSEEyNTYmWC1BbXotQ3JlZGVudGlhbD1BS0lBVkNPRFlMU0E1M1BRSzRaQSUyRjIwMjQwOTA1JTJGdXMtZWFzdC0xJTJGczMlMkZhd3M0X3JlcXVlc3QmWC1BbXotRGF0ZT0yMDI0MDkwNVQyMzA5NTZaJlgtQW16LUV4cGlyZXM9MzAwJlgtQW16LVNpZ25hdHVyZT0wZjhlMjY3ZDg2ZWE4YTYzMGNmYzUyZTg5OGVmMmUyNTRhNGFmYWYwNzE0NzMxNmNkNjEwYjQ2ZGVmMzc1NDNkJlgtQW16LVNpZ25lZEhlYWRlcnM9aG9zdCZhY3Rvcl9pZD0wJmtleV9pZD0wJnJlcG9faWQ9MCJ9.DEtFyoMaXMKInj9xNZbUrpUwsc5YVx-0I4mQo3mDDxY
