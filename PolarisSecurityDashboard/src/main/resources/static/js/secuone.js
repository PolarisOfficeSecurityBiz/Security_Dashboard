(function() {
  // ----- Endpoints -----
  var ENDPOINTS = {
    notices: "/api/v1/polar-notices?size=200",
    letters: "/api/v1/polar-letters?size=30",
    news:    "/api/v1/secu-news?size=30",
    ads:     "/api/v1/direct-ads?size=200"
  };

  // ----- Safe HTML escape -----
  function esc(val) {
    if (val == null) return "";
    var s = "";
    try { s = String(val); } catch (e) { return ""; }
    var out = "", i = 0, ch;
    for (i = 0; i < s.length; i++) {
      ch = s.charCodeAt(i);
      if (ch === 38) out += "&amp;";
      else if (ch === 60) out += "&lt;";
      else if (ch === 62) out += "&gt;";
      else if (ch === 34) out += "&quot;";
      else if (ch === 39) out += "&#39;";
      else out += s.charAt(i);
    }
    return out;
  }

  // ----- Utils -----
  function $(s, el) { return (el || document).querySelector(s); }
  function fmt(v) { return Number(v || 0).toLocaleString("ko-KR"); }
  function setText(id, v) { var el = document.getElementById(id); if (el) el.textContent = v; }
  function todayStr() {
    var n = new Date(), m = ("0" + (n.getMonth() + 1)).slice(-2), d = ("0" + n.getDate()).slice(-2);
    return n.getFullYear() + "-" + m + "-" + d;
  }

  // JSON fetch
  function fetchJSON(url) {
    console.log("Fetching data from: ", url);
    return fetch(url, { headers: { "Accept": "application/json" } })
      .then(function (res) {
        if (!res.ok) {
          return res.text().then(function (t) {
            throw new Error("HTTP " + res.status + " " + (t || ""));
          });
        }
        return res.json();
      })
      .then(function (data) {
        console.log("Fetched data from " + url, data);
        return data;
      })
      .catch(function (err) {
        console.error("Error fetching data from " + url, err);
      });
  }

  function toArray(data) {
    if (Array.isArray(data)) return data;
    if (data && Array.isArray(data.content)) return data.content;
    if (data && Array.isArray(data.items)) return data.items;
    return [];
  }

  // ----- KPIs -----
  function renderKPIs(groups) {
    setText("noticeCount", fmt(groups.notices.length));
    setText("letterCount", fmt(groups.letters.length));
    setText("newsCount", fmt(groups.news.length));
    setText("adCount", fmt(groups.ads.length));
  }

  // ----- Tables -----
  function renderTable(tbodyId, rows, valueKey) {
    var tb = document.getElementById(tbodyId);
    if (!tb) return;
    tb.innerHTML = "";

    if (!rows || rows.length === 0) {
      var tr0 = document.createElement("tr");
      tr0.innerHTML =
        "<td>-</td>" +
        "<td>미분류</td>" +
        "<td>0</td>";
      tb.appendChild(tr0);
      return;
    }

    rows.forEach(function (r) {
      var tr = document.createElement("tr");
      tr.innerHTML =
        "<td>" + esc(r.company) + "</td>" +
        "<td>" + esc(r.type || "미분류") + "</td>" +
        "<td>" + fmt(r[valueKey] || 0) + "</td>";
      tb.appendChild(tr);
    });
  }

  // ----- 광고 메트릭 (조회수/클릭수 합계 기준) -----
  function buildAdMetrics(ads) {
    var agg = {};   // "company|type" -> {company, type, clicks, views}

    ads.forEach(function (a) {
      var advertiserName = a.advertiserName || "-";
      var adType = a.adType || "미분류";

      var clicks = a.clickCount != null ? Number(a.clickCount) : 0;
      var views = a.viewCount != null ? Number(a.viewCount) : 0;

      var key = advertiserName + "|" + adType;
      if (!agg[key]) {
        agg[key] = { company: advertiserName, type: adType, clicks: 0, views: 0 };
      }
      agg[key].clicks += clicks;
      agg[key].views += views;
    });

    var clicksArr = [], viewsArr = [];
    Object.keys(agg).forEach(function (k) {
      var it = agg[k];
      clicksArr.push({ company: it.company, type: it.type, clicks: it.clicks });
      viewsArr.push({ company: it.company, type: it.type, views: it.views });
    });

    clicksArr.sort(function (a, b) { return (b.clicks || 0) - (a.clicks || 0); });
    viewsArr.sort(function (a, b) { return (b.views || 0) - (a.views || 0); });

    return { clicks: clicksArr, views: viewsArr };
  }

  // ----- Image helpers -----
  function proxyImg(u) {
    if (!u) return "/images/placeholder.jpg";
    return "/img-proxy?u=" + encodeURIComponent(u);
  }

  // ----- Carousels -----
  function renderCarousel(ulId, items) {
    var ul = document.getElementById(ulId);
    if (!ul) return;
    ul.innerHTML = "";
    var today = todayStr();

    items.forEach(function (it) {
      var li = document.createElement("li");
      li.className = (ulId === "lettersCarousel") ? "letter-card" : "news-card";

      var raw = it.imageURL || it.thumbnail || it.image || it.imageUrl || "";
      var imgSrc = proxyImg(raw);
      var date = it.date || it.publishedAt || it.createdAt || it.publishedDate || today;
      var link = it.url || it.href || it.targetUrl || "#";
      var title = it.title || "제목 없음";

      var a = document.createElement("a");
      a.className = "card-link";
      a.href = link;
      a.target = "_blank";
      a.rel = "noopener";

      var img = document.createElement("img");
      img.setAttribute("referrerpolicy", "no-referrer");
      img.loading = "lazy";
      img.alt = "";
      img.src = imgSrc;
      img.onerror = function () { img.onerror = null; img.src = "/images/placeholder.jpg"; };

      var ov = document.createElement("div");
      ov.className = "overlay";
      ov.innerHTML =
        '<div class="ttl">' + esc(title) + '</div>' +
        '<div class="date">' + esc(todayStr()) + '</div>';

      a.appendChild(img);
      a.appendChild(ov);
      li.appendChild(a);
      ul.appendChild(li);
    });
  }

  // ----- 버튼 이벤트 -----
  function bindNext(btnId, listId, redirectUrl) {
    var btn = document.getElementById(btnId);
    if (!btn) return;

    if (redirectUrl) {
      // 버튼 클릭 시 페이지 이동
      btn.addEventListener("click", function () {
        window.location.href = redirectUrl;
      });
    } else {
      // 기본 캐러셀 스크롤
      var listEl = document.getElementById(listId);
      var scrollPane = listEl ? listEl.parentElement : null;
      btn.addEventListener("click", function () {
        if (!scrollPane) return;
        var first = scrollPane.querySelector("li");
        var step = first ? first.getBoundingClientRect().width + 15 : 300;
        scrollPane.scrollBy({ left: step, behavior: "smooth" });
      });
    }
  }

  // ----- Load -----
  function load() {
    Promise.all([
      fetchJSON(ENDPOINTS.notices).catch(function (e) { console.warn("notices fail", e); return []; }),
      fetchJSON(ENDPOINTS.letters).catch(function (e) { console.warn("letters fail", e); return []; }),
      fetchJSON(ENDPOINTS.news).catch(function (e) { console.warn("news fail", e); return []; }),
      fetchJSON(ENDPOINTS.ads).catch(function (e) { console.warn("ads fail", e); return []; })
    ])
      .then(function (arr) {
        var notices = toArray(arr[0]);
        var letters = toArray(arr[1]);
        var news = toArray(arr[2]);
        var ads = toArray(arr[3]);

        renderKPIs({ notices: notices, letters: letters, news: news, ads: ads });

        var met = buildAdMetrics(ads);
        renderTable("adClicksTbody", met.clicks.slice(0, 10), "clicks");
        renderTable("adViewsTbody", met.views.slice(0, 10), "views");

        renderCarousel("lettersCarousel", letters.slice(0, 6));
        renderCarousel("newsCarousel", news.slice(0, 6));
      })
      .catch(function (err) {
        console.error("overview load failed:", err);
        ["noticeCount", "letterCount", "newsCount", "adCount"].forEach(function (id) {
          setText(id, "0");
        });
        renderTable("adClicksTbody", [], "clicks");
        renderTable("adViewsTbody", [], "views");
      });
  }

  // ----- Init -----
  document.addEventListener("DOMContentLoaded", function () {
    console.log("Page Loaded");
    // 버튼 클릭 시 상세 페이지 이동
    bindNext("lettersNextBtn", "lettersCarousel", "/admin/secuone/polarletter");
    bindNext("newsNextBtn", "newsCarousel", "/admin/secuone/secunews");
    // 새로고침 버튼
    var refresh = $("#refreshOverviewBtn");
    if (refresh) refresh.addEventListener("click", load);
    load();
  });
})();
