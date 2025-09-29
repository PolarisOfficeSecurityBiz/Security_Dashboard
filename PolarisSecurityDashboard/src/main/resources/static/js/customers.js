(function () {
  // ===== Modal =====
  const modal = document.getElementById('createCustomerModal');
  const btnOpen = document.getElementById('btnOpenCreateModal');
  const btnClose = document.getElementById('btnCloseCreateModal');
  const btnCancel = document.getElementById('btnCancelCreate');

  function openModal() {
    if (!modal) return;
    modal.classList.remove('hidden');
    modal.setAttribute('aria-hidden', 'false');
    document.getElementById('customerName')?.focus();
    document.body.style.overflow = 'hidden';
  }
  function closeModal() {
    if (!modal) return;
    modal.classList.add('hidden');
    modal.setAttribute('aria-hidden', 'true');
    btnOpen?.focus();
    document.body.style.overflow = '';
  }
  btnOpen?.addEventListener('click', openModal);
  btnClose?.addEventListener('click', closeModal);
  btnCancel?.addEventListener('click', closeModal);
  modal?.addEventListener('click', (e) => { if (e.target === modal) closeModal(); });
  document.addEventListener('keydown', (e) => { if (e.key === 'Escape') closeModal(); });

  // ===== Row click / keyboard : 이벤트 위임 =====
  const tbody = document.querySelector('.page-customers #customersTable tbody');

  function getHref(tr){
    return tr?.dataset?.href || tr?.getAttribute('data-href') || '';
  }
  function go(tr) {
    const url = getHref(tr);
    if (url) {
      window.location.assign(url);
    }
  }

  // 클릭
  tbody?.addEventListener('click', (e) => {
    const tr = e.target.closest('tr.row-link');
    if (!tr) return;
    go(tr);
  });

  // Enter 키
  tbody?.addEventListener('keydown', (e) => {
    if (e.key !== 'Enter') return;
    const tr = e.target.closest('tr.row-link') ||
               document.activeElement.closest('tr.row-link');
    go(tr);
  });

  // ===== Search filter =====
  const $search = document.getElementById('searchInput');
  const $table = document.getElementById('customersTable');
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
  $search?.addEventListener('input', filterRows);

  // ===== Sort dropdown =====
  const sortBtn = document.getElementById('sortBtn');
  const sortMenu = document.getElementById('sortMenu');
  const sortLabel = document.getElementById('sortLabel');

  function toggleMenu() {
    if (!sortMenu) return;
    sortMenu.classList.toggle('hidden');
    const expanded = sortBtn.getAttribute('aria-expanded') === 'true';
    sortBtn.setAttribute('aria-expanded', String(!expanded));
  }
  function closeMenu() {
    if (!sortMenu) return;
    sortMenu.classList.add('hidden');
    sortBtn.setAttribute('aria-expanded', 'false');
  }
  sortBtn?.addEventListener('click', (e)=>{ e.stopPropagation(); toggleMenu(); });
  document.addEventListener('click', (e)=>{
    if (e.target.closest('.page-customers .dropdown')) return;
    closeMenu();
  });

  function sortRows(mode){
    if (!$table) return;
    const tbody = $table.tBodies[0];
    const rows = Array.from(tbody.querySelectorAll('tr.row-link'));
    let accessor = ()=>'';
    if (mode === 'name') accessor = tr => (tr.cells[1]?.innerText || '').toLowerCase();
    if (mode === 'company') accessor = tr => (tr.cells[2]?.innerText || '').toLowerCase();
    if (mode === 'new') return; // 서버 최신순 유지

    rows.sort((a,b)=> accessor(a).localeCompare(accessor(b), 'ko'));
    rows.forEach(r=>tbody.appendChild(r));
  }

  sortMenu?.querySelectorAll('li[data-sort]').forEach(li=>{
    li.addEventListener('click', ()=>{
      const mode = li.getAttribute('data-sort');
      if (sortLabel) sortLabel.textContent = li.textContent.trim();
      sortRows(mode);
      closeMenu();
    });
    li.addEventListener('keydown', (e)=>{
      if (e.key === 'Enter' || e.key === ' ') {
        e.preventDefault();
        li.click();
      }
    });
  });
})();
