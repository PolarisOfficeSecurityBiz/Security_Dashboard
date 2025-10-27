document.addEventListener('DOMContentLoaded', () => {
  const tbody = document.getElementById('logTbody');
  const searchBtn = document.getElementById('searchBtn');
  const dateRange = document.getElementById('dateRange');
  const fromDate = document.getElementById('fromDate');
  const toDate = document.getElementById('toDate');
  const chartCanvas = document.getElementById('channelChart');
  const tableSection = document.querySelector('.table-section');

  if (!tbody || !searchBtn || !dateRange || !chartCanvas || !tableSection) {
    console.error("❌ secuone_logs.js: HTML 요소를 찾지 못했습니다.");
    return;
  }

  const ctx = chartCanvas.getContext('2d');
  let chart = null;
  let allData = []; // 전체 로그 캐싱
  let currentChannel = null; // 현재 클릭된 유입경로

  // 기본적으로 테이블 숨김
  tableSection.style.display = 'none';

  // 날짜 필터 제어
  dateRange.addEventListener('change', () => {
    const custom = dateRange.value === 'custom';
    fromDate.disabled = toDate.disabled = !custom;
  });

  searchBtn.addEventListener('click', fetchLogs);

  // 데이터 불러오기
  async function fetchLogs() {
    const url = `/admin/secuone/logs/api?eventType=acquisition`;
    const now = new Date();
    let from, to;

    switch (dateRange.value) {
      case '1d': from = new Date(now - 86400000); break;
      case '7d': from = new Date(now - 7 * 86400000); break;
      case '30d': from = new Date(now - 30 * 86400000); break;
      case 'custom':
        from = fromDate.value ? new Date(fromDate.value) : null;
        to = toDate.value ? new Date(toDate.value) : null;
        break;
      default: from = new Date(now - 7 * 86400000);
    }

    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;">📡 불러오는 중...</td></tr>`;

    try {
      const res = await fetch(url);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();

      // 날짜 필터
      allData = data.filter(log => {
        const t = new Date(log.eventTime);
        return (!from || t >= from) && (!to || t <= to);
      });

      renderChart(allData);
      tableSection.style.display = 'none'; // 다시 숨김
      currentChannel = null;

    } catch (err) {
      console.error('❌ 데이터 로드 실패:', err);
      tbody.innerHTML = `<tr><td colspan="7" style="color:red;text-align:center;">데이터 로드 실패</td></tr>`;
    }
  }

  // 테이블 렌더링
  function renderTable(data) {
    if (!data || data.length === 0) {
      tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;">데이터 없음</td></tr>`;
      return;
    }

    tbody.innerHTML = data.map(log => `
      <tr>
        <td>${log.id}</td>
        <td>${log.eventType === 'acquisition' ? '유입경로' : log.eventType}</td>
        <td>${log.sessionId || '-'}</td>
        <td>${log.ip || '-'}</td>
        <td>${log.acqChannel || '-'}</td>
        <td>${log.extra || '-'}</td>
        <td>${formatDate(log.eventTime)}</td>
      </tr>
    `).join('');
  }

  // 차트 렌더링
  function renderChart(data) {
    const counts = {};
    data.forEach(log => {
      const ch = log.acqChannel || '기타';
      counts[ch] = (counts[ch] || 0) + 1;
    });

    const labels = Object.keys(counts);
    const values = Object.values(counts);

    if (chart) chart.destroy();

    chart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels,
        datasets: [{
          label: '유입 수',
          data: values,
          borderWidth: 1,
          backgroundColor: ['#5B8DEF', '#36CFC9', '#F759AB', '#FAAD14', '#9254DE', '#13C2C2']
        }]
      },
      options: {
        plugins: {
          legend: { display: false },
          title: { display: true, text: '유입경로별 유입 수' }
        },
        scales: { y: { beginAtZero: true } },
        onClick: (evt, elements) => {
          if (elements.length > 0) {
            const index = elements[0].index;
            const selectedChannel = labels[index];
            toggleChannel(selectedChannel);
          }
        }
      }
    });
  }

  // 유입경로 클릭 시 해당 로그만 표시
  function toggleChannel(channel) {
    if (currentChannel === channel) {
      // 이미 선택된 항목 클릭 → 닫기
      tableSection.style.display = 'none';
      currentChannel = null;
      return;
    }

    const filtered = allData.filter(log => (log.acqChannel || '기타') === channel);
    renderTable(filtered);
    tableSection.style.display = 'block';
    currentChannel = channel;
  }

  // 날짜 포맷
  function formatDate(iso) {
    const d = new Date(iso);
    if (isNaN(d)) return '-';
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    const hh = String(d.getHours()).padStart(2, '0');
    const mi = String(d.getMinutes()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd} ${hh}:${mi}`;
  }

  fetchLogs(); // 초기 실행
});
