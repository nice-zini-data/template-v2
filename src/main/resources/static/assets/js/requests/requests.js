$(function(){
   headChange();
   photoUploader();
   storeListModal();

   addressSearchOpen();
   requestInstallEventListener();
});

const requestInstallEventListener = () => {

   // 요청등록
   $('#installDoneBtn').on('pointerup', function(){
      // 폼 정보 수집 (jQuery의 .serializeArray() 또는 FormData로 이벤트에서 전송 가능)
      // 폼에서 네이티브로 값 추출 - form[0]은 실제 DOM 폼 엘리먼트
      const $form = $('#requestInstallForm');
      if ($form.length === 0) {
          console.error('[REQUESTS] 폼을 찾을 수 없습니다: #requestInstallForm');
          return false;
      }
      
      const formData = Zinidata.form.toJson($form);
      formData.serviceGb = '0';  // 신규 설치

      // verifyCode를 제외한 input 필수값 체크 및 알림+포커스
      const requiredFields = $form.find('input, textarea').not('#verifyCode, #serviceContent');
      for (let i = 0; i < requiredFields.length; i++) {
         const $el = $(requiredFields[i]);
         // input:checkbox 등은 name별 체크 처리 가능하지만, 여기선 단일값 체크
         // type="checkbox"는 패스
         if ($el.is(':checkbox')) continue;
         if (!$el.val() || $el.val().trim() === '') {
            const labelText = $el.closest('.inputBox').find('.inputLabel').text() || '해당 항목을';
            alert(labelText + ' 입력해주세요');
            $el.focus();
            return false;
         }
      }

      installRequest(formData);
   });

   // AS 등록
   $('#asDoneBtn').on('pointerup', function(){
      const $form = $('#requestAsForm');
      if ($form.length === 0) {
          console.error('[REQUESTS] 폼을 찾을 수 없습니다: #requestAsForm');
          return false;
      }
      const formData = Zinidata.form.toJson($form);
      formData.serviceGb = '1';  // A/S

      // verifyCode를 제외한 input 필수값 체크 및 알림+포커스
      const requiredFields = $form.find('input, textarea').not('#vanId, #verifyCode, #fileInput');
      for (let i = 0; i < requiredFields.length; i++) {
         const $el = $(requiredFields[i]);
         // input:checkbox 등은 name별 체크 처리 가능하지만, 여기선 단일값 체크
         // type="checkbox"는 패스
         if ($el.is(':checkbox')) continue;
         if (!$el.val() || $el.val().trim() === '') {
            const labelText = $el.closest('.inputBox').find('.inputLabel').text() || '해당 항목을';
            alert(labelText + ' 입력해주세요');
            $el.focus();
            return false;
         }
      }

      asRequest(formData);
   });

}

// 전역 파일 배열 (파일 업로드를 위해 외부에서 접근 가능하도록)
let selectedFiles = []; // {file, url}

//사진 업로드 처리
const photoUploader = () => {
   const $input   = $('#fileInput');
   const $list    = $('#fileList');
   const $listLi  = $('#fileList').find('li');
   const $btnAdd  = $('#btnAdd');
   const $preview = $('#preview');
   const $pvImg   = $('#preview img.previewImg');
   const $pvName  = $('#preview .name');

   // 추가 버튼
   $btnAdd.on('click', ()=> $input.click());
 
   // 선택 시 리스트에 추가
   $input.on('change', function(){
     const picked = Array.from(this.files || []);
     picked.forEach(f=>{
       if(!f.type.startsWith('image/')) return;
       const url = URL.createObjectURL(f);
       selectedFiles.push({file:f, url});
     });
     this.value = ''; // 같은 파일 재선택 가능
     render();
   });
 
   // 리스트 렌더
   function render(){
     $list.empty();
     selectedFiles.forEach((item, idx)=>{
       // 사진은 최대 3개까지만 등록, 한번만 alert 띄움
       if (selectedFiles.length > 3) {
         alert("사진은 최대 3개까지만 등록 가능합니다.");
         selectedFiles = selectedFiles.slice(0, 3);
       }
       if (idx >= 3) return;
       const li = $(`
         <li>
           <p class="fileName"><span class="size-4-5 flexCenter"><img src="/assets/images/icons/file_img_icon.svg" alt=""></span><span class="name">${item.file.name}</span></p>
           <button class="size-4-5 flexCenter del" type="button" aria-label="삭제"><img src="/assets/images/icons/remove_dark_icon.svg" class="size-4-5" alt=""></button>
         </li>
       `);

       // 미리보기 - 새창으로 이미지 뷰어 열기
       li.on('click', function(e){
         if($(e.target).hasClass('del')) return; // 삭제 버튼은 제외
         openImageViewer(selectedFiles, idx);
       });
       // 삭제
       li.find('.del').on('click', function(){
         URL.revokeObjectURL(item.url);
         selectedFiles.splice(idx,1);
         render();
       });
       $list.append(li);
     });
 
     if(!selectedFiles.length) $preview.attr('hidden', true);

     if(selectedFiles.length > 2) {
      $btnAdd.addClass('!hidden');
     } else {
      $btnAdd.removeClass('!hidden');
     }

     // 모바일 여부에 따라 fileName의 max-width 설정
     const isMobile = Zinidata.device.isMobile;
     const maxWidth = isMobile ? '250px' : '450px';
     $('.photoList li p.fileName').css('max-width', maxWidth);

   }
 
   // 미리보기 닫기
   $preview.find('.close').on('click', ()=> $preview.attr('hidden', true));
   
   // 이미지 뷰어 새탭으로 열기
   function openImageViewer(imageFiles, currentIndex) {
     const imageData = imageFiles.map(item => ({
       url: item.url,
       name: item.file.name
     }));
     
     const params = new URLSearchParams({
       images: encodeURIComponent(JSON.stringify(imageData)),
       index: currentIndex
     });
     
     const viewerUrl = `/requests/image-viewer?${params.toString()}`;
     // 새탭으로 열기 (target="_blank"와 동일한 효과)
     window.open(viewerUrl, '_blank');
   }
}

