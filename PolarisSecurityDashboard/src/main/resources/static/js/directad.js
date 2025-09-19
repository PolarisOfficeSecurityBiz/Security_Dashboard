(function () {
  const API = '/api/v1/direct-ads';

  // === helpers ===
  const esc = (s) =>
    String(s ?? '')
      .replaceAll('&', '&amp;').replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;').replaceAll('"', '&quot;')
      .replaceAll("'", '&#39;');

  const url = (s) => { try { return encodeURI(String(s ?? '')); } catch { return '#'; } };
  const $ = (sel) => document.querySelector(sel);

  // 화면 상태
  const state = {
    raw: [],         // 전체 목록
    filtered: [],    // 필터링 목록
    current: null,   // 현재 선택된 아이템(수정 모드)
    editing: false,  // 편집 여부
    creating: false, // 생성 모드 여부
  };

  function show(id, visible) {
    const el = document.getElementById(id);
    if (el) el.hidden = !visible;
  }

  /* =========================
      Modal open/close
     ========================= */
  function openModal() {
    const modal = $('#adModal');
    if (!modal) return;

    // hidden 제거 + class open
    modal.removeAttribute('hidden');
    modal.classList.add('open');
    modal.style.setProperty('display', 'block', 'important');

    // 포커스
    const modalContent = document.getElementById('modalContent');
    if (modalContent) setTimeout(() => modalContent.focus(), 0);

    // ESC
    document.addEventListener('keydown', onEsc);
  }

  function closeModal() {
    const modal = $('#adModal');
    if (!modal) return;

    modal.classList.remove('open');
    modal.style.setProperty('display', 'none', 'important');
    modal.setAttribute('hidden', '');

    document.removeEventListener('keydown', onEsc);

    // 상태 초기화
    state.current = null;
    state.creating = false;
    setFormEditable(false);
    clearForm();
  }

  function onEsc(e) { if (e.key === 'Escape') closeModal(); }
  $('#adModal')?.addEventListener('click', (e) => {
    if (e.target === $('#adModal')) closeModal(); // 배경 클릭 시 닫기
  });

  /* =========================
      API
     ========================= */
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

  async function post(payload) {
    const res = await fetch(API, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
      body: JSON.stringify(payload),
    });
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

  /* =========================
      List / Filter / Render
     ========================= */
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
            <span class="color-dot" style="background:${esc(ad.backgroundColor || '#eee')}"></span>
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
          openForEdit(fresh);
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

  /* =========================
      Form helpers
     ========================= */
  function clearForm() {
    setVal('adId',''); setVal('adType',''); setVal('advertiserName','');
    setVal('backgroundColor',''); setVal('imageUrl',''); setVal('targetUrl','');
    $('#bgDot')?.style.setProperty('background', '#eee');
    $('#openImg')?.setAttribute('href', '#');
    $('#openLink')?.setAttribute('href', '#');
    const pv = $('#imgPreview'); if (pv) { pv.src = ''; pv.alt = '이미지 없음'; }
  }

  function fillForm(ad) {
    setVal('adId', ad.id);
    setVal('adType', ad.adType);
    setVal('advertiserName', ad.advertiserName);
    setVal('backgroundColor', ad.backgroundColor);
    setVal('imageUrl', ad.imageUrl);
    setVal('targetUrl', ad.targetUrl);

    $('#bgDot')?.style.setProperty('background', ad.backgroundColor || '#eee');
    $('#openImg')?.setAttribute('href', ad.imageUrl || '#');
    $('#openLink')?.setAttribute('href', ad.targetUrl || '#');

    const pv = $('#imgPreview');
    if (pv) { pv.src = ad.imageUrl || ''; pv.alt = ad.imageUrl ? '이미지 미리보기' : '이미지 없음'; }
  }

  function setVal(id, v) {
    const el = document.getElementById(id); if (el) el.value = v ?? '';
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
  function fmt(v) {
    const ms = toMs(v); if (!ms) return ''; const d = new Date(ms);
    const y = d.getFullYear(), m = String(d.getMonth()+1).padStart(2,'0'), da = String(d.getDate()).padStart(2,'0');
    const h = String(d.getHours()).padStart(2,'0'), mi = String(d.getMinutes()).padStart(2,'0');
    return `${y}-${m}-${da} ${h}:${mi}`;
  }
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

  /* =========================
      Modes (Edit / Create)
     ========================= */
  function setFormEditable(editable) {
    ['adType','advertiserName','backgroundColor','imageUrl','targetUrl']
      .forEach(id => { const el = document.getElementById(id); if (el) el.disabled = !editable; });

    const saveBtn   = document.getElementById('saveBtn');
    const editBtn   = document.getElementById('editBtn');
    const cancelBtn = document.getElementById('cancelBtn');

    if (state.creating) {
      // 생성 모드: 수정 버튼 숨기고 저장/취소만
      if (saveBtn)   saveBtn.hidden   = false;
      if (editBtn)   editBtn.hidden   = true;
      if (cancelBtn) cancelBtn.hidden = false;
      state.editing = true;
      return;
    }

    // 조회→수정 토글
    if (saveBtn)   saveBtn.hidden   = !editable;
    if (editBtn)   editBtn.hidden   =  editable;
    if (cancelBtn) cancelBtn.hidden = !editable;
    state.editing = editable;
  }

  function openForEdit(fresh) {
    state.current  = fresh;
    state.creating = false;
    fillForm(fresh);
    const titleEl = document.getElementById('adModalTitle');
    if (titleEl) titleEl.textContent = '광고 조회 및 수정';
    openModal();
    setFormEditable(false);
    document.getElementById('deleteBtn')?.classList.remove('hidden');
  }

  function openForCreate() {
    state.current  = null;
    state.creating = true;
    clearForm();
    const titleEl = document.getElementById('adModalTitle');
    if (titleEl) titleEl.textContent = '광고 등록';
    openModal();
    setFormEditable(true); // 입력 가능
    document.getElementById('deleteBtn')?.classList.add('hidden');
  }

  /* =========================
      Wire up
     ========================= */
  document.addEventListener('DOMContentLoaded', () => {
    // 목록/검색
    $('#refreshBtn')?.addEventListener('click', load);
    $('#q')?.addEventListener('input', applyFilter);
    $('#type')?.addEventListener('change', applyFilter);

    // 헤더 버튼
    $('#createBtn')?.addEventListener('click', openForCreate);

    // 모달 버튼
    $('#closeModal')?.addEventListener('click', closeModal);
    $('#cancelBtn')?.addEventListener('click', closeModal);
    $('#editBtn')?.addEventListener('click', () => { if (!state.creating) setFormEditable(true); });

    // 저장 (create / patch 구분)
    $('#adEditForm')?.addEventListener('submit', async (e) => {
      e.preventDefault();
      const saveBtn = $('#saveBtn');

      try {
        saveBtn && (saveBtn.disabled = true);

        if (state.creating) {
          // CREATE
          const payload = {
            adType:          $('#adType')?.value?.trim(),
            advertiserName:  $('#advertiserName')?.value?.trim(),
            backgroundColor: $('#backgroundColor')?.value?.trim(),
            imageUrl:        $('#imageUrl')?.value?.trim(),
            targetUrl:       $('#targetUrl')?.value?.trim(),
          };
          if (!payload.adType || !payload.advertiserName) {
            alert('유형과 광고주는 필수입니다.');
            return;
          }
          const created = await post(payload);
          openForEdit(created);
          await load();
          alert('등록되었습니다.');
        } else if (state.current) {
          // PATCH
          const payload = diffPayload(state.current);
          if (Object.keys(payload).length === 0) {
            alert('변경된 내용이 없습니다.');
            setFormEditable(false);
            return;
          }
          const updated = await patch(state.current.id, payload);
          openForEdit(updated);
          await load();
          alert('저장되었습니다.');
        }
      } catch (err) {
        console.error('[directad] save failed:', err);
        alert('처리에 실패했습니다.');
      } finally {
        saveBtn && (saveBtn.disabled = false);
      }
    });

    // 삭제
    $('#deleteBtn')?.addEventListener('click', async () => {
      if (!state.current) return;
      const yes = confirm('정말 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.');
      if (!yes) return;
      const btn = $('#deleteBtn');
      try {
        btn.disabled = true;
        await destroy(state.current.id);
        closeModal();
        await load();
        alert('삭제되었습니다.');
      } catch (err) {
        console.error('[directad] delete failed:', err);
        alert('삭제에 실패했습니다.');
      } finally {
        btn.disabled = false;
      }
    });

    // 최초 로드
    load();
  });
})();
