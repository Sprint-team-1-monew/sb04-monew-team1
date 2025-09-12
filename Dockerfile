# 멀티 스테이지, 경량 런타임

# build stage
FROM gradle:8.9.0-jdk17 AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew && ./gradlew clean bootJar --no-daemon

# run stage
FROM eclipse-temurin:17-jre
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
# 이 이미지는 n번 포트에서 돌아간다 / 이 이미지에 접근하기 위해서는 8080이라는 포트번호로 요청이 들어와야 접근 가능
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]