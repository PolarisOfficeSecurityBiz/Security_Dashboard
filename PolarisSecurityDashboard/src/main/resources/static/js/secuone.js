(function () {
  const now = new Date();
  const y = now.getFullYear();
  const m = String(now.getMonth() + 1).padStart(2, "0");
  const d = String(now.getDate()).padStart(2, "0");
  const nowEl = document.getElementById("nowText");
  if (nowEl) nowEl.textContent = `시스템 현황 요약 · ${y}-${m}-${d}`;

  // KPI 업데이트 헬퍼
  function setText(id, val) {
    const el = document.getElementById(id);
    if (el) el.textContent = val; // count 값을 여기서 설정
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

  // API에서 실제 데이터 가져오기
  async function fetchOverviewData() {
    try {
      const response = await fetch('/api/v1/overview');
      const data = await response.json();

      // 응답 데이터 출력하여 확인
      console.log("API Response:", data); // 응답 데이터 확인

      // KPI 값 세팅
      setText("noticeCount", data.notice.count);  // count 값이 잘 들어오는지 확인
      setText("letterCount", data.letter.count);
      setText("newsCount", data.news.count);
      setText("adCount", data.ad.count);

      // delta 값 세팅
      setDelta("noticeDelta", data.notice.delta);
      setDelta("letterDelta", data.letter.delta);
      setDelta("newsDelta", data.news.delta);
      setDelta("adDelta", data.ad.delta);

      // 리스트 렌더링
      renderList("noticeList", data.notice.items);
      renderList("letterList", data.letter.items);
      renderList("newsList", data.news.items);
      renderList("adList", data.ad.items);

    } catch (error) {
      console.error('Error fetching overview data:', error);
    }
  }


  // 페이지 로드 시 API 데이터 불러오기
  fetchOverviewData();

})();
