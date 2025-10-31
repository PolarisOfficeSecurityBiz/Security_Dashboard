document.addEventListener('DOMContentLoaded', function () {
  const el = document.getElementById('userChart');
  if (!el || !window.Chart) return;

  // 🔹 데이터 가져오기 (thymeleaf 주입 변수)
  const labels = window.chartLabels?.length ? window.chartLabels
    : ['1월','2월','3월','4월','5월','6월','7월','8월','9월','10월','11월','12월'];

  const dataNew = window.chartDataNew?.length ? window.chartDataNew : Array(12).fill(0);
  const dataReturn = window.chartDataReturn?.length ? window.chartDataReturn : Array(12).fill(0);

  const hasData = dataNew.some(v => v > 0) || dataReturn.some(v => v > 0);

  // 🔹 차트 렌더링
  new Chart(el, {
    type: 'bar',
    data: {
      labels,
      datasets: [
        {
          label: '신규 유입',
          data: dataNew,
          backgroundColor: 'rgba(108, 122, 255, 0.7)',
          borderRadius: 6
        },
        {
          label: '재방문',
          data: dataReturn,
          backgroundColor: 'rgba(225, 165, 255, 0.7)',
          borderRadius: 6
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { position: 'bottom' } },
      scales: { y: { beginAtZero: true } }
    }
  });

  // 🔹 데이터 없을 시 오버레이 표시
  const overlay = document.getElementById('noDataOverlay');
  if (overlay && !hasData) overlay.style.display = 'flex';
});
