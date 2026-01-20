$(function () {
  headChange();
  requestCancelBtn();
  copyBtn();

  // 상세 정보 조회
  selectRequestHistoryDetail();
});

const requestCancelBtn = () => {
  // 요청 취소 버튼
  $("#requestCancelBtn").on("pointerup", function () {
    if($(".status1").hasClass('hidden')){
      alert('진행중인 건만 요청 취소가 가능합니다.');
      return;
    }
    $(".errorRequestModal").addClass("active");
  });

  $(".modalClose, .modalBg, .modalLayout .close").on("pointerup", function () {
    $(".modalLayout").removeClass("active");
  });

  // 작업 확인 버튼
  $("#confirmBtn").on("pointerup", function () {
    $(".confirmModal").addClass("active");
  });

  $("#requestCancelDoneBtn").on('pointerup', function(){
    if($(".status1").hasClass('hidden')){
        alert('진행중인 건만 요청 취소가 가능합니다.');
        return;
    }
    requestCancelDone();
  });
};


// 파일 데이터를 메모리에 저장 (DOM에 노출되지 않도록)
const fileDataMap = new Map();

const selectRequestHistoryDetail = () => {
  // URL에서 encryptedSeq 추출 (seq= 또는 encryptedSeq=)
  // URLSearchParams.get()은 자동으로 디코딩하므로 추가 처리 불필요
  let urlParams = new URLSearchParams(location.search);
  let encryptedSeq = urlParams.get('encryptedSeq') || urlParams.get('seq');

  if (encryptedSeq) {

    $(".workCompletion").attr("onclick", "location.href='/requests/work-completion-proof?encryptedSeq=" + encodeURIComponent(encryptedSeq) + "'");

    Zinidata.api({
      url: "/api/requests/history-detail",
      method: "POST",
      contentType: "application/json",
      data: {
        encryptedSeq: encryptedSeq,
      },
      success: function (response) {

        let data = response.data.requestDetail;

        initMap(data);

        $(".crtName").text(data.crtName);
        $(".crtPhoneNumber").text(
          commonUtil.phoneNumber(data.crtPhoneNumber)
        );
        $(".storeCallNumber").text(data.storeCallNumber);
        $(".crtDt").text(data.strCrtDt);
        $(".installNm").text(data.installNm);
        $(".installAddr").text(data.installAddr);
        $(".serviceContent").text(data.serviceContent);
        $(".vanId").text(data.vanId != null ? data.vanId : "-");
        $(".payAmt").text(commonUtil.addComma(data.payAmt) + "원");
        $(".executeContent").text(data.executeContent);
        $(".accountNumber").text(data.accountNumber);
        $(".accountHolder").text(data.accountHolder);
        $(".bank").text(data.bank);
        if(!commonUtil.isEmpty(data.bank)){
          $(".bankSmallIcon").addClass(codeList.bank.find(bank => bank.name === data.bank).class);
        }

        $(".execName").text(data.execName);
        $(".execPhoneNumber").text(commonUtil.phoneNumber(data.execPhoneNumber));
        $(".executeDate").text(commonUtil.dateConvert(data.executeDate, 'YYYY-MM-DD'));
        // 하이픈, 공백 제거한 전화번호로 tel: 링크 설정
        const phoneNumber = data.crtPhoneNumber.replace(/[-\s()]/g, '');
        $("#callRequesterBtn").attr('href', 'tel:' + phoneNumber);

        $(".status0").addClass("hidden");
        $(".status1").addClass("hidden");
        $(".status2").addClass("hidden");
        $(".status" + data.status).removeClass("hidden");

        // 요청상태 중일때
        if (data.status === "0"){
          $("#requestCancelBtn").parent().removeClass('hidden');
          $("#confirmBtn").parent().addClass('hidden');
        }

        // 수행 완료 상태
        if (data.status === "2"){
          $("#requestCancelBtn").parent().removeClass('hidden');
          $("#confirmBtn").parent().removeClass('hidden');
        }

        // 취소 불가능한 상태 (대기중이 아닌 경우)
        if (data.status != "0") {
          // status가 "0"이 아닌 경우 취소 버튼 제거 (단, status "2"는 제외)
          if (data.status === "2") {
            $("#requestCancelBtn").parent().remove();
            $(".workCompletion").parent().remove();
          }
        }

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

            // executeSw 작업여부 (0: 작업 전, 1: 작업 완료)
            const target = file.executeSw == "1" ? ".complete" : ".symptom";
            $(target).append(`
              <div class="photoDown" data-file-id="${fileId}">
                <a href="javascript:void(0);" class="file-view-link">
                  <input type="text" class="inputCustom" value="${file.orgFileNm || ""}" name="image" style="cursor: pointer; text-decoration: underline;" readonly />
                </a>
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
      },
      error: function (error) {
        alert("요청 번호를 찾을 수 없습니다.");
        location.href = "/requests/work-history";
        return false;
      },
    });
  }else{
    alert("요청 번호를 찾을 수 없습니다.");
    location.href = "/requests/work-history";
    return false;
  }
};

const initMap = async (data) => {

  
  if(commonUtil.isEmpty(data)){
    return;
  }
  let centerx = data.centerX;
  let centery = data.centerY;

  if (!centerx || !centery) {
      console.warn('[MAP] 중심 좌표가 없습니다. centerX:', centerx, 'centerY:', centery);
      return;
  }

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
              enableUserLocation : false,
              center: [centery, centerx],
              zoom: 14,       // 초기 줌 레벨
              minZoom: 14, // 최소 줌 레벨 (고정)
              maxZoom: 14, // 최대 줌 레벨 (고정)
              debounceTime: 1,                // 마우스 이벤트 디바운스 시간 (ms)
              requestInterval: 1,             // API 요청 최소 간격 (ms)
              customEvents: [], // 커스텀 이벤트 배열 (드래그/줌 이벤트 제거)
          });

          // 지도 초기화 완료 후 실행되는 코드
          console.log('[MAP] 지도 초기화 완료');
          console.log(Zinidata.map.map.getBounds());
          
          // 드래그 및 줌 비활성화
          if (Zinidata.map.map) {
              // 드래그 비활성화
              Zinidata.map.map.setOptions({
                  draggable: false,              // 드래그 비활성화
                  scrollWheelZoom: false,       // 마우스 휠 줌 비활성화
                  disableDoubleClickZoom: true, // 더블클릭 줌 비활성화
                  disablePinchZoom: true,       // 핀치 줌 비활성화 (모바일)
                  keyboardShortcuts: false      // 키보드 단축키 비활성화
              });
              
              // 마커 표시
              let contentHtml = [];

              // serviceGb에 따라 마커 클래스 결정 (1: A/S, 0 또는 기타: 신규 설치)
              let classGb = "installMarker";
              let nmGb = "신규";
              if(data.serviceGb === "1" || data.serviceGb === 1){
                  classGb = "asMarker";
                  nmGb = "A/S";
              }
              
              let html = '<div class="mapMarker ' + classGb + '"><p><span>' + nmGb + '</span>' + data.installNm + '</p></div>';
              contentHtml.push(html);

              let tmp = [data];
              Zinidata.map.markerList(tmp, contentHtml);
              console.log('[MAP] 마커 표시 완료');
          }

      } catch (error) {
          console.error('[MAP] 지도 초기화 실패:', error);
      }
  } else {
      // map-common.js가 아직 로드되지 않았으면 재시도 (data 파라미터 전달)
      console.warn('[MAP] Zinidata.map이 아직 로드되지 않았습니다. 재시도...');
      setTimeout(() => initMap(data), 100);
  }
}



// 요청건 취소 ( 완전 삭제 처리 )
const requestCancelDone = () => {
  // URL에서 encryptedSeq 추출
  // URLSearchParams.get()은 자동으로 디코딩하므로 추가 처리 불필요
  let urlParams = new URLSearchParams(location.search);
  let encryptedSeq = urlParams.get('encryptedSeq') || urlParams.get('seq');
  
  Zinidata.api({
     url: '/api/requests/cancel',
     method: 'POST',
     data: {
        encryptedSeq: encryptedSeq, 
        executeSw : '1'
     },
     success: function(response){
        console.log('[REQUESTS] 요청 취소 성공:', response);
        if(response.code === "0000"){
          alert("수행 취소가 완료 되었습니다.");
        }else{
          alert(response.message);
        }
        
        location.href = "/requests/work-history";
     },
     error: function(error){
        console.error('[REQUESTS] 요청 취소 실패:', error);
        alert(error.message);
        location.href = "/requests/work-history";
     }
  });
}
