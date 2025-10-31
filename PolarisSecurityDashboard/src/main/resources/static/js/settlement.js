document.addEventListener("DOMContentLoaded", () => {
  console.log("✅ settlement.js loaded!");

  const monthSelect = document.getElementById("monthSelect");
  const serviceSelect = document.getElementById("serviceSelect");
  const partnerBody = document.getElementById("partnerBody");
  const title = document.getElementById("settlementTitle");
  const excelBtn = document.getElementById("excelBtn");

  async function loadData() {
    const month = monthSelect.value;
    const service = serviceSelect.value;

    console.log(`📅 ${month}월, 서비스: ${service}`);

    const res = await fetch(`/customer/settlement/api?month=${month}&service=${encodeURIComponent(service)}`);
    const data = await res.json();

    title.textContent = `${data.month}월 ${data.service} 정산 내역 (1일 ~ ${data.days}일)`;

    partnerBody.innerHTML = data.data.map(d => {
      const total = d.join * d.cpi + d.retain * d.rsRate;
      return `
        <tr>
          <td>${d.day}일</td>
          <td>${d.join}</td>
          <td>${d.leave}</td>
          <td>${d.retain}</td>
          <td>₩${d.cpi.toLocaleString()}</td>
          <td>₩${d.rsRate.toLocaleString()}</td>
          <td>₩${total.toLocaleString()}</td>
        </tr>
      `;
    }).join("");
  }

  monthSelect.addEventListener("change", loadData);
  serviceSelect.addEventListener("change", loadData);

  excelBtn.addEventListener("click", () => {
    const month = monthSelect.value;
    const service = serviceSelect.value;
    window.location.href = `/customer/settlement/excel?month=${month}&service=${encodeURIComponent(service)}`;
  });

  // 초기 로드
  loadData();
});
