// /static/js/company.js
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

    /* ------------------------------------------------------------------
     * 페이지 컨텍스트
     * ------------------------------------------------------------------ */
    const page = $('#companyPage');
    if (!page) {
      log('WARN: #companyPage not found. Stop init.');
      return;
    }
    const cid = page.dataset.customerId || '';
    if (!cid) log('WARN: data-customer-id is empty');

    /* ------------------------------------------------------------------
     * 공용 유틸: 토스트 + JSON POST
     * ------------------------------------------------------------------ */
    function toast(msg, type='info') {
      const el = document.createElement('div');
      el.className = 'toast';
      el.textContent = msg;
      Object.assign(el.style, {
        position: 'fixed', left: '50%', bottom: '24px', transform: 'translateX(-50%)',
        background: type==='success' ? '#16a34a'
                : type==='error'   ? '#ef4444'
                : type==='warn'    ? '#f59e0b'
                : '#334155',
        color: '#fff', padding: '10px 14px', borderRadius: '10px',
        fontWeight: 600, zIndex: 2000, boxShadow: '0 10px 30px rgba(0,0,0,.18)'
      });
      document.body.appendChild(el);
      setTimeout(()=>{ el.style.opacity='0'; el.style.transition='opacity .2s'; }, 2200);
      setTimeout(()=> el.remove(), 2600);
    }

    async function jsonPost(url, body) {
      const token  = document.querySelector('meta[name="_csrf"]')?.content;
      const header = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';

      const res = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { [header]: token } : {})
        },
        body: JSON.stringify(body),
        credentials: 'same-origin'
      });

      if (!res.ok) {
        const ct = res.headers.get('content-type') || '';
        let detail = '';
        try {
          if (ct.includes('application/json')) {
            const j = await res.json();
            detail = j?.message || JSON.stringify(j);
          } else {
            detail = await res.text();
          }
        } catch (_) {}
        throw new Error(`[${res.status}] ${detail || '요청 실패'}`);
      }

      return res.headers.get('content-type')?.includes('json') ? res.json() : null;
    }

    /* ------------------------------------------------------------------
     * 모달: 수정요청(회사/서비스 공용)
     * ------------------------------------------------------------------ */
    const modal        = $('#editRequestModal');
    const ta           = $('#editRequestText');
    const btnSubmitReq = $('#editRequestSubmit');
    const btnCloseEls  = $$('[data-close]');

    // 요청 컨텍스트 { type: 'COMPANY'|'SERVICE', targetId: string|number, title?: string }
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
      setTimeout(()=> ta?.focus(), 50);
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

    // 회사 “수정요청” 버튼 → 모달
    $('#requestCompanyEdit')?.addEventListener('click', (e) => {
      e.preventDefault();
      openModal({ type: 'COMPANY', targetId: cid, title: '회사 정보 수정요청' });
    });

    // 서비스 “수정요청” 버튼 → 모달 (위임)
    document.addEventListener('click', (e) => {
      const btn = e.target.closest('.requestServiceEdit');
      if (!btn) return;
      e.preventDefault();
      const form  = btn.closest('.service-form');
      const sid   = form?.getAttribute('data-sid');
      const name  = form?.querySelector('input[name="serviceName"]')?.value ?? '';
      openModal({ type: 'SERVICE', targetId: sid, title: `서비스 수정요청${name ? ' - ' + name : ''}` });
    });

    // 요청 제출
    btnSubmitReq?.addEventListener('click', async () => {
      const content = (ta?.value ?? '').trim();

      if (!currentContext) { toast('요청 컨텍스트가 없습니다.', 'error'); return; }
      if (!cid)           { toast('고객사 식별값이 없습니다.', 'error'); return; }
      if (!content)       { toast('요청 내용을 입력해 주세요.', 'warn'); ta?.focus(); return; }

      try {
        if (currentContext.type === 'COMPANY') {
          await jsonPost('/customer/edit-requests/company', {
            customerId: cid,
            content
          });
        } else if (currentContext.type === 'SERVICE') {
          if (!currentContext.targetId) {
            toast('서비스 식별값이 없습니다.', 'error');
            return;
          }
          await jsonPost('/customer/edit-requests/service', {
            customerId: cid,
            serviceId : Number(currentContext.targetId),
            content
          });
        } else {
          toast('알 수 없는 요청 유형입니다.', 'error');
          return;
        }

        toast('요청이 접수되었습니다. 관리자 검토 후 반영됩니다.', 'success');
        closeModal();
      } catch (err) {
        console.error(err);
        toast(`요청 제출 중 오류: ${err.message}`, 'error');
      }
    });

    /* ------------------------------------------------------------------
     * (옵션) 인라인 편집 토글 유틸 — 필요시 활성화
     *  지금은 ‘수정요청 → 모달 제출’ UX이므로 기본 Off.
     * ------------------------------------------------------------------ */
    function toggleEdit(container, on) {
      const form = container ? container.querySelector('form') : null;
      const editables = form ? form.querySelectorAll('[data-editable]') : [];
      if (on) editables.forEach(el => el.dataset.orig = el.value ?? '');
      editables.forEach(el => {
        if (on) { el.removeAttribute('readonly'); el.style.backgroundColor = '#fff'; }
        else    { el.setAttribute('readonly', 'readonly'); el.style.backgroundColor = ''; }
      });
      const save   = form?.querySelector('.save-btn');
      const cancel = form?.querySelector('.cancel-btn');
      if (save)   save.hidden   = !on;
      if (cancel) cancel.hidden = !on;
      container?.classList.toggle('editing', !!on);
    }
    function restore(container) {
      const form = container?.querySelector('form');
      form?.querySelectorAll('[data-editable]').forEach(el => el.value = el.dataset.orig ?? el.value);
      toggleEdit(container, false);
    }

    // (원한다면) 아래 이벤트를 켜서 인라인 수정 UX도 같이 사용 가능
    // $('#requestCompanyEdit')?.addEventListener('dblclick', (e) => {
    //   e.preventDefault();
    //   const block = document.getElementById('companyCard');
    //   toggleEdit(block, true);
    // });
    // document.addEventListener('click', (e) => {
    //   const btn = e.target.closest('.service-form .requestServiceEdit');
    //   if (btn) {
    //     e.preventDefault();
    //     const block = btn.closest('.service-block');
    //     toggleEdit(block, true);
    //   }
    // });
    // document.addEventListener('click', (e) => {
    //   const btn = e.target.closest('.cancel-btn');
    //   if (btn) {
    //     e.preventDefault();
    //     const block = btn.closest('.service-block') || document.getElementById('companyCard');
    //     restore(block);
    //   }
    // });

    log('ready. cid=', cid);
  }
})();
