$(function(){

    initMap();
    mapSearch();
    mapHandle();
    sheetTab();
    copyBtn();
    sheetChange();
    sheetAcceptModal();
    stDate();

    backCall();
    
});

const initMap = async () => {

    if (Zinidata.map && Zinidata.map.init) {
        try {
            // 지도 초기화 완료 대기
            await Zinidata.map.init({
                pageType: 'map',                // 페이지 타입 ('map' 등)
                enableMouseTracking: false,     // 마우스 이동 추적 비활성화
                enableAdmiDisplay: false,       // 행정동 경계 표시 비활성화
                enableClickToDraw: false,       // 지도 클릭 시 행정동 그리기 비활성화
                useCustomControls: false,
                zoomControl: false,
                mapTypeControl: false,
                mapDataControl: false,
                zoom: 14,       // 초기 줌 레벨
                minZoom: 6, // 최소 줌 레벨
                maxZoom: 21, // 최대 줌 레벨
                debounceTime: 1,                // 마우스 이벤트 디바운스 시간 (ms)
                requestInterval: 1,             // API 요청 최소 간격 (ms)
                customEvents: ['dragend', 'zoom_changed'], // 커스텀 이벤트 배열
            });

            // 지도 초기화 완료 후 실행되는 코드
            console.log('[MAP] 지도 초기화 완료');
            console.log(Zinidata.map.map.getBounds());

            // 관성 드래깅 활성화
            Zinidata.map.map.setOptions("disableKineticPan", false);

            // 지도 요청 탐색 호출
            callRequestMapApi();

            // 지도 드래그 종료 이벤트 추가
            setupMapEvents();

        } catch (error) {
            console.error('[MAP] 지도 초기화 실패:', error);
        }
    } else {
        // map-common.js가 아직 로드되지 않았으면 재시도
        setTimeout(initMap, 100);
    }
}

const setupMapEvents = () => {
    const map = Zinidata.map.map;
    if (!map) {
        console.warn('[MAP] 지도 객체가 없습니다.');
        return;
    }
    // 지도 드래그 종료 이벤트
    naver.maps.Event.addListener(map, 'dragend', function() {
        console.log('[MAP] 지도 드래그 종료');
        // API 호출
        callRequestMapApi();
    });
    // 줌 변경 이벤트
    naver.maps.Event.addListener(map, 'zoom_changed', function() {
        console.log('[MAP] 줌 변경:', map.getZoom());
        // API 호출
        callRequestMapApi();
    });
}

const mapSearch = () => {
    $('.mapSearchBox').on('pointerup', function(){
        $('.mapSearchHeader').addClass('active');
        $('.mapSearchForm').removeClass('pointer-events-none');
        $('.mapSearchList, .addressNull').removeClass('hidden');
        $('.mapSearchInputRemove').addClass('hidden');
        $('.mapSearchInput').val('').focus();
    });

    $('.mapSearchInput').on('keyup', function(){
        if($(this).val() === ''){
            $('.mapSearchInputRemove').addClass('hidden');
            $('.mapSearchListUl').addClass('hidden');
            $('.addressNull').removeClass('hidden');
        }else{
            $('.mapSearchListUl').removeClass('hidden');
            $('.mapSearchInputRemove').removeClass('hidden');
            $('.addressNull').addClass('hidden');
        }
    });

    $('.mapSearchInputRemove').on('pointerup', function(){
        $('.mapSearchInput').val('');
        $('.mapSearchListUl, .mapSearchInputRemove').addClass('hidden');
        $('.addressNull').removeClass('hidden');
    });

    $('.mapSearchBack').on('pointerup', function(){
        $('.mapSearchHeader').removeClass('active');
        $('.mapSearchInput').val('');
        $('.mapSearchInputRemove, mapSearchList, .mapSearchListUl, .mapSearchList').addClass('hidden');
    });

    $("#requestListBoxOdr").on('change', function(){
        const order = $(this).find('option:selected').data('order');
        console.log('선택된 정렬 옵션:', order);
        
        // 정렬된 배열 생성 (원본 배열을 변경하지 않음)
        const sortedList = [...requestList].sort((a, b) => {
            switch(order) {
                case 'crtDt':
                    // 최신 순: 날짜 내림차순
                    return new Date(b.crtDt) - new Date(a.crtDt);
                    
                case 'distance':
                    // 거리 순: 거리 오름차순
                    return (a.distance || 0) - (b.distance || 0);
                    
                case 'payAmt':
                    // 금액 높은 순: 금액 내림차순
                    return parseInt(b.payAmt || 0) - parseInt(a.payAmt || 0);
                    
                default:
                    return 0;
            }
        });
        
        listRender(sortedList);
    });

    $(".mapReSearch").on('pointerup', function(){

        Zinidata.auth.gps.getCurrentPosition(
            function(centerX, centerY) {
                if(centerX !== Zinidata.map.map.centerX && centerY !== Zinidata.map.map.centerY){

                    $(".mapReSearch").addClass('hidden');
                    mapMove(centerX, centerY, Zinidata.map.map.getZoom());
                }
            },
            function(error) {
                const coords = Zinidata.auth.gps.getCurrentCoordinates();
                coords.centerX
                coords.centerY

                if(coords.centerX !== Zinidata.map.map.centerX && coords.centerY !== Zinidata.map.map.centerY){
                    $(".mapReSearch").addClass('hidden');
                    mapMove(coords.centerX, coords.centerY, Zinidata.map.map.getZoom());
                }
            }

        );
    });

    $(".modalClose").on('pointerup', function(){
        $('.sheetStep02Modal').removeClass('active');
    });
}

