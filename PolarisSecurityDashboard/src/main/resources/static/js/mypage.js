document.addEventListener('DOMContentLoaded', () => {
  // 비밀번호 보기/숨기기 토글
  document.querySelectorAll('.pw-toggle').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = btn.getAttribute('data-target');
      const input = document.getElementById(id);
      if (!input) return;
      input.type = input.type === 'password' ? 'text' : 'password';
      btn.textContent = input.type === 'password' ? '보기' : '숨기기';
      btn.setAttribute('aria-label', input.type === 'password' ? '비밀번호 보기' : '비밀번호 숨기기');
    });
  });

  // 비밀번호 강도 측정
  const newPw = document.getElementById('newPassword');
  const meter = document.getElementById('pwStrength');
  const bar = meter ? meter.querySelector('span') : null;

  function evaluateStrength(v) {
    let score = 0;
    if (v.length >= 8) score++;
    if (/[A-Z]/.test(v)) score++;
    if (/[a-z]/.test(v)) score++;
    if (/\d/.test(v)) score++;
    if (/[^A-Za-z0-9]/.test(v)) score++;
    return score; // 0~5
  }

  if (newPw && meter && bar) {
    newPw.addEventListener('input', () => {
      const v = newPw.value || '';
      const score = evaluateStrength(v);
      const pct = Math.min(100, score * 20);
      bar.style.width = pct + '%';
      meter.classList.remove('good', 'strong');
      if (score >= 4) meter.classList.add('strong');
      else if (score >= 2) meter.classList.add('good');
    });
  }

  // 비밀번호 제출 유효성 검사
  const pwForm = document.getElementById('pwForm');
  if (pwForm) {
    pwForm.addEventListener('submit', (e) => {
      const curr = (document.getElementById('currentPassword') || {}).value || '';
      const npw = (document.getElementById('newPassword') || {}).value || '';
      const conf = (document.getElementById('confirmPassword') || {}).value || '';

      if (!curr || !npw || !conf) {
        alert('모든 비밀번호 필드를 입력해주세요.');
        e.preventDefault();
        return;
      }
      if (npw.length < 8) {
        alert('새 비밀번호는 8자 이상이어야 합니다.');
        e.preventDefault();
        return;
      }
      if (npw !== conf) {
        alert('새 비밀번호와 확인이 일치하지 않습니다.');
        e.preventDefault();
        return;
      }
      if (curr === npw) {
        alert('현재 비밀번호와 동일한 비밀번호는 사용할 수 없습니다.');
        e.preventDefault();
        return;
      }
    });
  }

  // 프로필 폼 간단 가드(선택)
  const profileForm = document.getElementById('profileForm');
  if (profileForm) {
    profileForm.addEventListener('submit', (e) => {
      const name = (document.getElementById('name') || {}).value || '';
      if (name.length > 60) {
        alert('이름은 60자 이하로 입력해주세요.');
        e.preventDefault();
      }
    });
  }
});