const storeListModal = () => {
   $('.storeSearchBox').on('pointerup', function(){
      $('.storeListModal').addClass('active');
   });
   
   $('.storeListUl li').on('pointerup', function(){
      $(this).addClass('check').siblings().removeClass('check');
      var storeName = $(this).children('p').text();
      $('.storeSearchBox input').val(storeName);
      $('.modalLayout').removeClass('active');
   });

   $('.modalClose, .modalBg, .modalLayout .close').on('pointerup', function(){
      $('.modalLayout').removeClass('active');
   });
}

const modalDone = () => {
   $('#installDoneBtn').on('pointerup', function(){
      $('.doneModal').addClass('active');
   });
}


const installRequest = (formData) => {
   console.log('[REQUESTS] 폼 데이터 수집 결과:', formData);
   Zinidata.api({
      url: '/api/requests/install',
      method: 'POST',
      data: formData,
      success: function(response){
         console.log('[REQUESTS] 요청 성공:', response);

         $('.doneModal').addClass('active');
      },
      error: function(error){
         console.error('[REQUESTS] 요청 실패:', error);
      }
   });
}

const asRequest = (formData) => {
   console.log('[REQUESTS] 폼 데이터 수집 결과:', formData);
   Zinidata.api({
      url: '/api/requests/as',
      method: 'POST',
      data: formData,
      success: function(response){
         console.log('[REQUESTS] A/S 요청 성공:', response);

         if (selectedFiles && selectedFiles.length > 0) {
            // 파일 업로드 처리 (요청 성공 후)
            uploadFilesIfExist(response);
         }else{
            $('.doneModal').addClass('active');
         }
      },
      error: function(error){
         console.error('[REQUESTS] A/S 요청 실패:', error);
      }
   });
}

// 파일 업로드 함수
const uploadFilesIfExist = (requestResponse) => {
   console.log('[REQUESTS] 파일 업로드 시작 - 함수호출');

   // 파일이 하나라도 있으면 업로드 API 호출
   var uploadForm = new FormData();

   // 여러 파일 업로드 지원 (최대 3개)
   const filesToUpload = selectedFiles.slice(0, 3);
   filesToUpload.forEach(function(item){
      uploadForm.append('files', item.file);
   });

   // 요청 번호를 함께 전송 (파일과 연결하기 위해)
   let requestSeq = null;
   let executeSw = '0';

   if (requestResponse && requestResponse.data) {
      // seq 또는 requestNo 중 사용 가능한 값 사용
      requestSeq = requestResponse.data.encryptedSeq;
      executeSw = requestResponse.data.executeSw != null ? requestResponse.data.executeSw : '0';
   }

   if (!requestSeq) {
      console.error('[REQUESTS] requestSeq를 찾을 수 없습니다:', requestResponse);
      alert('요청 번호를 찾을 수 없어 파일 업로드에 실패했습니다.');
      $('.doneModal').addClass('active');
      return;
   }

   // requestSeq를 파라미터로 추가
   uploadForm.append('requestSeq', requestSeq.toString());
   uploadForm.append('executeSw', executeSw);

   // 로딩 상태 표시
   console.log('[REQUESTS] 파일 업로드 시작 - 로딩 표시');
   $('body').append('<div id="fileUploadLoading" style="position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 9999; display: flex; align-items: center; justify-content: center; color: white; font-size: 18px;">파일 업로드 중...</div>');

   // 실제 파일 업로드 API 호출
   $.ajax({
      url: '/api/requests/upload-files',
      type: 'POST',
      data: uploadForm,
      processData: false,
      contentType: false,
      xhr: function() {
         const xhr = new window.XMLHttpRequest();
         // 업로드 진행률 모니터링
         xhr.upload.addEventListener('progress', function(e) {
            if (e.lengthComputable) {
               const percentComplete = (e.loaded / e.total) * 100;
               console.log('[REQUESTS] 업로드 진행률:', Math.round(percentComplete) + '%');
            }
         }, false);
         return xhr;
      },
      success: function(res) {
         console.log('[REQUESTS] 파일 업로드 성공:', res);
         $('#fileUploadLoading').remove();
         
         // 필요시 업로드 결과(res) 활용 or 콜백 내 로직 구현
         $('.doneModal').addClass('active');
      },
      error: function(xhr, status, error) {
         console.error('[REQUESTS] 파일 업로드 실패:', {
            status: status,
            error: error,
            readyState: xhr.readyState,
            statusCode: xhr.status,
            statusText: xhr.statusText,
            responseText: xhr.responseText
         });
         
         $('#fileUploadLoading').remove();
         
         // 에러 메시지 표시
         let errorMessage = '파일 업로드에 실패했습니다.';
         if (xhr.responseText) {
            try {
               const response = xhr.responseJSON || JSON.parse(xhr.responseText);
               errorMessage = response.message || errorMessage;
            } catch (e) {
               // JSON 파싱 실패 시 기본 메시지 사용
            }
         }
         alert(errorMessage);
      }
   });
}


// 주소 검색 페이지 새탭으로 열기
const addressSearchOpen = () => {
   $('.storeAddress').on('click', function(){
      // 주소 검색 페이지를 새탭으로 열기
      window.open('/requests/address-search', '_blank');
   });

}
