document.addEventListener("DOMContentLoaded", function () {
	console.log("✅ customer_detail.js 로드됨");

  /* ---------------------------
     1️⃣ 아코디언 (접기/펼치기)
  --------------------------- */
  document.querySelectorAll('.section, .panel').forEach(section => {
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


  /* ---------------------------
     2️⃣ 행 클릭 → 서비스 상세 이동
  --------------------------- */
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


  /* ---------------------------
     3️⃣ 고객사 수정 토글 (이름/연결사)
  --------------------------- */
  const form = document.getElementById('customerUpdateForm');
  const btnEdit = document.getElementById('btnEditCustomer');
  if (form && btnEdit) {
    const editable = Array.from(form.querySelectorAll('input[name="customerName"], input[name="connectedCompany"]'));
    const setDisabled = (flag) => editable.forEach(el => el.disabled = flag);

    // 기본은 비활성화
    setDisabled(true);
    btnEdit.dataset.mode = 'view';

    btnEdit.addEventListener('click', () => {
      if (btnEdit.dataset.mode === 'view') {
        setDisabled(false);
        btnEdit.dataset.mode = 'edit';
        btnEdit.textContent = '저장';
      } else {
        form.submit();
      }
    });
  }


  /* ---------------------------
     4️⃣ 고객사 삭제 확인
  --------------------------- */
  const btnDelete = document.getElementById('btnDeleteCustomerInline');
  const formDelete = document.getElementById('formDeleteCustomerInline');
  if (btnDelete && formDelete) {
    btnDelete.addEventListener('click', (e) => {
      e.preventDefault();
      if (confirm('고객사와 연결된 서비스/담당자 정보가 모두 삭제됩니다. 진행할까요?')) {
        formDelete.submit();
      }
    });
  }


  /* ---------------------------
     5️⃣ 모달 열기 / 닫기
  --------------------------- */
  const openModal = (id) => {
    const m = document.getElementById(id);
    if (!m) return;
    m.classList.add('show');
    m.setAttribute('aria-hidden', 'false');
    const focusable = m.querySelector('input, select, textarea, button');
    focusable && focusable.focus();
  };

  const closeModal = (id) => {
    const m = document.getElementById(id);
    if (!m) return;
    m.classList.remove('show');
    m.setAttribute('aria-hidden', 'true');
  };

  // ✅ 버튼 클릭 → 모달 열기
  const btnOpen = document.getElementById('btnOpenSvcCreate');
  if (btnOpen) {
    btnOpen.addEventListener('click', (e) => {
      e.preventDefault();
      console.log("✅ 서비스 추가 버튼 클릭됨");
      openModal('svcCreateModal');
    });
  } else {
    console.warn("⚠️ btnOpenSvcCreate 버튼을 찾을 수 없습니다.");
  }

  // 닫기 버튼
  document.querySelectorAll('[data-close]').forEach(b => {
    b.addEventListener('click', () => closeModal(b.dataset.close));
  });

  // 배경 클릭 시 닫기
  document.querySelectorAll('.modal').forEach(m => {
    m.addEventListener('click', (e) => {
      if (e.target === m) closeModal(m.id);
    });
  });

  // ESC 키로 닫기
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      document.querySelectorAll('.modal.show').forEach(m => closeModal(m.id));
    }
  });

}); // DOMContentLoaded 끝
