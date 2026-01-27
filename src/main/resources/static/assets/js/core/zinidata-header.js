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

$(function(){
    userMenu();
    headerGubun();
});

//사용자 메뉴 토글
const userMenu = () => {
    $('.userBtn').on('click', function() {
        $('.userMenu').slideToggle('500');
    });
}

const headerGubun = () => {
    const header = $('header').parent();
    if(header.hasClass('mapHeader')){
        header.parent().addClass('menuHeader');
    }else{
        header.parent().removeClass('menuHeader');
    }
}