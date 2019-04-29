<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Add User</title>
</head>
<body>
<center>
    <form action="doAddUser" method="post">
        <table>
            <tr>
                <td>name:</td>
                <td><input type="text" name="name" value="user1"></td>
            </tr>
            <tr>
                <td>pass:</td>
                <td><input type="password" name="pass"></td>
            </tr>
            <tr>
                <td>phone:</td>
                <td><input type="text" name="phone" value="13333333333"></td>
            </tr>
            <tr>
                <td>email:</td>
                <td><input type="text" name="email" value="user1@simple2l.com"></td>
            </tr>
            <tr>
                <td>level:</td>
                <td><input type="text" name="level" value="3"></td>
            </tr>
            <tr>
                <td>group:</td>
                <td><input type="text" name="group" value="dev"></td>
            </tr>
            <tr>
                <td colspan="2">
                <input type="submit" name="add">
                <input type="reset" name="reset">
                </td>
            </tr>
        </table>
    </form>
</center>

</body>
</html>