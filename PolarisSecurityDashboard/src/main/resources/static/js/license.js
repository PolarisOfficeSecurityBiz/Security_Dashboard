document.addEventListener("DOMContentLoaded", () => {
  const keyField = document.getElementById("licenseKey");
  const copyBtn = document.getElementById("copyKeyBtn");
  const extendBtn = document.getElementById("extendBtn");
  const downloadBtn = document.getElementById("downloadKeyBtn");

  // 📋 복사 버튼
  const showToast = (message) => {
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.innerText = message;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
  };

  if (copyBtn) {
    copyBtn.addEventListener("click", () => {
      navigator.clipboard.writeText(keyField.value);
      showToast("라이선스 키가 복사되었습니다.");
    });
  }

  // 🔄 연장요청 버튼
  if (extendBtn) {
    extendBtn.addEventListener("click", () => {
      showToast("라이선스 연장 요청이 전송되었습니다.");
    });
  }

  // ⭳ 다운로드 버튼
  if (downloadBtn) {
    downloadBtn.addEventListener("click", () => {
      downloadBtn.disabled = true;
      showToast("✅ 다운로드 클릭됨!");
      window.open("/customer/license/download", "_blank"); // 다운로드 링크를 새 탭에서 열기
    });
  }
});
