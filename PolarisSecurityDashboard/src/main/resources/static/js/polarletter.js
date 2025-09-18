(() => {
  const API = '/api/v1/polar-letters';

  const $q = document.getElementById('q');
  const $btn = document.getElementById('refreshBtn');
  const $tbody = document.getElementById('letter-tbody');
  const $count = document.getElementById('countText');
  const $empty = document.getElementById('empty');
  const $error = document.getElementById('error');
  const $loading = document.getElementById('loading');

  const fmtId = id => id?.slice(0, 6) ?? '';
  const esc = s => (s ?? '').replace(/[&<>"']/g, m => (
    { '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;' }[m]
  ));

  async function fetchList() {
    const params = new URLSearchParams();
    params.set('size', '200');
    const q = $q.value.trim();
    if (q) params.set('q', q);

    const url = `${API}?${params.toString()}`;
    const res = await fetch(url, { headers: { 'Accept': 'application/json' } });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return res.json();
  }

  function render(rows) {
    $tbody.innerHTML = '';
    if (!rows || rows.length === 0) {
      $empty.hidden = false; $error.hidden = true; $count.textContent = '0건';
      return;
    }
    $empty.hidden = true; $error.hidden = true;

    const frag = document.createDocumentFragment();

    rows.forEach(n => {
      const tr = document.createElement('tr');

      tr.innerHTML = `
        <td><span class="row-muted" title="${esc(n.id)}">${esc(fmtId(n.id))}</span></td>
        <td>
          ${n.thumbnail ? `<img class="letter-thumb" src="${esc(n.thumbnail)}" alt="">`
                        : `<div class="letter-thumb"></div>`}
        </td>
        <td>
          <div>${esc(n.title || '')}</div>
          ${n.content ? `<div class="row-muted">${esc(n.content.slice(0, 80))}${n.content.length>80?'…':''}</div>` : ''}
        </td>
        <td>${esc(n.author || '-')}</td>
        <td>${esc(n.createTime || '-')}</td>
        <td>${n.url ? `<a class="link-pill" target="_blank" rel="noopener" href="${esc(n.url)}">열기</a>` : '-'}</td>
      `;
      frag.appendChild(tr);
    });

    $tbody.appendChild(frag);
    $count.textContent = `${rows.length}건`;
  }

  async function load() {
    $loading.hidden = false; $empty.hidden = true; $error.hidden = true;
    try {
      const rows = await fetchList();
      render(rows);
    } catch (e) {
      console.error('[polarletter] fetch error:', e);
      $error.hidden = false;
    } finally {
      $loading.hidden = true;
    }
  }

  $btn.addEventListener('click', load);
  $q.addEventListener('keydown', e => { if (e.key === 'Enter') load(); });

  document.addEventListener('DOMContentLoaded', load);
})();
