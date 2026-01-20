// 전역 변수: 요청 내역 데이터 저장
let requestsHistoryData = [];
let currentSortType = "crt_dt"; // 기본 정렬: 최신 순
let currentPage = 1; // 현재 페이지
let currentSearchText = ""; // 현재 검색어
let isLoading = false; // 로딩 중 플래그
let hasMoreData = true; // 더 불러올 데이터가 있는지
let totalCount = 0; // 전체 개수

$(function () {
  headChange();
  bindEvent();
  selectRequestHistory(1, currentSearchText);

  // 필터 기능
  $("#filterStatus li").on("click", function () {
    $("#filterStatus li").removeClass("active");
    $(this).addClass("active");
    selectRequestHistory(1, $(".inputCustom").val());
  });
});

/**
 * 검색 처리
 */
const handleSearch = () => {
  const inputCustom = document.querySelector(".inputCustom");
  if (!inputCustom) return;

  const searchText = inputCustom.value.trim();
  console.log("[REQUESTS-HISTORY] 검색 실행:", searchText);

  // 검색 시 초기화
  currentPage = 1;
  currentSearchText = searchText;
  requestsHistoryData = [];
  hasMoreData = true;
  selectRequestHistory(1, searchText);
};

/**
 * 다음 페이지 로드
 */
const loadNextPage = () => {
  if (isLoading || !hasMoreData) return;
  currentPage += 1;
  selectRequestHistory(currentPage, currentSearchText);
};

/**
 * 요청 내역 조회
 */
const selectRequestHistory = (page = 1, searchText) => {
  if (isLoading) return; // 이미 로딩 중이면 중복 요청 방지
  isLoading = true;

  Zinidata.auth.gps.getCurrentPosition(
    // 성공 콜백
    function (centerX, centerY) {
      Zinidata.api({
        url: "/api/requests/history",
        method: "POST",
        contentType: "application/json",
        data: {
          searchText: searchText || "",
          pageNo: page,
          size: 10,
          sortType: currentSortType || "crtDt",
          centerX: centerX,
          centerY: centerY,
          status : $("#filterStatus > li.active").attr('data-gb') === null ? '99' : $("#filterStatus > li.active").attr('data-gb')
        },
        success: function (response) {
          console.log("요청 내역 조회 성공!", response);

          // 응답 구조 확인 후 데이터 추출
          const data = response.data?.data || response.data || response;
          const newData = data.requestsHistory || [];
          totalCount = newData.length > 0 ? newData[0].totalCount : 0;

          // 첫 페이지인 경우 전체 교체, 이후 페이지는 추가
          if (page === 1) {
            requestsHistoryData = newData;
            renderRequestHistory();
          } else {
            // 기존 데이터에 추가
            requestsHistoryData = [...requestsHistoryData, ...newData];
            appendRequestHistory(newData);
          }

          // 더 불러올 데이터가 있는지 확인
          if (newData.length < 10) {
            hasMoreData = false;
          }

          isLoading = false;
        },
        error: function (error) {
          console.error("요청 내역 조회 실패..ㅠㅠ", error);
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

/**
 * 화면 렌더링 (전체 교체)
 */
const renderRequestHistory = () => {
  const $container = $(".listChangeContent").first();
  // 전체 개수 업데이트
  updateTotalCount(totalCount, "요청");

  if (requestsHistoryData.length === 0) {
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
  requestsHistoryData.forEach((item) => {
    html += createHistoryItem(item, "request");
  });

  $container.html(html);
};

/**
 * 새로운 데이터 추가 렌더링 (무한 스크롤용)
 */
const appendRequestHistory = (newData) => {
  const $container = $(".listChangeContent").first();

  // null 상태 메시지 제거
  $container.find(".historyNull").remove();

  // 새로운 항목 추가
  newData.forEach((item) => {
    const html = createHistoryItem(item, "request");
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
    // 정렬 변경 시 서버에서 재조회 (초기화)
    currentPage = 1;
    requestsHistoryData = [];
    hasMoreData = true;
    selectRequestHistory(1, currentSearchText);
  });

  // 무한 스크롤 이벤트 설정
  setupInfiniteScroll(loadNextPage);
};

