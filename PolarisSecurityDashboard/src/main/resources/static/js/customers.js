(function () {
  const $modal  = document.getElementById('createCustomerModal');
  const $open   = document.getElementById('btnOpenCreateModal');
  const $close  = document.getElementById('btnCloseCreateModal');
  const $cancel = document.getElementById('btnCancelCreate');

  function openModal() {
    if (!$modal) return;
    $modal.classList.remove('hidden');
    $modal.setAttribute('aria-hidden', 'false');
    const first = document.getElementById('customerName');
    if (first) first.focus();
  }
  function closeModal() {
    if (!$modal) return;
    $modal.classList.add('hidden');
    $modal.setAttribute('aria-hidden', 'true');
    if ($open) $open.focus();
  }
  if ($open)  $open.addEventListener('click', openModal);
  if ($close) $close.addEventListener('click', closeModal);
  if ($cancel)$cancel.addEventListener('click', closeModal);
  document.addEventListener('keydown', (e)=>{ if (e.key === 'Escape') closeModal(); });
  if ($modal) $modal.addEventListener('click', (e)=>{ if (e.target === $modal) closeModal(); });

  // ---- 행 클릭 → 상세 이동 + 키보드 접근성
  (function () {
    const rows = document.querySelectorAll('tr.row-link[data-href]');
    rows.forEach(tr => {
      tr.style.cursor = 'pointer';
      tr.addEventListener('click', () => {
        if (window.getSelection && String(window.getSelection())) return;
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
  })();

  // ---- 검색 필터
  const $search = document.getElementById('searchInput');
  const $table  = document.getElementById('customersTable');
  function normalize(s){ return (s||'').toString().toLowerCase().trim(); }
  function filterRows() {
    if (!$table) return;
    const q = normalize($search && $search.value);
    const rows = $table.tBodies[0]?.rows || [];
    Array.from(rows).forEach(tr => {
      if (tr.classList.contains('no-data')) return;
      const text = normalize(tr.innerText);
      tr.style.display = text.includes(q) ? '' : 'none';
    });
  }
  if ($search) $search.addEventListener('input', filterRows);

  // ---- 정렬 드롭다운
  const $sortBtn   = document.getElementById('sortBtn');
  const $sortMenu  = document.getElementById('sortMenu');
  const $sortLabel = document.getElementById('sortLabel');

  function toggleSortMenu(){ if($sortMenu) $sortMenu.classList.toggle('hidden'); }
  function closeSortMenu(){ if($sortMenu) $sortMenu.classList.add('hidden'); }

  if ($sortBtn) $sortBtn.addEventListener('click', (e)=>{ e.stopPropagation(); toggleSortMenu(); });
  document.addEventListener('click', closeSortMenu);

  function sortRows(mode){
    if (!$table) return;
    const tbody = $table.tBodies[0];
    const rows = Array.from(tbody.querySelectorAll('tr.row-link'));

    // 현재 컬럼 순서: [0]=고객사ID, [1]=고객사명, [2]=연결사, [3]=생성일
    let accessor = ()=>'';
    if (mode === 'name')    accessor = tr => tr.cells[1].innerText.toLowerCase(); // 고객사명
    if (mode === 'company') accessor = tr => tr.cells[2].innerText.toLowerCase(); // 연결사
    if (mode === 'new')     { /* 서버 최신순이면 건드리지 않음 */ return; }

    rows.sort((a,b)=> accessor(a).localeCompare(accessor(b), 'ko'));
    rows.forEach(r=>tbody.appendChild(r));
  }

  if ($sortMenu){
    $sortMenu.querySelectorAll('li[data-sort]').forEach(li=>{
      li.addEventListener('click', ()=>{
        const mode = li.getAttribute('data-sort');
        if ($sortLabel) $sortLabel.textContent = li.textContent.trim();
        sortRows(mode);
        closeSortMenu();
      });
    });
  }
})();
