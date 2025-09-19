(() => {
  const API = '/api/v1/secu-news';

  const els = {
    q: document.querySelector('#q'),
    refresh: document.querySelector('#refreshBtn'),
    create: document.querySelector('#createBtn'),

    tbody: document.querySelector('#news-tbody'),
    count: document.querySelector('#countText'),
    empty: document.querySelector('#empty'),
    err: document.querySelector('#error'),
    loading: document.querySelector('#loading'),

    // modal
    modalWrap: document.querySelector('#news-modal-wrap'),
    modalClose: document.querySelector('#modalCloseBtn'),
    form: document.querySelector('#news-form'),
    id: document.querySelector('#f-id'),
    title: document.querySelector('#f-title'),
    category: document.querySelector('#f-category'),
    date: document.querySelector('#f-date'),
    url: document.querySelector('#f-url'),
    thumb: document.querySelector('#f-thumbnail'),
    thumbPrev: document.querySelector('#f-thumb-preview'),
    updated: document.querySelector('#f-updated'),

    edit: document.querySelector('#editBtn'),
    save: document.querySelector('#saveBtn'),
    cancel: document.querySelector('#cancelBtn'),
    del: document.querySelector('#deleteBtn'),
    modalTitle: document.querySelector('#news-modal-title'),
  };

  let lastRows = [];
  let current = null;   // null 이면 "생성 모드"
  let editing = false;

  function fmtDate(s) {
    if (!s) return '—';
    const d = s.length > 10 ? new Date(s) : new Date(s + 'T00:00:00');
    if (isNaN(d)) return s;
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    const hh = String(d.getHours()).padStart(2, '0');
    const mi = String(d.getMinutes()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd} ${hh}:${mi}`;
  }

  function setEditing(on) {
    editing = on;
    const dis = !on;
    [els.title, els.category, els.date, els.url, els.thumb].forEach(i => { if (i) i.disabled = dis; });
    els.edit?.classList.toggle('hidden', on || current === null); // 생성 모드에선 Edit 숨김
    els.save?.classList.toggle('hidden', !on);
    els.cancel?.classList.toggle('hidden', !on);
  }

  function openModal() {
    els.modalWrap?.classList.remove('hidden');
  }
  function closeModal() {
    els.modalWrap?.classList.add('hidden');
    current = null;
    setEditing(false);
  }

  // ===== List/Render =====
  function render(rows) {
    if (!els.tbody || !els.count || !els.empty) return;

    els.tbody.innerHTML = '';
    els.count.textContent = `${rows.length}건`;
    els.empty.hidden = rows.length !== 0;

    for (const n of rows) {
      const tr = document.createElement('tr');
      tr.dataset.id = n.id;

      const idTd = document.createElement('td');
      idTd.textContent = n.id?.slice(0, 8) ?? '—';

      const thTd = document.createElement('td');
      if (n.thumbnail) {
        const img = document.createElement('img');
        img.className = 'thumb';
        img.src = n.thumbnail; img.alt = 'thumbnail';
        thTd.appendChild(img);
      } else thTd.textContent = '—';

      const titleTd = document.createElement('td');
      titleTd.className = 'title-cell';
      titleTd.textContent = n.title ?? '—';

      const catTd = document.createElement('td'); catTd.textContent = n.category ?? '—';
      const dateTd = document.createElement('td'); dateTd.textContent = n.date ?? '—';

      const linkTd = document.createElement('td'); linkTd.className = 'link';
      if (n.url) {
        const a = document.createElement('a');
        a.href = n.url; a.target = '_blank'; a.rel = 'noopener'; a.textContent = '열기';
        a.addEventListener('click', (e) => e.stopPropagation());
        linkTd.appendChild(a);
      } else linkTd.textContent = '—';

      const updTd = document.createElement('td'); updTd.textContent = fmtDate(n.updatedAt);

      tr.append(idTd, thTd, titleTd, catTd, dateTd, linkTd, updTd);

      // 행 클릭 → 상세(수정 모드로 열기)
      tr.addEventListener('click', async () => {
        try {
          const res = await fetch(`${API}/${encodeURIComponent(n.id)}`, { credentials: 'same-origin' });
          if (!res.ok) throw new Error(`HTTP ${res.status}`);
          const fresh = await res.json();
          openForEdit(fresh);
        } catch (e) {
          console.error('[secu-news] fetch by id failed:', e);
          alert('항목을 불러오지 못했습니다.');
        }
      });

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

      const q = (els.q?.value || '').trim().toLowerCase();
      if (q) {
        rows = rows.filter(r =>
          (r.title || '').toLowerCase().includes(q) ||
          (r.category || '').toLowerCase().includes(q)
        );
      }

      lastRows = rows;
      render(rows);
    } catch (e) {
      console.error('[secu-news] load failed:', e);
      els.err.hidden = false;
    } finally {
      els.loading.hidden = true;
    }
  }

  // ===== Form helpers =====
  function clearForm() {
    current = null;
    els.id.value = '';
    els.title.value = '';
    els.category.value = '';
    els.date.value = '';
    els.url.value = '';
    els.thumb.value = '';
    els.thumbPrev.src = '';
    els.updated.value = '';
  }
  function fillForm(d) {
    current = d;
    els.id.value = d.id ?? '';
    els.title.value = d.title ?? '';
    els.category.value = d.category ?? '';
    els.date.value = d.date ?? '';
    els.url.value = d.url ?? '';
    els.thumb.value = d.thumbnail ?? '';
    els.thumbPrev.src = d.thumbnail || '';
    els.updated.value = fmtDate(d.updatedAt) ?? '';
  }
  function toPatchPayload() {
    const body = {};
    if (els.title.value    !== (current.title ?? ''))     body.title = els.title.value || null;
    if (els.category.value !== (current.category ?? ''))  body.category = els.category.value || null;
    if (els.date.value     !== (current.date ?? ''))      body.date = els.date.value || null;
    if (els.url.value      !== (current.url ?? ''))       body.url = els.url.value || null;
    if (els.thumb.value    !== (current.thumbnail ?? '')) body.thumbnail = els.thumb.value || null;
    return body;
  }
  function readCreateBody() {
    return {
      title: els.title.value?.trim(),
      category: els.category.value?.trim(),
      date: els.date.value?.trim(),
      url: els.url.value?.trim(),
      thumbnail: els.thumb.value?.trim(),
    };
  }

  // ===== Open modes =====
  function openForEdit(fresh) {
    fillForm(fresh);
    els.modalTitle.textContent = '뉴스 상세';
    els.del?.classList.remove('hidden');
    setEditing(false);
    openModal();
  }
  function openForCreate() {
    clearForm();
    els.modalTitle.textContent = '새 뉴스 등록';
    els.del?.classList.add('hidden');      // 생성모드에선 삭제 안 보임
    setEditing(true);                      // 바로 입력 가능
    openModal();
  }

  // ===== Events =====
  els.refresh?.addEventListener('click', load);
  els.q?.addEventListener('keydown', e => { if (e.key === 'Enter') load(); });
  els.create?.addEventListener('click', openForCreate);

  els.modalClose?.addEventListener('click', closeModal);
  els.modalWrap?.addEventListener('click', (e) => { if (e.target === els.modalWrap) closeModal(); });

  els.thumb?.addEventListener('input', () => { els.thumbPrev.src = els.thumb.value || ''; });

  els.edit?.addEventListener('click', () => { if (current) setEditing(true); });
  els.cancel?.addEventListener('click', () => {
    if (current) fillForm(current); else clearForm();
    setEditing(false);
    if (!current) closeModal(); // 생성모드 취소 시 닫기
  });

  // 저장 (생성/수정 공용)
  els.form?.addEventListener('submit', async (e) => {
    e.preventDefault();

    try {
      els.save.disabled = true;

      if (!current) {
        // === CREATE ===
        const body = readCreateBody();
        if (!body.title) { alert('제목은 필수입니다.'); els.save.disabled = false; return; }
        const res = await fetch(API, {
          method: 'POST',
          credentials: 'same-origin',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(body),
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const created = await res.json();
        openForEdit(created);
        await load();
        alert('등록되었습니다.');
      } else {
        // === PATCH ===
        const payload = toPatchPayload();
        if (Object.keys(payload).length === 0) {
          alert('변경된 내용이 없습니다.');
          setEditing(false); els.save.disabled = false; return;
        }
        const res = await fetch(`${API}/${encodeURIComponent(current.id)}`, {
          method: 'PATCH',
          credentials: 'same-origin',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload),
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const updated = await res.json();
        openForEdit(updated);
        await load();
        alert('저장되었습니다.');
      }
    } catch (err) {
      console.error('[secu-news] save failed:', err);
      alert('처리에 실패했습니다.');
    } finally {
      els.save.disabled = false;
    }
  });

  // 삭제
  els.del?.addEventListener('click', async () => {
    if (!current) return;
    const yes = confirm('정말 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.');
    if (!yes) return;

    try {
      els.del.disabled = true;
      const res = await fetch(`${API}/${encodeURIComponent(current.id)}`, {
        method: 'DELETE',
        credentials: 'same-origin',
      });
      if (!res.ok && res.status !== 204) throw new Error(`HTTP ${res.status}`);
      closeModal();
      await load();
      alert('삭제되었습니다.');
    } catch (err) {
      console.error('[secu-news] delete failed:', err);
      alert('삭제에 실패했습니다.');
    } finally {
      els.del.disabled = false;
    }
  });

  // 초기 로딩
  load();
})();
