<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page isELIgnored="false" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <%--<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"> --%>
    <title>Department</title>
    <%@ include file="/jsp/include/head.jsp" %>

</head>
<body class="d-flex flex-column h-100">


<%@ include file="/jsp/include/nav.jsp" %>

<main class="flex-shrink-0">
    <div class="container">
        <h3 class="mt-4">Department dashboard</h3>

        <c:choose>
            <c:when test="${not empty errorMessage}">
                <div class="alert alert-danger mt-2" role="alert">
                    <i class="fa-solid fa-triangle-exclamation fa-border" style="--fa-border-color: transparent"></i> <c:out value="${errorMessage}"/>
                </div>
            </c:when>
        </c:choose>

        <div class="card">
            <div class="card-header">
                Department information

                <c:if test="${role.toString() ne 'secretary'}">
                    <a href="<c:url value="/company/department/${dep.getID()}/profile"/>">
                        <i class="fa fa-cog"></i>
                    </a>
                </c:if>
            </div>
            <div class="card-body">
                <h5 class="card-title"><c:out value="${dep.getName()}"/></h5>

                <p class="card-text mt-1"><i class="fa-solid fa-user fa-border" style="--fa-border-color: transparent"></i>${dep.getManager()}</p>
                <p class="card-text"><i class="fa-solid fa-pen-to-square fa-border" style="--fa-border-color: transparent"></i>${dep.getDescription()}</p>

            </div>
        </div>

        <h4 class="mt-3">
            Services

            <c:if test="${role.toString() ne 'secretary'}">
                <a class="btn btn-sm btn-success float-end"
                   href="<c:url value="/company/department/"/>${dep.ID}/service/create">
                    <i class="fa fa-plus fa-border" style="--fa-border-color: transparent"></i>New
                </a>
            </c:if>

        </h4>

        <div class="row">
            <c:forEach items="${services}" var="service">
                <div class="col-md-6 col-sm-12 my-2">
                    <div class="card">
                        <div class="card-header">
                                ${service.name}
                        </div>
                        <div class="card-body">
                            <span class="rawDuration d-none">${service.duration}</span>
                            <p class="card-text mb-1"><i class="fa-solid fa-hourglass fa-border" style="--fa-border-color: transparent"></i>
                            </p>
                            <p class="card-text mt-1"><i class="fa-solid fa-pen-to-square fa-border" style="--fa-border-color: transparent"></i> ${service.description}
                            </p>
                            <div class="float-end">
                                <a href="<c:url value="/company/department/"/>${service.departmentID}/service/${service.ID}"
                                   class="btn btn-secondary btn-sm">
                                    <i class="fa fa-eye fa-border" style="--fa-border-color: transparent"></i> Show
                                </a>
                                <c:if test="${role.toString() ne 'secretary'}">
                                    <a href="<c:url value="/company/department/"/>${service.departmentID}/service/${service.ID}/delete"
                                       class="btn btn-danger btn-sm">
                                        <i class="fa fa-trash fa-border" style="--fa-border-color: transparent"></i>
                                        Delete
                                    </a>
                                </c:if>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>

        <c:if test="${role.toString() ne 'secretary'}">

            <h4 class="mt-3">
                Secretaries
                <button type="button" class="btn btn-success btn-sm float-end" data-bs-toggle="modal"
                        data-bs-target="#secretaryModal">
                    <i class="fa fa-plus fa-border" style="--fa-border-color: transparent"></i>New
                </button>
            </h4>

            <div class="row">
                <table class="table table-hover table-responsive">
                    <thead>
                    <tr>
                        <th scope="col">Email</th>
                        <th scope="col">Name</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${secretaries}" var="secretary">
                        <tr>
                            <td>${secretary.email}</td>
                            <td>${secretary.name}</td>
                            <td>
                                <form method="POST"
                                      action="<c:url value="/company/department/"/>${dep.ID}/secretary/delete">
                                    <input type="hidden" id="secretaryId" name="secretary"
                                           value="${secretary.email}">
                                    <button class="btn btn-link" type="submit">
                                        <i class="fa-solid fa-trash fa-border" style="--fa-border-color: transparent; color: red;"></i>
                                    </button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>

            <!-- Modal -->
            <div class="modal fade" id="secretaryModal" tabindex="-1" aria-labelledby="secretaryModalLabel" aria-hidden="true">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <form method="POST" action="<c:url value="/company/department/"/>${dep.ID}/secretary/add">
                            <div class="modal-header">
                                <h5 class="modal-title" id="exampleModalLabel">Add new secretary</h5>
                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                            </div>
                            <div class="modal-body">
                                <select name="new-secretary" style="width: 100%">
                                    <c:forEach items="${customers}" var="customer">
                                        <option value="${customer.email}">${customer.email} - ${customer.name}</option>
                                    </c:forEach>
                                </select>

                                <div id="secretaryErrorMessage" class="alert alert-danger mt-2 d-none" role="alert">
                                    <i class="fa-solid fa-triangle-exclamation fa-border" style="--fa-border-color: transparent"></i> Invalid eligible secretary
                                </div>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                                <button type="submit" class="btn btn-primary">Add secretary</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>


            <div class="row mb-2">
                <div class="col-sm-2 col-md-2">
                    <a href="<c:url value="/company"/>" class="btn btn-secondary float-start"><i class="fa-solid fa-angle-left fa-border" style="--fa-border-color: transparent"></i> Company</a>
                </div>
            </div>

        </c:if>
    </div>

</main>

<c:import url="/jsp/include/foot.jsp"/>
<script src="<c:url value="/js/company/department/index.js"/>"></script>
</body>
</html>