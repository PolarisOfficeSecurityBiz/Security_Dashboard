(function () {
  const $modal = document.getElementById('createCustomerModal');
  const $open = document.getElementById('btnOpenCreateModal');
  const $close = document.getElementById('btnCloseCreateModal');
  const $cancel = document.getElementById('btnCancelCreate');

  function openModal() {
    if ($modal) {
      $modal.classList.remove('hidden');
      $modal.setAttribute('aria-hidden', 'false');
      // 첫 입력 포커스
      const first = document.getElementById('customerName');
      if (first) first.focus();
    }
  }
  function closeModal() {
    if ($modal) {
      $modal.classList.add('hidden');
      $modal.setAttribute('aria-hidden', 'true');
      if ($open) $open.focus();
    }
  }

  if ($open) $open.addEventListener('click', openModal);
  if ($close) $close.addEventListener('click', closeModal);
  if ($cancel) $cancel.addEventListener('click', closeModal);

  // ESC 닫기
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') closeModal();
  });

  // 오버레이 클릭 닫기 (모달 바깥 클릭 시)
  if ($modal) {
    $modal.addEventListener('click', (e) => {
      if (e.target === $modal) closeModal();
    });
  }
  
  // 행 클릭 -> 상세 이동
  (function () {
    const rows = document.querySelectorAll('tr.row-link[data-href]');
    rows.forEach(tr => {
      tr.style.cursor = 'pointer';
      tr.addEventListener('click', (e) => {
        // 텍스트 드래그 중 클릭 등 방지
        if (window.getSelection && String(window.getSelection())) return;
        const url = tr.getAttribute('data-href');
        if (url) window.location.href = url;
      });
      // 접근성: Enter로도 이동
      tr.tabIndex = 0;
      tr.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
          const url = tr.getAttribute('data-href');
          if (url) window.location.href = url;
        }
      });
    });
  })();

})();
