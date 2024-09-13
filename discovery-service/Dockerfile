#base image
FROM eclipse-temurin:21-jdk-alpine

# work directory
WORKDIR /app

# build 완료 된 jar 파일을 컨테이너로 복사
COPY build/libs/*.jar /app/discovery-service.jar

#ENV SPRING_PROFILES_ACTIVE =dev

# jar 파일 실행 명령어 설정
ENTRYPOINT ["java", "-jar", "/app/discovery-service.jar"]