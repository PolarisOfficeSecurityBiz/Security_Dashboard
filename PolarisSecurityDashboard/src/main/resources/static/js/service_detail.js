/* =========================
   Service Detail – single-form edit toggle
   ========================= */
(() => {
  'use strict';

  const $  = (s) => document.querySelector(s);
  const $$ = (s) => Array.from(document.querySelectorAll(s));

  const form       = $('#svcForm');
  if (!form) return;

  const btnEdit      = $('#btnSvcEdit');
  const btnSave      = $('#btnSvcSave');
  const btnCancel    = $('#btnSvcCancel');
  const btnLicense   = $('#btnLicense');      // 발급하기 (있을 때만)
  const btnLicView   = $('#btnLicenseView');  // 상세보기 (있을 때만)
  const deleteForm   = $('#deleteForm');

  const editables = $$('[data-editable]');

  const snapshot = () => {
    editables.forEach(el => {
      el.dataset.orig = (el.type === 'checkbox' || el.type === 'radio')
        ? String(el.checked)
        : (el.value ?? '');
    });
  };

  const restore = () => {
    editables.forEach(el => {
      if (el.type === 'checkbox' || el.type === 'radio') {
        el.checked = (el.dataset.orig === 'true');
      } else {
        el.value = el.dataset.orig || '';
      }
    });
  };

  const setEditing = (on) => {
    editables.forEach(el => { el.disabled = !on; });

    if (btnEdit)   btnEdit.hidden   =  on;
    if (btnSave)   btnSave.hidden   = !on;
    if (btnCancel) btnCancel.hidden = !on;

    // 외부 액션 제어
    if (btnLicense) btnLicense.disabled = on;
    if (btnLicView) {
      btnLicView.style.pointerEvents = on ? 'none' : '';
      btnLicView.style.opacity = on ? '0.6' : '1';
      btnLicView.tabIndex = on ? -1 : 0;
    }
    if (deleteForm) {
      deleteForm.style.display = on ? 'none' : '';
      deleteForm.style.pointerEvents = on ? 'none' : '';
      deleteForm.style.opacity = on ? '0.5' : '1';
    }

    form.dataset.mode = on ? 'edit' : 'view';
  };

  setEditing(false);
  snapshot();

  btnEdit?.addEventListener('click', () => {
    snapshot();
    setEditing(true);
    const first = editables.find(el => !el.disabled && el.offsetParent !== null);
    first?.focus();
  });

  btnCancel?.addEventListener('click', () => {
    restore();
    setEditing(false);
  });

  form.addEventListener('submit', () => {
    if (btnSave) btnSave.disabled = true;
  });

  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && form.dataset.mode === 'edit') {
      e.preventDefault();
      btnCancel?.click();
    }
  });
})();

/* =========================
   Modal: 담당자 추가
   ========================= */
(() => {
  'use strict';

  const open = (id) => {
    const m = document.getElementById(id);
    if (!m) return;
    m.classList.add('show');
    m.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden';
    m.querySelector('input,textarea,select,button')?.focus();
  };

  const close = (id) => {
    const m = document.getElementById(id);
    if (!m) return;
    m.classList.remove('show');
    m.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = '';
  };

  document.getElementById('btnOpenContact')
    ?.addEventListener('click', () => open('contactModal'));

  document.querySelectorAll('[data-close]')
    .forEach(b => b.addEventListener('click', () => close(b.dataset.close)));

  document.querySelectorAll('.modal')
    .forEach(m => m.addEventListener('click', (e) => {
      if (e.target === m) m.classList.remove('show');
    }));

  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      document.querySelectorAll('.modal.show')
        .forEach(m => m.classList.remove('show'));
      document.body.style.overflow = '';
    }
  });
})();

/* =========================
   Domain Logs – /api/logs/report
   ========================= */
