<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page isELIgnored="false"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="req" value="${pageContext.request}" />
<c:set var="url">${req.requestURL}</c:set>
<c:set var="uri" value="${req.requestURI}" />

<!DOCTYPE html>
<html lang="en">
    <head>
     <meta charset="utf-8">
     <meta name="viewport" content="width=device-width, initial-scale=1">
     <title>Customer</title>
     <%@ include file="/jsp/include/head.jsp" %>
     <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/intl-tel-input/17.0.8/css/intlTelInput.css"/>
     <link rel="stylesheet" href="https://unpkg.com/leaflet@1.8.0/dist/leaflet.css" integrity="sha512-hoalWLoI8r4UszCkZ5kL8vayOGVae1oxXe/2A4AO6J9+580uKHDO3JdHb7NzwwzK5xr/Fs0W40kiNHxM9vyTtQ==" crossorigin=""/>
     <link href="<c:url value="/css/registration.css"/>" rel="stylesheet">
      <script src="http://code.jquery.com/jquery-latest.min.js"></script>


        <script>
            function onSelectService(selectObject) {
                console.log(selectObject.value);
                var url = "${fn:substring(url, 0, fn:length(url) - fn:length(uri))}${req.contextPath}/customer/appointment/update?appintmentId=" + selectObject.value;
                $.get(url, function(responseJson) {
                    var $select = $("#dropdownlist_datetime");
                    $select.find("option").remove();
                    $.each(responseJson, function(index, timeslot) {
                        console.log(timeslot);
                        $("<option>").val(timeslot.serviceID + "---" + timeslot.datetimeLong).text(timeslot.datetime).appendTo($select);
                    });
                });
            };
        </script>


        <%@ include file="/jsp/include/head.jsp" %>
    </head>
    <body class="bg-light">
        <%@ include file="/jsp/include/nav.jsp" %>
        <br/>
        <div class="container">
            <main>
                <div class = "py-5 text-center">
                    <h2>Update Your Appointment</h2>
                </div>
                <div class="row g-5">
                    <form method="POST" action="<c:url value="/customer/appointment/update"/>" align="center" novalidate>
                        <div class="row g-3">
                           <input type="hidden" name="appointmentId" value="${appointmentId}">
                           <div class="col-12" >
                                <label for="name" class="form-label">Datetime</label>
                                <select name ="datetime" id="dropdownlist_datetime">
                                    <c:forEach items="${timeslots}" var="timeslot">
                                        <option value="${timeslot.getDatetimeLong()}">${timeslot.getDatetime()}</option>
                                    </c:forEach>
                                </select>
                           </div>
                        </div>
                        <button type="submit" class="w-80 btn btn-primary btn-lg">Submit</button>

                    </form>

                </div>
            </main>
        </div>
    <c:import url="/jsp/include/foot.jsp"/>
    </body>

</html>