/**
 * ============================================
 * 지니데이타 위치 검색 모듈 (Location Module)
 * ============================================
 * 
 * 🎯 위치 검색 책임
 * ✅ 주소 검색: searchAddress()
 * ✅ 키워드 검색: searchKeyword()
 * ✅ 통합 검색: searchMixed()
 * ✅ 자동완성: initAutocomplete()
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 * @refactored 2024.12
 */

$(document).ready(function() {
    // ============================== 모듈 의존성 체크 ==============================
    if (typeof Zinidata === 'undefined' || !Zinidata.device) {
        console.error('[LOCATION] Core 모듈이 먼저 로드되어야 합니다.');
        return;
    }

    // ============================== 위치 검색 모듈 ==============================
    Zinidata.location = {
        
        /**
         * 주소 검색
         * @param {string} query - 검색어
         * @param {Object} options - 옵션 설정
         * @param {Function} options.success - 성공 콜백
         * @param {Function} options.error - 실패 콜백
         */
        searchAddress: function(query, options) {
            options = $.extend({
                success: null,
                error: null
            }, options);
            
            Zinidata.api({
                url: '/api/common/location/search/address',
                method: 'GET',
                data: { query: query },
                success: function(response) {
                    if (options.success) {
                        options.success(response);
                    }
                },
                error: function(xhr, status, error) {
                    console.error('[LOCATION] 주소 검색 실패:', error);
                    if (options.error) {
                        options.error(xhr, status, error);
                    }
                }
            });
        },
        
        /**
         * 키워드 검색
         * @param {string} query - 검색어
         * @param {Object} options - 옵션 설정
         * @param {Function} options.success - 성공 콜백
         * @param {Function} options.error - 실패 콜백
         */
        searchKeyword: function(query, options) {
            options = $.extend({
                success: null,
                error: null
            }, options);
            
            Zinidata.api({
                url: '/api/common/location/search/keyword',
                method: 'GET',
                data: { query: query },
                success: function(response) {
                    if (options.success) {
                        options.success(response);
                    }
                },
                error: function(xhr, status, error) {
                    console.error('[LOCATION] 키워드 검색 실패:', error);
                    if (options.error) {
                        options.error(xhr, status, error);
                    }
                }
            });
        },
        
        /**
         * 통합 검색 (주소 + 키워드)
         * @param {string} query - 검색어
         * @param {Object} options - 옵션 설정
         * @param {Function} options.success - 성공 콜백
         * @param {Function} options.error - 실패 콜백
         */
        searchMixed: function(query, options) {
            options = $.extend({
                success: null,
                error: null
            }, options);
            
            Zinidata.api({
                url: '/api/common/location/search/mixed',
                method: 'GET',
                data: { query: query },
                success: function(response) {
                    if (options.success) {
                        options.success(response);
                    }
                },
                error: function(xhr, status, error) {
                    console.error('[LOCATION] 통합 검색 실패:', error);
                    if (options.error) {
                        options.error(xhr, status, error);
                    }
                }
            });
        },
        
        /**
         * 자동완성 컴포넌트 초기화
         * @param {string} inputId - 입력 필드 ID
         * @param {string} suggestionsId - 제안 목록 컨테이너 ID
         * @param {Object} options - 설정 옵션
         * @param {string} options.searchType - 검색 타입 ('address', 'keyword', 'mixed')
         * @param {number} options.minLength - 최소 입력 길이
         * @param {number} options.debounceTime - 디바운스 시간
         * @param {number} options.maxResults - 최대 결과 수
         * @param {Function} options.onSelect - 선택 콜백
         * @param {Function} options.onClear - 초기화 콜백
         * @param {Function} options.formatResult - 결과 포맷 함수
         */
        initAutocomplete: function(inputId, suggestionsId, options) {
            options = $.extend({
                searchType: 'mixed', // 'address', 'keyword', 'mixed'
                minLength: 2,
                debounceTime: 300,
                maxResults: 8,
                onSelect: null,
                onClear: null,
                formatResult: null
            }, options);
            
            const $input = $('#' + inputId);
            const $suggestions = $('#' + suggestionsId);
            let debounceTimer = null;
            let selectedItem = null;
            
            
            // 입력 이벤트
            $input.on('input', function() {
                const query = $(this).val().trim();
                selectedItem = null;
                
                if (options.onClear) {
                    options.onClear();
                }
                
                clearTimeout(debounceTimer);
                
                if (query.length >= options.minLength) {
                    debounceTimer = setTimeout(function() {
                        performSearch(query);
                    }, options.debounceTime);
                } else {
                    hideSuggestions();
                }
            });
            
            // 포커스 이벤트 (기존 결과 복원)
            $input.on('focus', function() {
                const query = $(this).val().trim();
                if (query.length >= options.minLength && $suggestions.children().length > 0) {
                    showSuggestions();
                }
            });
            
            // 검색 실행
            function performSearch(query) {
                let searchMethod;
                switch (options.searchType) {
                    case 'address':
                        searchMethod = Zinidata.location.searchAddress;
                        break;
                    case 'keyword':
                        searchMethod = Zinidata.location.searchKeyword;
                        break;
                    default:
                        searchMethod = Zinidata.location.searchMixed;
                }
                
                searchMethod(query, {
                    success: function(response) {
                        if (response.success && response.data) {
                            displaySuggestions(response.data, query);
                        } else {
                            hideSuggestions();
                        }
                    },
                    error: function() {
                        hideSuggestions();
                    }
                });
            }
            
            // 제안 목록 표시
            function displaySuggestions(data, query) {
                $suggestions.empty();
                
                let results = [];
                
                // 데이터 타입에 따른 결과 처리
                if (data.results) {
                    // 단일 검색 결과
                    results = data.results;
                } else if (data.addressResults || data.keywordResults) {
                    // 통합 검색 결과
                    if (data.addressResults) {
                        results = results.concat(data.addressResults.map(function(item) {
                            item._searchType = 'address';
                            return item;
                        }));
                    }
                    if (data.keywordResults) {
                        results = results.concat(data.keywordResults.map(function(item) {
                            item._searchType = 'keyword';
                            return item;
                        }));
                    }
                }
                
                if (results.length === 0) {
                    hideSuggestions();
                    return;
                }
                
                // 최대 결과 수 제한
                results = results.slice(0, options.maxResults);
                
                results.forEach(function(item) {
                    const $item = createSuggestionItem(item, query);
                    $suggestions.append($item);
                });
                
                showSuggestions();
            }
            
            // 제안 항목 생성
            function createSuggestionItem(item, query) {
                const $item = $('<div class="px-4 py-3 cursor-pointer text-sm text-gray-900 border-b border-gray-100 hover:bg-gray-50"></div>');
                
                let content;
                if (options.formatResult) {
                    content = options.formatResult(item, query);
                } else {
                    content = formatDefaultResult(item);
                }
                
                $item.html(content);
                
                $item.on('click', function() {
                    selectItem(item);
                });
                
                return $item;
            }
            
            // 기본 결과 포맷
            function formatDefaultResult(item) {
                const name = item.place_name || item.address_name || '';
                const address = item.road_address_name || item.address_name || '';
                const category = item.category_name || '';
                const searchType = item._searchType || '';
                
                let html = '<div class="font-medium">' + name + '</div>';
                
                if (address && address !== name) {
                    html += '<div class="text-xs text-gray-500 mt-0.5">' + address + '</div>';
                }
                
                if (category) {
                    html += '<div class="text-xs text-blue-500 mt-0.5">' + category + '</div>';
                }
                
                if (searchType) {
                    const typeLabel = searchType === 'address' ? '주소' : '장소';
                    html += '<span class="inline-block bg-gray-100 text-gray-600 text-xs px-2 py-0.5 rounded mt-1">' + typeLabel + '</span>';
                }
                
                return html;
            }
            
            // 항목 선택
            function selectItem(item) {
                const displayName = item.place_name || item.address_name || '';
                $input.val(displayName);
                selectedItem = item;
                hideSuggestions();
                
                if (options.onSelect) {
                    options.onSelect(item);
                }
            }
            
            // 제안 목록 표시
            function showSuggestions() {
                $suggestions.removeClass('hidden');
            }
            
            // 제안 목록 숨김
            function hideSuggestions() {
                $suggestions.addClass('hidden');
            }
            
            // 외부 클릭 시 숨기기
            $(document).on('click', function(e) {
                if (!$input.is(e.target) && !$suggestions.is(e.target) && $suggestions.has(e.target).length === 0) {
                    hideSuggestions();
                }
            });
            
            // 선택된 항목 반환 메서드
            $input.data('getSelectedItem', function() {
                return selectedItem;
            });
            
            // 선택 항목 초기화 메서드
            $input.data('clearSelection', function() {
                selectedItem = null;
                if (options.onClear) {
                    options.onClear();
                }
            });
        }
    };

    // ============================== 초기화 ==============================
    function initializeLocation() {
        // ============================== 전역 노출 제거 ==============================
        // 모든 함수는 Zinidata 네임스페이스를 통해 접근
        // 예: Zinidata.location.searchAddress(), Zinidata.location.initAutocomplete()
        
        // 개발 환경에서만 디버깅 함수 노출
        if (Zinidata.config && Zinidata.config.debug) {
            window.ZinidataDebug = window.ZinidataDebug || {};
            window.ZinidataDebug.location = Zinidata.location;
        }
    }

    initializeLocation();
}); 