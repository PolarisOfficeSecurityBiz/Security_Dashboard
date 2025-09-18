(function () {
  const API = '/api/v1/direct-ads';
  const $ = (sel) => document.querySelector(sel);

  const state = { raw: [], filtered: [] };

  document.addEventListener('DOMContentLoaded', () => {
    $('#refreshBtn')?.addEventListener('click', load);
    $('#q')?.addEventListener('input', applyFilter);
    $('#type')?.addEventListener('change', applyFilter);
    load();
  });

  function show(id, visible) {
    const el = document.getElementById(id);
    if (el) el.hidden = !visible;
  }

  async function load() {
    show('error', false);
    show('empty', false);
    show('loading', true);

    try {
      const res = await fetch(API, { headers: { Accept: 'application/json' } });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      state.raw = Array.isArray(data) ? data : [];
      applyFilter();
    } catch (e) {
      console.error('[directad] load error:', e);
      $('#countText').textContent = '0건';
      document.getElementById('ad-tbody').innerHTML = '';
      show('error', true);
    } finally {
      show('loading', false);
    }
  }

  function applyFilter() {
    const q = ($('#q')?.value || '').trim().toLowerCase();
    const type = ($('#type')?.value || '').toUpperCase();

    let list = state.raw.slice();
    if (type) list = list.filter(x => (x.adType || '').toUpperCase() === type);
    if (q) {
      list = list.filter(x => {
        const hay = [x.id, x.adType, x.advertiserName, x.imageUrl, x.targetUrl]
          .filter(Boolean).join(' ').toLowerCase();
        return hay.includes(q);
      });
    }
    list.sort((a, b) => toMs(b.updateAt) - toMs(a.updateAt));

    state.filtered = list;
    render();
  }

  function render() {
    const tbody = document.getElementById('ad-tbody');
    tbody.innerHTML = '';

    if (!state.filtered.length) {
      show('empty', true);
      $('#countText').textContent = '0건';
      return;
    }
    show('empty', false);

    const frag = document.createDocumentFragment();

    state.filtered.forEach(ad => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td>${esc(short(ad.id))}</td>
        <td><strong>${esc(ad.adType || '')}</strong></td>
        <td>${esc(ad.advertiserName || '')}</td>
        <td>
          <span class="color-chip">
            <span class="color-dot" style="background:${cssColor(ad.backgroundColor)}"></span>
            <span>${esc(ad.backgroundColor || '')}</span>
          </span>
        </td>
        <td>${renderThumb(ad.imageUrl)}</td>
        <td>${ad.targetUrl ? `<a class="link" href="${url(ad.targetUrl)}" target="_blank" rel="noopener">열기</a>` : '—'}</td>
        <td>${num(ad.viewCount)}</td>
        <td>${num(ad.clickCount)}</td>
        <td>${fmt(ad.publishedDate)}</td>
        <td>${fmt(ad.updateAt)}</td>
      `;
      frag.appendChild(tr);
    });

    tbody.appendChild(frag);
    $('#countText').textContent = `${state.filtered.length}건`;

    // 깨진 이미지 처리 → 하이픈으로 대체
    document.querySelectorAll('.thumb').forEach(img => {
      img.addEventListener('error', () => {
        const td = img.closest('td');
        if (td) td.textContent = '—';
      });
    });
  }

  // === utils ===
  function renderThumb(u) {
    if (!u) return '—';
    const safe = url(u);
    return `
      <a class="img-link" href="${safe}" target="_blank" rel="noopener" aria-label="원본 열기">
        <img class="thumb" src="${safe}" alt="이미지 미리보기" loading="lazy" referrerpolicy="no-referrer">
      </a>`;
  }
  function num(v){ const n = Number(v ?? 0); return isFinite(n) ? n.toLocaleString() : '0'; }
  function short(v){ if(!v) return ''; v=String(v); return v.length>10?v.slice(0,10)+'…':v; }
  function fmt(v){ const ms = toMs(v); if(!ms) return ''; const d=new Date(ms);
    const y=d.getFullYear(), m=String(d.getMonth()+1).padStart(2,'0'), da=String(d.getDate()).padStart(2,'0');
    const h=String(d.getHours()).padStart(2,'0'), mi=String(d.getMinutes()).padStart(2,'0');
    return `${y}-${m}-${da} ${h}:${mi}`; }
  function toMs(v){
    if(!v) return 0;
    if(typeof v==='string'){ const t=Date.parse(v); return isNaN(t)?0:t; }
    if(typeof v==='object'){
      if(typeof v.seconds==='number') return v.seconds*1000;
      if(typeof v._seconds==='number') return v._seconds*1000;
      if(v.$date){ const t=Date.parse(v.$date); return isNaN(t)?0:t; }
    }
    return 0;
  }
  const esc = s => String(s??'')
    .replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;')
    .replaceAll('"','&quot;').replaceAll("'","&#39;");
  const url = s => { try { return encodeURI(String(s??'')); } catch { return '#'; } };
  const cssColor = s => String(s??'').replace(/[^#(),.%\-\s\w]/g,'');
})();
