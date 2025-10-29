(() => {
  'use strict';

  const $  = (s) => document.querySelector(s);
  const $$ = (s) => Array.from(document.querySelectorAll(s));

  const form      = $('#licForm');
  const btnEdit   = $('#btnEdit');
  const btnSave   = $('#btnSave');
  const btnCancel = $('#btnCancel');
  const deleteFrm = $('#deleteForm');

  if (!form) return;

  const editables = $$('[data-editable]');

  // snapshot & restore
  const snapshot = () => editables.forEach(el => el.dataset.orig = el.value ?? '');
  const restore  = () => editables.forEach(el => el.value = el.dataset.orig ?? '');

  function setEditing(on) {
    editables.forEach(el => { el.disabled = !on; });
    if (btnEdit)   btnEdit.hidden   =  on;
    if (btnSave)   btnSave.hidden   = !on;
    if (btnCancel) btnCancel.hidden = !on;

    // 위험 액션 잠금
    if (deleteFrm) {
      deleteFrm.style.pointerEvents = on ? 'none' : '';
      deleteFrm.style.opacity = on ? 0.5 : 1;
    }

    form.dataset.mode = on ? 'edit' : 'view';
  }

  // min(today) for expiry
  const exp = $('#expiryDate');
  if (exp) {
    const t = new Date();
    const yyyy = t.getFullYear();
    const mm   = String(t.getMonth()+1).padStart(2,'0');
    const dd   = String(t.getDate()).padStart(2,'0');
    exp.min = `${yyyy}-${mm}-${dd}`;
  }

  // init
  setEditing(false);
  snapshot();

  btnEdit?.addEventListener('click', () => {
    snapshot();
    setEditing(true);
    editables[0]?.focus();
  });

  btnCancel?.addEventListener('click', () => {
    restore();
    setEditing(false);
  });

  form.addEventListener('submit', () => { if (btnSave) btnSave.disabled = true; });

  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && form.dataset.mode === 'edit') {
      e.preventDefault();
      btnCancel?.click();
    }
  });
})();

/* ===== Service Add Modal ===== */
(() => {
  'use strict';

  const modal = document.getElementById('svcModal');
  const openBtn = document.getElementById('btnAddService');
  const form = document.getElementById('svcForm');
  if (!modal || !openBtn || !form) return;

  const open = () => {
    modal.classList.add('show');
    modal.setAttribute('aria-hidden','false');
    document.body.style.overflow = 'hidden';
    modal.querySelector('input,select,textarea,button')?.focus();
  };
  const close = () => {
    modal.classList.remove('show');
    modal.setAttribute('aria-hidden','true');
    document.body.style.overflow = '';
  };

  openBtn.addEventListener('click', open);
  document.querySelectorAll('[data-close="svcModal"]').forEach(b => b.addEventListener('click', close));
  modal.addEventListener('click', (e) => { if (e.target === modal) close(); });
  document.addEventListener('keydown', (e) => { if (e.key === 'Escape' && modal.classList.contains('show')) close(); });

  form.addEventListener('submit', () => {
    // 서버로 전송 (동일 페이지 리다이렉트 가정)
    // 중복 제출 방지
    form.querySelector('button[type="submit"]')?.setAttribute('disabled','true');
  });
})();
