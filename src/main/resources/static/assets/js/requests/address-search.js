// 선택된 주소의 좌표 정보를 저장하는 변수
let addressX = 0;
let addressY = 0;

$(function(){
    addressSearch();
    installAddrSearchListener();
});

const addressSearch = () => {
   $('.addressSearchInput').on('keyup', function(){
        if($(this).val() === ''){
            $('.addressInputRemove').addClass('hidden');
            $('.addressSearchList').addClass('hidden');
            $('.addressNull').removeClass('hidden');
        }else{
            $('.addressSearchList').removeClass('hidden');
            $('.addressInputRemove').removeClass('hidden');
            $('.addressNull').addClass('hidden');
        }
   });

   $('.addressInputRemove').on('pointerup', function(){
        $('.addressSearchInput').val('');
        $('.addressSearchInput').trigger('input');
        $('.addressInputRemove').addClass('hidden');
        $('.addressSearchList').addClass('hidden');
        $('.addressNull').removeClass('hidden');
   });

    $('.addressSearchListUl li').on('pointerup', function(){
        $('.addressSearchList').addClass('hidden');
        const address = $(this).children('p').text();
        const subAddress = $(this).children('span.subAddress').text();
        $('.addressText, .addressSearchInput').val(address);
        $('.subAddressView').val(subAddress);
        $('.addressNull').addClass('!hidden');
    });

    $('.addressDetailText').on('focus keyup', function(){
        if($(this).val() === ''){
            $('.addressBottom button').addClass('disabled');
        }else{
            $('.addressBottom button').removeClass('disabled');
        }
    });

    // 주소 선택 버튼 클릭 이벤트
    $('.addressBottom button').on('pointerup', function(){
        // disabled 상태가 아닐 때만 동작
        if($(this).hasClass('disabled')){
            return;
        }

        if(addressX === 0 && addressY === 0){
            alert('잘못된 주소입니다.');
            return;
        }
        
        // 주소 값 가져오기
        const addressText = $('.subAddressView').val();
        const addressDetail = $('.addressDetailText').val();
        
        // 부모 창(원본 페이지)에 값 전달
        if(window.opener && !window.opener.closed){
            // 주소 설정
            window.opener.$('#address').val(addressText);
            // 상세 주소 설정
            window.opener.$('#addressDetail').val(addressDetail);
            // 경도 설정
            window.opener.$('#centerX').val(addressX);
            // 위도 설정
            window.opener.$('#centerY').val(addressY);
            
            console.log('[REQUESTS] 부모 창 필드 값 설정 완료');
            
            console.log('[REQUESTS] 주소 선택 완료:', { 
                address: addressText, 
                addressDetail: addressDetail,
                x: addressX,
                y: addressY
            });
        }
        
        // 주소 검색 팝업/새탭 닫기
        window.close();
    });

    $(".closeBtn").on('pointerup', function(){
        window.close();
    });

};


// installAddr 입력 시 주소 검색 API 호출
const installAddrSearchListener = () => {
    const $addressSearchInput = $('.addressSearchInput');
    let searchTimeout = null;
    
    $addressSearchInput.on('input', function() {
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
    const $list = $('.addressSearchListUl');
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
            const $li = $('<li>');
            $li.append($('<p>').html(highlightedMainText));
            $li.append($('<span>').addClass('subAddress').text(subText));

            // 클릭 이벤트 추가
            $li.on('pointerup', function() {
                $('.addressSearchList').addClass('hidden');
                const mainTextValue = $(this).children('p').text();
                const subTextValue = $(this).children('span.subAddress').text();
                $('.addressText, .addressSearchInput').val(mainTextValue);
                $('.subAddressView').val(subTextValue);
                $('.addressNull').addClass('!hidden');
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
            const $li = $('<li>');
            $li.append($('<p>').html(highlightedMainText));
            $li.append($('<span>').addClass('subAddress').text(subText));

            // 클릭 이벤트 추가
            $li.on('pointerup', function() {
                $('.addressSearchList').addClass('hidden');
                const mainTextValue = $(this).children('p').text();
                const subTextValue = $(this).children('span.subAddress').text();
                $('.addressText, .addressSearchInput').val(mainTextValue);
                $('.subAddressView').val(subTextValue);
                $('.addressNull').addClass('!hidden');
                
            });

            $list.append($li);
            resultCount++;
        });
    }

    // 결과가 없으면 null 메시지 표시
    if (resultCount === 0) {
        $('.addressSearchList').addClass('hidden');
        $('.addressNull').removeClass('hidden');
        return;
    }

    $('.addressSearchList').removeClass('hidden');
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