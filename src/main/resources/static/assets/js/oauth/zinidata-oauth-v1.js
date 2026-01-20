/**
 * 통합 소셜 로그인 관리 모듈
 * 
 * 카카오, 네이버, 구글 등 다양한 소셜 로그인 제공자의 간편 로그인, 회원가입, 계정 연동 기능을 제공합니다.
 * OAuth 2.0 프로토콜을 기반으로 한 소셜 로그인 통합 관리 시스템입니다.
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 * 
 * =============================================
 * 📖 개발자 가이드 - 새로운 소셜 로그인 제공자 추가 방법
 * =============================================
 * 
 * 1. providers 설정에 새로운 소셜 로그인 제공자 추가:
 *    - name: 표시명 (예: '네이버', '구글')
 *    - agreementUrl: 회원가입 완료 API 엔드포인트
 *    - buttonSelector: 동의 버튼의 CSS 선택자
 *    - buttonText: 버튼에 표시될 텍스트
 * 
 * 2. HTML 템플릿 생성:
 *    - /templates/oauth/{provider}Agreement.html 생성 (camelCase 네이밍)
 *    - 버튼 ID는 buttonSelector와 일치해야 함
 *    - 약관 팝업은 Zinidata.oauth.terms.showPopup(1), Zinidata.oauth.terms.showPopup(2) 호출
 *    - 이 파일에 zinidata-oauth-v1.js 스크립트 로드
 * 
 * 3. 백엔드 API 구현:
 *    - /api/oauth/{provider} (소셜 로그인 인증 시작)
 *    - /api/oauth/complete-{provider}-signup (회원가입 완료)
 *    - /api/oauth/{provider}/connect (기존 계정 연동)
 * 
 * 4. URL 경로 규칙:
 *    - 동의 페이지: /oauth/{provider}Agreement (camelCase)
 *    - 자동 감지는 URL 경로의 {provider} 부분으로 수행됨
 * 
 * 예시 - 네이버 소셜 로그인 추가:
 * 1. providers에 추가:
 *    naver: {
 *        name: '네이버',
 *        agreementUrl: '/api/oauth/complete-naver-signup',
 *        buttonSelector: '#naverAgreementBtn',
 *        buttonText: '동의 및 계속하기'
 *    }
 * 
 * 2. HTML 템플릿 생성: /templates/oauth/naverAgreement.html
 *    - 약관 링크: onclick="Zinidata.oauth.terms.showPopup(1)"
 *    - 개인정보 링크: onclick="Zinidata.oauth.terms.showPopup(2)"
 * 3. 백엔드 API 구현: 네이버 소셜 로그인 관련 컨트롤러/서비스
 * 
 * =============================================
 * 🔧 현재 지원 소셜 로그인 제공자
 * =============================================
 * - kakao: 카카오 간편 로그인 (구현됨)
 * - naver: 네이버 간편 로그인 (설정만 준비됨)
 * - google: 구글 간편 로그인 (설정만 준비됨)
 * 
 * =============================================
 * 📝 주의사항
 * =============================================
 * - 모든 소셜 로그인 제공자는 동일한 약관 팝업 시스템 사용
 * - 약관 팝업은 /terms/service, /terms/privacy 경로 사용
 * - HTML에서 직접 Zinidata.oauth.terms.showPopup() 호출 (전역 함수 사용 금지)
 * - 자동 초기화는 URL 경로에 '/oauth/'와 'Agreement' 포함 시 실행
 * - 로딩 처리는 Zinidata.showLoading(), Zinidata.hideLoading() 공통함수 사용
 * - API 호출은 Zinidata.api() 공통함수 사용
 */

