<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page isELIgnored="false" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Company</title>
    <%@ include file="/jsp/include/head.jsp" %>
</head>
<body class="d-flex flex-column h-100">

<%--
<jsp:include page="/jsp/include/header.jsp">
  <jsp:param name="email" value="${email}" />
</jsp:include>
--%>

<%@ include file="/jsp/include/nav.jsp" %>

<!-- Begin page content -->
<main class="flex-shrink-0">
    <div class="container">

        <h3 class="mt-4">Company dashboard</h3>

        <c:choose>
            <c:when test="${not empty errorMessage}">
                <div class="alert alert-danger mt-2" role="alert">
                    <i class="fa-solid fa-triangle-exclamation fa-border" style="--fa-border-color: transparent"></i> <c:out
                        value="${errorMessage}"/>
                </div>
            </c:when>
        </c:choose>

        <c:set var="map_url" scope="session"
               value="https://www.google.com/maps/embed/v1/place?key=AIzaSyA0s1a7phLN0iaD6-UE7m4qP-z21pH0eSc&q=${company.getLat()},${company.getLon()}"/>

        <div class="card">
            <div class="card-header">
                Company information
                <a href="<c:url value="/company/profile"/>">
                    <i class="fa fa-cog"></i>
                </a>
            </div>
            <div class="card-body">
                <h5 class="card-title"><c:out value="${company.getName()}"/></h5>
                <div class="row">
                    <div class="col-lg-6 col-md-12">
                        <p class="card-text">
                            <i class="fa fa-map-location"></i> <c:out value="${company.getAddress()}"/>
                            <br/>
                            <i class="fa fa-phone"></i>
                            <a href="tel:<c:out value="${company.getPhone()}"/>">
                              <c:out value="${company.getPhone()}"/>
                            </a>
                            <br/>
                            <i class="fa fa-envelope"></i>
                            <a href="mailto:<c:out value="${company.getEmail()}"/>">
                              <c:out value="${company.getEmail()}"/>
                            </a>
                        </p>
                    </div>
                    <div class="col-lg-6 d-none d-md-block">
                        <div class="container-fluid">
                            <iframe src="${map_url}" frameborder="0"
                                    style="border:0; display: block; margin: 0 auto" allowfullscreen width="90%">
                            </iframe>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <h4 class="mt-3">
            Departments
            <a class="btn btn-sm btn-success float-end" href="<c:url value="/company/department/new"/>">
                <i class="fa fa-plus fa-border" style="--fa-border-color: transparent"></i>New
            </a>
        </h4>

        <div class="row">

            <c:forEach items="${departs}" var="dep">
                <div class="col-md-6 col-sm-12">
                    <div class="card">
                        <div class="card-header">
                                ${dep.getName()}
                        </div>
                        <div class="card-body">
                            <p class="card-text mb-1">${dep.getDescription()}</p>
                            <p class="card-text mt-1"><i class="fa-solid fa-user fa-border"
                                                         style="--fa-border-color: transparent"></i>
                                Manager: ${dep.getManager()}</p>
                            <div class="float-end">
                                <a href="<c:url value="/company/department/${dep.getID()}"/>"
                                   class="btn btn-secondary btn-sm">
                                    <i class="fa fa-eye fa-border" style="--fa-border-color: transparent"></i> Show
                                </a>
                                <a href="<c:url value="/company/department/${dep.getID()}/delete"/>"
                                   class="btn btn-danger btn-sm">
                                    <i class="fa fa-trash fa-border" style="--fa-border-color: transparent"></i> Delete
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>

        </div>
</main>


<c:import url="/jsp/include/foot.jsp"/>

</body>
</html>