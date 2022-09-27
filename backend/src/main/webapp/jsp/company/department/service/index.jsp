<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@page isELIgnored="false" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Service</title>
    <%@ include file="/jsp/include/head.jsp" %>
</head>
<div>
    <%@ include file="/jsp/include/nav.jsp" %>
    <main class="flex-shrink-0">
        <div class="container">

            <h4 class="mt-4">
                <c:choose>
                    <c:when test="${fn:contains(requestScope['jakarta.servlet.forward.request_uri'], 'create')}">
                        Create Service
                    </c:when>
                    <c:otherwise>
                        Service Information
                    </c:otherwise>
                </c:choose>
            </h4>

            <form method="POST" action="<c:out value = "${requestScope['jakarta.servlet.forward.request_uri']}"/>" class="needs-validation" id="serviceForm" novalidate>

                <div class="form-group">
                    <label for="name">Name</label>
                    <input type="text" class="form-control" id="name" name="name" value="${service.name}" <c:if test="${role.toString() eq 'secretary'}">readonly</c:if> required/>
                    <div class="invalid-feedback">
                        Please specify the service name.
                    </div>
                </div>


                <div class="form-group">
                    <label for="description">Description</label>
                    <textarea class="form-control" id="description" name="description" rows="3"  <c:if test="${role.toString() eq 'secretary'}">readonly</c:if> required><c:out value="${service.description}"/></textarea>
                    <div class="invalid-feedback">
                        Please provide a description for the service.
                    </div>
                </div>

                <div class="form-group">
                    <span class="col-md-2 control-label">Duration</span>
                    <div class="col-md-12">
                        <div class="form-group row">
                            <div class="col-md-4">
                                <input type="number" class="form-control" id="hours" name="hours" min="0" <c:if test="${not fn:contains(requestScope['jakarta.servlet.forward.request_uri'], 'create')}">readonly</c:if> required>
                                <small class="form-text text-muted">Hours</small>
                                <div class="invalid-feedback">
                                    Please provide a valid duration.
                                </div>
                            </div>
                            <div class="col-md-4">
                                <input type="number" class="form-control" id="minutes" name="minutes" min="0" <c:if test="${not fn:contains(requestScope['jakarta.servlet.forward.request_uri'], 'create')}">readonly</c:if> required>
                                <small class="form-text text-muted">Minutes</small>
                                <div class="invalid-feedback">
                                    Please provide a valid duration.
                                </div>
                            </div>
                            <div class="col-md-4">
                                <input type="number" class="form-control" id="seconds" name="seconds" min="0" <c:if test="${not fn:contains(requestScope['jakarta.servlet.forward.request_uri'], 'create')}">readonly</c:if> required>
                                <small class="form-text text-muted">Seconds</small>
                                <div class="invalid-feedback">
                                    Please provide a valid duration.
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <input id="oldDuration" type="hidden" value="${service.duration}" />
                <!--<div class="form-group">
                    <label for="duration">Description</label>
                    <input class="form-control" id="duration" name="duration" value="" type="number"
                            />
                    <div class="invalid-feedback">
                        Please provide a valid duration.
                    </div>
                </div>-->


                <c:if test="${role.toString() ne 'secretary'}">
                    <button type="submit" class="btn btn-primary mt-2"><c:choose>
                        <c:when test="${fn:contains(requestScope['jakarta.servlet.forward.request_uri'], 'create')}">
                            Create
                        </c:when>
                        <c:otherwise>
                            Update
                        </c:otherwise>
                    </c:choose></button>
                </c:if>
            </form>

            <c:choose>
            <c:when test="${not fn:contains(requestScope['jakarta.servlet.forward.request_uri'], 'create')}">

            <h4 class="mt-3">
                Timeslots
                <c:if test="${role.toString() ne 'secretary'}">
                    <a class="btn btn-success btn-sm float-end" href="<c:url value="/company/department/"/>${service.departmentID}/service/${service.ID}/timeslot/create">
                        <i class="fa fa-plus fa-border" style="--fa-border-color: transparent"></i>New
                    </a>
                </c:if>
            </h4>

            <div class="row">
                <table class="table table-hover">
                    <thead>
                    <tr>
                        <th scope="col">Datetime</th>
                        <th scope="col">No. places</th>
                        <th scope="col"></th>
                    </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${timeslots}" var="timeslot">
                            <tr>
                                <td><fmt:formatDate value="${timeslot.datetime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                                <td><c:out value="${timeslot.places}"/></td>
                                <td>
                                    <div class="float-start">
                                        <c:if test="${role.toString() ne 'secretary'}">
                                            <a href="<c:url value="/company/department/"/>${service.departmentID}/service/${service.ID}/timeslot/${timeslot.datetime.getTime()}/delete"><i class="fa-solid fa-trash fa-border" style="--fa-border-color: transparent; color: red"></i></a>
                                        </c:if>
                                        <a href="<c:url value="/company/department/"/>${service.departmentID}/service/${service.ID}/timeslot/${timeslot.datetime.getTime()}"><i class="fa-solid fa-pencil fa-border" style="--fa-border-color: transparent; color: black"></i></a>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
            </c:when>
            </c:choose>

            <c:choose>
                <c:when test="${not empty errorMessage}">
                    <div class="alert alert-danger mt-2" role="alert">
                        <i class="fa-solid fa-triangle-exclamation fa-border" style="--fa-border-color: transparent"></i> <c:out
                            value="${errorMessage}"/>
                    </div>
                </c:when>
            </c:choose>

            <div class="row mb-2">
                <div class="col-sm-2 col-md-2">
                    <c:choose>
                        <c:when test="${fn:contains(requestScope['jakarta.servlet.forward.request_uri'], 'create')}">
                            <a href="${fn:replace(requestScope['jakarta.servlet.forward.request_uri'], '/service/create', '')}" class="btn btn-secondary float-start mt-2"><i class="fa-solid fa-angle-left fa-border" style="--fa-border-color: transparent"></i> Department</a>
                        </c:when>
                        <c:otherwise>
                            <a  href="<c:url value="/company/department"/>/${service.departmentID}" class="btn btn-secondary float-start"><i class="fa-solid fa-angle-left fa-border" style="--fa-border-color: transparent"></i> Department</a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
    </main>
</div>

<c:import url="/jsp/include/foot.jsp"/>
<script src="<c:url value="/js/bootstrap_validation.js"/>"></script>
<script src="<c:url value="/js/company/department/service/index.js"/>"></script>

</body>
</html>