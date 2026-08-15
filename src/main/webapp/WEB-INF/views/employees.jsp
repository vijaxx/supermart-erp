<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="_header.jspf" %>
<h1>Employees</h1>
<c:if test="${param.saved == '1'}"><div class="info">Employee saved.</div></c:if>
<c:if test="${param.deleted == '1'}"><div class="info">Employee deleted.</div></c:if>
<form method="get" action="${pageContext.request.contextPath}/employees" style="margin-bottom:16px; max-width:320px;">
    <input type="text" name="q" placeholder="Search by name" value="<c:out value='${query}'/>">
</form>
<p><a class="btn" href="${pageContext.request.contextPath}/employees/form">+ Add employee</a></p>
<table>
    <tr><th>Name</th><th>Email</th><th>Department</th><th>Salary</th><th>Joined</th><th></th></tr>
    <c:forEach var="e" items="${employees}">
        <tr>
            <td><c:out value="${e.fullName}"/></td>
            <td><c:out value="${e.email}"/></td>
            <td><c:out value="${e.departmentName}"/></td>
            <td>&#8377;<fmt:formatNumber value="${e.salary}" pattern="#,##0.00"/></td>
            <td><c:out value="${e.joiningDate}"/></td>
            <td>
                <a class="btn secondary" href="${pageContext.request.contextPath}/employees/form?id=${e.id}">Edit</a>
                <form class="inline" method="post" action="${pageContext.request.contextPath}/employees/delete" onsubmit="return confirm('Delete this employee?');">
                    <input type="hidden" name="id" value="${e.id}">
                    <button class="btn danger" type="submit">Delete</button>
                </form>
            </td>
        </tr>
    </c:forEach>
    <c:if test="${empty employees}"><tr><td colspan="6">No employees found.</td></tr></c:if>
</table>
<%@ include file="_footer.jspf" %>
