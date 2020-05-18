<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>Prog.kiev.ua</title>
</head>
<body>
<div align="center">
    <c:url value="/newpassword" var="newUrl" />
    <form  role="form"  action="${newUrl}" method="POST">
        Login:<br/><input type="text" name="login"><br/>
        Enter new password:<br/><input type="password" name="password"><br/>
        <input type="submit" />

        <c:if test="${param.error ne null}">
            <p>Wrong login or password!</p>
        </c:if>

    </form>
</div>
</body>
</html>