/**
 * ============================================
 * 지니데이타 헤더 관리 모듈 (Header Management)
 * ============================================
 * 
 * 🎯 헤더 책임
 * ✅ 메뉴 토글: PC/모바일 메뉴 표시/숨김
 * ✅ 사용자 메뉴: 사용자 박스 드롭다운
 * ✅ 레이어팝업: 모달 닫기 및 페이지 이동
 * ✅ 커스텀 셀렉트: 드롭다운 선택 기능
 * 
 * @author NICE ZiniData 개발팀
 * @since 1.0
 * @refactored 2025.10
 */

$(document).ready(function() {
    // ============================== 헤더 메뉴 토글 ==============================
   $('.headerMenuBtn').on('pointerup', function() {
    $('.header').toggleClass('open');

    if($('.header').hasClass('open')){
       $('.headerMenuOpen').addClass('show').removeClass('hide');
       $('.mapSearchBox').addClass('hidden');
       $('.headerTitle').text('메뉴').removeClass('bakBtn');
    }else{
       $('.headerMenuOpen').removeClass('show').addClass('hide');
       $('.mapSearchBox').removeClass('hidden');
       // home, map 페이지에서는 bakBtn 클래스 추가하지 않음
       const currentPath = window.location.pathname;
       if (currentPath !== '/home' && currentPath !== '/requests/map' && currentPath !== '/') {
           $('.headerTitle').text('').addClass('bakBtn');
       } else {
           $('.headerTitle').text('').removeClass('bakBtn');
       }
    }

   });

   $('.scrollBox').on('wheel scroll', function() {
       if($('.scrollBox').scrollTop() === 0){
           $('header').addClass('border-b-transparent').removeClass('border-b-zinc-800/6')
       }else{
           $('header').addClass('border-b-zinc-800/6').removeClass('border-b-transparent')
       }
   })

   // 이벤트 위임 사용: 동적으로 추가되는 bakBtn에도 이벤트 바인딩
   $(document).on('pointerup', '.headerTitle.bakBtn', function() {
        history.go(-1);
   });

});


 
const stDate = () => {
    $.datepicker.regional['ko'] = {
        prevText: '',
        nextText: '',
        monthNames: ['1월','2월','3월','4월','5월','6월','7월','8월','9월','10월','11월','12월'],
        monthNamesShort: ['1월','2월','3월','4월','5월','6월','7월','8월','9월','10월','11월','12월'],
        dayNames: ['일요일','월요일','화요일','수요일','목요일','금요일','토요일'],
        dayNamesShort: ['일','월','화','수','목','금','토'],
        dayNamesMin: ['일','월','화','수','목','금','토'],
        weekHeader: 'Wk',
        dateFormat: 'yy-mm-dd',
        firstDay: 0,
        isRTL: false,
        showMonthAfterYear: true,
        yearSuffix: '년',
    };
    $.datepicker.setDefaults($.datepicker.regional['ko']);
    
    // 적용
    $('#date').datepicker({
        changeMonth: true,     // 월 셀렉트
        changeYear: true,      // 년 셀렉트
        showButtonPanel: false, // 오늘/닫기 버튼
        showAnim: 'fadeIn',
        minDate: 0,            // 오늘부터 선택 가능 (오늘 이전 날짜 선택 불가)
        beforeShowDay: function(date) {
            // 오늘 이전 날짜는 disabled 처리
            const today = new Date();
            today.setHours(0, 0, 0, 0);
            const currentDate = new Date(date);
            currentDate.setHours(0, 0, 0, 0);
            
            if (currentDate < today) {
                return [false, 'ui-state-disabled', '이전 날짜는 선택할 수 없습니다'];
            }
            return [true, '', ''];
        }
    });
    
    // 모바일에서 키패드가 올라오지 않도록 readonly 속성 추가
    $('#date').attr('readonly', 'readonly');
    
    // 모바일에서 포커스 시 키패드 대신 datepicker 열기
    $('#date').on('focus', function(e) {
        e.preventDefault();
        $(this).blur();
        $(this).datepicker('show');
    });
}