// addr 입력 시 주소 검색 API 호출
const addrSearchListener = () => {
    const $mapSearchInput = $('.mapSearchInput');
    let searchTimeout = null;

    $mapSearchInput.on('input', function() {
        const query = $(this).val().trim();
       // 빈 값이면 검색하지 않음
        if (!query || query.length === 0) {
            return;
        }
       // 디바운스 적용 (500ms 후 검색)
        if (searchTimeout) {
            clearTimeout(searchTimeout);
        }
        searchTimeout = setTimeout(function() {
          // /api/common/location/search/mixed API 호출
            Zinidata.location.searchMixed(query, {
                success: function(response) {
                    console.log('[REQUESTS] 주소 검색 결과:', response);

                    // 검색 결과를 화면에 표시
                    displayAddressResults(response.data, query);
                },
                error: function(xhr, status, error) {
                    console.error('[REQUESTS] 주소 검색 실패:', error);
                }
            });
        }, 500);
    });

    // 선택주소 + 상세 주소 좌표 찾기
    $(".addressDetailText").on('focus keyup', function(){
        const searchAddress = $(".subAddressView").val() + " " + $(this).val();
        const $detailText = $(this);
        Zinidata.location.searchMixed(searchAddress, {
            success: function(response) {
                if(response.data.addressResults && response.data.addressResults.length > 0){
                    addressX = response.data.addressResults[0].x;
                    addressY = response.data.addressResults[0].y;
                } else if(response.data.keywordResults && response.data.keywordResults.length > 0){
                    addressX = response.data.keywordResults[0].x;
                    addressY = response.data.keywordResults[0].y;
                }
            },
            error: function(xhr, status, error) {
                console.error('[REQUESTS] 주소 검색 실패:', error);
            }
        });
    });
}

