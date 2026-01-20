FROM openjdk:17-jdk-alpine

# 프록시 설정 (필요함)
ENV http_proxy http://yum.cloud.nice:3128
ENV https_proxy http://yum.cloud.nice:3128

# tzdata 패키지 설치
RUN apk add --no-cache tzdata

# timezone 설정
RUN cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime \
    && echo "Asia/Seoul" > /etc/timezone

ENV TZ=Asia/Seoul

# 앱 디렉토리 생성
RUN mkdir /app

# 헬스체크 추가
# HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
#   CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java","-jar","/app/nicevanas-1.0.0.war"]
