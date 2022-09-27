var map = null;
var marker = null;

map = L.map('map').setView([51.505, -0.09], 13);

var tiles = L.tileLayer('https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token=pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpejY4NXVycTA2emYycXBndHRqcmZ3N3gifQ.rJcFIG214AriISLbB6B5aw', {
    maxZoom: 18,
    attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, ' +
        'Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
    id: 'mapbox/streets-v11',
    tileSize: 512,
    zoomOffset: -1
}).addTo(map);

function onMapClick(e) {
    if(marker!=null){
        map.removeLayer(marker);
    }

    marker = new L.Marker(e.latlng, {draggable:true});
    map.addLayer(marker);

    $("#companyLat").val(e.latlng.lat);
    $("#companyLon").val(e.latlng.lng);

    centerLeafletMapOnMarker(map,marker);
}

function centerLeafletMapOnMarker(map, marker) {
    var latLngs = [ marker.getLatLng() ];
    var markerBounds = L.latLngBounds(latLngs);
    map.fitBounds(markerBounds);
}

map.on('click', onMapClick);

$(document).ready(function() {
    $('#companyDetails').hide();
});

$('#role').on('change', function() {
    console.log("change select " + $(this).val());
    if($(this).val() === "admin"){
        $('#companyDetails').show();
        map.invalidateSize();
    }else{
        $('#companyDetails').hide();
    }
});

$("#myBtn").click(function(){
    $('#myToast').toast({autohide: false});
    $("#myToast").toast("show");
});

var validator = $('#registrationForm').jbvalidator({
    errorMessage: true,
    successClass: true,
    language: "https://emretulek.github.io/jbvalidator/dist/lang/en.json"
});

function isEmail(email) {
    var EmailRegex = /^([a-zA-Z0-9_.+-])+\@(([a-zA-Z0-9-])+\.)+([a-zA-Z0-9]{2,4})+$/;
    return EmailRegex.test(email);
}

//custom validate methods
validator.validator.companyEmailValidator = function(el, event){
    if($(el).is('[name=companyEmail]') && $('#role').val()==="admin"){
        if ($(el).val()==="") {
            return "Please fill out this field";
        }

        if(!isEmail($(el).val())){
            return "Please enter an e-mail address.";
        }
    }
}

validator.validator.companyNameValidator = function(el, event){
    if($(el).is('[name=companyName]') && $('#role').val()==="admin" && !$(el).val()){
        return "Please fill out this field";
    }
}

validator.validator.companyAddressValidator = function(el, event){
    if($(el).is('[name=companyAddress]') && $('#role').val()==="admin" && !$(el).val()){
        return "Please fill out this field";
    }
}

validator.validator.companyPhoneValidator = function(el, event){
    if($(el).is('[name=companyPhone]') && $('#role').val()==="admin" && !$(el).val()){
        return "Please fill out this field";
    }
}

$( "#registrationForm" ).submit(function( event ) {
    if (validator.checkAll() > 0 || (!marker && $("#role").val()==="admin")) {

        if(!marker){

            $("#companyLat").removeClass("is-valid");
            $("#companyLon").removeClass("is-valid");
            $("#companyLat").addClass("is-invalid");
            $("#companyLon").addClass("is-invalid");

            if(!$("#map").next().length) {
                $("#map").after("<div class=\"invalid-feedback\">Please select a position</div>");
            }
        }else{
            if($("#map").next().length) {
                $("#map").next().remove();
            }
        }

        validator.reload();
        event.preventDefault();

        return;
    }
});
