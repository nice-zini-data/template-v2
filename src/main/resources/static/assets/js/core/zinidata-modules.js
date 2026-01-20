/**
 * ============================================
 * 지니데이타 비즈니스 모듈 (Business Modules)
 * ============================================
 * 
 * 🎯 비즈니스 책임
 * ✅ 폼 처리: form.*
 * ✅ 검증: validation.*
 * ✅ 스토리지: storage.*
 * ✅ DOM 조작: dom.*
 * ✅ 보안: security.*
 * ✅ 이벤트 처리: events.*
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 * @refactored 2024.12
 */

$(document).ready(function() {
    // ============================== 모듈 의존성 체크 ==============================
    if (typeof Zinidata === 'undefined' || !Zinidata.device) {
        console.error('[MODULES] Core 모듈이 먼저 로드되어야 합니다.');
        return;
    }

    // ============================== 폼 처리 모듈 ==============================
    Zinidata.form = {
        /**
         * 폼 데이터를 JSON으로 변환
         * @param {jQuery|string|Element} form - 폼 요소
         * @returns {Object} JSON 객체
         */
        toJson: function(form) {
            const $form = $(form);
            const json = {};

            // name 속성이 있는 요소들 먼저 처리
            const formData = $form.serializeArray();
            $.each(formData, function(i, field) {
                json[field.name] = field.value;
            });

            // name 속성이 없는 요소들은 id를 기준으로 처리
            const $elements = $form.find('input, select, textarea');
            $elements.each(function() {
                const $element = $(this);
                const name = $element.attr('name');
                const id = $element.attr('id');

                // name이 없고 id가 있는 경우에만 처리
                if (!name && id) {
                    json[id] = $element.val();
                }
            });
            
            return json;
        },

        toFormData: function(form) {
            // TODO: 구현 예정
        },

        setData: function(form, data) {
            // TODO: 구현 예정
        },

        reset: function(form) {
            // TODO: 구현 예정
        },

        validate: function(form, rules) {
            // TODO: 구현 예정
        }
    };

    // ============================== 검증 모듈 ==============================
    Zinidata.validation = {
        /**
         * 이메일 형식 검증
         * @param {string} email - 검증할 이메일 주소
         * @returns {boolean} 유효한 이메일 여부
         */
        email: function(email) {
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            return emailRegex.test(email);
        },

        /**
         * 전화번호 형식 검증
         * @param {string} phone - 검증할 전화번호
         * @returns {boolean} 유효한 전화번호 여부
         */
        phone: function(phone) {
            // 기존 패턴 (010, 011, 016, 017, 018, 019)
            const basicPattern = /^01[016789]-?\d{3,4}-?\d{4}$/;
            const cleanPhone = phone.replace(/[^0-9]/g, '');
            
            // 기본 패턴 체크
            if (basicPattern.test(cleanPhone)) {
                return true;
            }
            
            // 국제번호 패턴 체크 (+82-10, +82-11, +82-16, +82-17, +82-18, +82-19)
            const internationalPattern = /^821[016789]\d{8}$/;
            return internationalPattern.test(cleanPhone);
        },

        /**
         * 비밀번호 강도 검증
         * @param {string} password - 검증할 비밀번호
         * @returns {boolean} 유효한 비밀번호 여부
         */
        password: function(password) {
            const re = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
            return re.test(password);
        },

        /**
         * 사용자 ID 형식 검증
         * @param {string} userId - 검증할 사용자 ID
         * @returns {boolean} 유효한 사용자 ID 여부
         */
        userId: function(userId) {
            const re = /^[a-zA-Z0-9]{4,10}$/;
            return re.test(userId);
        },

        /**
         * 사업자등록번호 검증
         * @param {string} number - 검증할 사업자등록번호
         * @returns {boolean} 유효한 사업자등록번호 여부
         */
        businessNumber: function(number) {
            // TODO: 구현 예정
        },

        /**
         * 폼 필드 유효성 검사
         * @param {jQuery} $field - 검증할 필드
         * @param {string} message - 에러 메시지
         * @returns {boolean} 유효한 필드 여부
         */
        field: function($field, message) {
            if($field.val().trim() === '') {
                Zinidata.dom.showFieldError($field, message);
                return false;
            } else {
                Zinidata.dom.hideFieldError($field);
                return true;
            }
        }
    };

    // ============================== 스토리지 모듈 ==============================
    Zinidata.storage = {
        /**
         * 로컬 스토리지에 데이터 저장
         * @param {string} key - 저장할 키
         * @param {*} value - 저장할 값
         * @param {number} expireMinutes - 만료 시간 (분, 선택사항)
         */
        set: function(key, value, expireMinutes) {
            const data = {
                value: value,
                timestamp: Date.now()
            };
            
            if (expireMinutes) {
                data.expireTime = Date.now() + (expireMinutes * 60 * 1000);
            }
            
            localStorage.setItem(key, JSON.stringify(data));
        },

        /**
         * 로컬 스토리지에서 데이터 조회
         * @param {string} key - 조회할 키
         * @returns {*} 저장된 값 또는 null
         */
        get: function(key) {
            try {
                const item = localStorage.getItem(key);
                if (!item) return null;
                
                const data = JSON.parse(item);
                
                // 만료 시간 체크
                if (data.expireTime && Date.now() > data.expireTime) {
                    localStorage.removeItem(key);
                    return null;
                }
                
                return data.value;
            } catch (e) {
                return null;
            }
        },

        /**
         * 로컬 스토리지에서 데이터 제거
         * @param {string} key - 제거할 키
         */
        remove: function(key) {
            localStorage.removeItem(key);
        },

        /**
         * 로컬 스토리지 전체 초기화
         */
        clear: function() {
            localStorage.clear();
        },

        /**
         * 스토리지 정보 조회
         * @returns {Object} 스토리지 정보
         */
        getInfo: function() {
            // TODO: 구현 예정
        }
    };

    // ============================== DOM 조작 모듈 ==============================
    Zinidata.dom = {
        /**
         * 요소로 스크롤
         * @param {jQuery} $element - 스크롤할 요소
         * @param {number} offset - 오프셋 값 (기본값: 30)
         */
        scrollToElement: function($element, offset) {
            offset = offset || 30;
            if($element.length > 0) {
                const elementOffset = $element.offset();
                if(elementOffset) {
                    const scrollTop = elementOffset.top - offset;
                    $('html, body').animate({scrollTop: scrollTop}, 300);
                }
            }
        },

        /**
         * 필드 에러 표시
         * @param {jQuery} $field - 에러를 표시할 필드
         * @param {string} message - 에러 메시지
         */
        showFieldError: function($field, message) {
            $field.addClass('border-red-500');
            const $errorMsg = $field.parent().find('.error-message');
            if($errorMsg.length === 0) {
                $field.parent().append('<p class="error-message text-xs text-red-500 mt-1">' + message + '</p>');
            } else {
                $errorMsg.text(message).show();
            }
        },

        /**
         * 필드 에러 숨김
         * @param {jQuery} $field - 에러를 숨길 필드
         */
        hideFieldError: function($field) {
            $field.removeClass('border-red-500');
            $field.parent().find('.error-message').hide();
        },

        /**
         * 모든 필드 에러 초기화
         */
        clearFieldErrors: function() {
            $('.error-message').hide();
            $('.border-red-500').removeClass('border-red-500');
        }
    };

    // ============================== 보안 모듈 ==============================
    Zinidata.security = {
        /**
         * HTML 이스케이프 (XSS 방지)
         * @param {string} str - 이스케이프할 문자열
         * @returns {string} 이스케이프된 문자열
         */
        escapeHtml: function(str) {
            if (!str) return '';
            try {
                return String(str)
                    .replace(/&/g, '&amp;')
                    .replace(/</g, '&lt;')
                    .replace(/>/g, '&gt;')
                    .replace(/"/g, '&quot;')
                    .replace(/'/g, '&#39;');
            } catch (_) {
                return '';
            }
        },

        /**
         * 입력값 정제
         * @param {string} input - 정제할 입력값
         * @returns {string} 정제된 입력값
         */
        sanitizeInput: function(input) {
            // TODO: 구현 예정
        }
    };

    // ============================== 비밀번호 모듈 ==============================
    Zinidata.password = {
        /**
         * 비밀번호 보기/숨기기 토글
         * 표준 ID 사용: #password, #eyeIcon, #eyeSlashIcon
         */
        toggleVisibility: function() {
            const passwordInput = $("#password");
            const eyeIcon = $("#eyeIcon");
            const eyeSlashIcon = $("#eyeSlashIcon");
            
            if (passwordInput.attr("type") === "password") {
                passwordInput.attr("type", "text");
                eyeIcon.addClass("hidden");
                eyeSlashIcon.removeClass("hidden");
            } else {
                passwordInput.attr("type", "password");
                eyeIcon.removeClass("hidden");
                eyeSlashIcon.addClass("hidden");
            }
        },

        /**
         * 비밀번호 유효성 검사 (UI 업데이트 포함)
         * @param {string} password - 검사할 비밀번호
         * @param {Object} options - 옵션 설정
         * @param {string} options.inputId - 비밀번호 입력 필드 ID (기본값: 'password')
         * @param {string} options.checkListSelector - 체크리스트 셀렉터 (기본값: '.passwordCheckList li')
         * @param {string} options.infoBoxSelector - 정보박스 셀렉터 (기본값: '.passwordInfoBox')
         * @param {string} options.formCheckSelector - 폼 체크 셀렉터 (기본값: '.formCheck')
         * @returns {boolean} 모든 조건 만족 여부
         */
        validate: function(password, options) {
            options = options || {};
            const inputId = options.inputId || 'password';
            const checkListSelector = options.checkListSelector || '.passwordCheckList li';
            const infoBoxSelector = options.infoBoxSelector || '.passwordInfoBox';
            const formCheckSelector = options.formCheckSelector || '.formCheck';
            
            console.log('[PASSWORD] 비밀번호 유효성 검사:', password.length + '자');
            
            // 8~20자 길이 검사
            if(password.length >= 8 && password.length <= 20){
                $(checkListSelector + ':nth-child(1)').addClass('success');
            } else {
                $(checkListSelector + ':nth-child(1)').removeClass('success');
            }
            
            // 영문자 포함 검사
            const hasEnglish = /[a-zA-Z]/.test(password);
            if(hasEnglish){
                $(checkListSelector + ':nth-child(2)').addClass('success');
            } else {
                $(checkListSelector + ':nth-child(2)').removeClass('success');
            }
            
            // 숫자 포함 검사
            const hasNumber = /\d/.test(password);
            if (hasNumber) {
                $(checkListSelector + ':nth-child(3)').addClass('success');
            } else {
                $(checkListSelector + ':nth-child(3)').removeClass('success');
            }

            // 특수문자 포함 검사
            const hasSpecialChar = /[@$!%*#?&]/.test(password);
            if (hasSpecialChar) {
                $(checkListSelector + ':nth-child(4)').addClass('success');
            } else {
                $(checkListSelector + ':nth-child(4)').removeClass('success');
            }

            // 모든 조건 확인
            const successCount = $(checkListSelector + '.success').length;
            const allConditionsMet = successCount === 4;

            if (allConditionsMet) {
                $('#' + inputId).closest(formCheckSelector).removeClass('passwordError');
                $('#' + inputId).data('validated', true);
                // 모든 조건이 만족되면 조건창 숨김
                $(infoBoxSelector).hide();
            } else {
                $('#' + inputId).closest(formCheckSelector).addClass('passwordError');
                $('#' + inputId).data('validated', false);
                // 조건이 불충족되면 조건창 표시 (입력 중일 때만)
                if (password.length > 0) {
                    $(infoBoxSelector).show();
                }
            }

            return allConditionsMet;
        },

        /**
         * 비밀번호 조건 검증 (UI 업데이트 없이 조건만 체크)
         * @param {string} password - 검사할 비밀번호
         * @returns {boolean} 모든 조건 만족 여부
         */
        checkConditions: function(password) {
            // 빈 비밀번호는 유효하지 않음
            if (!password || password.trim() === '') {
                return false;
            }
            
            const conditions = {
                length: password.length >= 8 && password.length <= 20,
                hasLetter: /[a-zA-Z]/.test(password),
                hasNumber: /\d/.test(password),
                hasSpecial: /[@$!%*#?&]/.test(password)
            };
            
            // 모든 조건이 만족되었는지 확인
            const allConditionsMet = Object.values(conditions).every(condition => condition);
            return allConditionsMet;
        },

        /**
         * 비밀번호 조건 검증 (UI 업데이트 포함)
         * @param {string} password - 검사할 비밀번호
         * @param {Object} options - 옵션 설정
         * @param {string} options.checkListSelector - 체크리스트 셀렉터 (기본값: '.passwordCheckList li')
         * @param {string} options.infoBoxSelector - 정보박스 셀렉터 (기본값: '.passwordInfoBox')
         * @returns {boolean} 모든 조건 만족 여부
         */
        validateConditions: function(password, options) {
            options = options || {};
            const checkListSelector = options.checkListSelector || '.passwordCheckList li';
            const infoBoxSelector = options.infoBoxSelector || '.passwordInfoBox';
            
            const conditions = {
                length: password.length >= 8 && password.length <= 20,
                hasLetter: /[a-zA-Z]/.test(password),
                hasNumber: /[0-9]/.test(password),
                hasSpecial: /[@$!%*#?&]/.test(password)
            };
            
            const allConditionsMet = conditions.length && conditions.hasLetter && conditions.hasNumber && conditions.hasSpecial;
            
            // 아이콘 업데이트
            $(checkListSelector).each(function(index) {
                const $li = $(this);
                const $icon = $li.find('.passwordCheckIcon');
                
                if (index === 0) {
                    // 8~20자 길이
                    if (conditions.length) {
                        $li.addClass('success').removeClass('error');
                        $icon.html('✓');
                    } else {
                        $li.removeClass('success').addClass('error');
                        $icon.html('✗');
                    }
                } else if (index === 1) {
                    // 영문자 포함
                    if (conditions.hasLetter) {
                        $li.addClass('success').removeClass('error');
                        $icon.html('✓');
                    } else {
                        $li.removeClass('success').addClass('error');
                        $icon.html('✗');
                    }
                } else if (index === 2) {
                    // 숫자 포함
                    if (conditions.hasNumber) {
                        $li.addClass('success').removeClass('error');
                        $icon.html('✓');
                    } else {
                        $li.removeClass('success').addClass('error');
                        $icon.html('✗');
                    }
                } else if (index === 3) {
                    // 특수문자 포함
                    if (conditions.hasSpecial) {
                        $li.addClass('success').removeClass('error');
                        $icon.html('✓');
                } else {
                        $li.removeClass('success').addClass('error');
                        $icon.html('✗');
                    }
                }
            });
            
            // 모든 조건이 만족되면 팝업 숨기기
            if (allConditionsMet) {
                $(infoBoxSelector).addClass('hidden');
            }
            
            return allConditionsMet;
        }
    };

    // ============================== 이벤트 처리 모듈 ==============================
    Zinidata.events = {
        setupFormEvents: function() {
            // TODO: 구현 예정
        },

        setupCardEvents: function() {
            // TODO: 구현 예정
        },

        setupInputFormatting: function() {
            // TODO: 구현 예정
        },

        preventDoubleClick: function(button, options) {
            // TODO: 구현 예정
        }
    };

    // ============================== 메뉴 관리 모듈 ==============================
    Zinidata.menu = {
        /**
         * 페이지별 메뉴 활성화
         * @param {string} pageName - 페이지 이름 ('home', 'pricing', 'explorer' 등)
         * @param {string} menuId - 활성화할 메뉴 ID (기본값: 'pc-menu-' + pageName)
         */
        activate: function(pageName, menuId) {
            menuId = menuId || 'pc-menu-' + pageName;
            console.log('[MENU] ' + pageName + ' 페이지 메뉴 활성화 시작');
            
            // 현재 페이지 정보 설정
            window.setCurrentPage(pageName);
            
            // 전역 모바일 감지 시스템 사용
            if (window.isMobile()) {
                console.log('[MENU] 모바일 환경 - 메뉴 활성화 건너뜀');
                window.markMenuInitialized();
                return;
            }
            
            console.log('[MENU] PC 환경 - ' + pageName + ' 페이지 메뉴 활성화 시작');
            
            // 모든 메뉴에서 active 클래스 제거
            $('.headerMenuList ul li').removeClass('active');
            
            // 해당 메뉴에 active 클래스 추가
            const $targetMenu = $('#' + menuId);
            if ($targetMenu.length > 0) {
                $targetMenu.addClass('active');
                console.log('[MENU] ' + menuId + '에 active 클래스 추가 완료');
            } else {
                console.warn('[MENU] ' + menuId + ' 요소를 찾을 수 없습니다.');
            }
            
            // 메뉴 초기화 완료 표시
            window.markMenuInitialized();
        }
    };

    // ============================== 네비게이션 모듈 ==============================
    /**
     * Zinidata.navigation.go 사용 가이드
     * 
     * ✅ 사용 권장 케이스:
     * 1. JavaScript 파일 내부에서 페이지 이동
     *    예: $('.btn').on('click', function() { Zinidata.navigation.go('/page'); });
     * 
     * 2. 조건부 로직이 있는 경우
     *    예: if (isValid) { Zinidata.navigation.go('/success'); }
     * 
     * 3. 동적 파라미터 생성
     *    예: Zinidata.navigation.go('/result?id=' + userId);
     * 
     * 4. AJAX 콜백 내부
     *    예: success: function(data) { Zinidata.navigation.go('/complete'); }
     * 
     * 5. 새 탭 옵션이 필요한 경우
     *    예: Zinidata.navigation.go('/external', {newTab: true});
     * 
     * ❌ 사용 비권장 케이스 (대안):
     * 1. HTML 템플릿의 단순 링크 → <a href="/page">링크</a>
     * 2. 외부 URL → <a href="https://..." target="_blank" rel="noopener noreferrer">
     * 3. 페이지 새로고침 → location.reload()
     * 4. onclick 인라인 이벤트 → <a> 태그 권장 (SEO/접근성)
     */
    Zinidata.navigation = {
        /**
         * 페이지 이동
         * @param {string} url - 이동할 URL
         * @param {Object} options - 옵션 설정
         * @param {boolean} options.newTab - 새 탭에서 열기 (기본값: false)
         */
        go: function(url, options) {
            options = options || {};
            const newTab = options.newTab || false;
            
            console.log('[NAVIGATION] 페이지 이동:', url, newTab ? '(새 탭)' : '');
            
            // 프리미엄보고서 모바일 접근 차단
            if (url && (url.includes('explorer/premium') || url.includes('/explorer/premium'))) {
                if (Zinidata.device.isMobile) {
                    Zinidata.showAlert('프리미엄 보고서는 PC에서\n확인 가능합니다.', 'doneRed');
                    return; // 이동 중단
                }
            }
            
            if (newTab) {
                window.open(url, '_blank');
            } else {
                window.location.href = url;
            }
        }
    };

    // ============================== BetterBoss 모듈 ==============================
    Zinidata.betterBoss = {
        /**
         * URL 파라미터 확인하여 BetterBoss 자동 열기
         * @param {string} returnUrl - 로그인 후 돌아올 URL (기본값: 'betterboss')
         */
        checkAndOpen: function(returnUrl) {
            returnUrl = returnUrl || 'betterboss';
            const urlParams = new URLSearchParams(window.location.search);
            const openBetterBoss = urlParams.get('openBetterBoss');
            
            if (openBetterBoss === 'true') {
                console.log('[BETTERBOSS] 자동 열기 시작 - returnUrl:', returnUrl);
                
                // URL에서 파라미터 제거 (히스토리 클린업)
                const cleanUrl = window.location.pathname;
                window.history.replaceState({}, document.title, cleanUrl);
                
                // BetterBoss 기능이 비활성화된 경우
                if (!window.BETTERBOSS_ENABLED) {
                    Zinidata.showAlert('BetterBoss 기능이 현재 비활성화되어 있습니다.', 'doneRed');
                    return;
                }
                
                // JWT 토큰 생성 후 새창 열기
                this.generateToken();
            }
        },

        /**
         * BetterBoss용 JWT 토큰 생성 및 새창 열기
         */
        generateToken: function() {
            console.log('[BETTERBOSS] JWT 토큰 생성 시작');
            
            Zinidata.api({
                url: '/api/jwt/generate-token',
                method: 'POST',
                success: function(response) {
                    Zinidata.betterBoss.handleResponse(response);
                },
                error: function(xhr, status, error) {
                    Zinidata.betterBoss.handleError(xhr, status, error);
                }
            });
        },

        /**
         * BetterBoss 토큰 생성 성공 처리
         * @param {Object} response - API 응답
         */
        handleResponse: function(response) {
            console.log('[BETTERBOSS] JWT 토큰 생성 응답:', response);
            
            if (response.success) {
                console.log('[BETTERBOSS] JWT 토큰 생성 성공');
                console.log('[BETTERBOSS] 토큰 정보:', {
                    loginId: response.loginId,
                    memNm: response.memNm,
                    loginType: response.loginType,
                    tokenLength: response.tokenLength
                });
                
                // JWT 토큰이 쿠키에 설정되었으므로 BetterBoss로 새창 열기
                const betterBossUrl = window.BETTERBOSS_URL;
                console.log('[BETTERBOSS] BetterBoss로 새창 열기:', betterBossUrl);
                
                // 잠시 대기 후 새창 열기 (쿠키 설정 완료 대기)
                setTimeout(function() {
                    window.open(betterBossUrl, '_blank');
                }, 100);
            } else {
                console.error('[BETTERBOSS] JWT 토큰 생성 실패:', response.message);
                Zinidata.showAlert('JWT 토큰 생성에 실패했습니다: ' + response.message, 'doneRed');
            }
        },

        /**
         * BetterBoss 토큰 생성 실패 처리
         * @param {Object} xhr - XMLHttpRequest 객체
         * @param {string} status - 상태
         * @param {string} error - 오류 메시지
         */
        handleError: function(xhr, status, error) {
            console.error('[BETTERBOSS] JWT 토큰 생성 실패:', {
                status: xhr.status,
                statusText: xhr.statusText,
                error: error,
                responseText: xhr.responseText
            });
            
            if (xhr.status === 401) {
                Zinidata.showAlert('로그인이 필요합니다. 다시 로그인해주세요.', 'doneRed');
                return;
            } else {
                Zinidata.showAlert('JWT 토큰 생성 중 오류가 발생했습니다.', 'doneRed');
            }
        },

        /**
         * BetterBoss 버튼 클릭 처리
         * @param {string} returnUrl - 로그인 후 돌아올 URL (기본값: 'betterboss')
         * @param {Object} options - 옵션 설정 (선택사항)
         */
        handleClick: function(returnUrl, options) {
            returnUrl = returnUrl || 'betterboss';
            options = options || {};
            console.log('[BETTERBOSS] 버튼 클릭 - returnUrl:', returnUrl);
            
            // BetterBoss 기능이 비활성화된 경우
            if (!window.BETTERBOSS_ENABLED) {
                Zinidata.showAlert('BetterBoss 기능이 현재 비활성화되어 있습니다.', 'doneRed');
                return;
            }
            
            // 로그인 상태 확인 (렌더링된 HTML 구조 기반)
            const isLoggedIn = this.checkLoginStatus();
            console.log('[BETTERBOSS] 로그인 상태:', isLoggedIn);
            
            if (!isLoggedIn) {
                // 로그인되지 않은 경우 로그인 페이지로 이동 (returnUrl 파라미터 포함)
                console.log('[BETTERBOSS] 비로그인 - 로그인 페이지로 리다이렉트');
                window.location.href = '/auth/login?returnUrl=' + returnUrl;
                return;
            }
            
            // 로그인된 경우 JWT 토큰 생성 후 BetterBoss로 이동
            console.log('[BETTERBOSS] 로그인 확인 - JWT 토큰 생성 시작');
            this.generateToken();
        },

        /**
         * 로그인 상태 확인 (렌더링된 HTML 구조 기반)
         * @returns {boolean} 로그인 여부
         * @note zinidata-auth-v1.js의 isLoggedIn() 로직과 동일 (1980번 라인)
         */
        checkLoginStatus: function() {
            // 렌더링된 HTML 구조로 로그인 여부 확인
            const $userBoxWrap = $('.userBoxWrap');
            const $loginBeforeBtn = $('.py-1-5.px-2.rounded-sm.bg-blue-500\\/6'); // 로그인 전 버튼
            const isLoggedIn = $userBoxWrap.length > 0 && $loginBeforeBtn.length === 0;
            
            console.log('[BETTERBOSS] 로그인 상태 확인:', {
                userBoxWrap: $userBoxWrap.length,
                loginBeforeBtn: $loginBeforeBtn.length,
                isLoggedIn: isLoggedIn
            });
            
            return isLoggedIn;
        }
    };

    // ============================== 초기화 ==============================
    function initializeModules() {
        // ============================== 전역 노출 제거 ==============================
        // 모든 함수는 Zinidata 네임스페이스를 통해 접근
        // 예: Zinidata.storage.set(), Zinidata.form.toJson()
        
        // 개발 환경에서만 디버깅 함수 노출
        if (Zinidata.config && Zinidata.config.debug) {
            window.ZinidataDebug = window.ZinidataDebug || {};
            window.ZinidataDebug.form = Zinidata.form;
            window.ZinidataDebug.validation = Zinidata.validation;
            window.ZinidataDebug.storage = Zinidata.storage;
            window.ZinidataDebug.dom = Zinidata.dom;
            window.ZinidataDebug.security = Zinidata.security;
            window.ZinidataDebug.password = Zinidata.password;
            window.ZinidataDebug.events = Zinidata.events;
        }
    }

    initializeModules();
});