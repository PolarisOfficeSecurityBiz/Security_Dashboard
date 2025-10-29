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
    console.error("❌ 필수 HTML 요소를 찾지 못했습니다.");
    return;
  }

  const ctxChannel = channelCanvas.getContext('2d');
  const ctxFeature = featureCanvas.getContext('2d');
  let chartChannel = null;
  let chartFeature = null;
  let allSecuLogs = [];
  let currentFilter = null;

  // 기본적으로 테이블 숨김
  tableSection.style.display = 'none';

  // 날짜 필터 제어
  dateRange.addEventListener('change', () => {
    const custom = dateRange.value === 'custom';
    fromDate.disabled = toDate.disabled = !custom;
  });

  searchBtn.addEventListener('click', loadAllData);

  async function loadAllData() {
    const days = getSelectedDays();
    try {
      const [secuLogs, systemLogs] = await Promise.all([
        fetchSecuOneLogs(),
        fetchSystemLogs(days)
      ]);
      allSecuLogs = secuLogs || [];

      renderChannelChart(allSecuLogs);
      renderFeatureChart(allSecuLogs);
      tableSection.style.display = 'none';
      currentFilter = null;

      updateSummaryCards(systemLogs || []);
    } catch (err) {
      console.error('❌ 데이터 로드 실패:', err);
      tbody.innerHTML = `<tr><td colspan="7" style="color:red;text-align:center;">데이터 로드 실패</td></tr>`;
    }
  }

  async function fetchSecuOneLogs() {
    const url = '/admin/secuone/logs/api';
    const res = await fetch(url);
    if (!res.ok) throw new Error(`SecuOneLogs HTTP ${res.status}`);
    return res.json();
  }

  async function fetchSystemLogs(days) {
    const url = `/api/logs/report?days=${days}`;
    const res = await fetch(url);
    if (!res.ok) throw new Error(`SystemLogs HTTP ${res.status}`);
    return res.json();
  }

  function getSelectedDays() {
    switch (dateRange.value) {
      case '1d': return 1;
      case '7d': return 7;
      case '30d': return 30;
      default: return 7;
    }
  }

  function updateSummaryCards(data) {
    const malware = data.filter(l => l.type === 'MALWARE').length;
    const remote = data.filter(l => l.type === 'REMOTE').length;
    const rooting = data.filter(l => l.type === 'ROOTING').length;

    document.getElementById('todayCount').textContent = malware;
    document.getElementById('featureCount').textContent = remote;
    document.getElementById('totalCount').textContent = rooting;
  }

  function renderChannelChart(data) {
    const filtered = data.filter(d => d.eventType === "acquisition");
    const counts = {};
    filtered.forEach(log => {
      const ch = log.acqChannel || '기타';
      counts[ch] = (counts[ch] || 0) + 1;
    });

    const labels = Object.keys(counts).length ? Object.keys(counts) : ['데이터 없음'];
    const values = Object.values(counts).length ? Object.values(counts) : [0];

    if (chartChannel) chartChannel.destroy();
    chartChannel = new Chart(ctxChannel, {
      type: 'bar',
      data: { labels, datasets: [{ label: '유입 수', data: values, backgroundColor: '#5B8DEF' }] },
      options: {
        plugins: { legend: { display: false } },
        scales: { y: { beginAtZero: true } },
        onClick: (evt, el) => {
          if (el.length > 0) toggleFilter('channel', labels[el[0].index]);
        }
      }
    });
  }

  function renderFeatureChart(data) {
    const filtered = data.filter(d => d.eventType === "feature_click");
    const counts = {};
    filtered.forEach(log => {
      const feature = log.featureName || '기타';
      counts[feature] = (counts[feature] || 0) + 1;
    });

    const labels = Object.keys(counts).length ? Object.keys(counts) : ['데이터 없음'];
    const values = Object.values(counts).length ? Object.values(counts) : [0];

    if (chartFeature) chartFeature.destroy();
    chartFeature = new Chart(ctxFeature, {
      type: 'bar',
      data: { labels, datasets: [{ label: '기능 사용 횟수', data: values, backgroundColor: '#13C2C2' }] },
      options: {
        plugins: { legend: { display: false } },
        scales: { y: { beginAtZero: true } },
        onClick: (evt, el) => {
          if (el.length > 0) toggleFilter('feature', labels[el[0].index]);
        }
      }
    });
  }

  function toggleFilter(type, value) {
    if (currentFilter && currentFilter.type === type && currentFilter.value === value) {
      tableSection.style.display = 'none';
      currentFilter = null;
      return;
    }

    let filtered = [];
    if (type === 'channel') {
      filtered = allSecuLogs.filter(l => (l.acqChannel || '기타') === value);
    } else if (type === 'feature') {
      filtered = allSecuLogs.filter(l => (l.featureName || '기타') === value);
    }

    renderTable(filtered);
    tableSection.style.display = 'block';
    currentFilter = { type, value };
  }

  function renderTable(data) {
    if (!data.length) {
      tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;">데이터 없음</td></tr>`;
      return;
    }

    tbody.innerHTML = data.map(l => `
      <tr>
        <td>${l.id}</td>
        <td>${l.eventType === 'acquisition' ? '유입경로' : '기능 클릭'}</td>
        <td>${l.sessionId || '-'}</td>
        <td>${l.ip || '-'}</td>
        <td>${l.acqChannel || l.featureName || '-'}</td>
        <td>${l.extra || '-'}</td>
        <td>${formatDate(l.eventTime)}</td>
      </tr>
    `).join('');
  }

  function formatDate(iso) {
    const d = new Date(iso);
    if (isNaN(d)) return '-';
    return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`;
  }

  loadAllData();
});
