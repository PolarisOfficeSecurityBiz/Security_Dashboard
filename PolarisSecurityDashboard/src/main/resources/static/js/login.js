(function () {
  'use strict';

  /* ======================== 공통 유틸 ======================== */
  window.togglePassword = function (btnEl) {
    try {
      const btn = btnEl || document.querySelector('.pane:not([hidden]) .pw-toggle') || document.querySelector('.pw-toggle');
      if (!btn) return;
      const input = btn.previousElementSibling && btn.previousElementSibling.tagName === 'INPUT'
        ? btn.previousElementSibling
        : btn.closest('.password-wrap')?.querySelector('input');
      if (!input) return;
      const nextType = input.type === 'password' ? 'text' : 'password';
      input.type = nextType;
      btn.setAttribute('aria-label', nextType === 'password' ? 'Show password' : 'Hide password');
    } catch (_) {}
  };

  const qs  = (sel, root = document) => root.querySelector(sel);
  const qsa = (sel, root = document) => Array.prototype.slice.call(root.querySelectorAll(sel));
  const isValidEmail = (v) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v);

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
    const submitBtn = qs('button[type="submit"]', form);
    if (!submitBtn) return;
    if (on) {
      submitBtn.disabled = true;
      if (!submitBtn.dataset.originalText) submitBtn.dataset.originalText = submitBtn.textContent || '';
      submitBtn.textContent = 'Signing in…';
    } else {
      submitBtn.disabled = false;
      if (submitBtn.dataset.originalText != null) {
        submitBtn.textContent = submitBtn.dataset.originalText;
        delete submitBtn.dataset.originalText;
      }
    }
  };

  const attachCapsHint = function (pwEl) {
    if (!pwEl || pwEl.dataset.capsBound) return;
    const hint = document.createElement('small');
    hint.textContent = 'Caps Lock이 켜져 있습니다.';
    hint.className = 'caps-hint';
    hint.style.display = 'none';
    hint.style.color = '#b91c1c';
    hint.style.marginTop = '6px';
    pwEl.parentNode.appendChild(hint);
    const capsHandler = (ev) => {
      const on = ev.getModifierState && ev.getModifierState('CapsLock');
      hint.style.display = on ? 'block' : 'none';
    };
    pwEl.addEventListener('keyup', capsHandler);
    pwEl.addEventListener('keydown', capsHandler);
    pwEl.dataset.capsBound = '1';
  };

  const storageKeyFor = (form) =>
    form.id === 'form-admin' ? 'ps_last_email_admin' : 'ps_last_email_customer';

  /* 폼 초기화(토글 시 호출) */
  const resetForm = (form) => {
    if (!form) return;
    // 기본값으로 초기화
    form.reset();
    // value/상태 강제 초기화(혹시 value 속성이 있었을 경우 대비)
    qsa('input, textarea', form).forEach((el) => {
      if (el.type === 'checkbox' || el.type === 'radio') {
        el.checked = !!el.defaultChecked;
      } else {
        el.value = el.defaultValue || '';
      }
      // 비밀번호 input은 항상 password 타입으로 복원
      if (el.name === 'password' || el.type === 'password' || el.type === 'text') {
        if (el.closest('.password-wrap')) el.type = 'password';
      }
    });
    // 동적 에러/로딩 상태 제거
    const dyn = qs('.alert.dynamic', form);
    if (dyn) dyn.remove();
    setSubmitting(form, false);
    // CapsLock 힌트 숨김
    const hint = qs('.caps-hint', form);
    if (hint) hint.style.display = 'none';
  };

  const bindForm = function (form) {
    if (!form || form.dataset.bound) return;

    const emailEl    = qs('input[type="email"], input[name="username"]', form);
    const pwEl       = qs('input[type="password"][name="password"], .password-wrap input[type="password"], .password-wrap input[type="text"]', form);
    const rememberEl = qs('input[name="remember-me"]', form);
    const pwToggleBtn= qs('.pw-toggle', form);

    if (pwToggleBtn && !pwToggleBtn.getAttribute('onclick')) {
      pwToggleBtn.addEventListener('click', function (e) {
        e.preventDefault();
        window.togglePassword(pwToggleBtn);
      });
    }

    if (pwEl) attachCapsHint(pwEl);

    // 저장된 이메일은 "초기 진입"시에만 복원(탭 토글 시엔 reset 유지)
    try {
      const saved = localStorage.getItem(storageKeyFor(form));
      if (saved && emailEl && !emailEl.value) {
        emailEl.value = saved;
        if (rememberEl) rememberEl.checked = true;
      }
    } catch (_) {}

    form.addEventListener('submit', function (event) {
      const email = (emailEl && emailEl.value ? emailEl.value : '').trim();
      const password = (pwEl && pwEl.value ? pwEl.value : '');

      if (!email) {
        event.preventDefault();
        showError(form, '이메일을 입력해 주세요.');
        emailEl && emailEl.focus();
        return;
      }
      if (!isValidEmail(email)) {
        event.preventDefault();
        showError(form, '이메일 형식을 확인해 주세요.');
        emailEl && emailEl.focus();
        return;
      }
      if (!password) {
        event.preventDefault();
        showError(form, '비밀번호를 입력해 주세요.');
        pwEl && pwEl.focus();
        return;
      }

      try {
        if (rememberEl && rememberEl.checked) {
          localStorage.setItem(storageKeyFor(form), email);
        } else {
          localStorage.removeItem(storageKeyFor(form));
        }
      } catch (_) {}

      setSubmitting(form, true);
    });

    [emailEl, pwEl].forEach(function (el) {
      el && el.addEventListener('keydown', function (e) {
        if (e.key === 'Enter') setTimeout(function () { setSubmitting(form, true); }, 0);
      });
    });

    form.dataset.bound = '1';
  };

  /* --- 폴백: :has() 미지원 브라우저에서 폼/링크 토글 --- */
  const fallbackToggleIfNeeded = () => {
    const supportsHas = CSS && CSS.supports && CSS.supports('selector(:has(*))');
    const cust   = document.getElementById('seg-cust');
    const admin  = document.getElementById('seg-admin');
    const paneC  = document.querySelector('.pane-cust');
    const paneA  = document.querySelector('.pane-admin');
    const signupOutside = document.querySelector('.only-admin'); // (옵션)

    const sync = () => {
      const custOn = !!cust?.checked;
      if (!supportsHas) { // CSS 토글이 안 되는 브라우저만 display 제어
        if (paneC) paneC.style.display = custOn ? 'block' : 'none';
        if (paneA) paneA.style.display = custOn ? 'none' : 'block';
        if (signupOutside) signupOutside.style.display = custOn ? 'none' : 'block';
      }
    };

    // ✅ 탭 전환 시 값 초기화
    const resetOnToggle = () => {
      resetForm(document.getElementById('form-cust'));
      resetForm(document.getElementById('form-admin'));
      sync();
      focusActivePane();
    };

    cust?.addEventListener('change', resetOnToggle);
    admin?.addEventListener('change', resetOnToggle);

    // 초기 상태 반영
    sync();
  };

  const focusActivePane = function () {
    const activePane =
      qs('#seg-admin:checked ~ .panes .pane-admin, #seg-cust:checked ~ .panes .pane-cust') ||
      qs('.pane:not([hidden])') ||
      qs('.pane');
    const first = activePane && qs('input[type="email"], input[name="username"]', activePane);
    if (first && !first.value) first.focus();
  };

  document.addEventListener('DOMContentLoaded', function () {
    // 폼 바인딩
    qsa('#form-cust, #form-admin').forEach(bindForm);

    // 폴백 및 토글 초기화/포커스
    fallbackToggleIfNeeded();
    focusActivePane();
  });
})();
