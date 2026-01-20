// 전역 변수: 수행 내역 데이터 저장
let executeHistoryData = [];
let currentSortType = "crtDt"; // 기본 정렬: 최신 순
let currentPage = 1; // 현재 페이지
let currentSearchText = ""; // 현재 검색어
let isLoading = false; // 로딩 중 플래그
let hasMoreData = true; // 더 불러올 데이터가 있는지
let totalCount = 0; // 전체 개수

$(function () {
  headChange();
  bindEvent();
  selectExecuteHistory(1, currentSearchText);

  // 필터 기능
  $("#filterStatus li").on("click", function () {
    $("#filterStatus li").removeClass("active");
    $(this).addClass("active");
    selectExecuteHistory(1, $(".inputCustom").val());
  });
});

/**
 * 검색 처리
 */
const handleSearch = () => {
  const inputCustom = document.querySelector(".inputCustom");
  if (!inputCustom) return;

  const searchText = inputCustom.value.trim();
  console.log("[EXECUTE-HISTORY] 검색 실행:", searchText);

  currentPage = 1;
  currentSearchText = searchText;
  executeHistoryData = [];
  hasMoreData = true;
  selectExecuteHistory(1, currentSearchText);
};

/**
 * 다음 페이지 로드
 */
const loadNextPage = () => {
  if (isLoading || !hasMoreData) return;
  currentPage += 1;
  selectExecuteHistory(currentPage, currentSearchText);
};

/**
 * 수행 내역 조회
 */
const selectExecuteHistory = (page = 1, searchText) => {
  if (isLoading) return; // 이미 로딩 중이면 중복 요청 방지
  isLoading = true;

  Zinidata.auth.gps.getCurrentPosition(
    // 성공 콜백
    function (centerX, centerY) {

      // GPS 좌표를 가져온 후 API 호출
      Zinidata.api({
        url: "/api/requests/execute-history",
        method: "POST",
        contentType: "application/json",
        data: {
          memNo: JSON.parse(sessionStorage.getItem("userInfo")).memNo,
          searchText: searchText,
          pageNo: page,
          size: 10,
          sortType: currentSortType || "crtDt",
          centerX: centerX,
          centerY: centerY,
          status : $("#filterStatus > li.active").attr('data-gb') === null ? '1' : $("#filterStatus > li.active").attr('data-gb')
        },
        success: function (response) {
          console.log("수행내역 조회 성공!", response);

          // 응답 구조 확인 후 데이터 추출
          const data = response.data?.data || response.data || response;
          const newData = data.executeHistory || [];
          totalCount = newData.length > 0 ? newData[0].totalCount : 0;

          // 첫 페이지인 경우 전체 교체, 이후 페이지는 추가
          if (page === 1) {
            executeHistoryData = newData;
            renderExecuteHistory();
          } else {
            // 기존 데이터에 추가
            executeHistoryData = [...executeHistoryData, ...newData];
            appendExecuteHistory(newData);
          }

          if (totalCount > 0) {
            updateTotalCount(totalCount, "수행");
          }

          if (newData.length < 10) {
            hasMoreData = false;
          }

          isLoading = false;
        },
        error: function (error) {
          console.error("수행내역 조회 실패..ㅠㅠ", error);
          isLoading = false;
        },
      });
    },
    // 실패 콜백
    function (error) {
      console.error("GPS 좌표 가져오기 실패:", error);
    }
  );
};

// 화면 렌더링
const renderExecuteHistory = () => {
  // 서버에서 이미 정렬된 데이터를 사용 (클라이언트 정렬은 사용하지 않음)
  const $container = $(".listChangeContent").first();
  // 전체 개수 업데이트
  updateTotalCount(totalCount, "수행");

  if (executeHistoryData.length === 0) {
    $container.empty();
    $container.append(`
      <div class="historyNull">
        <div>
          <img src="/assets/images/icons/history_null.svg" class="size-6 mb-2 m-auto" alt="" />
          <p class="historyNullText">검색된 내역이 없습니다.</p>
        </div>
      </div>
    `);
    return;
  }

  // 리스트 HTML 생성
  let html = "";
  executeHistoryData.forEach((item) => {
    html += createHistoryItem(item, "execute");
  });

  $container.html(html);
};

/**
 * 새로운 데이터 추가 렌더링 (무한 스크롤용)
 */
const appendExecuteHistory = (newData) => {
  const $container = $(".listChangeContent").first();

  // null 상태 메시지 제거
  $container.find(".historyNull").remove();

  // 새로운 항목 추가
  newData.forEach((item) => {
    const html = createHistoryItem(item, "execute");
    $container.append(html);
  });
};

const bindEvent = () => {
  const inputCustom = document.querySelector(".inputCustom");
  if (inputCustom) {
    inputCustom.addEventListener("keypress", function (event) {
      if (event.key === "Enter") {
        handleSearch();
      }
    });
  }

  // 정렬 select 박스 초기화
  $(".selectBox").on("change", function () {
    currentSortType = $(this).find("option:selected").attr("name");
    // 정렬 변경 시 서버에서 재조회
    currentPage = 1;
    executeHistoryData = [];
    hasMoreData = true;
    selectExecuteHistory(1, currentSearchText);
  });

  // 무한 스크롤 이벤트 설정
  setupInfiniteScroll(loadNextPage);
};
