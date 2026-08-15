<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="_header.jspf" %>
<h1>Admin reports</h1>

<div class="cards">
    <div class="card"><h3>Total payroll</h3><div class="value">&#8377;<fmt:formatNumber value="${totalPayroll}" pattern="#,##0.00"/></div></div>
    <div class="card"><h3>Total inventory value</h3><div class="value">&#8377;<fmt:formatNumber value="${totalInventoryValue}" pattern="#,##0.00"/></div></div>
</div>

<h2>Employees per department (JOIN + GROUP BY)</h2>
<table>
    <tr><th>Department</th><th>Headcount</th><th>Total salary</th><th>Average salary</th><th>Max salary</th></tr>
    <c:forEach var="r" items="${departmentRows}">
        <tr>
            <td><c:out value="${r.departmentName}"/></td>
            <td><c:out value="${r.headcount}"/></td>
            <td>&#8377;<fmt:formatNumber value="${r.totalSalary}" pattern="#,##0.00"/></td>
            <td>&#8377;<fmt:formatNumber value="${r.averageSalary}" pattern="#,##0.00"/></td>
            <td>&#8377;<fmt:formatNumber value="${r.maxSalary}" pattern="#,##0.00"/></td>
        </tr>
    </c:forEach>
</table>

<h2 style="margin-top:32px;">Inventory valuation by category (JOIN + GROUP BY)</h2>
<table>
    <tr><th>Category</th><th>Products</th><th>Units in stock</th><th>Inventory value</th></tr>
    <c:forEach var="r" items="${categoryRows}">
        <tr>
            <td><c:out value="${r.categoryName}"/></td>
            <td><c:out value="${r.productCount}"/></td>
            <td><c:out value="${r.totalUnits}"/></td>
            <td>&#8377;<fmt:formatNumber value="${r.inventoryValue}" pattern="#,##0.00"/></td>
        </tr>
    </c:forEach>
</table>

<h2 style="margin-top:32px;">Low-stock report (products JOIN categories JOIN suppliers)</h2>
<table>
    <tr><th>Product</th><th>SKU</th><th>Category</th><th>Supplier</th><th>Supplier email</th><th>Stock</th><th>Reorder at</th><th>Shortfall</th></tr>
    <c:forEach var="r" items="${lowStockRows}">
        <tr>
            <td><c:out value="${r.productName}"/></td>
            <td><c:out value="${r.sku}"/></td>
            <td><c:out value="${r.categoryName}"/></td>
            <td><c:out value="${r.supplierName}"/></td>
            <td><c:out value="${r.supplierEmail}"/></td>
            <td class="low"><c:out value="${r.stockQuantity}"/></td>
            <td><c:out value="${r.reorderLevel}"/></td>
            <td><c:out value="${r.shortfall}"/></td>
        </tr>
    </c:forEach>
    <c:if test="${empty lowStockRows}"><tr><td colspan="8">Nothing is low on stock right now.</td></tr></c:if>
</table>
<%@ include file="_footer.jspf" %>
