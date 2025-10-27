document.addEventListener('DOMContentLoaded', () => {
  const tbody = document.getElementById('logTbody');
  const searchBtn = document.getElementById('searchBtn');
  const dateRange = document.getElementById('dateRange');
  const fromDate = document.getElementById('fromDate');
  const toDate = document.getElementById('toDate');
  const channelCanvas = document.getElementById('channelChart');
  const featureCanvas = document.getElementById('featureChart');
  const tableSection = document.querySelector('.table-section');

  if (!tbody || !searchBtn || !dateRange || !channelCanvas || !featureCanvas || !tableSection) {
    console.error("❌ secuone_logs.js: 필수 HTML 요소를 찾지 못했습니다.");
    return;
  }

  let allData = [];
  let currentFilter = null; // 현재 클릭된 필터 (유입경로 or 기능명)
  let chartChannel = null;
  let chartFeature = null;

  const ctxChannel = channelCanvas.getContext('2d');
  const ctxFeature = featureCanvas.getContext('2d');

  // 기본적으로 테이블 숨김
  tableSection.style.display = 'none';

  // 날짜 필터 제어
  dateRange.addEventListener('change', () => {
    const custom = dateRange.value === 'custom';
    fromDate.disabled = toDate.disabled = !custom;
  });

  searchBtn.addEventListener('click', fetchLogs);

  // 🧩 로그 데이터 불러오기
  async function fetchLogs() {
    const url = `/api/log/events`; // 서버에서 모든 로그(acquisition + feature_click) 제공
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

      // 날짜 필터 적용
      allData = data.filter(log => {
        const t = new Date(log.eventTime);
        return (!from || t >= from) && (!to || t <= to);
      });

      renderChannelChart(allData);
      renderFeatureChart(allData);

      tableSection.style.display = 'none';
      currentFilter = null;
    } catch (err) {
      console.error('❌ 데이터 로드 실패:', err);
      tbody.innerHTML = `<tr><td colspan="7" style="color:red;text-align:center;">데이터 로드 실패</td></tr>`;
    }
  }

  // ===============================
  // 🔹 유입경로별 차트
  // ===============================
  function renderChannelChart(data) {
    const filtered = data.filter(d => d.eventType === "acquisition");
    const counts = {};
    filtered.forEach(log => {
      const ch = log.acqChannel || '기타';
      counts[ch] = (counts[ch] || 0) + 1;
    });

    const labels = Object.keys(counts);
    const values = Object.values(counts);

    if (chartChannel) chartChannel.destroy();

    chartChannel = new Chart(ctxChannel, {
      type: 'bar',
      data: {
        labels,
        datasets: [{
          label: '유입 수',
          data: values,
          backgroundColor: ['#5B8DEF', '#36CFC9', '#F759AB', '#FAAD14', '#9254DE', '#13C2C2']
        }]
      },
      options: {
        plugins: {
          legend: { display: false },
          title: { display: true, text: '📈 유입경로별 유입 수' }
        },
        scales: { y: { beginAtZero: true } },
        onClick: (evt, elements) => {
          if (elements.length > 0) {
            const index = elements[0].index;
            const selected = labels[index];
            toggleFilter("channel", selected);
          }
        }
      }
    });
  }

  // ===============================
  // ⚙️ 주요 기능별 차트
  // ===============================
  function renderFeatureChart(data) {
    const filtered = data.filter(d => d.eventType === "feature_click");
    const counts = {};
    filtered.forEach(log => {
      const feature = log.featureName || '기타';
      counts[feature] = (counts[feature] || 0) + 1;
    });

    const labels = Object.keys(counts);
    const values = Object.values(counts);

    if (chartFeature) chartFeature.destroy();

    chartFeature = new Chart(ctxFeature, {
      type: 'bar',
      data: {
        labels,
        datasets: [{
          label: '기능 사용 횟수',
          data: values,
          backgroundColor: ['#1890FF', '#52C41A', '#FAAD14', '#F759AB', '#13C2C2', '#722ED1']
        }]
      },
      options: {
        plugins: {
          legend: { display: false },
          title: { display: true, text: '⚙️ 주요 기능별 사용 횟수' }
        },
        scales: { y: { beginAtZero: true } },
        onClick: (evt, elements) => {
          if (elements.length > 0) {
            const index = elements[0].index;
            const selected = labels[index];
            toggleFilter("feature", selected);
          }
        }
      }
    });
  }

  // ===============================
  // 🎯 차트 클릭 시 해당 로그만 표시
  // ===============================
  function toggleFilter(type, value) {
    if (currentFilter && currentFilter.type === type && currentFilter.value === value) {
      // 같은 항목 다시 클릭 → 닫기
      tableSection.style.display = 'none';
      currentFilter = null;
      return;
    }

    let filtered = [];
    if (type === "channel") {
      filtered = allData.filter(log => (log.acqChannel || '기타') === value);
    } else if (type === "feature") {
      filtered = allData.filter(log => (log.featureName || '기타') === value);
    }

    renderTable(filtered);
    tableSection.style.display = 'block';
    currentFilter = { type, value };
  }

  // ===============================
  // 📋 테이블 렌더링
  // ===============================
  function renderTable(data) {
    if (!data || data.length === 0) {
      tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;">데이터 없음</td></tr>`;
      return;
    }

    tbody.innerHTML = data.map(log => `
      <tr>
        <td>${log.id}</td>
        <td>${log.eventType === 'acquisition' ? '유입경로' : '기능 클릭'}</td>
        <td>${log.sessionId || '-'}</td>
        <td>${log.ip || '-'}</td>
        <td>${log.acqChannel || log.featureName || '-'}</td>
        <td>${log.extra || '-'}</td>
        <td>${formatDate(log.eventTime)}</td>
      </tr>
    `).join('');
  }

  // ===============================
  // 🕒 날짜 포맷
  // ===============================
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

  // 초기 실행
  fetchLogs();
});
