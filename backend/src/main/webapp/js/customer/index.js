/**
 * retrieves single cookie value
 * @param cookieName name of the cookie to be retrieved
*/
function getCookie(cookieName) {
    const allCookiesDecoded = decodeURIComponent(document.cookie);
    const allCookiesArray = allCookiesDecoded.split('; ');
    const name = cookieName + "=";
    let res = null;
    allCookiesArray.forEach(item => {
        if (item.indexOf(name) === 0)
            res = item.substring(name.length);
    })
    return res;
}


/**
 * show or hide appointments depending on their attributes
 */
function showAppointments() {
    document.querySelectorAll("#appointment_list .appointment_container").forEach(div => {
        if( (div.getAttribute("is_search_result") === "true") &&
            (div.getAttribute("is_filtered") === "true") )
            div.style.display = "block";
        else
            div.style.display = "none";
    });
}


/**
 * sorts the appointments depending on the provided criteria
 * @param orderingCriteria order to follow
 */
function sortAppointments(orderingCriteria){
    const criteria = (container_A, container_B) => {
        const date_A = container_A.querySelector(".appointmentDateTime").firstChild.nodeValue;
        const date_B = container_B.querySelector(".appointmentDateTime").firstChild.nodeValue;

        if (orderingCriteria === "latestFirst")
            return moment(date_A).isBefore(moment(date_B));
        else
            return moment(date_A).isAfter(moment(date_B));
    }


    //get all appointments divs
    let appointmentsDivs = document.querySelectorAll("#appointment_list div>div");

    //remove all appointments divs from DOM
    appointmentsDivs.forEach(node => { node.parentNode.removeChild(node)});

    //sort all appointments divs
    appointmentsDivs = Array.prototype.slice.call(appointmentsDivs, 0).sort(criteria);

    //insert all appointments divs
    let index = 0;
    document.querySelectorAll("#appointment_list>div").forEach(row =>{
        row.appendChild(appointmentsDivs[index++]);
        if(index < appointmentsDivs.length) //check to avoid indexOutOfBound for odd number of appointments
            row.appendChild(appointmentsDivs[index++]);
    });
}


/**
 * shows a toast message on the screen
 * @param isError true if the toast message is an error message
 * @param title a string representing the title of the toast message
 * @param message the message string shown in the toast
 */
