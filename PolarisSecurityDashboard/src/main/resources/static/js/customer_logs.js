document.addEventListener("DOMContentLoaded", () => {
  const tbody = document.getElementById("logTbody");
  const searchBtn = document.getElementById("searchBtn");
  const fromDate = document.getElementById("fromDate");
  const toDate = document.getElementById("toDate");
  const chartCanvas = document.getElementById("dailyChart");

  let chartInstance = null;

  // ✅ 기본 도메인 (템플릿에서 모델로 주입되면 전역 변수로 자동 렌더됨)
  const domain =
    document.querySelector("h2").textContent.split(" ")[0] || "m.yebyeol.co.kr";

  // 페이지 로드 시 초기 데이터 불러오기
  loadLogs();

  searchBtn.addEventListener("click", () => {
    loadLogs(fromDate.value, toDate.value);
  });

  // ✅ 로그 로딩 함수
  async function loadLogs(from, to) {
    tbody.innerHTML = `<tr><td colspan="5" class="empty">데이터 불러오는 중...</td></tr>`;

    try {
      const query = new URLSearchParams();
      if (domain) query.append("domain", domain);
      if (from) query.append("from", from);
      if (to) query.append("to", to);

      // 백엔드 API 호출 (LogController 기준)
      const res = await fetch(`/api/logs/by-domain?${query.toString()}`);

      if (!res.ok) throw new Error("API 요청 실패: " + res.status);

      const data = await res.json();

      if (!data || data.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="empty">로그 데이터가 없습니다.</td></tr>`;
        updateChart([]);
        return;
      }

      // ✅ 테이블 렌더링
      tbody.innerHTML = "";
      data.forEach(log => {
        // createdAt: "2025-10-29T13:11:05" → 날짜/시간 분리
        const dateObj = new Date(log.createdAt);
        const dateStr = dateObj.toLocaleDateString("ko-KR");
        const timeStr = dateObj.toLocaleTimeString("ko-KR");

        const row = `
          <tr>
            <td>${dateStr}</td>
            <td>${timeStr}</td>
            <td>${log.ip ?? "-"}</td>
            <td>${log.page ?? "-"}</td>
            <td title="${log.userAgent ?? ""}">
              ${log.userAgent ? log.userAgent.slice(0, 40) + "..." : "-"}
            </td>
          </tr>`;
        tbody.insertAdjacentHTML("beforeend", row);
      });

      updateChart(data);
    } catch (err) {
      console.error("❌ 로그 불러오기 실패:", err);
      tbody.innerHTML = `<tr><td colspan="5" class="empty">데이터를 불러오지 못했습니다.</td></tr>`;
    }
  }

  // ✅ 일자별 로그 개수 차트
  function updateChart(logs) {
    const counts = {};
    logs.forEach(log => {
      const day = new Date(log.createdAt).toLocaleDateString("ko-KR");
      counts[day] = (counts[day] || 0) + 1;
    });

    const labels = Object.keys(counts);
    const values = Object.values(counts);

    if (chartInstance) chartInstance.destroy();

    chartInstance = new Chart(chartCanvas, {
      type: "bar",
      data: {
        labels,
        datasets: [
          {
            label: "일별 접속 수",
            data: values,
            backgroundColor: "#19a974",
          },
        ],
      },
      options: {
        responsive: true,
        scales: {
          y: {
            beginAtZero: true,
            ticks: { stepSize: 1 },
          },
        },
        plugins: {
          legend: { display: false },
        },
      },
    });
  }
});
