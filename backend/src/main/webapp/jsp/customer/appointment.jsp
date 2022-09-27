<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page isELIgnored="false"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8">
         <meta name="viewport" content="width=device-width, initial-scale=1">

        <title>
            <c:choose>
                <c:when test="${fn:contains(requestScope['jakarta.servlet.forward.request_uri'], 'new')}">
                    Book appointment
                </c:when>
                <c:otherwise>
                    Update appointment
                </c:otherwise>
            </c:choose>
        </title>
        <%@ include file="/jsp/include/head.jsp" %>

    </head>
    <body class="d-flex flex-column h-100">
    <%@ include file="/jsp/include/nav.jsp" %>

    <!-- Begin page content -->
    <main class="flex-shrink-0">
        <div class="container">
            <h4 class="mt-4">
                <c:choose>
                    <c:when test="${fn:contains(requestScope['jakarta.servlet.forward.request_uri'], 'new')}">
                        Book appointment
                    </c:when>
                    <c:otherwise>
                        Update appointment
                    </c:otherwise>
                </c:choose>
            </h4>

            <form method = "POST" action="<c:out value = "${requestScope['jakarta.servlet.forward.request_uri']}"/>" class="needs-validation" novalidate>
                <div class="form-group mt-2">
                    <label for="companies" class="form-label">Company</label>
                    <select name ="companies" id="companies" class="form-control" <c:if test="${not fn:contains(requestScope['jakarta.servlet.forward.request_uri'], 'new')}">disabled</c:if>  required>
                        <c:choose>
                            <c:when test="${fn:contains(requestScope['jakarta.servlet.forward.request_uri'], 'new')}">
                                <option hidden disabled selected></option>

                                <c:forEach items="${companies}" var="company">
                                    <option value="${company.getName()}">${company.getName()}</option>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <option value="${companyName}" selected>${companyName}</option>
                            </c:otherwise>
                        </c:choose>
                    </select>
                    <div class="invalid-feedback">
                        Please select a company.
                    </div>
                </div>
                <div class="form-group mt-2">
                    <label for="services" class="form-label">Service</label>
                    <select name ="services" class="form-control" id = "services" <c:if test="${not fn:contains(requestScope['jakarta.servlet.forward.request_uri'], 'new')}">disabled</c:if> required>
                        <c:choose>
                            <c:when test="${fn:contains(requestScope['jakarta.servlet.forward.request_uri'], 'new')}">
                                <option hidden disabled selected></option>
                            </c:when>
                            <c:otherwise>
                                <option value="${serviceName}" selected>${serviceName}</option>
                            </c:otherwise>
                        </c:choose>
                    </select>
                    <div class="invalid-feedback">
                        Please select a service.
                    </div>
                </div>

                <div class="form-group mt-2">
                    <label for="timeslots" class="form-label">Timeslot</label>
                    <select name="datetime_long" id="timeslots" class="form-control" required>
                        <c:choose>
                            <c:when test="${fn:contains(requestScope['jakarta.servlet.forward.request_uri'], 'new')}">
                                <option hidden disabled selected></option>
                            </c:when>
                            <c:otherwise>
                                <c:forEach items="${timeslots}" var="timeslot">
                                    <option value="${timeslot.getDatetimeLong()}">${timeslot.getDatetime()}</option>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </select>
                    <div class="invalid-feedback">
                        Please select a timeslot.
                    </div>
                </div>

                <c:choose>
                    <c:when test="${not fn:contains(requestScope['jakarta.servlet.forward.request_uri'], 'new')}">
                        <input type="hidden" name="appointmentId" id="appointmentId" value="${appointmentId}">
                    </c:when>
                </c:choose>

                <c:choose>
                    <c:when test="${not empty errorMessage}">
                        <div class="alert alert-danger mt-2" role="alert">
                            <i class="fa-solid fa-triangle-exclamation fa-border" style="--fa-border-color: transparent"></i> <c:out value="${errorMessage}"/>
                        </div>
                    </c:when>
                </c:choose>

                <button type="submit" class="btn btn-primary mt-2">
                    <c:choose>
                        <c:when test="${fn:contains(requestScope['jakarta.servlet.forward.request_uri'], 'new')}">
                            Book
                        </c:when>
                        <c:otherwise>
                            Update
                        </c:otherwise>
                    </c:choose>
                </button>

                <a class="btn btn-secondary mt-2" href="<c:url value="/customer"/>">
                    <i class="fa fa-xmark" style="--fa-border-color: transparent"></i> Cancel
                </a>
            </form>
        </div>
    </main>


    <c:import url="/jsp/include/foot.jsp"/>
    <script src="<c:url value="/js/bootstrap_validation.js"/>"></script>
    <script src="<c:url value="/js/customer/appointment.js"/>"></script>

    </body>
</html>

