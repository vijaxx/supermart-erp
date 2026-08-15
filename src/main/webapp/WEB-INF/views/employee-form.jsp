<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="_header.jspf" %>
<h1><c:choose><c:when test="${not empty employee}">Edit employee</c:when><c:otherwise>Add employee</c:otherwise></c:choose></h1>
<c:if test="${not empty error}"><div class="error"><c:out value="${error}"/></div></c:if>
<div class="form-box">
<form method="post" action="${pageContext.request.contextPath}/employees">
    <c:if test="${not empty employee}"><input type="hidden" name="id" value="${employee.id}"></c:if>
    <label for="fullName">Full name</label>
    <input type="text" id="fullName" name="fullName" value="<c:out value='${employee.fullName}'/>" required>
    <label for="email">Email</label>
    <input type="email" id="email" name="email" value="<c:out value='${employee.email}'/>" required>
    <label for="departmentId">Department</label>
    <select id="departmentId" name="departmentId" required>
        <c:forEach var="d" items="${departments}">
            <option value="${d.id}" ${employee != null && employee.departmentId == d.id ? 'selected' : ''}><c:out value="${d.name}"/></option>
        </c:forEach>
    </select>
    <label for="salary">Salary (INR)</label>
    <input type="number" step="0.01" id="salary" name="salary" value="${employee.salary}" required>
    <label for="joiningDate">Joining date</label>
    <input type="date" id="joiningDate" name="joiningDate" value="${employee.joiningDate}" required>
    <button class="btn" type="submit" style="margin-top:16px;">Save</button>
    <a class="btn secondary" href="${pageContext.request.contextPath}/employees">Cancel</a>
</form>
</div>
<%@ include file="_footer.jspf" %>
