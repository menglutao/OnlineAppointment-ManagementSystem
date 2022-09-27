/**
 * shows a toast message on the screen
 * @param colorClass type of the toast message, expressed byt its color
 * @param title a string representing the title of the toast message
 * @param message the message string shown in the toast
 */
function showToast(colorClass, title ,message){
    //pick template toast
    const errorToastTemplate = document.getElementById('error_toast_template')

    //create new toast node
    let toastNode = errorToastTemplate.cloneNode(true);
    toastNode.classList.add(colorClass);

    //set error title
    toastNode.querySelector(".errTitle").appendChild(document.createTextNode(title));
    //set error message
    toastNode.querySelector(".errMessage").appendChild(document.createTextNode(message));

    //add new toast node to DOM
    document.querySelectorAll(".toast-container")[0].appendChild(toastNode);

    //show toast
    const toast = new bootstrap.Toast(toastNode);
    toast.show();

    //add event listener to remove toast when hidden
    toastNode.addEventListener('hidden.bs.toast', event => {
        event.target.parentNode.removeChild(event.target);
    })

}


$.urlParam = function(name){
    var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(window.location.href);

    if(!results) {
        return undefined;
    }
    return results[1] || 0;
}

$( document ).ready(function() {
    var message = $.urlParam("message");

    if(message){
        var messageBody = "";

        switch (message) {
            case 'issuedVerification':
                showToast("text-bg-success","Email verification", "Verification email sent");
                break;
            case 'verificationSuccess':
                showToast("text-bg-success","Email verification", "User successfully verified");
                break;
            case 'verificationError':
                showToast("text-bg-danger","Email verification", "Invalid verification token");
                break;
            case 'verificationPending':
                showToast("text-bg-warning","Email verification", "Before singing-in, please verify the account");
                break;
        }
    }
});




function isEmail(email) {
    var EmailRegex = /^([a-zA-Z0-9_.+-])+\@(([a-zA-Z0-9-])+\.)+([a-zA-Z0-9]{2,4})+$/;
    return EmailRegex.test(email);
}



$("#form_submit_btn").click(event => {
    //http://localhost:8080/backend/rest/auth
    const url = window.location.href.substring(0, window.location.href.indexOf("login")) + "rest/auth/";
    const email = document.getElementsByName("email")[0].value;
    const password = document.getElementsByName("password")[0].value;

    if(!isEmail(email)){
        //toast not email
        showToast("text-bg-warning", "Email invalid", "PLease, write a valid email address");
    } else {

        //make appi request for auth token
        $.ajax({
            url: url,
            type: "GET",
            dataType: "json",
            headers : { "Authorization" : "Basic " + btoa( email + ":" + password) },
            xhrFields: { withCredentials: false },

            success: function (data) {
                if(data.hasOwnProperty("error")) {
                    showToast("text-bg-danger","Error", data.error.message);
                } else if(data.hasOwnProperty("access_token")) {
                    //auth token successfully retrieved
                    document.cookie = "authToken=" + data.access_token;
                    document.getElementsByName("form_login")[0].submit();
                } else {
                    showToast("text-bg-danger", "Response error", "malformed response");
                }
            },
            error: function (data) {
                if(data.responseJSON.error.message)
                    showToast("text-bg-danger", "Error", data.responseJSON.error.message);
                else
                    showToast("text-bg-danger", "Request error", data.responseJSON.error.message);
            }
        });

    }

});