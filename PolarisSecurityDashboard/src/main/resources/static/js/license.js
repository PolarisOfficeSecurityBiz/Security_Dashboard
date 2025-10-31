document.addEventListener("DOMContentLoaded", () => {
  const keyField = document.getElementById("licenseKey");
  const copyBtn = document.getElementById("copyKeyBtn");
  const extendBtn = document.getElementById("extendBtn");
  const downloadBtn = document.getElementById("downloadKeyBtn");
  const issueBtn = document.getElementById("issueBtn");

  // 🧩 공통 토스트 함수
  const showToast = (message, type = "info") => {
    const toast = document.createElement("div");
    toast.className = `toast ${type}`;
    toast.innerText = message;
    document.body.appendChild(toast);
    setTimeout(() => toast.classList.add("visible"), 10);
    setTimeout(() => {
      toast.classList.remove("visible");
      setTimeout(() => toast.remove(), 300);
    }, 3000);
  };

  // 📋 복사 버튼
  if (copyBtn) {
    copyBtn.addEventListener("click", () => {
      if (!keyField || !keyField.value || keyField.value === "발급 전입니다.") {
        showToast("복사할 라이선스 키가 없습니다.", "warn");
        return;
      }
      navigator.clipboard.writeText(keyField.value);
      showToast("라이선스 키가 복사되었습니다. 📋", "success");
    });
  }

  // 🔄 연장요청 버튼
  if (extendBtn) {
    extendBtn.addEventListener("click", () => {
      if (extendBtn.disabled) {
        showToast("아직 발급되지 않은 라이선스입니다.", "warn");
        return;
      }
      showToast("라이선스 연장 요청이 전송되었습니다. 🔄", "success");
    });
  }

  // ⭳ 다운로드 버튼
  if (downloadBtn) {
    downloadBtn.addEventListener("click", () => {
      if (downloadBtn.disabled) {
        showToast("다운로드할 파일이 없습니다.", "warn");
        return;
      }
      downloadBtn.disabled = true;
      showToast("라이선스 파일 다운로드를 시작합니다. ⭳", "success");
      window.open("/customer/license/download", "_blank");
      setTimeout(() => (downloadBtn.disabled = false), 2000);
    });
  }

  // 🆕 발급요청 버튼
  if (issueBtn) {
    issueBtn.addEventListener("click", async () => {
      if (!confirm("라이선스 발급을 요청하시겠습니까?")) return;

      issueBtn.disabled = true;
      issueBtn.classList.add("loading");
      issueBtn.innerHTML = `<span class="spinner"></span> 요청 중...`;

      try {
        const res = await fetch("/api/license/issue", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            serviceName: document.getElementById("serviceName")?.value,
            domain: document.getElementById("domain")?.value,
          }),
        });

        if (!res.ok) throw new Error("요청 실패");

        // ✅ 요청 성공 시: 버튼 상태 변경
        issueBtn.classList.remove("loading");
        issueBtn.classList.add("waiting");
        issueBtn.innerHTML = "승인 대기중";
        issueBtn.disabled = true;

        showToast("✅ 라이선스 발급 요청이 완료되었습니다. 승인 대기중입니다.", "success");

      } catch (err) {
        console.error(err);
        showToast("⚠️ 발급 요청 중 오류가 발생했습니다.", "error");

        // 복귀
        issueBtn.disabled = false;
        issueBtn.classList.remove("loading");
        issueBtn.innerText = "발급요청";
      }
    });
  }
});