// 주소 검색 결과를 화면에 표시하는 함수
const displayAddressResults = (data, query) => {
    const $list = $('.mapSearchListUl');
    $list.empty();

    const MAX_RESULTS = 15;
    let resultCount = 0;

    // addressResults 먼저 표시
    if (data.addressResults && data.addressResults.length > 0) {
        data.addressResults.forEach(function(item) {
            if (resultCount >= MAX_RESULTS) {
                return;
            }

            // addressResults인 경우: road_address_name 또는 address_name을 메인에
            const mainText = item.road_address_name || item.address_name || '';
            const subText = item.address_name || '';

            // mainText에서 query 키워드 하이라이트
            const highlightedMainText = highlightKeyword(mainText, query);

            // 리스트 아이템 생성
            const $li = $('<li data-x="' + item.x + '" data-y="' + item.y + '">');
            $li.append($('<p>').html(highlightedMainText));
            $li.append($('<span>').addClass('subAddress').text(subText));

            // 클릭 이벤트 추가
            $li.on('pointerup', function() {
                $('.mapSearchList').addClass('hidden');
                $('.addressNull').addClass('!hidden');

                mapMove($(this).data('x'), $(this).data('y'), 15);
                callRequestMapApi();

            });

            $list.append($li);
            resultCount++;
        });
    }

    // addressResults가 15개 미만이면 keywordResults에서 추가
    if (resultCount < MAX_RESULTS && data.keywordResults && data.keywordResults.length > 0) {
        const neededCount = MAX_RESULTS - resultCount;
        const keywordResultsToAdd = data.keywordResults.slice(0, neededCount);

        keywordResultsToAdd.forEach(function(item) {
            // keywordResults인 경우: place_name을 메인에, road_address_name을 서브에
            const mainText = item.place_name || '';
            const subText = item.road_address_name || item.address_name || '';

            // mainText에서 query 키워드 하이라이트
            const highlightedMainText = highlightKeyword(mainText, query);

            // 리스트 아이템 생성
            const $li = $('<li data-x="' + item.x + '" data-y="' + item.y + '">');
            $li.append($('<p>').html(highlightedMainText));
            $li.append($('<span>').addClass('subAddress').text(subText));

            // 클릭 이벤트 추가
            $li.on('pointerup', function() {
                $('.mapSearchList').addClass('hidden');
                $('.addressNull').addClass('!hidden');

                mapMove($(this).data('x'), $(this).data('y'), 15);
                callRequestMapApi();
            });

            $list.append($li);
            resultCount++;
        });
    }

    // 결과가 없으면 null 메시지 표시
    if (resultCount === 0) {
        $('.mapSearchList').addClass('hidden');
        $('.addressNull').removeClass('hidden');
        return;
    }

    $('.mapSearchList').removeClass('hidden');
    $('.addressNull').addClass('hidden');
}

// 키워드 하이라이트 함수
const highlightKeyword = (text, query) => {
    if (!text || !query) {
        return text;
    }

    // 스페이스 제거한 버전으로 비교
    const textWithoutSpaces = text.replace(/\s+/g, '');
    const queryWithoutSpaces = query.replace(/\s+/g, '');

    // 대소문자 구분 없이 검색, 정규식 특수문자 이스케이프
    const escapedQuery = queryWithoutSpaces.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const regex = new RegExp(escapedQuery, 'gi');

    // 스페이스 제거한 버전에서 매칭 위치 찾기
    const matchIndex = textWithoutSpaces.search(regex);
    if (matchIndex === -1) {
        return text; // 매칭 없으면 원본 반환
    }

    // 원본 텍스트를 순회하면서 스페이스 제거한 버전의 인덱스와 매핑
    let result = '';
    let textWithoutSpacesIndex = 0;
    let inMatch = false;
    const matchEndIndex = matchIndex + queryWithoutSpaces.length;

    for (let i = 0; i < text.length; i++) {
        const char = text[i];
        const isSpace = /\s/.test(char);

        if (!isSpace) {
            // 매칭 시작점
            if (textWithoutSpacesIndex === matchIndex && !inMatch) {
                result += '<span class="keyword">';
                inMatch = true;
            }

            result += char;

            // 매칭 종료점 (다음 문자가 매칭 범위를 벗어남)
            if (inMatch && textWithoutSpacesIndex === matchEndIndex - 1) {
                result += '</span>';
                inMatch = false;
            }

            textWithoutSpacesIndex++;
        } else {
            // 스페이스는 그대로 추가 (매칭 중이면 하이라이트 안에 포함)
            result += char;
        }
    }

    // 마지막까지 매칭이 끝나지 않은 경우 (스페이스로 끝나는 경우)
    if (inMatch) {
        result += '</span>';
    }

    return result || text; // 결과가 없으면 원본 반환
}


