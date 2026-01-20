$(function(){
    dashboardChart();
    changeService();
    dashInfoBox();
});

const dashboardChart = () => {
    dashboardChart01("basicBarChartOptionX", {});
    dashboardChart02("pieChartOption", {});
    dashboardChart03("pieChartOption", {});
}


const dashboardChart01 = (gubun, data) => {
    var chartDom = document.getElementById("dashboardChart01");
    var myChart = echarts.init(chartDom);
  
    eval(gubun + "()");
    let newData = data;

    newData = [
        { name: "월", val: 123},
        { name: "화", val: 83},
        { name: "수", val: 123},
        { name: "목", val: 93},
        { name: "금", val: 106},
        { name: "토", val: 50},
        { name: "일", val: 30},
    ];
    // X축 데이터와 두 개의 시리즈 데이터 추출
    const xAxisData = newData.map(item => item.name);
    const dataValue = newData.map(item => item.val);

    option && myChart.setOption(option);
    myChart.setOption({
        // 반응형
        media: [
            {
            query: { maxWidth: 600 },   // 1280 미만이면 아래 가로
                option: {
                    xAxis:{axisLabel:{formatter: (v, i) => {
                        if (i === 0 || i === xAxisData.length - 1) {
                            const item = newData.find(d => d.name === v);
                            return item ? `${v}\n(${item.val})` : v;
                        }
                        return '';
                    }}}
                }
            }
        ]
    }, { replaceMerge: ['legend'] });

    myChart.setOption({
        tooltip: {
            trigger: 'axis',
            formatter: function(params) {
                let tooltip = `${params[0].axisValue}<br/>`;
                params.forEach(p => {
                    tooltip += `${p.marker} ${p.seriesName}: ${p.value.toLocaleString()}%<br/>`;
                });
                return tooltip;
            }
        },
        grid: {
            top: '5%', left: '0', right: '0', bottom: '6%',
        },
        xAxis: {
            data: xAxisData,
            axisLabel: {
                formatter: function(value, index) {
                    const item = newData.find(d => d.name === value);
                    return item ? `${value}` : value;
                }
            },
            axisLine: {show: false}
        },
        yAxis: {
            axisLine: {show: false},
            axisLabel: {show: false},
        },
        series: [
            {
                name:'매출 비중',
                type: 'bar',
                data: dataValue,
                barWidth: '24px',
                itemStyle: {
                    color: color[0],
                    barBorderRadius: [2, 2, 2, 2]
                },
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
    });

    //범례 테이블 - 요일별 비중 및 증감율
    const $container = $('#dashboardChart01Table');
    
    // 요일 전체 이름 매핑
    const dayNames = {
        '월': '월요일',
        '화': '화요일',
        '수': '수요일',
        '목': '목요일',
        '금': '금요일',
        '토': '토요일',
        '일': '일요일'
    };
    
    // 비중 계산 (전체 합계 대비)
    const total = dataValue.reduce((sum, val) => sum + val, 0);
    const percentages = dataValue.map(val => ((val / total) * 100).toFixed(1));
    
    // 증감율 하드코딩 (이미지 기준)
    const changeRates = [
        { value: 0.8, isPositive: true },   // 월요일
        { value: -0.3, isPositive: false },  // 화요일
        { value: 1.2, isPositive: true },   // 수요일
        { value: -0.5, isPositive: false }, // 목요일
        { value: 1.1, isPositive: true },   // 금요일
        { value: -1.8, isPositive: false }, // 토요일
        { value: -0.5, isPositive: false }  // 일요일
    ];
    
    let html = `
      <table class="legend-table">
        <thead>
          <tr>
            <th class="text-left">요일</th>
            <th class="text-right">비중</th>
            <th class="text-right">증감폭</th>
          </tr>
        </thead>
        <tbody>
          ${xAxisData.map((day, idx) => {
            const changeRate = changeRates[idx];
            const sign = changeRate.value >= 0 ? '+' : '';
            const colorClass = changeRate.isPositive ? '!text-rose-500' : '!text-blue-500';
            return `
            <tr>
              <td class="text-left">${dayNames[day] || day}</td>
              <td class="text-right">${percentages[idx]}%</td>
              <td class="text-right ${colorClass}">${sign}${changeRate.value.toFixed(1)} pp</td>
            </tr>
          `;
          }).join('')}
        </tbody>
      </table>`;
    
    $container.html(html);

    new ResizeObserver(() => myChart.resize()).observe(chartDom);
};
  
const dashboardChart02 = (gubun, data) => {
    var chartDom = document.getElementById('dashboardChart02');
    var myChart = echarts.init(chartDom);

    eval(gubun + "()");

    let newData = [];

    if(Object.keys(data).length === 0){
        newData = [
            {name:'소매유통업', val:31},
            {name:'음식업', val:21},
            {name:'생활서비스업', val:17},
            {name:'의료서비스업', val:11},
            {name:'교육서비스업', val:5},
            {name:'여가서비스업', val:3},
            {name:'기타', val:2},
        ];
    }else{
        newData = data;
    }

    let dataset = newData.map((item, index) => ({
        name: item.name,
        value: item.val,
        itemStyle: { color: color[index] }
    }));
      
    option && myChart.setOption(option);

    myChart.setOption({
        tooltip: {
            trigger: 'item',
            formatter: function(params) {
                return `${params.name}: ${params.value}%`;
            }
        },
        legend:{
            show: false,
        },
        series: {
            name:'',
            data: dataset,
            radius: ['60%', '95%'],
            center: ['50%', '50%'],
        },
    }, { replaceMerge: ['legend'] });

    attachPieCenter(myChart, 'bizSummaryChart01');
    
    new ResizeObserver(() => myChart.resize()).observe(chartDom);
}

const dashboardChart03 = (gubun, data) => {
    var chartDom = document.getElementById('dashboardChart03');
    var myChart = echarts.init(chartDom);

    eval(gubun + "()");

    let newData = [];

    if(Object.keys(data).length === 0){
        newData = [
            {name:'상위 5곳 매출', val:31},
            {name:'기타', val:69},
        ];
    }else{
        newData = data;
    }

    let dataset = newData.map((item, index) => ({
        name: item.name,
        value: item.val,
        itemStyle: { color: colorPie[index] }
    }));

    option && myChart.setOption(option);
    
    myChart.setOption({
        tooltip: {
            trigger: 'item',
            formatter: function(params) {
                return `${params.name}: ${params.value}%`;
            }
        },
        legend:{
            show: false,
        },
        series: {
            name:'',
            data: dataset,
            radius: ['60%', '95%'],
            center: ['50%', '50%'],
        },
    }, { replaceMerge: ['legend'] });

    attachPieCenter(myChart, 'bizSummaryChart01');
    
    new ResizeObserver(() => myChart.resize()).observe(chartDom);
}

const dashInfoBox = () => {
    $('.dashInfoBoxWrap').on('click', function(){
        $(this).children('.dashInfoBox').toggleClass('active');
    });
}

const changeService = () => {
    let lastScrollTop = 0;

    $('.dashboardWrap').on('scroll touchmove', function(){
    const currentScrollTop = $(this).scrollTop();

    if (Math.abs(currentScrollTop - lastScrollTop) < 5) return;

    if (currentScrollTop < lastScrollTop) {
        $('.changeServiceBtn').removeClass('hidden');
    } else {
        $('.changeServiceBtn').addClass('hidden');
    }

    lastScrollTop = currentScrollTop;
    });

}