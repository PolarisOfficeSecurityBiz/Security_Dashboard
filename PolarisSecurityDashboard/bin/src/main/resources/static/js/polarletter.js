(() => {
  const API = '/api/v1/polar-letters';

  const els = {
    q: document.getElementById('q'),
    btn: document.getElementById('refreshBtn'),
    create: document.getElementById('createNewBtn'),

    tbody: document.getElementById('letter-tbody'),
    count: document.getElementById('countText'),
    empty: document.getElementById('empty'),
    error: document.getElementById('error'),
    loading: document.getElementById('loading'),

    // modal
    modalWrap: document.getElementById('letter-modal-wrap'),
    modalClose: document.getElementById('modalCloseBtn'),
    modalTitle: document.getElementById('modalTitle'),

    form: document.getElementById('letter-form'),
    id: document.getElementById('f-id'),
    title: document.getElementById('f-title'),
    author: document.getElementById('f-author'),
    date: document.getElementById('f-date'),
    url: document.getElementById('f-url'),
    thumb: document.getElementById('f-thumbnail'),
    thumbPrev: document.getElementById('f-thumb-preview'),
    content: document.getElementById('f-content'),
    edit: document.getElementById('editBtn'),
    save: document.getElementById('saveBtn'),
    cancel: document.getElementById('cancelBtn'),
    del: document.getElementById('deleteBtn'),
  };

  let current = null;                 // 현재 열어둔 항목 (null이면 생성 모드)
  let editing = false;                // 입력 가능 여부

  const esc = s => (s ?? '').replace(/[&<>"']/g, m => (
    { '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;' }[m]
  ));
  const fmtId = id => id?.slice(0, 6) ?? '';

  // ===== API =====
  async function fetchList(q) {
    const params = new URLSearchParams();
    params.set('size', '200');
    if (q) params.set('q', q);
    const res = await fetch(`${API}?${params.toString()}`, { headers: { 'Accept':'application/json' } });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return res.json();
  }
  async function fetchById(id) {
    const res = await fetch(`${API}/${encodeURIComponent(id)}`, { headers: { 'Accept':'application/json' } });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return res.json();
  }
  async function create(body) {
    const res = await fetch(API, {
      method: 'POST',
      headers: { 'Content-Type':'application/json', 'Accept':'application/json' },
      body: JSON.stringify(body),
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return res.json();
  }
  async function patch(id, body) {
    const res = await fetch(`${API}/${encodeURIComponent(id)}`, {
      method: 'PATCH',
      headers: { 'Content-Type':'application/json', 'Accept':'application/json' },
      body: JSON.stringify(body),
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return res.json();
  }
  async function destroy(id) {
    const res = await fetch(`${API}/${encodeURIComponent(id)}`, { method: 'DELETE' });
    if (!res.ok && res.status !== 204) throw new Error(`HTTP ${res.status}`);
  }

  // ===== list & render =====
  async function load() {
    els.loading.hidden = false; els.empty.hidden = true; els.error.hidden = true;
    try {
      const rows = await fetchList(els.q.value.trim());
      render(rows);
    } catch (e) {
      console.error('[polarletter] load error', e);
      els.error.hidden = false;
    } finally {
      els.loading.hidden = true;
    }
  }

  function render(rows) {
    els.tbody.innerHTML = '';
    if (!rows || rows.length === 0) {
      els.empty.hidden = false; els.error.hidden = true; els.count.textContent = '0건'; return;
    }
    els.empty.hidden = true; els.error.hidden = true;

    const frag = document.createDocumentFragment();
    rows.forEach(n => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td><span class="row-muted" title="${esc(n.id)}">${esc(fmtId(n.id))}</span></td>
        <td>${n.thumbnail ? `<img class="letter-thumb" src="${esc(n.thumbnail)}" alt="">` : `<div class="letter-thumb"></div>`}</td>
        <td><div>${esc(n.title||'')}</div>${n.content?`<div class="row-muted">${esc(n.content.slice(0,80))}${n.content.length>80?'…':''}</div>`:''}</td>
        <td>${esc(n.author||'-')}</td>
        <td>${esc(n.createTime||'-')}</td>
        <td>${n.url?`<a class="link-pill" target="_blank" rel="noopener" href="${esc(n.url)}">열기</a>`:'-'}</td>
      `;
      tr.addEventListener('click', async () => {
        try {
          const fresh = await fetchById(n.id);
          openForEdit(fresh);
        } catch(e) {
          console.error('[polarletter] fetch by id failed', e);
          alert('항목을 불러오지 못했습니다.');
        }
      });
      frag.appendChild(tr);
    });

    els.tbody.appendChild(frag);
    els.count.textContent = `${rows.length}건`;
  }

  // ===== Form helpers =====
  function clearForm() {
    current = null;
    els.id.value = '';
    els.title.value = '';
    els.author.value = '';
    els.date.value = '';
    els.url.value = '';
    els.thumb.value = '';
    els.thumbPrev.src = '';
    els.content.value = '';
  }
  function fillForm(d) {
    current = d;
    els.id.value = d.id || '';
    els.title.value = d.title || '';
    els.author.value = d.author || '';
    els.date.value = d.createTime || '';
    els.url.value = d.url || '';
    els.thumb.value = d.thumbnail || '';
    els.thumbPrev.src = d.thumbnail || '';
    els.content.value = d.content || '';
  }
  function toPatchPayload() {
    const body = {};
    if (els.title.value !== (current.title ?? '')) body.title = els.title.value;
    if (els.author.value !== (current.author ?? '')) body.author = els.author.value;
    if (els.date.value !== (current.createTime ?? '')) body.createTime = els.date.value;
    if (els.url.value !== (current.url ?? '')) body.url = els.url.value;
    if (els.thumb.value !== (current.thumbnail ?? '')) body.thumbnail = els.thumb.value;
    if (els.content.value !== (current.content ?? '')) body.content = els.content.value;
    return body;
  }
  function readCreateBody() {
    return {
      title: els.title.value?.trim(),
      author: els.author.value?.trim(),
      createTime: els.date.value?.trim(),
      url: els.url.value?.trim(),
      thumbnail: els.thumb.value?.trim(),
      content: els.content.value?.trim(),
    };
  }
  function setEditing(on) {
    editing = on;
    [els.title, els.author, els.date, els.url, els.thumb, els.content].forEach(i => i && (i.disabled = !on));
    els.edit.classList.toggle('hidden', on || current === null); // 생성모드에서는 숨김
    els.save.classList.toggle('hidden', !on);
    els.cancel.classList.toggle('hidden', !on);
  }

  // ===== Open modal modes =====
  function openModal() {
    els.modalWrap.classList.remove('hidden');
    els.modalWrap.removeAttribute('hidden');
  }
  function closeModal() {
    els.modalWrap.classList.add('hidden');
    els.modalWrap.setAttribute('hidden','');
    current = null;
    setEditing(false);
  }

  // 편집 모드로 열기
  function openForEdit(fresh) {
    fillForm(fresh);
    els.modalTitle.textContent = '레터 상세';
    els.del.classList.remove('hidden');
    setEditing(false);
    openModal();
  }

  // 생성 모드로 열기
  function openForCreate() {
    clearForm();
    els.modalTitle.textContent = '새 레터 등록';
    els.del.classList.add('hidden');     // 생성 모드에서는 삭제 없음
    setEditing(true);                    // 바로 입력 가능
    openModal();
  }

  // ===== 이벤트 바인딩 =====
  els.btn?.addEventListener('click', load);
  els.q?.addEventListener('keydown', e => { if (e.key === 'Enter') load(); });
  els.create?.addEventListener('click', openForCreate);

  els.modalClose?.addEventListener('click', closeModal);
  els.modalWrap?.addEventListener('click', e => { if (e.target === els.modalWrap) closeModal(); });

  els.thumb?.addEventListener('input', () => { els.thumbPrev.src = els.thumb.value || ''; });

  els.edit?.addEventListener('click', () => { if (current) setEditing(true); });

  els.cancel?.addEventListener('click', () => {
    if (current) fillForm(current); else clearForm();
    setEditing(false);
    // 생성 모드에서 취소하면 모달을 닫는게 자연스러움
    if (!current) closeModal();
  });

  els.form?.addEventListener('submit', async (e) => {
    e.preventDefault();

    try {
      els.save.disabled = true;

      if (!current) {
        // === CREATE ===
        const body = readCreateBody();
        if (!body.title) { alert('제목은 필수입니다.'); els.save.disabled = false; return; }
        const created = await create(body);
        // 생성 후 상세 모드로 전환
        openForEdit(created);
        await load();
        alert('등록되었습니다.');
      } else {
        // === PATCH ===
        const payload = toPatchPayload();
        if (Object.keys(payload).length === 0) {
          alert('변경된 내용이 없습니다.');
          setEditing(false);
          els.save.disabled = false;
          return;
        }
        const updated = await patch(current.id, payload);
        openForEdit(updated);
        await load();
        alert('저장되었습니다.');
      }
    } catch (err) {
      console.error('[polarletter] save failed', err);
      alert('처리에 실패했습니다.');
    } finally {
      els.save.disabled = false;
    }
  });

  els.del?.addEventListener('click', async () => {
    if (!current) return;
    const yes = confirm('정말 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.');
    if (!yes) return;
    try {
      els.del.disabled = true;
      await destroy(current.id);
      closeModal();
      await load();
      alert('삭제되었습니다.');
    } catch (err) {
      console.error('[polarletter] delete failed', err);
      alert('삭제에 실패했습니다.');
    } finally {
      els.del.disabled = false;
    }
  });

  // 초기 로딩
  load();
})();
