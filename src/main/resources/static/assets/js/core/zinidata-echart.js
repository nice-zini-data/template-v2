let option;
const isSmall = window.innerWidth <= 1024; // 1024 이하
const isVerySmall = window.innerWidth <= 600; //600 이하

let color = ['#155DFC','#00A6F4','#4FC660','#8E51FF','#EFB100','#FF6900','#F6339A','#009966'];
let colorBlueLine = ['#F6339A', '#3655C1','#1447E6','#2B7FFF','#51A2FF','#8EC5FF','#BEDBFF','#DBEAFE','#A5C4FF'];
let colorXY = ['#155DFC', '#F6339A'];
let colorLine = ['#155DFC', '#3EA44B', '#FD9A00'];
let colorPie = ['#155DFC', '#E4E4E7'];

//막대 - 세로
const basicBarChartOptionX = () => {
    option = {
        legend: {
            show: false
        },
        grid: {
            top: '5%', left: '2%', right: '2%', bottom: '12%',  containLabel: true
        },
        xAxis: {
            type: 'category',
            data: [], // X축 데이터
            axisTick: { show: false },
            axisLine: {
                show: true,
                lineStyle: {
                    color: 'rgba(39,39,42,.1)',
                    width: 2,
                }
            },
            axisLabel: {
                show: true,
                interval: isSmall ? 'auto' : 0, 
                hideOverlap: true,
                textStyle: {
                    color: '#52525C', // X축 라벨 색상
                    fontSize: isVerySmall ? '11px' : isSmall ? '12px' : '13px',
                    fontWeight: '500',
                    fontFamily: 'SUIT'
                }
            },
        },
        yAxis: {
            type: 'value',
            axisLabel: {
                show: true,
                textStyle: {
                    color: 'rgba(39,39,42,.3)',
                    fontSize: isVerySmall ? '11px' : isSmall ? '12px' : '13px',
                    fontWeight: '400',
                    fontFamily: 'SUIT'
                }
            },
            splitLine: {
                show: true,
                lineStyle: {
                    color: 'rgba(39,39,42,.1)',
                    type: 'solid',
                    width: 1,
                }
            },
        },
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'shadow'  // bar 차트일 경우 그림자형 포인터 (선 대신 박스)
            },
            formatter: function (params) {
                let tooltip = `${params[0].axisValue}<br/>`;
                params.forEach(p => {
                    tooltip += `${p.marker} ${p.seriesName} ${p.value.toLocaleString()}<br/>`;
                });
                return tooltip;
            },
            textStyle: {
                color: '#09090B',
                fontSize: 13,
                fontWeight: '500',
                fontFamily: 'SUIT'
            }
        },
        series: [
            {
                type: 'bar',
                barWidth: '24px',
                itemStyle: {
                    barBorderRadius: [2, 2, 0, 0]
                }
            }
        ],
        media: [
            {
                query: { maxWidth: 400 },
                option: {
                    series: [{
                        barWidth: '20px'
                    }]
                }
            }
        ]
    };
}

//막대 - 가로
const basicBarChartOptionY = () => {
    option = {
        legend: {
            show: false
        },
        grid: {
            top: '5%', left: '3%', right: '2%', bottom: '5%',  containLabel: true
        },
        xAxis: {
            type: 'value',
            axisLabel: {
                show: true,
                textStyle: {
                    color: 'rgba(39,39,42,.3)',
                    fontSize: isVerySmall ? '11px' : isSmall ? '12px' : '13px',
                    fontWeight: '400',
                    fontFamily: 'SUIT'
                }
            },
            splitLine: {
                show: true,
                lineStyle: {
                    color: 'rgba(39,39,42,.1)',
                    type: 'solid',
                    width: 1,
                }
            },
        },
        yAxis: {
            type: 'category',
            data: [], // X축 데이터
            offset: isVerySmall ? 30 : isSmall ? 40 : 50,
            axisTick: { show: false },
            axisLine: {
                show: true,
                lineStyle: {
                    color: 'rgba(39,39,42,.1)',
                    width: 2,
                }
            },
            axisLabel: {
                show: true,
                interval: isSmall ? 'auto' : 0, 
                hideOverlap: true,
                align: 'left',
                margin: isVerySmall ? 50 : isSmall ? 60 : 80,
                textStyle: {
                    color: '#52525C', // X축 라벨 색상
                    fontSize: isVerySmall ? '11px' : isSmall ? '12px' : '13px',
                    fontWeight: '500',
                    fontFamily: 'SUIT'
                }
            },
        },
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'shadow'  // bar 차트일 경우 그림자형 포인터 (선 대신 박스)
            },
            formatter: function (params) {
                let tooltip = `${params[0].axisValue}<br/>`;
                params.forEach(p => {
                    tooltip += `${p.marker} ${p.seriesName} ${p.value.toLocaleString()}<br/>`;
                });
                return tooltip;
            },
            textStyle: {
                color: '#09090B',
                fontSize: 13,
                fontWeight: '500',
                fontFamily: 'SUIT'
            }
        },
        series: [
            {
                type: 'bar',
                barWidth: '24px',
                itemStyle: {
                    barBorderRadius: [2, 2, 0, 0]
                }
            }
        ],
        media: [
            {
                query: { maxWidth: 400 },
                option: {
                    series: [{
                        barWidth: '20px'
                    }]
                }
            }
        ]
    };
}

