// /js/mypage.js
(() => {
  function qs(id) { return document.getElementById(id); }

  document.addEventListener('DOMContentLoaded', () => {
    const dlg = qs('pwdDialog');
    const openBtn = qs('openPwdDialog');
    const cancelBtn = qs('cancelPwdDialog');
    const pwdForm = qs('pwdForm');

    // 모달 열기
    openBtn && openBtn.addEventListener('click', () => {
      if (!dlg) return;
      if (typeof dlg.showModal === 'function') dlg.showModal();
      else dlg.setAttribute('open', 'open');
      // 첫 포커스
      const first = qs('curPwd');
      setTimeout(() => first && first.focus(), 0);
      // 배경 스크롤 방지
      document.documentElement.style.overflow = 'hidden';
    });

    // 모달 닫기
    function closeDialog() {
      if (!dlg) return;
      try { dlg.close(); } catch (_) { dlg.removeAttribute('open'); }
      document.documentElement.style.overflow = '';
    }

    // 취소 버튼
    cancelBtn && cancelBtn.addEventListener('click', closeDialog);

    // 백드롭 클릭 닫기
    dlg && dlg.addEventListener('click', (e) => {
      const rect = dlg.getBoundingClientRect();
      const inside =
        rect.top <= e.clientY && e.clientY <= rect.bottom &&
        rect.left <= e.clientX && e.clientX <= rect.right;
      if (!inside) closeDialog();
    });

    // 다이얼로그가 닫힐 때 배경 스크롤 복구
    dlg && dlg.addEventListener('close', () => {
      document.documentElement.style.overflow = '';
    });

    // 보기/숨기기 토글 (버튼 data-toggle 속성)
    document.addEventListener('click', (e) => {
      const btn = e.target.closest('[data-toggle]');
      if (!btn) return;
      const id = btn.getAttribute('data-toggle');
      const input = qs(id);
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
