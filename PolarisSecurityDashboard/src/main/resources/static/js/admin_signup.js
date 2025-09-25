(function () {
  'use strict';

  /* ======== 유틸 ======== */
  const qs  = (sel, root = document) => root.querySelector(sel);
  const qsa = (sel, root = document) => Array.prototype.slice.call(root.querySelectorAll(sel));
  const isValidEmail = (v) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v);

  /* ======== 비번 토글 ======== */
  window.togglePassword = function (btnEl) {
    try {
      const btn = btnEl || qs('.pw-toggle');
      if (!btn) return;
      const input = btn.closest('.password-wrap')?.querySelector('input');
      if (!input) return;
      const nextType = input.type === 'password' ? 'text' : 'password';
      input.type = nextType;
      btn.setAttribute('aria-label', nextType === 'password' ? 'Show password' : 'Hide password');
    } catch (_) {}
  };

  /* ======== 에러 박스 ======== */
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

  const clearError = function (form) {
    const box = qs('.alert.dynamic', form);
    if (box) box.remove();
  };

  /* ======== 제출 상태 ======== */
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

  /* ======== 비번 강도 ======== */
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
    const perc = (sc / 5) * 100;
    if (bar) {
      bar.style.width = perc + '%';
      bar.style.height = '6px';
      bar.style.borderRadius = '6px';
      bar.style.transition = 'width .15s ease';
      bar.style.background = sc <= 2 ? '#ef4444' : sc <= 3 ? '#f59e0b' : '#10b981';
    }
    if (hint) {
      hint.textContent = '안전도: ' + (sc <= 2 ? '약함' : sc <= 3 ? '보통' : '강함');
    }
  };

  /* ======== 초기화 ======== */
  document.addEventListener('DOMContentLoaded', function () {
    const form = qs('#admin-signup-form');
    if (!form) return;

    // ✅ 서버 안전 가드: action을 반드시 /admin/signup 으로 고정
    try {
      const url = new URL(location.href);
      const base = url.origin; // http(s)://host[:port]
      form.setAttribute('action', '/admin/signup'); // 앱 컨텍스트 루트 기준
      // 필요 시 절대 URL로 강제하려면 아래 사용:
      // form.setAttribute('action', base + '/admin/signup');
    } catch (_) {
      form.setAttribute('action', '/admin/signup');
    }

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
    updateMeter(''); // 초기 표시

    // BFCache 복귀 시 버튼 잠김 해제
    window.addEventListener('pageshow', function (e) {
      if (e.persisted) setSubmitting(form, false);
    });

    // 제출
    form.addEventListener('submit', function (e) {
      clearError(form);

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

      // 제출 진행
      setSubmitting(form, true);
      // 서버 컨트롤러가 성공 시 반드시 redirect:/login 하도록 구성해야 최종 이동이 /login
      // 실패 시 RedirectAttributes로 error flash → 다시 /admin/signup 로 리다이렉트
    });
  });
})();
