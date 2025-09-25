// ==== 설정 ====
const API_URL = (days) => `/api/logs/report?days=${days}`;
const TZ = 'Asia/Seoul';

let allLogs = [];
let page = 1;
const pageSize = 20;

document.addEventListener('DOMContentLoaded', () => {
  const rangeSel = $("range");
  const typeSel  = $("type");
  const domainIn = $("domain");

  $("refresh").addEventListener('click', () => {
    page = 1;
    loadAndRender(+rangeSel.value, typeSel.value, domainIn.value.trim());
  });

  $("prev").addEventListener('click', () => { if (page > 1) { page--; renderTable(); }});
  $("next").addEventListener('click', () => {
    const totalPages = Math.max(1, Math.ceil(filtered().length / pageSize));
    if (page < totalPages) { page++; renderTable(); }
  });

  // 초기 로드
  loadAndRender(+rangeSel.value, typeSel.value, domainIn.value.trim());
});

// ========== 데이터 로드 ==========
async function loadAndRender(days, type, domain) {
  let info = '';
  try {
    const url = API_URL(days);
    console.log('[fetch]', url);
    const res = await fetch(url, { headers: { 'Accept': 'application/json' }});
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();
    allLogs = Array.isArray(data) ? data : [];
    console.log('[fetch] count=', allLogs.length, 'sample=', allLogs[0]);
  } catch (e) {
    allLogs = [];
    info = `API 오류: ${e.message}`;
    console.error('[fetch:error]', e);
  }

  setText('result-info', info || `${allLogs.length.toLocaleString()} rows`);
  renderKpis();
  renderGroups(days);
  renderTable();
}

// ========== 필터 ==========
function filtered() {
  const typeSel = ($("type").value || 'ALL').toUpperCase();
  const domainQ = ($("domain").value || '').trim().toLowerCase();

  return allLogs.filter(l => {
    const lt = (l.logType || '').toUpperCase();
    const dm = (l.domain  || '').toLowerCase();
    const okType   = (typeSel === 'ALL' || lt === typeSel);
    const okDomain = (domainQ === '' || dm.includes(domainQ));
    return okType && okDomain;
  });
}

// ========== KPI ==========
function renderKpis() {
  const rows = filtered();
  const norm = rows.map(r => ({...r, logType:(r.logType||'').toUpperCase()}));
  const c = countBy(norm, 'logType');
  setText('kpi-total', rows.length);
  setText('kpi-malware', c['MALWARE'] || 0);
  setText('kpi-remote',  c['REMOTE']  || 0);
  setText('kpi-rooting', c['ROOTING'] || 0);
}

// ========== 그룹(차트/도메인) ==========
function renderGroups(days) {
  const rows = filtered();

  // ✅ 레이아웃이 안정된 다음 프레임에 차트 렌더 (width/height 0 방지)
  requestAnimationFrame(() => renderDayChart(days, rows));

  // 도메인 리스트
  const byDom = groupBy(rows, r => (r.domain || '(unknown)'));
  const domPairs = Object.entries(byDom)
    .map(([k, a]) => [k, a.length]).sort((a,b)=>b[1]-a[1]).slice(0,20);
  const domHtml = domPairs.map(([k,n]) =>
    `<div class="item"><span>${escapeHtml(k)}</span><span class="badge">${n}</span></div>`
  ).join('');
  setHTML('by-domain', domHtml || `<div class="muted">데이터 없음</div>`);
}

// ========== 테이블 ==========
function renderTable() {
  const rows = filtered();
  const totalPages = Math.max(1, Math.ceil(rows.length / pageSize));
  if (page > totalPages) page = totalPages;

  const start = (page - 1) * pageSize;
  const slice = rows.slice(start, start + pageSize);

  const html = slice.map(r => {
    const created = fmtDate(r.createdAt);
    const extraStr = compactExtra(r.extra);
    const typeTxt = (r.logType||'').toUpperCase();
    return `
      <tr>
        <td>${r.id ?? ''}</td>
        <td>${created}</td>
        <td>${escapeHtml(r.domain || '')}</td>
        <td><span class="badge type-${typeTxt}">${typeTxt}</span></td>
        <td>${r.osVersion ?? ''}</td>
        <td>${r.appVersion ?? ''}</td>
        <td>${escapeHtml(extraStr)}</td>
      </tr>`;
  }).join('');

  setHTML('rows', html || `<tr><td colspan="7" class="muted" style="text-align:center">데이터 없음</td></tr>`);
  setText('page-info', `${page} / ${totalPages}`);
}

