document.addEventListener("DOMContentLoaded", () => {
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
    const cpi = parseFloat(cell.dataset.cpi);
    const rs = parseFloat(cell.dataset.rs);
    const join = parseInt(cell.dataset.join);
    const retain = parseInt(cell.dataset.retain);
    const days = parseInt(cell.dataset.days);

    const amount = join * cpi + retain * rs * days;
    cell.textContent = `₩${amount.toLocaleString()}`;
  });
});
