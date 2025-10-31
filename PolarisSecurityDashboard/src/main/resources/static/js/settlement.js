document.addEventListener("DOMContentLoaded", () => {
  console.log("✅ settlement.js loaded!");

  /* 🔹 탭 전환 */
  const tabs = document.querySelectorAll(".tab-btn");
  const contents = document.querySelectorAll(".tab-content");

  tabs.forEach(tab => {
    tab.addEventListener("click", () => {
      tabs.forEach(t => t.classList.remove("active"));
      contents.forEach(c => c.classList.remove("active"));
      tab.classList.add("active");
      document.getElementById(tab.dataset.tab).classList.add("active");
    });
  });

  /* 🔹 정산 금액 계산 */
  function calculateAll() {
    document.querySelectorAll(".calc").forEach(cell => {
      const cpi = parseFloat(cell.dataset.cpi || 0);
      const rs = parseFloat(cell.dataset.rs || 0);
      const join = parseInt(cell.dataset.join || 0);
      const retain = parseInt(cell.dataset.retain || 0);
      const days = parseInt(cell.dataset.days || 0);
      const amount = join * cpi + retain * rs * days;
      cell.textContent = `₩${amount.toLocaleString()}`;
    });
  }

  calculateAll();

  /* 🔹 월 선택 AJAX */
  const monthSelect = document.getElementById("monthSelect");
  const partnerBody = document.getElementById("partnerBody");
  const title = document.getElementById("settlementTitle");

  monthSelect.addEventListener("change", async () => {
    const month = monthSelect.value;
    console.log(`📅 ${month}월 선택됨`);

    try {
      const res = await fetch(`/customer/settlement/api?month=${month}`);
      if (!res.ok) throw new Error("서버 응답 실패");
      const data = await res.json();

      title.textContent = `${data.month}월 제휴사 정산 내역 (1일 ~ ${data.days}일)`;

      partnerBody.innerHTML = data.partners.map(p => `
        <tr>
          <td>${p.partnerName}</td>
          <td>${p.joinCount}</td>
          <td>₩${p.cpi.toLocaleString()}</td>
          <td>${p.retainCount}</td>
          <td>₩${p.rsRate.toLocaleString()}</td>
          <td>${p.days}</td>
          <td class="calc"
              data-cpi="${p.cpi}"
              data-rs="${p.rsRate}"
              data-join="${p.joinCount}"
              data-retain="${p.retainCount}"
              data-days="${p.days}">₩0</td>
        </tr>
      `).join("");

      calculateAll();
    } catch (err) {
      console.error("❌ AJAX 실패:", err);
      alert("데이터를 불러오는 중 오류가 발생했습니다.");
    }
  });
});
