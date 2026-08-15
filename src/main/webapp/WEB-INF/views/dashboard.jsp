<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="_header.jspf" %>
<h1>Dashboard</h1>
<p>Welcome, <c:out value="${sessionScope.authenticatedUser.fullName}"/>.</p>
<div class="cards">
    <div class="card"><h3>Employees</h3><div class="value"><c:out value="${headcount}"/></div></div>
    <div class="card"><h3>Products</h3><div class="value"><c:out value="${productCount}"/></div></div>
    <div class="card"><h3>Low stock items</h3><div class="value"><c:out value="${lowStockCount}"/></div></div>
    <div class="card"><h3>Inventory value</h3><div class="value">&#8377;<fmt:formatNumber value="${inventoryValue}" pattern="#,##0.00"/></div></div>
</div>
<p style="margin-top:24px;">
    <a class="btn" href="${pageContext.request.contextPath}/products">View inventory</a>
    <c:if test="${sessionScope.authenticatedUser.admin}">
        <a class="btn secondary" href="${pageContext.request.contextPath}/employees">Manage employees</a>
        <a class="btn secondary" href="${pageContext.request.contextPath}/reports">View reports</a>
    </c:if>
</p>
<%@ include file="_footer.jspf" %>
