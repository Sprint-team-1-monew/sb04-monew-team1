# =======================
# build stage
# =======================
FROM gradle:8.9.0-jdk17 AS build
WORKDIR /app

# Gradle 관련 파일만 먼저 복사 (캐시 활용)
COPY build.gradle settings.gradle gradlew /app/
COPY gradle /app/gradle
RUN chmod +x gradlew

# 의존성 다운로드 (변경 없으면 캐시 그대로 활용)
RUN ./gradlew dependencies --no-daemon || return 0

# 소스 코드 복사 (이 부분이 자주 바뀌므로 뒤로 배치)
COPY src /app/src

# 애플리케이션 빌드
RUN ./gradlew clean bootJar --no-daemon

# =======================
# run stage
# =======================
FROM eclipse-temurin:17-jre
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
WORKDIR /app

# 빌드된 JAR만 복사
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
