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
        return Array.isArray(arr) ? arr.map(n => +n || 0) : [0,0,0,0,0,0,0];
      }
    } catch (_) { /* fallthrough */ }
    // CSV
    return s.split(',').map(x => +x || 0);
  }

  const raw = parsePoints(el.dataset.points);
  const ctx = el.getContext('2d');

  // 한 번 그리는 함수
  function draw() {
    const dpr = window.devicePixelRatio || 1;
    const W = Math.max(120, el.clientWidth || 300);
    const H = Math.max(60,  el.clientHeight || 100);

    // 캔버스 스케일
    el.width  = Math.round(W * dpr);
    el.height = Math.round(H * dpr);
    ctx.setTransform(1, 0, 0, 1, 0, 0); // reset
    ctx.scale(dpr, dpr);

    // 배경
    ctx.clearRect(0, 0, W, H);
    ctx.fillStyle = '#eff3fb';
    ctx.fillRect(0, 0, W, H);

    // 축
    ctx.strokeStyle = '#dbe5f3';
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(0, H - 20);
    ctx.lineTo(W, H - 20);
    ctx.stroke();

    // 데이터 없으면 표시
    const max = Math.max(1, ...raw);
    const n = raw.length || 1;

    const gap = Math.max(6, Math.floor(W / (n * 8))); // 반응형 간격
    const barW = Math.max(4, (W - gap * (n + 1)) / n);

    for (let i = 0; i < n; i++) {
      const v = raw[i] || 0;
      const h = Math.round((H - 36) * (v / max));
      const x = gap + i * (barW + gap);
      const y = H - 20 - h;

      // 그라디언트 (상단 옅고 하단 진하게)
      const grd = ctx.createLinearGradient(0, y, 0, y + h);
      grd.addColorStop(0, '#93c5fd');
      grd.addColorStop(1, '#2563eb');
      ctx.fillStyle = grd;

      // 위쪽만 둥근 직사각형
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

  // ✅ 반응형: 리사이즈시 다시 그리기
  let rafId = null;
  const ro = new ResizeObserver(() => {
    if (rafId) cancelAnimationFrame(rafId);
    rafId = requestAnimationFrame(draw);
  });
  ro.observe(el);

  // 안전 해제 (이 페이지에서 SPA 전환 안 쓰면 없어도 OK)
  window.addEventListener('beforeunload', () => {
    try { ro.disconnect(); } catch (_) {}
  });
})();
