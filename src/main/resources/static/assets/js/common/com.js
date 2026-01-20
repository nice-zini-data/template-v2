/**
 * ============================================
 *  공통모듈
 * ============================================
 *
 * @author NICE ZiniData 개발팀
 * @since 1.0
 * @refactored 2025.10
 */

$(document).ready(function () {
  $(".skeleton-labal").html('<span class="skeleton-text"></span>');
  $(".skeleton-table").html('<span class="skeleton-text"></span>');
  $(".skeleton-whiteBox").html('<span class="skeleton-text"></span>');

});

/**
 * 탭 변경
 */
const tabChange = () => {
  $(".tabLineButton li").on("click", function () {
    $(this).addClass("active");
    $(this).siblings().removeClass("active");
    var idx = $(".tabLineButton li").index(this);
    $(".listChangeContent").addClass("hidden");
    $(".listChangeContent").eq(idx).removeClass("hidden");
  });
};

/**
 * 헤더 타이틀 변경 및 뒤로가기 이미지 추가
 */
const headChange = () => {
  $(".changeTitle").text($("title").text());
  $(".headerTitle").addClass("bakBtn");
};

/**
 * 무한 스크롤 이벤트 설정 (조건부 실행 - 변수가 정의된 경우에만)
 */
const setupInfiniteScroll = (loadNextPageCallback) => {
  const scrollContainer = $(".installContent.scrollBox");
  scrollContainer.off("scroll.infiniteScroll"); // 기존 이벤트 제거
  scrollContainer.on("scroll.infiniteScroll", function () {
    if (
      typeof isLoading === "undefined" ||
      typeof hasMoreData === "undefined"
    ) {
      return;
    }
    if (isLoading || !hasMoreData) return;

    const scrollTop = $(this).scrollTop();
    const scrollHeight = $(this)[0].scrollHeight;
    const clientHeight = $(this).innerHeight();

    // 스크롤이 끝에서 100px 전에 도달하면 다음 페이지 로드
    if (scrollTop + clientHeight >= scrollHeight - 100) {
      if (typeof loadNextPageCallback === "function") {
        loadNextPageCallback();
      } else if (typeof loadNextPage === "function") {
        loadNextPage();
      }
    }
  });
};

// 가격 포맷팅
const formatPrice = (price) => {
  const num = parseInt(price) || 0;
  return num.toLocaleString("ko-KR") + "원";
};

// 날짜 포맷팅
const formatDate = (dateStr) => {
  if (!dateStr) return "-";
  try {
    const date = new Date(dateStr);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const hours = String(date.getHours()).padStart(2, "0");
    const minutes = String(date.getMinutes()).padStart(2, "0");
    return `${year}.${month}.${day} ${hours}:${minutes}`;
  } catch (e) {
    return "-";
  }
};

// 상태 텍스트 반환
const getStatusText = (status) => {
  const statusMap = {
    0: "대기중",
    1: "수행중",
    2: "수행완료",
    3: "처리완료",
  };
  return statusMap[status] || "대기중";
};

// 상태 클래스 반환
const getStatusClass = (status) => {
  const classMap = {
    0: "waitFilter",
    1: "processingFilter",
    2: "completeFilter",
    3: "doneFilter",
  };
  return classMap[status] || "waitFilter";
};

/**
 * 파일 경로 정규화 함수
 * 백슬래시가 이상한 기호로 변환되지 않도록 안전하게 처리합니다.
 * JSON.stringify()는 자동으로 백슬래시를 처리하므로, HTML 속성에 삽입할 때만 특별 처리합니다.
 * 
 * @param {string} filePath 파일 경로
 * @returns {string} 정규화된 파일 경로 (백슬래시 그대로 유지)
 */
const normalizeFilePath = (filePath) => {
  if (!filePath) {
    return "";
  }
  // JSON.stringify()는 백슬래시를 자동으로 처리하므로 그대로 반환
  // HTML 속성에 삽입할 때는 백슬래시가 그대로 유지되도록 처리
  return String(filePath);
};

/**
 * 전체 개수 업데이트
 */
const updateTotalCount = (count, label = "요청") => {
  if (count > 0) {
    $(".setData").text(`${label} ${count}개`);
  } else {
    $(".setData").text(`${label} 0개`);
  }
};

/**
 * 히스토리 항목 HTML 생성 (공통)
 */
