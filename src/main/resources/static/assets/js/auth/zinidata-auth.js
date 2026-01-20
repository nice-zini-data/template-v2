/**
 * ============================================
 * 지니데이타 인증 모듈 (Authentication Module)
 * ============================================
 * 
 * 🎯 인증 책임
 * ✅ 로그인: login.*
 * ✅ 회원가입: signup.*
 * ✅ 아이디/비밀번호 찾기: findId.*
 * ✅ 세션 관리: session.*
 * ✅ 로그아웃: logout.*
 * ✅ OAuth: oauth.*
 * ✅ 유틸리티: utils.*
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 * @refactored 2025.10
 */

$(document).ready(function() {
    // ============================== 모듈 의존성 체크 ==============================
    if (typeof Zinidata === 'undefined' || !Zinidata.device) {
        console.error('[AUTH] Core 모듈이 먼저 로드되어야 합니다.');
        return;
    }

    // ============================== 인증 모듈 정의 ==============================
    Zinidata.auth = {
        
        // ============================== 사용자 상태 확인 ==============================
        /**
         * 사용자 정보 조회
         * @returns {Object|null} 사용자 정보 또는 null
         */
        getUserInfo: function() {
            // TODO: 실제 사용자 정보 조회 로직 구현 예정
            return {
                isLoggedIn: this.isLoggedIn(),
                userId: null,
                password: null
            };
        },
        
        // ============================== 로그인 함수 ==============================
        /**
         * 로그인 초기화
         */
        login: function() {
            console.log('[AUTH] 로그인 모듈 초기화');
            this.setupLoginEventListeners();
        },

        /**
         * 로그인 이벤트 리스너 설정
         */
        setupLoginEventListeners: function() {
            console.log('[AUTH] setupEventListeners 함수 실행 시작');
            const self = Zinidata.auth;
            
            // 폼의 action 속성을 완전히 비활성화 (강력한 보안)
            $('#loginForm').attr('action', 'javascript:void(0);');
            $('#loginForm').attr('onsubmit', 'return false;');
            $('#loginForm').attr('method', 'post'); // POST로 설정하되 action은 비활성화
            
            // 로그인 폼 submit 이벤트 (강화된 바인딩)
            $('#loginForm').on('submit', function(e) {
                console.log('[AUTH] 폼 제출 이벤트 감지');
                e.preventDefault();
                e.stopPropagation();
                e.stopImmediatePropagation();
                self.handleLoginSubmit(e);
                return false;
            });
            
            // 로그인 버튼 클릭 이벤트 (추가 보안)
            $('#loginForm button[type="submit"]').on('click', function(e) {
                console.log('[AUTH] 로그인 버튼 클릭');
                e.preventDefault();
                e.stopPropagation();
                e.stopImmediatePropagation();
                self.handleLoginSubmit(e);
                return false;
            });
            
            // 전화번호 자동 포맷팅
            $('#phoneNumber').on('input', function() {
                let value = $(this).val().replace(/[^0-9]/g, '');
                if (value.length >= 3) {
                    if (value.length <= 7) {
                        value = value.substring(0, 3) + '-' + value.substring(3);
                    } else if (value.length <= 11) {
                        value = value.substring(0, 3) + '-' + value.substring(3, 7) + '-' + value.substring(7);
                    } else {
                        value = value.substring(0, 11);
                    }
                }
                $(this).val(value);
            });
            
            // 폼의 모든 input에서 Enter 키 차단
            /* $('#loginForm input').on('keypress', function(e) {
                if (e.which === 13) { // Enter 키
                    console.log('[AUTH] Enter 키 감지');
                    e.preventDefault();
                    e.stopPropagation();
                    e.stopImmediatePropagation();
                    self.handleLoginSubmit(e);
                    return false;
                }
            }); */
            
            console.log('[AUTH] 로그인 이벤트 리스너 설정 완료');
        },

        /**
         * 로그인 폼 제출 처리
         */
        handleLoginSubmit: function(e) {
            console.log('[AUTH] 로그인 폼 제출 처리 시작');
            
            // 이벤트 전파 완전 차단
            if (e) {
                e.preventDefault();
                e.stopPropagation();
            }
            
            const $form = $('#loginForm');
            if ($form.length === 0) {
                console.error('[AUTH] 로그인 폼을 찾을 수 없습니다.');
                return false;
            }
            
            const formData = Zinidata.form.toJson($form);
            // console.log('[AUTH] 폼 데이터 추출:', { loginId: formData.loginId, password: '***' });
            
            this.submitLogin(formData);
            return false;
        },

        /**
         * 로그인 API 호출 (아이디 존재 여부 확인 후 회원가입/로그인 처리)
         */
        submitLogin: function(formData) {
            console.log('[AUTH] 로그인 API 호출:', formData.loginId);
            const self = this;
                    
            const loginId = formData.loginId;
            const password = formData.password;
            const phoneNumber = formData.phone;
            const saveInfo = $('#saveInfo').is(':checked');
            
            // 입력값 검증 - 각 필드에 focus
            if (!loginId) {
                alert('아이디를 입력해주세요.');
                $('#loginId').focus();
                return;
            }
            if (!password) {
                alert('비밀번호를 입력해주세요.');
                $('#password').focus();
                return;
            }
            if (!phoneNumber) {
                alert('휴대폰 번호를 입력해주세요.');
                $('#phone').focus();
                return;
            }

            // SMS 인증 완료 여부 확인
            // if (!window.certYn || window.certYn !== 'Y') {
            //     Zinidata.showAlert('휴대폰 인증을 완료해주세요.', 'doneRed');
            //     $('#phone').focus();
            //     return false;
            // }

            // 정보 저장 체크 시 쿠키에 저장
            if (saveInfo) {
                saveUserInfo(loginId, password, phoneNumber);
            } else {
                // 저장하지 않을 경우 쿠키 삭제
                clearSavedInfo();
            }
            
            self.callNibsContractLogin(formData);
            
            return true;
        },

        /**
         * NIBS 계약 로그인 API 호출
         */
        callNibsContractLogin: function(formData) {
            const self = this;

            console.log('[AUTH] NIBS 계약 로그인 API 호출 시작');

            Zinidata.api({
                url: '/api/auth/nibs-contract-login',
                method: 'POST',
                contentType: 'application/json',
                useToken: false,
                showLoading: true,
                loadingTarget: $('#loginForm button[type="submit"]'),
                data: {
                    userName:formData.userName,
                    loginId: formData.loginId,
                    password: formData.password,
                    phoneNumber: $('#phone').val()
                },
                success: function(response) {
                    console.log('[AUTH] NIBS 계약 로그인 성공:', response);
                    if (response.success === true) {
                        console.log('[AUTH] 로그인 성공:', response);
                        self.handleLoginSuccess(response.data);
                    } else {
                        console.error('[AUTH] NIBS 계약 로그인 실패:', response);
                        Zinidata.showAlert(response.message || '사용자 확인 중 오류가 발생했습니다.', 'doneRed');

                    }
                },
                error: function(xhr, status, error) {
                    console.error('[AUTH] NIBS 계약 로그인 API 오류:', error);
                    if (xhr.responseJSON) {
                        Zinidata.showAlert(xhr.responseJSON.message || '사용자 확인 중 오류가 발생했습니다.', 'doneRed');
                    } else {
                        Zinidata.showAlert('네트워크 오류가 발생했습니다. 다시 시도해주세요.', 'doneRed');
                    }
                }
            });
        },


        /**
         * 아이디 존재 여부 확인
         */
        /* checkUserExists: function(formData) {
            
            const self = this;
            
            Zinidata.api({
                url: '/api/auth/check-user',
                method: 'POST',
                contentType: 'application/json',
                useToken: false,
                showLoading: true,
                loadingTarget: $('#loginForm button[type="submit"]'),
                data: {
                    loginId: formData.loginId,
                    passWord: formData.passWord,
                },
                success: function(response) {
                    console.log('[AUTH] 사용자 존재 여부 확인:', response);
                    
                    if (response.success === true) {
                        
                        let formData2 = {};
                        formData2.loginId = formData.loginId;
                        formData2.passWord = formData.passWord;
                        formData2.userName = response.data.userName;

                        if (response.data.exists) {
                            // 사용자가 존재하면 로그인 시도
                            self.attemptLogin(formData);
                        } else {
                            // 사용자가 없으면 회원가입 후 자동 로그인
                            self.registerAndLogin(formData);
                        }
                    } else {
                        Zinidata.showAlert(response.message || '사용자 확인 중 오류가 발생했습니다.', 'doneRed');
                    }
                },
                error: function(xhr, status, error) {
                    console.error('[AUTH] 사용자 확인 API 오류:', error);
                    
                    if (xhr.responseJSON) {
                        Zinidata.showAlert(xhr.responseJSON.message || '사용자 확인 중 오류가 발생했습니다.', 'doneRed');
                    } else {
                        Zinidata.showAlert('사용자 확인 중 오류가 발생했습니다.', 'doneRed');
                    }
                }
            });
        }, */

        /**
         * 로그인 시도
         */
        /* attemptLogin: function(formData) {
            const self = this;
            
            console.log('[AUTH] 로그인 처리 시작');
            Zinidata.api({
                url: '/api/auth/login',
                method: 'POST',
                contentType: 'application/json',
                useToken: false,
                showLoading: true,
                loadingTarget: $('#loginForm button[type="submit"]'),
                data: {
                    loginId: formData.loginId,
                    password: formData.password,
                    memNm: formData.userName || formData.loginId, // 이름이 없으면 아이디 사용
                    mobileNo: $('#phone').val() || '',
                },
                success: function(response) {
                    console.log('[AUTH] 로그인 성공:', response);
                    if (response.success === true) {
                        self.handleLoginSuccess(response.data);
                    } else {
                        self.handleLoginError(response);
                    }
                },
                error: function(xhr, status, error) {
                    if (xhr.status !== 401) {
                        console.error('[AUTH] 로그인 API 오류:', error);
                    }
                    
                    if (xhr.responseJSON) {
                        self.handleLoginError(xhr.responseJSON);
                    } else {
                        self.handleLoginError({message: '네트워크 오류가 발생했습니다. 다시 시도해주세요.'});
                    }
                }
            });
        }, */


        /**
         * 회원가입 후 자동 로그인
         */
        /* registerAndLogin: function(formData) {
            const self = this;
            
            // 회원가입 데이터 준비
            const registerData = {
                loginId: formData.loginId,
                password: formData.password,
                memNm: formData.userName || formData.loginId, // 이름이 없으면 아이디 사용
                mobileNo: $('#phone').val() || '',
                authCd: formData.authCd,
            };
            
            console.log('[AUTH] 회원가입 시도:', registerData);
            
            Zinidata.api({
                url: '/api/auth/register',
                method: 'POST',
                contentType: 'application/json',
                useToken: false,
                showLoading: true,
                loadingTarget: $('#loginForm button[type="submit"]'),
                data: registerData,
                success: function(response) {
                    console.log('[AUTH] 회원가입 성공:', response);
                    
                    if (response.success === true) {
                        // 회원가입 성공 후 자동 로그인
                        Zinidata.showAlert('회원가입이 완료되었습니다. 자동으로 로그인합니다.', 'doneGreen');
                        
                        // 약간의 지연 후 로그인 시도
                        setTimeout(function() {
                            self.attemptLogin(formData);
                        }, 1000);
                    } else {
                        Zinidata.showAlert(response.message || '회원가입 중 오류가 발생했습니다.', 'doneRed');
                    }
                },
                error: function(xhr, status, error) {
                    console.error('[AUTH] 회원가입 API 오류:', error);
                    
                    if (xhr.responseJSON) {
                        Zinidata.showAlert(xhr.responseJSON.message || '회원가입 중 오류가 발생했습니다.', 'doneRed');
                    } else {
                        Zinidata.showAlert('회원가입 중 오류가 발생했습니다.', 'doneRed');
                    }
                }
            });
        }, */

        /**
         * 로그인 성공 처리 (세션 기반)
         */
        handleLoginSuccess: function(loginData) {
            console.log('[AUTH] 로그인 성공 처리:', loginData);
            
            try {
                // 1. 사용자 정보 저장 (sessionStorage만 사용)
                Zinidata.auth.session.saveUserInfo({
                    memNo: loginData.memNo,
                    loginId: loginData.loginId,
                    memNm: loginData.memNm,
                    emailAddr: loginData.emailAddr || loginData.email,
                    mobileNo: loginData.mobileNo,
                    authCd: loginData.authCd,
                    sessionId: loginData.sessionId,
                    loginTimestamp: loginData.loginTimestamp
                });
                
                // 2. 헤더 상태 업데이트
                // if (typeof window.updateHeaderLoginStatus === 'function') {
                //     window.updateHeaderLoginStatus();
                // }
                
                // 3. 성공 메시지
                Zinidata.showAlert('로그인되었습니다.', 'doneGreen');
                
                // 4. 페이지 이동
                setTimeout(function() {
                    // 서버에서 전달한 redirectUrl을 우선 사용
                    let redirectUrl = loginData.redirectUrl;
                    
                    // redirectUrl이 없으면 URL 파라미터에서 확인
                    if (!redirectUrl) {
                        const urlParams = new URLSearchParams(window.location.search);
                        redirectUrl = urlParams.get('returnUrl') || urlParams.get('redirect');
                    }
                    
                    // 최종 리다이렉트
                    window.location.href = redirectUrl && redirectUrl.trim() !== '' ? redirectUrl : '/';
                }, 1000);
                
            } catch (error) {
                console.error('[AUTH] 로그인 성공 처리 중 오류:', error);
                this.handleLoginError({message: '로그인 처리 중 오류가 발생했습니다.'});
            }
        },

        /**
         * 로그인 오류 처리
         */
        handleLoginError: function(response) {
            console.log('[AUTH] 로그인 오류:', response);
            
            // 세션 정보 정리
            Zinidata.auth.session.clear();
            
            // 로딩 해제
            const $submitBtn = $('#loginForm button[type="submit"]');
            if ($submitBtn.length > 0) {
                Zinidata.hideLoading($submitBtn, '로그인');
            }
            
            // 오류 메시지 표시
            const errorMessage = response.message || '알 수 없는 오류가 발생했습니다.';
            Zinidata.showAlert(errorMessage, 'doneRed');
        },

        // ============================== 회원가입 함수 ==============================
        // 실시간 중복 체크용 debounce 타이머 (Zinidata.performance 활용)
        userIdTimer: null,
        emailTimer: null,
        
        /**
         * 회원가입 초기화
         */
        signup: function() {
            console.log('[AUTH] 회원가입 모듈 초기화 중...');
            try {
                this.setupSignupEventListeners();
                this.setupPhoneNumberCheck();
                this.setupSignupInitialState();
                console.log('[AUTH] 회원가입 모듈 초기화 완료');
            } catch (error) {
                console.error('[AUTH] 회원가입 초기화 오류:', error);
            }
        },

        /**
         * 회원가입 이벤트 리스너 설정
         */
        setupSignupEventListeners: function() {
            const self = this;
            
            try {
                // 아이디 실시간 중복 체크 (debounce 적용)
                $("#userId").off('input').on('input', function() {
                    self.handleUserIdInput($(this).val());
                });
                
                // 이메일 실시간 중복 체크 (debounce 적용)
                $("#email").off('input').on('input', function() {
                    self.handleEmailInput($(this).val());
                });
                
                // 비밀번호 보기/숨기기 토글
                $("#togglePassword").off('click').on('click', function() {
                    self.togglePasswordVisibility();
                });
                
                // 비밀번호 실시간 유효성 검사
                $("#password").off('keyup').on('keyup', function() {
                    self.validatePassword($(this).val());
                });
                
                // 비밀번호 입력 필드 포커스 시 조건창 표시 (모든 조건 만족 시에는 제외)
                $("#password").off('focus').on('focus', function(){
                    const password = $(this).val();
                    const allConditionsMet = self.validatePasswordConditions(password);
                    
                    // 모든 조건이 만족되지 않았을 때만 조건창 표시
                    if(!allConditionsMet) {
                        $('.passwordInfoBox').show();
                    }
                });
                
                // 비밀번호 입력 필드에서 포커스가 벗어날 때 조건이 모두 만족되면 조건창 숨김
                $("#password").off('blur').on('blur', function(){
                    const password = $(this).val();
                    const allConditionsMet = self.validatePasswordConditions(password);
                    
                    if(allConditionsMet) {
                        $('.passwordInfoBox').hide();
                    }
                });
                
                // 전체 약관 동의 체크박스
                $("#checkboxAll").off('click').on('click', function() {
                    self.toggleAllAgreements($(this).is(':checked'));
                });
                
                // 개별 약관 체크박스들
                $(".checkboxCustomArrow").off('click').on('click', function() {
                    self.updateAllAgreementStatus();
                });
                
                // 회원가입 완료 버튼
                $("#signupDone").off('click').on('click', function() {
                    self.handleSignupSubmit();
                });
                
                console.log('[AUTH] 회원가입 이벤트 리스너 설정 완료 (실시간 중복 체크)');
            } catch (error) {
                console.error('[AUTH] 회원가입 이벤트 리스너 설정 오류:', error);
            }
        },

        /**
         * 전화번호 숫자만 입력 설정
         */
        setupPhoneNumberCheck: function() {
            try {
                $(".numberValue").off('keyup').on("keyup", function(e) {
                    $(this).val($(this).val().replace(/[^0-9]/g, ""));
                });
                console.log('[AUTH] 전화번호 숫자 입력 설정 완료');
            } catch (error) {
                console.error('[AUTH] 전화번호 설정 오류:', error);
            }
        },

        /**
         * 초기 상태 설정
         */
        setupSignupInitialState: function() {
            try {
                $('.passwordInfoBox').hide();
                $('.passwordCheckList li').removeClass('success');
                console.log('[AUTH] 초기 상태 설정 완료 - 비밀번호 조건창 숨김');
            } catch (error) {
                console.error('[AUTH] 초기 상태 설정 오류:', error);
            }
        },

        /**
         * 아이디 입력 처리 (debounce 적용)
         */
        handleUserIdInput: function(value) {
            const self = this;
            
            // 기존 타이머 클리어
            if (this.userIdTimer) {
                clearTimeout(this.userIdTimer);
            }
            
            // 입력값 정리
            value = value ? value.trim() : '';
            
            // 상태 초기화
            this.resetUserIdStatus();
            
            if (!value) {
                // 빈 값일 때는 체크하지 않음
                return;
            }
            
            // 기본 형식 검사 (Zinidata.validation 활용)
            if (!Zinidata.validation.userId(value)) {
                this.setUserIdStatus('error', '아이디는 영문과 숫자 조합으로 4-10자로 입력해주세요.');
                return;
            }
            
            // 500ms 후 중복 체크 실행 (Zinidata.performance.debounce 활용)
            if (Zinidata.performance && Zinidata.performance.debounce) {
                const debouncedCheck = Zinidata.performance.debounce(function() {
                    self.checkUserIdRealtime(value);
                }, 500);
                debouncedCheck();
            } else {
                // 기존 방식 (fallback)
                this.userIdTimer = setTimeout(function() {
                    self.checkUserIdRealtime(value);
                }, 500);
            }
            
            // 체크 중 상태 표시
            this.setUserIdStatus('checking', '확인 중...');
        },

        /**
         * 이메일 입력 처리 (debounce 적용)
         */
        handleEmailInput: function(value) {
            const self = this;
            
            // 기존 타이머 클리어
            if (this.emailTimer) {
                clearTimeout(this.emailTimer);
            }
            
            // 입력값 정리
            value = value ? value.trim() : '';
            
            // 상태 초기화
            this.resetEmailStatus();
            
            if (!value) {
                // 빈 값일 때는 체크하지 않음
                return;
            }
            
            // 기본 형식 검사 (Zinidata.validation 활용)
            if (!Zinidata.validation.email(value)) {
                this.setEmailStatus('error', '올바른 이메일 형식을 입력해주세요.');
                return;
            }
            
            // 500ms 후 중복 체크 실행 (Zinidata.performance.debounce 활용)
            if (Zinidata.performance && Zinidata.performance.debounce) {
                const debouncedCheck = Zinidata.performance.debounce(function() {
                    self.checkEmailRealtime(value);
                }, 500);
                debouncedCheck();
            } else {
                // 기존 방식 (fallback)
                this.emailTimer = setTimeout(function() {
                    self.checkEmailRealtime(value);
                }, 500);
            }
            
            // 체크 중 상태 표시
            this.setEmailStatus('checking', '확인 중...');
        },

        /**
         * 실시간 아이디 중복 체크
         */
        checkUserIdRealtime: function(userId) {
            console.log('[AUTH] 실시간 아이디 중복 체크:', userId);
            const self = this;
            
            Zinidata.api({
                url: '/api/auth/check-userid',
                method: 'POST',
                useToken: false,
                showLoading: false,
                data: {
                    loginId: userId
                },
                success: function(response) {
                    if (response.success === true) {
                        self.setUserIdStatus('success', '사용 가능한 아이디입니다.');
                        $("#userId").data('validated', true);
                    } else {
                        self.setUserIdStatus('error', response.message);
                        $("#userId").data('validated', false);
                    }
                },
                error: function(xhr, status, error) {
                    console.error('[AUTH] 아이디 중복체크 오류:', error);
                    
                    let message = '이미 사용중인 아이디입니다.';
                    if (xhr.responseJSON && xhr.responseJSON.message) {
                        message = xhr.responseJSON.message;
                    }
                    
                    self.setUserIdStatus('error', message);
                    $("#userId").data('validated', false);
                }
            });
        },

        /**
         * 실시간 이메일 중복 체크
         */
        checkEmailRealtime: function(emailAddr) {
            console.log('[AUTH] 실시간 이메일 중복 체크:', emailAddr);
            const self = this;
            
            Zinidata.api({
                url: '/api/auth/check-email',
                method: 'POST',
                useToken: false,
                showLoading: false,
                data: {
                    emailAddr: emailAddr
                },
                success: function(response) {
                    if (response.success === true) {
                        self.setEmailStatus('success', '사용 가능한 이메일입니다.');
                        $("#email").data('validated', true);
                    } else {
                        self.setEmailStatus('error', response.message);
                        $("#email").data('validated', false);
                    }
                },
                error: function(xhr, status, error) {
                    console.error('[AUTH] 이메일 중복체크 오류:', error);
                    
                    let message = '이미 사용중인 이메일입니다.';
                    if (xhr.responseJSON && xhr.responseJSON.message) {
                        message = xhr.responseJSON.message;
                    }
                    
                    self.setEmailStatus('error', message);
                    $("#email").data('validated', false);
                }
            });
        },

        /**
         * 비밀번호 보기/숨기기 토글
         */
        togglePasswordVisibility: function() {
            Zinidata.password.toggleVisibility();
        },

        /**
         * 비밀번호 유효성 검사
         */
        validatePassword: function(password) {
            return Zinidata.password.validate(password);
        },

        /**
         * 비밀번호 조건 검증 함수
         */
        validatePasswordConditions: function(password) {
            return Zinidata.password.validateConditions(password);
        },

        /**
         * 아이디 상태 표시
         */
        setUserIdStatus: function(status, message) {
            const $input = $("#userId");
            const $status = $("#userIdStatus");
            const $message = $("#userIdMessage");
            
            // 기존 스타일 제거
            $input.removeClass('border-red-400 border-green-400 border-gray-400');
            $message.removeClass('text-red-600 text-green-600 text-gray-600').addClass('hidden'); // 항상 숨김
            
            if (status === 'success') {
                $input.addClass('border-green-400');
                $status.empty();
            } else if (status === 'error') {
                $input.addClass('border-red-400');
                $status.empty();
            } else if (status === 'checking') {
                $status.html('<span class="text-gray-600 animate-bounce">...</span>');
            }
        },

        /**
         * 이메일 상태 표시
         */
        setEmailStatus: function(status, message) {
            const $input = $("#email");
            const $status = $("#emailStatus");
            const $message = $("#emailMessage");
            
            // 기존 스타일 제거
            $input.removeClass('border-red-400 border-green-400 border-gray-400');
            $message.removeClass('text-red-600 text-green-600 text-gray-600').addClass('hidden'); // 항상 숨김
            
            if (status === 'success') {
                $input.addClass('border-green-400');
                $status.empty();
            } else if (status === 'error') {
                $input.addClass('border-red-400');
                $status.empty();
            } else if (status === 'checking') {
                $status.html('<span class="text-gray-600 animate-bounce">...</span>');
            }
        },

        /**
         * 아이디 상태 초기화
         */
        resetUserIdStatus: function() {
            const $input = $("#userId");
            const $status = $("#userIdStatus");
            const $message = $("#userIdMessage");
            
            $input.removeClass('border-red-400 border-green-400 border-gray-400');
            $status.empty();
            $message.addClass('hidden').removeClass('text-red-600 text-green-600 text-gray-600');
            $("#userId").data('validated', false);
        },

        /**
         * 이메일 상태 초기화
         */
        resetEmailStatus: function() {
            const $input = $("#email");
            const $status = $("#emailStatus");
            const $message = $("#emailMessage");
            
            $input.removeClass('border-red-400 border-green-400 border-gray-400');
            $status.empty();
            $message.addClass('hidden').removeClass('text-red-600 text-green-600 text-gray-600');
            $("#email").data('validated', false);
        },

        /**
         * 전체 약관 동의 토글
         */
        toggleAllAgreements: function(checked) {
            $(".checkboxCustomArrow").prop('checked', checked);
        },

        /**
         * 전체 약관 동의 상태 업데이트
         */
        updateAllAgreementStatus: function() {
            const inputLength = $('.checkboxCustomArrow').length;
            const checkedLength = $('.checkboxCustomArrow:checked').length;
            
            $('#checkboxAll').prop('checked', inputLength === checkedLength);
        },

        /**
         * 회원가입 제출 처리
         */
        handleSignupSubmit: function() {
            console.log('[AUTH] 회원가입 처리 시작');
            
            // 휴대폰 인증 확인
            if (!Zinidata.cert || !Zinidata.cert.isCertified()) {
                Zinidata.showAlert('휴대폰 인증을 진행해주세요.', 'doneRed');
                $("#phone").focus();
                return;
            }
            
            // 기본 유효성 검사
            if (!this.validateForm()) {
                return;
            }
            
            // API 호출
            this.submitSignup();
        },

        /**
         * 폼 유효성 검사
         */
        validateForm: function() {
            // 기본 필드 검사 (Zinidata.validation.field 활용)
            if (!Zinidata.validation.field($("#userId"), '아이디를 입력해주세요.')) {
                return false;
            }
            
            if (!$("#userId").data('validated')) {
                Zinidata.showAlert('사용 가능한 아이디를 입력해주세요.', 'doneRed');
                $("#userId").focus();
                return false;
            }
            
            if (!Zinidata.validation.field($("#password"), '비밀번호를 입력해주세요.')) {
                return false;
            }
            
            if (!$("#password").data('validated')) {
                Zinidata.showAlert('비밀번호가 유효하지 않습니다. 조건을 확인해주세요.', 'doneRed');
                $("#password").focus();
                return false;
            }
            
            if (!Zinidata.validation.field($("#name"), '이름을 입력해주세요.')) {
                return false;
            }
            
            if (!Zinidata.validation.field($("#email"), '이메일을 입력해주세요.')) {
                return false;
            }
            
            if (!$("#email").data('validated')) {
                Zinidata.showAlert('사용 가능한 이메일을 입력해주세요.', 'doneRed');
                $("#email").focus();
                return false;
            }
            
            if (!$("#privacyCheck").is(':checked')) {
                Zinidata.showAlert('[필수] 개인정보 수집 이용 동의에 체크해주세요.', 'doneRed');
                $("#privacyCheck").focus();
                return false;
            }
            
            if (!$("#privacyCheck02").is(':checked')) {
                Zinidata.showAlert('[필수] 개인정보 수집 이용 동의(두 번째)에 체크해주세요.', 'doneRed');
                $("#privacyCheck02").focus();
                return false;
            }
            
            return true;
        },

        /**
         * 회원가입 API 호출
         */
        submitSignup: function() {
            console.log('[AUTH] 회원가입 API 호출');
            const self = this;
            
            const signupData = {
                loginId: $("#userId").val().trim(),
                password: $("#password").val(),
                memNm: $("#name").val().trim(),
                emailAddr: $("#email").val().trim(),
                mobileNo: $("#phone").val().trim(),
                privacyYn: 'Y',
                prjAuthCd: '1',
                socialLoginType: 'nice',
                prjCd: 'TEST'
            };
            
            console.log('[AUTH] 회원가입 데이터:', signupData);
            
            Zinidata.api({
                url: '/api/auth/signup',
                method: 'POST',
                useToken: false,
                showLoading: true,
                loadingTarget: $("#signupDone"),
                data: signupData,
                success: function(response) {
                    console.log('[AUTH] 회원가입 API 응답:', response);
                    
                    if (response.success === true) {
                        self.handleSignupSuccess(response);
                    } else {
                        self.handleSignupError(response);
                    }
                },
                error: function(xhr, status, error) {
                    console.error('[AUTH] 회원가입 API 오류:', error);
                    
                    if (xhr.responseJSON) {
                        self.handleSignupError(xhr.responseJSON);
                    } else {
                        self.handleSignupError({ message: "네트워크 오류가 발생했습니다. 다시 시도해주세요." });
                    }
                }
            });
        },

        /**
         * 회원가입 성공 처리
         */
        handleSignupSuccess: function(response) {
            console.log('[AUTH] 회원가입 성공:', response);
            
            Zinidata.showAlert('회원가입이 완료되었습니다.', 'doneGreen');
            
            setTimeout(function() {
                window.location.href = '/auth/login?message=' + encodeURIComponent('회원가입이 완료되었습니다. 로그인해주세요.');
            }, 2000);
        },

        /**
         * 회원가입 실패 처리
         */
        handleSignupError: function(response) {
            console.log('[AUTH] 회원가입 실패:', response);
            
            const errorMessage = response.message || '회원가입에 실패했습니다. 관리자에게 문의해주세요.';
            
            // 특정 에러 코드 처리
            if (response.code) {
                if (response.code === '1009') {
                    Zinidata.showAlert(errorMessage, 'doneRed');
                    $("#userId").focus();
                    return;
                } else if (response.code === '1011') {
                    Zinidata.showAlert(errorMessage, 'doneRed');
                    $("#phone").focus();
                    return;
                } else if (response.code === '3001') {
                    // 에러 메시지에 따른 포커스 처리
                    if (errorMessage.includes('아이디')) {
                        $("#userId").focus();
                    } else if (errorMessage.includes('비밀번호')) {
                        $("#password").focus();
                    } else if (errorMessage.includes('이름')) {
                        $("#name").focus();
                    } else if (errorMessage.includes('이메일')) {
                        $("#email").focus();
                    } else if (errorMessage.includes('휴대폰')) {
                        $("#phone").focus();
                    }
                }
            }
            
            Zinidata.showAlert(errorMessage, 'doneRed');
        },

        // ============================== 아이디/비밀번호 찾기 모듈 ==============================
        /**
         * 아이디 찾기 초기화
         */
        findId: function() {
            console.log('[AUTH] 아이디 찾기 모듈 초기화 중...');
            try {
                this.setupFindIdEventListeners();
                this.setupFindIdInitialState();
                console.log('[AUTH] 아이디 찾기 모듈 초기화 완료');
            } catch (error) {
                console.error('[AUTH] 아이디 찾기 초기화 오류:', error);
            }
        },

        /**
         * 아이디 찾기 이벤트 리스너 설정
         */
        setupFindIdEventListeners: function() {
            const self = this;
            
            try {
                // 아이디 찾기 버튼
                $("#findIdBtn").off('click').on('click', function() {
                    self.handleFindIdSubmit();
                });
                
                // 이전 버튼
                $("#goBackBtn").off('click').on('click', function() {
                    window.location.href = '/auth/login';
                });
                
                // 이름 입력 시 에러 메시지 숨김
                $("#name").off('input').on('input', function() {
                    self.hideErrorMessage();
                });
                
                // 휴대폰 번호 입력 시 에러 메시지 숨김
                $("#phone").off('input').on('input', function() {
                    self.hideErrorMessage();
                });
                
                console.log('[AUTH] 아이디 찾기 이벤트 리스너 설정 완료');
            } catch (error) {
                console.error('[AUTH] 아이디 찾기 이벤트 리스너 설정 오류:', error);
            }
        },

        /**
         * 아이디 찾기 초기 상태 설정
         */
        setupFindIdInitialState: function() {
            try {
                // 에러 메시지 숨김
                $("#noAccountMessage").hide();
                console.log('[AUTH] 아이디 찾기 초기 상태 설정 완료');
            } catch (error) {
                console.error('[AUTH] 아이디 찾기 초기 상태 설정 오류:', error);
            }
        },

        /**
         * 아이디 찾기 처리
         */
        handleFindIdSubmit: function() {
            console.log('[AUTH] === 아이디 찾기 처리 시작 ===');
            
            // 휴대폰 인증 확인
            if (!Zinidata.cert || !Zinidata.cert.isCertified()) {
                Zinidata.showAlert('휴대폰 인증을 진행해주세요.', 'doneRed');
                $("#phone").focus();
                return;
            }
            
            // 기본 유효성 검사
            if (!this.validateFindIdForm()) {
                return;
            }
            
            // API 호출
            this.submitFindId();
        },

        /**
         * 아이디 찾기 폼 유효성 검사
         */
        validateFindIdForm: function() {
            const name = $("#name").val().trim();
            const phone = $("#phone").val().trim();
            
            if (!name) {
                Zinidata.showAlert('이름을 입력해주세요.', 'doneRed');
                $("#name").focus();
                return false;
            }
            
            if (!phone) {
                Zinidata.showAlert('휴대폰 번호를 입력해주세요.', 'doneRed');
                $("#phone").focus();
                return false;
            }
            
            if (phone.length !== 11) {
                Zinidata.showAlert('올바른 휴대폰 번호를 입력해주세요.', 'doneRed');
                $("#phone").focus();
                return false;
            }
            
            return true;
        },

        /**
         * 아이디 찾기 API 호출
         */
        submitFindId: function() {
            console.log('[AUTH] === 아이디 찾기 API 호출 ===');
            const self = this;
            
            const findData = {
                memNm: $("#name").val().trim(),
                mobileNo: $("#phone").val().trim()
            };
            
            console.log('[AUTH] 아이디 찾기 데이터:', findData);
            
            Zinidata.api({
                url: '/api/auth/find-id',
                method: 'POST',
                useToken: false,
                showLoading: true,
                loadingTarget: $("#findIdBtn"),
                data: findData,
                success: function(response) {
                    console.log('[AUTH] === 아이디 찾기 API 응답 ===', response);
                    
                    if (response.success === true) {
                        self.handleFindIdSuccess(response.data);
                    } else {
                        self.handleFindIdError(response);
                    }
                },
                error: function(xhr, status, error) {
                    console.error('[AUTH] === 아이디 찾기 API 오류 ===', error);
                    
                    if (xhr.responseJSON) {
                        self.handleFindIdError(xhr.responseJSON);
                    } else {
                        self.handleFindIdError({ message: "네트워크 오류가 발생했습니다. 다시 시도해주세요." });
                    }
                }
            });
        },

        /**
         * 아이디 찾기 성공 처리
         */
        handleFindIdSuccess: function(data) {
            console.log('[AUTH] === 아이디 찾기 성공 ===', data);
            
            // 찾은 아이디 정보를 세션에 저장 (결과 페이지에서 사용)
            sessionStorage.setItem('findIdResult', JSON.stringify({
                maskedLoginId: data.maskedLoginId,
                memNm: data.memNm
            }));
            
            // 결과 페이지로 이동
            window.location.href = '/auth/findId/result';
        },

        /**
         * 아이디 찾기 실패 처리
         */
        handleFindIdError: function(response) {
            console.log('[AUTH] === 아이디 찾기 실패 ===', response);
            
            const errorMessage = response.message || '아이디 찾기에 실패했습니다.';
            
            // 가입 내역이 없는 경우 에러 메시지 표시
            if (response.code === 'USER_NOT_FOUND' || errorMessage.includes('가입한 내역이 없습니다')) {
                this.showErrorMessage();
            } else {
                Zinidata.showAlert(errorMessage, 'doneRed');
            }
        },

        /**
         * 에러 메시지 표시
         */
        showErrorMessage: function() {
            $("#noAccountMessage").removeClass('hidden').show();
        },

        /**
         * 에러 메시지 숨김
         */
        hideErrorMessage: function() {
            $("#noAccountMessage").addClass('hidden').hide();
        },

        // ============================== 아이디 찾기 결과 모듈 ==============================
        /**
         * 아이디 찾기 결과 페이지 초기화
         */
        findIdResult: function() {
            console.log('[AUTH] 아이디 찾기 결과 페이지 초기화 중...');
            try {
                this.setupFindIdResultEventListeners();
                this.loadFindIdResult();
                console.log('[AUTH] 아이디 찾기 결과 페이지 초기화 완료');
            } catch (error) {
                console.error('[AUTH] 아이디 찾기 결과 페이지 초기화 오류:', error);
            }
        },

        /**
         * 아이디 찾기 결과 이벤트 리스너 설정
         */
        setupFindIdResultEventListeners: function() {
            try {
                // 로그인 버튼
                $("#goToLoginBtn").off('click').on('click', function() {
                    window.location.href = '/auth/login';
                });
                
                // 이전 버튼 (아이디 찾기 페이지로)
                $("#goBackToFindBtn").off('click').on('click', function() {
                    window.location.href = '/auth/findId';
                });
                
                console.log('[AUTH] 아이디 찾기 결과 페이지 이벤트 리스너 설정 완료');
            } catch (error) {
                console.error('[AUTH] 아이디 찾기 결과 페이지 이벤트 리스너 설정 오류:', error);
            }
        },

        /**
         * 아이디 찾기 결과 로드
         */
        loadFindIdResult: function() {
            try {
                // sessionStorage에서 결과 데이터 가져오기
                const resultData = sessionStorage.getItem('findIdResult');
                
                if (!resultData) {
                    console.warn('[AUTH] 아이디 찾기 결과 데이터가 없습니다. 아이디 찾기 페이지로 이동합니다.');
                    Zinidata.showAlert('잘못된 접근입니다.', 'doneRed');
                    setTimeout(function() {
                        window.location.href = '/auth/findId';
                    }, 1500);
                    return;
                }
                
                const result = JSON.parse(resultData);
                
                // 찾은 아이디 표시
                $("#foundId").text(result.maskedLoginId);
                $("#foundIdMessage").html('회원님의 아이디는 <strong>' + result.maskedLoginId + '</strong> 입니다.');
                
                // 사용된 결과 데이터 정리
                sessionStorage.removeItem('findIdResult');
                
                console.log('[AUTH] 아이디 찾기 결과 표시 완료:', result.maskedLoginId);
                
            } catch (error) {
                console.error('[AUTH] 아이디 찾기 결과 로드 오류:', error);
                Zinidata.showAlert('결과를 불러오는 중 오류가 발생했습니다.', 'doneRed');
                setTimeout(function() {
                    window.location.href = '/auth/findId';
                }, 1500);
            }
        },

        // ============================== 비밀번호 찾기 모듈 ==============================
        /**
         * 비밀번호 찾기 초기화
         */
        findPassword: function() {
            console.log('[AUTH] 비밀번호 찾기 모듈 초기화');
            this.setupFindPasswordEventListeners();
            this.setupFindPasswordInitialState();
        },

        /**
         * 비밀번호 찾기 이벤트 리스너 설정
         */
        setupFindPasswordEventListeners: function() {
            const self = this;
            
            // 비밀번호 찾기 버튼
            $('#findPasswordBtn').off('click').on('click', function(e) {
                e.preventDefault();
                self.handleFindPasswordSubmit();
            });
        },

        /**
         * 비밀번호 찾기 초기 상태 설정
         */
        setupFindPasswordInitialState: function() {
            // 초기 상태 설정
            console.log('[AUTH] 비밀번호 찾기 초기 상태 설정');
        },

        /**
         * 비밀번호 찾기 처리
         */
        handleFindPasswordSubmit: function() {
            console.log('[AUTH] 비밀번호 찾기 요청');
            
            if (!this.validateFindPasswordForm()) {
                return;
            }
            
            this.submitFindPassword();
        },

        /**
         * 비밀번호 찾기 폼 유효성 검사
         */
        validateFindPasswordForm: function() {
            const userId = $('#userId').val().trim();
            const phone = $('#phone').val().trim();
            
            if (!userId) {
                Zinidata.showAlert('아이디를 입력해주세요.', 'doneRed');
                $('#userId').focus();
                return false;
            }
            
            if (!phone) {
                Zinidata.showAlert('휴대폰 번호를 입력해주세요.', 'doneRed');
                $('#phone').focus();
                return false;
            }
            
            // SMS 인증 완료 여부 확인
            if (!window.certYn || window.certYn !== 'Y') {
                Zinidata.showAlert('휴대폰 인증을 완료해주세요.', 'doneRed');
                return false;
            }
            
            return true;
        },

        /**
         * 비밀번호 찾기 API 호출
         */
        submitFindPassword: function() {
            const self = this;
            const userId = $('#userId').val().trim();
            const phone = $('#phone').val().trim();
            
            console.log('[AUTH] 비밀번호 찾기 API 호출 - userId:', userId, 'phone:', phone);
            
            Zinidata.api({
                url: '/api/auth/find-password',
                method: 'POST',
                useToken: false,
                showLoading: true,
                loadingTarget: $('#findPasswordBtn'),
                data: {
                    loginId: userId,
                    mobileNo: phone
                },
                success: function(response) {
                    console.log('[AUTH] 비밀번호 찾기 성공:', response);
                    self.handleFindPasswordSuccess(response);
                },
                error: function(xhr, status, error) {
                    console.error('[AUTH] 비밀번호 찾기 실패:', xhr, status, error);
                    self.handleFindPasswordError(xhr);
                }
            });
        },

        /**
         * 비밀번호 찾기 성공 처리
         */
        handleFindPasswordSuccess: function(response) {
            console.log('[AUTH] 비밀번호 찾기 성공 처리');
            
            // 성공 알림
            Zinidata.showAlert('인증이 완료되었습니다.\n비밀번호 변경 페이지로 이동합니다.', 'doneGreen', function() {
                // 비밀번호 변경 페이지로 이동
                window.location.href = '/auth/findPasswordResult';
            });
        },

        /**
         * 비밀번호 찾기 실패 처리
         */
        handleFindPasswordError: function(xhr) {
            console.error('[AUTH] 비밀번호 찾기 오류 처리');
            
            let errorMessage = '비밀번호 찾기 중 오류가 발생했습니다.';
            
            if (xhr.responseJSON && xhr.responseJSON.message) {
                errorMessage = xhr.responseJSON.message;
            }
            
            Zinidata.showAlert(errorMessage, 'doneRed');
        },

        // ============================== 비밀번호 변경 모듈 ==============================
        /**
         * 비밀번호 변경 초기화
         */
        changePassword: function() {
            console.log('[AUTH] 비밀번호 변경 모듈 초기화');
            this.setupChangePasswordEventListeners();
            this.setupChangePasswordInitialState();
        },

        /**
         * 비밀번호 변경 이벤트 리스너 설정
         */
        setupChangePasswordEventListeners: function() {
            const self = this;
            
            // 비밀번호 변경 버튼
            $('#changePasswordBtn').off('click').on('click', function(e) {
                e.preventDefault();
                self.handleChangePasswordSubmit();
            });
            
            // 비밀번호 토글 버튼들
            $('#toggleNewPassword').off('click').on('click', function() {
                self.toggleChangePasswordVisibility('newPassword', 'eyeIconNew', 'eyeSlashIconNew');
            });
            
            $('#toggleConfirmPassword').off('click').on('click', function() {
                self.toggleChangePasswordVisibility('confirmPassword', 'eyeIconConfirm', 'eyeSlashIconConfirm');
            });
            
            // 새 비밀번호 입력 시 유효성 검사
            $('#newPassword').off('focus').on('focus', function() {
                $('.passwordInfoBox').removeClass('hidden');
            });
            
            $('#newPassword').off('blur').on('blur', function() {
                const password = $(this).val();
                if (password.length === 0) {
                    $('.passwordInfoBox').addClass('hidden');
                }
            });
            
            $('#newPassword').off('input').on('input', function() {
                self.validateChangePasswordConditions($(this).val());
            });
            
            // 비밀번호 확인 입력 시 일치 여부 확인
            $('#confirmPassword').off('input').on('input', function() {
                self.checkPasswordMatch();
            });
            
            // 로그인으로 이동 버튼 (비밀번호 변경 페이지용)
            $('#goToLoginFromPasswordBtn').off('click').on('click', function() {
                window.location.href = '/auth/login';
            });
        },

        /**
         * 비밀번호 변경 초기 상태 설정
         */
        setupChangePasswordInitialState: function() {
            console.log('[AUTH] 비밀번호 변경 초기 상태 설정');
            $('.passwordInfoBox').addClass('hidden');
        },

        /**
         * 비밀번호 변경 처리
         */
        handleChangePasswordSubmit: function() {
            console.log('[AUTH] 비밀번호 변경 요청');
            
            if (!this.validateChangePasswordForm()) {
                return;
            }
            
            this.submitChangePassword();
        },

        /**
         * 비밀번호 변경 폼 유효성 검사
         */
        validateChangePasswordForm: function() {
            const newPassword = $('#newPassword').val();
            const confirmPassword = $('#confirmPassword').val();
            
            if (!newPassword) {
                Zinidata.showAlert('새 비밀번호를 입력해주세요.', 'doneRed');
                $('#newPassword').focus();
                return false;
            }
            
            if (!this.validateChangePasswordConditions(newPassword)) {
                Zinidata.showAlert('비밀번호 조건을 만족해주세요.', 'doneRed');
                $('#newPassword').focus();
                return false;
            }
            
            if (!confirmPassword) {
                Zinidata.showAlert('비밀번호 확인을 입력해주세요.', 'doneRed');
                $('#confirmPassword').focus();
                return false;
            }
            
            if (newPassword !== confirmPassword) {
                Zinidata.showAlert('비밀번호가 일치하지 않습니다.', 'doneRed');
                $('#confirmPassword').focus();
                return false;
            }
            
            return true;
        },

        /**
         * 비밀번호 변경용 비밀번호 조건 검증
         */
        validateChangePasswordConditions: function(password) {
            return Zinidata.password.validateConditions(password);
        },

        /**
         * 비밀번호 일치 확인
         */
        checkPasswordMatch: function() {
            const newPassword = $('#newPassword').val();
            const confirmPassword = $('#confirmPassword').val();
            const $message = $('#passwordMatchMessage');
            
            if (confirmPassword.length === 0) {
                $message.addClass('hidden');
                return;
            }
            
            if (newPassword === confirmPassword) {
                $message.removeClass('hidden').text('비밀번호가 일치합니다.').removeClass('text-red-500').addClass('text-green-500');
            } else {
                $message.removeClass('hidden').text('비밀번호가 일치하지 않습니다.').removeClass('text-green-500').addClass('text-red-500');
            }
        },

        /**
         * 비밀번호 변경용 비밀번호 보기/숨기기 토글
         */
        toggleChangePasswordVisibility: function(inputId, eyeIconId, eyeSlashIconId) {
            const $input = $('#' + inputId);
            const $eyeIcon = $('#' + eyeIconId);
            const $eyeSlashIcon = $('#' + eyeSlashIconId);
            
            if ($input.attr('type') === 'password') {
                $input.attr('type', 'text');
                $eyeIcon.addClass('hidden');
                $eyeSlashIcon.removeClass('hidden');
            } else {
                $input.attr('type', 'password');
                $eyeIcon.removeClass('hidden');
                $eyeSlashIcon.addClass('hidden');
            }
        },

        /**
         * 비밀번호 변경 API 호출
         */
        submitChangePassword: function() {
            const self = this;
            const newPassword = $('#newPassword').val();
            
            console.log('[AUTH] 비밀번호 변경 API 호출');
            
            Zinidata.api({
                url: '/api/auth/change-password',
                method: 'POST',
                useToken: false,
                showLoading: true,
                loadingTarget: $('#changePasswordBtn'),
                data: {
                    newPassword: newPassword
                },
                success: function(response) {
                    console.log('[AUTH] 비밀번호 변경 성공:', response);
                    self.handleChangePasswordSuccess(response);
                },
                error: function(xhr, status, error) {
                    console.error('[AUTH] 비밀번호 변경 실패:', xhr, status, error);
                    self.handleChangePasswordError(xhr);
                }
            });
        },

        /**
         * 비밀번호 변경 성공 처리
         */
        handleChangePasswordSuccess: function(response) {
            console.log('[AUTH] 비밀번호 변경 성공 처리');
            
            // 성공 알림
            Zinidata.showAlert('비밀번호가 성공적으로 변경되었습니다.\n로그인 페이지로 이동합니다.', 'doneGreen', function() {
                // 로그인 페이지로 이동
                window.location.href = '/auth/login';
            });
        },

        /**
         * 비밀번호 변경 실패 처리
         */
        handleChangePasswordError: function(xhr) {
            console.error('[AUTH] 비밀번호 변경 오류 처리');
            
            let errorMessage = '비밀번호 변경 중 오류가 발생했습니다.';
            
            if (xhr.responseJSON && xhr.responseJSON.message) {
                errorMessage = xhr.responseJSON.message;
            }
            
            Zinidata.showAlert(errorMessage, 'doneRed');
        },

        // ============================== 로그아웃 함수 ==============================
        /**
         * 로그아웃 실행
         */
        logout: function() {
            console.log('[AUTH] === 로그아웃 처리 시작 ===');
            
            const self = this;
            
            // 서버 로그아웃 API 호출
            this.submitLogout();
        },

        // ============================== OAuth 함수 ==============================
        /**
         * 카카오 OAuth 초기화
         */
        oauth: {
            /**
             * 카카오 OAuth 초기화
             */
            init: function() {
                console.log('[AUTH] 카카오 OAuth 모듈 초기화');
                this.setupEventListeners();
            },
            
            /**
             * 카카오 OAuth 이벤트 리스너 설정
             */
            setupEventListeners: function() {
                const self = this;
                
                try {
                    // 카카오 로그인 버튼 클릭 이벤트
                    $('#kakaoLoginBtn').off('click').on('click', function(e) {
                        e.preventDefault();
                        self.startLogin();
                    });
                    
                    // 카카오 계정 연동 버튼 (마이페이지용)
                    $('#kakaoConnectBtn').off('click').on('click', function(e) {
                        e.preventDefault();
                        self.startConnect();
                    });
                    
                    // OAuth 취소 버튼
                    $('.kakao-cancel-btn').off('click').on('click', function(e) {
                        e.preventDefault();
                        self.cancelProcess();
                    });
                    
                    console.log('[AUTH] 카카오 OAuth 이벤트 리스너 설정 완료');
                } catch (error) {
                    console.error('[AUTH] 카카오 OAuth 이벤트 리스너 설정 오류:', error);
                }
            },
            
            /**
             * 카카오 로그인 시작
             */
            startLogin: function() {
                console.log('[AUTH] === 카카오 로그인 시작 ===');
                
                try {
                    // 로딩 상태 표시
                    this.showLoading('카카오 로그인 중...');
                    
                    // 현재 페이지의 redirect 파라미터 확인
                    const urlParams = new URLSearchParams(window.location.search);
                    const redirectUrl = urlParams.get('returnUrl') || urlParams.get('redirect');
                    
                    // 카카오 인증 페이지로 리다이렉트 (redirect 파라미터 포함)
                    let kakaoUrl = '/api/oauth/kakao';
                    if (redirectUrl && redirectUrl.trim() !== '') {
                        kakaoUrl += '?redirect=' + encodeURIComponent(redirectUrl);
                        console.log('[AUTH] 카카오 로그인 리다이렉트 URL 포함:', redirectUrl);
                    }
                    
                    window.location.href = kakaoUrl;
                    
                } catch (error) {
                    console.error('[AUTH] 카카오 로그인 시작 실패:', error);
                    this.hideLoading();
                    Zinidata.showAlert('카카오 로그인 중 오류가 발생했습니다.', 'doneRed');
                }
            },
            
            /**
             * 카카오 계정 연동 시작 (마이페이지)
             */
            startConnect: function() {
                console.log('[AUTH] === 카카오 계정 연동 시작 ===');
                
                try {
                    // 로그인 상태 확인
                    if (!Zinidata.auth.session.isLoggedIn()) {
                        Zinidata.showAlert('로그인 후 이용해주세요.', 'doneRed');
                        return;
                    }
                    
                    // 로딩 상태 표시
                    this.showLoading('카카오 계정 연동 중...');
                    
                    // 카카오 연동 페이지로 리다이렉트
                    window.location.href = '/api/oauth/kakao/connect';
                    
                } catch (error) {
                    console.error('[AUTH] 카카오 계정 연동 시작 실패:', error);
                    this.hideLoading();
                    Zinidata.showAlert('카카오 계정 연동 중 오류가 발생했습니다.', 'doneRed');
                }
            },
            
            /**
             * OAuth 프로세스 취소
             */
            cancelProcess: function() {
                console.log('[AUTH] === 카카오 OAuth 프로세스 취소 ===');
                
                if (confirm('카카오 로그인을 취소하시겠습니까?')) {
                    window.location.href = '/api/oauth/cancel-oauth';
                }
            },
            
            /**
             * 로딩 상태 표시
             */
            showLoading: function(message) {
                const $btn = $('#kakaoLoginBtn, #kakaoConnectBtn');
                $btn.prop('disabled', true).find('span:last').text(message || '처리중');
            },
            
            /**
             * 로딩 상태 해제
             */
            hideLoading: function() {
                const $loginBtn = $('#kakaoLoginBtn');
                const $connectBtn = $('#kakaoConnectBtn');
                
                $loginBtn.prop('disabled', false).find('span:last').text('카카오로 간편하게 시작하기');
                $connectBtn.prop('disabled', false).find('span:last').text('카카오 계정 연동');
            },
            
            /**
             * 카카오 로그인 완료 후 처리
             */
            handleLoginComplete: function(userData) {
                console.log('[AUTH] === 카카오 로그인 완료 ===', userData);
                
                // 세션 정보 업데이트
                Zinidata.auth.session.saveUserInfo(userData);
                
                // 헤더 상태 업데이트
                if (typeof window.updateHeaderLoginStatus === 'function') {
                    window.updateHeaderLoginStatus();
                }
                
                // 메인 페이지로 리다이렉트
                window.location.href = userData.redirectUrl || '/';
            }
        },

        /**
         * 로그아웃 API 호출
         */
        submitLogout: function() {
            console.log('[AUTH] === 로그아웃 API 호출 ===');
            const self = this;
            
            Zinidata.api({
                url: '/api/auth/logout',
                method: 'POST',
                useToken: false,
                showLoading: false,
                data: { logoutType: 'user' }, // 사용자 직접 로그아웃
                success: function(response) {
                    console.log('[AUTH] === 서버 로그아웃 성공 ===', response);
                    self.handleLogoutSuccess(response);
                },
                error: function(xhr, status, error) {
                    console.error('[AUTH] === 서버 로그아웃 오류 ===', error);
                    // 서버 오류가 있어도 클라이언트 로그아웃은 진행
                    self.handleLogoutError(xhr);
                }
            });
        },

        /**
         * 로그아웃 성공 처리
         */
        handleLogoutSuccess: function(response) {
            console.log('[AUTH] === 로그아웃 성공 처리 ===', response);
            this.handleLogoutComplete();
        },

        /**
         * 로그아웃 실패 처리
         */
        handleLogoutError: function(xhr) {
            console.log('[AUTH] === 로그아웃 실패 처리 ===', xhr);
            // 서버 오류가 있어도 클라이언트 로그아웃은 진행
            this.handleLogoutComplete();
        },

        /**
         * 로그아웃 완료 처리
         */
        handleLogoutComplete: function() {
            // 세션 정보 정리
            Zinidata.auth.session.clear();
            
            // 헤더 상태 업데이트
            if (typeof window.updateHeaderLoginStatus === 'function') {
                window.updateHeaderLoginStatus();
            }
            
            // 성공 메시지
            Zinidata.showAlert('로그아웃되었습니다.', 'doneGreen');
            
            setTimeout(function() {
                window.location.href = '/auth/login';
            }, 1000);
        },

        // ============================== 세션 관리 모듈 ==============================
        session: {
            
            // 세션 만료 타이머 관련 변수
            timer: null,
            timeLeft: 0,
            timeout: 1800, // 30분 (1800초)

            /**
             * 사용자 정보 저장 (세션 기반)
             */
            saveUserInfo: function(userInfo) {
                sessionStorage.setItem('userInfo', JSON.stringify(userInfo));
                
                // 전역 변수에도 설정 (기존 코드 호환성)
                if (typeof window !== 'undefined') {
                    window.strMemNo = userInfo.memNo;
                    window.strloginId = userInfo.loginId;
                    window.strMemNm = userInfo.memNm;
                    window.strMobileNo = userInfo.mobileNo;
                }
                
                console.log('[AUTH] === 사용자 정보 저장 완료 ===', userInfo.loginId);
            },

            /**
             * 사용자 정보 가져오기 (동기 방식)
             * sessionStorage에 캐시된 정보를 먼저 확인하고, 없으면 null 반환
             * 최신 정보가 필요하면 getUserInfoAsync() 사용 권장
             * @returns {Object|null} 사용자 정보 또는 null
             */
            getUserInfo: function() {
                try {
                    const userInfoStr = sessionStorage.getItem('userInfo');
                    if (userInfoStr) {
                        const cachedInfo = JSON.parse(userInfoStr);
                        // 캐시된 정보가 있고 유효한 경우 반환
                        if (cachedInfo && cachedInfo.memNo) {
                            return cachedInfo;
                        }
                    }
                } catch (error) {
                    console.warn('[AUTH] sessionStorage 조회 오류:', error);
                }
                return null;
            },

            /**
             * 사용자 정보 비동기 조회
             * 서버 세션에서 최신 사용자 정보를 조회합니다.
             * @returns {Promise<Object|null>} 사용자 정보 또는 null
             */
            getUserInfoAsync: function() {
                const self = this; // 컨텍스트 저장
                return new Promise((resolve, reject) => {
                    $.ajax({
                        url: '/api/auth/session',
                        type: 'GET',
                        dataType: 'json',
                        async: false,
                        success: (response) => {
                            if (response && response.success && response.data) {
                                const userInfo = response.data;
                                // sessionStorage에 캐시 저장
                                self.saveUserInfo(userInfo);
                                
                                console.log('[AUTH] 서버에서 사용자 정보 조회 완료:', userInfo.loginId);
                                resolve(userInfo);
                            } else {
                                console.warn('[AUTH] 세션 정보가 없습니다.');
                                self.clear();
                                resolve(null);
                            }
                        },
                        error: (xhr, status, error) => {
                            console.error('[AUTH] 사용자 정보 조회 오류:', error);
                            if (xhr.status === 401) {
                                // 세션 만료
                                self.clear();
                                resolve(null);
                            } else {
                                reject(error);
                            }
                        }
                    });
                });
            },

            /**
             * 서버에서 사용자 정보 새로고침 (자동 호출)
             * 페이지 로드 시 자동으로 호출되어 최신 세션 정보를 가져옵니다.
             */
            refreshUserInfo: function() {
                // 로그인 상태가 아닌 경우 스킵
                if (!this.isLoggedIn()) {
                    // 로그인 상태가 아니어도 서버에서 확인 (세션 만료 체크)
                    this.getUserInfoAsync().catch(err => {
                        console.debug('[AUTH] 세션 정보 새로고침 실패 (로그인 안 됨):', err);
                    });
                    return;
                }
                
                // 서버에서 최신 정보 가져오기
                this.getUserInfoAsync().then(userInfo => {
                    if (userInfo) {
                        console.log('[AUTH] 세션 정보 새로고침 완료');
                    }
                }).catch(err => {
                    console.error('[AUTH] 세션 정보 새로고침 실패:', err);
                });
            },

            /**
             * 로그인 상태 확인
             */
            isLoggedIn: function() {
                const userInfo = this.getUserInfo();
                return userInfo && userInfo.memNo && userInfo.sessionId;
            },

            /**
             * 세션 정보 정리
             */
            clear: function() {
                sessionStorage.removeItem('userInfo');
                
                // 전역 변수 정리
                if (typeof window !== 'undefined') {
                    window.strMemNo = '';
                    window.strloginId = '';
                    window.strMemNm = '';
                }
                
                console.log('[AUTH] === 세션 정보 정리 완료 ===');
            },

            /**
             * 세션 만료 타이머 초기화
             */
            initExpirationTimer: function(timeoutSeconds) {
                // ✅ 표준: 로그인 상태만 확인, DOM 의존성 제거
                if (!this.isLoggedIn()) {
                    console.log('[AUTH] 로그인 상태가 아니므로 세션 타이머 시작하지 않음');
                    return;
                }
                
                this.timeout = timeoutSeconds || 60;
                this.startTimer();
                this.setupUserActivityListeners();
                
                console.log('[AUTH] 세션 만료 타이머 초기화 완료 - ' + this.timeout + '초');
            },

            /**
             * 타이머 시작
             */
            startTimer: function() {
                const self = this;
                
                clearInterval(this.timer);
                this.timeLeft = this.timeout;
                this.updateTimerDisplay();
                
                this.timer = setInterval(function() {
                    self.timeLeft--;
                    self.updateTimerDisplay();
                    
                    if (self.timeLeft <= 0) {
                        clearInterval(self.timer);
                        self.handleExpiration();
                    }
                }, 1000);
            },

            /**
             * 타이머 표시 업데이트
             */
            updateTimerDisplay: function() {
                // ✅ 표준: DOM 표시 없이 로그만 출력 (10초 알림 전용)
                const minutes = Math.floor(this.timeLeft / 60);
                const seconds = this.timeLeft % 60;
                const display = (minutes < 10 ? '0' : '') + minutes + ':' + (seconds < 10 ? '0' : '') + seconds;
                
                // 진행상황 로그 (5초 간격으로 출력)
                if (this.timeLeft % 60 === 0 || this.timeLeft <= 3) {
                    // console.log('[AUTH] 세션 타이머: ' + display + ' 남음');
                }
                
                // DOM 요소가 있으면 표시 (있을 때만)
                if ($('#timerDisplay').length) {
                    $('#timerDisplay').text(display);
                }
            },

            /**
             * 사용자 액션 리스너 설정
             */
            setupUserActivityListeners: function() {
                const self = this;
                
                function resetTimer() {
                    // console.log('사용자 액션 감지 - 타이머 리셋');
                    self.startTimer();
                }
                
                // 사용자 액션 이벤트 리스너
                $(document).on('mousedown mousemove keypress scroll touchstart', resetTimer);
                $('input, textarea, select').on('input change focus', resetTimer);
                $('button, a').on('click', resetTimer);
            },

            /**
             * 세션 만료 처리
             */
            handleExpiration: function() {
                const self = this;
                
                console.log('[AUTH] === 클라이언트 세션 타이머 만료 감지 ===');
                console.log('[AUTH] === 세션 타임아웃 감지 - 자동 로그아웃 처리 ===');
                
                // ✅ 표준: 프로젝트 표준 알림 함수 사용
                Zinidata.showAlert('세션이 만료되었습니다. 다시 로그인해주세요.', 'doneRed', function() {
                    // ✅ 표준: 클라이언트 정리 및 리다이렉트
                    self.clear();
                    window.location.href = '/auth/login';
                });
            }
        },

        // ============================== 인증 유틸리티 모듈 ==============================
        utils: {
            /**
             * 인증 필요 페이지 접근 제어
             */
            requireAuth: function() {
                if (!Zinidata.auth.session.isLoggedIn()) {
                    Zinidata.showAlert('로그인이 필요합니다.', 'doneRed');
                    setTimeout(function() {
                        const currentUrl = encodeURIComponent(window.location.href);
                        window.location.href = '/auth/login?returnUrl=' + currentUrl;
                    }, 1000);
                    return false;
                }
                return true;
            },

            /**
             * 현재 사용자 정보 표시
             */
            displayUserInfo: function() {
                Zinidata.auth.session.getUserInfoAsync();
                const userInfo = Zinidata.auth.session.getUserInfo();
                if (userInfo) {
                    console.log('[AUTH] === 현재 로그인 사용자 ===');
                    console.log('[AUTH] 회원번호:', userInfo.memNo);
                    console.log('[AUTH] 로그인ID:', userInfo.loginId);
                    console.log('[AUTH] 이름:', userInfo.memNm);
                    console.log('[AUTH] 세션ID:', userInfo.sessionId);
                    $(".memNm").text(userInfo.memNm);
                    $("#phone").val(userInfo.mobileNo);

                    return userInfo;
                } else {
                    console.log('[AUTH] === 로그인되지 않음 ===');
                    return null;
                }
            },

            /**
             * 세션 체크 (Promise 반환)
             * @returns {Promise<boolean>} 세션 유효 여부
             */
            /* checkSession: function() {
                console.log('[AUTH] === 세션 체크 시작 ===');
                
                return new Promise((resolve, reject) => {
                    Zinidata.api({
                        url: '/api/auth/session',
                        method: 'GET',
                        useToken: false,
                        showLoading: false,
                        success: function(response) {
                            console.log('[AUTH] === 세션 체크 성공 ===', response);
                            if (response.success === true && response.data.valid === true) {
                                // 세션 유효 - 사용자 정보 업데이트
                                Zinidata.auth.session.saveUserInfo({
                                    memNo: response.data.memNo,
                                    loginId: response.data.loginId,
                                    memNm: response.data.memNm,
                                    mobileNo: response.data.mobileNo,
                                    authCd: response.data.authCd,
                                    emailAddr: response.data.emailAddr,
                                    sessionId: response.data.sessionId
                                });
                                resolve(true);
                            } else {
                                // 세션 무효
                                Zinidata.auth.session.clear();
                                resolve(false);
                            }
                        },
                        error: function(xhr, status, error) {
                            console.log('[AUTH] === 세션 체크 실패 ===', error);
                            // 세션 체크 실패 시 세션 정리
                            Zinidata.auth.session.clear();
                            resolve(false);
                        }
                    });
                });
            } */
        },

        // ============================== 자동 초기화 ==============================
        /**
         * 자동 초기화 (페이지 경로 기반)
         */
        autoInit: function() {
            const path = window.location.pathname;
            
            // 페이지 로드 시 서버에서 세션 정보 자동 조회 (모든 페이지)
            this.session.refreshUserInfo();

            // 페이지별 초기화
            if (path.includes('/login')) {
                console.log('[AUTH] === 로그인 페이지 초기화 ===');
                Zinidata.auth.login();
                this.oauth.init(); // OAuth 로그인 버튼 활성화
            } else if (path.includes('/signup')) {
                console.log('[AUTH] === 회원가입 페이지 초기화 ===');
                this.signup();
            } else if (path.includes('/findId/result')) {
                console.log('[AUTH] === 아이디 찾기 결과 페이지 초기화 ===');
                this.findIdResult();
            } else if (path.includes('/findId')) {
                console.log('[AUTH] === 아이디 찾기 페이지 초기화 ===');
                this.findId();
            } else if (path.includes('/findPassword') && !path.includes('/result')) {
                console.log('[AUTH] === 비밀번호 찾기 페이지 초기화 ===');
                this.findPassword();
            } else if (path.includes('/findPasswordResult')) {
                console.log('[AUTH] === 비밀번호 변경 페이지 초기화 ===');
                this.changePassword();
            } else if (path.includes('/mypage') || path.includes('/profile')) {
                console.log('[AUTH] === 마이페이지 초기화 ===');
                this.oauth.init(); // OAuth 연동 버튼 활성화
            }
            
            // 현재 로그인 상태 표시 (sessionStorage 기반)
            this.utils.displayUserInfo();
            
            // 로그인된 사용자의 경우 세션 타이머 자동 초기화
            if (this.session.isLoggedIn()) {
                // ✅ 표준: 세션 타임아웃 알림을 위한 클라이언트 타이머 활성화
                this.session.initExpirationTimer(1800); // 30분 (1800초)
                console.log('[AUTH] 세션 타임아웃 알림 타이머 시작: 30분 (1800초)');
            }
        },

        // ============================== 로그인 타입 감지 (Core에서 이동) ==============================
        /**
         * 로그인 타입 감지 (Spring Security + Thymeleaf 표준 방식)
         * @returns {string} 로그인 타입 ('NORMAL', 'KAKAO', 'NAVER', 'GOOGLE')
         */
        getLoginType: function() {
            // 1. 렌더링된 HTML 구조로 로그인 여부 확인 (Spring Security 태그는 렌더링 후 제거됨)
            const $userBoxWrap = $('.userBoxWrap');
            const $loginBeforeBtn = $('.py-1-5.px-2.rounded-sm.bg-blue-500\\/6'); // 로그인 전 버튼
            
            // 로그인하지 않은 경우
            if ($userBoxWrap.length === 0 || $loginBeforeBtn.length > 0) {
                return null;
            }
            
            // 2. 서버에서 렌더링된 세션 정보 확인 (표준 방식)
            const loginTypeElement = document.querySelector('meta[name="login-type"]');
            if (loginTypeElement) {
                return loginTypeElement.getAttribute('content');
            }
            
            // 3. Spring Security principal에서 확인 (백업 방식)
            const principalElement = document.querySelector('[sec\\:authentication="principal"]');
            if (principalElement) {
                // 카카오 로그인인지 확인 (kakaoId 존재 여부)
                const kakaoIdElement = document.querySelector('[data-kakao-id]');
                if (kakaoIdElement && kakaoIdElement.getAttribute('data-kakao-id')) {
                    return 'KAKAO';
                }
            }
            
            // 4. 기본값 (일반 로그인으로 가정)
            return 'NORMAL';
        },

        /**
         * 로그인 여부 확인 (렌더링된 HTML 구조 기반)
         * @returns {boolean} 로그인 여부
         */
        isLoggedIn: function() {
            // 렌더링된 HTML 구조로 로그인 여부 확인
            const $userBoxWrap = $('.userBoxWrap');
            const $loginBeforeBtn = $('.py-1-5.px-2.rounded-sm.bg-blue-500\\/6'); // 로그인 전 버튼
            return $userBoxWrap.length > 0 && $loginBeforeBtn.length === 0;
        },

        /**
         * 로그인 상태 정보 조회
         * @returns {Object} 로그인 상태 정보
         */
        getLoginInfo: function() {
            return {
                isLoggedIn: this.isLoggedIn(),
                loginType: this.getLoginType(),
                timestamp: Date.now()
            };
        },

        // ============================== GPS 기능 ==============================
        gps: {
            // 전역 GPS 좌표 변수
            centerX: null,
            centerY: null,
            
            /**
             * GPS 좌표 획득
             * @param {Function} successCallback 성공 콜백
             * @param {Function} errorCallback 실패 콜백
             * @param {Object} options GPS 옵션
             */
            getCurrentPosition: function(successCallback, errorCallback, options) {
                const defaultOptions = {
                    enableHighAccuracy: true,
                    timeout: 10000,
                    maximumAge: 300000 // 5분
                };
                
                const gpsOptions = Object.assign({}, defaultOptions, options || {});
                
                if (navigator.geolocation) {
                    navigator.geolocation.getCurrentPosition(
                        function(position) {
                            const centerX = position.coords.longitude; // 경도
                            const centerY = position.coords.latitude;  // 위도
                            
                            // 전역 변수에 저장
                            Zinidata.auth.gps.centerX = centerX;
                            Zinidata.auth.gps.centerY = centerY;
                            
                            console.log('[AUTH] GPS 좌표 획득 성공:', { centerX, centerY });
                            
                            if (typeof successCallback === 'function') {
                                successCallback(centerX, centerY);
                            }
                        },
                        function(error) {
                            console.error('[AUTH] GPS 위치 정보를 가져올 수 없습니다:', error.message);
                            
                            // 기본 좌표 설정 (서울 시청)
                            Zinidata.auth.gps.centerX = 126.9780;
                            Zinidata.auth.gps.centerY = 37.5665;
                            
                            console.log('[AUTH] 기본 좌표 사용:', { 
                                centerX: Zinidata.auth.gps.centerX, 
                                centerY: Zinidata.auth.gps.centerY 
                            });
                            
                            if (typeof errorCallback === 'function') {
                                errorCallback(error);
                            }
                        },
                        gpsOptions
                    );
                } else {
                    console.error('[AUTH] 이 브라우저는 GPS를 지원하지 않습니다.');
                    
                    // 기본 좌표 설정 (서울 시청)
                    Zinidata.auth.gps.centerX = 126.9780;
                    Zinidata.auth.gps.centerY = 37.5665;
                    
                    if (typeof errorCallback === 'function') {
                        errorCallback(new Error('GPS를 지원하지 않는 브라우저입니다.'));
                    }
                }
            },
            
            /**
             * 현재 GPS 좌표 반환
             * @returns {Object} GPS 좌표 정보
             */
            getCurrentCoordinates: function() {
                return {
                    centerX: this.centerX,
                    centerY: this.centerY,
                    hasGps: this.centerX !== null && this.centerY !== null
                };
            },
            
            /**
             * GPS 좌표 초기화
             */
            clear: function() {
                this.centerX = null;
                this.centerY = null;
                console.log('[AUTH] GPS 좌표 초기화 완료');
            }
        }
    };

    // ============================== 초기화 ==============================
    function initializeAuth() {
        // ============================== 전역 노출 제거 ==============================
        // 모든 함수는 Zinidata 네임스페이스를 통해 접근
        // 예: Zinidata.auth.login(), Zinidata.auth.signup.submit()
        
        // 개발 환경에서만 디버깅 함수 노출
        if (Zinidata.config && Zinidata.config.debug) {
            window.ZinidataDebug = window.ZinidataDebug || {};
            window.ZinidataDebug.auth = Zinidata.auth;
        }
        
        // ============================== 자동 초기화 실행 ==============================
        // 페이지 경로에 따라 해당 모듈 자동 초기화
        Zinidata.auth.autoInit();
    }

    // 쿠키에서 사용자 정보 불러오기
    function loadSavedInfo() {
        const loginId = getCookie('saved_loginId');
        const password = getCookie('saved_password');
        const phoneNumber = getCookie('saved_phoneNumber');
        
        if (password && loginId && phoneNumber) {
            $('#loginId').val(loginId);
            $('#password').val(password);
            $('#phone').val(phoneNumber);
            $('#saveInfo').prop('checked', true);
        }
    }
    
    // 사용자 정보를 쿠키에 저장
    function saveUserInfo(loginId, password, phoneNumber) {
        setCookie('saved_loginId', loginId, 30);
        setCookie('saved_password', password, 30); // 30일
        setCookie('saved_phoneNumber', phoneNumber, 30);
    }
    
    // 저장된 정보 쿠키 삭제
    function clearSavedInfo() {
        deleteCookie('saved_loginId');
        deleteCookie('saved_password');
        deleteCookie('saved_phoneNumber');
    }
    
    // 쿠키 설정 함수
    function setCookie(name, value, days) {
        const expires = new Date();
        expires.setTime(expires.getTime() + (days * 24 * 60 * 60 * 1000));
        document.cookie = name + '=' + encodeURIComponent(value) + ';expires=' + expires.toUTCString() + ';path=/';
    }
    
    // 쿠키 가져오기 함수
    function getCookie(name) {
        const nameEQ = name + '=';
        const ca = document.cookie.split(';');
        for (let i = 0; i < ca.length; i++) {
            let c = ca[i];
            while (c.charAt(0) === ' ') c = c.substring(1, c.length);
            if (c.indexOf(nameEQ) === 0) return decodeURIComponent(c.substring(nameEQ.length, c.length));
        }
        return null;
    }
    
    // 쿠키 삭제 함수
    function deleteCookie(name) {
        document.cookie = name + '=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
    }

    initializeAuth();
    
    loadSavedInfo();
    
});
