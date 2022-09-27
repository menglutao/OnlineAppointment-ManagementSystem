<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/functions" prefix = "fn" %>
<%@page isELIgnored="false"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Timeslot</title>
    <%@ include file="/jsp/include/head.jsp" %>
</head>

<body class="d-flex flex-column h-100">
<%@ include file="/jsp/include/nav.jsp" %>

<!-- Begin page content -->
<main class="flex-shrink-0">
    <div class="container">
        <h3 class="mt-4">
            <c:choose>
                <c:when test="${fn:contains(requestScope['jakarta.servlet.forward.request_uri'], 'create')}">
                    Create timeslot
                </c:when>
                <c:otherwise>
                    Timeslot information
                </c:otherwise>
            </c:choose>
        </h3>

        <form class="needs-validation" method="POST" action="<c:out value = "${requestScope['jakarta.servlet.forward.request_uri']}"/>" novalidate>
            <div class="form-group">
                <div class="col-md-12">
                    <div class="form-group row">
                        <div class="col-md-6 col-sm-12">
                            <label for="datetime">Datetime</label>
                            <input type="datetime-local" value="<fmt:formatDate value="${timeslot.datetime}" pattern="yyyy-MM-dd" />T<fmt:formatDate value="${timeslot.datetime}" pattern="HH:mm:ss" />" <c:if test="${not fn:contains(requestScope['jakarta.servlet.forward.request_uri'], 'create')}">
                                   readonly
                                   </c:if> class="form-control" id="datetime" name="datetime" step="1" required>
                            <div class="invalid-feedback">
                                Please provide datetime of the timeslot
                            </div>
                        </div>
                        <div class="col-md-6 col-sm-12">
                            <label for="places">Places</label>
                            <input type="number" class="form-control" id="places" name="places" min="0" value="${timeslot.places}" <c:if test="${role.toString() eq 'secretary'}">readonly</c:if> required>
                            <div class="invalid-feedback">
                                Please provide the number of place available
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <c:if test="${role.toString() ne 'secretary'}">
                <button type="submit" class="btn btn-primary mt-2">
                    <c:choose>
                        <c:when test="${fn:contains(requestScope['jakarta.servlet.forward.request_uri'], 'create')}">
                            Create
                        </c:when>
                        <c:otherwise>
                            Update
                        </c:otherwise>
                    </c:choose>
                </button>
            </c:if>

        </form>

        <c:choose>
            <c:when test="${not empty errorMessage}">
                <div class="alert alert-danger mt-2" role="alert">
                    <i class="fa-solid fa-triangle-exclamation fa-border" style="--fa-border-color: transparent"></i> <c:out value="${errorMessage}"/>
                </div>
            </c:when>
        </c:choose>

        <c:choose>
            <c:when test="${not fn:contains(requestScope['jakarta.servlet.forward.request_uri'], 'create')}">

            <h4 class="mt-3">Appointments</h4>

            <div class="row">
                <form method="POST" action="${requestScope['jakarta.servlet.forward.request_uri']}/syncAppointments">
                    <table class="table table-hover">
                        <thead>
                        <tr>
                            <th scope="col">Customer</th>
                            <th scope="col">Status</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${appointments}" var="appointment">
                                <tr>
                                    <td><c:out value="${appointment.userID}"/></td>
                                    <td>
                                        <select name="${appointment.ID}-status">
                                            <option value="BOOKED" <c:if test="${appointment.status.toString()=='BOOKED'}">selected="selected"</c:if>>Booked</option>
                                            <option value="CONFIRMED" <c:if test="${appointment.status.toString()=='CONFIRMED'}">selected="selected"</c:if>>Confirmed</option>
                                            <option value="CHECKED_IN" <c:if test="${appointment.status.toString()=='CHECKED_IN'}">selected="selected"</c:if>>Checked in</option>
                                        </select>
                                    </td>
                                    <td>
                                        <a href="${requestScope['jakarta.servlet.forward.request_uri']}/appointment/${appointment.ID}/delete" class="float-start"><i class="fa-solid fa-trash fa-border" style="--fa-border-color: transparent; color: red"></i></a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>

                    <button type="submit" class="btn btn-primary">
                        Update
                    </button>
                </form>
            </div>

            </c:when>
        </c:choose>

        <div class="row mb-2">
            <div class="col-sm-2 col-md- mt-2">
                <a href="<c:out value = "${fn:substring(requestScope['jakarta.servlet.forward.request_uri'], 0, fn:indexOf(requestScope['jakarta.servlet.forward.request_uri'], '/timeslot'))}"/>" class="btn btn-secondary float-start"><i class="fa-solid fa-angle-left fa-border" style="--fa-border-color: transparent"></i> Service</a>
            </div>
        </div>
    </div>
</main>

<%@ include file="/jsp/include/foot.jsp" %>
<script src="<c:url value="/js/bootstrap_validation.js"/>"></script>

</body>
</html>
