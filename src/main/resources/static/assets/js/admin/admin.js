$(function () {
    App.ui.adminTable.render('#homeTable', {
        headers: [
          { text: 'No' },
          { text: '번호' },
          { text: '일시' },
          { text: '버튼' },
          { text: '건수' }
        ],
        rows: [
          ['1', '이ㅏ아아아', '2025-03-31 09:28:37', "<button class='lineBtn gray'>133</button>", '1234'],
          ['2', '123', '번호', "<button></button>", '1234건']
        ]
    });

    App.ui.adminTable.render('#homeTable2', {
        headers: [
          { text: 'No' },
          { text: '번호' },
          { text: '일시' },
          { text: '버튼' },
          { text: '건수' }
        ],
        rows: [
          ['1', '123', '2025-03-31 09:28:37', "<button class='lineBtn gray'>233</button>", '1234'],
          ['2', '123', '다다', "<button></button>", '1234건']
        ]
      });
});