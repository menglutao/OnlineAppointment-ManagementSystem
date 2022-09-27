<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page isELIgnored="false"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="/jsp/include/head.jsp" %>
    <link href="<c:url value="/css/customer/index.css"/>" rel="stylesheet">

    <title>Appointments</title>
</head>

<body>

<%@ include file="/jsp/include/nav.jsp" %>

<main class="flex-shrink-0">
    <div class="container">


        <!-- Toast container for messages -->
        <div aria-live="polite" aria-atomic="true" class="position-relative">

            <div class="toast-container position-fixed end-0 p-4">
                <div class="toast" id="error_toast_template" role="alert" aria-live="assertive" aria-atomic="true">

                    <div class="d-flex">
                        <div class="toast-body">
                            <strong class="errTitle"></strong>
                            <br/>
                            <span class="errMessage"></span>
                        </div>
                        <button type="button" class="btn-close me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
                    </div>

                </div>
            </div>

        </div>


        <!-- Modal for confirmation of cancellation of an appointment -->
        <div class="modal fade" id="cancel_modal" tabindex="-1" aria-labelledby="cancel_modal_label" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header">
                        <h4 class="modal-title" id="cancel_modal_label">Are you sure to cancel?</h4>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body" id="cancel_modal_body">
                        The appointment for <strong id="modal_service_name"></strong> held on <em id="modal_appointmentDateTime"></em> will be cancelled
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                        <button type="button" class="btn btn-danger" id="modal_cancel_btn" data-bs-dismiss="modal">Yes, cancel the appointment</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Modal for update of an appointment -->
        <div class="modal fade" id="update_modal" tabindex="-1" aria-labelledby="update_modal_label" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header">
                        <h4 class="modal-title" id="update_modal_label">Do you want to update this appointment?</h4>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body" id="update_modal_body">
                       The appointment for <strong id="update_modal_service_name"></strong> held on <em id="update_modal_appointmentDateTime"></em> will be updated.
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                        <button type="button" class="btn btn-primary" id="modal_update_btn" data-bs-dismiss="modal">Yes, update the appointment</button>
                    </div>
                </div>
            </div>
        </div>



        <h1 class="mt-4">My Appointments</h1>



        <c:choose>
            <c:when test="${appointmentsPairs != null && appointmentsPairs.size() > 0}">
                <!-- The customer has at least one appointment -->
                <div class="container mt-4 mb-4">
                    <div class="row">

                        <div class="col-lg-2 pr-4 pb-4">
                            <!-- sorting select -->
                            <label for="order_select" class="form-label fs-3 mt-2">Show : </label>
                            <select class="form-select form-select-lg fs-5 mb-4" id="order_select" aria-label=".form-select-lg example">
                                <option value="latestFirst">latest first</option>
                                <option value="oldestFirst">oldest first</option>
                            </select>


                            <!-- SearchBox -->
                            <label for="search_box" class="form-label fs-3">Filter : </label>
                            <input class="form-control fs-5 mb-3" id="search_box" type="text" placeholder="service, company..">


                            <!-- status checkboxes -->
                            <div class="container fs-5" id="filter_checkboxes_container">
                                <div class="form-check">
                                    <input class="form-check-input ckb_status" type="checkbox" value="all" id="ckb_all" checked>
                                    <label class="form-check-label" for="ckb_all">
                                        All
                                    </label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input ckb_status" type="checkbox" value="checked in" id="ckb_checked_in">
                                    <label class="form-check-label" for="ckb_checked_in">
                                        Checked in
                                    </label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input ckb_status" type="checkbox" value="confirmed" id="ckb_confirmed">
                                    <label class="form-check-label" for="ckb_confirmed">
                                        Confirmed
                                    </label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input ckb_status" type="checkbox" value="booked" id="ckb_booked">
                                    <label class="form-check-label" for="ckb_booked">
                                        Booked
                                    </label>
                                </div>
                            </div>
                        </div>


                        <div class="col-lg-10">
                            <div class="container mb-10" id="appointment_list">
                                <c:forEach var="appointment" items="${appointmentsPairs}">
                                    <div class="row">

                                        <!-- index 0 -->
                                        <jsp:include page="/jsp/include/appointment_card.jsp">
                                            <jsp:param name="appointmentId" value="${appointment[0].getID()}" />
                                            <jsp:param name="serviceName" value="${servicesMap.get(appointment[0].getServiceID()).getName()}" />
                                            <jsp:param name="companyName" value="${companyNamesMap.get(appointment[0].getServiceID())}" />
                                            <jsp:param name="appointmentDateTime" value="${appointment[0].getDatetime()}" />
                                            <jsp:param name="status" value="${appointment[0].getStatus().toString().toLowerCase().replace('_',' ')}" />
                                        </jsp:include>

                                        <!-- index 1 -->
                                        <jsp:include page="/jsp/include/appointment_card.jsp">
                                            <jsp:param name="appointmentId" value="${appointment[1].getID()}" />
                                            <jsp:param name="serviceName" value="${servicesMap.get(appointment[1].getServiceID()).getName()}" />
                                            <jsp:param name="companyName" value="${companyNamesMap.get(appointment[1].getServiceID())}" />
                                            <jsp:param name="appointmentDateTime" value="${appointment[1].getDatetime()}" />
                                            <jsp:param name="status" value="${appointment[1].getStatus().toString().toLowerCase().replace('_',' ')}" />
                                        </jsp:include>

                                    </div>
                                </c:forEach>

                                <c:choose>
                                    <c:when test="${lastAppointment != null}">
                                        <!-- the appointments number was odd, display the last appointment -->
                                        <div class="row">
                                            <jsp:include page="/jsp/include/appointment_card.jsp">
                                                <jsp:param name="appointmentId" value="${lastAppointment.getID()}" />
                                                <jsp:param name="serviceName" value="${servicesMap.get(lastAppointment.getServiceID()).getName()}" />
                                                <jsp:param name="companyName" value="${companyNamesMap.get(lastAppointment.getServiceID())}" />
                                                <jsp:param name="appointmentDateTime" value="${lastAppointment.getDatetime()}" />
                                                <jsp:param name="status" value="${lastAppointment.getStatus().toString().toLowerCase().replace('_',' ')}" />
                                            </jsp:include>
                                        </div>
                                    </c:when>
                                    <c:otherwise><!-- the appointments number was even, nothing else to show --></c:otherwise>
                                </c:choose>
                            </div>
                        </div>


                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <!-- The customer has no appointments -->
                <h2 class="font-monospace" id="no_appo_txt">NO APPOINTMENTS BOOKED</h2>
            </c:otherwise>
        </c:choose>


        <button class="btn btn-primary btn-lg position-fixed bottom-0 end-0 m-4" type="button" id="add_app_btn"><i class="fa-solid fa-plus"></i> Book an appointment</button>



        <!-- show error message-->
        <p><c:out value = "${errorMessage}"/></p>

        <p>${MyErrorMessage}</p>

        <p>${newAppointmentMessage}</p>


    </div>
</main>

<c:import url="/jsp/include/foot.jsp"/>

<!-- JQuery -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
<!-- MomentJs to format datetimes-->
<script src="https://momentjs.com/downloads/moment-with-locales.min.js"></script>
<!-- script -->
<script src="<c:url value="/js/customer/index.js"/>"></script>

</body>
</html>