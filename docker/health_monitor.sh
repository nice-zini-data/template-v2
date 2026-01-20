#!/bin/bash

# ===============================================
# Spring Boot 애플리케이션 헬스체크 및 자동 재시작 스크립트
# ===============================================
# 작성일: 2025.09.23
# 목적: 컨테이너 헬스체크 실패 시 자동 재시작
# ===============================================

CONTAINER_NAME="template-app"
LOG_FILE="/home/nice/template/logs/health_monitor.log"
RESTART_THRESHOLD=3  # 연속 실패 횟수

# 로그 디렉토리 생성
mkdir -p /home/nice/template/logs

# 로그 함수
log_message() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" >> $LOG_FILE
}

# 헬스체크 함수
check_health() {
    local health_status=$(docker inspect $CONTAINER_NAME --format='{{.State.Health.Status}}' 2>/dev/null)
    
    if [ $? -ne 0 ]; then
        log_message "ERROR: Failed to inspect container $CONTAINER_NAME"
        return 1
    fi
    
    log_message "Health Status: $health_status"
    
    if [ "$health_status" = "unhealthy" ]; then
        return 1
    elif [ "$health_status" = "healthy" ]; then
        return 0
    else
        # starting, none 등의 상태는 정상으로 간주
        return 0
    fi
}

# 재시작 함수
restart_container() {
    log_message "WARNING: Container $CONTAINER_NAME is unhealthy. Attempting restart..."
    
    cd /home/nice/template/docker/compose
    
    # 컨테이너 재시작
    docker-compose restart template-app
    
    if [ $? -eq 0 ]; then
        log_message "SUCCESS: Container $CONTAINER_NAME restarted successfully"
        
        # 재시작 후 30초 대기 후 헬스체크
        sleep 30
        if check_health; then
            log_message "SUCCESS: Container $CONTAINER_NAME is healthy after restart"
        else
            log_message "WARNING: Container $CONTAINER_NAME still unhealthy after restart"
        fi
    else
        log_message "ERROR: Failed to restart container $CONTAINER_NAME"
    fi
}

# 메인 로직
log_message "Starting health check for $CONTAINER_NAME"

if ! check_health; then
    restart_container
else
    log_message "Container $CONTAINER_NAME is healthy"
fi

log_message "Health check completed"
