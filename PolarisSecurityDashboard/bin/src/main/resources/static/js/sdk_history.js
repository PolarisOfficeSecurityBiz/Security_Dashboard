document.addEventListener("DOMContentLoaded", () => {
  const modal = document.getElementById("editModal");
  const noteInput = document.getElementById("noteInput");
  const saveBtn = document.getElementById("saveEdit");
  const cancelBtn = document.getElementById("cancelEdit");
  const searchInput = document.getElementById("historySearch");
  const sortBtn = document.getElementById("sortBtn");
  const tableBody = document.querySelector(".history-table tbody");

  let currentEditId = null;
  let sortDesc = true;

  if (!modal) return;

  // ✏ 수정 → 모달 열기
  document.querySelectorAll(".edit-btn").forEach(btn => {
    btn.addEventListener("click", () => {
      currentEditId = btn.dataset.id;
      const noteSpan = document.getElementById("note-" + currentEditId);
      noteInput.value = noteSpan ? noteSpan.textContent.trim() : "";
      modal.classList.add("show");
      setTimeout(() => noteInput.focus(), 0);
    });
  });

  // ❌ 취소
  cancelBtn.addEventListener("click", () => {
    modal.classList.remove("show");
    currentEditId = null;
  });

  // 🔲 오버레이 클릭 닫기
  modal.addEventListener("click", e => {
    if (e.target === modal) modal.classList.remove("show");
  });

  // ⎋ ESC 닫기
  document.addEventListener("keydown", e => {
    if (e.key === "Escape") modal.classList.remove("show");
  });

  // 💾 저장
  saveBtn.addEventListener("click", async () => {
    if (!currentEditId) return;

    const newNote = noteInput.value.trim();
    const noteSpan = document.getElementById("note-" + currentEditId);

    // 🔐 CSRF 토큰 읽기
    const csrfToken  = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    const headers = { "Content-Type": "application/json" };
    if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

    const res = await fetch(`/admin/vguard/history/${currentEditId}/note`, {
      method: "PATCH",
      headers,
      body: JSON.stringify({ releaseNote: newNote })
    });

    if (res.ok) {
      noteSpan.textContent = newNote;
      modal.classList.remove("show");
      alert("릴리즈 노트가 수정되었습니다.");
    } else {
      const text = await res.text().catch(()=>"");
      alert("수정 중 오류가 발생했습니다.\n" + text);
    }
  });


  // 🔍 검색
  if (searchInput) {
    searchInput.addEventListener("keyup", () => {
      const q = searchInput.value.toLowerCase();
      tableBody.querySelectorAll("tr").forEach(tr => {
        const visible = tr.textContent.toLowerCase().includes(q);
        tr.style.display = visible ? "" : "none";
      });
    });
  }

  // 🔽 정렬 (등록일 컬럼 4번째 index=3)
  if (sortBtn) {
    sortBtn.addEventListener("click", () => {
      const rows = Array.from(tableBody.querySelectorAll("tr"));
      rows.sort((a, b) => {
        const aDate = new Date(a.children[3].textContent.trim());
        const bDate = new Date(b.children[3].textContent.trim());
        return sortDesc ? bDate - aDate : aDate - bDate;
      });
      sortDesc = !sortDesc;
      sortBtn.textContent = sortDesc ? "최신순" : "오래된순";
      rows.forEach(r => tableBody.appendChild(r));
    });
  }
});
