(function () {
  // 오늘 날짜 텍스트 세팅
  const now = new Date();
  const y = now.getFullYear();
  const m = String(now.getMonth() + 1).padStart(2, "0");
  const d = String(now.getDate()).padStart(2, "0");
  const nowEl = document.getElementById("nowText");
  if (nowEl) nowEl.textContent = `시스템 현황 요약 · ${y}-${m}-${d}`;

  // KPI 업데이트 헬퍼
  function setText(id, val) {
    const el = document.getElementById(id);
    if (el) el.textContent = val;
  }
  function setDelta(id, val) {
    const el = document.getElementById(id);
    if (el) el.textContent = val >= 0 ? `+${val} 신규` : `${val} 감소`;
  }

  // 리스트 렌더링 헬퍼
  function renderList(ulId, items) {
    const ul = document.getElementById(ulId);
    if (!ul) return;
    ul.innerHTML = "";
    items.forEach((it) => {
      const li = document.createElement("li");
      li.className = "item";
      li.innerHTML = `
        <div class="left">
          <div class="title">${it.title ?? ""}</div>
          <div class="sub">${it.subtitle ?? ""}</div>
        </div>
        <a href="${it.href ?? '#'}">바로가기</a>
      `;
      ul.appendChild(li);
    });
  }

  // ===== 데모 데이터 (API 연동 전 자리표시) =====
  const demoKpis = { notice: 18, letter: 9, news: 27, ad: 6 };
  setText("noticeCount", demoKpis.notice);
  setText("letterCount", demoKpis.letter);
  setText("newsCount", demoKpis.news);
  setText("adCount", demoKpis.ad);
  setDelta("noticeDelta", 2);
  setDelta("letterDelta", 1);
  setDelta("newsDelta", 4);
  setDelta("adDelta", 0);

  renderList("noticeList", [
    { title: "시스템 점검 안내", subtitle: "2025-09-15 · 운영팀", href: "/notice" },
    { title: "정책 변경 안내", subtitle: "2025-09-13 · 운영팀", href: "/notice" },
  ]);
  renderList("letterList", [
    { title: "보안 이슈 브리핑", subtitle: "2025-09-12 · 콘텐츠팀", href: "/polarletter" },
  ]);
  renderList("newsList", [
    { title: "취약점 패치 릴리스", subtitle: "2025-09-10 · 외부", href: "/secunews" },
  ]);
  renderList("adList", [
    { title: "BANNER · Polaris Office", subtitle: "2025-09-14 · 조회 123 · 클릭 12", href: "/directad" },
  ]);

  // ===== 실제 연동 시 예시 =====
  // fetch('/api/v1/overview')
  //   .then(r => r.json())
  //   .then(data => {
  //     setText('noticeCount', data.notice.count);
  //     setDelta('noticeDelta', data.notice.delta);
  //     renderList('noticeList', data.notice.items);
  //     // ... 나머지 섹션 동일
  //   });
})();
