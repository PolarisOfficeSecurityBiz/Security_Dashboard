// /js/mypage.js
document.addEventListener("DOMContentLoaded", () => {
  const dlg = document.getElementById("pwdDialog");
  const openBtn = document.getElementById("openPwdDialog");
  const cancelBtn = document.getElementById("cancelPwdDialog");
  const cancelBtn2 = document.getElementById("cancelPwdDialog2");
  const pwdForm = document.getElementById("pwdForm");

  // ✅ 다이얼로그 열기
  if (openBtn && dlg) {
    openBtn.addEventListener("click", () => {
      if (typeof dlg.showModal === "function") {
        dlg.showModal();
      } else {
        dlg.setAttribute("open", "open");
        dlg.style.display = "block";
      }

      // 포커스 이동
      setTimeout(() => document.getElementById("curPwd")?.focus(), 50);

      // 스크롤 방지
      document.documentElement.style.overflow = "hidden";
    });
  }

  // ✅ 다이얼로그 닫기 함수
  const closeDialog = () => {
    if (!dlg) return;
    try {
      dlg.close();
    } catch (e) {
      dlg.removeAttribute("open");
      dlg.style.display = "none";
    }
    document.documentElement.style.overflow = "";
  };

  // 취소 버튼 (X, 취소)
  if (cancelBtn) cancelBtn.addEventListener("click", closeDialog);
  if (cancelBtn2) cancelBtn2.addEventListener("click", closeDialog);

  // 백드롭 클릭 시 닫기
  if (dlg) {
    dlg.addEventListener("click", (e) => {
      const rect = dlg.getBoundingClientRect();
      const inside =
        rect.top <= e.clientY &&
        e.clientY <= rect.bottom &&
        rect.left <= e.clientX &&
        e.clientX <= rect.right;

      if (!inside) closeDialog();
    });

    dlg.addEventListener("close", () => {
      document.documentElement.style.overflow = "";
    });
  }

  // ✅ 보기/숨기기 토글
  document.addEventListener("click", (e) => {
    const btn = e.target.closest("[data-toggle]");
    if (!btn) return;
    const input = document.getElementById(btn.getAttribute("data-toggle"));
    if (!input) return;

    const isPw = input.type === "password";
    input.type = isPw ? "text" : "password";
    btn.textContent = isPw ? "숨기기" : "보기";
  });

  // ✅ 새 비밀번호 일치 검사
  if (pwdForm) {
    pwdForm.addEventListener("submit", (e) => {
      const a = (document.getElementById("newPwd")?.value || "").trim();
      const b = (document.getElementById("newPwd2")?.value || "").trim();

      if (a !== b) {
        e.preventDefault();
        alert("새 비밀번호와 확인이 일치하지 않습니다.");
        document.getElementById("newPwd2")?.focus();
      }
    });
  }

  // ✅ 정보수정 버튼 토글
  const editBtn = document.getElementById("editBtn");
  const resetBtn = document.getElementById("resetBtn");
  const username = document.getElementById("username");
  const form = document.getElementById("profileForm");
  let editing = false;

  if (editBtn && username && form) {
    editBtn.addEventListener("click", () => {
      editing = !editing;

      username.readOnly = !editing;
      username.style.backgroundColor = editing ? "#fff" : "#f0f0f0";

      editBtn.textContent = editing ? "저장하기" : "정보수정";
      resetBtn.style.display = editing ? "inline-flex" : "none";

      if (!editing) form.submit();
    });
  }

  // ✅ 초기화 버튼 (편의)
  if (resetBtn) {
    resetBtn.addEventListener("click", () => {
      form.reset();
    });
  }
});
