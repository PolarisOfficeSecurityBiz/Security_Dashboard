document.addEventListener('DOMContentLoaded', () => {
  const tbody = document.getElementById('logTbody');
  const searchBtn = document.getElementById('searchBtn');
  const eventTypeInput = document.getElementById('eventType');
  const keywordInput = document.getElementById('searchKeyword');

  async function fetchLogs() {
    const eventType = eventTypeInput.value;
    const keyword = keywordInput.value;
    let url = `/admin/secuone/logs/api?`;
    if (eventType) url += `eventType=${encodeURIComponent(eventType)}&`;
    if (keyword) url += `keyword=${encodeURIComponent(keyword)}`;

    tbody.innerHTML = `
      <tr><td colspan="8" style="text-align:center; color:#666;">📡 로그 불러오는 중...</td></tr>
    `;

    try {
      const res = await fetch(url);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();

      if (data.length === 0) {
        tbody.innerHTML = `
          <tr><td colspan="8" style="text-align:center; color:#999;">데이터가 없습니다.</td></tr>
        `;
        return;
      }

      tbody.innerHTML = data.map(log => `
        <tr>
          <td>${log.id}</td>
          <td>${log.eventType}</td>
          <td>${log.userId || '-'}</td>
          <td>${log.sessionId || '-'}</td>
          <td>${log.ip || '-'}</td>
          <td>${log.featureName || log.acqChannel || '-'}</td>
          <td>${log.extra || '-'}</td>
          <td>${formatDate(log.eventTime)}</td>
        </tr>
      `).join('');

    } catch (err) {
      console.error(err);
      tbody.innerHTML = `
        <tr><td colspan="8" style="text-align:center; color:red;">❌ 로그 불러오기 실패</td></tr>
      `;
    }
  }

  // 날짜 포맷 함수
  function formatDate(iso) {
    try {
      const d = new Date(iso);
      return `${d.getFullYear()}-${(d.getMonth() + 1)
        .toString().padStart(2, '0')}-${d.getDate()
        .toString().padStart(2, '0')} ${d.getHours()
        .toString().padStart(2, '0')}:${d.getMinutes()
        .toString().padStart(2, '0')}:${d.getSeconds()
        .toString().padStart(2, '0')}`;
    } catch {
      return '-';
    }
  }

  // 검색 버튼 클릭
  searchBtn.addEventListener('click', fetchLogs);

  // Enter 키로 검색
  keywordInput.addEventListener('keydown', e => {
    if (e.key === 'Enter') fetchLogs();
  });

  // 초기 로드 시 실행
  fetchLogs();
});
