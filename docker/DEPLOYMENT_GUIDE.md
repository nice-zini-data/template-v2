# Spring Boot Docker 배포 가이드

## 📋 목차
1. [배포 가이드](#배포-가이드)
2. [헬스체크 및 자동 재시작](#헬스체크-및-자동-재시작)
3. [파일 구조](#파일-구조)
4. [트러블슈팅](#트러블슈팅)

---

## 🚀 배포 가이드

### 1. 최초 설정 (1회만 실행)

#### Redis 공용 서비스 시작
```bash
cd /home/nice/template/docker/compose
docker-compose -f redis-compose.yml up -d
```

#### Spring Boot 애플리케이션 시작
```bash
cd /home/nice/template/docker/compose
docker-compose up -d
```

### 2. 일반 배포 (매번 실행)

#### 자동 배포 스크립트 사용 (권장)
```bash
cd /home/nice/template
./docker/template_build.sh
```

#### 수동 배포
```bash
# 1. Git 소스코드 업데이트
git pull origin master

# 2. Maven 빌드
mvn clean install -Pprod -e

# 3. Docker 컨테이너 재시작
cd /home/nice/template/docker/compose
docker-compose down
docker-compose up -d --build
```

### 3. 상태 확인

#### 컨테이너 상태 확인
```bash
docker ps
```

#### 헬스체크 상태 확인
```bash
docker inspect template-app | grep -A 5 "Health"
```

#### 애플리케이션 로그 확인
```bash
docker logs template-app
docker logs -f template-app  # 실시간 로그
```

### 4. 서비스 중지

#### Spring Boot 앱만 중지
```bash
cd /home/nice/template/docker/compose
docker-compose down
```

#### Redis도 중지 (주의: 다른 프로젝트에서 사용 중이면 중지하지 말 것)
```bash
docker-compose -f redis-compose.yml down
```

---

## 🔍 헬스체크 및 자동 재시작

### 헬스체크 설정

#### Dockerfile 헬스체크
```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
```

- **30초마다** 헬스체크 실행
- **3초 타임아웃** 후 실패 처리
- **60초 시작 대기** (앱 초기화 시간)
- **3회 연속 실패** 시 컨테이너 상태를 `unhealthy`로 변경

### 자동 재시작 스크립트

#### health_monitor.sh 생성
```bash
#!/bin/bash

# 헬스체크 및 자동 재시작 스크립트
CONTAINER_NAME="template-app"
LOG_FILE="/home/nice/template/logs/health_monitor.log"
RESTART_THRESHOLD=3  # 연속 실패 횟수

# 로그 디렉토리 생성
mkdir -p /home/nice/template/logs

# 헬스체크 함수
check_health() {
    local health_status=$(docker inspect $CONTAINER_NAME --format='{{.State.Health.Status}}' 2>/dev/null)
    echo "$(date '+%Y-%m-%d %H:%M:%S') - Health Status: $health_status" >> $LOG_FILE
    
    if [ "$health_status" = "unhealthy" ]; then
        return 1
    else
        return 0
    fi
}

# 재시작 함수
restart_container() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - Restarting container: $CONTAINER_NAME" >> $LOG_FILE
    
    cd /home/nice/template/docker/compose
    docker-compose restart template-app
    
    if [ $? -eq 0 ]; then
        echo "$(date '+%Y-%m-%d %H:%M:%S') - Container restarted successfully" >> $LOG_FILE
    else
        echo "$(date '+%Y-%m-%d %H:%M:%S') - Failed to restart container" >> $LOG_FILE
    fi
}

# 메인 로직
if ! check_health; then
    restart_container
fi
```

#### 스크립트 실행 권한 부여
```bash
chmod +x /home/nice/template/docker/health_monitor.sh
```

#### Cron 설정 (5분마다 체크)
```bash
# crontab 편집
crontab -e

# 다음 라인 추가
*/5 * * * * /home/nice/template/docker/health_monitor.sh
```

### 모니터링 명령어

#### 실시간 헬스체크 모니터링
```bash
# 1초마다 헬스체크 상태 확인
watch -n 1 'docker inspect template-app --format="{{.State.Health.Status}}"'
```

#### 헬스체크 로그 확인
```bash
tail -f /home/nice/template/logs/health_monitor.log
```

---

## 📁 파일 구조

```
docker/
├── Dockerfile                    # Spring Boot 앱용 Dockerfile
├── template_build.sh            # 자동 배포 스크립트
├── health_monitor.sh            # 헬스체크 및 자동 재시작 스크립트
└── compose/
    ├── docker-compose.yml       # Spring Boot 앱용 Compose
    └── redis-compose.yml        # Redis 공용 서비스용 Compose
```

---

## 🔧 트러블슈팅

### 1. 컨테이너가 시작되지 않는 경우
```bash
# 로그 확인
docker logs template-app

# 컨테이너 상태 확인
docker ps -a
```

### 2. 헬스체크 실패 시
```bash
# 수동으로 헬스체크 실행
curl -f http://localhost:8080/actuator/health

# 컨테이너 내부에서 확인
docker exec template-app wget --spider http://localhost:8080/actuator/health
```

### 3. 메모리 부족 시
```bash
# 컨테이너 리소스 사용량 확인
docker stats template-app

# 메모리 제한 설정 (docker-compose.yml에 추가)
deploy:
  resources:
    limits:
      memory: 1G
```

### 4. 네트워크 연결 문제
```bash
# 네트워크 확인
docker network ls
docker network inspect template-template-network
```

---

## 📞 지원

문제가 발생하면 다음 정보와 함께 문의하세요:
- 컨테이너 로그: `docker logs template-app`
- 헬스체크 상태: `docker inspect template-app --format='{{.State.Health}}'`
- 시스템 리소스: `docker stats template-app`
