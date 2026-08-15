<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="_header.jspf" %>
<h1><c:choose><c:when test="${not empty product}">Edit product</c:when><c:otherwise>Add product</c:otherwise></c:choose></h1>
<c:if test="${not empty error}"><div class="error"><c:out value="${error}"/></div></c:if>
<div class="form-box">
<form method="post" action="${pageContext.request.contextPath}/products">
    <c:if test="${not empty product}"><input type="hidden" name="id" value="${product.id}"></c:if>
    <label for="name">Name</label>
    <input type="text" id="name" name="name" value="<c:out value='${product.name}'/>" required>
    <label for="sku">SKU</label>
    <input type="text" id="sku" name="sku" value="<c:out value='${product.sku}'/>" required>
    <label for="categoryId">Category</label>
    <select id="categoryId" name="categoryId" required>
        <c:forEach var="cat" items="${categories}">
            <option value="${cat.id}" ${product != null && product.categoryId == cat.id ? 'selected' : ''}><c:out value="${cat.name}"/></option>
        </c:forEach>
    </select>
    <label for="supplierId">Supplier</label>
    <select id="supplierId" name="supplierId" required>
        <c:forEach var="s" items="${suppliers}">
            <option value="${s.id}" ${product != null && product.supplierId == s.id ? 'selected' : ''}><c:out value="${s.name}"/></option>
        </c:forEach>
    </select>
    <label for="unitPrice">Unit price (INR)</label>
    <input type="number" step="0.01" id="unitPrice" name="unitPrice" value="${product.unitPrice}" required>
    <label for="stockQuantity">Stock quantity</label>
    <input type="number" id="stockQuantity" name="stockQuantity" value="${product.stockQuantity}" required>
    <label for="reorderLevel">Reorder level</label>
    <input type="number" id="reorderLevel" name="reorderLevel" value="${product.reorderLevel}" required>
    <button class="btn" type="submit" style="margin-top:16px;">Save</button>
    <a class="btn secondary" href="${pageContext.request.contextPath}/products">Cancel</a>
</form>
</div>
<%@ include file="_footer.jspf" %>
