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

      // Center the map on the user's location
      map.getView().setCenter(userLocation);
      map.getView().setZoom(12); // Adjust the zoom level as needed
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
  });
});