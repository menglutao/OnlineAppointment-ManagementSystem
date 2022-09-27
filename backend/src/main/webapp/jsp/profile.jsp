<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page isELIgnored="false"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>User profile</title>

    <%@ include file="/jsp/include/head.jsp" %>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/intl-tel-input/17.0.8/css/intlTelInput.css"/>
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.8.0/dist/leaflet.css" integrity="sha512-hoalWLoI8r4UszCkZ5kL8vayOGVae1oxXe/2A4AO6J9+580uKHDO3JdHb7NzwwzK5xr/Fs0W40kiNHxM9vyTtQ==" crossorigin=""/>
    <link href="<c:url value="/css/registration.css"/>" rel="stylesheet">
</head>

<body class="bg-light">

<div class="container">
    <main>
        <div class="py-5 text-center">
            <h2>User Information Update</h2>
        </div>
        <div class="row g-5">
            <form method ="POST" action="<c:url value="/profile"/>" novalidate>
                <div class="row g-3">
                    <div class="col-12">
                        <label for="name" class="form-label">Name</label>
                        <input id="name" class = "form-control" name="name" value="${sessionScope.name}" type="text" disabled/>
                    </div>
                    <div class="col-12">
                        <label for="phone" class="form-label">Telephone</label>
                        <input id="phone" class="form-control" name="phone" value="${sessionScope.phone}" type="text" disabled/>
                    </div>
                    <div class="col-sm-6">
                        <label for="password" class="form-label">Password</label>
                        <input id="password" type="password" class="form-control"  name="password" />
                    </div>

                    <div class="col-sm-6">
                        <label for="passwordCheck" class="form-label">Confirm password</label>
                        <input id="passwordCheck" class="form-control" name="passwordCheck" type="password" />
                    </div>

                    <hr class="my-4">
                    <button class="w-80 btn btn-primary btn-lg" type="submit">submit</button>
                    <p><c:out value = "${errorMessage}"/></p>

               </div>
            </form>
        </div>
    </main>
</div>


<%--
<h3>Update user info</h3>

<form method="POST" action="<c:url value="/profile"/>">
    <label for="name">name:</label>
    <input id="name" name="name" value="${sessionScope.name}" type="text"/><br/><br/>
    <label for="phone">phone:</label>
    <input id="phone" name="phone" value="${sessionScope.phone}" type="text"/><br/><br/>
    <label for="password">password:</label>
    <input id="password" name="password" type="password"/>
    <label for="passwordCheck">Repeat password:</label>
    <input id="passwordCheck" name="passwordCheck" type="password"/>
    <br/><br/>
    <button type="submit">Submit</button><br/>

</form>
<p><c:out value = "${errorMessage}"/></p>

--%>
<c:import url="/jsp/include/foot.jsp"/>


</body>
</html>