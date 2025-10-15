document.addEventListener("DOMContentLoaded", () => {
  const keyField = document.getElementById("licenseKey");
  const copyBtn = document.getElementById("copyKeyBtn");
  const extendBtn = document.getElementById("extendBtn");
  const downloadBtn = document.getElementById("downloadKeyBtn");

  // 📋 복사 버튼
  if (copyBtn) {
    copyBtn.addEventListener("click", () => {
      navigator.clipboard.writeText(keyField.value);
      alert("라이선스 키가 복사되었습니다.");
    });
  }

  // 🔄 연장요청 버튼
  if (extendBtn) {
    extendBtn.addEventListener("click", () => {
      alert("라이선스 연장 요청이 전송되었습니다.");
    });
  }

  // ⭳ 다운로드 버튼
  if (downloadBtn) {
    downloadBtn.addEventListener("click", () => {
		
      // 서버의 라이선스 다운로드 API 호출
	  alert("✅ 다운로드 클릭됨!");
      window.location.href = "/customer/license/download";
    });
  }
});
