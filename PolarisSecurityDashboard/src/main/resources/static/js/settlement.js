document.addEventListener("DOMContentLoaded", () => {
  // 🔹 탭 전환
  const tabs = document.querySelectorAll(".tab-btn");
  const contents = document.querySelectorAll(".tab-content");

  tabs.forEach((tab) => {
    tab.addEventListener("click", () => {
      // 모든 탭 비활성화
      tabs.forEach((t) => t.classList.remove("active"));
      contents.forEach((c) => c.classList.remove("active"));

      // 클릭된 탭과 연결된 콘텐츠 활성화
      tab.classList.add("active");
      const target = document.getElementById(tab.dataset.tab);
      if (target) target.classList.add("active");
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
});
