document.addEventListener('DOMContentLoaded', function () {
  function fetchWeatherByCity(event) {
    event.preventDefault();
    var city = document.getElementById('city').value;
    fetch(`/weather/city/${city}`)
      .then(response => response.json())
      .then(data => updateWeatherInfo(data))
      .catch(error => console.error('Error fetching weather data:', error));
  }

  function fetchWeatherByLatLong(event) {
    event.preventDefault();
    var lat = document.getElementById('latitude').value;
    var long = document.getElementById('longitude').value;
    fetch(`/weather/latlong/${lat},${long}`)
      .then(response => response.json())
      .then(data => updateWeatherInfo(data))
      .catch(error => console.error('Error fetching weather data:', error));
  }

  function fetchWeatherByMap(event) {
    event.preventDefault();
    var lat = document.getElementById('map-latitude').value;
    var long = document.getElementById('map-longitude').value;
    fetch(`/weather/latlong/${lat},${long}`)
      .then(response => response.json())
      .then(data => updateWeatherInfo(data))
      .catch(error => console.error('Error fetching weather data:', error));
  }

  function updateWeatherInfo(data) {
    var weatherInfoDiv = document.getElementById('weatherInfo');
    weatherInfoDiv.innerHTML = '';

    var card = document.createElement('div');
    card.className = 'card';

    var cardBody = document.createElement('div');
    cardBody.className = 'card-body';

    var cardTitle = document.createElement('h5');
    cardTitle.className = 'card-title';
    cardTitle.textContent = data.location;

    var weatherIcon = document.createElement('img');
    weatherIcon.className = 'weather-icon mr-3';
    weatherIcon.src = data.icon;

    var weatherDetails = document.createElement('div');
    weatherDetails.className = 'flex-grow-1';

    var currentTemp = document.createElement('p');
    currentTemp.className = 'display-4';
    currentTemp.textContent = `${data.currentTemperature}°F (Current)`;

    var shortForecast = document.createElement('p');
    shortForecast.className = 'lead';
    shortForecast.textContent = data.shortForecast;

    var dayNightTemp = document.createElement('p');
    dayNightTemp.className = 'text-muted';
    dayNightTemp.textContent = `Day: ${data.dayTemperature}°F, Night: ${data.nightTemperature}°F`;

    var characterization = document.createElement('p');
    characterization.className = 'text-muted';
    characterization.textContent = `The outside air feels ${data.characterization} right now`;

    weatherDetails.appendChild(currentTemp);
    weatherDetails.appendChild(shortForecast);
    weatherDetails.appendChild(dayNightTemp);
    weatherDetails.appendChild(characterization);

    cardBody.appendChild(cardTitle);
    cardBody.appendChild(weatherIcon);
    cardBody.appendChild(weatherDetails);
    card.appendChild(cardBody);
    weatherInfoDiv.appendChild(card);
  }

  window.fetchWeatherByCity = fetchWeatherByCity;
  window.fetchWeatherByLatLong = fetchWeatherByLatLong;
  window.fetchWeatherByMap = fetchWeatherByMap;
});