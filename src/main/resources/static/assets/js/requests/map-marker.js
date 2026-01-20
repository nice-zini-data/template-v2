let requestList;

$(function(){
});

const callRequestMapApi = () => {

    $(".sheetListNull").removeClass("hidden");

     // 현재 지도 경계 가져오기
     const bounds = Zinidata.map.map.getBounds();
     const param = {
         minx: bounds._min.x,
         miny: bounds._min.y,
         maxx: bounds._max.x,
         maxy: bounds._max.y,
         gubun: Zinidata.map.getZoomGubun()
     };

    Zinidata.api({
        url: '/api/requests/map',
        method: 'POST',
        data: param,
        success: function(response) {
            console.log('[MAP] 지도 요청 탐색 호출 성공:', response);

            if(Zinidata.map.getZoomGubun() === "block"){
                requestMarker(response.data.result);
            }else{
                requestAdmiMarker(response.data.result);
            }

            // 전역 변수에 넣기. 정렬을 위해
            requestList = response.data.list;
            if(commonUtil.isEmpty(response.data.list)){
                requestList = response.data.result;
            }
            // 목록 출력
            listRender(requestList);
        }
    });
}

// 상세 마커 정보
const requestMarker = (data) => {
    console.log('[MAP] 지도 요청 block 탐색 호출 성공:', data);

    let contentHtml = [];

    data.forEach(function(val){
        let classGb = "installMarker";
        let nmGb = "신규";
        if(val.serviceGb === "1"){
            classGb = "asMarker";
            nmGb = "A/S"
        }
        let html = '<div class="mapMarker ' +classGb+ '" data-lng="' + val.lng+'" data-lat="' + val.lat+ '" data-seq="' + val.encryptedSeq+ '">'
        html += '<p><span>' +nmGb+ '</span>' +val.nm+ '</p>';
        html += '</div>';

        contentHtml.push(html);
    });

    Zinidata.map.markerList(data, contentHtml);

    $('.mapMarker').on('pointerup',function(){
        $('.mapMarker').removeClass('active');
        $(this).toggleClass('active');
        requestDetail($(this).data('seq'));
    });
}

// 지역단위 클러스터링 마커 정보
const requestAdmiMarker = (data) => {
    console.log('[MAP] 지도 요청 Admi 탐색 호출 성공:', data);

    let contentHtml = [];

    data.forEach(function(val){
        let html = '<div class="clusteringMarker" data-lng="' + val.lng+'" data-lat="' + val.lat+ '">'
        html += '<p>' +val.nm+ '</p>';
        html += '<p class="count">' +val.cnt+ '</p>';
        html += '</div>';

        contentHtml.push(html);
    });


    Zinidata.map.markerList(data, contentHtml);

    $('.clusteringMarker').on('pointerup',function(){
        let lng = $(this).data('lng');
        let lat = $(this).data('lat');
        mapMove(lng, lat, Zinidata.map.map.getZoom() + 1);
    });
}


// 요청건 목록 list 표시
const listRender = (data) => {
    console.log('[REQUESTS] 목록 출력:', data);

    let html = '';

    // 목록이 없을떄
    if(data.length < 1){
        $("#requestListBox").html(html);
        return;
    }

    data.forEach(function(val){
        let classGb = "sheetInstall";
        let classGb2 = "installBadge";
        let nmGb = "신규 설치";
        if(val.serviceGb === "1"){
            classGb = "sheetAs";
            classGb2 = "asBadge";
            nmGb = "A/S";
        }

        html += '<div class="sheetListBox ' +classGb+ '">';
        html += '   <p class="checkBadge ' +classGb2+ ' mb-1">' +nmGb+ '</p>';
        html += '   <p class="sheetListTitle">' + val.installNm + '</p>';
        html += '   <p class="sheetListTxt">' + val.installAddr + '</p>';
        html += '   <div class="flexBetween">';
        html += '       <p class="sheetListPrice">' + formatPrice(val.payAmt) + '</p>';
        html += '       <button type="button" class="btn textBtn text-sm/5 font-medium text-zinc-500 flexCener gap-0-5" onclick="requestDetail(\'' + (val.encryptedSeq || val.seq) + '\')">상세보기 <img src="/assets/images/icons/right_arrow_gray.svg" class="size-4" alt=""></button>';
        html += '   </div>';
        html += '</div>';
    });

    $("#requestListBox").html(html);
    $(".sheetListNull").addClass("hidden");

    sheetChange();
    
}

// 파일 데이터를 메모리에 저장 (DOM에 노출되지 않도록)
const fileDataMap = new Map();

