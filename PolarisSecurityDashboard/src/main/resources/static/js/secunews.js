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
  };

  // === DOM 진단 로그 ===
  Object.entries(els).forEach(([k, v]) => {
    if (v == null) console.warn(`[secu-news] element not found: els.${k}`);
  });

  let lastRows = [];
  let current = null;      // 현재 열람 중인 데이터
  let editing = false;     // 편집 모드 여부

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

    if (els.edit) els.edit.classList.toggle('hidden', on);
    if (els.save) els.save.classList.toggle('hidden', !on);
    if (els.cancel) els.cancel.classList.toggle('hidden', !on);
  }

  function openModal() {
    if (!els.modalWrap) {
      alert('모달 마크업이 페이지에 없습니다. 콘솔 경고를 확인하세요.');
      return;
    }
    els.modalWrap.classList.remove('hidden');
    setEditing(false);
  }
  function closeModal() {
    if (els.modalWrap) els.modalWrap.classList.add('hidden');
    current = null;
  }

  function fillForm(data) {
    current = data;
    if (els.id) els.id.value = data.id ?? '';
    if (els.title) els.title.value = data.title ?? '';
    if (els.category) els.category.value = data.category ?? '';
    if (els.date) els.date.value = data.date ?? '';
    if (els.url) els.url.value = data.url ?? '';
    if (els.thumb) els.thumb.value = data.thumbnail ?? '';
    if (els.thumbPrev) els.thumbPrev.src = data.thumbnail || '';
    if (els.updated) els.updated.value = fmtDate(data.updatedAt) ?? '';
  }

  function toPatchPayload() {
    const body = {};
    if (els.title && els.title.value !== current.title) body.title = els.title.value || null;
    if (els.category && els.category.value !== current.category) body.category = els.category.value || null;
    if (els.date && els.date.value !== current.date) body.date = els.date.value || null;
    if (els.url && els.url.value !== current.url) body.url = els.url.value || null;
    if (els.thumb && els.thumb.value !== current.thumbnail) body.thumbnail = els.thumb.value || null;
    return body;
  }

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
        a.addEventListener('click', (e) => e.stopPropagation());
        linkTd.appendChild(a);
      } else {
        linkTd.textContent = '—';
      }

      const updTd = document.createElement('td');
      updTd.textContent = fmtDate(n.updatedAt);

      tr.append(idTd, thTd, titleTd, catTd, dateTd, linkTd, updTd);

      // 행 클릭 → 상세 모달
      tr.addEventListener('click', async () => {
        try {
          const res = await fetch(`${API}/${encodeURIComponent(n.id)}`, { credentials: 'same-origin' });
          if (!res.ok) throw new Error(`HTTP ${res.status}`);
          const fresh = await res.json();
          fillForm(fresh);
          openModal();
        } catch (e) {
          console.error('[secu-news] fetch by id failed:', e);
          alert('항목을 불러오지 못했습니다.');
        }
      });

      els.tbody.appendChild(tr);
    }
  }

  async function load() {
    if (!els.err || !els.loading) return;

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

  // === 이벤트 바인딩 ===
  if (els.refresh) els.refresh.addEventListener('click', load);
  if (els.q) els.q.addEventListener('keydown', e => { if (e.key === 'Enter') load(); });

  // 모달 닫기
  if (els.modalClose) els.modalClose.addEventListener('click', closeModal);
  if (els.modalWrap) els.modalWrap.addEventListener('click', (e) => {
    if (e.target === els.modalWrap) closeModal(); // 배경 클릭 시 닫기
  });

  // 썸네일 미리보기
  if (els.thumb) els.thumb.addEventListener('input', () => {
    if (els.thumbPrev) els.thumbPrev.src = els.thumb.value || '';
  });

  // 편집
  if (els.edit) els.edit.addEventListener('click', () => {
    if (!current) return;
    setEditing(true);
  });

  // 취소
  if (els.cancel) els.cancel.addEventListener('click', () => {
    if (!current) return;
    fillForm(current);
    setEditing(false);
  });

  // 저장 (form submit)
  if (els.form) els.form.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!current) return;

    const payload = toPatchPayload();
    if (Object.keys(payload).length === 0) {
      alert('변경된 내용이 없습니다.');
      setEditing(false);
      return;
    }

    try {
      if (els.save) els.save.disabled = true;
      const res = await fetch(`${API}/${encodeURIComponent(current.id)}`, {
        method: 'PATCH',
        credentials: 'same-origin',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const updated = await res.json();
      fillForm(updated);
      setEditing(false);
      await load(); // 목록 갱신
      alert('저장되었습니다.');
    } catch (err) {
      console.error('[secu-news] patch failed:', err);
      alert('저장에 실패했습니다.');
    } finally {
      if (els.save) els.save.disabled = false;
    }
  });

  // 삭제
  if (els.del) els.del.addEventListener('click', async () => {
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
      await load(); // 목록 갱신
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