const mapHandle = () => {
    const $bottomSheet = $('#sheet');
    const $scrollArea = $bottomSheet.find('.sheetContent');
  
    const stage1 = window.innerHeight - 160;
    const stage2 = window.innerHeight / 2;
    const stage3 = 0;
  
    let startY = 0;
    let startTranslateY = stage2;
    let currentTranslateY = stage2;
    let allowSheetDrag = false;
    let manuallyTriggered = false;
  
    // ✅ 추가: 전역 드래그 상태
    let isDragging = false;
  
    setTimeout(() => moveToStage('2'), 0);
  
    function moveToStage(stage) {
      let translateY;
      switch (stage) {
        case '1': translateY = stage1; break;
        case '2': translateY = stage2; break;
        case '3': translateY = stage3; break;
      }
  
      $bottomSheet.css({
        transform: `translateY(${translateY}px)`,
        transition: 'transform 0.2s ease-out'
      });
  
      currentTranslateY = translateY;
      $bottomSheet.attr('data-stage', stage);
      toggleScroll(stage);
    }
  
    function toggleScroll(stage) {
      $scrollArea.off('touchstart touchmove');
  
      if (stage === '3') {
        // stage 3일 때 height를 100%로 설정하여 브라우저 하단까지 꽉 채움
        $scrollArea.css({
          'overflow-y': 'auto',
          'height': 'calc(100% - 70px)'
        });
  
        $scrollArea.on('touchstart', function (e) {
          startY = e.originalEvent.touches[0].clientY;
          allowSheetDrag = false;
          manuallyTriggered = false;
        });
  
        $scrollArea.on('touchmove', function (e) {
          const currentY = e.originalEvent.touches[0].clientY;
          const deltaY = currentY - startY;
          const scrollTop = $(this).scrollTop();
  
          const isAtTop = scrollTop <= 10;
          const isDraggingDown = deltaY > 0;
  
          if (isAtTop && isDraggingDown) {
            allowSheetDrag = true;
  
            if (!manuallyTriggered) {
              manuallyTriggered = true;
              isDragging = true;
  
              const simulatedStart = new TouchEvent('touchstart', {
                touches: [e.originalEvent.touches[0]],
                bubbles: true,
                cancelable: true
              });
              $bottomSheet[0].dispatchEvent(simulatedStart);
            }
  
            e.preventDefault();
          } else {
            allowSheetDrag = false;
            isDragging = false;
            manuallyTriggered = false;
            e.stopPropagation();
          }

        });
        $('.mapReSearch').addClass('hidden');
      } else {
        // stage 1, 2일 때는 기존 CSS 값(calc(100% - 130px))으로 복원
        $scrollArea.css({
          'overflow-y': 'auto',
          'height': 'calc(100% - 70px)'
        });
        $scrollArea.on('touchmove', e => e.preventDefault());
        $('.mapReSearch').removeClass('hidden');
      }

        //위치 변경 버튼
      /* if(stage === '3'){
          $('.mapReSearch').addClass('hidden');
      }else{
          $('.mapReSearch').removeClass('hidden');
      } */
    }
  
    $bottomSheet.on('touchstart', function (e) {
      startY = e.originalEvent.touches[0].clientY;
      const transform = $bottomSheet.css('transform');
      startTranslateY = transform !== 'none' ? new DOMMatrix(transform).m42 : currentTranslateY;
      isDragging = true;
      $bottomSheet.css('transition', 'none');
    });
  
    $bottomSheet.on('touchmove', function (e) {
      if (!isDragging) return;
  
      const touch = e.originalEvent.touches[0];
      const diffY = touch.clientY - startY;
  
      let newTranslateY = startTranslateY + diffY;
      newTranslateY = Math.max(stage3, Math.min(stage1, newTranslateY));
  
      $bottomSheet.css('transform', `translateY(${newTranslateY}px)`);
      currentTranslateY = newTranslateY;
  
      e.preventDefault();
    });
  
    // ✅ 교체: 스냅 판정(중간값 기준) → 1↔2 이동 중 3으로 튀는 현상 방지
    $bottomSheet.on('touchend', function () {
      isDragging = false;
  
      const mid32 = (stage3 + stage2) / 2; // 0과 stage2의 중간
      const mid21 = (stage2 + stage1) / 2; // stage2와 stage1의 중간
  
      let targetStage;
      if (currentTranslateY <= mid32)      targetStage = '3';
      else if (currentTranslateY <= mid21) targetStage = '2';
      else                                 targetStage = '1';
  
      moveToStage(targetStage);
    });
  
    // ✅ 교체: 외부 트리거는 transform 직접 변경 금지 + 드래그 중 무시
    $('#mapContainer').on('pointerup touchstart', function(){
      if (isDragging) return;
      moveToStage('1');
    });
  
    $('.sheetTab').on('pointerup', function(){
      if (isDragging) return;
      moveToStage('3');
      $('.sheetContent').animate({scrollTop: 0}, 300);
    });
  };


