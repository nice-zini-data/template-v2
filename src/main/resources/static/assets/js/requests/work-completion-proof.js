$(function(){
    $('.changeTitle').text('완료 증빙');
    stDate();
    bankList();

    $('#workCompletionBtn').on('pointerup', function(){
      let urlParams = new URLSearchParams(location.search);
      let encryptedSeq = urlParams.get('encryptedSeq') || urlParams.get('seq');
      // URL 파라미터에서 + 문자가 공백으로 변환되는 문제 해결
      if (encryptedSeq) {
        encryptedSeq = encryptedSeq.replace(/ /g, '+');
      }

      if (encryptedSeq) {
        const $form = $('#requestCompletionForm');
        if ($form.length === 0) {
            console.error('[REQUESTS] 폼을 찾을 수 없습니다: #requestAsForm');
            return false;
        }
        const formData = Zinidata.form.toJson($form);

        let formSw = true;
        // verifyCode를 제외한 input 필수값 체크 및 알림+포커스
        const requiredFields = $form.find('input, textarea').not('#vanId').not('#fileInput');
        for (let i = 0; i < requiredFields.length; i++) {
          const $el = $(requiredFields[i]);
          // input:checkbox 등은 name별 체크 처리 가능하지만, 여기선 단일값 체크
          // type="checkbox"는 패스
          if ($el.is(':checkbox')) continue;
          if (!$el.val() || $el.val().trim() === '') {
              const labelText = $el.closest('.inputBox').find('.inputLabel').text() || '해당 항목을';
              alert(labelText + ' 입력해주세요');
              $el.focus();
              formSw = false;
              return false;
          }
        }
        
        if(formSw){
          $('.doneCheckModal').addClass('active');
  
          $('.doneCheckInfoList .bankSmallIcon').addClass($('.bankSelect .bankIcon').attr('class')).removeClass('bankIcon');
          $('.doneCheckInfoList .accountNumberText').text($('.bankSelect input').val());
          $('.doneCheckInfoList .accountNumberText02').text($('.accountNumberInput').val());
          $('.doneCheckInfoList .accountHolder').text($('.accountHolderInput').val());
          $(".execPhoneNumber").text(commonUtil.phoneNumber($('#phone').val()));
          $(".execName").text(JSON.parse(sessionStorage.userInfo).memNm);
        }

      }else{
        alert('요청 번호를 찾을 수 없습니다.');
        return false;
      }
   });

   $('#workCompletionDoneBtn').on('pointerup', function(){
    // URLSearchParams.get()은 자동으로 디코딩하므로 추가 처리 불필요
    let urlParams = new URLSearchParams(location.search);
    let encryptedSeq = urlParams.get('encryptedSeq') || urlParams.get('seq');

    const $form = $('#requestCompletionForm');
    if ($form.length === 0) {
        console.error('[REQUESTS] 폼을 찾을 수 없습니다: #requestAsForm');
        return false;
    }
    const formData = Zinidata.form.toJson($form);
    formData.encryptedSeq = encryptedSeq;  // A/S
    completionRequest(formData);
   });
});


const bankList = () => {
  codeList.bank.forEach(bank => {
    $('.bankList').append('<li><span class="bankListIcon ' + bank.class + '"></span>' + bank.name + '</li>');
  });

  $('.bankSelect').on('pointerup', function(){
    $('.bankListModal').addClass('active');
  });

  $('.bankListModal .modalClose').on('pointerup', function(){
    $('.bankListModal').removeClass('active');
  });

  $('.bankList li').on('pointerup', function(){
      $('.bankSelect').addClass('active');       
      var bankName = $(this).text();
      var bankClass = $(this).children('span').attr('class');
      
      $('.bankSelect input').val(bankName); 
      $('.bankSelect .bankIcon').removeClass().addClass('bankIcon bankSmallIcon ' + bankClass).removeClass('bankListIcon');
      $('.bankListModal').removeClass('active');
      $('.bankSelect').siblings('input').focus();
  });
}


const completionRequest = (formData) => {
  console.log('[REQUESTS] 폼 데이터 수집 결과:', formData);
  Zinidata.api({
     url: '/api/requests/completion',
     method: 'POST',
     data: formData,
     success: function(response){
        console.log('[REQUESTS] A/S 요청 성공:', response);

        if (selectedFiles && selectedFiles.length > 0) {
           // 파일 업로드 처리 (요청 성공 후)
           response.data.encryptedSeq = response.data.requestDetail.encryptedExecSeq;
           response.data.executeSw = '1';

           uploadFilesIfExist(response);
        }else{
           $('.doneModal').addClass('active');
        }

        setTimeout(() => {
          location.href = '/requests/work-history';
        }, 5000);
        
     },
     error: function(error){
        console.error('[REQUESTS] A/S 요청 실패:', error);
     }
  });
}