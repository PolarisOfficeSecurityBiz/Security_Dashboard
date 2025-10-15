// static/js/overview.js

(function drawMiniBar() {
  const el = document.getElementById('by-day-canvas');
  if (!el) return;

  const raw = (el.dataset.points || '0,0,0,0,0,0,0').split(',').map(n => +n || 0);
  const dpr = window.devicePixelRatio || 1;
  const ctx = el.getContext('2d');
  const W = el.clientWidth, H = el.clientHeight;

  el.width = W * dpr;
  el.height = H * dpr;
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

  const max = Math.max(1, ...raw);
  const gap = 8;
  const barW = (W - gap * (raw.length + 1)) / raw.length;

  for (let i = 0; i < raw.length; i++) {
    const v = raw[i];
    const h = Math.round((H - 36) * (v / max));
    const x = gap + i * (barW + gap);
    const y = H - 20 - h;

    const grd = ctx.createLinearGradient(0, y, 0, y + h);
    grd.addColorStop(0, '#93c5fd');
    grd.addColorStop(1, '#2563eb');
    ctx.fillStyle = grd;

    const r = 6;
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
})();
