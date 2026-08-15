<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<% response.setStatus(403); %>
<%@ include file="_header.jspf" %>
<h1>403 - Access denied</h1>
<div class="error">
    Your account (<c:out value="${sessionScope.authenticatedUser.role}"/>) does not have permission to view
    <c:out value="${deniedPath}"/>. This area is restricted to ADMIN users.
</div>
<p><a class="btn" href="${pageContext.request.contextPath}/dashboard">Back to dashboard</a></p>
<%@ include file="_footer.jspf" %>
