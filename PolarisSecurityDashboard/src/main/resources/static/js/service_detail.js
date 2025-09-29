/* =========================
   Service Detail – single-form edit toggle
   ========================= */
(() => {
  'use strict';

  const $  = (s) => document.querySelector(s);
  const $$ = (s) => Array.from(document.querySelectorAll(s));

  const form       = $('#svcForm');
  if (!form) return;

  const btnEdit    = $('#btnSvcEdit');
  const btnSave    = $('#btnSvcSave');
  const btnCancel  = $('#btnSvcCancel');
  const btnLicense = $('#btnLicense');
  const deleteForm = $('#deleteForm');     // 삭제 버튼이 들어있는 form (보기 모드에서만 표시)

  // 편집 가능한 요소들(필드에 data-editable 넣기)
  const editables = $$('[data-editable]');

  // 원본값 스냅샷
  const snapshot = () => {
    editables.forEach(el => {
      el.dataset.orig = (el.type === 'checkbox' || el.type === 'radio')
        ? String(el.checked)
        : (el.value ?? '');
    });
  };

  // 값 복원
  const restore = () => {
    editables.forEach(el => {
      if (el.type === 'checkbox' || el.type === 'radio') {
        el.checked = (el.dataset.orig === 'true');
      } else {
        el.value = el.dataset.orig || '';
      }
    });
  };

  // 모드 전환 (on = 편집)
  const setEditing = (on) => {
    // 필드 활성/비활성
    editables.forEach(el => { el.disabled = !on; });

    // 상단 액션 버튼 토글
    if (btnEdit)   btnEdit.hidden   =  on;  // 편집 중이면 숨김
    if (btnSave)   btnSave.hidden   = !on;  // 편집 중에만 보임
    if (btnCancel) btnCancel.hidden = !on;  // 편집 중에만 보임

    // 위험/외부 액션은 편집 중 비활성
    if (btnLicense) btnLicense.disabled = on;
    if (deleteForm) {
      deleteForm.style.display = on ? 'none' : '';
      deleteForm.style.pointerEvents = on ? 'none' : '';
      deleteForm.style.opacity = on ? '0.5' : '1';
    }

    form.dataset.mode = on ? 'edit' : 'view';
  };

  // 초기: 조회 모드
  setEditing(false);
  snapshot();

  // 수정 클릭 → 편집 모드
  btnEdit?.addEventListener('click', () => {
    snapshot();
    setEditing(true);
    const first = editables.find(el => !el.disabled && el.offsetParent !== null);
    first?.focus();
  });

  // 취소 클릭 → 값 복원 + 조회 모드
  btnCancel?.addEventListener('click', () => {
    restore();
    setEditing(false);
  });

  // 저장 중 중복 클릭 방지
  form.addEventListener('submit', () => {
    if (btnSave) btnSave.disabled = true;
  });

  // ESC로 취소
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

  // 열기
  document.getElementById('btnOpenContact')
    ?.addEventListener('click', () => open('contactModal'));

  // 닫기 버튼들
  document.querySelectorAll('[data-close]')
    .forEach(b => b.addEventListener('click', () => close(b.dataset.close)));

  // 배경 클릭으로 닫기
  document.querySelectorAll('.modal')
    .forEach(m => m.addEventListener('click', (e) => {
      if (e.target === m) m.classList.remove('show');
    }));

  // ESC로 닫기
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      document.querySelectorAll('.modal.show')
        .forEach(m => m.classList.remove('show'));
      document.body.style.overflow = '';
    }
  });
})();

/* =========================
   Domain Logs (service domain) – /api/logs/report 기반
   ========================= */
(() => {
  'use strict';

  const hostSection = document.getElementById('logsSection');
  if (!hostSection) return;

  // DOM
  const table      = document.getElementById('logsTable');
  const tbody      = table?.querySelector('tbody');
  const daysSel    = document.getElementById('logsDays');
  const infoSpan   = document.getElementById('logsResultInfo');
  const btnPrev    = document.getElementById('logsPrev');
  const btnNext    = document.getElementById('logsNext');
  const pageInfo   = document.getElementById('logsPageInfo');

  // Params
  const domainInit = (hostSection.getAttribute('data-domain') || '').trim();
  if (!domainInit) {
    // 도메인이 비어있으면 로그 섹션을 비활성화
    hostSection.style.display = 'none';
    return;
  }
  const PAGE_SIZE = 10;

  // State
  let rows = [];      // 전체 원본
  let page = 1;       // 1-based
  let pages = 1;

  // API 호출 (동일 오리진, 세션 쿠키 포함)
  async function fetchLogs({ days, domain }) {
    const u = new URL('/api/logs/report', window.location.origin);
    if (days)   u.searchParams.set('days',   String(days));
    if (domain) u.searchParams.set('domain', domain);

    // 필요 시 타입 필터:
    // u.searchParams.set('type','MALWARE');

    const res = await fetch(u.toString(), { credentials: 'same-origin' });
    if (!res.ok) {
      console.warn('[logs] fetch failed:', res.status, await res.text());
      return [];
    }
    return await res.json();
  }

  // 날짜 포맷
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

  // 행 빌드
  function buildRow(item) {
    // 백엔드 DTO 예시: id, createdAt, domain, logType, osVersion, appVersion, extra
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

  // 렌더 (클라이언트 페이징)
  function render() {
    if (!tbody) return;

    // 비우기
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

    // 페이지 정보
    pages = Math.max(1, Math.ceil(rows.length / PAGE_SIZE));
    if (page > pages) page = pages;

    const start = (page - 1) * PAGE_SIZE;
    const slice = rows.slice(start, start + PAGE_SIZE);

    slice.forEach(item => tbody.appendChild(buildRow(item)));

    infoSpan && (infoSpan.textContent = `${rows.length} rows`);
    pageInfo && (pageInfo.textContent = `${page} / ${pages}`);
  }

  // 데이터 로드 + 렌더
  async function loadAndRender() {
    const days = Number(daysSel?.value || hostSection.getAttribute('data-days') || 7);
    const domain = domainInit;

    rows = await fetchLogs({ days, domain });

    // 필요하면 완전일치 추가 필터:
    // rows = rows.filter(r => (r.domain || '').toLowerCase() === domain.toLowerCase());

    // 최신순 정렬
    rows.sort((a,b) => {
      const A = new Date(a.createdAt ?? a.createAt).getTime();
      const B = new Date(b.createdAt ?? b.createAt).getTime();
      return B - A;
    });

    page = 1;
    render();
  }

  // 이벤트
  daysSel?.addEventListener('change', () => loadAndRender());
  btnPrev?.addEventListener('click', () => { if (page > 1) { page--; render(); } });
  btnNext?.addEventListener('click', () => { if (page < pages) { page++; render(); } });

  // 초기 로드
  loadAndRender();
})();
