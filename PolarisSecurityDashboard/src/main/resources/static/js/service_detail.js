/* =========================
   Service Detail – single-form edit toggle
   ========================= */
(() => {
  const $  = (s) => document.querySelector(s);
  const $$ = (s) => Array.from(document.querySelectorAll(s));

  const form = $('#svcForm');
  if (!form) return;

  const btnEdit    = $('#btnSvcEdit');
  const btnSave    = $('#btnSvcSave');
  const btnCancel  = $('#btnSvcCancel');
  const btnLicense = $('#btnLicense');
  const deleteForm = $('#deleteForm');

  const editables = $$('[data-editable]');

  // 스냅샷/복원
  const snapshot = () => editables.forEach(el => {
    el.dataset.orig = (el.type === 'checkbox' || el.type === 'radio')
      ? String(el.checked)
      : (el.value ?? '');
  });
  const restore = () => editables.forEach(el => {
    if (el.type === 'checkbox' || el.type === 'radio') {
      el.checked = (el.dataset.orig === 'true');
    } else {
      el.value = el.dataset.orig || '';
    }
  });

  const setEditing = (on) => {
    editables.forEach(el => el.disabled = !on);

    // 보기/편집 전환
    btnEdit.hidden   =  on;
    btnSave.hidden   = !on;
    btnCancel.hidden = !on;

    // 편집 중에는 위험/외부 액션 막기
    if (btnLicense) btnLicense.disabled = on;
    if (deleteForm) deleteForm.style.display = on ? 'none' : '';

    form.dataset.mode = on ? 'edit' : 'view';
  };

  // 초기: 조회 모드
  setEditing(false);
  snapshot();

  // 이벤트
  btnEdit?.addEventListener('click', () => {
    snapshot();
    setEditing(true);
    editables.find(el => !el.disabled && el.offsetParent !== null)?.focus();
  });

  btnCancel?.addEventListener('click', () => {
    restore();
    setEditing(false);
  });

  // 저장 중 재클릭 방지
  form.addEventListener('submit', () => { btnSave.disabled = true; });

  // ESC 취소
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

  // 닫기 버튼
  document.querySelectorAll('[data-close]')
    .forEach(b => b.addEventListener('click', () => close(b.dataset.close)));

  // 배경 클릭 닫기
  document.querySelectorAll('.modal')
    .forEach(m => m.addEventListener('click', (e) => {
      if (e.target === m) m.classList.remove('show');
    }));

  // ESC 닫기
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      document.querySelectorAll('.modal.show')
        .forEach(m => m.classList.remove('show'));
      document.body.style.overflow = '';
    }
  });
})();
