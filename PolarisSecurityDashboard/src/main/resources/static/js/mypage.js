// /js/mypage.js
(() => {
  const qs = (id) => document.getElementById(id);

  document.addEventListener('DOMContentLoaded', () => {
    const dlg = qs('pwdDialog');
    const openBtn = qs('openPwdDialog');
    const cancelBtn = qs('cancelPwdDialog');
    const cancelBtn2 = qs('cancelPwdDialog2'); // ✅ 추가
    const pwdForm = qs('pwdForm');

    // 모달 열기
    openBtn && openBtn.addEventListener('click', () => {
      if (!dlg) return;
      if (typeof dlg.showModal === 'function') dlg.showModal();
      else dlg.setAttribute('open', 'open');
      setTimeout(() => qs('curPwd')?.focus(), 0);
      document.documentElement.style.overflow = 'hidden';
    });

    // 공통 닫기
    function closeDialog() {
      if (!dlg) return;
      try { dlg.close(); } catch (_) { dlg.removeAttribute('open'); }
      document.documentElement.style.overflow = '';
    }

    cancelBtn && cancelBtn.addEventListener('click', closeDialog);
    cancelBtn2 && cancelBtn2.addEventListener('click', closeDialog); // ✅ 추가

    dlg && dlg.addEventListener('click', (e) => {
      const rect = dlg.getBoundingClientRect();
      const inside = rect.top <= e.clientY && e.clientY <= rect.bottom &&
                     rect.left <= e.clientX && e.clientX <= rect.right;
      if (!inside) closeDialog();
    });

    dlg && dlg.addEventListener('close', () => {
      document.documentElement.style.overflow = '';
    });

    // 보기/숨기기 토글
    document.addEventListener('click', (e) => {
      const btn = e.target.closest('[data-toggle]');
      if (!btn) return;
      const input = qs(btn.getAttribute('data-toggle'));
      if (!input) return;
      const isPw = input.type === 'password';
      input.type = isPw ? 'text' : 'password';
      btn.textContent = isPw ? '숨기기' : '보기';
    });

    // 새 비밀번호 일치 검사
    pwdForm && pwdForm.addEventListener('submit', (e) => {
      const a = (qs('newPwd')?.value || '').trim();
      const b = (qs('newPwd2')?.value || '').trim();
      if (a !== b) {
        e.preventDefault();
        alert('새 비밀번호와 확인이 일치하지 않습니다.');
        qs('newPwd2')?.focus();
      }
    });
  });
})();
