<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="_header.jspf" %>
<h1><c:choose><c:when test="${lowStockOnly}">Low stock products</c:when><c:otherwise>Inventory</c:otherwise></c:choose></h1>
<c:if test="${param.saved == '1'}"><div class="info">Product saved.</div></c:if>
<c:if test="${param.deleted == '1'}"><div class="info">Product deleted.</div></c:if>
<c:if test="${param.adjusted == '1'}"><div class="info">Stock updated.</div></c:if>
<c:if test="${not empty error}"><div class="error"><c:out value="${error}"/></div></c:if>

<p>
    <a class="btn secondary" href="${pageContext.request.contextPath}/products">All products</a>
    <a class="btn secondary" href="${pageContext.request.contextPath}/products/low-stock">Low stock only</a>
    <c:if test="${sessionScope.authenticatedUser.admin}">
        <a class="btn" href="${pageContext.request.contextPath}/products/form">+ Add product</a>
    </c:if>
</p>

<c:if test="${not lowStockOnly}">
<form method="get" action="${pageContext.request.contextPath}/products" style="margin-bottom:16px; max-width:280px;">
    <select name="categoryId" onchange="this.form.submit()">
        <option value="0">All categories</option>
        <c:forEach var="cat" items="${categories}">
            <option value="${cat.id}" ${selectedCategory == cat.id ? 'selected' : ''}><c:out value="${cat.name}"/></option>
        </c:forEach>
    </select>
</form>
</c:if>

<table>
    <tr><th>SKU</th><th>Name</th><th>Category</th><th>Supplier</th><th>Price</th><th>Stock</th><th>Reorder at</th><th></th></tr>
    <c:forEach var="p" items="${products}">
        <tr>
            <td><c:out value="${p.sku}"/></td>
            <td><c:out value="${p.name}"/></td>
            <td><c:out value="${p.categoryName}"/></td>
            <td><c:out value="${p.supplierName}"/></td>
            <td>&#8377;<fmt:formatNumber value="${p.unitPrice}" pattern="#,##0.00"/></td>
            <td class="${p.lowStock ? 'low' : ''}"><c:out value="${p.stockQuantity}"/><c:if test="${p.lowStock}"> (low)</c:if></td>
            <td><c:out value="${p.reorderLevel}"/></td>
            <td>
                <form class="inline" method="post" action="${pageContext.request.contextPath}/products/adjust">
                    <input type="hidden" name="id" value="${p.id}">
                    <input type="hidden" name="delta" value="10">
                    <button class="btn secondary" type="submit" title="Receive 10 units">+10</button>
                </form>
                <form class="inline" method="post" action="${pageContext.request.contextPath}/products/adjust">
                    <input type="hidden" name="id" value="${p.id}">
                    <input type="hidden" name="delta" value="-1">
                    <button class="btn secondary" type="submit" title="Sell 1 unit">-1</button>
                </form>
                <c:if test="${sessionScope.authenticatedUser.admin}">
                    <a class="btn secondary" href="${pageContext.request.contextPath}/products/form?id=${p.id}">Edit</a>
                    <form class="inline" method="post" action="${pageContext.request.contextPath}/products/delete" onsubmit="return confirm('Delete this product?');">
                        <input type="hidden" name="id" value="${p.id}">
                        <button class="btn danger" type="submit">Delete</button>
                    </form>
                </c:if>
            </td>
        </tr>
    </c:forEach>
    <c:if test="${empty products}"><tr><td colspan="8">No products found.</td></tr></c:if>
</table>
<%@ include file="_footer.jspf" %>
