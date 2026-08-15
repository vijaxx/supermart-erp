<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Sign in - Supermart ERP</title>
    <style>
        body { font-family: Arial, Helvetica, sans-serif; background: #14532d; display: flex; align-items: center; justify-content: center; height: 100vh; margin: 0; }
        .box { background: #fff; padding: 32px; border-radius: 8px; width: 320px; box-shadow: 0 4px 12px rgba(0,0,0,0.25); }
        h1 { font-size: 20px; margin-top: 0; color: #14532d; }
        label { display: block; margin: 12px 0 4px; font-weight: 600; font-size: 13px; }
        input { width: 100%; padding: 8px; border: 1px solid #d1d5db; border-radius: 4px; box-sizing: border-box; }
        button { margin-top: 16px; width: 100%; background: #16a34a; color: #fff; border: none; padding: 10px; border-radius: 4px; font-size: 15px; cursor: pointer; }
        .error { background: #fef2f2; color: #b91c1c; padding: 8px 10px; border-radius: 4px; margin-bottom: 10px; font-size: 13px; border: 1px solid #fecaca; }
        .info { background: #eff6ff; color: #1e40af; padding: 8px 10px; border-radius: 4px; margin-bottom: 10px; font-size: 13px; border: 1px solid #bfdbfe; }
        .demo { margin-top: 16px; font-size: 12px; color: #6b7280; }
    </style>
</head>
<body>
<div class="box">
    <h1>Supermart ERP</h1>
    <c:if test="${not empty error}"><div class="error"><c:out value="${error}"/></div></c:if>
    <c:if test="${not empty message}"><div class="info"><c:out value="${message}"/></div></c:if>
    <c:if test="${param.loggedOut == '1'}"><div class="info">You have been signed out.</div></c:if>
    <form method="post" action="${pageContext.request.contextPath}/login">
        <label for="username">Username</label>
        <input type="text" id="username" name="username" value="<c:out value='${username}'/>" autofocus required>
        <label for="password">Password</label>
        <input type="password" id="password" name="password" required>
        <button type="submit">Sign in</button>
    </form>
    <div class="demo">
        Demo accounts (seeded on first run): <br>
        admin / Admin@123 &nbsp;-&nbsp; staff / Staff@123
    </div>
</div>
</body>
</html>
