/* 아코디언 */
(function () {
  const init = () => {
    document.querySelectorAll('.section').forEach(section => {
      const btn = section.querySelector('.chevron-btn');
      if (!btn) return;
      const panel = section.querySelector('#' + btn.getAttribute('aria-controls'));
      const setOpen = (open) => {
        btn.setAttribute('aria-expanded', open ? 'true' : 'false');
        panel?.classList.toggle('show', open);
      };
      setOpen(btn.getAttribute('aria-expanded') !== 'false');
      btn.addEventListener('click', (e) => {
        e.preventDefault();
        setOpen(!(btn.getAttribute('aria-expanded') === 'true'));
      });
    });
  };
  (document.readyState === 'loading') ? document.addEventListener('DOMContentLoaded', init) : init();
})();

/* 행 클릭 → 서비스 상세 이동 */
(function () {
  document.querySelectorAll('tr.row-link[data-href]').forEach(tr => {
    tr.addEventListener('click', () => {
      const url = tr.getAttribute('data-href');
      if (url) window.location.href = url;
    });
    tr.tabIndex = 0;
    tr.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') {
        const url = tr.getAttribute('data-href');
        if (url) window.location.href = url;
      }
    });
  });
})();

/* 고객사 수정 토글 (고객사명/연결사만 편집) */
(function () {
  const form = document.getElementById('customerUpdateForm');
  const btn  = document.getElementById('btnEditCustomer');
  if (!form || !btn) return;

  const editable = ['customerName', 'connectedCompany']
    .map(id => document.getElementById(id))
    .filter(Boolean);

  const setDisabled = (flag) => editable.forEach(el => el.disabled = flag);

  setDisabled(true);
  btn.dataset.mode = 'view';
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

/* 고객사 삭제 확인 */
(function () {
  const btn = document.getElementById('btnDeleteCustomerInline');
  const form = document.getElementById('formDeleteCustomerInline');
  if (!btn || !form) return;
  btn.addEventListener('click', (e) => {
    e.preventDefault();
    if (confirm('고객사와 연결된 서비스/담당자 정보가 모두 삭제됩니다. 진행할까요?')) {
      form.submit();
    }
  });
})();

/* 모달 열고닫기 (서비스 추가) */
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
  document.getElementById('btnOpenSvcCreate')?.addEventListener('click', () => open('svcCreateModal'));
  document.querySelectorAll('[data-close]').forEach(b => b.addEventListener('click', () => close(b.dataset.close)));
  document.querySelectorAll('.modal').forEach(m => m.addEventListener('click', (e) => { if (e.target === m) m.classList.remove('show'); }));
  document.addEventListener('keydown', (e) => { if (e.key === 'Escape') document.querySelectorAll('.modal.show').forEach(m => m.classList.remove('show')); });
})();
