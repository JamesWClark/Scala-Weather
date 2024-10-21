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
        console.log("Reverse geocoding result:", city, state, "Lat:", position.coords.latitude, "Lng:", position.coords.longitude); // Log the result
        // Animate the map to the user's location
        map.getView().animate({
          center: userLocation,
          zoom: 12, // Adjust the zoom level as needed
          duration: 700 // Duration in milliseconds
        });

        // Listen for the animation end event
        map.once('moveend', function () {
          // Update the input fields with the city, state, latitude, and longitude
          var cityInput = document.getElementById('city');
          var stateInput = document.getElementById('state');
          var latInput = document.getElementById('lat');
          var longInput = document.getElementById('long');
          console.log("Updating input fields with:", city, state, "Lat:", position.coords.latitude, "Lng:", position.coords.longitude); // Log the update
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
    document.getElementById('lat').value = lat.toFixed(6);
    document.getElementById('long').value = lon.toFixed(6);
    reverseGeocode(lat, lon, function (city, state) {
      console.log('Reverse geocoding result:', 'city: ', city, 'state: ', state, 'Lat:', lat, 'Lng:', lon); // Log the result
      var cityInput = document.getElementById('city');
      var stateInput = document.getElementById('state');
      cityInput.value = city;
      stateInput.value = state;
    });
  });

  function reverseGeocode(lat, lng, callback) {
    var apiKey = '53771be1069a4f5c9d775211de433846';
    fetch(`https://api.opencagedata.com/geocode/v1/json?q=${lat}+${lng}&key=${apiKey}`)
      .then(response => response.json())
      .then(data => {
        if (data.results.length > 0) {
          var components = data.results[0].components;
          var city = components.city || components.town || components.village;
          var state = components.state;
          console.log("Fetched city and state:", city, state, "Lat:", lat, "Lng:", lng); // Log the fetched data
          callback(city, state);
        }
      })
      .catch(error => console.error('Error fetching reverse geocode results:', error));
  }

  // Expose the map object to the global scope for use in autocomplete.js
  window.map = map;
});