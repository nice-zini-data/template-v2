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
      { text: '번호' },
      { text: '일시' },
      { text: '버튼' },
      { text: '건수' }
    ],
    rows: [
      ['1', '이ㅏ아아아', '2025-03-31 09:28:37', "<button class='lineBtn gray'>133</button>", '1234'],
      ['2', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],      
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '1234건', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '1234건', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '번호', "<button></button>", '1234건'],
      ['3', '123', '1234건', "<button></button>", '1234건'],
      ['25', '123', '번호', "<button></button>", '1234건'],
    ],
    // scrollTarget: '.dashboardWrap'
  });

  // =========================
  // 테이블2 (rows를 connect 안에)
  // =========================
  App.ui.connectPagedTable({
    tableSel: '#homeTable2',
    pagingSel: '#homeTable2Pagination',
    pageSize,
    headers: [
      { text: 'No' },
      { text: '번호' },
      { text: '일시' },
      { text: '버튼' },
      { text: '건수' }
    ],
    rows: [
      ['1', '테이블2-AAA', '2025-03-31 09:28:37', "<button class='lineBtn gray'>200</button>", '888'],
      ['2', '테이블2-BBB', '번호', "<button></button>", '999건'],
      ['2', '테이블2-BBB', '번호', "<button></button>", '999건'],
      ['2', '테이블2-BBB', '번호', "<button></button>", '999건'],
      ['2', '테이블2-BBB', '번호', "<button></button>", '999건'],
      ['11', '테이블2-KKK', '번호', "<button></button>", '999건'],
      ['1', '테이블2-AAA', '2025-03-31 09:28:37', "<button class='lineBtn gray'>200</button>", '888'],
      ['2', '테이블2-BBB', '번호', "<button></button>", '999건'],
      ['2', '테이블2-BBB', '번호', "<button></button>", '999건'],
      ['2', '테이블2-BBB', '번호', "<button></button>", '999건'],
      ['2', '테이블2-BBB', '번호', "<button></button>", '999건'],
      ['11', '테이블2-KKK', '번호', "<button></button>", '999건'],
    ]
  });
});
