<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>Prog.kiev.ua</title>
</head>
<body>
<div align="center">
    <c:url value="/mail" var="mailUrl" />
    <form  role="form"  action="${mailUrl}" method="POST">
        <h3>Attach email to your login</h3>
        Enter your Mail:<br/><input type="text" name="mail"><br/>
        <input type="submit" />

        <c:if test="${param.error ne null}">
            <p>Wrong login or password!</p>
        </c:if>

    </form>
</div>
</body>
</html>