(() => {
  const API = '/api/v1/polar-notices';  // API 경로 설정

  // HTML 요소들
  const $q = document.getElementById('q');
  const $btn = document.getElementById('refreshBtn');
  const $tbody = document.getElementById('letter-tbody');
  const $count = document.getElementById('countText');
  const $empty = document.getElementById('empty');
  const $error = document.getElementById('error');
  const $loading = document.getElementById('loading');

  // 공백 제거 후 검색어 포맷팅
  const esc = s => (s ?? '').replace(/[&<>"']/g, m => (
    { '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;' }[m]
  ));

  // 카테고리명을 한글로 변환하는 함수
  const convertCategory = category => {
    switch (category) {
      case 'EMERGENCY':
        return '보안';
      case 'EVENT':
        return '이벤트';
      case 'SERVICE_GUIDE':
        return '안내';
      case 'UPDATE':
        return '업데이트';
      default:
        return category;
    }
  };

  // 카테고리에 맞는 스타일 클래스 반환
  const getCategoryClass = category => {
    switch (category) {
      case 'EMERGENCY':
        return 'category--emergency';
      case 'EVENT':
        return 'category--event';
      case 'SERVICE_GUIDE':
        return 'category--service-guide';
      case 'UPDATE':
        return 'category--update';
      default:
        return '';
    }
  };

  // API에서 공지사항을 가져오는 함수
  async function fetchNotices() {
    const params = new URLSearchParams();
    const q = $q.value.trim();
    if (q) params.set('q', q);

    const url = `${API}?${params.toString()}`;  // API 요청 URL
    const res = await fetch(url, { headers: { 'Accept': 'application/json' } });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return res.json();  // 받은 데이터를 JSON으로 파싱
  }

  // 테이블에 데이터를 렌더링하는 함수
  function render(rows) {
    $tbody.innerHTML = '';  // 기존 테이블 내용 초기화
    if (!rows || rows.length === 0) {
      $empty.hidden = false;
      $error.hidden = true;
      $count.textContent = '0건';
      return;
    }
    $empty.hidden = true;
    $error.hidden = true;

    const frag = document.createDocumentFragment();  // 테이블 행들을 담을 fragment 생성

    rows.forEach(n => {
      const tr = document.createElement('tr');  // 새로운 테이블 행 생성

      // 카테고리 변환
      const categoryLabel = convertCategory(n.category);

      tr.innerHTML = `
        <td><span class="row-muted" title="${esc(n.id)}">${esc(n.id)}</span></td>
        <td>
          ${n.imageURL ? `<img class="letter-thumb" src="${esc(n.imageURL)}" alt="">`
                        : `<div class="letter-thumb"></div>`}
        </td>
        <td>
          <div>${esc(n.title || '')}</div>
          ${n.content ? `<div class="row-muted">${esc(n.content.slice(0, 80))}${n.content.length > 80 ? '…' : ''}</div>` : ''}
        </td>
        <td>${esc(n.author || '-')}</td>
        <td>${esc(n.date || '-')}</td>
        <td class="category">${categoryLabel}</td>  <!-- 카테고리 텍스트로 변환 -->
      `;
      frag.appendChild(tr);  // 생성한 행을 fragment에 추가
    });

    $tbody.appendChild(frag);  // fragment를 tbody에 추가
    $count.textContent = `${rows.length}건`;  // 결과 건수 표시
  }

  // 공지사항을 불러오는 함수
  async function load() {
    $loading.hidden = false;
    $empty.hidden = true;
    $error.hidden = true;

    try {
      const rows = await fetchNotices();  // API 호출
      render(rows);  // 데이터를 테이블에 렌더링
    } catch (e) {
      console.error('[polar-notices] fetch error:', e);  // 에러 출력
      $error.hidden = false;  // 오류 메시지 표시
    } finally {
      $loading.hidden = true;  // 로딩 메시지 숨김
    }
  }

  // 새로고침 버튼 클릭 시 데이터 로드
  $btn.addEventListener('click', load);

  // 검색창에서 Enter 키를 눌렀을 때 데이터 로드
  $q.addEventListener('keydown', e => { if (e.key === 'Enter') load(); });

  // 페이지 로드 시 자동으로 데이터 로드
  document.addEventListener('DOMContentLoaded', load);
})();
