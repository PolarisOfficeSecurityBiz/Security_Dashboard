document.addEventListener("DOMContentLoaded", () => {
  console.log("✅ settlement.js loaded!");

  // 🔹 탭 전환
  const tabs = document.querySelectorAll(".tab-btn");
  const contents = document.querySelectorAll(".tab-content");

  tabs.forEach((tab) => {
    tab.addEventListener("click", () => {
      tabs.forEach((t) => t.classList.remove("active"));
      contents.forEach((c) => c.classList.remove("active"));

      tab.classList.add("active");
      document.getElementById(tab.dataset.tab).classList.add("active");
    });
  });

  // 🔹 정산 금액 계산
  const calcCells = document.querySelectorAll(".calc");
  calcCells.forEach((cell) => {
    const cpi = parseFloat(cell.dataset.cpi || 0);
    const rs = parseFloat(cell.dataset.rs || 0);
    const join = parseInt(cell.dataset.join || 0);
    const retain = parseInt(cell.dataset.retain || 0);
    const days = parseInt(cell.dataset.days || 0);
    const amount = join * cpi + retain * rs * days;
    cell.textContent = `₩${amount.toLocaleString()}`;
  });

  // 🔹 월 선택 시 제목 업데이트
  const monthSelect = document.getElementById("monthSelect");
  const title = document.getElementById("settlementTitle");

  monthSelect.addEventListener("change", () => {
    const month = monthSelect.value;
    const daysInMonth = new Date(2025, month, 0).getDate(); // 월별 일수 계산
    title.textContent = `${month}월 제휴사 정산 내역 (1일 ~ ${daysInMonth}일)`;

    console.log(`📅 ${month}월 선택됨`);
    // TODO: Ajax 요청으로 해당 월 데이터 다시 불러올 수도 있음
  });
});