const requestDetail = (seq) => {
    // seq가 암호화된 값인지 확인 (일반적으로 암호화된 값은 Base64 형태)
    // 목록에서 받은 seq는 이미 암호화된 값일 수 있으므로 그대로 사용
    if (seq) {
        Zinidata.api({
            url: "/api/requests/history-detail",
            method: "POST",
            contentType: "application/json",
            data: {
                encryptedSeq: seq,  // 목록에서 받은 seq를 encryptedSeq로 전달
            },
            success: function (response) {
                console.log(response);
                let data = response.data.requestDetail;

                $(".crtName").text(data.crtName);
                $(".crtPhoneNumber").text(commonUtil.phoneNumber(data.crtPhoneNumber));
                $(".storeCallNumber").text(data.storeCallNumber);
                $(".crtDt").text(data.strCrtDt);
                $(".installNm").text(data.installNm);
                $(".installAddr").text(data.installAddr);
                $(".serviceContent").text(data.serviceContent);
                $(".payAmt").text(commonUtil.addComma(data.payAmt));
                $(".serviceGb0").removeClass('hidden');
                $(".serviceGb1").addClass('hidden');
                $(".sheetDetail").addClass('installDetail').removeClass('asDetail');
                $("#requestExecuteBtn").attr('data-service-gb', data.serviceGb);
                
                // 하이픈, 공백 제거한 전화번호로 tel: 링크 설정
                const phoneNumber = data.crtPhoneNumber.replace(/[-\s()]/g, '');
                $("#callRequesterBtn").attr('href', 'tel:' + phoneNumber);

                // AS
                if(data.serviceGb == "1"){
                    $(".sheetDetail").removeClass('installDetail').addClass('asDetail');
                    $(".serviceGb0").addClass('hidden');
                    $(".serviceGb1").removeClass('hidden');

                    if (response.data.fileList.length > 0) {
                        $(".fileList").html("");
                        // 기존 파일 데이터 맵 초기화
                        fileDataMap.clear();
                    
                        response.data.fileList.map(function (file, index) {
                            // 파일 경로 정규화 (백슬래시 그대로 유지)
                            const normalizedFilePath = normalizeFilePath(file.filePath || "");
                            
                            // 고유 ID 생성 (타임스탬프 + 인덱스)
                            const fileId = `file_${Date.now()}_${index}`;
                            
                            // 파일 정보를 메모리에 저장 (DOM에 노출되지 않음)
                            fileDataMap.set(fileId, {
                                fileNm: file.fileNm || "",
                                filePath: normalizedFilePath,
                                orgFileNm: file.orgFileNm || "",
                            });
                            
                            $(".fileList").append(`
                                <div class="photoDown" data-file-id="${fileId}">
                                  <a href="javascript:void(0);" class="file-view-link"><input type="text" class="inputCustom" value="${file.orgFileNm || ""}" style="cursor: pointer; text-decoration: underline;" readonly /></a>
                                  <button type="button" class="btn file-download-btn">
                                    <img src="/assets/images/icons/download_blue.svg" alt="download">
                                  </button>
                                </div>
                              `);
                        });

                        // 이미지 미리보기 이벤트 처리 (a 태그 클릭 시 새 창으로 이미지 열기)
                        $(".fileList").on("click", ".file-view-link", function (e) {
                            e.preventDefault();
                            const $photoDown = $(this).closest(".photoDown");
                            const fileId = $photoDown.attr("data-file-id");
                            const fileData = fileDataMap.get(fileId);
                            
                            if (!fileData) {
                                console.error("파일 데이터를 찾을 수 없습니다:", fileId);
                                alert("파일 정보를 찾을 수 없습니다.");
                                return;
                            }

                            fileView(fileData.fileNm, fileData.filePath, fileData.orgFileNm);
                        });

                        // 다운로드 버튼 이벤트 처리 (메모리에서 파일 정보 조회)
                        $(".fileList").on("click", ".file-download-btn", function () {
                            const $photoDown = $(this).closest(".photoDown");
                            const fileId = $photoDown.attr("data-file-id");
                            const fileData = fileDataMap.get(fileId);
                            
                            if (!fileData) {
                                console.error("파일 데이터를 찾을 수 없습니다:", fileId);
                                alert("파일 정보를 찾을 수 없습니다.");
                                return;
                            }

                            fileDownload(fileData.fileNm, fileData.filePath, fileData.orgFileNm);
                        });
                    }
                }

                // encryptedSeq를 data-seq 속성에 설정
                const encryptedSeq = data.encryptedSeq || data.seq;
                $("#requestExecuteDone").attr('data-seq', encryptedSeq);
                
                $('.sheetTab, .sheetInner').addClass('hidden');
                $('.sheetBack, .sheetDetail').removeClass('hidden');
                setTimeout(() => {
                    $('#sheet').css('transform', 'translateY(0)');  
                    $('.sheetContent').animate({scrollTop: 0}, 300);
                }, 100);

            },
            error: function (error) {
                alert("요청 번호를 찾을 수 없습니다.a");
                return false;
            }
        });
    }else{
        alert("요청 번호를 찾을 수 없습니다.b");
    }
}

// 수락 기능
const requestExecute = (seq) => {
    console.log('[REQUESTS] 수락 기능:', seq);

    Zinidata.api({
        url: '/api/requests/execute',
        method: 'POST',
        data: {
            encryptedSeq: seq  // 목록에서 받은 seq는 이미 암호화된 값
        }
    });
}