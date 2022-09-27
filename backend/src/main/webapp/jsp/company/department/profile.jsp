<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page isELIgnored="false" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Department</title>
    <%@ include file="/jsp/include/head.jsp" %>
</head>
<body class="d-flex flex-column h-100">
<%-- <jsp:include page="/jsp/include/header.jsp">
  <jsp:param name="email" value="${email}" />
  </jsp:include> --%>
<%@ include file="/jsp/include/nav.jsp" %>
<main class="flex-shrink-0">
    <div class="container">

        <c:if test="${actionUrl !=null}">
            <h3 class="mt-4">Update Department Profile</h3>
        </c:if>
        <c:if test="${actionUrl ==null}">
            <h3 class="mt-4">Add new Department </h3>
        </c:if>

        <form method="POST" action="<c:url value="${actionUrl}"/>" class="needs-validation" novalidate>
            <div class="form-group">
                <label for="name" class="form-label">Name:</label>
                <input class="form-control" id="name" name="name" value="<c:out value = "${dep.getName()}"/>" type="text" required/>
                <div class="invalid-feedback">
                    Please specify the department name.
                </div>
            </div>

            <div class="form-group">
                <label for="desc" class="form-label">Description:</label>
                <textarea class="form-control" id="desc" name="desc" rows="3" required><c:out value = "${dep.getDescription()}"/></textarea>
                <div class="invalid-feedback">
                    Please provide a description for the department.
                </div>
            </div>

            <div class="form-group">
                <label for="manager" class="form-label">Manager's Email:</label>
                <input class="form-control" id="manager" name="manager" value="<c:out value = "${dep.getManager()}"/>" type="text" required/>
                <div class="invalid-feedback">
                    Please provide the email of a registered manager.
                </div>
            </div>

            <c:choose>
                <c:when test="${not empty errorMessage}">
                    <div class="alert alert-danger mt-2" role="alert">
                        <i class="fa-solid fa-triangle-exclamation fa-border" style="--fa-border-color: transparent"></i> <c:out value="${errorMessage}"/>
                    </div>
                </c:when>
            </c:choose>

            <button type="submit" class="btn btn-primary mt-2">
                <i class="fa fa-check" style="--fa-border-color: transparent"></i> Submit
            </button>

            <c:if test="${actionUrl !=null}">
                <a class="btn btn-secondary mt-2" href="<c:url value="/company/department/${dep.getID()}"/>">
                    <i class="fa fa-xmark" style="--fa-border-color: transparent"></i> Cancel
                </a>
            </c:if>
            <c:if test="${actionUrl ==null}">
                <a class="btn btn-secondary mt-2" href="<c:url value="/company"/>">
                    <i class="fa fa-xmark" style="--fa-border-color: transparent"></i> Cancel
                </a>
            </c:if>

        </form>
    </div>
</main>
</body>

<c:import url="/jsp/include/foot.jsp"/>

<script src="<c:url value="/js/bootstrap_validation.js"/>"></script>
</html>