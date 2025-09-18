// directad.js
(function () {
  // === helpers ===
  const esc = (s) =>
    String(s ?? '')
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#39;');

  const url = (s) => { try { return encodeURI(String(s ?? '')); } catch { return '#'; } };
  const cssColor = (s) => String(s ?? '').replace(/[^#(),.%\-\s\w]/g, '');
  const $ = (sel) => document.querySelector(sel);

  const state = { raw: [], filtered: [] };

  function show(id, visible) {
    const el = document.getElementById(id);
    if (el) el.hidden = !visible;
  }

  // === init ===
  document.addEventListener('DOMContentLoaded', () => {
    // 검색/필터/새로고침
    $('#refreshBtn')?.addEventListener('click', load);
    $('#q')?.addEventListener('input', applyFilter);
    $('#type')?.addEventListener('change', applyFilter);

    // 모달 관련 참조
    const modal = document.getElementById('adModal');
    const modalContent = document.getElementById('modalContent');
    const closeBtn = document.getElementById('closeModal');
    const cancelBtn = document.getElementById('cancelBtn');
    const editBtn = document.getElementById('editBtn');
    const saveBtn = document.getElementById('saveBtn');
    const form = document.getElementById('adEditForm');

    // 행 클릭 → 모달 열기
    document.getElementById('ad-tbody')?.addEventListener('click', (event) => {
      const row = event.target.closest('tr');
      if (!row) return;

      // 데이터 채우기
      const ds = row.dataset;
      setVal('adType', ds.type);
      setVal('advertiserName', ds.advertiser);
      setVal('backgroundColor', ds.bg);
      setVal('imageUrl', ds.img);
      setVal('targetUrl', ds.url);

      setFormEditable(false);
      openModal();
    });

    function setVal(id, v) { const el = document.getElementById(id); if (el) el.value = v ?? ''; }
    function setFormEditable(editable) {
      ['adType','advertiserName','backgroundColor','imageUrl','targetUrl']
        .forEach(id => { const el = document.getElementById(id); if (el) el.disabled = !editable; });
      if (saveBtn) saveBtn.hidden = !editable;
    }

    // ── 모달 강제 오픈/클로즈(충돌 무력화) ──
    function openModal() {
      if (!modal) return;
      // 숨김 속성 제거 + 클래스/스타일 강제
      modal.removeAttribute('hidden');
      modal.classList.add('open');
      modal.style.setProperty('display', 'block', 'important');
      modal.style.zIndex = '2147483646';
      // 컨텐츠도 최상단
      if (modalContent) {
        modalContent.style.zIndex = '2147483647';
        // 포커스
        setTimeout(() => modalContent.focus(), 0);
      }
      // ESC로 닫기
      document.addEventListener('keydown', onEsc);
    }
    function closeModal() {
      if (!modal) return;
      modal.classList.remove('open');
      modal.style.setProperty('display', 'none', 'important');
      document.removeEventListener('keydown', onEsc);
    }
    function onEsc(e){ if (e.key === 'Escape') closeModal(); }

    // 오버레이 클릭 시 닫기 (컨텐츠 바깥만)
    modal?.addEventListener('click', (e) => {
      if (e.target === modal) closeModal();
    });

    closeBtn?.addEventListener('click', closeModal);
    cancelBtn?.addEventListener('click', closeModal);
    editBtn?.addEventListener('click', () => setFormEditable(true));

    form?.addEventListener('submit', (e) => {
      e.preventDefault();
      const payload = {
        adType: $('#adType')?.value ?? '',
        advertiserName: $('#advertiserName')?.value ?? '',
        backgroundColor: $('#backgroundColor')?.value ?? '',
        imageUrl: $('#imageUrl')?.value ?? '',
        targetUrl: $('#targetUrl')?.value ?? '',
      };
      console.log('[directad] save payload:', payload);
      setFormEditable(false);
      closeModal();
    });

    // 최초 로드
    load();
  });

  // === data load ===
  async function load() {
    show('error', false);
    show('empty', false);
    show('loading', true);

    try {
      const res = await fetch('/api/v1/direct-ads', { headers: { Accept: 'application/json' } });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      state.raw = Array.isArray(data) ? data : [];
      applyFilter();
    } catch (e) {
      console.error('[directad] load error:', e);
      $('#countText') && ($('#countText').textContent = '0건');
      const tbody = document.getElementById('ad-tbody');
      if (tbody) tbody.innerHTML = '';
      show('error', true);
    } finally {
      show('loading', false);
    }
  }

  // === filter/sort ===
  function applyFilter() {
    const q = ($('#q')?.value || '').trim().toLowerCase();
    const type = ($('#type')?.value || '').toUpperCase();

    let list = state.raw.slice();
    if (type) list = list.filter(x => String(x.adType || '').toUpperCase() === type);
    if (q) {
      list = list.filter(x => {
        const hay = [x.id, x.adType, x.advertiserName, x.imageUrl, x.targetUrl]
          .filter(Boolean).join(' ').toLowerCase();
        return hay.includes(q);
      });
    }

    list.sort((a, b) => toMs(b.updatedAt ?? b.updateAt) - toMs(a.updatedAt ?? a.updateAt));
    state.filtered = list;
    render();
  }

  // === render ===
  function render() {
    const tbody = document.getElementById('ad-tbody');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (!state.filtered.length) {
      show('empty', true);
      $('#countText') && ($('#countText').textContent = '0건');
      return;
    }
    show('empty', false);

    const frag = document.createDocumentFragment();

    state.filtered.forEach(ad => {
      const tr = document.createElement('tr');

      // dataset(모달 채우기용)
      tr.dataset.id = ad.id ?? '';
      tr.dataset.type = ad.adType ?? '';
      tr.dataset.advertiser = ad.advertiserName ?? '';
      tr.dataset.bg = ad.backgroundColor ?? '';
      tr.dataset.img = ad.imageUrl ?? '';
      tr.dataset.url = ad.targetUrl ?? '';

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
        <td>${ad.targetUrl
          ? `<a class="link" href="${url(ad.targetUrl)}" target="_blank" rel="noopener">열기</a>`
          : '—'}</td>
        <td>${num(ad.viewCount)}</td>
        <td>${num(ad.clickCount)}</td>
        <td>${fmt(ad.publishedDate)}</td>
        <td>${fmt(ad.updatedAt ?? ad.updateAt)}</td>
      `;
      frag.appendChild(tr);
    });

    tbody.appendChild(frag);
    $('#countText') && ($('#countText').textContent = `${state.filtered.length}건`);
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

  function num(v) { const n = Number(v ?? 0); return isFinite(n) ? n.toLocaleString() : '0'; }
  function short(v) { if (!v) return ''; v = String(v); return v.length > 10 ? v.slice(0,10) + '…' : v; }
  function fmt(v) { const ms = toMs(v); if (!ms) return ''; const d = new Date(ms);
    const y = d.getFullYear(), m = String(d.getMonth()+1).padStart(2,'0'), da = String(d.getDate()).padStart(2,'0');
    const h = String(d.getHours()).padStart(2,'0'), mi = String(d.getMinutes()).padStart(2,'0');
    return `${y}-${m}-${da} ${h}:${mi}`; }
  function toMs(v) {
    if (!v) return 0;
    if (typeof v === 'string') { const t = Date.parse(v); return isNaN(t) ? 0 : t; }
    if (typeof v === 'number') return v;
    if (typeof v === 'object') {
      if (typeof v.seconds === 'number') return v.seconds * 1000;
      if (typeof v._seconds === 'number') return v._seconds * 1000;
      if (v.$date) { const t = Date.parse(v.$date); return isNaN(t) ? 0 : t; }
    }
    return 0;
  }
})();
