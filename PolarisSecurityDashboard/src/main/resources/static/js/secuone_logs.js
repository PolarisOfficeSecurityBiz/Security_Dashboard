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
  let currentFilter = null; // 현재 클릭된 필터
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

  // ===============================
  // 📡 로그 데이터 불러오기
  // ===============================
  async function fetchLogs() {
    let days = 7;
    switch (dateRange.value) {
      case '1d': days = 1; break;
      case '7d': days = 7; break;
      case '30d': days = 30; break;
      default: days = 7;
    }

    const url = `/api/logs/report?days=${days}`;
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;">📡 불러오는 중...</td></tr>`;

    try {
      const res = await fetch(url);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();

      allData = data || [];

      renderChannelChart(allData);
      renderFeatureChart(allData);
      updateSummaryCards(allData);

      tableSection.style.display = 'none';
      currentFilter = null;

    } catch (err) {
      console.error('❌ 데이터 로드 실패:', err);
      tbody.innerHTML = `<tr><td colspan="7" style="color:red;text-align:center;">데이터 로드 실패</td></tr>`;
    }
  }

  // ===============================
  // 📊 요약 카드 업데이트
  // ===============================
  function updateSummaryCards(data) {
    const today = new Date().toISOString().slice(0, 10);
    const todayLogs = data.filter(l => (l.createdAt || '').startsWith(today));

    const total = data.length;
    const malware = data.filter(l => l.type === 'MALWARE').length;
    const remote = data.filter(l => l.type === 'REMOTE').length;
    const rooting = data.filter(l => l.type === 'ROOTING').length;
    const todayCount = todayLogs.length;

    document.getElementById('todayCount')?.textContent = todayCount;
    document.getElementById('featureCount')?.textContent = malware + remote + rooting;
    document.getElementById('totalCount')?.textContent = total;
  }

  // ===============================
  // 📈 유입경로별 차트
  // ===============================
  function renderChannelChart(data) {
    const counts = {};
    data.forEach(log => {
      const ch = log.domain || '기타';
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
          title: { display: true, text: '📈 도메인(유입경로)별 로그 수' }
        },
        scales: { y: { beginAtZero: true } },
        onClick: (evt, elements) => {
          if (elements.length > 0) {
            const index = elements[0].index;
            const selected = labels[index];
            toggleFilter("domain", selected);
          }
        }
      }
    });
  }

  // ===============================
  // ⚙️ 로그 타입별 차트 (MALWARE/REMOTE/ROOTING)
  // ===============================
  function renderFeatureChart(data) {
    const counts = {};
    data.forEach(log => {
      const type = log.type || '기타';
      counts[type] = (counts[type] || 0) + 1;
    });

    const labels = Object.keys(counts);
    const values = Object.values(counts);

    if (chartFeature) chartFeature.destroy();

    chartFeature = new Chart(ctxFeature, {
      type: 'bar',
      data: {
        labels,
        datasets: [{
          label: '발생 수',
          data: values,
          backgroundColor: ['#1890FF', '#52C41A', '#FAAD14', '#F759AB', '#13C2C2', '#722ED1']
        }]
      },
      options: {
        plugins: {
          legend: { display: false },
          title: { display: true, text: '⚙️ 로그 타입별 발생 수' }
        },
        scales: { y: { beginAtZero: true } },
        onClick: (evt, elements) => {
          if (elements.length > 0) {
            const index = elements[0].index;
            const selected = labels[index];
            toggleFilter("type", selected);
          }
        }
      }
    });
  }

  // ===============================
  // 🎯 차트 클릭 시 테이블 필터링
  // ===============================
  function toggleFilter(type, value) {
    if (currentFilter && currentFilter.type === type && currentFilter.value === value) {
      tableSection.style.display = 'none';
      currentFilter = null;
      return;
    }

    let filtered = [];
    if (type === "domain") {
      filtered = allData.filter(log => (log.domain || '기타') === value);
    } else if (type === "type") {
      filtered = allData.filter(log => (log.type || '기타') === value);
    }

    renderTable(filtered);
    tableSection.style.display = 'block';
    currentFilter = { type, value };
  }

  // ===============================
  // 📋 상세 로그 테이블 렌더링
  // ===============================
  function renderTable(data) {
    if (!data || data.length === 0) {
      tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;">데이터 없음</td></tr>`;
      return;
    }

    tbody.innerHTML = data.map(log => `
      <tr>
        <td>${log.id}</td>
        <td>${log.type || '-'}</td>
        <td>${log.sessionId || '-'}</td>
        <td>${log.ip || '-'}</td>
        <td>${log.domain || '-'}</td>
        <td>${log.detail || '-'}</td>
        <td>${formatDate(log.createdAt)}</td>
      </tr>
    `).join('');
  }

  // ===============================
  // 🕒 날짜 포맷 함수
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
