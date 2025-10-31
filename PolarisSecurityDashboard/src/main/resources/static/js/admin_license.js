document.addEventListener("DOMContentLoaded", () => {
  // 🔽 접기/펼치기 기능
  document.querySelectorAll(".arrow-btn").forEach(btn => {
    btn.addEventListener("click", () => {
      const panel = btn.closest(".panel, .panel-item");
      const body = panel.querySelector(".panel-body");
      const isHidden = body.style.display === "none";
      body.style.display = isHidden ? "block" : "none";
      btn.textContent = isHidden ? "›" : "‹";
    });
  });

  // 🔍 필터 기능
  const searchBtn = document.getElementById("searchBtn");
  const searchInput = document.getElementById("searchInput");
  const sdkFilter = document.getElementById("sdkFilter");
  const typeFilter = document.getElementById("typeFilter");

  searchBtn.addEventListener("click", () => {
    const keyword = searchInput.value.trim().toLowerCase();
    const sdk = sdkFilter.value.toLowerCase();
    const type = typeFilter.value;

    document.querySelectorAll(".panel, .panel-item").forEach(panel => {
      const panelType = panel.dataset.type || "";
      let visible = true;

      if (type && panelType !== type) visible = false;

      panel.querySelectorAll("tbody tr").forEach(row => {
        const text = row.innerText.toLowerCase();
        const sdkText = row.innerText.toLowerCase();

        if (
          (!keyword || text.includes(keyword)) &&
          (!sdk || sdkText.includes(sdk))
        ) {
          row.style.display = "";
        } else {
          row.style.display = "none";
        }
      });

      panel.style.display = visible ? "" : "none";
    });
  });
});
