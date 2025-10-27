document.addEventListener('DOMContentLoaded', () => {
  const tbody = document.getElementById('logTbody');
  const searchBtn = document.getElementById('searchBtn');
  const dateRange = document.getElementById('dateRange');
  const fromDate = document.getElementById('fromDate');
  const toDate = document.getElementById('toDate');
  const ctx = document.getElementById('channelChart').getContext('2d');

  let chart;

  // 날짜 필터 제어
  dateRange.addEventListener('change', () => {
    const custom = dateRange.value === 'custom';
    fromDate.disabled = toDate.disabled = !custom;
  });

  searchBtn.addEventListener('click', fetchLogs);

  async function fetchLogs() {
    let url = `/admin/secuone/logs/api?eventType=acquisition`;
    const now = new Date();
    let from, to;

    switch (dateRange.value) {
      case '1d': from = new Date(now - 86400000); break;
      case '7d': from = new Date(now - 604800000); break;
      case '30d': from = new Date(now - 2592000000); break;
      case 'custom':
        from = fromDate.value ? new Date(fromDate.value) : new Date(now - 604800000);
        to = toDate.value ? new Date(toDate.value) : now;
        break;
      default: from = new Date(now - 604800000);
    }

    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;">📡 불러오는 중...</td></tr>`;

    try {
      const res = await fetch(url);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();

      // 날짜 필터링
      const filtered = data.filter(log => {
        const t = new Date(log.eventTime);
        return (!from || t >= from) && (!to || t <= to);
      });

      renderTable(filtered);
      renderChart(filtered);
    } catch (err) {
      console.error(err);
      tbody.innerHTML = `<tr><td colspan="7" style="color:red;text-align:center;">❌ 데이터 로드 실패</td></tr>`;
    }
  }

  function renderTable(data) {
    if (data.length === 0) {
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
          backgroundColor: ['#5B8DEF', '#36CFC9', '#F759AB', '#FAAD14']
        }]
      },
      options: {
        plugins: {
          legend: { display: false },
          title: { display: true, text: '유입경로별 유입 수' }
        },
        scales: { y: { beginAtZero: true } }
      }
    });
  }

  function formatDate(iso) {
    const d = new Date(iso);
    return `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate()
      .toString().padStart(2, '0')} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes()
      .toString().padStart(2, '0')}`;
  }

  fetchLogs(); // 초기 실행
});