function showToast(isError, title, message){
    //pick template toast
    const errorToastTemplate = document.getElementById('error_toast_template')

    //create new toast node
    let toastNode = errorToastTemplate.cloneNode(true);
    //set error title
    if(isError)
        toastNode.classList.add("text-bg-danger");
    else
        toastNode.classList.add("text-bg-success");

    //set error title
    toastNode.querySelector(".errTitle").appendChild(document.createTextNode(title))
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






//---------------------------event handlers---------------------------

/**
 * Event handler executed when the "book new appointment" button is clicked
 * triggers navigation to the create appointment page
 * @param event occurred event
 */
function bookAppointmentClickEventHandler(event) {
    //http://localhost:8080/backend/customer/appointment/new
    const url = window.location.href + "/appointment/new";
    window.location.assign(url);
}


/**
 * Event handler for a click event occurred on a "update" button of an
 * appointment or on a appointment_container div
 * A confirmation modal is opened and the appointment data is added to it
 * @param event occurred event
 */

function updateAppointmentClickEventHandler(event) {
    let appointmentContainer = event.currentTarget;
    if(event.currentTarget.tagName === "BUTTON") {
        //update button clicked
        event.stopPropagation();
        appointmentContainer = event.target.parentNode;
    }


    const serviceName = document.createTextNode(appointmentContainer.querySelector(".service_name").firstChild.nodeValue);
    let serviceNameContainer = document.getElementById("update_modal_service_name");

    if(serviceNameContainer.lastChild) {
        serviceNameContainer.removeChild(serviceNameContainer.lastChild);
    }

    serviceNameContainer.appendChild(serviceName);
    //datetime

    const dateTime = document.createTextNode(appointmentContainer.querySelector(".appointmentDateTime").firstChild.nodeValue);
    let dateTimeContainer = document.getElementById("update_modal_appointmentDateTime");

    if(dateTimeContainer.lastChild) {
        dateTimeContainer.removeChild(dateTimeContainer.lastChild);
    }

    dateTimeContainer.appendChild(dateTime);

    //pass appointment id
    document.getElementById("modal_update_btn").setAttribute("appointment_id", appointmentContainer.id.toString());
}



/**
 * EventHandler executed when the "update" button of the confirmation modal for
 * appointment update is clicked
 * navigate to updateAppointment.jsp
 */
function modalUpdateClickEventHandler(event) {
    const appointmentId = event.target.getAttribute("appointment_id");
    //http://localhost:8080/appointment/customer/appointment/update
    const url = window.location.href + "/appointment/update?appointmentId=" + appointmentId.toString();//window.location.href.substring(0, window.location.href.indexOf("customer")) + "customer/appointment/update?appointmentId=" + appointmentId.toString();
    window.location.assign(url);
}



/**
 * EventHandler executed when the "cancel" button of an appointment is clicked
 * a confirmation modal is opened and the appointment data is added to it
 */
function cancelAppointmentClickEventHandler(event) {
    event.stopPropagation(); //to prevent navigation to the appointment page
    const appointmentId = event.target.parentNode.id;

    //display some appointment data in modal
    //service name
    const serviceName = document.createTextNode(event.target.parentNode.querySelector(".service_name").firstChild.nodeValue);
    const serviceContainer = document.getElementById("modal_service_name");
    if(serviceContainer.lastChild) {
        serviceContainer.removeChild(serviceContainer.lastChild);
    }
    serviceContainer.appendChild(serviceName);

    //datetime
    const dateTime = document.createTextNode(event.target.parentNode.querySelector(".appointmentDateTime").firstChild.nodeValue);
    const dateTimeContainer = document.getElementById("modal_appointmentDateTime");
    if(dateTimeContainer.lastChild) {
        dateTimeContainer.removeChild(dateTimeContainer.lastChild);
    }
    dateTimeContainer.appendChild(dateTime);

    //pass appointment id
    document.getElementById("modal_cancel_btn").setAttribute("appointment_id", appointmentId.toString());
}



/**
 * EventHandler executed when the "cancel" button of the confirmation modal for appointment cancellation is clicked
 * Makes an ajax request to delete the clicked appointment
 */
function modalCancelClickEventHandler(event) {
    const appointmentId = event.target.getAttribute("appointment_id");

    //http://localhost:8080/backend/rest/appointment/appointmentId
    const url = window.location.href.substring(0, window.location.href.indexOf("customer")) + "rest/appointment/" + appointmentId.toString();

    //api request
    $.ajax({
        url: url,
        type: "DELETE",
        dataType: "json",
        headers : { "Authorization" : "Bearer " + getCookie("authToken") },
        xhrFields: { withCredentials: false },
        success: function (data) {
            if(data.error === false) {
                //successfully deleted remove appointment container
                let elementToBeRemoved = document.getElementById(appointmentId.toString());
                elementToBeRemoved.parentNode.removeChild(elementToBeRemoved);
                showToast(false, "Success", "Appointment deleted correctly");
            } else {
                showToast(true, "Error " + data.error.code, data.error.message);
            }

        },
        error: function (data) {
            if(data.responseJSON.error.message){
                showToast(true, "Error " + data.responseJSON.error.code, data.responseJSON.error.message);
            } else {
                showToast(true, "Request error ", JSON.stringify(data));
            }
        }
    });

}



/**
 * EventHandler executed every time the value of a checkbox is changed
 * Set the checkboxes to consistent values
 * Filters the appointment by setting is_filtered attribute
 * refreshes appointment list
 */
function checkboxChangeEventHandler(event) {
    let ids = ["ckb_all", "ckb_booked", "ckb_confirmed", "ckb_checked_in"];
    let checkedValues = ids.map( id => document.getElementById(id).checked);
    const aStatuIsChecked = checkedValues.slice(1).reduce( (res, current) => (res || current), false);

    //set consistent checkbox status
    if(!aStatuIsChecked && !checkedValues[0]) //nothing selected
        document.getElementById("ckb_all").checked = true;
    if(aStatuIsChecked && event.target.id !== ids[0])  //a status checkbox is selected and
        document.getElementById("ckb_all").checked = false;
    if(aStatuIsChecked && event.target.id === ids[0])  //a status checkbox is selected and
        ids.slice(1).forEach(id => {  //uncheck other checkboxes
            document.getElementById(id).checked = false;
        });

    //get selected checkboxes values
    ids = ids.slice(1);
    if (!document.getElementById("ckb_all").checked)
        ids = ids.filter( id => document.getElementById(id).checked);
    const selectedCkbValues = ids.map(id => document.getElementById(id).value);


    //filter appointments
    document.querySelectorAll("#appointment_list .appointment_status").forEach(statusNode => {
        statusNode.parentNode.removeAttribute("is_filtered");
        if(selectedCkbValues.includes(statusNode.firstChild.nodeValue))
            statusNode.parentNode.setAttribute("is_filtered", "true");
        else
            statusNode.parentNode.setAttribute("is_filtered", "false");
    });

    //refresh appointments list
    showAppointments();
}



/**
 * EventHandler executed every time the input event is triggered in the searchBox
 * Filters the appointment by setting is_search_result attribute
 * refreshes appointment list
 */
function searchBoxInputEventHandler(event) {
    const queryText = event.target.value;

    //filter appointments
    document.querySelectorAll("#appointment_list .appointment_container").forEach(appointment_container => {
        const serviceName = appointment_container.querySelector(".service_name").firstChild.nodeValue;
        const companyName = appointment_container.querySelector(".company_name").firstChild.nodeValue;

        appointment_container.parentNode.removeAttribute("is_filtered");
        if((serviceName.toLowerCase().indexOf(queryText.toLowerCase()) > -1) || 
            (companyName.toLowerCase().indexOf(queryText.toLowerCase()) > -1) 
        )
            appointment_container.setAttribute("is_search_result", "true");
        else
            appointment_container.setAttribute("is_search_result", "false");
    });

    //refresh appointments list
    showAppointments();
}



/**
 * EventHandler executed every time the change event is triggered in the order select
 * sorts the appointments basing on the value selected by the user
 */
function orderSelectChangeEventHandler(event) {
    const selectedValue = event.target.value;
    sortAppointments(selectedValue);
}




//-------------------------assign event handlers-------------------------

//create appointment
document.getElementById("add_app_btn").addEventListener('click', bookAppointmentClickEventHandler);

//searchBox
document.getElementById("search_box").addEventListener('input', searchBoxInputEventHandler);

//order select
document.getElementById("order_select").addEventListener('change', orderSelectChangeEventHandler);

//update appointment
document.querySelectorAll(".update-button").forEach(node => {
    node.addEventListener('click', updateAppointmentClickEventHandler);
});
document.querySelectorAll(".appointment_container.active").forEach(node => {
    node.addEventListener('click', updateAppointmentClickEventHandler);
});

//confirmation modal of appointment update
document.getElementById("modal_update_btn").addEventListener('click',modalUpdateClickEventHandler);

//cancel appointment
document.querySelectorAll(".cancel-button").forEach(node => {
    node.addEventListener('click', cancelAppointmentClickEventHandler);
});
//confirmation modal of appointment cancellation
document.getElementById("modal_cancel_btn").addEventListener('click', modalCancelClickEventHandler);

//filter checkBoxes
document.querySelectorAll("input.ckb_status").forEach(node => {
    node.addEventListener('change', checkboxChangeEventHandler);
});


//-------------------------format dates----------------------------------
document.querySelectorAll(".appointmentDateTime").forEach(node => {
    //format datetime
    let formattedDate = node.firstChild.nodeValue.toString();
    const correctDate = formattedDate.substring(0, formattedDate.indexOf("."));

    //replace original date with correctDate
    node.replaceChild(document.createTextNode(correctDate), node.childNodes[0]);
    node.style.display = "none"; // hide element, used only for ordering

    //format date
    formattedDate = moment(correctDate);
    if(formattedDate.isBetween(moment().subtract(1, 'w'), moment().add(1, 'w')))
        formattedDate = moment(formattedDate).calendar();
    else
        formattedDate = moment(formattedDate).format("D MMM YYYY [ at ] h:mm a");


    //display formattedDate
    const newDateEl = document.createElement("p");
    const newText = document.createTextNode(formattedDate);
    newDateEl.appendChild(newText);
    node.parentNode.insertBefore(newDateEl, node.nextSibling);

});

//sort appointments on page refresh
sortAppointments(document.getElementById("order_select").value);