//음수 양수
const basicBarChartOptionCenter = () => {
    option = {
        legend: {
            show: false
        },
        grid: {
            top: '5%', left: '3%', right: '2%', bottom: '5%',  containLabel: true
        },
        xAxis: {
            type: 'value',
            axisLabel: {
                show: true,
                textStyle: {
                    color: 'rgba(39,39,42,.3)',
                    fontSize: isVerySmall ? '11px' : isSmall ? '12px' : '13px',
                    fontWeight: '400',
                    fontFamily: 'SUIT'
                }
            },
            splitLine: {
                show: true,
                lineStyle: {
                    color: 'rgba(39,39,42,.1)',
                    type: 'solid',
                    width: 1,
                }
            },
        },
        yAxis: {
            type: 'category',
            data: [], // X축 데이터
            offset:50,
            axisTick: {show: false},
            axisLine: {show: false},
            axisLabel: {
                show: true,
                interval: isSmall ? 'auto' : 0, 
                hideOverlap: true,
                align: 'left',
                margin: 80,
                textStyle: {
                    color: '#52525C', // X축 라벨 색상
                    fontSize: isVerySmall ? '11px' : isSmall ? '12px' : '13px',
                    fontWeight: '500',
                    fontFamily: 'SUIT'
                }
            },
        },
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'shadow'  // bar 차트일 경우 그림자형 포인터 (선 대신 박스)
            },
            formatter: function (params) {
                let tooltip = `${params[0].axisValue}<br/>`;
                params.forEach(p => {
                    tooltip += `${p.marker} ${p.seriesName} ${Math.abs(p.value.toLocaleString())}<br/>`;
                });
                return tooltip;
            },
            textStyle: {
                color: '#09090B',
                fontSize: 13,
                fontWeight: '500',
                fontFamily: 'SUIT'
            }
        },
        series: [
            {
                type: 'bar',
                barWidth: '24px',
                itemStyle: {
                    barBorderRadius: [2, 2, 0, 0]
                }
            }
        ],
        media: [
            {
                query: { maxWidth: 400 },
                option: {
                    series: [{
                        barWidth: '20px'
                    }]
                }
            }
        ]
    };
}

//총 막대 - 세로
const basicBarChartOptionTotalMaxX = () => {
    option = {
        legend: {
            show: false
        },
        grid: {
            top: '5%', left: '2%', right: '2%', bottom: '12%',  containLabel: true
        },
        xAxis: {
            type: 'category',
            data: [], // X축 데이터
            axisTick: { show: false },
            axisLine: {
                show: true,
                lineStyle: {
                    color: 'rgba(39,39,42,.1)',
                    width: 2,
                }
            },
            axisLabel: {
                show: true,
                interval: isSmall ? 'auto' : 0, 
                hideOverlap: true,
                textStyle: {
                    color: '#52525C', // X축 라벨 색상
                    fontSize: isVerySmall ? '11px' : isSmall ? '12px' : '13px',
                    fontWeight: '500',
                    fontFamily: 'SUIT'
                }
            },
        },
        yAxis: {
            type: 'value',
            max: 100, //최대값
            axisLabel: {
                show: true,
                textStyle: {
                    color: 'rgba(39,39,42,.3)',
                    fontSize: isVerySmall ? '11px' : isSmall ? '12px' : '13px',
                    fontWeight: '400',
                    fontFamily: 'SUIT'
                }
            },
            splitLine: {
                show: true,
                lineStyle: {
                    color: 'rgba(39,39,42,.1)',
                    type: 'solid',
                    width: 1,
                }
            },
        },
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'shadow'  // bar 차트일 경우 그림자형 포인터 (선 대신 박스)
            },
            formatter: function (params) {
                let tooltip = `${params[0].axisValue}<br/>`;
                params.forEach(p => {
                    tooltip += `${p.marker} ${p.seriesName} ${p.value.toLocaleString()}<br/>`;
                });
                return tooltip;
            },
            textStyle: {
                color: '#09090B',
                fontSize: 13,
                fontWeight: '500',
                fontFamily: 'SUIT'
            }
        },
        series: [
            {
                type: 'bar',
                barWidth: '24px',
                itemStyle: {
                    barBorderRadius: [2, 2, 0, 0]
                }
            }
        ],
        media: [
            {
                query: { maxWidth: 400 },
                option: {
                    series: [{
                        barWidth: '20px'
                    }]
                }
            }
        ]
    };
}

