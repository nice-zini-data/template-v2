/**
 * ============================================
 * 지니데이타 고급 UI 컴포넌트 (Advanced UI)
 * ============================================
 * 
 * 🎯 고급 UI 책임
 * ✅ 테이블: table.*
 * ✅ 페이지네이션: pagination.*
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 * @refactored 2024.12
 */

$(document).ready(function() {
    // ============================== 모듈 의존성 체크 ==============================
    if (typeof Zinidata === 'undefined' || !Zinidata.device) {
        console.error('[ADVANCED] Core 모듈이 먼저 로드되어야 합니다.');
        return;
    }

    // ============================== 테이블 컴포넌트 ==============================
    Zinidata.table = {
        /**
         * 테이블 생성
         * @param {Object} options - 테이블 옵션
         * @returns {jQuery} 생성된 테이블 요소
         */
        create: function(options) {
            // TODO: 구현 예정
        },

        /**
         * 테이블 데이터 업데이트
         * @param {string} tableId - 테이블 ID
         * @param {Array} data - 업데이트할 데이터
         */
        update: function(tableId, data) {
            // TODO: 구현 예정
        },

        /**
         * 테이블 정렬
         * @param {string} tableId - 테이블 ID
         * @param {number} columnIndex - 정렬할 컬럼 인덱스
         * @param {string} direction - 정렬 방향 ('asc' 또는 'desc', 기본값: 'asc')
         */
        sort: function(tableId, columnIndex, direction) {
            const $table = $('#' + tableId);
            const $tbody = $table.find('tbody');
            const rows = $tbody.find('tr').toArray();
            
            direction = direction || 'asc';
            
            rows.sort(function(a, b) {
                let aVal = $(a).find('td').eq(columnIndex).text().trim();
                let bVal = $(b).find('td').eq(columnIndex).text().trim();
                
                // 숫자 비교
                if (!isNaN(aVal) && !isNaN(bVal)) {
                    aVal = parseFloat(aVal);
                    bVal = parseFloat(bVal);
                }
                
                if (direction === 'asc') {
                    return aVal > bVal ? 1 : (aVal < bVal ? -1 : 0);
                } else {
                    return aVal < bVal ? 1 : (aVal > bVal ? -1 : 0);
                }
            });
            
            $tbody.empty().append(rows);
        },

        /**
         * 테이블 필터링
         * @param {string} tableId - 테이블 ID
         * @param {number} columnIndex - 필터링할 컬럼 인덱스
         * @param {string} filterValue - 필터 값
         */
        filter: function(tableId, columnIndex, filterValue) {
            const $table = $('#' + tableId);
            const $rows = $table.find('tbody tr');
            
            if (!filterValue) {
                $rows.show();
                return;
            }
            
            $rows.each(function() {
                const cellValue = $(this).find('td').eq(columnIndex).text().toLowerCase();
                const show = cellValue.indexOf(filterValue.toLowerCase()) !== -1;
                $(this).toggle(show);
            });
        },

        /**
         * 테이블 행 선택 설정
         * @param {string} tableId - 테이블 ID
         * @param {Object} options - 선택 옵션
         * @param {boolean} options.multiple - 다중 선택 여부 (기본값: false)
         * @param {string} options.checkboxSelector - 체크박스 선택자 (기본값: 'input[type="checkbox"]')
         * @param {string} options.selectedClass - 선택된 행 CSS 클래스 (기본값: 'selected')
         */
        setupRowSelection: function(tableId, options) {
            options = $.extend({
                multiple: false,
                checkboxSelector: 'input[type="checkbox"]',
                selectedClass: 'selected'
            }, options);
            
            const $table = $('#' + tableId);
            
            $table.on('click', 'tbody tr', function() {
                const $row = $(this);
                
                if (!options.multiple) {
                    $table.find('tbody tr').removeClass(options.selectedClass);
                }
                
                $row.toggleClass(options.selectedClass);
                
                // 체크박스 동기화
                const $checkbox = $row.find(options.checkboxSelector);
                if ($checkbox.length) {
                    $checkbox.prop('checked', $row.hasClass(options.selectedClass));
                }
            });
        }
    };

    // ============================== 페이지네이션 컴포넌트 ==============================
    Zinidata.pagination = {
        /**
         * 페이징 UI 생성 (ID 우선 접근)
         * @param {string} containerId - 컨테이너 ID (예: 'ordersPagination')
         * @param {Object} options - 페이징 옵션
         * @param {number} options.totalPages - 전체 페이지 수
         * @param {number} options.currentPage - 현재 페이지 (기본값: 1)
         * @param {number} options.maxButtons - 최대 버튼 수 (기본값: 5)
         * @param {boolean} options.showFirstLast - 첫/마지막 페이지 버튼 표시 (기본값: true)
         * @param {boolean} options.showPrevNext - 이전/다음 버튼 표시 (기본값: true)
         * @param {Function} options.onPageChange - 페이지 변경 콜백 함수
         * @param {string} options.prevText - 이전 버튼 텍스트 (기본값: '')
         * @param {string} options.nextText - 다음 버튼 텍스트 (기본값: '')
         * @param {string} options.firstText - 첫 페이지 버튼 텍스트 (기본값: '')
         * @param {string} options.lastText - 마지막 페이지 버튼 텍스트 (기본값: '')
         */
        create: function(containerId, options) {
            options = $.extend({
                totalPages: 1,
                currentPage: 1,
                maxButtons: 5,
                showFirstLast: true,
                showPrevNext: true,
                onPageChange: null,
                prevText: '',
                nextText: '',
                firstText: '',
                lastText: ''
            }, options);
            
            // ID 우선 접근 (성능 최적화)
            const container = document.getElementById(containerId);
            if (!container) {
                console.warn('[PAGINATION] 컨테이너를 찾을 수 없습니다:', containerId);
                return;
            }
            
            const $container = $(container);
            
            // ul 요소 찾기 또는 생성
            let $ul = $container.find('ul');
            if ($ul.length === 0) {
                $ul = $('<ul></ul>');
                $container.append($ul);
            }
            
            // 페이지네이션 HTML 생성
            const html = this.generatePaginationHTML(options);
            $ul.html(html);
            
            // 이벤트 바인딩
            this.bindEvents($container, options);
        },

        /**
         * 페이지네이션 HTML 생성
         * @param {Object} options - 페이징 옵션
         * @returns {string} HTML 문자열
         */
        generatePaginationHTML: function(options) {
            const { totalPages, currentPage, maxButtons, showFirstLast, showPrevNext, prevText, nextText, firstText, lastText } = options;
            
            if (totalPages <= 1) {
                return '';
            }
            
            const hasPrev = currentPage > 1;
            const hasNext = currentPage < totalPages;
            
            let html = '';
            
            // 첫 페이지 버튼
            if (showFirstLast) {
                html += `<li class="prevAll ${!hasPrev ? 'disabled' : ''}">${firstText}</li>`;
            }
            
            // 이전 버튼
            if (showPrevNext) {
                html += `<li class="prev ${!hasPrev ? 'disabled' : ''}">${prevText}</li>`;
            }
            
            // 페이지 번호들
            const pageRange = Math.floor(maxButtons / 2);
            let startPage = Math.max(1, currentPage - pageRange);
            let endPage = Math.min(totalPages, currentPage + pageRange);
            
            // 5개가 안 될 경우 조정
            if (endPage - startPage < maxButtons - 1) {
                if (startPage === 1) {
                    endPage = Math.min(totalPages, startPage + maxButtons - 1);
                } else if (endPage === totalPages) {
                    startPage = Math.max(1, endPage - maxButtons + 1);
                }
            }
            
            for (let i = startPage; i <= endPage; i++) {
                const activeClass = i === currentPage ? 'active' : '';
                html += `<li class="${activeClass}">${i}</li>`;
            }
            
            // 다음 버튼
            if (showPrevNext) {
                html += `<li class="next ${!hasNext ? 'disabled' : ''}">${nextText}</li>`;
            }
            
            // 마지막 페이지 버튼
            if (showFirstLast) {
                html += `<li class="nextAll ${!hasNext ? 'disabled' : ''}">${lastText}</li>`;
            }
            
            return html;
        },

        /**
         * 이벤트 바인딩
         * @param {jQuery} $container - 컨테이너 요소
         * @param {Object} options - 페이징 옵션
         */
        bindEvents: function($container, options) {
            const self = this;
            
            // 기존 이벤트 제거
            $container.off('click.pagination');
            
            // 새 이벤트 바인딩
            $container.on('click.pagination', 'li', function(e) {
                e.preventDefault();
                
                const $li = $(this);
                if ($li.hasClass('disabled')) return;
                
                const currentPage = options.currentPage;
                const totalPages = options.totalPages;
                let targetPage = null;
                
                if ($li.hasClass('prev')) {
                    targetPage = currentPage - 1;
                } else if ($li.hasClass('next')) {
                    targetPage = currentPage + 1;
                } else if ($li.hasClass('prevAll')) {
                    targetPage = 1;
                } else if ($li.hasClass('nextAll')) {
                    targetPage = totalPages;
                } else if (!$li.hasClass('active') && $li.text().trim()) {
                    const pageNum = parseInt($li.text().trim());
                    if (!isNaN(pageNum)) {
                        targetPage = pageNum;
                    }
                }
                
                if (targetPage && targetPage !== currentPage && targetPage >= 1 && targetPage <= totalPages) {
                    if (options.onPageChange && typeof options.onPageChange === 'function') {
                        options.onPageChange(targetPage);
                    }
                }
            });
        },

        /**
         * 페이징 UI 업데이트
         * @param {string} containerId - 컨테이너 ID
         * @param {Object} options - 업데이트할 옵션
         */
        update: function(containerId, options) {
            const container = document.getElementById(containerId);
            if (!container) return;
            
            const $container = $(container);
            
            // 기존 옵션과 새 옵션 병합
            const currentOptions = $container.data('pagination-options') || {};
            const newOptions = $.extend({}, currentOptions, options);
            
            // HTML 재생성
            const html = this.generatePaginationHTML(newOptions);
            $container.find('ul').html(html);
            
            // 옵션 저장
            $container.data('pagination-options', newOptions);
            
            // 이벤트 재바인딩
            this.bindEvents($container, newOptions);
        },

        /**
         * 특정 페이지로 이동
         * @param {string} containerId - 컨테이너 ID
         * @param {number} page - 이동할 페이지
         */
        goToPage: function(containerId, page) {
            const container = document.getElementById(containerId);
            if (!container) return;
            
            const $container = $(container);
            
            const options = $container.data('pagination-options');
            if (!options) return;
            
            if (page >= 1 && page <= options.totalPages && page !== options.currentPage) {
                if (options.onPageChange && typeof options.onPageChange === 'function') {
                    options.onPageChange(page);
                }
            }
        },

        /**
         * 페이지네이션 제거
         * @param {string} containerId - 컨테이너 ID
         */
        destroy: function(containerId) {
            const container = document.getElementById(containerId);
            if (!container) return;
            
            const $container = $(container);
            
            // 이벤트 제거
            $container.off('click.pagination');
            
            // 데이터 제거
            $container.removeData('pagination-options');
            
            // HTML 제거
            $container.find('ul').empty();
        }
    };

    // ============================== 초기화 ==============================
    function initializeAdvanced() {
        // ============================== 전역 노출 제거 ==============================
        // 모든 함수는 Zinidata 네임스페이스를 통해 접근
        // 예: Zinidata.table.sort(), Zinidata.pagination.create()
    }

    initializeAdvanced();
});