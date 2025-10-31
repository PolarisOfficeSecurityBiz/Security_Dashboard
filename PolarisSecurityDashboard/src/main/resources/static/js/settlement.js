document.addEventListener("DOMContentLoaded", () => {
  console.log("✅ settlement.js loaded");

  // 🔹 탭 전환 기능
  const tabs = document.querySelectorAll(".tab-btn");
  const contents = document.querySelectorAll(".tab-content");

  if (!tabs.length) {
    console.error("❌ No tabs found");
    return;
  }

  tabs.forEach((tab) => {
    tab.addEventListener("click", () => {
      tabs.forEach((t) => t.classList.remove("active"));
      contents.forEach((c) => c.classList.remove("active"));

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
