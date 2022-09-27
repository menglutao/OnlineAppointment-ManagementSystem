function epochToDuration(epoch){
    var hours = epoch/3600;
    var minutes = (epoch%3600)/60;
    var seconds = (epoch%3600)%60;

    return [Math.floor(hours),Math.floor(minutes),Math.floor(seconds)];
}

function durationToEpoch(hours,minutes,seconds){
    return hours*3600+minutes*60+seconds;
}

$( "#serviceForm" ).submit(function( event ) {
    var hours = parseInt($("#hours").val());
    var minutes = parseInt($("#minutes").val());
    var seconds = parseInt($("#seconds").val());

    $("<input />").attr("type", "hidden")
        .attr("name", "duration")
        .attr("value", durationToEpoch(hours,minutes,seconds))
        .appendTo("#serviceForm");
});

$( document ).ready(function() {
    var oldDuration = $("#oldDuration");

    if(oldDuration.length){
        var epoch = parseInt(oldDuration.val());
        var duration = epochToDuration(epoch);

        $("#hours").val(duration[0]);
        $("#minutes").val(duration[1]);
        $("#seconds").val(duration[2]);
    }
});
