/**
 * 지니데이타 문자인증 모듈
 * 
 * 휴대폰 문자인증 관련 기능을 제공합니다.
 * 인증번호 발송, 확인, 타이머 관리 등을 담당합니다.
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 */

$(document).ready(function() {

    // Cert 모듈 정의
    Zinidata.cert = {
        
        // =============================================
        // 📱 문자인증 관련 기능
        // =============================================
        
        // 모듈 내부 상태 변수들
        state: {
            randomStr: 0,
            seqNo: 0,
            timeLeft: 600, // 10분 = 600초
            timerInterval: null,
            certYn: "N", // 인증 상태
            objPage: {
                navbar: "",
                side: "",
                page: window.location.pathname // 현재 페이지 경로로 동적 설정
            }
        },

        /**
         * 초기화
         */
        init: function() {
            console.log('문자인증 모듈 초기화 중...');
            try {
                this.setupEventListeners();
                this.resetState();
                console.log('문자인증 모듈 초기화 완료');
            } catch (error) {
                console.error('문자인증 초기화 오류:', error);
            }
        },

        /**
         * 상태 초기화
         */
        resetState: function() {
            this.state.timeLeft = 600;
            this.state.certYn = "N";
            this.clearTimer();
            
            // 전역 변수 동기화 (기존 코드 호환성)
            window.certYn = this.state.certYn;
            
            console.log('문자인증 상태 초기화 완료');
        },

        /**
         * 이벤트 리스너 설정
         */
        setupEventListeners: function() {
            var self = this;
            
            try {
                // 인증번호 받기 버튼
                $("#cert").off('click').on('click', function() {
                    self.sendCertNumber();
                });

                // 인증번호 확인 버튼
                $("#getCert").off('click').on('click', function() {
                    self.verifyCertNumber();
                });
                
                console.log('문자인증 이벤트 리스너 설정 완료');
            } catch (error) {
                console.error('문자인증 이벤트 리스너 설정 오류:', error);
            }
        },

        /**
         * 인증번호 발송
         */
        sendCertNumber: function() {
            console.log('=== 인증번호 발송 요청 ===');
            
            // 휴대폰 번호 유효성 검사
            if (!this.validatePhoneNumber()) {
                return;
            }

            var self = this;
            var phoneNumber = $("#phone").val();
            var currentPath = this.state.objPage.page;
            var memNm = $("#crtName").val();
            
            Zinidata.api({
                url: '/api/cert/send',
                method: 'POST',
                useToken: false,
                showLoading: true,
                loadingTarget: $("#cert"),
                data: {
                    mobileNo: phoneNumber,
                    pathName: currentPath,
                    memNm: memNm
                },
                success: function(response) {
                    console.log('=== 인증번호 발송 API 응답 ===', response);
                    
                    if (response.success === true) {
                        self.handleSendSuccess(response);
                    } else {
                        self.handleError(response, '인증번호 발송');
                    }
                },
                error: function(xhr, status, error) {
                    console.error('=== 인증번호 발송 API 오류 ===', error);
                    
                    if (xhr.responseJSON) {
                        self.handleError(xhr.responseJSON, '인증번호 발송');
                    } else {
                        self.handleError({message: '네트워크 오류가 발생했습니다. 다시 시도해주세요.'}, '인증번호 발송');
                    }
                }
            });
        },

        /**
         * 인증번호 확인
         */
        verifyCertNumber: function() {
            console.log('=== 인증번호 확인 요청 ===');
            
            // 시간 초과 체크
            if (this.state.timeLeft < 0) {
                Zinidata.showAlert('인증 시간이 초과되었습니다. 인증번호를 재요청해주세요.', "fail");
                return;
            }

            // 인증번호 입력 체크
            var verifyCode = $("#verifyCode").val();
            if (!verifyCode || verifyCode.trim() === '') {
                Zinidata.showAlert("인증번호를 입력해주세요.", "fail");
                $("#verifyCode").focus();
                return;
            }

            var self = this;
            
            Zinidata.api({
                url: '/api/cert/verify',
                method: 'POST',
                useToken: false,
                showLoading: true,
                loadingTarget: $("#getCert"),
                data: {
                    mobileNo: $('#phone').val(),
                    certNo: verifyCode.trim()
                },
                success: function(response) {
                    console.log('=== 인증번호 확인 API 응답 ===', response);
                    
                    if (response.success === true) {
                        self.handleVerifySuccess(response);
                    } else {
                        self.handleError(response, '인증번호 확인');
                    }
                },
                error: function(xhr, status, error) {
                    console.error('=== 인증번호 확인 API 오류 ===', error);
                    
                    if (xhr.responseJSON) {
                        self.handleError(xhr.responseJSON, '인증번호 확인');
                    } else {
                        self.handleError({message: '네트워크 오류가 발생했습니다. 다시 시도해주세요.'}, '인증번호 확인');
                    }
                }
            });
        },

        /**
         * 휴대폰 번호 유효성 검사
         */
        validatePhoneNumber: function() {
            var phoneNumber = $("#phone").val();
            
            if (!phoneNumber || phoneNumber.trim() === '') {
                Zinidata.showAlert("휴대폰번호를 입력해주세요.", "fail");
                $("#phone").focus();
                return false;
            }

            // 공통함수로 휴대폰 번호 유효성 검사
            if (!Zinidata.validation.phone(phoneNumber)) {
                Zinidata.showAlert("올바른 휴대폰번호 형식이 아닙니다. (010-XXXX-XXXX 또는 +82 10-XXXX-XXXX)", "fail");
                $("#phone").focus();
                return false;
            }

            return true;
        },

        /**
         * 인증번호 발송 성공 처리
         */
        handleSendSuccess: function(response) {
            console.log('=== 인증번호 발송 성공 ===', response);
            
            // 인증 상태 초기화
            this.state.certYn = "N";
            window.certYn = this.state.certYn;

            // 타이머 시작
            this.startTimer();

            // UI 업데이트
            this.updateUIAfterSend();
            
            // 성공 메시지
            Zinidata.showAlert(response.message || "인증번호가 발송되었습니다.", "success");
        },

        /**
         * 인증번호 확인 성공 처리
         */
        handleVerifySuccess: function(response) {
            console.log('=== 인증번호 확인 성공 ===', response);
            
            // 인증 상태 업데이트
            this.state.certYn = "Y";
            window.certYn = this.state.certYn;
            
            // 타이머 중지
            this.clearTimer();
            
            // UI 업데이트
            this.updateUIAfterVerify();
            
            // 성공 메시지
            Zinidata.showAlert(response.message || "휴대폰 인증이 완료되었습니다.", "success");
        },

        /**
         * 에러 처리
         */
        handleError: function(response, type) {
            console.log(`=== ${type} 오류 ===`, response);
            
            var errorMessage = response.message || '알 수 없는 오류가 발생했습니다.';
            Zinidata.showAlert(errorMessage, "fail");
            
            // 인증번호 확인 실패 시 인증 상태 초기화
            if (type === '인증번호 확인') {
                this.state.certYn = "N";
                window.certYn = this.state.certYn;
            }
        },

        /**
         * 발송 후 UI 업데이트
         */
        updateUIAfterSend: function() {
            try {
                $("#getCert").prop('disabled', false);
                $("#getCert").removeClass("wh_time_n");
                $("#cert").text("재전송");
                $("#tel02").attr('disabled', false);
                $(".verifyCode").removeClass("hidden").show();
                
                console.log('인증번호 발송 후 UI 업데이트 완료');
            } catch (error) {
                console.error('UI 업데이트 오류:', error);
            }
        },

        /**
         * 인증 완료 후 UI 업데이트
         */
        updateUIAfterVerify: function() {
            try {
                // 휴대폰 번호 입력 필드 비활성화
                $("#phone").attr('disabled', true);
                
                // 인증번호 입력란 슬라이드 업
                $(".verifyCode").slideUp(500);
                
                // 인증번호 재전송 버튼 비활성화
                $("#cert").prop('disabled', true);
                $("#cert").addClass('wh_time_n');
                $("#cert").text("인증 완료");

                // 타이머 완료 표시
                $('.time').text('인증완료');
                
                // 로그인 버튼 활성화
                $("#loginBtn").removeClass('disabled');

                console.log('인증 완료 후 UI 업데이트 완료');
                
            } catch (error) {
                console.error('UI 업데이트 오류:', error);
            }
        },

        /**
         * 타이머 시작
         */
        startTimer: function() {
            this.clearTimer();
            this.state.timeLeft = 600;
            
            var self = this;
            this.state.timerInterval = setInterval(function() {
                self.updateTimer();
            }, 1000);
            
            console.log('인증 타이머 시작 (10분)');
        },

        /**
         * 타이머 업데이트
         */
        updateTimer: function() {
            var minutes = Math.floor(this.state.timeLeft / 60);
            var seconds = this.state.timeLeft % 60;
            
            // 시간을 00:00 형태로 포맷팅
            var formattedTime = (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
            
            // 타이머 UI 업데이트
            $('.time').text(formattedTime);
            
            // 시간 감소
            this.state.timeLeft--;
            
            // 시간 만료 시 타이머 중지
            if (this.state.timeLeft < 0) {
                this.handleTimeExpired();
            }
        },

        /**
         * 타이머 만료 처리
         */
        handleTimeExpired: function() {
            console.log('=== 인증 시간 만료 ===');
            
            this.clearTimer();
            this.state.certYn = "N";
            window.certYn = this.state.certYn;
            
            // UI 업데이트
            $('.time').text('시간만료');
            $("#getCert").prop('disabled', true);
            $("#getCert").addClass("wh_time_n");
            
            Zinidata.showAlert('인증 시간이 만료되었습니다. 인증번호를 재요청해주세요.', 'fail');
        },

        /**
         * 타이머 정리
         */
        clearTimer: function() {
            if (this.state.timerInterval) {
                clearInterval(this.state.timerInterval);
                this.state.timerInterval = null;
            }
        },

        /**
         * 인증 상태 반환
         */
        getCertStatus: function() {
            return this.state.certYn;
        },

        /**
         * 인증 완료 여부 확인
         */
        isCertified: function() {
            return this.state.certYn === "Y";
        },

        /**
         * 인증 상태 초기화 (외부 호출용)
         */
        resetCertification: function() {
            console.log('=== 인증 상태 외부 초기화 ===');
            this.resetState();
            this.updateUIAfterReset();
        },

        /**
         * 초기화 후 UI 업데이트
         */
        updateUIAfterReset: function() {
            try {
                // 휴대폰 번호 입력 필드 활성화
                $("#phone").attr('disabled', false);
                
                // 인증번호 입력란 숨기기
                $(".verifyCode").hide();
                
                // 버튼 상태 초기화
                $("#cert").prop('disabled', false);
                $("#cert").removeClass('wh_time_n');
                $("#cert").text("인증번호 받기");
                
                $("#getCert").prop('disabled', true);
                $("#getCert").addClass("wh_time_n");
                
                // 타이머 초기화
                $('.time').text('10:00');
                
                console.log('인증 초기화 후 UI 업데이트 완료');
            } catch (error) {
                console.error('UI 초기화 오류:', error);
            }
        }
    };

    // 모듈 자동 초기화 (로드되는 페이지에서 바로 초기화)
    console.log('지니데이타 문자인증 모듈 로드 완료');
    console.log('=== 문자인증 모듈 자동 초기화 ===');
    Zinidata.cert.init();

}); 

// =============================================
// 🔧 기존 코드 호환성을 위한 전역 변수
// =============================================

// 기존 코드 호환성을 위한 전역 변수
window.certYn = "N";

// certYn 상태 동기화 (기존 코드 호환성)
$(document).ready(function() {
    setInterval(function() {
        if (typeof Zinidata !== 'undefined' && Zinidata.cert) {
            window.certYn = Zinidata.cert.getCertStatus();
        }
    }, 500); // 0.5초마다 동기화
});