$( "#services" ).change(function (event) {
    console.log(event.target.value);
    var appointmentID = $("#appointmentId");
    var url = null;

    if(appointmentID.length){
        url = window.location.origin+"/backend/customer/getTimeslotsByServiceId?serviceID=" + event.target.value +"&appointmentID="+appointmentID.val();
    }else{
        url = window.location.origin+"/backend/customer/getTimeslotsByServiceId?serviceID=" + event.target.value;
    }


    $.get(url, function (responseJson) {
        var tz = Intl.DateTimeFormat().resolvedOptions().timeZone;
        var locale = "pt-BR";

        var timeslotSelect = $("#timeslots");
        timeslotSelect.find("option.appended").remove();

        $.each(responseJson, function (index, timeslot) {
            var dt = new Date(timeslot.datetimeLong).toLocaleString(locale, {timeZone: tz});

            $("<option>").val(timeslot.datetimeLong).addClass("appended").text(dt).appendTo(timeslotSelect);
        });
    });
});

$( "#companies" ).change(function (event) {
    console.log(event.target.value);
    var url = window.location.origin+"/backend/customer/getServicesByCompany?companyName=" + event.target.value;

    $.get(url, function (responseJson) {
        var serviceSelect = $("#services");
        serviceSelect.find("option.appended").remove();
        $("#timeslots").find("option.appended").remove();

        $.each(responseJson, function (index, service) {
            console.log(service);
            $("<option>").val(service.ID).text(service.name).addClass("appended").appendTo(serviceSelect);
        });
    });
});