const createHistoryItem = (item, viewType = "request") => {
  const serviceGb = item.serviceGb || item.service_gb || "0";
  const status = item.status || "0";
  const installNm = item.installNm || item.install_nm || "-";
  const installAddr = item.installAddr || item.install_addr || "-";
  const payAmt = formatPrice(item.payAmt || item.pay_amt || "0");
  const crtDt = formatDate(item.crtDt || item.crt_dt);
  const seq = item.seq;
  // encryptedSeq 우선 사용, 없으면 seq 사용 (하위 호환성)
  const encryptedSeq = item.encryptedSeq || seq;

  // 상태 텍스트 및 클래스
  const statusText = getStatusText(status);
  const statusClass = getStatusClass(status);

  // 서비스 구분 배지
  const serviceBadge =
    serviceGb === "0"
      ? '<p class="checkBadge installBadge">신규 설치</p>'
      : '<p class="checkBadge asBadge">A/S</p>';

  return `
    <div class="whiteBox">
      <div class="historyList" data-seq="${seq}">
        <div class="flexBetween mb-1">
          ${serviceBadge}
          <p class="checkFilter ${statusClass}">${statusText}</p>
        </div>
        <p class="historyListTitle">${installNm}</p>
        <ul class="subInfoList">
          <li>
            <p class="subInfoListTitle">주소</p>
            <p class="subInfoListTxt">${installAddr}</p>
          </li>
          <li>
            <p class="subInfoListTitle">처리 일시</p>
            <p class="subInfoListTxt">${crtDt}</p>
          </li>
          <li>
            <p class="subInfoListTitle">금액</p>
            <p class="subInfoListTxt">${payAmt}</p>
          </li>
        </ul>
        <button type="button" class="btn softBtn w-full h-9 text-sm/5 font-medium rounded-full mt-4" onclick="location.href='/requests/${viewType === "execute" ? "work-history" : "request-history"}-${serviceGb === "0" ? "install" : "as"}-view?encryptedSeq=${encodeURIComponent(encryptedSeq)}'">
          상세 보기
        </button>
        ${viewType === "execute" && status === "1" ? `
          <button type="button" class="btn primaryLineBtn w-full h-9 text-sm/5 font-medium rounded-full mt-2" onclick="location.href='/requests/work-completion-proof?encryptedSeq=${encodeURIComponent(encryptedSeq)}'"> 완료 증빙 </button>
        ` : ``}
      </div>
    </div>
  `;
};


const copyBtn = () => {
  $(".copyBtn").on("pointerup", function () {
    const text = $(".copyBoxTxt span").text();
    navigator.clipboard.writeText(text);
    alert("계좌번호가 복사되었습니다!");
  });

  $(".areaCopyBtn").on("pointerup", function () {
    const text = $(".areaCopyTxt").text();
    navigator.clipboard.writeText(text);
    alert("주소가 복사되었습니다!");
  });
};

// 이미지 새 창으로 미리보기
const fileView = (fileNm, filePath, orgFileNm) => {
  console.log("이미지 미리보기 시작", fileNm, filePath, orgFileNm);

  // normalizeFilePath는 백슬래시를 그대로 유지하도록 처리
  const normalizedFilePath = normalizeFilePath(filePath);

  // 파일 정보를 쿼리 파라미터로 인코딩
  const params = new URLSearchParams();
  params.append("fileNm", fileNm || "");
  params.append("filePath", normalizedFilePath || "");
  params.append("orgFileNm", orgFileNm || "");

  // 이미지 조회 API URL 생성
  const imageUrl = `/api/requests/view-image?${params.toString()}`;
  
  // 새 창으로 이미지 열기
  const newWindow = window.open(imageUrl, "_blank");
  if (!newWindow) {
    alert("팝업이 차단되었습니다. 팝업 차단을 해제해주세요.");
    return;
  }
  console.log("이미지 새 창 열기 완료");
};


// 파일 다운로드
const fileDownload = (fileNm, filePath, orgFileNm) => {
  console.log("파일 다운로드 시작", fileNm, filePath, orgFileNm);

  // normalizeFilePath는 백슬래시를 그대로 유지하도록 처리
  const normalizedFilePath = normalizeFilePath(filePath);

  // 파일 다운로드는 fetch API를 사용하여 Blob으로 받아야 함
  fetch("/api/requests/download-files", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    credentials: "same-origin",
    body: JSON.stringify({
      fileNm: fileNm,
      filePath: normalizedFilePath,
      orgFileNm: orgFileNm,
    }),
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      // Blob으로 변환
      return response.blob();
    })
    .then((blob) => {
      // Blob URL 생성
      const url = window.URL.createObjectURL(blob);
      // 다운로드 링크 생성
      const a = document.createElement("a");
      a.href = url;
      a.download = orgFileNm || fileNm || "download";
      document.body.appendChild(a);
      a.click();
      // 정리
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      console.log("파일 다운로드 완료");
    })
    .catch((error) => {
      console.error("파일 다운로드 실패:", error);
      alert("파일 다운로드 중 오류가 발생했습니다.");
    });
};