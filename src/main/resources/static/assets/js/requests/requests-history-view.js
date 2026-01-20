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
    $(".errorRequestModal").addClass("active");
  });

  $(".modalClose, .modalBg, .modalLayout .close").on("pointerup", function () {
    $(".modalLayout").removeClass("active");
  });

  // 작업 확인 버튼
  $("#confirmBtn").on("pointerup", function () {
    $(".confirmModal").addClass("active");
  });

  $(".requestCancelDone").on('pointerup', function(){
    if($(".status0").hasClass('hidden')){
        alert('대기중인 건만 요청 취소가 가능합니다.');
        return;
    }
    requestCancelDone();
  });

   // 요청/as 작업 확인 버튼
  $('#requestDoneBtn').on('pointerup', function(){
    requestDone();
    $(".confirmModal").removeClass("active");
  });

};

// 파일 데이터를 메모리에 저장 (DOM에 노출되지 않도록)
const fileDataMap = new Map();

const selectRequestHistoryDetail = () => {
  // URL에서 encryptedSeq 추출 (seq= 또는 encryptedSeq=)
  // URLSearchParams.get()은 자동으로 디코딩하므로 추가 처리 불필요
  let urlParams = new URLSearchParams(location.search);
  let encryptedSeq = urlParams.get('encryptedSeq') || urlParams.get('seq');
  console.log('encryptedSeq:', encryptedSeq);
  
  if (encryptedSeq) {
    Zinidata.api({
      url: "/api/requests/history-detail",
      method: "POST",
      contentType: "application/json",
      data: {
        encryptedSeq: encryptedSeq,
      },
      success: function (response) {
        let data = response.data.requestDetail;
        let fileList = response.data.fileList;

        console.log(data);
        console.log(fileList);
        
        $(".crtName").text(data.crtName);
        $(".crtPhoneNumber").text(commonUtil.phoneNumber(data.crtPhoneNumber));
        $(".storeCallNumber").text(data.storeCallNumber);
        $(".crtDt").text(data.strCrtDt);
        $(".installNm").text(data.installNm);
        $(".installAddr").text(data.installAddr);
        $(".serviceContent").text(data.serviceContent);
        $(".vanId").text(data.vanId != null ? data.vanId : "-");
        $(".payAmt").text(commonUtil.addComma(data.payAmt) + "원");

        $(".status0").addClass("hidden");
        $(".status1").addClass("hidden");
        $(".status2").addClass("hidden");
        $(".status" + data.status).removeClass("hidden");
        if(data.status === '3'){
          $(".status2").removeClass("hidden");
        }
        
        $(".execName").text(data.execName);
        $(".execPhoneNumber").text(commonUtil.phoneNumber(data.execPhoneNumber));
        $(".executeDate").text(commonUtil.dateConvert(data.executeDate, 'YYYY-MM-DD'));
        $(".executeContent").text(data.executeContent);
        $(".accountNumber").text(data.accountNumber);
        $(".accountHolder").text(data.accountHolder);
        $(".bank").text(data.bank);
        if(!commonUtil.isEmpty(data.bank)){
          $(".bankSmallIcon").addClass(codeList.bank.find(bank => bank.name === data.bank).class);
        }
        // 하이픈, 공백 제거한 전화번호로 tel: 링크 설정
        $("#callExecutorBtn").attr('href', 'tel:' + data.execPhoneNumber).removeClass('hidden');


        // 요청상태 중일때
        if (data.status === "0"){
          $("#requestCancelBtn").parent().removeClass('hidden');
          $("#confirmBtn").parent().addClass('hidden');
        }

        // 수행 완료 상태
        if (data.status === "2"){
          $("#requestCancelBtn").parent().addClass('hidden');
          $("#confirmBtn").parent().removeClass('hidden');
        }

        // 취소 불가능한 상태 (대기중이 아닌 경우)
        if (data.status != "0") {
          // status가 "0"이 아닌 경우 취소 버튼 제거 (단, status "2"는 제외)
          if (data.status !== "2") {
            $("#requestCancelBtn").remove();
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
                  <input type="text" class="inputCustom" value="${file.orgFileNm || ""}" style="cursor: pointer; text-decoration: underline;" readonly />
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
        location.href = "/requests/request-history";
        return false;
      },
    });
  }
};



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
        encryptedSeq: encryptedSeq
     },
     success: function(response){
        console.log('[REQUESTS] 요청 취소 성공:', response);
        if(response.code === "0000"){
          alert("요청 취소가 완료 되었습니다.");
        }else{
          alert(response.message);
        }
        
        location.href = "/requests/request-history";
     },
     error: function(error){
        console.error('[REQUESTS] 요청 취소 실패:', error);
        alert(error.message);
        location.href = "/requests/request-history";
     }
  });
}

// 요청 처리 완료
const requestDone = () => {

  let urlParams = new URLSearchParams(location.search);
  let encryptedSeq = urlParams.get('encryptedSeq') || urlParams.get('seq');
  Zinidata.api({
    url: '/api/requests/done',
    method: 'POST',
    data: {
      encryptedSeq: encryptedSeq
    },
    success: function(response){
      $('.doneModal').addClass('active');
      console.log('[REQUESTS] 요청 작업 확인 성공:', response);
    },
    error: function(error){
      console.error('[REQUESTS] 요청 작업 확인 실패:', error);
    }
  });
}