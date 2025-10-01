// /js/customers.js
(() => {
  // 디버그 플래그
  window.__customers_loaded = true;
  const LOG = (...a) => console.debug('[customers]', ...a);

  const MODAL_ID  = 'createCustomerModal';
  const OPEN_ID   = 'btnOpenCreateModal';
  const CLOSE_ID  = 'btnCloseCreateModal';
  const CANCEL_ID = 'btnCancelCreate';

  const qs  = (s, r = document) => r.querySelector(s);
  const qsa = (s, r = document) => Array.from(r.querySelectorAll(s));
  const get = (id) => document.getElementById(id);

  // 전역 폴백 (CSP로 inline 막혀도 외부에서 쓸 수 있게)
  function exposeGlobals(openFn, closeFn) {
    window.__openCreateCustomerModal  = openFn;
    window.__closeCreateCustomerModal = closeFn;
  }

  function bringToFront(m) {
    if (!m) return;
    // stacking context 회피: modal을 body로 이동
    if (m.parentNode !== document.body) {
      document.body.appendChild(m);
      LOG('moved modal to <body>');
    }
    // 최상단/클릭 가능 강제
    m.style.position = 'fixed';
    m.style.inset = '0';
    m.style.zIndex = '2147483000';
    m.style.pointerEvents = 'auto';
    qsa('*', m).forEach(el => {
      const cs = getComputedStyle(el);
      if (cs.pointerEvents === 'none') el.style.pointerEvents = 'auto';
    });
  }

  function openModal() {
    const m = get(MODAL_ID); if (!m) return;
    bringToFront(m);
    m.classList.remove('hidden');
    m.setAttribute('aria-hidden', 'false');
    document.body.classList.add('modal-open');
    get(OPEN_ID)?.setAttribute('aria-expanded', 'true');
    (qs('#customerName', m) || m.querySelector('input,select,textarea,button,[tabindex]') || m).focus();
  }

  function closeModal() {
    const m = get(MODAL_ID); if (!m) return;
    m.classList.add('hidden');
    m.setAttribute('aria-hidden', 'true');
    document.body.classList.remove('modal-open');
    const opener = get(OPEN_ID);
    opener?.setAttribute('aria-expanded', 'false');
    opener?.focus();
  }

  exposeGlobals(openModal, closeModal);

  // 버튼 직접 바인딩 + 강제 동작(캡처/버블 + pointerdown)
  function bindHard(el, handler) {
    if (!el) return;
    const h = (e) => { e && (e.preventDefault(), e.stopPropagation(), e.stopImmediatePropagation()); handler(); };
    el.addEventListener('pointerdown', h, { capture: true });
    el.addEventListener('pointerdown', h);
    el.addEventListener('click', h, { capture: true });
    el.addEventListener('click', h);
    el.onclick = h; // 일부 프레임워크 상호운용
    el.style.pointerEvents = 'auto';
  }

  function bindAll() {
    const modal     = get(MODAL_ID);
    const openBtn   = get(OPEN_ID);
    const closeBtn  = get(CLOSE_ID);
    const cancelBtn = get(CANCEL_ID);

    // 항상 최상단 보장
    modal && bringToFront(modal);

    // 열기
    if (openBtn) {
      const open = (e) => { e && e.preventDefault(); openModal(); };
      openBtn.addEventListener('click', open, { capture: true });
      openBtn.addEventListener('click', open);
      openBtn.onclick = open;
      openBtn.setAttribute('data-open', MODAL_ID);
      LOG('bound open');
    }

    // 닫기/취소
    bindHard(closeBtn, closeModal);
    bindHard(cancelBtn, closeModal);
    LOG('bound close/cancel');

    // 배경 클릭 닫기
    if (modal) {
      modal.addEventListener('click', (e) => {
        if (e.target === modal) {
          e.stopImmediatePropagation();
          closeModal();
        }
      }, { capture: true });
    }
  }

  // 전역 캡처 위임 — 버블에서 막혀도 동작
  document.addEventListener('click', (e) => {
    const openBtn = e.target.closest?.('[data-open]');
    if (openBtn && (openBtn.getAttribute('data-open') || MODAL_ID) === MODAL_ID) {
      e.preventDefault(); openModal(); return;
    }
    const closeBtn = e.target.closest?.('[data-close]');
    if (closeBtn && (closeBtn.getAttribute('data-close') || MODAL_ID) === MODAL_ID) {
      e.preventDefault(); closeModal(); return;
    }
  }, { capture: true });

  // ESC
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && qs('.modal:not(.hidden)')) closeModal();
  }, { capture: true });

  // 동적 교체(PJAX/Turbo/HTMX 등) 대응: 뷰가 바뀌면 자동 재바인딩
  const rebinder = () => {
    try { bindAll(); } catch (e) { console.error(e); }
  };
  const mo = new MutationObserver((muts) => {
    for (const m of muts) {
      if (m.type === 'childList') {
        if (m.addedNodes.length || m.removedNodes.length) {
          // 핵심 요소들 중 하나라도 없어졌거나 새로 생겼으면 재바인딩
          if (!get(OPEN_ID) || !get(CLOSE_ID) || !get(CANCEL_ID) || !get(MODAL_ID)) {
            setTimeout(rebinder, 0);
          } else {
            // 존재해도 속성/포지션이 바뀌는 경우 대비
            setTimeout(rebinder, 0);
          }
          break;
        }
      }
    }
  });
  mo.observe(document.documentElement, { childList: true, subtree: true });

  function init() {
    rebinder();
    LOG('initialized');
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init, { once: true });
  } else {
    init();
  }
})();
