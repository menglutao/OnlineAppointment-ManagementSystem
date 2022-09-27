<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page isELIgnored="false"%>

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>Login</title>

    <%@ include file="/jsp/include/head.jsp" %>

    <!-- Custom styles for this template -->
    <link href="<c:url value="/css/login.css"/>" rel="stylesheet">
</head>
<body class="text-center">


<!-- Toast container for messages -->
<div aria-live="polite" aria-atomic="true" class="position-relative">

    <div class="toast-container position-fixed top-0 end-0 p-4">
        <div class="toast" id="error_toast_template" role="alert" aria-live="assertive" aria-atomic="true">

            <div class="d-flex">
                <div class="toast-body">
                    <strong class="errTitle"></strong>
                    </br>
                    <span class="errMessage"></span>
                </div>
                <button type="button" class="btn-close me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>

        </div>
    </div>

</div>



<main class="form-signin">
    <form action="<c:url value="/login"/>" method="POST" name="form_login">
        <h1 class="h3 mb-3 fw-normal">Appointments app</h1>

        <div class="form-floating">
            <input type="email" class="form-control" id="floatingInput" name="email" placeholder="name@example.com" required>
            <label for="floatingInput">Email address</label>
        </div>
        <div class="form-floating">
            <input type="password" class="form-control" id="floatingPassword" name="password" placeholder="Password" required>
            <label for="floatingPassword">Password</label>
        </div>

        <c:choose>
            <c:when test="${not empty errorMessage}">
                <div class="mb-2">
                    <small class="text-danger"><i class="fa-solid fa-triangle-exclamation fa-border" style="--fa-border-color: transparent"></i> <c:out value = "${errorMessage}"/></small>
                </div>
            </c:when>
        </c:choose>
        <!-- button not of type "submit" because the form submission is performed via Javascript, after API auth token retrieval-->
        <button class="w-100 btn btn-lg btn-primary" type="button" id="form_submit_btn">Sign in</button>

        <p class="mt-5 mb-3 text-muted">Not a member? <a href="<c:url value="/register"/>">Register</a></p>
    </form>
</main>

<c:import url="/jsp/include/foot.jsp"/>
<script src="<c:url value="/js/login.js"/>"></script>
</body>
</html>