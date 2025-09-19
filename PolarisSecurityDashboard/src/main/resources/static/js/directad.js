(function () {
  const API = '/api/v1/direct-ads';

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

  const state = { raw: [], filtered: [], current: null, editing: false };

  function show(id, visible) { const el = document.getElementById(id); if (el) el.hidden = !visible; }

  function setFormEditable(editable) {
    ['adType','advertiserName','backgroundColor','imageUrl','targetUrl']
      .forEach(id => { const el = document.getElementById(id); if (el) el.disabled = !editable; });
    const saveBtn  = document.getElementById('saveBtn');
    const editBtn  = document.getElementById('editBtn');
    const cancelBtn = document.getElementById('cancelBtn');
    if (saveBtn)  saveBtn.hidden  = !editable;
    if (editBtn)  editBtn.hidden  =  !!editable;
    if (cancelBtn) cancelBtn.hidden = !editable;
    state.editing = editable;
  }

  function openModal() {
    const modal = document.getElementById('adModal');
    if (!modal || !state.current) return;

    fillForm(state.current);

    modal.removeAttribute('hidden');
    modal.classList.add('open');
    modal.style.setProperty('display', 'block', 'important');

    const modalContent = document.getElementById('modalContent');
    if (modalContent) setTimeout(() => modalContent.focus(), 0);

    document.addEventListener('keydown', onEsc);
  }

  function closeModal() {
    const modal = $('#adModal');
    if (!modal) return;
    modal.classList.remove('open');
    modal.style.setProperty('display', 'none', 'important');
    modal.setAttribute('hidden', '');
    document.removeEventListener('keydown', onEsc);
    state.current = null;
    setFormEditable(false);
  }
  function onEsc(e) { if (e.key === 'Escape') closeModal(); }
  $('#adModal')?.addEventListener('click', (e) => { if (e.target === $('#adModal')) closeModal(); });

  // === API ===
  async function fetchAll() {
    const res = await fetch(API, { headers: { Accept: 'application/json' } });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return res.json();
  }

  async function fetchById(id) {
    const res = await fetch(`${API}/${encodeURIComponent(id)}`, { headers: { Accept: 'application/json' } });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return res.json();
  }

  async function patch(id, payload) {
    const res = await fetch(`${API}/${encodeURIComponent(id)}`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
      body: JSON.stringify(payload),
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return res.json();
  }

  async function destroy(id) {
    const res = await fetch(`${API}/${encodeURIComponent(id)}`, { method: 'DELETE' });
    if (!res.ok && res.status !== 204) throw new Error(`HTTP ${res.status}`);
  }

  // === list/filter/render ===
  async function load() {
    show('error', false); show('empty', false); show('loading', true);
    try {
      const data = await fetchAll();
      state.raw = Array.isArray(data) ? data : [];
      applyFilter();
    } catch (e) {
      console.error('[directad] load error:', e);
      $('#countText') && ($('#countText').textContent = '0건');
      const tbody = $('#ad-tbody'); if (tbody) tbody.innerHTML = '';
      show('error', true);
    } finally {
      show('loading', false);
    }
  }

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

  function render() {
    const tbody = $('#ad-tbody'); if (!tbody) return;
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
      tr.dataset.id = ad.id ?? '';

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

      tr.addEventListener('click', async () => {
        const id = tr.dataset.id;
        if (!id) return;
        try {
          const fresh = await fetchById(id);
          state.current = fresh;
          fillForm(fresh);
          openModal();
          setFormEditable(false);
        } catch (e) {
          console.error('[directad] open failed:', e);
          alert('항목을 불러오지 못했습니다.');
        }
      });

      frag.appendChild(tr);
    });

    tbody.appendChild(frag);
    $('#countText') && ($('#countText').textContent = `${state.filtered.length}건`);
  }

  // === form helpers ===
  function fillForm(ad) {
    setVal('adId', ad.id);
    setVal('adType', ad.adType);
    setVal('advertiserName', ad.advertiserName);
    setVal('backgroundColor', ad.backgroundColor);
    setVal('imageUrl', ad.imageUrl);
    setVal('targetUrl', ad.targetUrl);

    document.getElementById('bgDot')?.style.setProperty('background', ad.backgroundColor || '#eee');
    document.getElementById('openImg')?.setAttribute('href', ad.imageUrl || '#');
    document.getElementById('openLink')?.setAttribute('href', ad.targetUrl || '#');
    const pv = document.getElementById('imgPreview');
    if (pv) { pv.src = ad.imageUrl || ''; pv.alt = ad.imageUrl ? '이미지 미리보기' : '이미지 없음'; }
  }
  function setVal(id, v) { const el = document.getElementById(id); if (el) el.value = v ?? ''; }

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

  function diffPayload(orig) {
    const cur = {
      adType: $('#adType')?.value,
      advertiserName: $('#advertiserName')?.value,
      backgroundColor: $('#backgroundColor')?.value,
      imageUrl: $('#imageUrl')?.value,
      targetUrl: $('#targetUrl')?.value,
    };
    const out = {};
    Object.keys(cur).forEach(k => { if ((orig?.[k] ?? '') !== (cur?.[k] ?? '')) out[k] = cur[k]; });
    return out;
  }

  // === wire up ===
  document.addEventListener('DOMContentLoaded', () => {
    $('#refreshBtn')?.addEventListener('click', load);
    $('#q')?.addEventListener('input', applyFilter);
    $('#type')?.addEventListener('change', applyFilter);

    $('#closeModal')?.addEventListener('click', closeModal);
    $('#cancelBtn')?.addEventListener('click', closeModal);
    $('#editBtn')?.addEventListener('click', () => setFormEditable(true));

    $('#adEditForm')?.addEventListener('submit', async (e) => {
      e.preventDefault();
      if (!state.current) return;
      const payload = diffPayload(state.current);
      if (Object.keys(payload).length === 0) {
        alert('변경된 내용이 없습니다.');
        setFormEditable(false);
        return;
      }
      try {
        $('#saveBtn').disabled = true;
        const updated = await patch(state.current.id, payload);
        state.current = updated; fillForm(updated);
        setFormEditable(false);
        await load();
        alert('저장되었습니다.');
      } catch (err) {
        console.error('[directad] patch failed:', err);
        alert('저장에 실패했습니다.');
      } finally {
        $('#saveBtn').disabled = false;
      }
    });

    $('#deleteBtn')?.addEventListener('click', async () => {
      if (!state.current) return;
      const yes = confirm('정말 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.');
      if (!yes) return;
      try {
        $('#deleteBtn').disabled = true;
        await destroy(state.current.id);
        closeModal();
        await load();
        alert('삭제되었습니다.');
      } catch (err) {
        console.error('[directad] delete failed:', err);
        alert('삭제에 실패했습니다.');
      } finally {
        $('#deleteBtn').disabled = false;
      }
    });

    load();
  });
})();
