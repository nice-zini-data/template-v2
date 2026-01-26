$(function () {
  const pageSize = 10;

  App.ui.pagination.init(document);
  
  
  // =========================
  // 테이블1 (rows를 connect 안에)
  // =========================
  App.ui.connectPagedTable({
    tableSel: '#homeTable',
    pagingSel: '#homeTablePagination',
    pageSize,
    headers: [
      { text: 'No' },
      { text: '로그인ID' },
      { text: '회원 권한' },
      { text: '이름' },
      { text: '이메일' },
      { text: '전화번호' },
      { text: '가입일' },
      { text: '최근접속일' },
      { text: '다운로드 권한' },
      { text: '계정승인' },
      { text: '관리' },
    ],
    rows: [
      ['1', 'nicezinidata', '김지니', 'nice@nice.co.kr', '01012341234', '2025-03-31 09:28:37', '2025-03-31 09:28:37', '일반 사용자', '<div class="toggleBtn"></div>',  '<button class="bgBtn black w-[60px]">승인</button>', '<button class="lineBtn gray size-7"><img src="/assets/images/icons/manage_icon.svg" alt=""></button>' ],
      ['2', 'nicezinidata', '김지니', 'nice@nice.co.kr', '01012341234', '2025-03-31 09:28:37', '2025-03-31 09:28:37', '일반 사용자', '<div class="toggleBtn active"></div>',  '<button class="bgBtn gray w-[60px]">승인취소</button>', '<button class="lineBtn gray size-7"><img src="/assets/images/icons/manage_icon.svg" alt=""></button>' ],
      ['3', 'nicezinidata', '김지니', 'nice@nice.co.kr', '01012341234', '2025-03-31 09:28:37', '2025-03-31 09:28:37', '일반 사용자', '<div class="toggleBtn"></div>',  '<button class="bgBtn black w-[60px]">승인</button>', '<button class="lineBtn gray size-7"><img src="/assets/images/icons/manage_icon.svg" alt=""></button>' ],
      ['4', 'nicezinidata', '김지니', 'nice@nice.co.kr', '01012341234', '2025-03-31 09:28:37', '2025-03-31 09:28:37', '일반 사용자', '<div class="toggleBtn active"></div>',  '<button class="bgBtn black w-[60px]">승인</button>', '<button class="lineBtn gray size-7"><img src="/assets/images/icons/manage_icon.svg" alt=""></button>' ],
      ['5', 'nicezinidata', '김지니', 'nice@nice.co.kr', '01012341234', '2025-03-31 09:28:37', '2025-03-31 09:28:37', '일반 사용자', '<div class="toggleBtn"></div>',  '<button class="bgBtn black w-[60px]">승인</button>', '<button class="lineBtn gray size-7"><img src="/assets/images/icons/manage_icon.svg" alt=""></button>' ],
      ['6', 'nicezinidata', '김지니', 'nice@nice.co.kr', '01012341234', '2025-03-31 09:28:37', '2025-03-31 09:28:37', '일반 사용자', '<div class="toggleBtn active"></div>',  '<button class="bgBtn black w-[60px]">승인</button>', '<button class="lineBtn gray size-7"><img src="/assets/images/icons/manage_icon.svg" alt=""></button>' ],
      ['7', 'nicezinidata', '김지니', 'nice@nice.co.kr', '01012341234', '2025-03-31 09:28:37', '2025-03-31 09:28:37', '일반 사용자', '<div class="toggleBtn"></div>',  '<button class="bgBtn black w-[60px]">승인</button>', '<button class="lineBtn gray size-7"><img src="/assets/images/icons/manage_icon.svg" alt=""></button>' ],
      ['8', 'nicezinidata', '김지니', 'nice@nice.co.kr', '01012341234', '2025-03-31 09:28:37', '2025-03-31 09:28:37', '일반 사용자', '<div class="toggleBtn active"></div>',  '<button class="bgBtn black w-[60px]">승인</button>', '<button class="lineBtn gray size-7"><img src="/assets/images/icons/manage_icon.svg" alt=""></button>' ],
      ['9', 'nicezinidata', '김지니', 'nice@nice.co.kr', '01012341234', '2025-03-31 09:28:37', '2025-03-31 09:28:37', '일반 사용자', '<div class="toggleBtn"></div>',  '<button class="bgBtn black w-[60px]">승인</button>', '<button class="lineBtn gray size-7"><img src="/assets/images/icons/manage_icon.svg" alt=""></button>' ],
      ['10', 'nicezinidata', '김지니', 'nice@nice.co.kr', '01012341234', '2025-03-31 09:28:37', '2025-03-31 09:28:37', '일반 사용자', '<div class="toggleBtn active"></div>',  '<button class="bgBtn black w-[60px]">승인</button>', '<button class="lineBtn gray size-7"><img src="/assets/images/icons/manage_icon.svg" alt=""></button>' ],
      ['11', 'nicezinidata', '김지니', 'nice@nice.co.kr', '01012341234', '2025-03-31 09:28:37', '2025-03-31 09:28:37', '일반 사용자', '<div class="toggleBtn"></div>',  '<button class="bgBtn black w-[60px]">승인</button>', '<button class="lineBtn gray size-7"><img src="/assets/images/icons/manage_icon.svg" alt=""></button>' ],
    ],
    // scrollTarget: '.dashboardWrap'
  });


  searchChange();
  toggleChange();
});


const searchChange = () => {
  $('[data-search-input]').attr('placeholder', '검색어를 입력해 주세요.');
}

const toggleChange = () => {
  $('.toggleBtn').on('click', function () {
    $(this).toggleClass('active');
  });
}