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
        if($(this).val() !== ''){
          $searchInput.siblings('[data-search-list]').removeClass('hidden');
        }else{
          $searchInput.siblings('[data-search-list]').addClass('hidden');
        }
        $searchInput.find('[data-search-clear]').removeClass('hidden');
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
    // Pagination
    // =========================

    App.ui.pagination = App.ui.pagination || {};

    // 처음/끝 페이지 체크 및 disabled 클래스 업데이트
    function updatePaginationState($pagination) {
      const $pageItems = $pagination.find('[data-page]');
      if ($pageItems.length === 0) return;

      // 현재 활성화된 페이지 번호
      const currentPage = Number($pagination.find('li.active').data('page')) || 1;
      
      // 모든 페이지 번호 배열
      const pageNumbers = $pageItems.map(function() {
        return Number($(this).data('page'));
      }).get();
      
      // 처음 페이지와 끝 페이지 찾기
      const firstPage = Math.min(...pageNumbers);
      const lastPage = Math.max(...pageNumbers);
      
      // 처음 페이지인지 확인
      const isFirstPage = currentPage === firstPage;
      // 끝 페이지인지 확인
      const isLastPage = currentPage === lastPage;
      
      // prev, first 버튼 disabled 처리
      $pagination.find('[data-page-btn="prev"], [data-page-btn="first"]').toggleClass('disabled', isFirstPage);
      
      // next, last 버튼 disabled 처리
      $pagination.find('[data-page-btn="next"], [data-page-btn="last"]').toggleClass('disabled', isLastPage);
    }

    function bind($pagination) {
      if ($pagination.data('bound')) return;
      $pagination.data('bound', true);

      // 초기 상태 업데이트
      updatePaginationState($pagination);
      
      $pagination.on('click.pagination', 'li', function (e) {
        const $li = $(this);
        
        // disabled 또는 active 클래스가 있으면 클릭 무시
        if ($li.hasClass('disabled') || $li.hasClass('active')) {
          e.preventDefault();
          e.stopPropagation();
          return false;
        }
  
        let payload = {};
  
        if ($li.data('pageBtn')) {
          payload.action = $li.data('pageBtn'); // first | prev | next | last
        } else if ($li.data('page')) {
          payload.page = Number($li.data('page'));
          // 페이지 번호 클릭 시 active 클래스 업데이트
          $li.addClass('active').siblings().removeClass('active');
          // 상태 업데이트 (처음/끝 페이지 체크)
          updatePaginationState($pagination);
        }
  
        // ✅ 페이지로 알림만 보냄
        $pagination.trigger('pagination:change', payload);
      });
      
      // 외부에서 페이지가 변경될 때도 상태 업데이트
      $pagination.on('pagination:change', function() {
        // active 클래스가 업데이트된 후 상태 체크
        setTimeout(function() {
          updatePaginationState($pagination);
        }, 0);
      });
    }
  
    App.ui.pagination.init = function (scope) {
      const $scope = scope ? $(scope) : $(document);
      $scope.find('[data-pagination]').each(function () {
        bind($(this));
      });
    };


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
        dateFormat: 'Y-m-d',
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
  
    $(function () {
      App.ui.search.init(document);
      App.ui.pagination.init(document);
      App.ui.calendar.init(document);
      App.ui.tableEnhance.init(document);
      
    });
  })(window.App = window.App || {}, jQuery);