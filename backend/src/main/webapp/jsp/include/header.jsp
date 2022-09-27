<%@page isELIgnored="false"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div style="display: inline-block;"><span>You are logged in as ${param.email}</span></div>
<div style="display: inline-block; float: right;">
    <a href="<c:url value="/profile"/>">Edit profile</a>
    <a href="<c:url value="/logout"/>">Logout</a>
</div>
<hr/>