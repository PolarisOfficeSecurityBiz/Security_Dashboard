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
      deleteForm.style.display = on ? 'none' : ''; // 삭제 버튼 숨김/표시
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
    // 첫 편집 필드 포커스
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
