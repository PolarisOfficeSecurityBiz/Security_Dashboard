document.addEventListener("DOMContentLoaded", () => {
  const chartCanvas = document.getElementById("chart");

  // Thymeleaf에서 전달된 데이터 (서버 사이드에서 렌더링된 logEntries)
  const logs = window.logsData || []; // 혹시 inline으로 주입하고 싶을 때 대비용

  if (!chartCanvas || logs.length === 0) return;

  // 로그 타입별 집계
  const typeCount = {};
  logs.forEach(log => {
    const key = log.logType || "기타";
    typeCount[key] = (typeCount[key] || 0) + 1;
  });

  new Chart(chartCanvas, {
    type: "pie",
    data: {
      labels: Object.keys(typeCount),
      datasets: [{
        data: Object.values(typeCount),
        backgroundColor: ["#19a974", "#f39c12", "#e74c3c", "#3498db"],
      }],
    },
    options: {
      plugins: {
        legend: { position: "bottom" },
        title: {
          display: true,
          text: "보안 로그 유형별 분포",
          font: { size: 16 },
        },
      },
    },
  });
});
