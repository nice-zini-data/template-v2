(function (App, $) {
    App.ui = App.ui || {};
    
    // =========================
    // Search
    // =========================
    App.ui.search = App.ui.search || {};
    
    function bindClick($searchInput) {        
      if ($searchInput.data('bound')) return;
      $searchInput.data('bound', true);

      $searchInput.on('click.search', '[data-search-input]', function () {
        if($(this).val() !== ''){
          $searchInput.siblings('[data-search-list]').removeClass('hidden');
        }else{
          $searchInput.siblings('[data-search-list]').addClass('hidden');
        }
      });
  
      // $searchInput의 상위 컨테이너(.c-searchbox)를 찾아서 이벤트 위임
      const $container = $searchInput.closest('.c-searchbox');
      
      // submit
      $container.on('click.search', '[data-search-item]', function () {        
        // const text = $(this).find('.keyword').text().trim() || $(this).text().trim();        
        const text = $(this).find('p:first-child').text().trim();
        // input에 값 채우기
        $searchInput.find('[data-search-input]').val(text);
        // clear 버튼 표시
        $searchInput.find('[data-search-clear]').removeClass('hidden');
        // 리스트 닫기
        $container.find('[data-search-list]').addClass('hidden');

        // ✅ submit 이벤트 발생
        $searchInput.trigger('search:submit', {
            key: $searchInput.data('searchKey') || null,
            keyword: text,
            source: 'list'
        });
      });
  
      // clear
      $container.on('click.search', '[data-search-clear]', function () {
        $searchInput.find('[data-search-input]').val('');
        $(this).addClass('hidden');
        $container.find('[data-search-list]').addClass('hidden');
        
        
        $searchInput.trigger('search:clear', {
          key: $searchInput.data('searchKey') || null
        });
      });

      // 타이핑 검색
      $searchInput.on('keyup.search', '[data-search-input]', function () {
        const value = $(this).val();
        if(value !== ''){
          $searchInput.siblings('[data-search-list]').removeClass('hidden');
          $searchInput.find('[data-search-clear]').removeClass('hidden');
        }else{
          $searchInput.siblings('[data-search-list]').addClass('hidden');
          $searchInput.find('[data-search-clear]').addClass('hidden');
        }
      });

      // searchbox 외부 클릭 시 리스트 닫기
      $(document).on('click.search', function (e) {
        if (!$container.is(e.target) && $container.has(e.target).length === 0) {
          $container.find('[data-search-list]').addClass('hidden');
        }
        if($('[data-search-input]').val() === ''){
          $searchInput.find('[data-search-clear]').addClass('hidden');
        }
      });

    }
  
    App.ui.search.init = function (scope) {
      const $scope = scope ? $(scope) : $(document);
      // click과 typing 모두 처리
      $scope.find('[data-search="click"], [data-search="typing"]').each(function () {
        bindClick($(this));
      });
    };

    // =========================
// Table Enhance (네가 만든 것 그대로 사용)
// =========================
App.ui.tableEnhance = App.ui.tableEnhance || {};

// 타입 판별 유틸
function inferCellType(text) {
  const normalized = text.trim();

  // 날짜 + 시간 (공백 포함) -> 좌측 정렬 대상
  const dateTimeLike =
    /^\d{4}[-./]\d{1,2}[-./]\d{1,2}\s+\d{1,2}:\d{2}(:\d{2})?$/.test(normalized);
  if (dateTimeLike) return 'date';

  // 날짜만 / 시간만 -> 좌측 정렬 대상
  const dateLike =
    /^\d{4}[-./]\d{1,2}[-./]\d{1,2}$/.test(normalized) ||
    /^\d{1,2}:\d{2}(:\d{2})?$/.test(normalized);
  if (dateLike) return 'date';

  // 숫자(단위 포함 허용) -> 우측 정렬 대상
  const numberLike =
    /^[-+]?(\d{1,3}(,\d{3})+|\d+)(\.\d+)?/.test(normalized);
  if (numberLike) return 'number';

  return 'text';
}

App.ui.tableEnhance.init = function (scope) {
  const $scope = scope ? $(scope) : $(document);

  $scope.find('table[data-enhance-table]').each(function () {
    const $table = $(this);
    if ($table.data('enhanceBound')) return;
    $table.data('enhanceBound', true);

    // 그룹 헤더 중앙정렬(옵션)
    $table.find('th[data-group-head]').each(function () {
      this.style.textAlign = 'center';
      this.style.verticalAlign = 'middle';
    });

    // 열 기준 정렬
    const columnAlignMap = {};

    // 1) th 기준 선처리: data-align 우선, "No"면 center
    $table.find('thead th').each(function (index) {
      const $th = $(this);
      const thText = $th.text().trim();

      const explicitAlign = $th.data('align');
      if (explicitAlign) {
        columnAlignMap[index] = explicitAlign;
        return;
      }

      if (/^no$/i.test(thText)) {
        columnAlignMap[index] = 'center';
      }
    });

    // 2) button 있는 컬럼은 center
    $table.find('tbody tr').each(function () {
      $(this).children('td').each(function (index) {
        if (columnAlignMap[index]) return;
        if ($(this).find('button').length > 0) {
          columnAlignMap[index] = 'center';
        }
      });
    });

    // 3) td 기준 자동 판별
    $table.find('tbody tr').each(function () {
      $(this).children('td').each(function (index) {
        if (columnAlignMap[index]) return;

        const $td = $(this);
        if ($td.find('a, .tag').length) return;

        const text = $td.text().trim();
        if (!text) return;

        const type = inferCellType(text);
        columnAlignMap[index] = (type === 'number') ? 'right' : 'left';
      });
    });

    // 4) th/td 일괄 적용
    $table.find('tr').each(function () {
      $(this).children('th, td').each(function (index) {
        const align = columnAlignMap[index];
        if (!align) return;

        this.style.textAlign = align;
        this.style.verticalAlign = 'middle';

        if (align === 'right') {
          this.style.fontVariantNumeric = 'tabular-nums';
        }
      });
    });
  });
};


// =========================
// Admin Table (렌더러)
// =========================
App.ui.adminTable = App.ui.adminTable || {};

/**
 * @param {string} selector - '#homeTable'
 * @param {Object} options
 * @param {Array} options.headers - [{text:'No', align?:'left|center|right'}] 또는 ['No','번호',...]
 * @param {Array} options.rows - [['1','123',...], ...]  (cell은 텍스트 or HTML 문자열 가능)
 */
App.ui.adminTable.render = function (selector, options) {
  const $table = $(selector);
  if ($table.length === 0) {
    console.warn('[ADMIN_TABLE] 테이블을 찾을 수 없습니다:', selector);
    return;
  }

  const { headers = [], rows = [] } = options || {};

  // thead/tbody 없으면 생성(안전)
  if ($table.find('thead').length === 0) $table.prepend('<thead></thead>');
  if ($table.find('tbody').length === 0) $table.append('<tbody></tbody>');

  // thead
  const $thead = $table.find('thead');
  $thead.empty();

  if (headers.length > 0) {
    const $tr = $('<tr></tr>');
    headers.forEach(h => {
      const isStr = (typeof h === 'string');
      const text = isStr ? h : (h.text || '');
      const align = isStr ? null : (h.align || null);

      const $th = $('<th></th>').text(text);
      if (align) $th.attr('data-align', align);
      $tr.append($th);
    });
    $thead.append($tr);
  }

  // tbody
  const $tbody = $table.find('tbody');
  $tbody.empty();

  rows.forEach(row => {
    const $tr = $('<tr></tr>');
    (row || []).forEach(cell => {
      const $td = $('<td></td>');
      if (typeof cell === 'string' && cell.trim().startsWith('<')) $td.html(cell);
      else $td.text(cell ?? '');
      $tr.append($td);
    });
    $tbody.append($tr);
  });

  // ✅ 정렬 재적용 (렌더 후 항상 다시 계산)
  $table.removeData('enhanceBound');
  App.ui.tableEnhance.init($table.closest('.tableDataDom').length ? $table.closest('.tableDataDom') : $table);
};


    // =========================
    // Pagination (FINAL)
    // =========================
    window.App = window.App || {};
    App.ui = App.ui || {};
    App.ui.pagination = App.ui.pagination || {};

    (function () {
      const NS = '.pagination';

      function clamp(n, min, max) { return Math.max(min, Math.min(max, n)); }

      function getState($p) {
        return {
          totalPages: Number($p.data('totalPages')) || 1,
          currentPage: Number($p.data('currentPage')) || 1,
          windowSize: Number($p.data('windowSize')) || 5,
        };
      }

      function setState($p, st) {
        $p.data('totalPages', st.totalPages);
        $p.data('currentPage', st.currentPage);
        $p.data('windowSize', st.windowSize);
      }

      // ✅ 페이지 번호 li를 totalPages/currentPage 기준으로 생성(슬라이딩)
      function renderPageItems($p) {
        const st = getState($p);
        const total = st.totalPages;
        const cur = st.currentPage;
        const win = st.windowSize;

        const $ul = $p.find('ul').first();
        if (!$ul.length) return;

        // 기존 번호 li 제거
        $ul.find('li[data-page]').remove();

        // 표시할 구간 계산 (1..total에서 cur 중심으로 win개)
        let start = Math.max(1, cur - Math.floor(win / 2));
        let end = start + win - 1;

        if (end > total) {
          end = total;
          start = Math.max(1, end - win + 1);
        }

        // 삽입 기준: prev/first 다음에 넣고, next/last 앞에 위치
        const $after = $ul.find('li[data-page-btn="prev"]');
        const $before = $ul.find('li[data-page-btn="next"]');

        const items = [];
        for (let p = start; p <= end; p++) {
          const activeCls = (p === cur) ? 'active' : '';
          items.push(`<li data-page="${p}" class="${activeCls}">${p}</li>`);
        }

        // prev 뒤에 넣기
        if ($after.length) {
          $after.after(items.join(''));
        } else if ($before.length) {
          $before.before(items.join(''));
        } else {
          $ul.append(items.join(''));
        }
      }

      // ✅ first/prev/next/last disabled 처리 (총 페이지 기준)
      function updateDisabled($p) {
        const st = getState($p);
        const cur = st.currentPage;
        const total = st.totalPages;

        $p.find('[data-page-btn="first"], [data-page-btn="prev"]')
          .toggleClass('disabled', cur <= 1);

        $p.find('[data-page-btn="next"], [data-page-btn="last"]')
          .toggleClass('disabled', cur >= total);
      }

      function setActive($p, page) {
        $p.find('li[data-page]').removeClass('active');
        $p.find(`li[data-page="${page}"]`).addClass('active');
      }

      // ✅ 내부 공통: page 변경 처리
      function applyPage($p, nextPage) {
        const st = getState($p);
        const page = clamp(nextPage, 1, st.totalPages);

        st.currentPage = page;
        setState($p, st);

        renderPageItems($p);
        setActive($p, page);
        updateDisabled($p);

        // 외부로 알림
        $p.trigger('pagination:page', { page });
      }

      // ✅ 클릭 바인딩 (한 번만)
      function bind($p) {
        if ($p.data('bound')) return;
        $p.data('bound', true);

        $p.off('click' + NS).on('click' + NS, 'li', function (e) {
          const $li = $(this);

          if ($li.hasClass('disabled')) return;

          const st = getState($p);
          const cur = st.currentPage;

          // 번호 클릭
          const page = $li.data('page');
          if (page) {
            applyPage($p, Number(page));
            return;
          }

          // 버튼 클릭
          const action = $li.data('pageBtn');
          if (!action) return;

          if (action === 'first') applyPage($p, 1);
          if (action === 'prev')  applyPage($p, cur - 1);
          if (action === 'next')  applyPage($p, cur + 1);
          if (action === 'last')  applyPage($p, st.totalPages);
        });
      }

      // ✅ init: DOM에 있는 paginationWrap에 이벤트만 걸어줌
      App.ui.pagination.init = function (scope) {
        const $scope = scope ? $(scope) : $(document);
        $scope.find('[data-pagination]').each(function () {
          bind($(this));
        });
      };

      /**
       * connect: 페이지네이션 상태 세팅 + goToPage 연결 + 스크롤 TOP
       * @param {string|Element|jQuery} target - '#homePagination'
       * @param {Object} opt
       * @param {number} opt.totalPages
       * @param {number} [opt.currentPage=1]
       * @param {number} [opt.windowSize=5]  // 화면에 보여줄 페이지 li 개수
       * @param {function} opt.goToPage
       * @param {string|Element|jQuery} [opt.scrollTarget] // 없으면 window
       */
      App.ui.pagination.connect = function (target, opt) {
        const $p = (target instanceof jQuery) ? target : $(target);
        if (!$p.length) {
          console.warn('[PAGINATION] target not found:', target);
          return;
        }

        bind($p); // connect만 호출해도 바인딩 되도록

        const total = Math.max(1, Number(opt?.totalPages) || 1);
        const win = Math.max(1, Number(opt?.windowSize) || 5);
        let cur = Number(opt?.currentPage) || 1;
        cur = clamp(cur, 1, total);

        const goToPage = opt?.goToPage;
        if (typeof goToPage !== 'function') {
          console.warn('[PAGINATION] goToPage 함수가 필요합니다.');
          return;
        }

        setState($p, { totalPages: total, currentPage: cur, windowSize: win });
        renderPageItems($p);
        setActive($p, cur);
        updateDisabled($p);

        // 중복 연결 방지 (id별)
        $p.off('pagination:page.bridge').on('pagination:page.bridge', function (_e, ev) {
          goToPage(ev.page);
          scrollToTop(opt?.scrollTarget);
        });

        return {
          setTotalPages(n) {
            const st = getState($p);
            st.totalPages = Math.max(1, Number(n) || 1);
            st.currentPage = clamp(st.currentPage, 1, st.totalPages);
            setState($p, st);
            renderPageItems($p);
            setActive($p, st.currentPage);
            updateDisabled($p);
          },
          setCurrent(n) { applyPage($p, Number(n) || 1); },
          getCurrent() { return getState($p).currentPage; }
        };
      };

      function scrollToTop(target) {
        if (!target) {
          window.scrollTo({ top: 0, behavior: 'smooth' });
          return;
        }
        const $t = (target instanceof jQuery) ? target : $(target);
        if (!$t.length) {
          window.scrollTo({ top: 0, behavior: 'smooth' });
          return;
        }
        $t.stop().animate({ scrollTop: 0 }, 200);
      }
    })();




    // =========================
    // Calendar
    // =========================

    App.ui.calendar = App.ui.calendar || {};

    function bind($calendar) {
      if ($calendar.data('bound')) return;
    
      if ($calendar.closest('[data-pagination]').length > 0 || $calendar.is('[data-pagination]')) return;
    
      if ($calendar[0]._flatpickr) $calendar[0]._flatpickr.destroy();
    
      $calendar.data('bound', true);
    
      const targetSel = $calendar.data('target') || '#calendarDay';
      const $target = $(targetSel);
    
      const writeSelected = (inst) => {
        const d = inst.selectedDates?.[0];
        if (!d) return;
    
        const ymd = inst.formatDate(d, 'Y-m-d'); // ✅ 로컬 기준 안전
        $target.text(ymd); // input이면 .val(ymd)
      };
    
      const fp = flatpickr($calendar[0], {
        locale: 'ko',
        dateFormat: 'Y년 m월 d일',
        defaultDate: 'today',
        disableMobile: true,
    
        onReady: function (_, __, inst) {
          writeSelected(inst); // ✅ 초기값도 반영
        },
    
        onChange: function (_, __, inst) {
          writeSelected(inst); // ✅ 날짜 클릭할 때마다 반영
        },
      });
    }

    App.ui.calendar.init = function (scope) {
      const $scope = scope ? $(scope) : $(document);
      $scope.find('[data-calendar]').each(function () {
        bind($(this));
      });      
    };

    // =========================
    // Table Enhance
    // =========================

    App.ui.tableEnhance = App.ui.tableEnhance || {};

    // 타입 판별 유틸
    function inferCellType(text) {
      const normalized = text.trim();
    
      // ✅ 날짜 + 시간 (공백 포함)
      const dateTimeLike =
        /^\d{4}[-./]\d{1,2}[-./]\d{1,2}\s+\d{1,2}:\d{2}(:\d{2})?$/.test(normalized);
    
      if (dateTimeLike) return 'date';
    
      // ✅ 날짜만
      const dateLike =
        /^\d{4}[-./]\d{1,2}[-./]\d{1,2}$/.test(normalized) ||
        /^\d{1,2}:\d{2}(:\d{2})?$/.test(normalized);
    
      if (dateLike) return 'date';
    
      // 숫자 (콤마, 소수, 부호, 단위 허용)
      const numberLike =
        /^[-+]?(\d{1,3}(,\d{3})+|\d+)(\.\d+)?/.test(normalized);
    
      if (numberLike) return 'number';
    
      return 'text';
    }

    App.ui.tableEnhance.init = function (scope) {
      const $scope = scope ? $(scope) : $(document);

      $scope.find('table[data-enhance-table]').each(function () {
        const $table = $(this);
        if ($table.data('enhanceBound')) return;
        $table.data('enhanceBound', true);

        // 1) 그룹 헤더(th) 중앙 정렬
        $table.find('th[data-group-head]').each(function () {
          this.style.textAlign = 'center';
          this.style.verticalAlign = 'middle';
        });

        // 2) 자동 정렬 (열 기준)
        const columnAlignMap = {};

        // --- 1단계: th 기준 선처리 ---
        $table.find('thead th').each(function (index) {
          const $th = $(this);
          const text = $th.text().trim();

          // (1) 명시적 data-align 최우선
          const explicitAlign = $th.data('align');
          if (explicitAlign) {
            columnAlignMap[index] = explicitAlign;
            return;
          }

          // (2) "No" 컬럼은 무조건 center
          if (/^no$/i.test(text)) {
            columnAlignMap[index] = 'center';
          }
        });

        // --- 2단계: button 컬럼 감지 → center ---
        $table.find('tbody tr').each(function () {
          $(this).children('td').each(function (index) {
            if (columnAlignMap[index]) return;

            if ($(this).find('button').length > 0) {
              columnAlignMap[index] = 'center';
            }
          });
        });

        // --- 3단계: td 기준 자동 판별 ---
        $table.find('tbody tr').each(function () {
          $(this).children('td').each(function (index) {
            // 이미 정렬이 결정된 컬럼은 스킵
            if (columnAlignMap[index]) return;

            const $td = $(this);

            // 링크/태그 등 복합 콘텐츠는 스킵
            if ($td.find('a, .tag').length) return;

            const text = $td.text().trim();
            if (!text) return;

            const type = inferCellType(text);
            columnAlignMap[index] = (type === 'number') ? 'right' : 'left';
          });
        });

        // --- 4단계: th / td에 일괄 적용 ---
        $table.find('tr').each(function () {
          $(this).children('th, td').each(function (index) {
            const align = columnAlignMap[index];
            if (!align) return;

            this.style.textAlign = align;
            this.style.verticalAlign = 'middle';

            if (align === 'right') {
              this.style.fontVariantNumeric = 'tabular-nums';
            }
          });
        });
      });
    };

    // =========================
    // Admin Table
    // =========================

    App.ui.adminTable = App.ui.adminTable || {};

    /**
     * Admin 테이블 렌더링
     * @param {string} selector - 테이블 선택자 (예: '#homeTable')
     * @param {Object} options - 테이블 옵션
     * @param {Array} options.headers - 헤더 배열 [{ text: 'No' }, ...]
     * @param {Array} options.rows - 행 데이터 배열 [['1', '123', ...], ...]
     */
    App.ui.adminTable.render = function (selector, options) {
      const $table = $(selector);
      if ($table.length === 0) {
        console.warn('[ADMIN_TABLE] 테이블을 찾을 수 없습니다:', selector);
        return;
      }

      const { headers = [], rows = [] } = options || {};

      // thead 렌더링
      const $thead = $table.find('thead');
      $thead.empty();
      
      if (headers.length > 0) {
        const $tr = $('<tr></tr>');
        headers.forEach(header => {
          const text = typeof header === 'string' ? header : (header.text || '');
          const align = header.align || null;
          const $th = $('<th></th>').text(text);
          if (align) {
            $th.attr('data-align', align);
          }
          $tr.append($th);
        });
        $thead.append($tr);
      }

      // tbody 렌더링
      const $tbody = $table.find('tbody');
      $tbody.empty();
      
      rows.forEach(row => {
        const $tr = $('<tr></tr>');
        row.forEach(cell => {
          const $td = $('<td></td>');
          // HTML 문자열인 경우 html()로, 아니면 text()로
          if (typeof cell === 'string' && cell.trim().startsWith('<')) {
            $td.html(cell);
          } else {
            $td.text(cell || '');
          }
          $tr.append($td);
        });
        $tbody.append($tr);
      });

      // tableEnhance 재적용(렌더 후 정렬 규칙 다시 계산)
      $table.removeData('enhanceBound');
      App.ui.tableEnhance.init($table.closest('.tableDataDom').length ? $table.closest('.tableDataDom') : $table);
    };

    // =========================
    // Paged Table Connector (공통 헬퍼)
    // =========================
    
    /**
     * 테이블과 페이지네이션을 연결하는 공통 헬퍼 함수
     * connect + slice + render를 한 번에 처리
     * 
     * @param {Object} cfg - 설정 객체
     * @param {string} cfg.tableSel - 테이블 선택자 (예: '#homeTable')
     * @param {string} cfg.pagingSel - 페이지네이션 선택자 (예: '#homeTablePagination')
     * @param {Array} cfg.headers - 테이블 헤더 배열 [{ text: 'No' }, ...]
     * @param {Array} cfg.rows - 전체 행 데이터 배열 [['1', '123', ...], ...]
     * @param {number} [cfg.pageSize=10] - 페이지당 행 수
     * @param {number} [cfg.currentPage=1] - 현재 페이지
     * @param {number} [cfg.windowSize=5] - 페이지네이션에 표시할 페이지 번호 개수
     * @param {string|Element|jQuery} [cfg.scrollTarget] - 페이지 변경 시 스크롤할 대상
     */
    App.ui.connectPagedTable = function (cfg) {
      const tableSel = cfg.tableSel;
      const pagingSel = cfg.pagingSel;
      const headers = cfg.headers || [];
      const rowsAll = cfg.rows || [];
      const size = cfg.pageSize || 10;

      const totalPages = Math.max(1, Math.ceil(rowsAll.length / size));

      function getPageRows(page) {
        const start = (page - 1) * size;
        return rowsAll.slice(start, start + size);
      }

      function goToPage(page) {
        App.ui.adminTable.render(tableSel, {
          headers,
          rows: getPageRows(page)
        });
      }

      // 최초 렌더
      goToPage(cfg.currentPage || 1);

      // pagination 연결
      App.ui.pagination.connect(pagingSel, {
        totalPages,
        currentPage: cfg.currentPage || 1,
        windowSize: cfg.windowSize || 5,
        goToPage,
        scrollTarget: cfg.scrollTarget
      });
    };

    // =========================
    // Modal
    // =========================
    
    /**
     * 모달 닫기 기능 초기화
     * .modalLayout 내부의 .close 또는 .modalBg 클릭 시 active 클래스 제거
     */
    App.ui.modal = App.ui.modal || {};
    
    App.ui.modal.init = function (scope) {
      const $scope = scope ? $(scope) : $(document);
      
      // 이벤트 위임: .modalLayout 내부의 .close 또는 .modalBg 클릭 시
      $scope.on('click.modal', '.modalLayout .close, .modalLayout .modalBg', function (e) {
        e.stopPropagation();
        const $modalLayout = $(this).closest('.modalLayout');
        $modalLayout.removeClass('active');
      });
    };

    
  
    $(function () {
      App.ui.search.init(document);
      App.ui.pagination.init(document);
      App.ui.calendar.init(document);
      App.ui.tableEnhance.init(document);
      App.ui.modal.init(document);
      
    });
  })(window.App = window.App || {}, jQuery);