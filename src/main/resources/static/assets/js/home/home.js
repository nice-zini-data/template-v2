/**
 * ============================================
 * 지니데이타 홈페이지 (Home Page)
 * ============================================
 * 
 * 🎯 홈페이지 책임
 * ✅ 메뉴 활성화: PC/모바일 메뉴 상태 관리
 * ✅ 타이핑 애니메이션: 메인 텍스트 타이핑 효과
 * ✅ 카운트업 애니메이션: 통계 숫자 카운트업
 * ✅ 슬라이더: Swiper 슬라이더 관리
 * ✅ 섹션 애니메이션: AOS 라이브러리 초기화
 * ✅ 버튼 이벤트: 상권 분석, 트렌드, 제휴 문의, BetterBoss
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 * @refactored 2025.10
 */

$(document).ready(function() {
    /**
     * 모든 타이밍 관련 타이머 초기화
     */
    function clearAllTimeouts() {
        if (animationTimeout) {
            clearTimeout(animationTimeout);
        }
    }

    /**
     * 로그인 정보 없으면 로그인 페이지로 자동 이동
    */

    const loginInfoCheck = async () => {
        console.log(Zinidata.auth.session.isLoggedIn());

        if (Zinidata.auth.session.isLoggedIn() === null || Zinidata.auth.session.isLoggedIn() === "") {
            // sessionStorage 등에 로그인 정보가 없으면
            window.location.href = '/auth/login';
        }

    }

    function homeStats(){
        // Zinidata.auth.gps를 사용하여 GPS 좌표 획득
        Zinidata.auth.gps.getCurrentPosition(
            // 성공 콜백
            function(centerX, centerY) {
                const radius = 1000; // 1000미터
                
                console.log('[HOME] GPS 좌표 획득:', { centerX, centerY, radius });
                
                // API 호출
                callHomeStatsApi(centerX, centerY, radius);
            },
            // 실패 콜백
            function(error) {
                console.error('[HOME] GPS 위치 정보를 가져올 수 없습니다:', error.message);
                
                // GPS 실패 시에도 전역 변수에 기본 좌표가 설정되어 있음
                const coords = Zinidata.auth.gps.getCurrentCoordinates();
                const radius = 2000;
                
                console.log('[HOME] 기본 좌표 사용:', { 
                    centerX: coords.centerX, 
                    centerY: coords.centerY, 
                    radius 
                });
                
                // API 호출
                callHomeStatsApi(coords.centerX, coords.centerY, radius);
            }
        );
    }
    
    /**
     * 홈 통계 API 호출 함수
     * @param {number} centerX 경도
     * @param {number} centerY 위도
     * @param {number} radius 반경
     */
    function callHomeStatsApi(centerX, centerY, radius) {
        Zinidata.api({
            url: '/api/home/stats',
            method: 'POST',
            data: {
                centerX: centerX,
                centerY: centerY,
                radius: radius
            },
            success: function(response) {
                console.log('[HOME] 통계 정보 조회 성공:', response);
                
                // 통계 데이터 처리
                if (response.success && response.data) {
                    const stats = response.data;
                    console.log('[HOME] 반경 내 요청 수:', stats.radiusCnt);
                    console.log('[HOME] 오늘 요청 수:', stats.todayCnt);
                    console.log('[HOME] 사용자 요청 수:', stats.requestCnt);
                    console.log('[HOME] 사용자 실행 수:', stats.execCnt);
                    
                    // HTML 요소에 값 설정
                    updateHomeStatsDisplay(stats);
                }
            },
            error: function(xhr, status, error) {
                console.error('[HOME] 통계 정보 조회 실패:', error);
                console.error('[HOME] 응답:', xhr.responseText);
                
                // 에러 시 기본값 표시
                updateHomeStatsDisplay({
                    radiusCnt: 0,
                    todayCnt: 0,
                    requestCnt: 0,
                    execCnt: 0
                });
            }
        });
    }
    
    /**
     * 홈 통계 데이터를 HTML에 표시
     * @param {Object} stats 통계 데이터
     */
    function updateHomeStatsDisplay(stats) {
        try {
            // 반경 내 요청 수
            const $radiusCnt = $('#homeStatsRadiusCnt');
            if ($radiusCnt.length > 0) {
                // skeleton 제거하고 실제 데이터 삽입
                $radiusCnt.empty().text((stats.radiusCnt || 0) + '건');
            }
            
            // 오늘 등록된 요청 수
            const $todayCnt = $('#homeStatsTodayCnt');
            if ($todayCnt.length > 0) {
                // skeleton 제거하고 실제 데이터 삽입
                $todayCnt.empty().text((stats.todayCnt || 0) + '건');
            }
            
            // 사용자 요청 수 (요청 진행중)
            const $requestCnt = $('#homeStatsRequestCnt');
            if ($requestCnt.length > 0) {
                // skeleton 제거하고 실제 데이터 삽입
                $requestCnt.empty().text(stats.requestCnt || 0);
            }
            
            // 사용자 실행 수 (처리중)
            const $execCnt = $('#homeStatsExecCnt');
            if ($execCnt.length > 0) {
                // skeleton 제거하고 실제 데이터 삽입
                $execCnt.empty().text(stats.execCnt || 0);
            }
            
            console.log('[HOME] 통계 데이터 표시 완료:', {
                radiusCnt: stats.radiusCnt || 0,
                todayCnt: stats.todayCnt || 0,
                requestCnt: stats.requestCnt || 0,
                execCnt: stats.execCnt || 0
            });
            
        } catch (error) {
            console.error('[HOME] 통계 데이터 표시 중 오류:', error);
        }
    }

    // 세션 체크 완료 후 다른 프로세스 실행
    loginInfoCheck().then(() => {
        homeStats();
    });
    
    // 메뉴 활성화 실행
    Zinidata.menu.activate('home');
    
    // 페이지 로드 시 자동 시작
    window.addEventListener('load', () => {
        console.log('[HOME] 페이지 로드 완료');
    });

    changeService();
});



const changeService = () => {
    let lastScrollTop = 0;

    $('.mainContent.scrollBox').on('scroll touchmove', function(){
    const currentScrollTop = $(this).scrollTop();

    if (Math.abs(currentScrollTop - lastScrollTop) < 5) return;

    if (currentScrollTop < lastScrollTop) {
        $('.changeServiceBtn').removeClass('hidden');
    } else {
        $('.changeServiceBtn').addClass('hidden');
    }

    lastScrollTop = currentScrollTop;
    });

}