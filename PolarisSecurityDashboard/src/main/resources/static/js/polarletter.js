(() => {
  const API = '/api/v1/polar-letters';

  const els = {
    q: document.getElementById('q'),
    btn: document.getElementById('refreshBtn'),
    tbody: document.getElementById('letter-tbody'),
    count: document.getElementById('countText'),
    empty: document.getElementById('empty'),
    error: document.getElementById('error'),
    loading: document.getElementById('loading'),
    // modal
    modalWrap: document.getElementById('letter-modal-wrap'),
    modalClose: document.getElementById('modalCloseBtn'),
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

  let current = null;
  let editing = false;

  const esc = s => (s ?? '').replace(/[&<>"']/g, m => (
    { '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;' }[m]
  ));
  const fmtId = id => id?.slice(0, 6) ?? '';

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
          fillForm(fresh);
          openModal();
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

  function toPayload() {
    const body = {};
    if (els.title.value !== (current.title ?? '')) body.title = els.title.value;
    if (els.author.value !== (current.author ?? '')) body.author = els.author.value;
    if (els.date.value !== (current.createTime ?? '')) body.createTime = els.date.value;
    if (els.url.value !== (current.url ?? '')) body.url = els.url.value;
    if (els.thumb.value !== (current.thumbnail ?? '')) body.thumbnail = els.thumb.value;
    if (els.content.value !== (current.content ?? '')) body.content = els.content.value;
    return body;
  }

  function setEditing(on) {
    editing = on;
    [els.title, els.author, els.date, els.url, els.thumb, els.content].forEach(i => i && (i.disabled = !on));
    els.edit.classList.toggle('hidden', on);
    els.save.classList.toggle('hidden', !on);
    els.cancel.classList.toggle('hidden', !on);
  }

  function openModal() {
    if (els.modalWrap.hasAttribute('hidden')) els.modalWrap.removeAttribute('hidden');
    els.modalWrap.classList.remove('hidden'); // .hidden 유틸을 쓰는 경우 대비
    setEditing(false);
  }
  function closeModal() {
    els.modalWrap.classList.add('hidden');
    els.modalWrap.setAttribute('hidden', '');
    current = null;
  }

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

  // === 이벤트 바인딩 ===
  if (els.btn) els.btn.addEventListener('click', load);
  if (els.q) els.q.addEventListener('keydown', e => { if (e.key === 'Enter') load(); });

  if (els.modalClose) els.modalClose.addEventListener('click', closeModal);
  if (els.modalWrap) els.modalWrap.addEventListener('click', e => { if (e.target === els.modalWrap) closeModal(); });

  if (els.thumb) els.thumb.addEventListener('input', () => {
    els.thumbPrev.src = els.thumb.value || '';
  });

  if (els.edit) els.edit.addEventListener('click', () => {
    if (!current) return;
    setEditing(true);
  });

  if (els.cancel) els.cancel.addEventListener('click', () => {
    if (!current) return;
    fillForm(current);
    setEditing(false);
  });

  if (els.form) els.form.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!current) return;

    const payload = toPayload();
    if (Object.keys(payload).length === 0) {
      alert('변경된 내용이 없습니다.');
      setEditing(false);
      return;
    }

    try {
      els.save.disabled = true;
      const res = await fetch(`${API}/${encodeURIComponent(current.id)}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        body: JSON.stringify(payload),
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const updated = await res.json();
      fillForm(updated);
      setEditing(false);
      await load();
      alert('저장되었습니다.');
    } catch (err) {
      console.error('[polarletter] patch failed:', err);
      alert('저장에 실패했습니다.');
    } finally {
      els.save.disabled = false;
    }
  });

  if (els.del) els.del.addEventListener('click', async () => {
    if (!current) return;
    const yes = confirm('정말 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.');
    if (!yes) return;

    try {
      els.del.disabled = true;
      const res = await fetch(`${API}/${encodeURIComponent(current.id)}`, {
        method: 'DELETE',
      });
      if (!res.ok && res.status !== 204) throw new Error(`HTTP ${res.status}`);
      closeModal();
      await load();
      alert('삭제되었습니다.');
    } catch (err) {
      console.error('[polarletter] delete failed:', err);
      alert('삭제에 실패했습니다.');
    } finally {
      els.del.disabled = false;
    }
  });

  // 초기 로딩
  // (defer 스크립트라 DOMContentLoaded 없이도 가능하지만, 안전하게 즉시 호출)
  load();
})();
