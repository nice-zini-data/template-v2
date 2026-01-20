#!/bin/bash
 
# ===============================================
# Spring Boot 애플리케이션 자동 배포 스크립트
# ===============================================
# 작성일: 2025.10.15
# 목적: Git pull → Maven 빌드 → Docker 재배포
# ===============================================
 
set -e  # 에러 발생 시 스크립트 중단
 
# 프로젝트 루트 디렉토리로 이동
cd /home/nice/nice_van_as
 
echo "==============================================="
echo "🚀 Spring Boot 애플리케이션 배포 시작"
echo "==============================================="
 
# ===============================================
# 1. Git 소스코드 업데이트
# ===============================================
echo "📥 Git 소스코드 업데이트 중..."
 
git fetch --all
git reset --hard origin/main
git pull origin main
 
echo "✅ Git 업데이트 완료"
 
# ===============================================
# 2. Maven 빌드 실행
# ===============================================
echo "🔨 Maven 빌드 실행 중..."
 
# Java 17 환경 설정 (자동 감지)
if [ -d "/usr/lib/jvm/java-17-openjdk" ]; then
    export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
elif [ -d "/usr/lib/jvm/java-17-amazon-corretto" ]; then
    export JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto
elif [ -d "/usr/lib/jvm/java-17" ]; then
    export JAVA_HOME=/usr/lib/jvm/java-17
else
    echo "❌ Java 17이 설치되지 않았습니다. 설치 후 다시 시도하세요."
    exit 1
fi
 
export PATH=$JAVA_HOME/bin:$PATH
 
# Java 버전 확인
echo "📋 사용 중인 Java 버전:"
java -version
 
mvn clean install -Pmain -e
 
# 빌드 결과 확인
build_result=$?
echo $build_result > nicevanas_check.log
 
if [ "$build_result" -ne 0 ]; then
    echo "❌ ============ 빌드 실패 =============="
    echo "🔄 Git 롤백 실행 중..."
   
    # 빌드 실패 시 이전 커밋으로 롤백
    git reset --hard ORIG_HEAD
   
    echo "❌ 배포 실패 - 이전 버전으로 롤백됨"
   
    exit 1
else
    echo "✅ ============ 빌드 성공 =============="
fi
 
# ===============================================
# 3. Docker 컨테이너 재배포
# ===============================================
echo "🐳 Docker 컨테이너 재배포 중..."
 
cd /home/nice/nice_van_as/docker/compose

# 현재 서버 정보 확인
CURRENT_HOSTNAME=$(hostname)
CURRENT_IP=$(hostname -I | awk '{print $1}')

# Redis 컨테이너 상태 확인 (WAS1에서만 실행)
if [[ "$CURRENT_HOSTNAME" == "cmap-was01" ]] || [[ "$CURRENT_IP" == "10.175.1.101" ]]; then
    echo "📍 현재 서버: WAS1 (Redis 서버) - $CURRENT_HOSTNAME ($CURRENT_IP)"
    if ! docker ps | grep -q "shared-redis"; then
        echo "🔴 Redis 컨테이너가 없습니다. Redis를 먼저 시작합니다..."
        docker-compose -f redis-compose.yml up -d
        sleep 5
    else
        echo "🟢 Redis 컨테이너가 이미 실행 중입니다."
    fi
else
    echo "📍 현재 서버: WAS2 (Redis 클라이언트) - $CURRENT_HOSTNAME ($CURRENT_IP)"
    echo "ℹ️  WAS2(10.175.1.101)의 Redis를 사용합니다. 로컬 Redis는 실행하지 않습니다."
fi
 
# Spring Boot 컨테이너만 재시작
echo "🛑 Spring Boot 컨테이너 중지 중..."
docker-compose stop nicevanas-app 2>/dev/null || true
docker-compose rm -f nicevanas-app 2>/dev/null || true

echo "🚀 Spring Boot 컨테이너 시작 중..."
# 프로파일별 Redis 호스트 설정
SPRING_PROFILES_ACTIVE=main SPRING_REDIS_HOST=10.175.1.101 docker-compose up -d --build nicevanas-app
 
echo "✅ ============ 배포 완료 =============="
echo "🌐 애플리케이션 접속: https://ncs.nicebizmap.co.kr"
echo "==============================================="
 
exit 0