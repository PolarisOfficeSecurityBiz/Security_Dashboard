(() => {
  const API = '/api/v1/polar-notices';

  const els = {
    q: document.getElementById('q'),
    btn: document.getElementById('refreshBtn'),
    tbody: document.getElementById('letter-tbody'),
    count: document.getElementById('countText'),
    empty: document.getElementById('empty'),
    error: document.getElementById('error'),
    loading: document.getElementById('loading'),
    // modal
    modalWrap: document.getElementById('notice-modal-wrap'),
    modalClose: document.getElementById('modalCloseBtn'),
    form: document.getElementById('notice-form'),
    id: document.getElementById('f-id'),
    title: document.getElementById('f-title'),
    author: document.getElementById('f-author'),
    date: document.getElementById('f-date'),
    category: document.getElementById('f-category'),
    image: document.getElementById('f-image'),
    thumbPrev: document.getElementById('f-thumb-preview'),
    content: document.getElementById('f-content'),
    edit: document.getElementById('editBtn'),
    save: document.getElementById('saveBtn'),
    cancel: document.getElementById('cancelBtn'),
    del: document.getElementById('deleteBtn'),
  };

  let current = null;

  const esc = s => (s ?? '').replace(/[&<>"']/g, m => (
    { '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;' }[m]
  ));

  const labelOf = (category) => {
    switch (category) {
      case 'EMERGENCY': return '보안';
      case 'EVENT': return '이벤트';
      case 'SERVICE_GUIDE': return '안내';
      case 'UPDATE': return '업데이트';
      default: return category || '-';
    }
  };

  async function fetchList(q) {
    const params = new URLSearchParams();
    if (q) params.set('q', q);
    const res = await fetch(`${API}?${params.toString()}`, { headers: { 'Accept': 'application/json' } });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return res.json();
  }
  async function fetchById(id) {
    const res = await fetch(`${API}/${encodeURIComponent(id)}`, { headers: { 'Accept': 'application/json' } });
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
        <td><span class="row-muted" title="${esc(n.id)}">${esc(n.id)}</span></td>
        <td>${n.imageURL ? `<img src="${esc(n.imageURL)}" alt="" />` : `<div style="width:50px;height:50px;border-radius:6px;background:#eef2f7;"></div>`}</td>
        <td><div>${esc(n.title || '')}</div>${n.content ? `<div class="row-muted">${esc(n.content.slice(0, 80))}${n.content.length > 80 ? '…' : ''}</div>` : ''}</td>
        <td>${esc(n.author || '-')}</td>
        <td>${esc(n.date || '-')}</td>
        <td class="category">${esc(labelOf(n.category))}</td>
      `;
      tr.addEventListener('click', async () => {
        try {
          const fresh = await fetchById(n.id);
          fillForm(fresh);
          openModal();
        } catch (e) {
          console.error('[polar-notice] fetch by id failed', e);
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
    els.date.value = d.date || '';
    els.category.value = d.category || '';
    els.image.value = d.imageURL || '';
    els.thumbPrev.src = d.imageURL || '';
    els.content.value = d.content || '';
  }

  function toPatchPayload() {
    const body = {};
    if ((current.title ?? '')     !== els.title.value)    body.title = els.title.value;
    if ((current.author ?? '')    !== els.author.value)   body.author = els.author.value;
    if ((current.date ?? '')      !== els.date.value)     body.date = els.date.value;
    if ((current.category ?? '')  !== els.category.value) body.category = els.category.value || null;
    if ((current.imageURL ?? '')  !== els.image.value)    body.imageURL = els.image.value || null;
    if ((current.content ?? '')   !== els.content.value)  body.content = els.content.value;
    return body;
  }

  function setEditing(on) {
    [els.title, els.author, els.date, els.category, els.image, els.content].forEach(i => i.disabled = !on);
    els.edit.classList.toggle('hidden', on);
    els.save.classList.toggle('hidden', !on);
    els.cancel.classList.toggle('hidden', !on);
  }

  function openModal() {
    els.modalWrap.classList.remove('hidden');
    setEditing(false);
  }
  function closeModal() {
    els.modalWrap.classList.add('hidden');
    current = null;
  }

  async function load() {
    els.loading.hidden = false; els.empty.hidden = true; els.error.hidden = true;
    try {
      const rows = await fetchList(els.q.value.trim());
      render(rows);
    } catch (e) {
      console.error('[polar-notice] load error', e);
      els.error.hidden = false;
    } finally {
      els.loading.hidden = true;
    }
  }

  /* === 이벤트 === */
  els.btn.addEventListener('click', load);
  els.q.addEventListener('keydown', e => { if (e.key === 'Enter') load(); });

  els.modalClose.addEventListener('click', closeModal);
  els.modalWrap.addEventListener('click', e => { if (e.target === els.modalWrap) closeModal(); });

  els.image.addEventListener('input', () => { els.thumbPrev.src = els.image.value || ''; });

  els.edit.addEventListener('click', () => { if (current) setEditing(true); });

  els.cancel.addEventListener('click', () => {
    if (!current) return;
    fillForm(current);
    setEditing(false);
  });

  els.form.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!current) return;

    const payload = toPatchPayload();
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
      console.error('[polar-notice] patch failed:', err);
      alert('저장에 실패했습니다.');
    } finally {
      els.save.disabled = false;
    }
  });

  els.del.addEventListener('click', async () => {
    if (!current) return;
    const yes = confirm('정말 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.');
    if (!yes) return;

    try {
      els.del.disabled = true;
      const res = await fetch(`${API}/${encodeURIComponent(current.id)}`, { method: 'DELETE' });
      if (!res.ok && res.status !== 204) throw new Error(`HTTP ${res.status}`);
      closeModal();
      await load();
      alert('삭제되었습니다.');
    } catch (err) {
      console.error('[polar-notice] delete failed:', err);
      alert('삭제에 실패했습니다.');
    } finally {
      els.del.disabled = false;
    }
  });

  /* 초기 로딩 */
  document.addEventListener('DOMContentLoaded', load);
})();
