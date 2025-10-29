document.addEventListener("DOMContentLoaded", () => {
  const modal = document.getElementById("uploadModal");
  const openBtn = document.querySelector(".upload-btn"); // 상단 업로드 버튼
  const cancelBtn = modal.querySelector(".cancel-btn");

  // 모달 열기
  openBtn.addEventListener("click", (e) => {
    e.preventDefault();
    modal.classList.remove("hidden");
  });

  // 닫기
  cancelBtn.addEventListener("click", () => {
    modal.classList.add("hidden");
  });

  // 배경 클릭 시 닫기
  modal.addEventListener("click", (e) => {
    if (e.target === modal) {
      modal.classList.add("hidden");
    }
  });

  // ✅ Toast 자동 사라짐
  const toast = document.getElementById("toastMessage");
  if (toast) {
    setTimeout(() => {
      toast.classList.remove("show");
    }, 3000);
  }
});