//총 막대 - 가로
const basicBarChartOptionTotalMaxY = () => {
    option = {
        legend: {
            show: false
        },
        grid: {
            top: '5%', left: '2%', right: '2%', bottom: '12%',  containLabel: true
        },
        xAxis: {
            type: 'value',
            max: 100, //최대값
            axisLabel: {
                show: true,
                textStyle: {
                    color: 'rgba(39,39,42,.3)',
                    fontSize: isVerySmall ? '11px' : isSmall ? '12px' : '13px',
                    fontWeight: '400',
                    fontFamily: 'SUIT'
                }
            },
            splitLine: {
                show: true,
                lineStyle: {
                    color: 'rgba(39,39,42,.1)',
                    type: 'solid',
                    width: 1,
                }
            },
        },
        yAxis: {
            type: 'category',
            data: [], // X축 데이터
            offset:50,
            axisTick: { show: false },
            axisLine: {
                show: true,
                lineStyle: {
                    color: 'rgba(39,39,42,.1)',
                    width: 2,
                }
            },
            axisLabel: {
                show: true,
                interval: isSmall ? 'auto' : 0, 
                hideOverlap: true,
                align: 'left',
                margin: 80,
                textStyle: {
                    color: '#52525C', // X축 라벨 색상
                    fontSize: isVerySmall ? '11px' : isSmall ? '12px' : '13px',
                    fontWeight: '500',
                    fontFamily: 'SUIT'
                }
            },
        },
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'shadow'  // bar 차트일 경우 그림자형 포인터 (선 대신 박스)
            },
            formatter: function (params) {
                let tooltip = `${params[0].axisValue}<br/>`;
                params.forEach(p => {
                    tooltip += `${p.marker} ${p.seriesName} ${p.value.toLocaleString()}<br/>`;
                });
                return tooltip;
            },
            textStyle: {
                color: '#09090B',
                fontSize: 13,
                fontWeight: '500',
                fontFamily: 'SUIT'
            }
        },
        series: [
            {
                type: 'bar',
                barWidth: '24px',
                itemStyle: {
                    barBorderRadius: [2, 2, 0, 0]
                }
            }
        ],
        media: [
            {
                query: { maxWidth: 400 },
                option: {
                    series: [{
                        barWidth: '20px'
                    }]
                }
            }
        ]
    };
}

const pieChartOption = () => {
    option = {
        legend: [
            {
                orient: 'vertical',
                align: 'left',
                top: '20%',
                left: '48%',
                itemWidth: 13,
                itemGap: 20,
                textStyle: {
                    fontSize: isVerySmall ? '12px' : isSmall ? '12px' : '13px',
                    fontWeight: '500',
                    fontFamily: 'SUIT',
                    color: '#09090B',
                },

            },
            {
                orient: 'vertical',
                align: 'left',
                top: '20%',
                left: '75%',
                itemWidth: 13,
                itemGap: 20,
                textStyle: {
                    fontSize: isVerySmall ? '12px' : isSmall ? '12px' : '13px',
                    fontWeight: '500',
                    fontFamily: 'SUIT',
                    color: '#09090B'
                },
            }
        ],
        grid: {
            top: '5%',
            left:'5%'
        },
        series: [
            {
                name: [],
                type: 'pie',
                radius: ['55%', '90%'],
                center: ['50%', '30%'],
                data: [],
                selectedMode: false,
                itemStyle: {
                    borderColor: '#fff', borderWidth: 1
                },
                label: {
                    show: false,
                    position: 'center',
                    rich: {
                        value: {
                            fontSize: isVerySmall ? '18px' : isSmall ? '20px' : '24px',
                            fontWeight: 'bold',
                            color: '#09090B',
                            lineHeight: 28,
                        },
                        name: {
                            fontSize: isVerySmall ? '13px' : isSmall ? '13px' : '13px',
                            color: '#71717B',
                            lineHeight: 18,
                        }
                    }
                },
                emphasis: {
                    label: {
                        show: false
                    },
                },
                blur: {
                    itemStyle: {
                        opacity: 0.5
                    }
                }
            },
        ],
        tooltip: {
            trigger: 'item', // 마우스 오버 시 툴팁 표시
            formatter: function (params) {
                return `${params.marker}${params.name}<br/>${params.seriesName}: ${params.value}`;
            }, // 툴팁 포맷
            textStyle: {
                color: '#000',
                fontSize: 13,
                fontWeight: '400',
                fontFamily: 'SUIT'
            }
        }
    };
}


