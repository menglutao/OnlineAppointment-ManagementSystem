<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page isELIgnored="false"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Department</title>
    <%@ include file="/jsp/include/head.jsp" %>
</head>
<body>
<jsp:include page="/jsp/include/header.jsp">
    <jsp:param name="email" value="${email}" />
</jsp:include>

<h1>Add new secretary</h1>

<form method="POST" action="<c:url value="${actionUrl}"/>">
    <select name="new-secretary">
        <c:forEach items="${customers}" var="customer">
            <option value="${customer.email}">${customer.email} - ${customer.name}</option>
        </c:forEach>
        <br/><br/>
    </select>

    <button type="submit">Add</button>
</form><br/>

<p><c:out value = "${errorMessage}"/></p>

<c:import url="/jsp/include/foot.jsp"/>
</body>
</html>