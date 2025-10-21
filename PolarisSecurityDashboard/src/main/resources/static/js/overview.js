// static/js/overview.js
(function initMiniBar() {
  const el = document.getElementById('by-day-canvas');
  if (!el) return;

  // ✅ data-points: "1,2,3" 또는 "[1,2,3]" 모두 허용
  function parsePoints(attr) {
    if (!attr || !attr.trim()) return [0, 0, 0, 0, 0, 0, 0];
    const s = attr.trim();
    try {
      if (s.startsWith('[')) {
        const arr = JSON.parse(s);
        return Array.isArray(arr) ? arr.map(n => Number(n) || 0) : [0,0,0,0,0,0,0];
      }
    } catch (_) { /* ignore */ }
    return s.split(',').map(x => Number(x) || 0);
  }

  const raw = parsePoints(el.dataset.points);
  const colorStart = el.dataset.colorStart || '#93c5fd';
  const colorEnd   = el.dataset.colorEnd   || '#2563eb';
  const bgColor    = el.dataset.bg         || '#eff3fb';

  const ctx = el.getContext && el.getContext('2d');
  if (!ctx) return;

  let ro = null;
  let rafId = null;
  let destroyed = false;

  function getSize() {
    // 실제 렌더 박스 기준으로 계산 (레이아웃 영향 최소화)
    const rect = el.getBoundingClientRect();
    // rect가 0일 때(숨김/탭 미노출 등) 기본값 보정
    const W = Math.max(120, Math.floor(rect.width) || el.clientWidth || 300);
    const H = Math.max(60,  Math.floor(rect.height) || el.clientHeight || 100);
    return { W, H };
  }

  function draw() {
    if (destroyed) return;

    const { W, H } = getSize();
    const dpr = window.devicePixelRatio || 1;

    // 캔버스 스케일
    el.width  = Math.round(W * dpr);
    el.height = Math.round(H * dpr);
    ctx.setTransform(1, 0, 0, 1, 0, 0);
    ctx.scale(dpr, dpr);

    // 배경
    ctx.clearRect(0, 0, W, H);
    ctx.fillStyle = bgColor;
    ctx.fillRect(0, 0, W, H);

    // 축
    ctx.strokeStyle = '#dbe5f3';
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(0, H - 20);
    ctx.lineTo(W, H - 20);
    ctx.stroke();

    const explicitMax = Number(el.dataset.max);
    const max = Number.isFinite(explicitMax) && explicitMax > 0 ? explicitMax : Math.max(1, ...raw);
    const n = raw.length || 1;

    // 간격/막대폭
    const gap = Math.max(6, Math.floor(W / (n * 8)));
    const barW = Math.max(4, (W - gap * (n + 1)) / n);

    for (let i = 0; i < n; i++) {
      const v = raw[i] || 0;
      // 전부 0일 때도 1px 표시
      const h = Math.max(1, Math.round((H - 36) * (v / max)));
      const x = gap + i * (barW + gap);
      const y = H - 20 - h;

      const grd = ctx.createLinearGradient(0, y, 0, y + h);
      grd.addColorStop(0, colorStart);
      grd.addColorStop(1, colorEnd);
      ctx.fillStyle = grd;

      const r = Math.min(6, barW / 2);
      ctx.beginPath();
      ctx.moveTo(x, y + r);
      ctx.arcTo(x, y, x + r, y, r);
      ctx.lineTo(x + barW - r, y);
      ctx.arcTo(x + barW, y, x + barW, y + r, r);
      ctx.lineTo(x + barW, y + h);
      ctx.lineTo(x, y + h);
      ctx.closePath();
      ctx.fill();
    }

    // max 텍스트
    ctx.fillStyle = '#64748b';
    ctx.font = '12px ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto';
    ctx.fillText('max ' + max, 8, 14);
  }

  // 최초 1회
  draw();

  // 반응형
  if ('ResizeObserver' in window) {
    ro = new ResizeObserver(() => {
      if (rafId) cancelAnimationFrame(rafId);
      rafId = requestAnimationFrame(draw);
    });
    ro.observe(el);
  } else {
    // 폴백
    window.addEventListener('resize', () => {
      if (rafId) cancelAnimationFrame(rafId);
      rafId = requestAnimationFrame(draw);
    });
  }

  // 안전 해제
  const cleanup = () => {
    destroyed = true;
    try { ro && ro.disconnect(); } catch (_) {}
  };
  window.addEventListener('beforeunload', cleanup);
  // SPA가 아니면 없어도 됨: el이 DOM에서 제거되면 정리
  const mo = new MutationObserver(() => {
    if (!document.body.contains(el)) {
      cleanup();
      try { mo.disconnect(); } catch (_) {}
    }
  });
  mo.observe(document.body, { childList: true, subtree: true });
})();
