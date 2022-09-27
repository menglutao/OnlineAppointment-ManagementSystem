<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page isELIgnored="false" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Customer</title>

    <%@ include file="/jsp/include/head.jsp" %>

    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.8.0/dist/leaflet.css"
          integrity="sha512-hoalWLoI8r4UszCkZ5kL8vayOGVae1oxXe/2A4AO6J9+580uKHDO3JdHb7NzwwzK5xr/Fs0W40kiNHxM9vyTtQ=="
          crossorigin=""/>
</head>
<body>

<%@ include file="/jsp/include/nav.jsp" %>

<!-- Begin page content -->
<main class="flex-shrink-0">
    <div class="container">
        <h3 class="mt-4">Update company profile</h3>

        <form method="POST" action="<c:url value="/company/profile"/>" class="needs-validation" novalidate>
            <div class="form-group">
                <label for="name" class="form-label">Name:</label>
                <input id="name" name="name" class="form-control" disabled
                       value="<c:out value = "${company.getName()}"/>"
                       type="text"
                />
            </div>

            <div class="form-group">
                <label for="address" class="form-label">Address</label>
                <input id="address" name="address" class="form-control"
                       value="<c:out value = "${company.getAddress()}"/>"
                       type="text" required
                />
                <div class="invalid-feedback">Please enter an address</div>
            </div>

            <div class="form-group">
                <label class="form-label">Choose location</label>

                <div id="map" style="height: 40vh;"></div>

                <input id="lat" name="lat" class="form-control"
                       value="<c:out value = "${company.getLat()}"/>"
                       type="hidden"/>

                <input id="lon" name="lon" class="form-control"
                       value="<c:out value = "${company.getLon()}"/>"
                       type="hidden"/>
            </div>


            <div class="form-group">
                <label for="phone" class="form-label">Phone</label>
                <input id="phone" name="phone" class="form-control"
                       value="<c:out value = "${company.getPhone()}"/>"
                       type="text" required
                />
                <div class="invalid-feedback">Please enter a phone number</div>
            </div>

            <div class="form-group">
                <label for="email" class="form-label">Email</label>
                <input id="email" name="email" class="form-control"
                       value="<c:out value = "${company.getEmail()}"/>"
                       type="text" required
                />
                <div class="invalid-feedback">Please enter an email address</div>
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

            <a class="btn btn-secondary mt-2" href="<c:url value="/company"/>">
                <i class="fa fa-xmark" style="--fa-border-color: transparent"></i> Cancel
            </a>
        </form>

        <c:import url="/jsp/include/foot.jsp"/>

        <script src="https://unpkg.com/leaflet@1.8.0/dist/leaflet.js"
                integrity="sha512-BB3hKbKWOc9Ez/TAwyWxNXeoV9c1v6FIeYiBieIWkpLjauysF18NzgR1MBNBXf8/KABdlkX68nAhlwcDFLGPCQ=="
                crossorigin=""></script>

        <script src="<c:url value="/js/company/profile.js"/>"></script>

    </div>
</main>


</body>
</html>