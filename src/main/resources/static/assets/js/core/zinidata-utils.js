/**
 * ============================================
 * 지니데이타 유틸리티 (Utilities)
 * ============================================
 * 
 * 🎯 유틸리티 책임
 * ✅ 날짜/시간: date.*
 * ✅ 문자열 처리: string.*
 * ✅ 성능 최적화: performance.*
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 * @refactored 2024.12
 */

$(document).ready(function() {
    // ============================== 모듈 의존성 체크 ==============================
    if (typeof Zinidata === 'undefined' || !Zinidata.device) {
        console.error('[UTILS] Core 모듈이 먼저 로드되어야 합니다.');
        return;
    }

    // ============================== 날짜/시간 유틸리티 ==============================
    Zinidata.date = {
        /**
         * 날짜 형식화
         * @param {Date|string} date - 형식화할 날짜
         * @param {string} format - 출력 형식 (기본값: 'YYYY-MM-DD')
         * @returns {string} 형식화된 날짜 문자열
         */
        format: function(date, format) {
            format = format || 'YYYY-MM-DD';
            const d = date instanceof Date ? date : new Date(date);
            
            const year = d.getFullYear();
            const month = String(d.getMonth() + 1).padStart(2, '0');
            const day = String(d.getDate()).padStart(2, '0');
            const hours = String(d.getHours()).padStart(2, '0');
            const minutes = String(d.getMinutes()).padStart(2, '0');
            const seconds = String(d.getSeconds()).padStart(2, '0');
            
            return format
                .replace('YYYY', year)
                .replace('MM', month)
                .replace('DD', day)
                .replace('HH', hours)
                .replace('mm', minutes)
                .replace('ss', seconds);
        },

        /**
         * 날짜 문자열 파싱
         * @param {string} dateString - 파싱할 날짜 문자열
         * @returns {Date} Date 객체
         */
        parse: function(dateString) {
            return new Date(dateString);
        },

        /**
         * 날짜 범위 정보 계산
         * @param {Date|string} startDate - 시작 날짜
         * @param {Date|string} endDate - 종료 날짜
         * @returns {Object} 날짜 범위 정보
         */
        getRange: function(startDate, endDate) {
            const start = startDate instanceof Date ? startDate : new Date(startDate);
            const end = endDate instanceof Date ? endDate : new Date(endDate);
            const diffTime = Math.abs(end - start);
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
            
            return {
                days: diffDays,
                startDate: start,
                endDate: end
            };
        },

        /**
         * 근무일 여부 확인
         * @param {Date|string} date - 확인할 날짜
         * @returns {boolean} 근무일 여부
         */
        isWorkingDay: function(date) {
            const d = date instanceof Date ? date : new Date(date);
            const dayOfWeek = d.getDay();
            return dayOfWeek !== 0 && dayOfWeek !== 6; // 0: 일요일, 6: 토요일
        },

        /**
         * 상대적 시간 표시 (예: 3일 전, 2시간 후)
         * @param {Date|string} date - 기준 날짜
         * @returns {string} 상대적 시간 문자열
         */
        getRelativeTime: function(date) {
            // TODO: 구현 예정
        },

        /**
         * 년월 포맷팅 (yyyymm -> yy년 mm월)
         * @param {string} yyyymm - YYYYMM 형식의 년월 문자열
         * @returns {string} 포맷팅된 년월 문자열 (예: 202412 -> 24년 12월)
         */
        formatYearMonth: function(yyyymm) {
            if (!yyyymm || yyyymm.length !== 6) {
                return yyyymm;
            }
            
            const year = yyyymm.substring(0, 4);
            const month = yyyymm.substring(4, 6);
            
            // 년도는 뒤 2자리만 사용
            const shortYear = year.substring(2, 4);
            
            return `${shortYear}년 ${month}월`;
        }
    };

    // ============================== 문자열 처리 유틸리티 ==============================
    Zinidata.string = {
        /**
         * 민감한 정보 마스킹
         * @param {string} value - 마스킹할 값
         * @param {string} type - 마스킹 타입 (email, phone, password, default)
         * @returns {string} 마스킹된 문자열
         */
        maskSensitive: function(value, type) {
            if (!value) return value;
            
            switch (type) {
                case 'email':
                    const parts = value.split('@');
                    if (parts.length === 2) {
                        const name = parts[0];
                        const domain = parts[1];
                        const maskedName = name.length > 2 ? 
                            name.substring(0, 2) + '*'.repeat(name.length - 2) : 
                            name;
                        return maskedName + '@' + domain;
                    }
                    return value;
                    
                case 'phone':
                    const cleaned = value.replace(/[^0-9]/g, '');
                    if (cleaned.length === 11) {
                        return cleaned.substring(0, 3) + '****' + cleaned.substring(7);
                    }
                    return value;
                    
                case 'password':
                    return '****';
                    
                default:
                    const len = value.length;
                    if (len <= 2) return value;
                    return value.substring(0, 2) + '*'.repeat(len - 2);
            }
        },

        /**
         * 통화 형식으로 포맷팅
         * @param {number|string} amount - 금액
         * @param {string} symbol - 통화 기호 (기본값: '원')
         * @returns {string} 포맷팅된 통화 문자열
         */
        formatCurrency: function(amount, symbol) {
            symbol = symbol || '원';
            if (typeof amount !== 'number') {
                amount = parseFloat(amount) || 0;
            }
            return amount.toLocaleString() + symbol;
        },

        /**
         * 텍스트 자르기
         * @param {string} text - 자를 텍스트
         * @param {number} maxLength - 최대 길이
         * @param {string} suffix - 접미사 (기본값: '...')
         * @returns {string} 잘린 텍스트
         */
        truncate: function(text, maxLength, suffix) {
            suffix = suffix || '...';
            if (text.length <= maxLength) return text;
            return text.substring(0, maxLength - suffix.length) + suffix;
        },

        /**
         * HTML 태그 제거
         * @param {string} html - HTML 문자열
         * @returns {string} 순수 텍스트
         */
        removeHtml: function(html) {
            const tmp = document.createElement('div');
            tmp.innerHTML = html;
            return tmp.textContent || tmp.innerText || '';
        },

        /**
         * 고유 ID 생성
         * @param {string} prefix - ID 접두사 (기본값: 'id')
         * @returns {string} 고유 ID
         */
        generateId: function(prefix) {
            prefix = prefix || 'id';
            return prefix + '_' + Math.random().toString(36).substr(2, 9) + '_' + Date.now();
        }
    };

    // ============================== 성능 최적화 유틸리티 ==============================
    Zinidata.performance = {
        /**
         * 디바운스 함수 (연속 호출을 제한하여 마지막 호출만 실행)
         * @param {Function} func - 실행할 함수
         * @param {number} wait - 대기 시간 (밀리초)
         * @param {boolean} immediate - 즉시 실행 여부 (기본값: false)
         * @returns {Function} 디바운스된 함수
         */
        debounce: function(func, wait, immediate) {
            let timeout;
            return function() {
                const context = this, args = arguments;
                const later = function() {
                    timeout = null;
                    if (!immediate) func.apply(context, args);
                };
                const callNow = immediate && !timeout;
                clearTimeout(timeout);
                timeout = setTimeout(later, wait);
                if (callNow) func.apply(context, args);
            };
        },

        /**
         * 스로틀링 함수 (일정 시간 간격으로만 실행)
         * @param {Function} func - 실행할 함수
         * @param {number} limit - 제한 시간 (밀리초)
         * @returns {Function} 스로틀링된 함수
         */
        throttle: function(func, limit) {
            let inThrottle;
            return function() {
                const args = arguments;
                const context = this;
                if (!inThrottle) {
                    func.apply(context, args);
                    inThrottle = true;
                    setTimeout(function() { inThrottle = false; }, limit);
                }
            };
        },

        /**
         * 메모이제이션 함수 (결과 캐싱)
         * @param {Function} func - 메모이제이션할 함수
         * @param {Function} keyGenerator - 캐시 키 생성 함수
         * @returns {Function} 메모이제이션된 함수
         */
        memoize: function(func, keyGenerator) {
            // TODO: 구현 예정
        }
    };

    // ============================== 초기화 ==============================
    function initializeUtils() {
        // ============================== 전역 노출 제거 ==============================
        // 모든 함수는 Zinidata 네임스페이스를 통해 접근
        // 예: Zinidata.date.format(), Zinidata.string.maskSensitive()
        
        // 개발 환경에서만 디버깅 함수 노출
        if (Zinidata.config && Zinidata.config.debug) {
            window.ZinidataDebug = window.ZinidataDebug || {};
            window.ZinidataDebug.date = Zinidata.date;
            window.ZinidataDebug.string = Zinidata.string;
            window.ZinidataDebug.performance = Zinidata.performance;
        }
    }

    initializeUtils();
});