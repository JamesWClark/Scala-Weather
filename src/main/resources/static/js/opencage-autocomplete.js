document.addEventListener('DOMContentLoaded', function () {
  var input = document.getElementById('city');
  var suggestionsContainer = document.createElement('div');
  suggestionsContainer.className = 'autocomplete-suggestions';
  input.parentNode.appendChild(suggestionsContainer);

  input.addEventListener('input', function () {
    var query = input.value;
    if (query.length < 3) {
      suggestionsContainer.innerHTML = '';
      return; // Wait until the user has typed at least 3 characters
    }

    var apiKey = '53771be1069a4f5c9d775211de433846';
    fetch(`https://api.opencagedata.com/geocode/v1/json?q=${query}&key=${apiKey}`)
      .then(response => response.json())
      .then(data => {
        var results = data.results;
        suggestionsContainer.innerHTML = '';
        results.forEach(result => {
          var suggestion = document.createElement('div');
          suggestion.className = 'autocomplete-suggestion';
          suggestion.textContent = result.formatted;
          suggestion.addEventListener('click', function () {
            input.value = result.formatted;
            suggestionsContainer.innerHTML = '';
            var lat = result.geometry.lat;
            var lng = result.geometry.lng;
            centerMapOnLocation(lat, lng);
          });
          suggestionsContainer.appendChild(suggestion);
        });
      })
      .catch(error => console.error('Error fetching autocomplete results:', error));
  });

  function centerMapOnLocation(lat, lng) {
    var userLocation = ol.proj.fromLonLat([lng, lat]);
    map.getView().animate({
      center: userLocation,
      zoom: 12, // Adjust the zoom level as needed
      duration: 2000 // Duration in milliseconds
    });

    // Add a marker at the selected location
    var userFeature = new ol.Feature({
      geometry: new ol.geom.Point(userLocation)
    });

    var userSource = new ol.source.Vector({
      features: [userFeature]
    });

    var userLayer = new ol.layer.Vector({
      source: userSource,
      style: new ol.style.Style({
        image: new ol.style.Circle({
          radius: 6,
          fill: new ol.style.Fill({ color: 'red' }),
          stroke: new ol.style.Stroke({
            color: [255, 0, 0], width: 2
          })
        })
      })
    });

    map.addLayer(userLayer);
  }
});