const mapMove =(x, y, zoom)=>{
    const center = new naver.maps.LatLng(y, x);
    Zinidata.map.map.setZoom(zoom - 1);
    Zinidata.map.map.morph(center, zoom, {
        // 부가적인 애니메이션 옵션 (필요한 경우)
        duration: 1000, // 애니메이션 지속 시간 (밀리초)
        easing: 'easeOutQuart' // 애니메이션 이징 효과
    });
}

const sheetTab = () => { 
    $('.sheetTab li').on('pointerup', function(){
        $(this).addClass('active');
        $(this).siblings().removeClass('active');

        var idx = $('.sheetTab li');

        if(idx.eq(0).hasClass('active')){
            $('.sheetListBox').removeClass('hidden');
        }else if(idx.eq(1).hasClass('active')){
            $('.sheetListBox.sheetInstall').removeClass('hidden');
            $('.sheetListBox.sheetAs').addClass('hidden');
        }else if(idx.eq(2).hasClass('active')){
            $('.sheetListBox.sheetAs').removeClass('hidden');
            $('.sheetListBox.sheetInstall').addClass('hidden');
        }

    });

}

const sheetChange = () => {

    $('.sheetBackBtn').off('pointerup');
    $('.sheetBackBtn').on('pointerup', function(){
        $('.sheetTab, .sheetInner').removeClass('hidden');
        $('.sheetBack, .sheetDetail').addClass('hidden');
    });
}
const sheetAcceptModal = () => {
   /*  $('.sheetBottom button').on('pointerup', function(){
        $('.sheetAcceptModal').addClass('active');
    }); */
    $('.sheetAcceptModal .close, .modalBg').on('pointerup', function(){
        $('.sheetAcceptModal').removeClass('active');
    });

    $("#requestExecuteBtn").on('pointerup', function(){
        $(".sheetStep02Modal").addClass('active');
        if($(this).attr('data-service-gb') == "0"){
            $(".sheetStep02Modal").removeClass('asStep02Modal').addClass('installStep02Modal');
        }else{
            $(".sheetStep02Modal").removeClass('installStep02Modal').addClass('asStep02Modal');
        }
    });

    $("#requestExecuteDone").on('pointerup', function(){
        requestExecuteDone($(this).attr('data-seq'));
    });
}

const requestExecuteDone = (seq) => {
    console.log('수락하기 버튼 클릭');

    // 필수 입력값 검증
    const execName = ($("#crtName").val() || '').trim();
    const execPhoneNumber = ($("#phone").val() || '').trim();
    const executeDate = ($(".executeDate").val() || '').trim();
    
    // 1. 작업자 이름 필수 체크
    if (!execName || execName === '') {
        alert('작업자 이름을 입력해주세요.');
        $("#crtName").focus();
        return false;
    }
    
    // 2. 휴대폰 번호 필수 체크
    if (!execPhoneNumber || execPhoneNumber === '') {
        alert('휴대폰 번호를 입력해주세요.');
        $("#phone").focus();
        return false;
    }
    
    // 3. 작업 일자 필수 체크
    if (!executeDate || executeDate === '') {
        alert('작업 일자를 선택해주세요.');
        $(".executeDate").focus();
        return false;
    }
    

    Zinidata.api({
        url: '/api/requests/execute',
        method: 'POST',
        data: {
            encryptedSeq: seq  // 목록에서 받은 seq는 이미 암호화된 값
            , execName : execName
            , execPhoneNumber : execPhoneNumber
            , executeDate : commonUtil.replace(executeDate, '-', '')
        }, success: function(response){
            console.log('수락하기 상태변경, 등록 성공:', response);
            if(response.code === "0000"){
                $(".sheetStep02Modal").removeClass('active');
                $('.sheetTab, .sheetInner').removeClass('hidden');
                $('.sheetBack, .sheetDetail').addClass('hidden');

                // 입력칸 초기화
                $("#crtName").val('');
                $("#phone").val('').attr('disabled', false);
                $("#cert").attr('disabled', false);
                $("#verifyCode").val('');
                $(".executeDate").val('');

                alert('작업 수락이 완료 되었습니다.');

                //팝업 끄기
                callRequestMapApi();
            }else{
                alert(response.message);
                location.reload();
            }
        }, error: function(error){
            console.error('수락하기 실패:', error);
        }
    });
}

