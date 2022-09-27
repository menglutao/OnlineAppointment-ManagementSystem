$.urlParam = function(name){
    var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(window.location.href);

    if(!results) {
        return undefined;
    }
    return results[1] || 0;
}

function epochToDuration(epoch){
    var hours = epoch/3600;
    var minutes = (epoch%3600)/60;
    var seconds = (epoch%3600)%60;

    return [Math.floor(hours),Math.floor(minutes),Math.floor(seconds)];
}

$( document ).ready(function() {
    var message = $.urlParam("errorMessage");

    if(message && message==="invalidSecretary"){
        $('#secretaryErrorMessage').removeClass("d-none");
        $('#secretaryModal').modal('show');
    }

    var rawDuration = $(".rawDuration");

    rawDuration.each(function(){
        var epoch = parseInt($(this).text());
        var duration = epochToDuration(epoch);
        var durationField = $(this).next();

        durationField.append(document.createTextNode(duration[0]+":"+duration[1]+":"+duration[2]));
    });

});