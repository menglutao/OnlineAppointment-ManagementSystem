<%@ page import="java.util.Enumeration" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page isELIgnored="false"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <title>Error</title>
        <%@ include file="/jsp/include/head.jsp" %>

        <link href="<c:url value="/css/error.css"/>" rel="stylesheet">
    </head>
    <body>

        <div class="page-wrap d-flex flex-row align-items-center">
            <div class="container">
                <div class="row justify-content-center">
                    <div class="col-md-12 text-center">
                        <span class="display-1 d-block"><c:out value="${param.code}"/><c:out value="${errorCode}"/></span>
                        <div class="mb-4 lead"><c:out value="${param.message}"/><c:out value="${errorMessage}"/></div>
                    </div>
                </div>
            </div>
        </div>

        <c:import url="/jsp/include/foot.jsp"/>
    </body>
</html>