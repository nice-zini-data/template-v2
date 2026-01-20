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
          console.log('click1');
        }else{
          $searchInput.siblings('[data-search-list]').addClass('hidden');
          console.log('click');
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
        console.log('clear');
        
        
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
  
    $(function () {
      App.ui.search.init(document);
      App.ui.pagination.init(document);
    });
  })(window.App = window.App || {}, jQuery);