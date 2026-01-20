/**
 * 활성 세션 모니터링 JavaScript
 * 
 * @description 현재 로그인 중인 사용자 세션을 실시간으로 모니터링합니다.
 * @author ZiniData 개발팀
 * @since 2025.01.15
 */

// API 기본 URL
const API_BASE_URL = '/api/admin/statistics';

// 자동 갱신 타이머 ID
let refreshTimer = null;

/**
 * 페이지 초기화
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('[SESSIONS] 페이지 초기화 시작');
    
    // 초기 데이터 로드
    loadActiveSessions();
    
    // 이벤트 리스너 등록
    setupEventListeners();
    
    // 자동 갱신 시작
    const interval = document.getElementById('refreshInterval').value;
    startAutoRefresh(parseInt(interval));
});

/**
 * 이벤트 리스너 설정
 */
function setupEventListeners() {
    // 새로고침 버튼
    document.getElementById('refreshBtn').addEventListener('click', function() {
        console.log('[SESSIONS] 수동 새로고침 실행');
        loadActiveSessions();
    });
    
    // 갱신 주기 변경
    document.getElementById('refreshInterval').addEventListener('change', function(e) {
        const interval = parseInt(e.target.value);
        console.log(`[SESSIONS] 갱신 주기 변경: ${interval}ms`);
        
        // 기존 타이머 중지
        stopAutoRefresh();
        
        // 새로운 타이머 시작
        if (interval > 0) {
            startAutoRefresh(interval);
        }
    });
}

/**
 * 자동 갱신 시작
 */
function startAutoRefresh(interval) {
    if (interval <= 0) {
        console.log('[SESSIONS] 자동 갱신 비활성화');
        return;
    }
    
    console.log(`[SESSIONS] 자동 갱신 시작: ${interval}ms 주기`);
    
    refreshTimer = setInterval(() => {
        loadActiveSessions(true); // silent=true (토스트 메시지 안 띄움)
    }, interval);
}

/**
 * 자동 갱신 중지
 */
function stopAutoRefresh() {
    if (refreshTimer) {
        console.log('[SESSIONS] 자동 갱신 중지');
        clearInterval(refreshTimer);
        refreshTimer = null;
    }
}

/**
 * 활성 세션 목록 조회
 */
function loadActiveSessions(silent = false) {
    fetch(`${API_BASE_URL}/active-sessions`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    })
    .then(apiResponse => {
        console.log('[SESSIONS] API 응답:', apiResponse);
        
        if (apiResponse.success && apiResponse.data) {
            renderSessionList(apiResponse.data);
            updateLastUpdateTime();
            
            if (!silent) {
                showToast('세션 목록이 새로고침되었습니다.', 'success');
            }
        } else {
            throw new Error(apiResponse.message || '데이터 로드 실패');
        }
    })
    .catch(error => {
        console.error('[SESSIONS] 조회 오류:', error);
        showToast('세션 목록 조회 중 오류가 발생했습니다.', 'error');
    });
}

/**
 * 세션 목록 렌더링
 */
function renderSessionList(data) {
    console.log('[SESSIONS] 세션 목록 렌더링:', data);
    
    // 상단 통계 업데이트
    const totalUsers = data.totalUsers || 0;
    const totalSessions = data.sessions ? data.sessions.length : 0;
    
    document.getElementById('totalUsers').textContent = totalUsers;
    document.getElementById('totalSessions').textContent = totalSessions;
    document.getElementById('sessionInfo').textContent = 
        `로그인 ${totalUsers}명 / 전체 세션 ${totalSessions}개`;
    
    const tbody = document.getElementById('sessionTableBody');
    const emptyState = document.getElementById('emptyState');
    
    // 세션이 없는 경우
    if (!data.sessions || data.sessions.length === 0) {
        tbody.innerHTML = '';
        emptyState.classList.remove('hidden');
        return;
    }
    
    // 빈 상태 숨김
    emptyState.classList.add('hidden');
    
    // 테이블 렌더링
    tbody.innerHTML = data.sessions.map((session, index) => {
        const lastRequest = new Date(session.lastRequest);
        const idleMinutes = session.idleTimeMinutes || 0;
        const isExpired = session.expired;
        
        // 유휴 시간에 따른 색상 결정
        let idleColor = 'text-green-600 bg-green-50';
        if (idleMinutes > 30) {
            idleColor = 'text-red-600 bg-red-50';
        } else if (idleMinutes > 15) {
            idleColor = 'text-orange-600 bg-orange-50';
        } else if (idleMinutes > 5) {
            idleColor = 'text-yellow-600 bg-yellow-50';
        }
        
        return `
            <tr class="hover:bg-gray-50 transition-colors">
                <td class="px-6 py-3 text-sm font-medium text-gray-900">${index + 1}</td>
                <td class="px-6 py-3">
                    <div class="flex items-center gap-2">
                        <div class="w-8 h-8 rounded-full ${session.authenticated ? 'bg-blue-100' : 'bg-gray-100'} flex items-center justify-center">
                            <span class="text-xs font-semibold ${session.authenticated ? 'text-blue-600' : 'text-gray-400'}">${session.username.charAt(0).toUpperCase()}</span>
                        </div>
                        <span class="text-sm font-medium ${session.authenticated ? 'text-gray-900' : 'text-gray-500'}">${session.username}</span>
                    </div>
                </td>
                <td class="px-6 py-3 text-sm text-gray-700">
                    ${formatDateTime(lastRequest)}
                </td>
                <td class="px-6 py-3">
                    <span class="inline-flex items-center gap-1.5 px-2 py-1 rounded-full text-xs font-medium ${idleColor}">
                        <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clip-rule="evenodd"/>
                        </svg>
                        ${formatIdleTime(idleMinutes)}
                    </span>
                </td>
                <td class="px-6 py-3">
                    ${isExpired 
                        ? '<span class="inline-flex items-center gap-1 px-2 py-1 rounded-full bg-red-50 text-red-600 text-xs font-medium"><span class="w-1.5 h-1.5 bg-red-500 rounded-full"></span>만료됨</span>'
                        : '<span class="inline-flex items-center gap-1 px-2 py-1 rounded-full bg-green-50 text-green-600 text-xs font-medium"><span class="w-1.5 h-1.5 bg-green-500 rounded-full animate-pulse"></span>활성</span>'
                    }
                </td>
            </tr>
        `;
    }).join('');
}

/**
 * 날짜/시간 포맷팅
 */
function formatDateTime(date) {
    if (!date) return '-';
    
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    const hours = String(d.getHours()).padStart(2, '0');
    const minutes = String(d.getMinutes()).padStart(2, '0');
    const seconds = String(d.getSeconds()).padStart(2, '0');
    
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

/**
 * 유휴 시간 포맷팅
 */
function formatIdleTime(minutes) {
    if (minutes < 1) {
        return '방금 전';
    } else if (minutes < 60) {
        return `${Math.floor(minutes)}분 전`;
    } else {
        const hours = Math.floor(minutes / 60);
        const mins = Math.floor(minutes % 60);
        return `${hours}시간 ${mins}분 전`;
    }
}

/**
 * 마지막 업데이트 시간 갱신
 */
function updateLastUpdateTime() {
    const now = new Date();
    const timeString = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`;
    
    document.getElementById('lastUpdate').textContent = timeString;
    document.getElementById('lastUpdateTime').textContent = timeString;
}


