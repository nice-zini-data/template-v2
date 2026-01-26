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
        series: []
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
        series: []
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
        series: []
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
        series: []
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
        series: []
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
  