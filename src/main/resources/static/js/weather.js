document.addEventListener('DOMContentLoaded', function () {
  var form = document.getElementById('weatherForm');
  form.addEventListener('submit', function (event) {
    event.preventDefault();
    var formData = new FormData(form);
    var query = new URLSearchParams(formData).toString();
    fetch('/weather?' + query)
      .then(response => response.text())
      .then(html => {
        var parser = new DOMParser();
        var doc = parser.parseFromString(html, 'text/html');
        var weatherInfo = doc.getElementById('weatherInfo').innerHTML;
        document.getElementById('weatherInfo').innerHTML = weatherInfo;
      })
      .catch(error => console.error('Error fetching weather data:', error));
  });
});