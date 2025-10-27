document.addEventListener('DOMContentLoaded', () => {
  const tbody = document.getElementById('logTbody');
  const searchBtn = document.getElementById('searchBtn');

  async function fetchLogs() {
    const eventType = document.getElementById('eventType').value;
    const keyword = document.getElementById('searchKeyword').value;
    let url = `/api/log/events?`;
    if (eventType) url += `eventType=${eventType}&`;
    if (keyword) url += `keyword=${encodeURIComponent(keyword)}`;

    tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;">불러오는 중...</td></tr>`;
    try {
      const res = await fetch(url);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();

      if (data.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;">데이터 없음</td></tr>`;
        return;
      }

      tbody.innerHTML = data.map(log => `
        <tr>
          <td>${log.id}</td>
          <td>${log.eventType}</td>
          <td>${log.userId ?? '-'}</td>
          <td>${log.sessionId ?? '-'}</td>
          <td>${log.ip ?? '-'}</td>
          <td>${log.featureName || log.acqChannel || '-'}</td>
          <td>${log.extra || '-'}</td>
          <td>${new Date(log.eventTime).toLocaleString()}</td>
        </tr>
      `).join('');

    } catch (err) {
      console.error(err);
      tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;color:red;">불러오기 실패</td></tr>`;
    }
  }

  searchBtn.addEventListener('click', fetchLogs);
  fetchLogs(); // 초기 로드
});
