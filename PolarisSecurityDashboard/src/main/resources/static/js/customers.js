(function () {
  const API = {
    METRICS: "/admin/api/customers/metrics",
    CUSTOMERS: "/admin/api/customers/list"
  };

  const $ = (s) => document.querySelector(s);

  function esc(v) {
    if (v == null) return "—";
    return String(v).replace(/[&<>"']/g, (m) => ({
      "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    }[m]));
  }

  function fmtDate(str) {
    if (!str) return "—";
    const d = new Date(str);
    if (isNaN(d)) return esc(str);
    return d.toISOString().slice(0, 10);
  }

  function renderCustomers(list) {
    const tb = $("#customersTbody");
    if (!tb) {
      console.error("❌ #customersTbody 를 찾을 수 없습니다.");
      return;
    }
    tb.innerHTML = "";

    if (!list || list.length === 0) {
      tb.innerHTML = `<tr><td colspan="3" class="empty">등록된 고객사가 없습니다.</td></tr>`;
      return;
    }

    list.forEach((c) => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td class="link-cell">
          <a href="/admin/customers/${esc(c.customerId)}" class="customer-link">
            ${esc(c.customerName)}
          </a>
        </td>
        <td>${esc(c.connectedCompanyName)}</td>
        <td>${fmtDate(c.createAt)}</td>
      `;
      tb.appendChild(tr);
    });
  }

  // 🔍 검색 필터 (고객사명 / 서비스명 / 연결사명)
  function bindSearch(customers) {
    const input = $("#searchInput");
    if (!input) return;
    input.addEventListener("input", (e) => {
      const keyword = e.target.value.toLowerCase();
      const filtered = customers.filter((c) =>
        (c.customerName || "").toLowerCase().includes(keyword) ||
        (c.connectedCompanyName || "").toLowerCase().includes(keyword) ||
        (c.services || "").toLowerCase().includes(keyword)
      );
      renderCustomers(filtered);
    });
  }

  async function fetchJson(url) {
    const res = await fetch(url, { credentials: "same-origin" });
    if (!res.ok) {
      // 로그인 리다이렉트(302)나 권한 문제 등 디버깅에 도움
      throw new Error(`HTTP ${res.status} @ ${url}`);
    }
    return res.json();
  }

  async function load() {
    try {
      const [metrics, customers] = await Promise.all([
        fetchJson(API.METRICS).catch(() => ({})),
        fetchJson(API.CUSTOMERS).catch(() => [])
      ]);
      const list = Array.isArray(customers) ? customers : (customers.content || []);
      renderCustomers(list);
      bindSearch(list);
    } catch (err) {
      console.error("불러오기 실패", err);
      const tb = $("#customersTbody");
      if (tb) tb.innerHTML = `<tr><td colspan="3" class="empty">데이터 로드 실패 (${esc(err.message)})</td></tr>`;
    }
  }

  document.addEventListener("DOMContentLoaded", load);
})();
