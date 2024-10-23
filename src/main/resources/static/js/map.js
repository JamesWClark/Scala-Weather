document.addEventListener('DOMContentLoaded', function () {
  var map = new ol.Map({
    target: 'map',
    layers: [
      new ol.layer.Tile({
        source: new ol.source.OSM()
      })
    ],
    view: new ol.View({
      center: ol.proj.fromLonLat([0, 0]),
      zoom: 2
    })
  });

  // Center the map on the user's location and add a marker
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(function (position) {
      var userLonLat = [position.coords.longitude, position.coords.latitude];
      var userLocation = ol.proj.fromLonLat(userLonLat);

      // Create a feature for the user's location
      var userFeature = new ol.Feature({
        geometry: new ol.geom.Point(userLocation)
      });

      // Create a vector source and layer for the user's location
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

      // Add the user layer to the map
      map.addLayer(userLayer);

      // Perform reverse geocoding immediately
      reverseGeocode(position.coords.latitude, position.coords.longitude, function (city, state) {
        // Animate the map to the user's location
        map.getView().animate({
          center: userLocation,
          zoom: 12, // Adjust the zoom level as needed
          duration: 700 // Duration in milliseconds
        });

        // Listen for the animation end event
        map.once('moveend', function () {
          // Update the input fields with the city, state, latitude, and longitude
          var cityInput = document.getElementById('map-city');
          var stateInput = document.getElementById('map-state');
          var latInput = document.getElementById('map-latitude');
          var longInput = document.getElementById('map-longitude');
          cityInput.value = city;
          stateInput.value = state;
          latInput.value = position.coords.latitude.toFixed(6);
          longInput.value = position.coords.longitude.toFixed(6);
        });
      });
    }, function (error) {
      console.error('Error getting user location:', error);
    });
  } else {
    console.error('Geolocation is not supported by this browser.');
  }

  map.on('click', function (evt) {
    var coordinate = ol.proj.toLonLat(evt.coordinate);
    var lon = coordinate[0];
    var lat = coordinate[1];
    document.getElementById('map-latitude').value = lat.toFixed(6);
    document.getElementById('map-longitude').value = lon.toFixed(6);

    // Fetch city and state using the reverse geocoding endpoint
    fetch(`/geocoding/reverse-geocode?latitude=${lat}&longitude=${lon}`)
      .then(response => response.json())
      .then(data => {
        console.log('Reverse geocoding result:', 'city: ', data.city, 'state: ', data.state, 'Lat:', lat, 'Lng:', lon); // Log the result
        document.getElementById('map-city').value = data.city;
        document.getElementById('map-state').value = data.state;
        document.getElementById('map-latitude').value = lat.toFixed(6);
        document.getElementById('map-longitude').value = lon.toFixed(6);
      })
      .catch(error => console.error('Error fetching reverse geocode results:', error));
  });

  function reverseGeocode(lat, lng, callback) {
    fetch(`/geocoding/reverse-geocode?latitude=${lat}&longitude=${lng}`)
      .then(response => response.json())
      .then(data => {
        callback(data.city, data.state);
      })
      .catch(error => console.error('Error fetching reverse geocode results:', error));
  }

  document.getElementById('mapForm').addEventListener('submit', function(event) {
    event.preventDefault();
    var lat = document.getElementById('map-latitude').value;
    var long = document.getElementById('map-longitude').value;
    window.location.href = `/weather/latlong/${lat},${long}`;
  });

  // Expose the map object to the global scope for use in autocomplete.js
  window.map = map;
});