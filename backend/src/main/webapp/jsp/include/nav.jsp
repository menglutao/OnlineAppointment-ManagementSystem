<%@page isELIgnored="false"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<header>
    <!-- Fixed navbar -->
    <nav class="navbar navbar-expand-md navbar-dark fixed-top bg-dark">
        <div class="container">
            <span class="navbar-brand">Appointments app</span>
            <div class="dropdown text-end">
                <a href="#" class="d-block link-light text-decoration-none dropdown-toggle" id="dropdownUser1" data-bs-toggle="dropdown" aria-expanded="false">
                    <img src="<c:url value="/resources/user.jpg"/>" alt="mdo" width="32" height="32" class="rounded-circle">
                </a>
                <ul class="dropdown-menu dropdown-menu-end text-small" aria-labelledby="dropdownUser1">
                    <li><a class="dropdown-item" href="<c:url value="/profile"/>">Profile</a></li>
                    <li><hr class="dropdown-divider"></li>
                    <li><a class="dropdown-item" href="<c:url value="/logout"/>">Sign out</a></li>
                </ul>
            </div>
        </div>
    </nav>
</header>