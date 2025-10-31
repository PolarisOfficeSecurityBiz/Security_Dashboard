document.addEventListener("DOMContentLoaded", () => {
  const monthSelect = document.getElementById("monthSelect");
  const serviceSelect = document.getElementById("serviceSelect");
  const tableBody = document.getElementById("partnerBody");
  const summaryRow = document.getElementById("summaryRow");

  const sumJoin = document.getElementById("sumJoin");
  const sumLeave = document.getElementById("sumLeave");
  const sumRetain = document.getElementById("sumRetain");
  const sumTotal = document.getElementById("sumTotal");

  const excelBtn = document.getElementById("excelBtn");

  // ⚡ 데이터 렌더링 (mock 예시)
  function renderData() {
    const month = parseInt(monthSelect.value);
    const service = serviceSelect.value;
    const days = 31; // 임시: 31일 기준

    let totalJoin = 0, totalLeave = 0, totalRetain = 0, totalAmount = 0;
    let html = "";

    for (let d = 1; d <= days; d++) {
      const join = 100 + d;
      const leave = 20 + Math.floor(d / 2);
      const retain = 60 + Math.floor(d / 3);
      const cpi = service === "제휴사 B" ? 1200 : 1000;
      const rs = service === "제휴사 B" ? 150 : 200;
      const total = join * cpi + retain * rs;

      totalJoin += join;
      totalLeave += leave;
      totalRetain += retain;
      totalAmount += total;

      html += `
        <tr>
          <td>${d}일</td>
          <td>${join.toLocaleString()}</td>
          <td>${leave.toLocaleString()}</td>
          <td>${retain.toLocaleString()}</td>
          <td>₩${cpi.toLocaleString()}</td>
          <td>₩${rs.toLocaleString()}</td>
          <td>₩${total.toLocaleString()}</td>
        </tr>
      `;
    }

    tableBody.innerHTML = html;
    summaryRow.style.display = "table-footer-group";

    sumJoin.textContent = totalJoin.toLocaleString();
    sumLeave.textContent = totalLeave.toLocaleString();
    sumRetain.textContent = totalRetain.toLocaleString();
    sumTotal.textContent = "₩" + totalAmount.toLocaleString();
  }

  monthSelect.addEventListener("change", renderData);
  serviceSelect.addEventListener("change", renderData);

  excelBtn.addEventListener("click", () => {
    const month = monthSelect.value;
    const service = serviceSelect.value;
    window.location.href = `/customer/settlement/excel?month=${month}&service=${encodeURIComponent(service)}`;
  });
});