// 픽셀 변환 헬퍼 함수
function toPx(value, containerSize) {
    if (typeof value === 'string' && value.includes('%')) {
        const percent = parseFloat(value.replace('%', ''));
        return (containerSize * percent) / 100;
    }
    return parseFloat(value) || 0;
}

// 공통 헬퍼: 파이 중심 텍스트(label 방식으로 처리)
function attachPieCenter(chart, chartId){
    console.log('🔧 attachPieCenter 함수 호출됨 (label 방식)');
    
    // 이벤트 제거
    chart.off('mouseover');
    chart.off('mouseout');

    const option = chart.getOption();
    const seriesData = option.series[0].data;
    
    // 가장 큰 값 찾기
    const maxItem = seriesData.reduce((max, item) => 
        (item.value > max.value) ? item : max
    );
    
    console.log('📊 가장 큰 값:', maxItem.value, maxItem.name);
    
    // 초기 상태: 가장 큰 값을 첫 번째 항목으로 설정
    const updateCenterText = (targetValue, targetName) => {
        if(chartId === 'bizSummaryChart01'){
            targetValue = targetValue.toLocaleString()+'%';
        }else if(chartId === 'bizSummaryChart02'){
            targetValue = targetValue.toLocaleString()+'개';
        }
        
        const updatedData = seriesData.map((item, index) => ({
            ...item,
            label: {
                show: index === 0, // 첫 번째 항목에서만 중앙 텍스트 표시
                position: 'center',
                formatter: () => `{value|${targetValue}}\n{name|${targetName}}`,
                rich: {
                    value: {
                        fontSize: isVerySmall ? '18px' : isSmall ? '20px' : '24px',
                        fontWeight: 'bold',
                        color: '#09090B',
                        lineHeight: 28
                    },
                    name: {
                        fontSize: '13px',
                        color: '#71717B',
                        lineHeight: 18
                    }
                }
            }
        }));
        
        chart.setOption({
            series: [{
                data: updatedData
            }]
        });
    };
    
    // 초기값 설정 (가장 큰 값)
    updateCenterText(maxItem.value, maxItem.name);
    
    // 마우스 이벤트
    chart.on('mouseover', function (params) {
        if (params.componentType === 'series') {
            updateCenterText(params.value, params.name);
        }
    });
    
    chart.on('mouseout', function () {
        updateCenterText(maxItem.value, maxItem.name);
    });
}




//범례 테이블 공통으로 쓰이는 코드
/**
 * 범례를 테이블로 생성하고, 클릭 시 해당 시리즈를 차트에서 토글합니다.
 * @param {echarts.ECharts} chartInstance - ECharts 인스턴스
 * @param {String} containerSelector - 범례를 넣을 DOM 셀렉터
 * @param {Array} seriesData - 차트에 들어갈 series 배열
 */
