<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page isELIgnored="false"%>

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>Registration</title>

    <%@ include file="/jsp/include/head.jsp" %>

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/intl-tel-input/17.0.8/css/intlTelInput.css"/>
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.8.0/dist/leaflet.css" integrity="sha512-hoalWLoI8r4UszCkZ5kL8vayOGVae1oxXe/2A4AO6J9+580uKHDO3JdHb7NzwwzK5xr/Fs0W40kiNHxM9vyTtQ==" crossorigin=""/>

    <link href="<c:url value="/css/registration.css"/>" rel="stylesheet">
</head>
<body class="bg-light">

<div class="container">
    <main>
        <div class="py-5 text-center">
            <h2>Registration form</h2>
        </div>
        <div class="row g-5">
            <div class="col-md-8 col-lg-10 offset-md-2 offset-lg-1 ">
                <h4 class="mb-3">General information</h4>
                <form id="registrationForm" method="POST" action="<c:url value="/register"/>" novalidate>
                    <div class="row g-3">

                        <div class="col-12">
                            <label for="role" class="form-label">Role</label>
                            <select class="form-select" id="role" name="role" required>
                                <option value="customer">Customer</option>
                                <option value="admin">Company's admin</option>
                            </select>
                        </div>

                        <div class="col-12">
                            <label for="email" class="form-label">Email</label>
                            <input type="email" class="form-control" id="email" name="email" required>
                        </div>

                        <div class="col-12">
                            <label for="name" class="form-label">Name</label>
                            <input type="text" class="form-control" id="name" name="name" required>
                        </div>

                        <div class="col-12">
                            <label for="phone" class="form-label">Telephone</label>
                            <input id="phone" type="text" name="phone" class="form-control" required/>
                        </div>

                        <div class="col-sm-6">
                            <label for="password" class="form-label">Password</label>
                            <input id="password" type="password" class="form-control"  name="password" required/>
                        </div>

                        <div class="col-sm-6">
                            <label for="passwordCheck" class="form-label">Confirmation password</label>
                            <input type="password" class="form-control"  name="passwordCheck" id="passwordCheck" data-v-equal="#password" required/>
                        </div>
                    </div>

                    <hr class="my-4">
                    <div id="companyDetails">
                        <h4 class="mb-3">Company information</h4>

                        <div class="row gy-3">
                            <div class="col-12">
                                <label for="companyEmail" class="form-label">Email</label>
                                <input type="email" class="form-control" id="companyEmail" name="companyEmail"/>

                            </div>

                            <div class="col-12">
                                <label for="companyName" class="form-label">Name</label>
                                <input type="text" class="form-control" id="companyName" name="companyName"/>

                            </div>

                            <div class="col-12">
                                <label for="companyPhone" class="form-label">Telephone</label>
                                <input id="companyPhone" type="text" name="companyPhone" class="form-control" />

                            </div>

                            <div class="col-12">
                                <label for="companyAddress" class="form-label">Address</label>
                                <input type="text" id="companyAddress" name="companyAddress" class="form-control"/>

                            </div>

                            <div class="col-12">
                                <input id="companyLat" name="companyLat" type="hidden"/>
                                <input id="companyLon" name="companyLon" type="hidden"/>

                                <div id="map"></div>
                            </div>
                        </div>

                        <hr class="my-4">
                    </div>

                    <c:choose>
                        <c:when test="${not empty errorMessage}">
                            <div class="alert alert-danger" role="alert">
                                <i class="fa-solid fa-triangle-exclamation fa-border" style="--fa-border-color: transparent"></i> <c:out value = "${errorMessage}"/>
                            </div>
                        </c:when>
                    </c:choose>

                    <button class="w-100 btn btn-primary btn-lg" type="submit">Register</button>
                </form>
            </div>
        </div>
    </main>

    <footer class="my-5 text-muted text-center text-small">
        <p class="mb-1">Already registered? <a href="<c:url value="/login"/>">Login</a></p>
    </footer>

    <c:import url="/jsp/include/foot.jsp"/>
    <script src="https://cdn.jsdelivr.net/npm/@emretulek/jbvalidator"></script>
    <script src="https://unpkg.com/leaflet@1.8.0/dist/leaflet.js" integrity="sha512-BB3hKbKWOc9Ez/TAwyWxNXeoV9c1v6FIeYiBieIWkpLjauysF18NzgR1MBNBXf8/KABdlkX68nAhlwcDFLGPCQ==" crossorigin=""></script>

    <script src="<c:url value="/js/registration.js"/>"></script>
</div>
</body>
</html>