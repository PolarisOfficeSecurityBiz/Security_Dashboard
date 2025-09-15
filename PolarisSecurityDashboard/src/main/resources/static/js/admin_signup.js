(function () {
  'use strict';

  /* ======== 유틸 ======== */
  const qs  = (sel, root = document) => root.querySelector(sel);
  const qsa = (sel, root = document) => Array.prototype.slice.call(root.querySelectorAll(sel));
  const isValidEmail = (v) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v);

  // 비번 토글 (로그인 페이지와 동일 UX)
  window.togglePassword = function (btnEl) {
    try {
      const btn = btnEl || qs('.pw-toggle');
      if (!btn) return;
      const input = btn.closest('.password-wrap')?.querySelector('input');
      if (!input) return;
      const nextType = input.type === 'password' ? 'text' : 'password';
      input.type = nextType;
      btn.setAttribute('aria-label', nextType === 'password' ? 'Show password' : 'Hide password');
    } catch(_) {}
  };

  // 에러박스
  const showError = function (form, text) {
    let box = qs('.alert.dynamic', form);
    if (!box) {
      box = document.createElement('div');
      box.className = 'alert error dynamic';
      box.style.background = '#fee2e2';
      box.style.color = '#991b1b';
      box.style.borderRadius = '10px';
      box.style.padding = '10px 12px';
      box.style.fontSize = '13px';
      box.style.marginBottom = '10px';
      form.prepend(box);
    }
    box.textContent = text;
  };

  const setSubmitting = function (form, on) {
    const btn = qs('#btn-submit', form);
    if (!btn) return;
    if (on) {
      btn.disabled = true;
      if (!btn.dataset.originalText) btn.dataset.originalText = btn.textContent || '';
      btn.textContent = 'Creating…';
    } else {
      btn.disabled = false;
      if (btn.dataset.originalText != null) {
        btn.textContent = btn.dataset.originalText;
        delete btn.dataset.originalText;
      }
    }
  };

  // 비번 강도
  const scorePassword = (pw) => {
    let s = 0;
    if (!pw) return 0;
    if (pw.length >= 8) s += 1;
    if (/[A-Z]/.test(pw)) s += 1;
    if (/[a-z]/.test(pw)) s += 1;
    if (/\d/.test(pw))    s += 1;
    if (/[^A-Za-z0-9]/.test(pw)) s += 1;
    return Math.min(s, 5);
  };

  const updateMeter = (pw) => {
    const bar = qs('#pw-bar');
    const hint = qs('#pw-hint');
    const sc = scorePassword(pw);
    const perc = (sc/5)*100;
    if (bar) {
      bar.style.width = perc + '%';
      bar.style.background = sc <= 2 ? '#ef4444' : sc <= 3 ? '#f59e0b' : '#10b981';
    }
    if (hint) {
      hint.textContent = '안전도: ' + (sc <= 2 ? '약함' : sc <= 3 ? '보통' : '강함');
    }
  };

  document.addEventListener('DOMContentLoaded', function () {
    const form = qs('#admin-signup-form');
    if (!form) return;

    const nameEl = qs('#username', form);
    const emailEl = qs('#email', form);
    const pwEl = qs('#password', form);
    const pw2El = qs('#password2', form);
    const agreeEl = qs('#agree', form);
    const toggleBtn = qs('.pw-toggle', form);

    // 토글
    toggleBtn && toggleBtn.addEventListener('click', function (e) {
      e.preventDefault();
      window.togglePassword(toggleBtn);
    });

    // 강도 업데이트
    pwEl && pwEl.addEventListener('input', () => updateMeter(pwEl.value));

    // 전송
    form.addEventListener('submit', function (e) {
      const name = (nameEl?.value || '').trim();
      const email = (emailEl?.value || '').trim();
      const pw = pwEl?.value || '';
      const pw2 = pw2El?.value || '';
      const agreed = !!agreeEl?.checked;

      // 클라이언트 검증
      if (!name || name.length < 2) {
        e.preventDefault(); showError(form, '이름을 2자 이상 입력해 주세요.'); nameEl?.focus(); return;
      }
      if (!email || !isValidEmail(email)) {
        e.preventDefault(); showError(form, '이메일 형식을 확인해 주세요.'); emailEl?.focus(); return;
      }
      if (!pw || pw.length < 8) {
        e.preventDefault(); showError(form, '비밀번호는 최소 8자입니다.'); pwEl?.focus(); return;
      }
      if (pw !== pw2) {
        e.preventDefault(); showError(form, '비밀번호가 일치하지 않습니다.'); pw2El?.focus(); return;
      }
      if (!agreed) {
        e.preventDefault(); showError(form, '약관에 동의해 주세요.'); agreeEl?.focus(); return;
      }

      // 서버 DTO와 일치: email, password, username, role=ADMIN (hidden)
      // 컨트롤러는 /signup POST 처리 후 /login으로 리다이렉트함
      setSubmitting(form, true);
    });

    // 초기 강도 표시
    updateMeter('');
  });
})();
