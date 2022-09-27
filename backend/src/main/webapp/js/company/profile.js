/* Select the form */
var form = document.querySelector("form");
/* Object to hold leaflet map */
var map = null;
/* Marker when user click on map */
var marker = null;
/* Initial marker which shows current company location on
 * map. It will be deleted when user click on map for the
 * first time.
 */
var init_marker = null;


form.addEventListener('submit', function (event) {
    if (!form.checkValidity()) {
        event.preventDefault()
        event.stopPropagation()
    }

    form.classList.add('was-validated')
}, false);

/* Get lat and lon from hidden form elements */
var lat = parseFloat(document.getElementById("lat").value);
var lon = parseFloat(document.getElementById("lon").value);

/* Create map object inside div with id `map' */
map = L.map('map').setView([lat, lon], 13);
/* Place the initial marker on the map */
init_marker = new L.marker([lat, lon]);
init_marker.addTo(map);

/* Load the map tiles */
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
}).addTo(map);

/* Center and zoom the map after clicking */
function centerLeafletMapOnMarker(map, marker) {
    var latLngs = [marker.getLatLng()];
    var markerBounds = L.latLngBounds(latLngs);
    map.fitBounds(markerBounds);
}

/* Click handler function */
function onMapClick(e) {
    /* Remove marker if exist */
    if (marker != null) {
        map.removeLayer(marker);
    }

    /* Remove initial marker if exist and never
     * place it again, since marker will do the job
     */
    if (init_marker != null) {
        map.removeLayer(init_marker);
    }

    /* Create a new marker and place it on
     * clicked position
     */
    marker = new L.Marker(e.latlng, {draggable: true});
    map.addLayer(marker);

    /* Update form hidden values */
    $("#lat").val(e.latlng.lat);
    $("#lon").val(e.latlng.lng);

    /* Call the function to re-Center the map */
    centerLeafletMapOnMarker(map, marker);
}

/* Map click handler */
map.on('click', onMapClick);
