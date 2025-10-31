document.addEventListener("DOMContentLoaded", () => {
  // ✅ DOM 로드 후 모든 요소 가져오기
  const monthSelect = document.getElementById("monthSelect");
  const serviceSelect = document.getElementById("serviceSelect");
  const tableBody = document.getElementById("partnerBody");
  const summaryRow = document.getElementById("summaryRow");

  // ✅ 합계 영역
  const sumJoin = document.getElementById("sumJoin");
  const sumLeave = document.getElementById("sumLeave");
  const sumRetain = document.getElementById("sumRetain");
  const sumTotal = document.getElementById("sumTotal");

  const excelBtn = document.getElementById("excelBtn");

  // ✅ 안전 확인 (없을 경우 로그)
  if (!tableBody || !summaryRow) {
    console.error("❌ settlement.html 구조 확인 필요: #partnerBody 또는 #summaryRow 없음");
    return;
  }

  /** ✅ 랜덤 데이터 생성 후 렌더링 */
  function renderData() {
    const month = parseInt(monthSelect.value);
    const service = serviceSelect.value;
    const days = 31;

    let totalJoin = 0;
    let totalLeave = 0;
    let totalRetain = 0;
    let totalAmount = 0;

    let html = "";

    for (let d = 1; d <= days; d++) {
      // 🔹 임시 랜덤 데이터 생성
      const join = 100 + Math.floor(Math.random() * 100);
      const leave = 30 + Math.floor(Math.random() * 30);
      const retain = 50 + Math.floor(Math.random() * 50);

      const cpi = service === "제휴사 B" ? 1200 : 1000;
      const rs = service === "제휴사 B" ? 150 : 200;
      const total = join * cpi + retain * rs;

      // 🔹 합계 누적
      totalJoin += join;
      totalLeave += leave;
      totalRetain += retain;
      totalAmount += total;

      // 🔹 테이블 행 추가
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

    // ✅ 데이터 렌더링
    tableBody.innerHTML = html;

    // ✅ 합계 표시
    summaryRow.style.display = "table-footer-group";

    // ✅ 합계 값 출력 (숫자 → 문자열 변환)
    sumJoin.textContent = totalJoin.toLocaleString();
    sumLeave.textContent = totalLeave.toLocaleString();
    sumRetain.textContent = totalRetain.toLocaleString();
    sumTotal.textContent = "₩" + totalAmount.toLocaleString();
  }

  // ✅ 페이지 진입 시 1회 렌더링
  renderData();

  // ✅ 필터 변경 시 재렌더링
  monthSelect.addEventListener("change", renderData);
  serviceSelect.addEventListener("change", renderData);

  // ✅ 엑셀 다운로드
  excelBtn.addEventListener("click", () => {
    const month = monthSelect.value;
    const service = serviceSelect.value;
    window.location.href = `/customer/settlement/excel?month=${month}&service=${encodeURIComponent(service)}`;
  });
});