// ========== 바 차트 (최근 N일) ==========
function renderDayChart(days, rows){
  const el = document.getElementById('by-day-chart');
  if (!el) return;

  // ✅ 차트 영역 강제 높이(혹시 CSS가 안 먹을 때)
  const wrap = el.parentElement;
  if (wrap && (!wrap.style.height || wrap.clientHeight === 0)) {
    wrap.style.height = '260px';
  }

  // devicePixelRatio 보정 + 사이즈 계산
  const dpr = window.devicePixelRatio || 1;
  const rect = wrap.getBoundingClientRect();
  const cssW = rect.width  || 600;
  const cssH = rect.height || 260;
  el.width  = Math.round(cssW * dpr);
  el.height = Math.round(cssH * dpr);
  const ctx = el.getContext('2d');
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
  ctx.clearRect(0,0,cssW,cssH);

  // 데이터 준비 (타임존 고려 키)
  const dayKeys = lastNDays(days);
  const byDay   = groupBy(rows, r => dateKey(r.createdAt));
  const counts  = dayKeys.map(k => (byDay[k] || []).length);

  console.log('[chart] size=', cssW, cssH, 'keys=', dayKeys, 'counts=', counts);

  // 색상/영역
  const axisColor = '#94a3b8';
  const textColor = '#0f172a';
  const barColor  = '#2563eb';
  const gridColor = '#e5e7eb';
  const pad = { l: 40, r: 12, t: 8, b: 34 };
  const W = cssW - pad.l - pad.r;
  const H = cssH - pad.t - pad.b;

  // X축
  ctx.strokeStyle = axisColor;
  ctx.lineWidth = 1;
  ctx.beginPath();
  ctx.moveTo(pad.l, pad.t + H + 0.5);
  ctx.lineTo(pad.l + W, pad.t + H + 0.5);
  ctx.stroke();

  // Y축/그리드
  const maxVal = Math.max(1, Math.max(...counts));
  const step = Math.max(1, Math.ceil(maxVal / 4));
  ctx.font = '12px system-ui, -apple-system, Segoe UI, Roboto, Arial';
  ctx.fillStyle = textColor;
  ctx.textAlign = 'right';
  ctx.textBaseline = 'middle';
  for (let v=0; v<=maxVal; v+=step){
    const y = pad.t + H - (v / maxVal) * H + 0.5;
    ctx.strokeStyle = gridColor;
    ctx.beginPath(); ctx.moveTo(pad.l, y); ctx.lineTo(pad.l + W, y); ctx.stroke();
    ctx.fillStyle = axisColor;
    ctx.fillText(String(v), pad.l - 6, y);
  }

  // 데이터 없으면 안내
  if (counts.reduce((a,b)=>a+b,0) === 0){
    ctx.fillStyle = '#64748b';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText('최근 7일 데이터 없음', pad.l + W/2, pad.t + H/2);
    // ✅ 캔버스 동작 테스트(디버그용): 사각형 한 번 그려보기
    ctx.fillStyle = 'rgba(37,99,235,0.15)';
    ctx.fillRect(pad.l + 8, pad.t + H/2 + 18, 80, 12);
    return;
  }

  // 막대
  const n = dayKeys.length;
  const gap = 10;
  const barW = Math.max(8, (W - gap*(n-1)) / n);
  ctx.fillStyle = barColor;

  for (let i=0;i<n;i++){
    const x = pad.l + i*(barW+gap);
    const h = (counts[i] / maxVal) * H;
    const y = pad.t + H - h;

    ctx.beginPath();
    if (ctx.roundRect) ctx.roundRect(x, y, barW, h, 4);
    else ctx.rect(x, y, barW, h);
    ctx.fill();

    if (counts[i] > 0){
      ctx.fillStyle = textColor;
      ctx.textAlign = 'center';
      ctx.textBaseline = 'bottom';
      ctx.fillText(String(counts[i]), x + barW/2, y - 4);
      ctx.fillStyle = barColor;
    }
  }

  // X 라벨 (MM-DD)
  ctx.fillStyle = textColor;
  ctx.textAlign = 'center';
  ctx.textBaseline = 'top';
  for (let i=0;i<n;i++){
    const x = pad.l + i*(barW+gap) + barW/2;
    ctx.fillText(dayKeys[i].slice(5), x, pad.t + H + 6);
  }
}

// ===== 유틸 =====
function $(id){ return document.getElementById(id); }
function setText(id, v){ const el=$(id); if(el) el.textContent = v; }
function setHTML(id, v){ const el=$(id); if(el) el.innerHTML = v; }
function groupBy(arr, keyFn){ return arr.reduce((a,x)=>{ const k=keyFn(x); (a[k] ||= []).push(x); return a; }, {}); }
function countBy(arr, key){ return arr.reduce((a,x)=>{ const k=x[key] ?? '(null)'; a[k]=(a[k]||0)+1; return a; }, {}); }

// 날짜 파서 + 키
function parseDate(v){
  if (v == null) return new Date(NaN);
  if (typeof v === 'number') return new Date(v);
  if (typeof v === 'string') {
    let s = v.trim();
    if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}/.test(s)) s = s.replace(' ', 'T'); // 'yyyy-MM-dd HH:mm:ss' 지원
    return new Date(s);
  }
  return new Date(v);
}
function dateKey(v){
  const d = (v instanceof Date) ? v : parseDate(v);
  const y = new Intl.DateTimeFormat('en-CA', { timeZone: TZ, year:'numeric'}).format(d);
  const m = new Intl.DateTimeFormat('en-CA', { timeZone: TZ, month:'2-digit'}).format(d);
  const day = new Intl.DateTimeFormat('en-CA', { timeZone: TZ, day:'2-digit'}).format(d);
  return `${y}-${m}-${day}`;
}
function lastNDays(n){
  const out = [];
  const now = new Date();
  for (let i=n-1;i>=0;i--){
    const d = new Date(now);
    d.setHours(0,0,0,0);
    d.setDate(d.getDate()-i);
    out.push(dateKey(d)); // ✅ UTC 변환 안 함
  }
  return out;
}
function fmtDate(iso){
  const d = parseDate(iso);
  return new Intl.DateTimeFormat('ko-KR', {
    timeZone: TZ, year:'numeric', month:'2-digit', day:'2-digit',
    hour:'2-digit', minute:'2-digit', second:'2-digit'
  }).format(d);
}
function escapeHtml(s){ return String(s).replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); }
function compactExtra(extra){
  if (extra == null) return '';
  if (typeof extra === 'string') {
    try { return compactExtra(JSON.parse(extra)); } catch { return extra; }
  }
  if (typeof extra === 'object') {
    const keys = Object.keys(extra);
    return keys.slice(0,3).map(k => `${k}:${short(String(extra[k]))}`).join(', ')
      + (keys.length>3 ? ` …(+${keys.length-3})` : '');
  }
  return String(extra);
}
function short(s){ return s.length>24 ? s.slice(0,24)+'…' : s; }