$(document).ready(function() {

    // 통합 소셜 로그인 모듈 정의
    Zinidata.oauth = {
        
        // =============================================
        // 🔧 소셜 로그인 제공자별 설정
        // =============================================
        // 
        // 📝 새로운 소셜 로그인 제공자 추가 시 이곳에 설정을 추가하세요.
        // 자세한 가이드는 파일 상단의 개발자 가이드를 참조하세요.
        //
        providers: {
            // ✅ 구현됨: 카카오 간편 로그인
            kakao: {
                name: '카카오',
                agreementUrl: '/api/oauth/complete-kakao-signup',
                buttonSelector: '#kakaoAgreementBtn',
                buttonText: '동의 및 계속하기'
            },
            
            // 🔄 준비됨: 네이버 간편 로그인 (백엔드 API 구현 필요)
            naver: {
                name: '네이버',
                agreementUrl: '/api/oauth/complete-naver-signup',
                buttonSelector: '#naverAgreementBtn',
                buttonText: '동의 및 계속하기'
            },
            
            // 🔄 준비됨: 구글 간편 로그인 (백엔드 API 구현 필요)
            google: {
                name: '구글',
                agreementUrl: '/api/oauth/complete-google-signup',
                buttonSelector: '#googleAgreementBtn',
                buttonText: '동의 및 계속하기'
            }
            
            // 💡 새로운 소셜 로그인 제공자 추가 예시:
            // apple: {
            //     name: 'Apple',
            //     agreementUrl: '/api/oauth/complete-apple-signup',
            //     buttonSelector: '#appleAgreementBtn',
            //     buttonText: '동의 및 계속하기'
            // }
        },
        
        // =============================================
        // 🔑 소셜 로그인 동의 및 회원가입 처리 (공통)
        // =============================================
        agreement: {
            /**
             * 소셜 로그인 동의 페이지 초기화
             */
            init: function(provider) {
                console.log('=== 소셜 로그인 동의 페이지 초기화 ===', provider);
                this.currentProvider = provider || this.detectProvider();
                this.setupEventListeners();
            },
            
            /**
             * 현재 소셜 로그인 제공자 자동 감지
             * 
             * 📝 URL 경로에서 소셜 로그인 제공자를 자동으로 감지합니다.
             * 예: /oauth/kakaoAgreement → 'kakao'
             *     /oauth/naverAgreement → 'naver'
             * 
             * 새로운 소셜 로그인 제공자 추가 시 이 함수도 업데이트해야 합니다.
             */
            detectProvider: function() {
                var path = window.location.pathname;
                if (path.includes('kakao')) return 'kakao';
                if (path.includes('naver')) return 'naver';
                if (path.includes('google')) return 'google';
                // 💡 새로운 소셜 로그인 제공자 추가 시 여기에 조건 추가
                // if (path.includes('apple')) return 'apple';
                
                return 'kakao'; // 기본값
            },
            
            /**
             * 이벤트 리스너 설정
             */
            setupEventListeners: function() {
                var self = this;
                var provider = this.currentProvider;
                var config = Zinidata.oauth.providers[provider];
                
                if (!config) {
                    console.error('지원하지 않는 소셜 로그인 제공자:', provider);
                    return;
                }
                
                // 동의 버튼 클릭 이벤트
                $(config.buttonSelector).off('click').on('click', function() {
                    self.handleAgreement(provider);
                });
                
                console.log(provider + ' 동의 이벤트 리스너 설정 완료');
            },
            
            /**
             * 약관 동의 처리
             */
            handleAgreement: function(provider) {
                console.log('=== 소셜 로그인 약관 동의 처리 시작 ===', provider);
                var self = this;
                var config = Zinidata.oauth.providers[provider];
                const $button = $(config.buttonSelector);
                
                // 버튼 로딩 상태 (공통함수 활용)
                Zinidata.showLoading($button, '처리중');
                
                // 🔥 마케팅 동의 정보 수집
                var requestData = {};
                
                // 카카오 가입 시 마케팅 동의 정보 포함
                if (provider === 'kakao') {
                    requestData.isMarketingAgreed = $('#marketingConsent').is(':checked');
                    requestData.isNewsletterSubscribed = $('#newsletterConsent').is(':checked');
                    
                    console.log('마케팅 동의 정보:', {
                        isMarketingAgreed: requestData.isMarketingAgreed,
                        isNewsletterSubscribed: requestData.isNewsletterSubscribed
                    });
                }
                
                // 약관 동의 및 회원가입 처리
                Zinidata.api({
                    url: config.agreementUrl,
                    method: 'POST',
                    useToken: false,
                    showLoading: false, // 버튼에서 직접 로딩 처리
                    loadingTarget: $button, // 로딩 타겟 지정
                    data: requestData,
                    success: function(response) {
                        console.log('=== 소셜 로그인 회원가입 성공 ===', response);
                        self.handleSuccess(response, provider);
                    },
                    error: function(xhr, status, error) {
                        console.error('=== 소셜 로그인 회원가입 실패 ===', error);
                        self.handleError(xhr, $button, provider);
                    }
                });
            },
            
            /**
             * 성공 처리
             */
            handleSuccess: function(response, provider) {
                console.log('=== 소셜 로그인 회원가입 성공 처리 ===', response);
                var config = Zinidata.oauth.providers[provider];
                const $button = $(config.buttonSelector);
                
                // 로딩 해제
                Zinidata.hideLoading($button);
                
                // 성공 메시지
                Zinidata.showAlert('회원가입이 완료되었습니다!', 'doneGreen');
                
                // 메인 페이지로 이동
                setTimeout(function() {
                    window.location.href = '/';
                }, 1500);
            },
            
            /**
             * 오류 처리
             */
            handleError: function(xhr, $button, provider) {
                console.log('=== 소셜 로그인 회원가입 오류 처리 ===', xhr);
                var config = Zinidata.oauth.providers[provider];
                
                let errorMessage = '회원가입 중 오류가 발생했습니다.';
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    errorMessage = xhr.responseJSON.message;
                }
                
                // 오류 메시지 표시
                Zinidata.showAlert(errorMessage, 'doneRed');
                
                // 버튼 상태 복원 (공통함수 활용)
                Zinidata.hideLoading($button, config.buttonText);
            }
        },
        
        // =============================================
        // 📋 약관 팝업 관리 (공통)
        // =============================================
        terms: {
            /**
             * 약관 팝업 표시
             * @param {number} type - 1: 서비스 약관, 2: 개인정보처리방침
             */
            showPopup: function(type) {
                console.log('=== 약관 팝업 표시 ===', type);
                
                if (type === 1) {
                    // 서비스 약관 팝업
                    window.open('/terms/service', '_blank', 'width=800,height=600,scrollbars=yes,resizable=yes');
                } else if (type === 2) {
                    // 개인정보처리방침 팝업
                    window.open('/terms/privacy', '_blank', 'width=800,height=600,scrollbars=yes,resizable=yes');
                } else {
                    console.warn('알 수 없는 약관 타입:', type);
                }
            }
        },
        
        // =============================================
        // 🔗 카카오 계정 연동 관리
        // =============================================
        linking: {
            /**
             * 카카오 계정 연동 페이지 초기화
             */
            init: function() {
                console.log('=== 카카오 계정 연동 페이지 초기화 ===');
                this.setupEventListeners();
            },
            
            /**
             * 이벤트 리스너 설정
             */
            setupEventListeners: function() {
                var self = this;
                const linkBtn = document.getElementById('linkKakaoAccountBtn');
                
                if (linkBtn) {
                    linkBtn.addEventListener('click', function() {
                        self.handleKakaoLinking();
                    });
                    console.log('카카오 계정 연결 버튼 이벤트 리스너 설정 완료');
                }
            },
            
            /**
             * 카카오 계정 연동 처리
             */
            handleKakaoLinking: function() {
                console.log('=== 카카오 계정 연결 요청 ===');
                
                // HTML data 속성에서 데이터 가져오기
                const linkBtn = document.getElementById('linkKakaoAccountBtn');
                if (!linkBtn) {
                    console.error('카카오 연결 버튼을 찾을 수 없습니다.');
                    return;
                }
                
                var kakaoId = linkBtn.getAttribute('data-kakao-id');
                var existingLoginId = linkBtn.getAttribute('data-existing-login-id');
                
                if (!kakaoId || !existingLoginId) {
                    console.error('카카오 연동 데이터가 없습니다:', { kakaoId, existingLoginId });
                    Zinidata.showAlert('연동에 필요한 정보가 없습니다. 다시 시도해주세요.', 'doneRed');
                    return;
                }
                
                console.log('전송할 데이터 - kakaoId:', kakaoId, 'existingLoginId:', existingLoginId);
                
                // 로딩 상태 표시 (공통함수 활용)
                Zinidata.showLoading(linkBtn, '연결 중...');
                
                // 카카오 계정 연결 API 호출
                var requestData = {
                    kakaoId: kakaoId,
                    existingLoginId: existingLoginId
                };
                console.log('최종 전송 데이터:', requestData);
                
                Zinidata.api({
                    url: '/api/oauth/link/kakao',
                    method: 'POST',
                    showLoading: false, // 수동 로딩 처리
                    loadingTarget: linkBtn, // 로딩 타겟 지정
                    data: requestData,
                    success: function(response) {
                        console.log('카카오 계정 연결 성공:', response);
                        
                        if (response.success) {
                            // 로딩 해제
                            Zinidata.hideLoading(linkBtn);
                            
                            // 성공 메시지 표시 (프로젝트 표준 알림)
                            Zinidata.showAlert('카카오 계정이 성공적으로 연결되었습니다.', 'doneGreen', function() {
                                // 메인 페이지로 리다이렉트
                                window.location.href = '/';
                            });
                        } else {
                            Zinidata.showAlert('계정 연결에 실패했습니다: ' + (response.message || '알 수 없는 오류'), 'doneRed');
                            Zinidata.hideLoading(linkBtn);
                        }
                    },
                    error: function(xhr, status, error) {
                        console.error('카카오 계정 연결 오류:', error);
                        console.error('HTTP 상태:', xhr.status);
                        console.error('응답 텍스트:', xhr.responseText);
                        
                        var errorMessage = '계정 연결 중 오류가 발생했습니다.';
                        try {
                            var response = JSON.parse(xhr.responseText);
                            if (response.message) {
                                errorMessage = response.message;
                            }
                        } catch (e) {
                            console.error('응답 파싱 오류:', e);
                        }
                        
                        Zinidata.showAlert(errorMessage, 'doneRed');
                        Zinidata.hideLoading(linkBtn);
                    }
                });
            }
        },
        
        // =============================================
        // 🔧 유틸리티 기능 (필요시 추가)
        // =============================================
    };
    
    // =============================================
    // 🚀 자동 초기화 (로드 시 바로 실행)
    // =============================================
    
    console.log('통합 소셜 로그인 모듈 로드 완료');
    console.log('=== 소셜 로그인 모듈 자동 초기화 ===');
    
    // 소셜 로그인 동의 페이지 초기화
    Zinidata.oauth.agreement.init();
    
    // 카카오 계정 연동 페이지 초기화
    Zinidata.oauth.linking.init();
    
    // =============================================
    // 📚 개발자 참고사항
    // =============================================
    // 
    // 1. 새로운 소셜 로그인 제공자 추가 시 체크리스트:
    //    ✅ providers 설정에 새 제공자 추가
    //    ✅ detectProvider() 함수에 감지 로직 추가
    //    ✅ HTML 템플릿 생성 (/templates/oauth/{provider}Agreement.html)
    //    ✅ HTML에서 약관 팝업: Zinidata.oauth.terms.showPopup(1), Zinidata.oauth.terms.showPopup(2)
    //    ✅ 백엔드 API 구현 (/api/oauth/complete-{provider}-signup)
    //    ✅ 로그인 페이지에 소셜 로그인 버튼 추가
    // 
    // 2. 테스트 방법:
    //    - 소셜 로그인 동의 페이지 접속: /oauth/{provider}Agreement
    //    - 브라우저 개발자 도구 콘솔에서 로그 확인
    //    - 약관 팝업 정상 동작 확인 (Zinidata.oauth.terms.showPopup 호출)
    //    - 회원가입 완료 후 리다이렉트 확인
    //    - 로딩 상태 표시/해제 확인
    // 
    // 3. 문제 해결:
    //    - 소셜 로그인 제공자가 감지되지 않으면 detectProvider() 확인
    //    - 버튼이 동작하지 않으면 buttonSelector 확인
    //    - API 호출 실패 시 agreementUrl 확인
    //    - 약관 팝업이 안 열리면 Zinidata.oauth.terms.showPopup() 호출 확인
    //    - 로딩이 안 되면 Zinidata.showLoading(), Zinidata.hideLoading() 확인
});