const backCall = () => {
    console.log("[MAP] 뒤로가기 감지 이벤트 초기화 (크롬/엣지/웨일/모바일 모두 지원)");

    // 브라우저 감지
    const userAgent = navigator.userAgent;
    const isWhale = userAgent.indexOf('Whale') !== -1;
    const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(userAgent);
    const isIOS = /iPhone|iPad|iPod/i.test(userAgent);
    
    console.log('[MAP] 브라우저 정보 - 웨일:', isWhale, ', 모바일:', isMobile, ', iOS:', isIOS);

    // 웨일 브라우저와 모바일을 포함한 모든 브라우저에서 작동하도록 처리
    const handleBackButton = (event) => {
        console.log('[MAP] 뒤로가기 감지됨! (모든 브라우저)');
        
        const $sheetStep02Modal = $('.sheetStep02Modal');
        
        // 모달이 활성화되어 있을 때만 처리
        if ($sheetStep02Modal.hasClass('active')) {
            console.log('[MAP] sheetStep02Modal 닫기');
            $sheetStep02Modal.removeClass('active');
            
            // 모달이 열려있을 때만 뒤로가기 차단 (pushState로 히스토리 스택 쌓기)
            // requestAnimationFrame과 setTimeout을 조합하여 확실하게 처리
            requestAnimationFrame(() => {
                setTimeout(() => {
                    history.pushState(null, document.title, location.href);
                    history.pushState(null, document.title, location.href);
                    // 웨일과 모바일에서는 추가로 한 번 더
                    if (isWhale || isMobile) {
                        history.pushState(null, document.title, location.href);
                    }
                }, 0);
            });
        } else {
            // 모달이 활성화되어 있지 않으면 일반 뒤로가기 작동 (아무것도 하지 않음)
            console.log('[MAP] 모달이 비활성화 상태 - 일반 뒤로가기 허용');
        }
    };

    // popstate 이벤트 리스너 등록
    // 웨일과 모바일에서도 작동하도록 여러 방법으로 등록
    const popStateHandler = (event) => {
        console.log('[MAP] popstate 이벤트 발생');
        handleBackButton(event);
    };
    
    window.addEventListener('popstate', popStateHandler, { passive: false });
    window.onpopstate = popStateHandler;
    
    // hashchange 이벤트도 감지 (일부 브라우저에서 추가 보완)
    window.addEventListener('hashchange', (event) => {
        console.log('[MAP] hashchange 이벤트 감지');
        handleBackButton(event);
    });
    
    // 모달이 열릴 때 히스토리 스택을 쌓기 위한 함수
    const setupHistoryStack = () => {
        const $sheetStep02Modal = $('.sheetStep02Modal');
        if ($sheetStep02Modal.hasClass('active')) {
            // 모달이 열려있을 때만 히스토리 스택 쌓기
            history.pushState(null, document.title, location.href);
            history.pushState(null, document.title, location.href);
            
            // 웨일과 모바일에서는 추가로 한 번 더
            if (isWhale || isMobile) {
                setTimeout(() => {
                    history.pushState(null, document.title, location.href);
                }, 50);
            }
        }
    };
    
    // 모달이 열릴 때 히스토리 스택 설정
    // MutationObserver를 사용하여 모달의 active 클래스 변경 감지
    const $sheetStep02Modal = $('.sheetStep02Modal');
    if ($sheetStep02Modal.length > 0) {
        const observer = new MutationObserver((mutations) => {
            mutations.forEach((mutation) => {
                if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
                    const isActive = $sheetStep02Modal.hasClass('active');
                    if (isActive) {
                        console.log('[MAP] 모달이 열림 - 히스토리 스택 설정');
                        setupHistoryStack();
                    }
                }
            });
        });
        
        observer.observe($sheetStep02Modal[0], {
            attributes: true,
            attributeFilter: ['class']
        });
        
        // 초기 상태 확인
        if ($sheetStep02Modal.hasClass('active')) {
            setupHistoryStack();
        }
    }
    
    console.log('[MAP] 히스토리 길이:', history.length);
}