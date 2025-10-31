document.addEventListener('DOMContentLoaded', function () {
  const el = document.getElementById('userChart');
  if (!el || !window.Chart) return;

  // 🧩 안전한 데이터 파싱 함수
  function parseSafeArray(value, length = 12) {
    try {
      if (!value) return Array(length).fill(0);
      if (Array.isArray(value)) return value;
      // 문자열로 넘어온 경우 (ex: "[1,2,3]")
      const parsed = JSON.parse(value);
      return Array.isArray(parsed) ? parsed : Array(length).fill(0);
    } catch (e) {
      return Array(length).fill(0);
    }
  }

  // ✅ 데이터 변환 (비었으면 기본값 0)
  const labels = window.chartLabels?.length
    ? window.chartLabels
    : ['1월','2월','3월','4월','5월','6월','7월','8월','9월','10월','11월','12월'];

  const dataNew = parseSafeArray(window.chartDataNew);
  const dataReturn = parseSafeArray(window.chartDataReturn);

  // ✅ 진짜 데이터가 있는지 판단
  const hasData =
    dataNew.some(v => Number(v) > 0) ||
    dataReturn.some(v => Number(v) > 0);

  // ✅ Chart.js 렌더링
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

  // ✅ 데이터 없으면 오버레이 표시
  const overlay = document.getElementById('noDataOverlay');
  if (overlay) overlay.style.display = hasData ? 'none' : 'flex';
});
