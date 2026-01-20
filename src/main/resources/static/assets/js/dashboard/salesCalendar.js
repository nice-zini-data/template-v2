$(function(){
  calendarCont()
});

const $title = $('.calendarTitle');
const $sheet = $('.monthSheet');
const $list  = $('.monthList');
const dayMap = {
  '2026-01-02': 2853,
  '2026-01-10': 2845,
  '2026-01-15': 2838,
};

const fp = flatpickr('#calendar', {
  inline: true,
  locale: 'ko',
  dateFormat: 'Y-m-d',
  defaultDate: 'today',
  disableMobile: true,

  onReady: (_, __, inst) => syncTitle(inst),

  onDayCreate: function (_, __, inst, dayElem) {
    const d = dayElem.dateObj;
    const key = inst.formatDate(d, 'Y-m-d');
  
    if (dayMap[key]) {
      const extra = document.createElement('span');
      extra.className = 'day-extra';
      extra.textContent = dayMap[key].toLocaleString() + '만'; 
      dayElem.appendChild(extra);
    }
  },

  onChange: (selectedDates, dateStr, inst) => {
    updateCalendarSelectText(inst);
    updateSalesAmount(dateStr, inst);
  },
  onMonthChange: (_, __, inst) => {
    syncTitle(inst);
    updateCalendarSelectText(inst);
  },
  onYearChange:  (_, __, inst) => {
    syncTitle(inst);
    updateCalendarSelectText(inst);
  },
});

function syncTitle(inst) {
  const y = inst.currentYear;
  const m = inst.currentMonth + 1;
  $title.text(`${y}년 ${m}월`);
}

// 바텀시트 열기
$('.monthTrigger').on('click', () => {
  $('.monthSheet').removeClass('hidden');
  $('.dashFilterBox').removeClass('filterDown')
  setTimeout(() => {
      $('body').addClass('overflow-hidden');
      $('.dashFilterBox').addClass('filterUp');
  }, 100);
  buildMonthList(fp);
  openSheet();
});

// 닫기
$('.sheetClose, .sheetDim').on('click', closeSheet);

function openSheet() {
  $sheet.addClass('is-open').attr('aria-hidden', 'false');
  $('body').addClass('noScroll');
}
function closeSheet() {
  $sheet.removeClass('is-open').attr('aria-hidden', 'true');
  $('body').removeClass('noScroll overflow-hidden');
  $('.dashFilterBox').removeClass('filterUp').addClass('filterDown');
  setTimeout(() => {
      $('.dashFilterBoxWrap').addClass('hidden');
  }, 300);
}

// 월 리스트 만들기 (최근 24개월 예시)
function buildMonthList(inst) {
    const monthsBack = 24; // 2년 전(24개월 전)까지
    const now = new Date(); // ✅ '현재 월' 기준
    const start = new Date(now.getFullYear(), now.getMonth(), 1);
  
    $list.empty();
  
    for (let i = 0; i <= monthsBack; i++) { // ✅ 현재월 포함 + 24개월 전까지 (총 25개)
      const d = new Date(start.getFullYear(), start.getMonth() - i, 1);
      const y = d.getFullYear();
      const m = d.getMonth(); // 0~11
  
      const isActive = (y === inst.currentYear && m === inst.currentMonth);
  
      $list.append(`
        <li class="monthItem ${isActive ? 'is-active' : ''}">
          <button type="button" data-y="${y}" data-m="${m}">
            ${y}년 ${m + 1}월
          </button>
        </li>
      `);
    }
    
}
 

// 월 클릭 → flatpickr 월 이동
$list.on('click', 'button', function () {  
  const y = Number($(this).data('y'));
  const m = Number($(this).data('m'));

  fp.changeYear(y);
  fp.changeMonth(m - fp.currentMonth, false); // 상대 이동
  // 날짜도 1일로 맞추고 싶으면:
  fp.setDate(new Date(y, m, 1), true);

  closeSheet();
});


// 요일 배열
const weekdays = ['일', '월', '화', '수', '목', '금', '토'];

// 캘린더 선택 텍스트 업데이트 함수
const updateCalendarSelectText = (inst) => {
  let selectedDate;
  
  // 선택된 날짜가 있으면 그 날짜 사용
  if (inst.selectedDates && inst.selectedDates.length > 0) {
    selectedDate = inst.selectedDates[0];
  } else {
    // 선택된 날짜가 없으면 현재 월의 1일 사용
    selectedDate = new Date(inst.currentYear, inst.currentMonth, 1);
  }
  
  const month = selectedDate.getMonth() + 1;
  const day = selectedDate.getDate();
  const weekday = weekdays[selectedDate.getDay()];
  
  $('.calendarSelectText').text(`${month}월 ${day}일 ${weekday}요일`);
};

// 매출액 업데이트 함수
const updateSalesAmount = (dateStr, inst) => {
  if (!dateStr) {
    // 선택된 날짜가 없으면 0으로 표시
    $('.salesAmount').text('0');
    return;
  }
  
  let dayExtraValue = null;
  
  // 선택된 날짜의 day element 찾기 (flatpickr의 selected 클래스 사용)
  const $selectedDay = $(inst.calendarContainer).find('.flatpickr-day.selected, .flatpickr-day.flatpickr-selected');
  
  if ($selectedDay.length > 0) {
    const $dayExtra = $selectedDay.find('.day-extra');
    if ($dayExtra.length > 0) {
      // "만" 제거하고 숫자만 추출
      dayExtraValue = $dayExtra.text().replace('만', '').replace(/,/g, '');
    }
  }
  
  // 선택된 날짜를 찾지 못했거나 day-extra가 없는 경우, 모든 day element를 순회하여 찾기
  if (!dayExtraValue) {
    $(inst.calendarContainer).find('.flatpickr-day').each(function() {
      const dayDate = $(this).attr('aria-label') || 
                      (this.dateObj ? inst.formatDate(this.dateObj, 'Y-m-d') : null);
      
      if (dayDate === dateStr) {
        const $dayExtra = $(this).find('.day-extra');
        if ($dayExtra.length > 0) {
          dayExtraValue = $dayExtra.text().replace('만', '').replace(/,/g, '');
          return false; // break
        }
      }
    });
  }
  
  // dayMap에서 직접 가져오는 방법도 시도
  if (!dayExtraValue && dayMap[dateStr]) {
    dayExtraValue = dayMap[dateStr].toString();
  }
  
  if (dayExtraValue) {
    $('.salesAmount').text(Number(dayExtraValue).toLocaleString());
  } else {
    $('.salesAmount').text('0');
  }
};

const calendarCont = () => {
  const today = new Date();
  const month = today.getMonth() + 1;
  const day = today.getDate();
  const weekday = weekdays[today.getDay()];
  $('.calendarSelectText').text(`${month}월 ${day}일 ${weekday}요일`);

  const todayStr = today.toISOString().slice(0, 10);
  if (dayMap[todayStr]) {
    $('.salesAmount').text(dayMap[todayStr].toLocaleString());
  } else {
    $('.salesAmount').text('0');
  }
}