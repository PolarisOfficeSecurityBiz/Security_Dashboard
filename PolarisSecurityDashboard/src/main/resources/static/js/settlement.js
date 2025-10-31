window.addEventListener("load", () => {
  console.log("✅ settlement.js loaded!");

  const tableBody = document.getElementById("partnerBody");
  const sumJoin = document.getElementById("sumJoin");
  const sumLeave = document.getElementById("sumLeave");
  const sumRetain = document.getElementById("sumRetain");
  const sumTotal = document.getElementById("sumTotal");

  if (!tableBody || !sumJoin) {
    console.error("❌ HTML 구조를 찾을 수 없습니다.");
    return;
  }

  const days = 31;
  let totalJoin = 0, totalLeave = 0, totalRetain = 0, totalAmount = 0;
  let html = "";

  for (let d = 1; d <= days; d++) {
    const join = 120 + Math.floor(Math.random() * 80);
    const leave = 40 + Math.floor(Math.random() * 20);
    const retain = 60 + Math.floor(Math.random() * 40);
    const cpi = 1000;
    const rs = 200;
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

  // ✅ 합계 표시
  sumJoin.textContent = totalJoin.toLocaleString();
  sumLeave.textContent = totalLeave.toLocaleString();
  sumRetain.textContent = totalRetain.toLocaleString();
  sumTotal.textContent = "₩" + totalAmount.toLocaleString();

  console.log("✅ 합계 계산 완료", { totalJoin, totalLeave, totalRetain, totalAmount });
});
