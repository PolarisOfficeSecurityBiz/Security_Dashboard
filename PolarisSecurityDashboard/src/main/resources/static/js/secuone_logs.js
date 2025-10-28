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

  // ===============================
  // 📡 전체 데이터 로드
  // ===============================
  async function loadAllData() {
    const days = getSelectedDays();

    // 두 API 병렬 호출
    Promise.all([
      fetchSecuOneLogs(),      // /admin/secuone/logs/api
      fetchSystemLogs(days)    // /api/logs/report
    ]).then(([secuLogs, systemLogs]) => {
      allSecuLogs = secuLogs || [];

      // 1️⃣ SecuOne 로그 렌더링
      renderChannelChart(allSecuLogs);
      renderFeatureChart(allSecuLogs);
      tableSection.style.display = 'none';
      currentFilter = null;

      // 2️⃣ 시스템 로그 카드 렌더링
      updateSummaryCards(systemLogs || []);
    }).catch(err => {
      console.error('❌ 데이터 로드 실패:', err);
      tbody.innerHTML = `<tr><td colspan="7" style="color:red;text-align:center;">데이터 로드 실패</td></tr>`;
    });
  }

  // ===============================
  // 🧩 API 1: SecuOne Logs
  // ===============================
  async function fetchSecuOneLogs() {
    const url = `/admin/secuone/logs/api`;
    const res = await fetch(url);
    if (!res.ok) throw new Error(`SecuOneLogs HTTP ${res.status}`);
    return await res.json();
  }

  // ===============================
  // 🧩 API 2: System Logs (MALWARE / REMOTE / ROOTING)
  // ===============================
  async function fetchSystemLogs(days) {
    const url = `/api/logs/report?days=${days}`;
    const res = await fetch(url);
    if (!res.ok) throw new Error(`SystemLogs HTTP ${res.status}`);
    return await res.json();
  }

  // ===============================
  // 📅 날짜 필터
  // ===============================
  function getSelectedDays() {
    switch (dateRange.value) {
      case '1d': return 1;
      case '7d': return 7;
      case '30d': return 30;
      default: return 7;
    }
  }

  // ===============================
  // 📊 요약 카드 업데이트
  // ===============================
  function updateSummaryCards(data) {
    const total = data.length;
    const malware = data.filter(l => l.type === 'MALWARE').length;
    const remote = data.filter(l => l.type === 'REMOTE').length;
    const rooting = data.filter(l => l.type === 'ROOTING').length;

    document.getElementById('todayCount')?.textContent = malware;
    document.getElementById('featureCount')?.textContent = remote;
    document.getElementById('totalCount')?.textContent = rooting;

    console.log("✅ System logs loaded:", { malware, remote, rooting });
  }

  // ===============================
  // 📈 유입경로별 차트
  // ===============================
  function renderChannelChart(data) {
    const filtered = data.filter(d => d.eventType === "acquisition");
    const counts = {};
    filtered.forEach(log => {
      const ch = log.acqChannel || '기타';
      counts[ch] = (counts[ch] || 0) + 1;
    });

    let labels = Object.keys(counts);
    let values = Object.values(counts);
    if (labels.length === 0) { labels = ['데이터 없음']; values = [0]; }

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
        plugins: { legend: { display: false } },
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
  // ⚙️ 주요 기능 사용률 차트
  // ===============================
  function renderFeatureChart(data) {
    const filtered = data.filter(d => d.eventType === "feature_click");
    const counts = {};
    filtered.forEach(log => {
      const feature = log.featureName || '기타';
      counts[feature] = (counts[feature] || 0) + 1;
    });

    let labels = Object.keys(counts);
    let values = Object.values(counts);
    if (labels.length === 0) { labels = ['데이터 없음']; values = [0]; }

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
        plugins: { legend: { display: false } },
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
  // 🎯 차트 클릭 시 로그 필터링
  // ===============================
  function toggleFilter(type, value) {
    if (currentFilter && currentFilter.type === type && currentFilter.value === value) {
      tableSection.style.display = 'none';
      currentFilter = null;
      return;
    }

    let filtered = [];
    if (type === "channel") {
      filtered = allSecuLogs.filter(log => (log.acqChannel || '기타') === value);
    } else if (type === "feature") {
      filtered = allSecuLogs.filter(log => (log.featureName || '기타') === value);
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
  loadAllData();
});
