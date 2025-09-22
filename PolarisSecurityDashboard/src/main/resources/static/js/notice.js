(function () {
  const API = '/api/v1/polar-notices';
  const $ = (s) => document.querySelector(s);

  const state = { raw: [], filtered: [], current: null, mode: 'create' }; // mode: 'create' | 'edit'

  // ====== helpers ======
  const esc = (s) => String(s ?? '').replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;');
  const toMs = (v) => {
    if (!v) return 0;
    const n = Date.parse(v);
    return isNaN(n) ? 0 : n;
  };

  function show(id, visible) {
    const el = document.getElementById(id);
    if (!el) return;
    el.style.display = visible ? 'block' : 'none';
  }

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
      // id는 서버 생성(POST) 또는 path 변수(PATCH)
      title: $('#title').value?.trim(),
      category: $('#category').value?.trim(),
      author: $('#author').value?.trim(),
      date: $('#date').value?.trim(),
      imageURL: $('#imageURL').value?.trim(),
      content: $('#content').value?.trim(),
    };
  }

  // ====== API ======
  async function list(size, q) {
    const params = new URLSearchParams();
    if (size) params.set('size', size);
    if (q) params.set('q', q);
    const res = await fetch(`${API}?${params.toString()}`, { headers: { Accept: 'application/json'}});
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return res.json();
  }

  async function getById(id) {
    const res = await fetch(`${API}/${encodeURIComponent(id)}`, { headers: { Accept: 'application/json'}});
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return res.json();
  }

  async function create(body) {
    const res = await fetch(API, {
      method: 'POST',
      headers: { 'Content-Type':'application/json', Accept: 'application/json' },
      body: JSON.stringify(body),
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return res.json();
  }

  async function patch(id, body) {
    const res = await fetch(`${API}/${encodeURIComponent(id)}`, {
      method: 'PATCH',
      headers: { 'Content-Type':'application/json', Accept: 'application/json' },
      body: JSON.stringify(body),
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return res.json();
  }

  async function destroy(id) {
    const res = await fetch(`${API}/${encodeURIComponent(id)}`, { method: 'DELETE' });
    if (!res.ok && res.status !== 204) throw new Error(`HTTP ${res.status}`);
  }

  // ====== list & render ======
  async function load() {
    show('loading', true); show('error', false); show('empty', false);
    try {
      const size = Number($('#size')?.value || 100);
      const q = $('#q')?.value?.trim();
      const data = await list(size, q);
      state.raw = Array.isArray(data) ? data : [];

      // 간단 정렬: 최신 작성일/수정일 우선(모델은 date만 있으므로 date 기준)
      state.raw.sort((a, b) => toMs(b?.date) - toMs(a?.date));
      state.filtered = state.raw;

      render();
    } catch (e) {
      console.error('[notice] load failed', e);
      $('#notice-tbody').innerHTML = '';
      show('error', true);
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
      $('#countText').textContent = '0건';
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
          state.current = fresh;
          state.mode = 'edit';
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
    $('#countText').textContent = `${state.filtered.length}건`;
  }

  // ====== wire up ======
  document.addEventListener('DOMContentLoaded', () => {
    $('#refreshBtn')?.addEventListener('click', load);
    $('#q')?.addEventListener('input', () => load());
    $('#size')?.addEventListener('change', load);

    // 새 공지
    $('#createBtn')?.addEventListener('click', () => {
      state.current = null;
      state.mode = 'create';
      $('#noticeModalTitle').textContent = '공지 등록';
      $('#deleteBtn').classList.add('hidden');
      fillForm(null);
      setModal(true);
    });

    // 이미지 URL 프리뷰
    $('#imageURL')?.addEventListener('input', () => {
      $('#imgThumb').src = $('#imageURL').value || '';
    });

    // 저장
    $('#saveBtn')?.addEventListener('click', async (e) => {
      e.preventDefault();
      try {
        const body = readForm();
        if (!body.title) { alert('제목은 필수입니다.'); return; }

        if (state.mode === 'create') {
          const created = await create(body);
          state.current = created;
        } else {
          const id = $('#id').value;
          const updated = await patch(id, body);
          state.current = updated;
        }
        setModal(false);
        await load();
        alert('저장되었습니다.');
      } catch (err) {
        console.error('[notice] save failed', err);
        alert('저장에 실패했습니다.');
      }
    });

    // 삭제
    $('#deleteBtn')?.addEventListener('click', async () => {
      if (!state.current?.id) return;
      const yes = confirm('정말 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.');
      if (!yes) return;
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

    // 최초 로드
    load();
  });
})();