(() => {
  'use strict';

  const hostSection = document.getElementById('logsSection');
  if (!hostSection) return;

  const table      = document.getElementById('logsTable');
  const tbody      = table?.querySelector('tbody');
  const daysSel    = document.getElementById('logsDays');
  const infoSpan   = document.getElementById('logsResultInfo');
  const btnPrev    = document.getElementById('logsPrev');
  const btnNext    = document.getElementById('logsNext');
  const pageInfo   = document.getElementById('logsPageInfo');

  const domainInit = (hostSection.getAttribute('data-domain') || '').trim();
  if (!domainInit) { hostSection.style.display = 'none'; return; }

  const PAGE_SIZE = 10;
  let rows = [];
  let page = 1;
  let pages = 1;

  async function fetchLogs({ days, domain }) {
    const u = new URL('/api/logs/report', window.location.origin);
    if (days)   u.searchParams.set('days',   String(days));
    if (domain) u.searchParams.set('domain', domain);

    const res = await fetch(u.toString(), { credentials: 'same-origin' });
    if (!res.ok) {
      console.warn('[logs] fetch failed:', res.status, await res.text());
      return [];
    }
    return await res.json();
  }

  function fmtDate(s) {
    if (!s) return '';
    try {
      const d = new Date(s);
      return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit', second: '2-digit'
      }).format(d);
    } catch { return s; }
  }

  function buildRow(item) {
    const tr = document.createElement('tr');
    const tdId   = document.createElement('td'); tdId.textContent   = item.id ?? '';
    const tdAt   = document.createElement('td'); tdAt.textContent   = fmtDate(item.createdAt ?? item.createAt);
    const tdDom  = document.createElement('td'); tdDom.textContent  = item.domain ?? '';
    const tdType = document.createElement('td');
    const chip   = document.createElement('span');
    chip.className = 'badge-muted strong';
    chip.textContent = item.logType ?? '';
    tdType.appendChild(chip);
    const tdOS   = document.createElement('td'); tdOS.textContent   = item.osVersion ?? '';
    const tdApp  = document.createElement('td'); tdApp.textContent  = item.appVersion ?? '';
    const tdEx   = document.createElement('td'); tdEx.textContent   =
      (typeof item.extra === 'string' ? item.extra : (item.extra ? JSON.stringify(item.extra) : ''));
    tr.append(tdId, tdAt, tdDom, tdType, tdOS, tdApp, tdEx);
    return tr;
  }

  function render() {
    if (!tbody) return;
    tbody.innerHTML = '';

    if (!rows.length) {
      const tr = document.createElement('tr');
      tr.className = 'empty-row';
      const td = document.createElement('td');
      td.colSpan = 7;
      td.className = 'empty';
      td.textContent = '로그 데이터가 없습니다.';
      tr.appendChild(td);
      tbody.appendChild(tr);
      infoSpan && (infoSpan.textContent = '0 rows');
      page = pages = 1;
      pageInfo && (pageInfo.textContent = '1 / 1');
      return;
    }

    pages = Math.max(1, Math.ceil(rows.length / PAGE_SIZE));
    if (page > pages) page = pages;

    const start = (page - 1) * PAGE_SIZE;
    const slice = rows.slice(start, start + PAGE_SIZE);
    slice.forEach(item => tbody.appendChild(buildRow(item)));

    infoSpan && (infoSpan.textContent = `${rows.length} rows`);
    pageInfo && (pageInfo.textContent = `${page} / ${pages}`);
  }

  async function loadAndRender() {
    const days = Number(daysSel?.value || hostSection.getAttribute('data-days') || 7);
    const domain = domainInit;
    rows = await fetchLogs({ days, domain });
    rows.sort((a,b) => new Date(b.createdAt ?? b.createAt) - new Date(a.createdAt ?? a.createAt));
    page = 1;
    render();
  }

  daysSel?.addEventListener('change', () => loadAndRender());
  btnPrev?.addEventListener('click', () => { if (page > 1) { page--; render(); } });
  btnNext?.addEventListener('click', () => { if (page < pages) { page++; render(); } });

  loadAndRender();
})();

/* =========================
   License Issue Modal
   ========================= */
(() => {
  'use strict';

  const btnOpen   = document.getElementById('btnLicense');         // 미발급일 때만 존재
  const modal     = document.getElementById('licenseModal');
  const formModal = document.getElementById('licenseForm');
  const issueForm = document.getElementById('licenseIssueForm');
  if (!modal || !formModal || !issueForm) return;

  // 미발급 상태가 아닐 수도 있으므로 버튼 없으면 모달 로직은 스킵
  if (!btnOpen) return;

  const $ = (s) => document.querySelector(s);

  function open() {
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm   = String(today.getMonth() + 1).padStart(2, '0');
    const dd   = String(today.getDate()).padStart(2, '0');
    const todayStr = `${yyyy}-${mm}-${dd}`;

    const next = new Date(today); next.setFullYear(next.getFullYear() + 1);
    const ny = next.getFullYear();
    const nm = String(next.getMonth() + 1).padStart(2, '0');
    const nd = String(next.getDate()).padStart(2, '0');
    const nextStr = `${ny}-${nm}-${nd}`;

    const exp = $('#licExpires');
    if (exp) { exp.min = todayStr; if (!exp.value) exp.value = nextStr; }

    modal.classList.add('show');
    modal.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden';
    exp?.focus();
  }

  function close() {
    modal.classList.remove('show');
    modal.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = '';
  }

  btnOpen.addEventListener('click', (e) => {
    e.preventDefault();
    open();
  });

  document.querySelectorAll('[data-close="licenseModal"]').forEach(b => b.addEventListener('click', close));
  modal.addEventListener('click', (e) => { if (e.target === modal) close(); });
  document.addEventListener('keydown', (e) => { if (e.key === 'Escape' && modal.classList.contains('show')) close(); });

  function setHidden(name, value) {
    let el = issueForm.querySelector(`input[name="${name}"]`);
    if (!el) {
      el = document.createElement('input');
      el.type = 'hidden';
      el.name = name;
      issueForm.appendChild(el);
    }
    el.value = value;
  }

  formModal.addEventListener('submit', (e) => {
    e.preventDefault();

    const expiryDate     = $('#licExpires')?.value || '';
    const usageLimit     = $('#licMaxUses')?.value || '2';
    const licenseType    = $('#licType')?.value || 'PROD';
    const licenseVersion = $('#licVersion')?.value || 'SDK3';

    if (!expiryDate) { alert('만료일을 선택하세요.'); return; }
    const n = Number(usageLimit);
    if (!Number.isFinite(n) || n < 1) { alert('사용제한수는 1 이상의 정수여야 합니다.'); return; }

    setHidden('expiryDate', expiryDate);
    setHidden('usageLimit', String(Math.trunc(n)));
    setHidden('licenseType', licenseType);
    setHidden('licenseVersion', licenseVersion);

    issueForm.submit(); // 서버에서 발급 후 동일 페이지로 리다이렉트 → 버튼은 '상세보기'로 렌더됨
  });
})();
