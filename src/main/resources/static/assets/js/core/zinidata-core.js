/**
 * ============================================
 * 지니데이타 핵심 인프라 (Core Infrastructure)
 * ============================================
 * 
 * 🎯 핵심 책임
 * ✅ API 통신: api()
 * ✅ 알림 시스템: showAlert(), showConfirmModal()
 * ✅ 로딩 관리: showLoading(), hideLoading()
 * ✅ 디바이스 감지: device.*
 * ✅ 페이지 관리: page.*
 * ✅ 전역 설정: config.*
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 * @refactored 2024.12
 */

$(document).ready(function() {
    window.Zinidata = {
        version: '1.0.0',
        config: {
            apiBaseUrl: '/api',
            timeout: 30000,
            debug: false
        },
        
        // ============================== 내부 상태 ==============================
        alertTimer: null,

        // ============================== 핵심 API ==============================
        /**
         * 범용 API 호출 함수
         * @param {Object} options - 요청 옵션
         * @param {string} options.url - API URL
         * @param {string} options.method - HTTP 메서드 (기본값: GET)
         * @param {Object} options.data - 요청 데이터
         * @param {Function} options.success - 성공 콜백
         * @param {Function} options.error - 실패 콜백
         * @param {boolean} options.useToken - CSRF 토큰 사용 여부 (기본값: true)
         * @param {string} options.contentType - Content-Type (기본값: application/json)
         * @param {boolean} options.showLoading - 로딩 표시 여부 (기본값: false)
         * @param {jQuery} options.loadingTarget - 로딩을 표시할 버튼 요소
         * @returns {jQuery.jqXHR} jQuery AJAX 객체
         */
        api: function(options) {
            // 기본 설정
            const config = $.extend({
                method: 'GET',
                dataType: 'json',
                timeout: Zinidata.config.timeout,
                useToken: true,
                contentType: 'application/json',
                showLoading: false,
                disableAutoRedirect: false
            }, options);
            
            // 로딩 시작
            if (config.showLoading && config.loadingTarget) {
                Zinidata.showLoading(config.loadingTarget, '처리중');
            }
            
            // 헤더 설정 (options.headers 병합)
            const headers = $.extend({}, config.headers || {});
            
            // CSRF 토큰 설정
            if (config.useToken) {
                const csrfToken = $('meta[name="_csrf"]').attr('content');
                if (csrfToken) {
                    headers['X-CSRF-TOKEN'] = csrfToken;
                }
            }
            
            // 데이터 준비
            let requestData = config.data || {};
            
            // GET 요청에서 빈 객체인 경우 undefined로 설정
            if (config.method.toUpperCase() === 'GET' && 
                requestData && 
                typeof requestData === 'object' && 
                Object.keys(requestData).length === 0) {
                requestData = undefined;
            }
            
            // Content-Type에 따른 데이터 처리
            let processedData;
            if (config.method.toUpperCase() === 'GET') {
                processedData = requestData;
            } else if (config.contentType === 'application/json' && requestData !== undefined) {
                processedData = JSON.stringify(requestData);
            } else {
                processedData = requestData;
            }
            
            // URL 처리 - 상대 경로인 경우 현재 프로토콜 사용
            let requestUrl = config.url;
            if (requestUrl.startsWith('/')) {
                requestUrl = window.location.protocol + '//' + window.location.host + requestUrl;
            }
            
            // AJAX 요청 설정
            const ajaxConfig = {
                url: requestUrl,
                method: config.method,
                dataType: config.dataType,
                headers: headers,
                data: processedData,
                timeout: config.timeout,
                disableAutoRedirect: config.disableAutoRedirect,

                success: function(response) {
                    // 로딩 해제
                    if (config.showLoading && config.loadingTarget) {
                        Zinidata.hideLoading(config.loadingTarget);
                    }
                    
                    // 성공 콜백 실행
                    if (config.success && typeof config.success === 'function') {
                        config.success(response);
                    }
                },
                error: function(xhr, status, error) {
                    // 로딩 해제
                    if (config.showLoading && config.loadingTarget) {
                        Zinidata.hideLoading(config.loadingTarget);
                    }
                    
                    // 401 에러는 콘솔 로그 출력 방지
                    if (xhr.status === 401) {
                        if (config.error && typeof config.error === 'function') {
                            config.error(xhr, status, error);
                        }
                        return;
                    }
                    
                    // 커스텀 에러 콜백
                    if (config.error && typeof config.error === 'function') {
                        config.error(xhr, status, error);
                    } else {
                        // 기본 에러 처리
                        let errorMessage = '요청 처리 중 오류가 발생했습니다.';
                        if (xhr.responseJSON && xhr.responseJSON.message) {
                            errorMessage = xhr.responseJSON.message;
                        }
                        Zinidata.showAlert(errorMessage, 'fail');
                    }
                }
            };
            
            // GET 요청이 아닌 경우에만 Content-Type 설정
            if (config.method.toUpperCase() !== 'GET') {
                ajaxConfig.contentType = config.contentType;
            }
            
            // AJAX 요청 실행
            return $.ajax(ajaxConfig);
        },

        // ============================== 알림 시스템 ==============================
        /**
         * 알림 메시지 표시
         * @param {string} message - 표시할 메시지
         * @param {string} type - 알림 타입 (fail, success 등)
         * @param {Function} afterFunc - 알림 완료 후 실행할 함수
         */
        showAlert: function(message, type, afterFunc) {
            // 기존 타이머 취소
            if (Zinidata.alertTimer) {
                clearTimeout(Zinidata.alertTimer);
                Zinidata.alertTimer = null;
            }
            
            let $modal = $('.alertModal');
            let $text = $('.alertText');
            
            // alertModal이 없으면 동적으로 생성
            if ($modal.length === 0) {
                $('body').append(
                    '<div class="alertModal">' +
                        '<div class="alertIcon"></div>' +
                        '<div class="alertText"></div>' +
                    '</div>'
                );
                $modal = $('.alertModal');
                $text = $('.alertText');
            } 
            // else {
                // 기존 모달도 중앙 정렬 보정
            //     $modal.css('transform', 'translateX(-50%)');
            // }
            
            // 1. 기존 클래스 모두 제거 (애니메이션 리셋)
            $modal.removeClass('success fail');
            
            // 2. 강제로 리플로우 발생시켜 CSS 변경사항 즉시 적용
            if ($modal[0]) {
                $modal[0].offsetHeight;
            }
            
            // 3. 새로운 메시지와 스타일 적용 (줄바꿈 처리)
            $text.html(message.replace(/\n/g, '<br>'));
            
            // 공백 추가
            const classesToAdd = type + ' flex';
            $modal.addClass(classesToAdd);
            
            // 4. 2초 후 클래스 제거 - 새로운 타이머 설정
            Zinidata.alertTimer = setTimeout(function() {
                $modal.removeClass(classesToAdd);
                
                // 타이머 초기화
                Zinidata.alertTimer = null;

                if (afterFunc) {
                    afterFunc();
                }
            }, 2000);
        },

        /**
         * 범용 확인 모달 표시 (페이지 이동, 회원 탈퇴 등 모든 확인용)
         * @param {Object} options - 모달 옵션
         * @param {string} options.title - 모달 제목 (기본값: '확인')
         * @param {string} options.message - 메시지 (기본값: '정말로 진행하시겠습니까?')
         * @param {string} options.subMessage - 부가 메시지 (선택사항)
         * @param {string} options.cancelText - 취소 버튼 텍스트 (기본값: '취소')
         * @param {string} options.confirmText - 확인 버튼 텍스트 (기본값: '확인')
         * @param {string} options.confirmClass - 확인 버튼 CSS 클래스 (기본값: 'primaryBtn')
         * @param {string} options.overlayColor - 배경 오버레이 색상 (기본값: 'rgba(0, 0, 0, 0.6)')
         * @param {Function} options.onConfirm - 확인 버튼 클릭 시 실행할 함수
         * @param {Function} options.onCancel - 취소 버튼 클릭 시 실행할 함수
         */
        showConfirmModal: function(options) {
            // 기본 옵션 설정
            const config = $.extend({
                title: '확인',
                message: '정말로 진행하시겠습니까?',
                subMessage: '',
                cancelText: '취소',
                confirmText: '확인',
                confirmClass: 'primaryBtn',
                overlayColor: 'rgba(0, 0, 0, 0.6)',
                onConfirm: function() {
                    // 기본 동작 없음
                },
                onCancel: function() {
                    // 기본 동작 없음
                }
            }, options);

            // 기존 모달 제거
            $('.confirmModal').remove();

            // 모달 HTML 생성
            const modalHtml = `
                <div class="layerModal confirmModal">
                    <div class="layerModalContent">
                        <p class="layerModalTitle">${config.title}</p>
                        <div class="layerModalInner pb-4">
                            <p class="layerModalText">
                                ${config.message}${config.subMessage ? '<br/>' + config.subMessage : ''}
                            </p>
                        </div>        
                        <div class="flexBetween gap-2">
                            <button type="button" class="btn grayBtn w-1/2 h-10 confirmModalCancel">${config.cancelText}</button>
                            <button type="button" class="btn ${config.confirmClass} w-1/2 h-10 confirmModalConfirm">${config.confirmText}</button>
                        </div>
                    </div>
                    <div class="layerPopupBg pointer-events-none" style="background: ${config.overlayColor};"></div>
                </div>
            `;

            // 모달을 body에 추가
            $('body').append(modalHtml);

            // 이벤트 바인딩
            $('.confirmModalCancel').on('click', function() {
                $('.confirmModal').remove();
                if (config.onCancel && typeof config.onCancel === 'function') {
                    config.onCancel();
                }
            });

            $('.confirmModalConfirm').on('click', function() {
                $('.confirmModal').remove();
                if (config.onConfirm && typeof config.onConfirm === 'function') {
                    config.onConfirm();
                }
            });

            // 배경 클릭 시 닫기
            $('.confirmModal .layerPopupBg').on('click', function() {
                $('.confirmModal').remove();
                if (config.onCancel && typeof config.onCancel === 'function') {
                    config.onCancel();
                }
            });

            // ESC 키로 닫기
            $(document).on('keydown.confirmModal', function(e) {
                if (e.key === 'Escape') {
                    $('.confirmModal').remove();
                    $(document).off('keydown.confirmModal');
                    if (config.onCancel && typeof config.onCancel === 'function') {
                        config.onCancel();
                    }
                }
            });
        },

        /**
         * 단일/이중 버튼 알림 모달 표시 (완료/성공/알림용)
         * @param {Object} options - 모달 옵션
         * @param {string} options.title - 모달 제목 (기본값: '알림')
         * @param {string} options.message - 메시지 (기본값: '처리가 완료되었습니다.')
         * @param {string} options.subMessage - 부가 메시지 (선택사항)
         * @param {string} options.buttonText - 버튼 텍스트 (기본값: '확인')
         * @param {string} options.buttonClass - 버튼 CSS 클래스 (기본값: 'primaryBtn')
         * @param {string} options.secondaryButtonText - 두 번째 버튼 텍스트 (선택사항)
         * @param {string} options.secondaryButtonClass - 두 번째 버튼 CSS 클래스 (기본값: 'secondaryBtn')
         * @param {string} options.overlayColor - 배경 오버레이 색상 (기본값: 'rgba(0, 0, 0, 0.6)')
         * @param {Function} options.onConfirm - 버튼 클릭 시 실행할 함수
         * @param {Function} options.onSecondaryConfirm - 두 번째 버튼 클릭 시 실행할 함수
         */
        showAlertModal: function(options) {
            // 기본 옵션 설정
            const config = $.extend({
                title: '알림',
                message: '처리가 완료되었습니다.',
                subMessage: '',
                buttonText: '확인',
                buttonClass: 'primaryBtn',
                secondaryButtonText: '',
                secondaryButtonClass: 'secondaryBtn',
                overlayColor: 'rgba(0, 0, 0, 0.6)',
                onConfirm: function() {
                    // 기본 동작 없음
                },
                onSecondaryConfirm: function() {
                    // 기본 동작 없음
                }
            }, options);

            // 기존 모달 제거
            $('.alertModalDialog').remove();

            // 버튼 영역 HTML 생성
            let buttonAreaHtml;
            if (config.secondaryButtonText) {
                // 두 개 버튼
                buttonAreaHtml = `
                    <div class="flexBetween gap-2">
                        <button type="button" class="btn ${config.buttonClass} w-1/2 h-10 alertModalDialogConfirm">${config.buttonText}</button>
                        <button type="button" class="btn ${config.secondaryButtonClass} w-1/2 h-10 alertModalDialogSecondaryConfirm">${config.secondaryButtonText}</button>
                    </div>
                `;
            } else {
                // 단일 버튼
                buttonAreaHtml = `
                    <div class="flexCenter">
                        <button type="button" class="btn ${config.buttonClass} h-10 px-8 alertModalDialogConfirm">${config.buttonText}</button>
                    </div>
                `;
            }

            // 모달 HTML 생성
            const modalHtml = `
                <div class="layerModal alertModalDialog">
                    <div class="layerModalContent">
                        <p class="layerModalTitle">${config.title}</p>
                        <div class="layerModalInner pb-4">
                            <p class="layerModalText">
                                ${config.message}${config.subMessage ? '<br/>' + config.subMessage : ''}
                            </p>
                        </div>        
                        ${buttonAreaHtml}
                    </div>
                    <div class="layerPopupBg pointer-events-none" style="background: ${config.overlayColor};"></div>
                </div>
            `;

            // 모달을 body에 추가
            $('body').append(modalHtml);

            // 이벤트 바인딩
            $('.alertModalDialogConfirm').on('click', function() {
                $('.alertModalDialog').remove();
                if (config.onConfirm && typeof config.onConfirm === 'function') {
                    config.onConfirm();
                }
            });

            // 두 번째 버튼 이벤트 바인딩
            if (config.secondaryButtonText) {
                $('.alertModalDialogSecondaryConfirm').on('click', function() {
                    $('.alertModalDialog').remove();
                    if (config.onSecondaryConfirm && typeof config.onSecondaryConfirm === 'function') {
                        config.onSecondaryConfirm();
                    }
                });
            }

            // 배경 클릭 시 닫기
            $('.alertModalDialog .layerPopupBg').on('click', function() {
                $('.alertModalDialog').remove();
                if (config.onConfirm && typeof config.onConfirm === 'function') {
                    config.onConfirm();
                }
            });

            // ESC 키로 닫기
            $(document).on('keydown.alertModalDialog', function(e) {
                if (e.key === 'Escape') {
                    $('.alertModalDialog').remove();
                    $(document).off('keydown.alertModalDialog');
                    if (config.onConfirm && typeof config.onConfirm === 'function') {
                        config.onConfirm();
                    }
                }
            });
        },

        // ============================== 로딩 관리 ==============================
        /**
         * 버튼/요소 로딩 표시
         * @param {jQuery|Element|string} button - 로딩을 표시할 요소
         * @param {string} text - 로딩 중 표시할 텍스트 (기본값: '처리중')
         */
        showLoading: function(button, text) {
            text = text || '처리중';
            const $el = $(button);
            if ($el.length === 0) return;
            
            // 공통 비활성화 스타일
            $el.prop('disabled', true);
            $el.addClass('opacity-60 cursor-not-allowed');

            const tag = ($el[0].tagName || '').toUpperCase();
            if (tag === 'BUTTON') {
                // 기존 HTML 백업 후 점 애니메이션 스피너 + 텍스트 적용
                $el.data('originalHtml', $el.html());
                $el.html(`
                    <div style="display: flex; align-items: center; justify-content: center;">
                        <span class="loading-dots">
                            <span class="dot"></span>
                            <span class="dot"></span>
                            <span class="dot"></span>
                        </span>
                        <span>${text}</span>
                    </div>
                `);
            } else {
                // 버튼이 아닌 경우는 텍스트만 백업/교체 (스피너 삽입은 생략)
                $el.data('originalText', $el.text());
                $el.text(text);
            }
            
            // CSS 애니메이션 추가 (한 번만)
            if (!document.getElementById('loadingSpinnerStyle')) {
                const style = document.createElement('style');
                style.id = 'loadingSpinnerStyle';
                style.textContent = `
                    .loading-dots {
                        display: flex;
                        align-items: center;
                        gap: 3px;
                        margin-right: 8px;
                    }
                    .loading-dots .dot {
                        width: 4px;
                        height: 4px;
                        background: currentColor;
                        border-radius: 50%;
                        animation: dot-bounce 1.4s infinite ease-in-out both;
                    }
                    .loading-dots .dot:nth-child(1) {
                        animation-delay: 0s;
                    }
                    .loading-dots .dot:nth-child(2) {
                        animation-delay: 0.2s;
                    }
                    .loading-dots .dot:nth-child(3) {
                        animation-delay: 0.4s;
                    }
                    @keyframes dot-bounce {
                        0%, 80%, 100% { 
                            transform: scale(0);
                            opacity: 0.5;
                        }
                        40% { 
                            transform: scale(1);
                            opacity: 1;
                        }
                    }
                `;
                document.head.appendChild(style);
            }
        },

        /**
         * 버튼/요소 로딩 해제
         * @param {jQuery|Element|string} button - 로딩을 해제할 요소
         * @param {string} text - 복원할 텍스트 (지정된 경우)
         */
        hideLoading: function(button, text) {
            const $el = $(button);
            if ($el.length === 0) return;

            $el.prop('disabled', false);
            $el.removeClass('opacity-60 cursor-not-allowed');

            const tag = ($el[0].tagName || '').toUpperCase();
            if (typeof text === 'string' && text.length > 0) {
                // 호출부에서 복원 텍스트가 지정된 경우 우선 적용
                if (tag === 'BUTTON') $el.html(text);
                else $el.text(text);
            } else {
                // 지정 텍스트가 없으면 기존 콘텐츠 복구
                const originalHtml = $el.data('originalHtml');
                if (originalHtml != null && tag === 'BUTTON') {
                    $el.html(originalHtml);
                } else {
                    const originalText = $el.data('originalText');
                    if (originalText != null) $el.text(originalText);
                }
            }

            // 백업 데이터 제거
            $el.removeData('originalHtml');
            $el.removeData('originalText');
        },

        /**
         * 컨테이너 오버레이 로딩 표시
         * @param {string} containerId - 로딩을 표시할 컨테이너 ID
         * @param {string} text - 로딩 중 표시할 텍스트 (기본값: '로딩 중...')
         */
        showOverlayLoading: function(containerId, text) {
            text = text || '데이터를 불러오는 중입니다...';
            const container = document.getElementById(containerId);
            if (!container) {
                console.warn('[ZINIDATA] 컨테이너를 찾을 수 없습니다:', containerId);
                return;
            }

            // 기존 로딩 요소 제거 (중복 방지)
            this.hideOverlayLoading(containerId);

            // 오버레이 로딩 요소 생성
            const overlay = document.createElement('div');
            overlay.id = 'loadingIndicator';
            overlay.className = 'zinidata-overlay-loading';
            overlay.setAttribute('data-container', containerId);
            overlay.setAttribute('role', 'alert');
            overlay.setAttribute('aria-live', 'assertive');
            overlay.innerHTML = `
                <div class="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-[9999] flex items-center justify-center">
                    <div class="bg-white p-4 rounded-lg flex items-center space-x-3">
                        <svg aria-hidden="true" class="animate-spin h-5 w-5 text-blue-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                        <span class="text-sm text-slate-800">${text}</span>
                    </div>
                </div>
            `;

            // 컨테이너에 상대 위치 설정
            const containerStyle = window.getComputedStyle(container);
            if (containerStyle.position === 'static') {
                container.style.position = 'relative';
            }

            // 오버레이 추가
            container.appendChild(overlay);

            // 컨테이너에 로딩 상태 클래스 추가
            container.classList.add('loading-state');
        },

        /**
         * 컨테이너 오버레이 로딩 해제
         * @param {string} containerId - 로딩을 해제할 컨테이너 ID
         */
        hideOverlayLoading: function(containerId) {
            const container = document.getElementById(containerId);
            if (!container) return;

            // 컨테이너 로딩 상태 클래스 제거
            container.classList.remove('loading-state');

            // 모든 로딩 관련 요소 제거
            const loadingElements = container.querySelectorAll(
                '.zinidata-overlay-loading, .loading-overlay, .overlay-loading, .loading-spinner, [class*="loading"], .zinidata-loading'
            );
            loadingElements.forEach(element => element.remove());

            // 부모 컨테이너에서도 로딩 요소 찾아서 제거
            const parentContainer = container.parentElement;
            if (parentContainer) {
                const parentLoadingElements = parentContainer.querySelectorAll(
                    '.loading-overlay, .overlay-loading, .loading-spinner, [class*="loading"], .zinidata-loading'
                );
                parentLoadingElements.forEach(element => element.remove());
            }

            // 전체 페이지에서 해당 컨테이너 관련 로딩 요소 제거
            document.querySelectorAll(`[data-container="${containerId}"], [data-target="${containerId}"]`)
                .forEach(element => element.remove());
        },

        // ============================== 디바이스 감지 ==============================
        device: {
            isMobile: false,
            mediaQuery: null,
            
            /**
             * 모바일 감지 시스템 초기화
             */
            initialize: function() {
                // UA 기반 모바일 감지 (보조용)
                const _isMobileUA = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini|Mobile|mobile|CriOS|Chrome\/[.0-9]* Mobile/i.test(navigator.userAgent || '');

                // 뷰포트 기반 모바일 감지 (메인)
                this.isMobile = this.isMobileViewport() || _isMobileUA;

                // MediaQuery 객체 생성
                this.mediaQuery = window.matchMedia('(max-width: 1024px)');
                
                console.log('[CORE] 모바일 감지 시스템 초기화 - isMobile:', this.isMobile, ' _isMobileUA:', _isMobileUA);

                // 실시간 반응형 감지 설정
                this.setupResponsiveDetection();
            },

            /**
             * 뷰포트 기반 모바일 체크
             * @returns {boolean} 모바일 여부
             */
            isMobileViewport: function() {
                try { 
                    return this.mediaQuery ? this.mediaQuery.matches : window.innerWidth <= 1024; 
                } catch (_) { 
                    return window.innerWidth <= 1024; 
                }
            },

            /**
             * 실시간 반응형 감지 설정
             */
            setupResponsiveDetection: function() {
                const self = this;
                
                // MediaQuery 기반 실시간 감지
                try {
                    if (this.mediaQuery && this.mediaQuery.addEventListener) {
                        this.mediaQuery.addEventListener('change', function(e) { 
                            const prevMobile = self.isMobile;
                            self.isMobile = e.matches;
                            console.log('[CORE] 모바일 상태 변경:', prevMobile, '->', self.isMobile);
                            
                            // 메뉴 활성화 상태 동적 관리
                            if (Zinidata.page && Zinidata.page.handleResponsiveMenuActivation) {
                                Zinidata.page.handleResponsiveMenuActivation(prevMobile, self.isMobile);
                            }
                        });
                    } else if (this.mediaQuery && this.mediaQuery.addListener) {
                        // Safari 구버전 대응
                        this.mediaQuery.addListener(function(e) { 
                            const prevMobile = self.isMobile;
                            self.isMobile = e.matches; 
                            if (Zinidata.page && Zinidata.page.handleResponsiveMenuActivation) {
                                Zinidata.page.handleResponsiveMenuActivation(prevMobile, self.isMobile);
                            }
                        });
                    }
                } catch (_) {
                    console.warn('[CORE] MediaQuery 지원 안됨 - resize 이벤트로 대체');
                }

                // resize 이벤트 백업 (MediaQuery 미지원 환경)
                window.addEventListener('resize', function() {
                    const wasMobile = self.isMobile;
                    const nowMobile = self.isMobileViewport();
                    if (wasMobile !== nowMobile) {
                        self.isMobile = nowMobile;
                        console.log('[CORE] resize 감지 - 모바일 상태 변경:', wasMobile, '->', nowMobile);
                        
                        // 메뉴 활성화 상태 동적 관리
                        if (Zinidata.page && Zinidata.page.handleResponsiveMenuActivation) {
                            Zinidata.page.handleResponsiveMenuActivation(wasMobile, nowMobile);
                        }
                    }
                });
            }
        },

        // ============================== 페이지 관리 ==============================
        page: {
            current: null,
            initialized: false,
            
            /**
             * 현재 페이지 설정
             * @param {string} pageName - 페이지 이름
             */
            setCurrent: function(pageName) {
                this.current = pageName;
                console.log('[CORE] 현재 페이지 설정:', pageName);
            },

            /**
             * 메뉴 초기화 완료 표시
             */
            markInitialized: function() {
                this.initialized = true;
                console.log('[CORE] 메뉴 초기화 완료');
            },

            /**
             * 반응형 메뉴 활성화 상태 동적 관리
             * @param {boolean} wasMobile - 이전 모바일 상태
             * @param {boolean} nowMobile - 현재 모바일 상태
             */
            handleResponsiveMenuActivation: function(wasMobile, nowMobile) {
                try {
                    const currentPage = this.current;
                    if (!currentPage || !this.initialized) {
                        console.log('[CORE] 페이지 정보 없음 - 메뉴 활성화 건너뜀');
                        return;
                    }

                    console.log('[CORE] 반응형 메뉴 활성화 처리:', {
                        currentPage,
                        wasMobile,
                        nowMobile
                    });

                    if (nowMobile) {
                        // PC → 모바일: PC 메뉴 비활성화
                        $('.headerMenuList ul li').removeClass('active');
                        console.log('[CORE] PC → 모바일: PC 메뉴 비활성화');
                        
                        // Explorer 하위 페이지의 경우 모바일 2depth 메뉴 활성화
                        if (['summary', 'flowpop', 'density', 'markets', 'premium'].includes(currentPage)) {
                            $(`#mo-submenu-${currentPage}`).addClass('active');
                            console.log(`[CORE] 모바일 2depth 메뉴 활성화: mo-submenu-${currentPage}`);
                        }
                    } else {
                        // 모바일 → PC: 모바일 메뉴 비활성화 후 PC 메뉴 활성화
                        $('.headerMenuList ul li').removeClass('active');
                        console.log('[CORE] 모바일 → PC: 모든 메뉴 초기화');
                        
                        // 페이지별 PC 메뉴 활성화
                        switch (currentPage) {
                            case 'home':
                                $('#pc-menu-home').addClass('active');
                                break;
                            case 'pricing':
                                $('#pc-menu-pricing').addClass('active');
                                break;
                            case 'explorer':
                                $('#pc-menu-explorer').addClass('active');
                                break;
                            case 'summary':
                            case 'flowpop':
                            case 'density':
                            case 'markets':
                            case 'premium':
                                $('#pc-menu-explorer').addClass('active');
                                $(`#pc-submenu-${currentPage}`).addClass('active');
                                break;
                        }
                        console.log(`[CORE] PC 메뉴 활성화 완료: ${currentPage}`);
                    }
                } catch (error) {
                    console.error('[CORE] 반응형 메뉴 활성화 오류:', error);
                }
            }
        },

        // ============================== 전역 설정 ==============================
        config: {
            set: function(key, value) {
                // TODO: 구현 예정
            },

            get: function(key) {
                // TODO: 구현 예정
            }
        },

    };

    // ============================== 초기화 ==============================
    function initializeCore() {
        // 디바이스 모듈 초기화
        Zinidata.device.initialize();
        
        // 모바일 브라우저 실제 viewport 높이 설정
        function setViewportHeight() {
            const vh = window.innerHeight * 0.01;
            document.documentElement.style.setProperty('--vh', `${vh}px`);
        }
        
        // 초기 설정
        setViewportHeight();
        
        // 화면 크기 변경 및 회전 시 재계산
        window.addEventListener('resize', setViewportHeight);
        window.addEventListener('orientationchange', setViewportHeight);
        
        // ============================== 핵심 전역 노출 (8개) ==============================
        // 1. 메인 네임스페이스
        // window.Zinidata (이미 위에서 설정됨)
        
        // 2. 디바이스 감지
        window.isMobile = function() { return Zinidata.device.isMobile; };
        
        // 3. API 통신
        window.api = Zinidata.api.bind(Zinidata);
        
        // 4. 알림 시스템
        window.showAlert = Zinidata.showAlert.bind(Zinidata);
        window.showConfirmModal = Zinidata.showConfirmModal.bind(Zinidata);
        window.showAlertModal = Zinidata.showAlertModal.bind(Zinidata);
        
        // 5. 로딩 관리
        window.showLoading = Zinidata.showLoading.bind(Zinidata);
        window.hideLoading = Zinidata.hideLoading.bind(Zinidata);
        window.showOverlayLoading = Zinidata.showOverlayLoading.bind(Zinidata);
        window.hideOverlayLoading = Zinidata.hideOverlayLoading.bind(Zinidata);
        
        // 6. 페이지 관리
        window.setCurrentPage = Zinidata.page.setCurrent.bind(Zinidata.page);
        window.markMenuInitialized = Zinidata.page.markInitialized.bind(Zinidata.page);
        
        // 개발 환경에서만 디버깅 함수 노출
        if (Zinidata.config.debug) {
            window.ZinidataDebug = {
                device: Zinidata.device,
                config: Zinidata.config,
                version: Zinidata.version
            };
        }
    }

    initializeCore();
});