function renderEchartTableLegend(chartInstance, containerSelector, seriesData) {
    const $container = $(containerSelector);
    const chartColors = chartInstance.getOption().color || [];

    // ✅ xAxis 데이터 내부에서 직접 추출 (전역 필요 없음)
    const option = chartInstance.getOption(); // ✅ 추가됨
  
    let xAxisData = [];
    
    
    if(containerSelector.includes('bizSummaryChart05Table')){
        //종합보고서 - 성별/연령별 차트 테이블 에외처리
        if (option.xAxis?.[0]?.data) {
            xAxisData = option.xAxis[0].data.reverse() || [];
          } else if (option.yAxis?.[0]?.data) {
            xAxisData = option.yAxis[0].data.reverse() || [];
          };
    }else{
        if (option.xAxis?.[0]?.data) {
          xAxisData = option.xAxis[0].data || [];
        } else if (option.yAxis?.[0]?.data) {
          xAxisData = option.yAxis[0].data || [];
        }
    }
  
    let html = `
      <table class="legend-table">
        <thead>
          <tr>
            <th class="fixText">항목</th>
               ${xAxisData.map(c => `<th class="text-right">${c}</th>`).join('')}
          </tr>
        </thead>
        <tbody>
          ${seriesData.map((series, idx) => `
            <tr class="legend-row" data-series="${series.name}">
              <td class="fixText">
                <div class="flexStart">
                  <span class="iconBox size-3" style="background-color:${series.itemStyle?.color || chartColors[idx] || '#ccc'}"></span>
                  <span>${series.name}</span>
                </div>
              </td>
              ${series.data.map(val => `<td class="text-right">${(val || 0).toLocaleString()}${series.valType || ''}</td>`).join('')}
            </tr>
          `).join('')}
        </tbody>
      </table>`;
  
    $container.html(html);
  
    // 스크롤을 맨 오른쪽으로 이동
    setTimeout(() => {
      $container.scrollLeft($container[0].scrollWidth);
    }, 0);
  
    function detectTableScroll($container) {
      const $table = $container.find('table');
      if ($table[0]?.scrollWidth > $container[0]?.clientWidth) {
        $container.addClass('tableScrollOn');
      } else {
        $container.removeClass('tableScrollOn');
      }
    }
    detectTableScroll($container);
  
  // ⬇️ 화면 크기 변경 시도 체크 (선택사항)
    $(window).on('resize', function () {
      detectTableScroll($container);
    });
  
    // 시리즈 토글 관리 객체
    const visibilityMap = {};
    seriesData.forEach(s => { visibilityMap[s.name] = true; });
  
    $(".legend-row", $container).on("click", function () {
      const $row = $(this);
      const seriesName = $row.data("series");
      visibilityMap[seriesName] = !visibilityMap[seriesName];
      $row.toggleClass("opacity-30");
  
      const currentOption = chartInstance.getOption();
      const updatedSeries = currentOption.series.map(s => {
        if (s.name === seriesName) {
          return {
            ...s,
            data: visibilityMap[seriesName] ? s._originalData || s.data : [],
            _originalData: s._originalData || s.data
          };
        }
        return s;
      });
  
      chartInstance.setOption({ series: updatedSeries });
    });
  
    const chartBox = document.getElementById(chartInstance.getDom().id);
  
    if (chartBox?.classList.contains('chartBoxYAuto')) {
      const itemCount = xAxisData.length;
      const seriesCount = (option.series || []).length;
      const heightPerItemPerSeries = 32; // 막대 1개당 기본 높이 (간격 포함 고려)
  
      // ⚠️ 시리즈가 겹치는 구조이므로 1개당 높이를 곱해서 더 큰 값 확보
      const height = Math.max(itemCount * seriesCount * heightPerItemPerSeries);
      chartBox.style.height = `${height}px`;
    }
  }
  
//범례 테이블 예외 케이스 처리를 위한 함수
/**
 * 범례를 테이블로 생성하고, 클릭 시 해당 시리즈를 차트에서 토글합니다.
 * @param {echarts.ECharts} chartInstance - ECharts 인스턴스
 * @param {String} containerSelector - 범례를 넣을 DOM 셀렉터
 * @param {Array} seriesData - 차트에 들어갈 series 배열
 */

