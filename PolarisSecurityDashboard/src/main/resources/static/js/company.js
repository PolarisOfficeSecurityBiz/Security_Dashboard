(function () {
  'use strict';

  const DEBUG = true;
  const log = (...a) => DEBUG && console.log('[company]', ...a);

  // DOM Ready
  (function ready(fn){
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', fn, { once: true });
    } else { fn(); }
  })(init);

  function init() {
    const $  = (s, r=document) => r.querySelector(s);
    const $$ = (s, r=document) => Array.from(r.querySelectorAll(s));

    /* ------------------------------------------------------------------ */
    /* 모달: 수정요청(회사/서비스 공용) */
    const modal = $('#editRequestModal');
    const ta = $('#editRequestText');
    const btnSubmitReq = $('#editRequestSubmit');
    const btnCloseEls = $$('[data-close]');

    let currentContext = null;

    const openModal = (ctx) => {
      currentContext = ctx;
      if (ta) ta.value = '';
      if (ctx?.title) {
        const h = modal?.querySelector('h3');
        if (h) h.textContent = ctx.title;
      }
      modal?.classList.add('show');
      modal?.setAttribute('aria-hidden', 'false');
      document.body.style.overflow = 'hidden';
      setTimeout(() => ta?.focus(), 50);
      log('modal open:', ctx);
    };

    const closeModal = () => {
      modal?.classList.remove('show');
      modal?.setAttribute('aria-hidden','true');
      document.body.style.overflow = '';
      currentContext = null;
      log('modal close');
    };

    btnCloseEls.forEach(b => b.addEventListener('click', closeModal));
    modal?.addEventListener('click', (e) => { if (e.target === modal) closeModal(); });
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && modal?.classList.contains('show')) closeModal();
    });

    // 요청 제출
    btnSubmitReq?.addEventListener('click', async () => {
      const content = (ta?.value ?? '').trim();

      if (!currentContext) { toast('요청 컨텍스트가 없습니다.', 'error'); return; }
      if (!content) { toast('요청 내용을 입력해 주세요.', 'warn'); ta?.focus(); return; }

      try {
        // 예시로 회사 정보 요청 처리
        if (currentContext.type === 'COMPANY') {
          await jsonPost('/customer/edit-requests/company', {
            customerId: currentContext.targetId,
            content
          });
        }

        toast('요청이 접수되었습니다. 관리자 검토 후 반영됩니다.', 'success');
        closeModal();
      } catch (err) {
        console.error(err);
        toast(`요청 제출 중 오류: ${err.message}`, 'error');
      }
    });

    // 공용 토스트 메세지
    function toast(msg, type='info') {
      const el = document.createElement('div');
      el.className = 'toast';
      el.textContent = msg;
      document.body.appendChild(el);
      setTimeout(()=> el.remove(), 2600);
    }

    log('ready.');
  }
})();
