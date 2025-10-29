(function () {
  // ============ 🔧 설정 ============
  // 같은 오리진으로 호출
  const API_BASE = '';
  const API = `${API_BASE}/api/v1/polar-notices`;

  // 타임아웃(ms)
  const FETCH_TIMEOUT = 10000;

  // ============ 🔧 유틸 ============
  const $ = (s) => document.querySelector(s);
  const state = { raw: [], filtered: [], current: null, mode: 'create' };

  const esc = (s) => String(s ?? '').replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;');
  const toMs = (v) => { if (!v) return 0; const n = Date.parse(v); return isNaN(n) ? 0 : n; };
  function show(id, visible) { const el = document.getElementById(id); if (el) el.style.display = visible ? 'block' : 'none'; }
  function setText(id, text) { const el = document.getElementById(id); if (el) el.textContent = text ?? ''; }

  function setModal(open) {
    const wrap = $('#noticeModal');
    if (!wrap) return;
    wrap.classList.toggle('hidden', !open);
    if (open) setTimeout(() => $('#modalContent')?.focus(), 0);
  }

  function fillForm(n) {
    $('#id').value = n?.id ?? '';
    $('#title').value = n?.title ?? '';
    $('#category').value = n?.category ?? '';
    $('#author').value = n?.author ?? '';
    $('#date').value = n?.date ?? '';
    $('#imageURL').value = n?.imageURL ?? '';
    $('#imgThumb').src = n?.imageURL || '';
    $('#content').value = n?.content ?? '';
  }

  function readForm() {
    return {
      title: $('#title').value?.trim(),
      category: $('#category').value?.trim(),
      author: $('#author').value?.trim(),
      date: $('#date').value?.trim(),
      imageURL: $('#imageURL').value?.trim(),
      content: $('#content').value?.trim(),
    };
  }

  // ============ 공통 fetch ============
  function withFetchOptions(extra = {}) {
    const base = { headers: { Accept: 'application/json' } };
    if (extra.headers) base.headers = { ...base.headers, ...extra.headers };
    return { ...base, ...extra };
  }

  async function fetchJSON(url, options = {}) {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), FETCH_TIMEOUT);
    const opts = withFetchOptions({ ...options, signal: controller.signal });

    try {
      const res = await fetch(url, opts);
      if (!res.ok) {
        let detail = '';
        try { const txt = await res.text(); detail = txt ? ` - ${txt.slice(0, 500)}` : ''; } catch {}
        throw new Error(`HTTP ${res.status}${detail}`);
      }
      if (res.status === 204) return null;

      const ct = res.headers.get('content-type') || '';
      if (!ct.includes('application/json')) {
        const txt = await res.text();
        throw new Error(`Unexpected content-type: ${ct} - ${txt.slice(0, 200)}`);
      }
      return await res.json();
    } finally {
      clearTimeout(timer);
    }
  }

  // ============ 📡 API 래퍼 ============
  async function list(size, q) {
    const params = new URLSearchParams();
    if (size) params.set('size', size);
    if (q) params.set('q', q);
    return fetchJSON(`${API}?${params.toString()}`, { method: 'GET' });
  }
  async function getById(id) { return fetchJSON(`${API}/${encodeURIComponent(id)}`, { method: 'GET' }); }
  async function create(body) {
    return fetchJSON(API, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
  }
  async function patch(id, body) {
    return fetchJSON(`${API}/${encodeURIComponent(id)}`, { method: 'PATCH', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
  }
  async function destroy(id) { await fetchJSON(`${API}/${encodeURIComponent(id)}`, { method: 'DELETE' }); }

  // ============ 📋 목록 & 렌더 ============
  async function load() {
    show('loading', true); show('error', false); show('empty', false); setText('errorText', '');
    try {
      const size = Number($('#size')?.value || 100);
      const q = $('#q')?.value?.trim();

      const data = await list(size, q);

      let arr = [];
      if (Array.isArray(data)) arr = data;
      else if (Array.isArray(data?.content)) arr = data.content;
      else if (Array.isArray(data?.items)) arr = data.items;

      state.raw = arr;
      state.raw.sort((a, b) => toMs(b?.date) - toMs(a?.date));
      state.filtered = state.raw;

      render();
    } catch (e) {
      console.error('[notice] load failed', e);
      $('#notice-tbody') && ($('#notice-tbody').innerHTML = '');
      show('error', true);
      setText('errorText', String(e.message || e));
    } finally {
      show('loading', false);
    }
  }

  function render() {
    const tb = $('#notice-tbody');
    if (!tb) return;
    tb.innerHTML = '';

    if (!state.filtered.length) {
      show('empty', true);
      setText('countText', '0건');
      return;
    }
    show('empty', false);

    const frag = document.createDocumentFragment();
    state.filtered.forEach(n => {
      const tr = document.createElement('tr');
      tr.dataset.id = n.id ?? '';
      tr.innerHTML = `
        <td>${esc(n.date || '')}</td>
        <td>${esc(n.title || '')}</td>
        <td>${esc(n.category || '')}</td>
        <td>${esc(n.author || '')}</td>
        <td>${n.imageURL ? `<img src="${esc(n.imageURL)}" alt="" style="width:50px;height:36px;object-fit:cover;border-radius:6px;border:1px solid #e5e7eb">` : '—'}</td>
      `;
      tr.addEventListener('click', async () => {
        const id = tr.dataset.id;
        if (!id) return;
        try {
          const fresh = await getById(id);
          state.current = fresh; state.mode = 'edit';
          $('#noticeModalTitle').textContent = '공지 수정';
          $('#deleteBtn').classList.remove('hidden');
          fillForm(fresh);
          setModal(true);
        } catch (err) {
          console.error('[notice] open failed', err);
          alert('항목을 불러오지 못했습니다.');
        }
      });
      frag.appendChild(tr);
    });
    tb.appendChild(frag);
    setText('countText', `${state.filtered.length}건`);
  }

  // ============ 🔗 바인딩 ============
  document.addEventListener('DOMContentLoaded', () => {
    $('#refreshBtn')?.addEventListener('click', load);
    $('#q')?.addEventListener('input', () => load());
    $('#size')?.addEventListener('change', load);

    $('#createBtn')?.addEventListener('click', () => {
      state.current = null; state.mode = 'create';
      $('#noticeModalTitle').textContent = '공지 등록';
      $('#deleteBtn').classList.add('hidden');
      fillForm(null);
      setModal(true);
    });

    $('#imageURL')?.addEventListener('input', () => { $('#imgThumb').src = $('#imageURL').value || ''; });

    $('#saveBtn')?.addEventListener('click', async (e) => {
      e.preventDefault();
      try {
        const body = readForm();
        if (!body.title) { alert('제목은 필수입니다.'); return; }
        if (state.mode === 'create') state.current = await create(body);
        else state.current = await patch($('#id').value, body);
        setModal(false);
        await load();
        alert('저장되었습니다.');
      } catch (err) {
        console.error('[notice] save failed', err);
        alert('저장에 실패했습니다.');
      }
    });

    $('#deleteBtn')?.addEventListener('click', async () => {
      if (!state.current?.id) return;
      if (!confirm('정말 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) return;
      try {
        await destroy(state.current.id);
        setModal(false);
        await load();
        alert('삭제되었습니다.');
      } catch (err) {
        console.error('[notice] delete failed', err);
        alert('삭제에 실패했습니다.');
      }
    });

    $('#closeModal')?.addEventListener('click', () => setModal(false));
    $('#cancelBtn')?.addEventListener('click', () => setModal(false));

    load();
  });
})();