function renderEchartTableLegend2(chartInstance, containerSelector, seriesData) {
    const $container = $(containerSelector);
    const chartColors = chartInstance.getOption().color || [];
  
    // ✅ xAxis 데이터 내부에서 직접 추출 (전역 필요 없음)
    const option = chartInstance.getOption(); // ✅ 추가됨
  
    let xAxisData = [];
  
    if (option.xAxis?.[0]?.data) {
      xAxisData = option.xAxis[0].data || [];
    } else if (option.yAxis?.[0]?.data) {
      xAxisData = option.yAxis[0].data || [];
    }
  
    let html = `
      <table class="legend-table">
        <thead>
          <tr>
            <th class="fixText">항목</th>
            <th class="text-right">업종 비중</th>
          </tr>
        </thead>
        <tbody>
          ${seriesData.map((series, idx) => `
            <tr class="legend-row" data-series="${series.name}">
              <td class="fixText">
                <div class="flexStart">
                  <span class="iconBox size-3" style="background-color:${series.itemStyle?.color || chartColors[idx] || '#ccc'}"></span>
                  <span>${series.name}</span>
                </div>
              </td>
              ${series.data.map(val => `<td class="text-right">${(val || 0).toLocaleString()}</td>`).join('')}
            </tr>
          `).join('')}
        </tbody>
      </table>`;
  
    $container.html(html);
  
    // 스크롤을 맨 오른쪽으로 이동
    setTimeout(() => {
      $container.scrollLeft($container[0].scrollWidth);
    }, 0);
  
    function detectTableScroll($container) {
      const $table = $container.find('table');
      if ($table[0]?.scrollWidth > $container[0]?.clientWidth) {
        $container.addClass('tableScrollOn');
      } else {
        $container.removeClass('tableScrollOn');
      }
    }
    detectTableScroll($container);
  
    // ⬇️ 화면 크기 변경 시도 체크 (선택사항)
    $(window).on('resize', function () {
      detectTableScroll($container);
    });
  
    // 시리즈 토글 관리 객체
    const visibilityMap = {};
    seriesData.forEach(s => { visibilityMap[s.name] = true; });
  
    $(".legend-row", $container).on("click", function () {
      const $row = $(this);
      const seriesName = $row.data("series");
      visibilityMap[seriesName] = !visibilityMap[seriesName];
      $row.toggleClass("opacity-30");
  
      const currentOption = chartInstance.getOption();
      const updatedSeries = currentOption.series.map(s => {
        if (s.name === seriesName) {
          return {
            ...s,
            data: visibilityMap[seriesName] ? s._originalData || s.data : [],
            _originalData: s._originalData || s.data
          };
        }
        return s;
      });
  
      chartInstance.setOption({ series: updatedSeries });
    });
  
    const chartBox = document.getElementById(chartInstance.getDom().id);
  
    if (chartBox?.classList.contains('chartBoxYAuto')) {
      const itemCount = xAxisData.length;
      const seriesCount = (option.series || []).length;
      const heightPerItemPerSeries = 32; // 막대 1개당 기본 높이 (간격 포함 고려)
  
      // ⚠️ 시리즈가 겹치는 구조이므로 1개당 높이를 곱해서 더 큰 값 확보
      const height = Math.max(itemCount * seriesCount * heightPerItemPerSeries);
      chartBox.style.height = `${height}px`;
    }
  }
  

  (function(){
    // 차트 인스턴스 저장소 (리사이즈용)
    const chartInstances = new Map();
    
    function getMeta(el){
      let decimals = 0;
      if (el.dataset.zdDecimals) {
        const parsed = Number(el.dataset.zdDecimals);
        // toLocaleString의 minimumFractionDigits/maximumFractionDigits는 0-20 사이여야 함
        decimals = isNaN(parsed) ? 0 : Math.max(0, Math.min(20, Math.floor(parsed)));
      }
      return {
        unit: el.dataset.zdUnit || '',
        decimals: decimals,
        legendTarget: el.dataset.zdLegendTarget || ''
      };
    }
  
    function formatValue(v, meta){
      if (v == null || isNaN(v)) return '-';
      // decimals 값을 안전하게 처리 (0-20 범위)
      const decimals = Math.max(0, Math.min(20, Math.floor(meta.decimals || 0)));
      const num = Number(v).toLocaleString('ko-KR', {
        minimumFractionDigits: decimals,
        maximumFractionDigits: decimals
      });
      // unit이 있고, "${unit}" 같은 템플릿 문자열이 아닐 때만 추가
      const unit = meta.unit || '';
      if (unit && unit.trim() && !unit.includes('${') && !unit.includes('${unit}')) {
        return `${num}${unit}`;
      }
      return num;
    }
  
    function buildOption(type){
      // 너가 가진 공통 option 함수 “세팅 방식” 그대로 사용(전역 option 세팅)
      // 타입 정규화 (공백 제거, 소문자 변환)
      const normalizedType = (type || '').trim().toLowerCase();
      
      if(normalizedType === 'bar-x') {
        basicBarChartOptionX();
      } else if(normalizedType === 'bar-y') {
        console.log('[ZDCharts] buildOption: Calling basicBarChartOptionY()');
        basicBarChartOptionY();
      } else if(normalizedType === 'bar-center') {
        basicBarChartOptionCenter();
      } else if(normalizedType === 'bar-total-x') {
        basicBarChartOptionTotalMaxX();
      } else if(normalizedType === 'bar-total-y') {
        basicBarChartOptionTotalMaxY();
      } else if(normalizedType === 'pie') {
        pieChartOption();
      } else {
        console.warn('[ZDCharts] Unknown chart type:', type, '- using bar-x as default');
        basicBarChartOptionX();
      }
  
      // ⚠️ 차트 여러 개면 option 공유로 꼬일 수 있어서 복사본 반환
      return JSON.parse(JSON.stringify(option));
    }
  
    function injectUnitFormatters(opt, meta){
      // yAxis 처리 (배열 또는 객체, value 타입만 - category 타입은 제외)
      if (opt.yAxis) {
        if (Array.isArray(opt.yAxis)) {
          opt.yAxis.forEach(axis => {
            if (axis && axis.type === 'value' && axis.axisLabel) {
              axis.axisLabel.formatter = function(v){ return formatValue(v, meta); };
            }
          });
        } else if (opt.yAxis.type === 'value' && opt.yAxis.axisLabel) {
          opt.yAxis.axisLabel.formatter = function(v){ return formatValue(v, meta); };
        }
      }
      
      // xAxis 처리 (배열 또는 객체, value 타입만)
      if (opt.xAxis) {
        if (Array.isArray(opt.xAxis)) {
          opt.xAxis.forEach(axis => {
            if (axis && axis.type === 'value' && axis.axisLabel) {
              axis.axisLabel.formatter = function(v){ return formatValue(v, meta); };
            }
          });
        } else if (opt.xAxis.type === 'value' && opt.xAxis.axisLabel) {
          opt.xAxis.axisLabel.formatter = function(v){ return formatValue(v, meta); };
        }
      }
      
      return opt;
    }
  
    function renderOne(el){
      const id = el.id;
      const type = el.dataset.zdChart;
      const data = window.ZD_CHART_DATA && window.ZD_CHART_DATA[id];
      if(!id || !type || !data) return;

      const meta = getMeta(el);
      
      // 타입 정규화 (buildOption과 동일하게)
      const normalizedType = (type || '').trim().toLowerCase();
      
      let opt = buildOption(type);
      opt = injectUnitFormatters(opt, meta);
      
      // 디버깅: 타입 확인
      console.log('[ZDCharts] Chart ID:', id);
      console.log('[ZDCharts] Original Type:', type, 'Normalized:', normalizedType);
      console.log('[ZDCharts] Option check - xAxis type:', opt.xAxis?.type, 'yAxis type:', opt.yAxis?.type);
      console.log('[ZDCharts] yAxis data before:', opt.yAxis?.data);
      console.log('[ZDCharts] Data provided - xAxis:', data.xAxis, 'yAxis:', data.yAxis);

      // 축 데이터 주입
      if (data.xAxis && opt.xAxis) {
        if (Array.isArray(opt.xAxis) && opt.xAxis[0]) {
          opt.xAxis[0].data = data.xAxis;
        } else if (!Array.isArray(opt.xAxis)) {
          opt.xAxis.data = data.xAxis;
        }
      }
      if (data.yAxis && opt.yAxis) {
        if (Array.isArray(opt.yAxis) && opt.yAxis[0]) {
          opt.yAxis[0].data = data.yAxis;
          console.log('[ZDCharts] yAxis data set (array):', opt.yAxis[0].data);
        } else if (!Array.isArray(opt.yAxis)) {
          opt.yAxis.data = data.yAxis;
          console.log('[ZDCharts] yAxis data set (object):', opt.yAxis.data);
        }
      }

      // series 주입 - 기존 옵션 보존하면서 병합
      if (data.series) {
        const isBarChart = normalizedType.startsWith('bar');
        const isPieChart = normalizedType === 'pie';
        
        if (isPieChart && opt.series && opt.series[0]) {
          // pie 차트: 기존 opt.series[0]의 모든 옵션 보존, data.series의 데이터만 병합
          const baseSeries = opt.series[0];
          opt.series = data.series.map((s, idx) => ({
            ...baseSeries,  // 기존 옵션들 (radius, center, label, itemStyle 등)
            ...s,           // data.series의 옵션으로 덮어쓰기
            type: s.type || 'pie',
            data: s.data || baseSeries.data
          }));
        } else {
          // bar 차트: 기본 series 템플릿(barWidth, itemStyle 등) 보존하면서 병합
          const baseSeriesTemplate = opt.series && opt.series[0] ? opt.series[0] : {
            type: 'bar',
            barWidth: '24px',
            itemStyle: {
              barBorderRadius: [2, 2, 0, 0]
            }
          };
          opt.series = data.series.map((s, idx) => {
            // 기본 템플릿 사용 (name, data는 제외하고 공통 옵션만)
            return {
              ...baseSeriesTemplate,  // 기본 옵션 (barWidth, itemStyle 등)
              ...s,                    // data.series로 덮어쓰기 (name, data, color 등)
              type: s.type || (isBarChart ? 'bar' : 'bar')
            };
          });
        }
      }

      // pie 데이터 주입(페이지에서 data로 내려주는 경우)
      if (normalizedType === 'pie' && data.data && opt.series && opt.series[0]) {
        opt.series[0].data = data.data;
      }

      // 툴팁에 valType 추가 (bar 차트만)
      if (normalizedType.startsWith('bar') && opt.tooltip && opt.series) {
        const originalFormatter = opt.tooltip.formatter;
        opt.tooltip.formatter = function(params) {
          // params가 배열이면 (axis trigger)
          if (Array.isArray(params)) {
            let tooltip = `${params[0].axisValue}<br/>`;
            params.forEach(p => {
              const seriesIndex = p.seriesIndex;
              const series = opt.series[seriesIndex];
              const valType = series?.valType || '';
              const value = p.value != null ? p.value.toLocaleString() : '-';
              tooltip += `${p.marker} ${p.seriesName} ${value}${valType}<br/>`;
            });
            return tooltip;
          } 
          // params가 단일 객체면 (item trigger - pie 차트 등)
          else if (params && typeof params === 'object') {
            const seriesIndex = params.seriesIndex;
            const series = opt.series[seriesIndex];
            const valType = series?.valType || '';
            const value = params.value != null ? params.value.toLocaleString() : '-';
            return `${params.marker}${params.name}<br/>${params.seriesName}: ${value}${valType}`;
          }
          // 기존 formatter가 있으면 사용
          if (typeof originalFormatter === 'function') {
            return originalFormatter(params);
          }
          return '';
        };
      }

      const chart = echarts.init(el);
      chart.setOption(opt);
      
      // 차트 인스턴스 저장 (리사이즈용)
      chartInstances.set(id, chart);
  
      // 파이 중앙 텍스트(기존 함수 재사용)
      if (normalizedType === 'pie') attachPieCenter(chart, id);
  
      // 테이블 연동
      if (meta.legendTarget) {
        // series.valType 없으면 data-zd-unit을 테이블 단위로 쓰고 싶다면:
        opt.series.forEach(s => { if(!s.valType) s.valType = meta.unit; });
  
        renderEchartTableLegend(chart, meta.legendTarget, opt.series);
      }
    }
  
    // 리사이즈 이벤트 핸들러 (디바운싱 적용)
    let resizeTimer = null;
    function handleResize() {
      if (resizeTimer) {
        clearTimeout(resizeTimer);
      }
      resizeTimer = setTimeout(() => {
        chartInstances.forEach((chart, id) => {
          try {
            if (chart && !chart.isDisposed()) {
              chart.resize();
            }
          } catch (e) {
            console.warn('[ZDCharts] Resize error for chart:', id, e);
            // 에러 발생 시 인스턴스 제거
            chartInstances.delete(id);
          }
        });
      }, 150); // 150ms 디바운싱
    }
    
    // window resize 이벤트 리스너 등록 (한 번만)
    if (!window._zdChartsResizeListener) {
      window.addEventListener('resize', handleResize);
      window._zdChartsResizeListener = true;
    }
  
    window.ZDCharts = window.ZDCharts || {};
    window.ZDCharts.init = function(root){
      const base = root || document;
      base.querySelectorAll('[data-zd-chart]').forEach(renderOne);
    };
    
    // 차트 인스턴스 제거 함수 (필요시 사용)
    window.ZDCharts.dispose = function(chartId) {
      if (chartId) {
        const chart = chartInstances.get(chartId);
        if (chart && !chart.isDisposed()) {
          chart.dispose();
        }
        chartInstances.delete(chartId);
      } else {
        // 모든 차트 제거
        chartInstances.forEach((chart, id) => {
          if (chart && !chart.isDisposed()) {
            chart.dispose();
          }
        });
        chartInstances.clear();
      }
    };
  })();