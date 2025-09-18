(() => {
  const API = '/api/v1/secu-news';

  const els = {
    q: document.querySelector('#q'),
    refresh: document.querySelector('#refreshBtn'),
    tbody: document.querySelector('#news-tbody'),
    count: document.querySelector('#countText'),
    empty: document.querySelector('#empty'),
    err: document.querySelector('#error'),
    loading: document.querySelector('#loading'),
  };

  function fmtDate(s) {
    if (!s) return '—';
    // "2025-09-11T08:35:22.865Z" 혹은 "2025-09-11" 지원
    const d = s.length > 10 ? new Date(s) : new Date(s + 'T00:00:00');
    if (isNaN(d)) return s;
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    const hh = String(d.getHours()).padStart(2, '0');
    const mi = String(d.getMinutes()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd} ${hh}:${mi}`;
  }

  function render(rows) {
    els.tbody.innerHTML = '';
    els.count.textContent = `${rows.length}건`;
    els.empty.hidden = rows.length !== 0;

    for (const n of rows) {
      const tr = document.createElement('tr');

      const idTd = document.createElement('td');
      idTd.textContent = n.id?.slice(0, 8) ?? '—';

      const thTd = document.createElement('td');
      if (n.thumbnail) {
        const img = document.createElement('img');
        img.className = 'thumb';
        img.src = n.thumbnail;
        img.alt = 'thumbnail';
        thTd.appendChild(img);
      } else {
        thTd.textContent = '—';
      }

      const titleTd = document.createElement('td');
      titleTd.className = 'title-cell';
      titleTd.textContent = n.title ?? '—';

      const catTd = document.createElement('td');
      catTd.textContent = n.category ?? '—';

      const dateTd = document.createElement('td');
      dateTd.textContent = n.date ?? '—';

      const linkTd = document.createElement('td');
      linkTd.className = 'link';
      if (n.url) {
        const a = document.createElement('a');
        a.href = n.url;
        a.target = '_blank';
        a.rel = 'noopener';
        a.textContent = '열기';
        linkTd.appendChild(a);
      } else {
        linkTd.textContent = '—';
      }

      const updTd = document.createElement('td');
      updTd.textContent = fmtDate(n.updatedAt);

      tr.append(idTd, thTd, titleTd, catTd, dateTd, linkTd, updTd);
      els.tbody.appendChild(tr);
    }
  }

  async function load() {
    els.err.hidden = true;
    els.loading.hidden = false;

    try {
      const res = await fetch(API, { credentials: 'same-origin' });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      let rows = await res.json();

      const q = (els.q.value || '').trim().toLowerCase();
      if (q) {
        rows = rows.filter(r =>
          (r.title || '').toLowerCase().includes(q) ||
          (r.category || '').toLowerCase().includes(q)
        );
      }

      render(rows);
    } catch (e) {
      console.error('[secu-news] load failed:', e);
      els.err.hidden = false;
    } finally {
      els.loading.hidden = true;
    }
  }

  els.refresh.addEventListener('click', load);
  els.q.addEventListener('keydown', e => { if (e.key === 'Enter') load(); });

  load();
})();
