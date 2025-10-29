document.addEventListener('DOMContentLoaded', function() {
  const el = document.getElementById('userChart');
  if (!el || !window.Chart) return;

  new Chart(el, {
    type: 'bar',
    data: {
      labels: ['1월', '2월', '3월', '4월', '5월', '6월'],
      datasets: [
        {
          label: '신규 유입',
          data: [30, 40, 55, 60, 35, 50],
          backgroundColor: 'rgba(108, 122, 255, 0.7)'
        },
        {
          label: '재방문',
          data: [15, 25, 35, 30, 25, 40],
          backgroundColor: 'rgba(225, 165, 255, 0.7)'
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { position: 'bottom' } },
      scales: { y: { beginAtZero: true } }
    }
  });
});
