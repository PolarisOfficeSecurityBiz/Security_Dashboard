/* =========================
   Customer Detail Scripts
   ========================= */

/* 1) 섹션별 아코디언 (aria-controls 사용) */
(function () {
  const init = () => {
    document.querySelectorAll('.section').forEach(section => {
      const btn = section.querySelector('.chevron-btn');
      if (!btn) return;

      const panel = btn.getAttribute('aria-controls')
        ? section.querySelector('#' + btn.getAttribute('aria-controls'))
        : section.querySelector('.accordion-panel');
      if (!panel) return;

      const setOpen = (open) => {
        btn.setAttribute('aria-expanded', open ? 'true' : 'false');
        panel.classList.toggle('show', open);
      };

      // 초기 상태 동기화
      setOpen(btn.getAttribute('aria-expanded') !== 'false');

      btn.addEventListener('click', (e) => {
        e.preventDefault();
        e.stopPropagation();
        setOpen(!(btn.getAttribute('aria-expanded') === 'true'));
      });
    });
  };

  (document.readyState === 'loading')
    ? document.addEventListener('DOMContentLoaded', init)
    : init();
})();

/* 2) 고객사 삭제 확인 (상단/인라인) */
(function () {
  const hook = (btn, form) => {
    if (!btn || !form) return;
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      if (confirm('고객사와 연결된 서비스/담당자 정보가 모두 삭제됩니다. 진행할까요?')) {
        form.submit();
      }
    });
  };

  hook(document.getElementById('btnDeleteCustomer'),
       document.getElementById('formDeleteCustomer'));

  hook(document.getElementById('btnDeleteCustomerInline'),
       document.getElementById('formDeleteCustomerInline') || document.getElementById('formDeleteCustomer'));
})();

/* 3) 고객사 수정 모드 토글(수정하기 → 저장) */
(function () {
  const form = document.getElementById('customerUpdateForm');
  const btn  = document.getElementById('btnEditCustomer');
  if (!form || !btn) return;

  const editable = ['customerName', 'connectedCompany']
    .map(id => document.getElementById(id))
    .filter(Boolean);

  const setDisabled = (flag) => editable.forEach(el => { el.disabled = flag; });

  // 기본은 조회 모드
  setDisabled(true);
  btn.dataset.mode = 'view';
  btn.textContent = '수정하기';
  btn.type = 'button';

  btn.addEventListener('click', () => {
    if (btn.dataset.mode === 'view') {
      setDisabled(false);
      btn.dataset.mode = 'edit';
      btn.textContent = '저장';
    } else {
      form.submit();
    }
  });
})();

/* 4) 모달 공통 (서비스 추가/수정) */
(function () {
  const open = (id) => {
    const m = document.getElementById(id);
    if (!m) return;
    m.classList.add('show');
    m.setAttribute('aria-hidden', 'false');
    m.querySelector('input,select,textarea,button')?.focus();
  };
  const close = (id) => {
    const m = document.getElementById(id);
    if (!m) return;
    m.classList.remove('show');
    m.setAttribute('aria-hidden', 'true');
  };

  document.getElementById('btnOpenSvcCreate')
    ?.addEventListener('click', () => open('svcCreateModal'));

  document.querySelectorAll('[data-close]')
    .forEach(b => b.addEventListener('click', () => close(b.dataset.close)));

  document.querySelectorAll('.modal')
    .forEach(m => m.addEventListener('click', (e) => { if (e.target === m) m.classList.remove('show'); }));

  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') document.querySelectorAll('.modal.show').forEach(m => m.classList.remove('show'));
  });

  // (옵션) 서비스 수정 모달용 데이터 주입 훅 (현재 리스트에 수정 버튼이 없다면 미사용)
  const editForm = document.getElementById('svcEditForm');
  if (editForm) {
    const base = editForm.getAttribute('data-baseaction') || editForm.action;
    editForm.setAttribute('data-baseaction', base);

    document.querySelectorAll('.btnOpenSvcEdit').forEach(btn => {
      btn.addEventListener('click', () => {
        editForm.action = base.replace(/\/services\/\d+\/update/, `/services/${btn.dataset.id}/update`);
        const set = (id, v) => { const el = document.getElementById(id); if (el) el.value = v ?? ''; };
        set('svcEditName',    btn.dataset.name);
        set('svcEditDomain',  btn.dataset.domain);
        set('svcEditProduct', btn.dataset.product);
        set('svcEditCpi',     btn.dataset.cpi);
        set('svcEditRs',      btn.dataset.rs);
        set('svcEditLic',     btn.dataset.lic);
        open('svcEditModal');
      });
    });
  }
})();

/* 5) data-confirm 공통 */
(function () {
  document.addEventListener('click', function(e){
    const btn = e.target.closest('button[data-confirm]');
    if (!btn) return;
    const msg = btn.getAttribute('data-confirm') || '진행할까요?';
    if (!confirm(msg)) {
      e.preventDefault();
      e.stopPropagation();
    }
  });
})();
