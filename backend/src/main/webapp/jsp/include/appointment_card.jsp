<%@page isELIgnored="false"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<div class="col-lg appointment_container ${param.status.equals('booked') ? 'active' : ''}" id="${param.appointmentId}"
     ${param.status.equals('booked') ? 'data-bs-toggle="modal" data-bs-target="#update_modal"' : '' }
     is_search_result="true"
     is_filtered="true" >
    <h3 class=" fw-normal service_name">${param.serviceName}</h3>
    <em class="fw-light company_name">${param.companyName}</em>
    <p class="appointmentDateTime fs-4">${param.appointmentDateTime} </p>

    <!-- Set color to status span based on their content -->
    <c:choose>
        <c:when test="${ param.status.equals('checked in')}">
            <span class="badge rounded-pill appointment_status text-bg-success">${param.status}</span>
        </c:when>
        <c:when test="${ param.status.equals('confirmed')}">
            <span class="badge rounded-pill appointment_status text-bg-primary">${param.status}</span>
        </c:when>
        <c:otherwise>   <!-- booked case-->
            <span class="badge rounded-pill appointment_status text-bg-warning">${param.status}</span>
        </c:otherwise>
    </c:choose>

    <!-- when the appointment is checked-in cannot be changed or deleted by the customer -->
    <!-- when the appointment is confirmed can only be deleted by the customer -->
    <button type="button" class="btn btn-sm btn-danger cancel-button" data-bs-toggle="modal" data-bs-target="#cancel_modal"
        ${param.status.equals('checked in') ? 'disabled' : ''} >
        <i class="fa-solid fa-xmark x-icon"></i> Cancel
    </button>
    <button type="button" class="btn btn-sm btn-success update-button" data-bs-toggle="modal" data-bs-target="#update_modal"
        ${param.status.equals('checked in') || param.status.equals('confirmed') ? 'disabled' : ''} >
        <i class="fa-solid fa-pen pen-icon "></i> Update
    </button>

</div>
